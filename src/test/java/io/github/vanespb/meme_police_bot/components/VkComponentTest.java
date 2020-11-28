package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import io.github.vanespb.meme_police_bot.objects.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        files.add(new File("src/test/resources/picture.jpg"));

        Integer actual = vkComponent.sendMessage(message, files);
        log.info(actual + "");
        assertNotNull(actual);
    }

    @Test
    void uploadFile() throws ClientException, ApiException {
        VkComponent vkComponent = getVkComponent();

        File file = new File("src/test/resources/picture.jpg");

        String actual = vkComponent.uploadFile(file);
        log.info(actual);
        assertFalse(actual.matches("photo[0-9]*_[0-9]*"));
    }

    @Test
    void sendMessageObject() {
        VkComponent vkComponent = getVkComponent();

        File picture = new File("src/test/resources/testFiles/picture.jpg");
        File video = new File("src/test/resources/testFiles/video.mp4");
        File file = new File("src/test/resources/testFiles/file.torrent");
        File animation = new File("src/test/resources/testFiles/animation.gif");

        MessageDto message = MessageDto.builder()
                .author("Тестовый секретный агент")
                .text("Посылаю Вам экстренное сообщение")
                .mediaFiles(Arrays.asList(picture, file, video, animation))
                .build();

        assertDoesNotThrow(() -> vkComponent.sendMessage(message));
    }


    private VkComponent getVkComponent() {
        Map<String, String> env = System.getenv();
        return new VkComponent(
                Integer.parseInt(env.get("VK_GROUP_ID")),
                env.get("VK_GROUP_TOKEN"),
                Integer.parseInt(env.get("VK_CONFERENCE_ID")),
                VkUserComponentTest.getVkUserComponent(),
                VkVideoDownloaderTest.getVkVideoDownloader());
    }
}