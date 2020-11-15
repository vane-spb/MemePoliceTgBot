package io.github.vanespb.meme_police_bot.controllers;

import com.vk.api.sdk.objects.messages.Message;
import io.github.vanespb.meme_police_bot.components.TelegrammComponent;
import io.github.vanespb.meme_police_bot.components.VkComponent;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class SystemInfoRestController {
    @Autowired
    private TelegrammComponent tg;
    @Autowired
    private VkComponent vk;

    @GetMapping("/")
    public String getStatus() {
        return "I'm OK!";
    }

    @GetMapping("vk/message")
    public Message getVkMessage() {
        return vk.getLastMessage();
    }

    @GetMapping("vk")
    public String getVk() {
        return new JSONObject()
                .put("token", Objects.nonNull(vk.getActor().getAccessToken()))
                .put("group_id", vk.getActor().getGroupId())
                .put("id", vk.getActor().getId())
                .put("vk-version", vk.getVk().getVersion())
                .put("error", vk.getError())
                .put("tgBot", vk.getTgBot().getBotUsername())
                .put("last_message", vk.getLastMessage()).toString();
    }

    @PostMapping("send_to_vk")
    public String sendToVk(@RequestParam(value = "message") String message, @RequestParam(value = "content") String url) {
        vk.sendMessage(message, null);
        return "Done!";
    }

    @PostMapping("send_to_tg")
    public String sendToTg(@RequestParam(value = "message") String message) {
        tg.sendMessage(message);
        return "Done!";
    }
}
