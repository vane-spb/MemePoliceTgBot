package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class VkComponentTest {

    @Test
    void sendMessage() {
        VkComponent vkComponent = getVkComponent();
        Integer messageId = vkComponent.sendMessage("Я делаю обратную связь", new ArrayList<>());
        System.out.println(messageId);
        assertNotNull(messageId);
    }

    @Test
    void testSendMessage() {
        VkComponent vkComponent = getVkComponent();

        String message = "Тестовое сообщение";
        List<File> files = new ArrayList<>();
        files.add(new File("src/test/resources/3. Леопард.jpg"));

        Integer actual = vkComponent.sendMessage(message, files);
        log.info(actual + "");
        assertNotNull(actual);
    }

    @Test
    void uploadFile() throws ClientException, ApiException {
        VkComponent vkComponent = getVkComponent();

        File file = new File("src/test/resources/3. Леопард.jpg");

        String actual = vkComponent.uploadFile(file);
        log.info(actual);
        assertFalse(actual.matches("photo[0-9]*_[0-9]*"));
    }


    private VkComponent getVkComponent() {
        Map<String, String> env = System.getenv();
        return new VkComponent(Integer.parseInt(env.get("VK_GROUP_ID")), env.get("VK_GROUP_TOKEN"),
                Integer.parseInt(env.get("VK_CONFERENCE_ID")));
    }
}