package io.github.vanespb.meme_police_bot.components;

import io.github.vanespb.meme_police_bot.objects.exceptions.VideoDownloadingException;
import io.github.vanespb.meme_police_bot.objects.models.CookieModel;
import io.github.vanespb.meme_police_bot.objects.repositories.CookiesRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VkVideoDownloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    public static final String SITE_URL = "https://m.vk.com/";
    private Map<String, String> cookies;
    @Autowired
    protected CookiesRepository cookiesRepository;
    //login cache
    private Map<String, String> cookiesTemp;
    private String authorisationLink = null;
    @Setter
    private String captcha1 = null;
    @Setter
    private String captcha2 = null;
    @Getter
    private String captchaSid1 = null;
    @Getter
    private String captchaSid2 = null;

    @PostConstruct
    public void getCookiesFromDatabase() {
        cookies = cookiesRepository.findAll().stream()
                .collect(Collectors.toMap(CookieModel::getKey, CookieModel::getValue));
    }

    @Transactional
    public void setTempCookiesAsActive() {
        cookies = cookiesTemp;
        cookiesTemp = null;
        authorisationLink = null;
        saveCookiesToDatabase();
    }

    @Transactional
    public void saveCookiesToDatabase() {
        if (cookiesRepository == null) return;
        cookiesRepository.deleteAll();
        cookiesRepository.saveAll(cookies.entrySet().stream()
                .map(it -> new CookieModel(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));
    }

    private void updateCookies(Map<String, String> newCookies) {
        cookies.putAll(newCookies);
        saveCookiesToDatabase();
    }

    public String loadLoginPage() throws IOException {
        Connection.Response response = Jsoup.connect(SITE_URL)
                .userAgent(USER_AGENT)
                .followRedirects(true)
                .method(Connection.Method.GET)
                .execute();
        cookiesTemp = new HashMap<>(response.cookies());
        Document parse = response.parse();
        authorisationLink = parse.select("form").attr("action");
        captchaSid1 = parse.select("input").attr("value");
        return response.body();
    }

    public String firstStepAuthorisation(String email, String password) throws IOException {
        Connection connection = Jsoup.connect(authorisationLink)
                .userAgent(USER_AGENT)
                .cookies(cookiesTemp)
                .followRedirects(true)
                .data("email", email)
                .data("pass", password)
                .method(Connection.Method.POST);
        if (captchaSid1 != null && captcha2 != null)
            connection
                    .data("captcha_key", captcha1)
                    .data("captcha_sid", captchaSid1);
        Connection.Response loginResponse = connection.execute();

        authorisationLink = loginResponse.parse().select("form").attr("action");
        captchaSid2 = loginResponse.parse().select("input").attr("value");
        cookiesTemp = loginResponse.cookies();
        return loginResponse.body();
    }


    public String secondAuthorisationStep(String code) throws IOException {
        Connection connection = Jsoup.connect(SITE_URL + authorisationLink)
                .userAgent(USER_AGENT)
                .referrer(SITE_URL + authorisationLink)
                .cookies(cookiesTemp)
                .followRedirects(true)
                .data("code", code)
                .data("remember", "1")
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true);
        if (captcha2 != null && captchaSid2 != null)
            connection = connection
                    .data("captcha_key", captcha2)
                    .data("captcha_sid", captchaSid2);
        Connection.Response response = connection
                .execute();
        cookiesTemp = new HashMap<>(response.cookies());
        return response.body();
    }

    public boolean isLoggedIn() {
        try {
            Document document = Jsoup.connect(SITE_URL)
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .followRedirects(true)
                    .get();
            return document.title().contains("Новости");
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public String getVideoUrl(String video) throws IOException, VideoDownloadingException {
        if (!isLoggedIn())
            throw new VideoDownloadingException("Vk video downloading service is not logged in.");
        String videoUrl = SITE_URL + video;
        Connection.Response response = Jsoup.connect(videoUrl)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .execute();
        updateCookies(response.cookies());
        Document document = response.parse();
        return document.select("video").select("source").get(1).attr("src");
    }
}
