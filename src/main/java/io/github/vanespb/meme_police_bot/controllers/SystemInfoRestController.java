package io.github.vanespb.meme_police_bot.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class SystemInfoRestController {
    @GetMapping
    public String statusResponse() {
        return "I'm OK!";
    }
}
