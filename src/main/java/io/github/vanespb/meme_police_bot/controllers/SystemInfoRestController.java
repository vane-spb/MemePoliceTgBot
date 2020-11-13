package io.github.vanespb.meme_police_bot.controllers;

import io.github.vanespb.meme_police_bot.components.TelegrammComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemInfoRestController {
    @Autowired
    private TelegrammComponent tg;

    @GetMapping("/")
    public String statusResponse() {
        return "I'm OK!";
    }

    @PostMapping("send_to_tg")
    public String sendToTg(@RequestParam(value = "message") String message) {
        tg.sendMessage(message);
        return "Done!";
    }
}
