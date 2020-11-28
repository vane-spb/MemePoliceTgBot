package io.github.vanespb.meme_police_bot.components;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class VkVideoDownloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    public static final String SITE_URL = "https://m.vk.com/";
    @Getter
    private boolean loggedIn = false;
    private Map<String, String> cookies;
    private String secondAuthorisationStepLink;

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
        Connection.Response loginSecondStepResponse = Jsoup.connect(SITE_URL + secondAuthorisationStepLink)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .followRedirects(true)
                .data("code", code)
                .method(Connection.Method.POST)
                .execute();
        cookies = loginSecondStepResponse.cookies();
        loggedIn = true;
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

}
