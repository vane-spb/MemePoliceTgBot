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
        try {
            vkVideoDownloader.secondAuthorisationStep(code);
            return "logged in successfully";
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
}
