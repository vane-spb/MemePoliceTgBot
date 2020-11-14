package io.github.vanespb.meme_police_bot.components;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class VkComponentTest {

    @Test
    void sendMessage() {
        Map<String, String> env = System.getenv();
        VkComponent vkComponent = new VkComponent(Integer.parseInt(env.get("VK_GROUP_ID")), env.get("VK_GROUP_TOKEN"),
                Integer.parseInt(env.get("VK_CONFERENCE_ID")));
        Integer messageId = vkComponent.sendMessage("Я делаю обратную связь", new ArrayList<>());
        System.out.println(messageId);
        assertNotNull(messageId);
    }
}