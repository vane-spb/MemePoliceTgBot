package io.github.vanespb.meme_police_bot.controllers;

import io.github.vanespb.meme_police_bot.components.VkVideoDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class VkDownloaderController {
    @Autowired
    VkVideoDownloader vkVideoDownloader;

    @PostMapping("/d_execute")
    public String execute() throws IOException {
        return vkVideoDownloader.execute();
    }

    @GetMapping("/downloader")
    public String status() {
        return String.format("logged = %s, captcha = http://vk.com/captcha.php?sid=%s",
                vkVideoDownloader.isLoggedIn(),
                vkVideoDownloader.getCaptchaSid());
    }

    @PutMapping("/ep")
    public String setEmailAndPassword(@RequestParam String email,
                                      @RequestParam String password) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        if (email != null) {
            vkVideoDownloader.setEmail(email);
            stringBuilder.append("set email\n");
        }
        if (password != null) {
            vkVideoDownloader.setPassword(password);
            stringBuilder.append("set password\n");
        }
        stringBuilder.append("done!");
        return stringBuilder.toString();
    }

    @PutMapping("/code")
    public String setCode(@RequestParam String code) throws IOException {
        vkVideoDownloader.setCode(code);
        return "done!";
    }

    @PutMapping("/captcha")
    public String setCaptcha(@RequestParam String captcha) throws IOException {
        vkVideoDownloader.setCaptcha(captcha);
        return "done!";
    }
}
