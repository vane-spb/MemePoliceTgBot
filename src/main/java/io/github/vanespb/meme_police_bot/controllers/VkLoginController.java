package io.github.vanespb.meme_police_bot.controllers;

import io.github.vanespb.meme_police_bot.components.VkVideoDownloader;
import io.github.vanespb.meme_police_bot.objects.LoginModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
public class VkLoginController {
    LoginModel loginModel = new LoginModel();

    @Autowired
    VkVideoDownloader downloader;

    @GetMapping("/vk-login")
    public String get(Model model) throws IOException {
        loginModel.setFrame(downloader.loadLoginPage());
        loginModel.setCaptcha1Url(downloader.getCaptchaSid1());
        return getLoginPage(model);
    }

    @PostMapping("/vk-login/user-and-password")
    public String userAndPassword(@ModelAttribute(name = "login") String login,
                                  @ModelAttribute(name = "password") String password,
                                  Model model) throws IOException {
        loginModel.setFrame(downloader.firstStepAuthorisation(login, password));
        loginModel.setLogin(login);
        loginModel.setPassword("*******");
        loginModel.setCaptcha2Url(downloader.getCaptchaSid2());
        return getLoginPage(model);
    }

    @PostMapping("/vk-login/code")
    public String code(@ModelAttribute(name = "code") String code, Model model) throws IOException {
        loginModel.setFrame(downloader.secondAuthorisationStep(code));
        loginModel.setCode(code);
        return getLoginPage(model);
    }

    @PostMapping("/vk-login/captcha1")
    public String captcha1(@ModelAttribute(name = "captcha1") String captcha, Model model) {
        downloader.setCaptcha1(captcha);
        loginModel.setCaptcha1(captcha);
        return getLoginPage(model);
    }

    @PostMapping("/vk-login/captcha2")
    public String captcha2(@ModelAttribute(name = "captcha2") String captcha, Model model) {
        downloader.setCaptcha2(captcha);
        loginModel.setCaptcha2(captcha);
        return getLoginPage(model);
    }

    @PostMapping("/vk-login/submit")
    public String submit(Model model) {
        downloader.setTempCookiesAsActive();
        return getLoginPage(model);
    }

    private String getLoginPage(Model model) {
        model.addAttribute("login", loginModel);
        return "vk-login";
    }
}
