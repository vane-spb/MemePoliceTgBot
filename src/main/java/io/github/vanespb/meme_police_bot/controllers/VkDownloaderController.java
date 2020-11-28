package io.github.vanespb.meme_police_bot.controllers;

import io.github.vanespb.meme_police_bot.components.VkVideoDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/downloader")
public class VkDownloaderController {
    @Autowired
    VkVideoDownloader vkVideoDownloader;

    @PostMapping
    public String configuration(@RequestParam String code) {
        if (vkVideoDownloader.isLoggedIn()) return "everything ok, i did not need codes";
        try {
            if (vkVideoDownloader.getCaptchaSid() == null)
                vkVideoDownloader.secondAuthorisationStep(code);
            else
                vkVideoDownloader.proceedCaptcha(code);
            if (vkVideoDownloader.getCaptchaSid() == null)
                return "logged in successfully";
            else
                return String.format("Now we need captcha, look at http://vk.com/captcha.php?sid=%s", vkVideoDownloader.getCaptchaSid());
        } catch (IOException exception) {
            exception.printStackTrace();
            return "error on login";
        }

    }

    @GetMapping
    public String status() {
        if (vkVideoDownloader.isLoggedIn())
            return "Vk downloader is ready";
        else
            return "please give me secret code";
    }

    @PutMapping
    public String login(@RequestParam String email,
                        @RequestParam String password) throws IOException {
        return vkVideoDownloader.firstStepAuthorisation(email, password);
    }
}
