package io.github.vanespb.meme_police_bot.components;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class VkVideoDownloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";
    public static final String SITE_URL = "https://m.vk.com/";
    @Getter
    private boolean loggedIn = false;
    private Map<String, String> cookies;

    private String secondAuthorisationStepLink;
    private String code;
    @Getter
    private String captchaSid = null;


    public VkVideoDownloader(@Value("${vk-user.email}") String email,
                             @Value("${vk-user.password}") String password) throws IOException {
        firstStepAuthorisation(email, password);
    }

    public void firstStepAuthorisation(String email, String password) throws IOException {
        Connection.Response loginForm = Jsoup.connect(SITE_URL)
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .execute();
        String loginActionLink = loginForm.parse().select("form").attr("action");
        Connection.Response loginResponse = Jsoup.connect(loginActionLink)
                .userAgent(USER_AGENT)
                .cookies(loginForm.cookies())
                .followRedirects(true)
                .data("email", email)
                .data("pass", password)
                .method(Connection.Method.POST)
                .execute();

        secondAuthorisationStepLink = loginResponse.parse().select("form").attr("action");
        cookies = loginResponse.cookies();
    }

    public void secondAuthorisationStep(String code) throws IOException {
        this.code = code;
        Connection.Response response = Jsoup.connect(SITE_URL + secondAuthorisationStepLink)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .data("code", code)
                .data("remember", "1")
                .method(Connection.Method.POST)
                .execute();
        checkIfCaptchaNeeded(response);
    }

    private void checkIfCaptchaNeeded(Connection.Response response) throws IOException {
        if (response.url().toString().equals("https://m.vk.com/feed")) {
            loggedIn = true;
            captchaSid = null;
        } else {
            captchaSid = response.parse().select("input").attr("value");
        }
    }

    public void proceedCaptcha(String code) throws IOException {
        if (captchaSid == null) return;
        Connection.Response response = Jsoup.connect(SITE_URL + secondAuthorisationStepLink)
                .userAgent(USER_AGENT)
                .referrer(SITE_URL + secondAuthorisationStepLink)
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
    }

    public String getVideoUrl(String video) throws IOException {
        if (!loggedIn) return null;
        String videoUrl = SITE_URL + video;

        Document document = Jsoup.connect(videoUrl)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .get();

        return document.select("video").select("source").first().attr("src");
    }

}
