package io.github.vanespb.meme_police_bot.components;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class VkVideoDownloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";
    public static final String SITE_URL = "https://m.vk.com/";

    //technical cache
    private Map<String, String> cookies;
    private String authorisationLink;

    //login data
    @Setter
    private String email;
    @Setter
    private String password;
    @Setter
    private String code;
    @Setter
    private String captcha;

    //status
    @Getter
    private boolean loggedIn = false;
    @Getter
    private boolean firstStepPassed = false;
    @Getter
    private String captchaSid = null;


    public VkVideoDownloader(@Value("${vk-user.email}") String email,
                             @Value("${vk-user.password}") String password) throws IOException {
        this.email = email;
        this.password = password;
    }

    @PostConstruct
    public void tryToLoginOnStart() throws IOException {
        Connection.Response response = getCookies();
        if (!checkIfCaptchaNeeded(response)) {
            firstStepAuthorisation(email, password);
        }
    }

    public Connection.Response getCookies() throws IOException {
        Connection.Response response = Jsoup.connect(SITE_URL)
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .execute();
        saveActionLink(response);
        cookies = new HashMap<>(response.cookies());
        return response;
    }

    private void saveActionLink(Connection.Response loginResponse) throws IOException {
        authorisationLink = loginResponse.parse().select("form").attr("action");
    }

    private boolean checkIfCaptchaNeeded(Connection.Response response) throws IOException {
        boolean hasCaptcha = response.body().contains("name=\"captcha_key\"");
        if (hasCaptcha) {
            captchaSid = response.parse().select("input").attr("value");
            log.info("we need captcha " + captchaSid);
        } else captchaSid = null;
        return hasCaptcha;
    }

    public Connection.Response firstStepAuthorisation(String email, String password) throws IOException {
        Connection.Response loginResponse = Jsoup.connect(authorisationLink)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .data("email", email)
                .data("pass", password)
                .method(Connection.Method.POST)
                .execute();

        saveActionLink(loginResponse);
        cookies = loginResponse.cookies();
        firstStepPassed = true;
        return loginResponse;
    }


    public Connection.Response secondAuthorisationStep(String code) throws IOException {
        log.info("Second auth step on " + SITE_URL + authorisationLink);
        this.code = code;
        Connection.Response response = Jsoup.connect(SITE_URL + authorisationLink)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .data("code", code)
                .data("remember", "1")
                .method(Connection.Method.POST)
                .execute();
        if (!checkIfCaptchaNeeded(response)) loggedIn = true;
        return response;
    }

    public Connection.Response proceedCaptcha(String code) throws IOException {
        if (captchaSid == null) return null;
        Connection.Response response = Jsoup.connect(SITE_URL + authorisationLink)
                .userAgent(USER_AGENT)
                .referrer(SITE_URL + authorisationLink)
                .cookies(cookies)
                .followRedirects(true)
                .data("captcha_sid", captchaSid)
                .data("code", this.code)
                .data("remember", "1")
                .data("captcha_key", code)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        cookies = new HashMap<>(response.cookies());
        checkIfCaptchaNeeded(response);
        loggedIn = true;
        return response;
    }

    public String getVideoUrl(String video) throws IOException {
        if (!loggedIn) return null;
        String videoUrl = SITE_URL + video;

        Document document = Jsoup.connect(videoUrl)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .get();

        return document.select("video").select("source").get(1).attr("src");
    }

    public String execute() throws IOException {
        if (authorisationLink == null)
            tryToLoginOnStart();
        if (captchaSid != null && captcha == null)
            return "captcha needed";
        if (!loggedIn && code == null)
            return "code needed";
        if (!firstStepPassed)
            firstStepAuthorisation(email, password);
        if (captcha == null)
            secondAuthorisationStep(code);
        else
            proceedCaptcha(captcha);
        if (captcha != null)
            return "captcha needed";
        return "done!";
    }
}
