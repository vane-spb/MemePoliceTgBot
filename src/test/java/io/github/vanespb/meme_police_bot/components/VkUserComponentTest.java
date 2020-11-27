package io.github.vanespb.meme_police_bot.components;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VkUserComponentTest {

    public static VkUserComponent getVkUserComponent() {
        Map<String, String> env = System.getenv();
        return new VkUserComponent(
                Integer.parseInt(env.get("VK_USER_ID")),
                Integer.parseInt(env.get("VK_GROUP_ID")),
                env.get("VK_USER_TOKEN")
        );
    }

    @Test
    void uploadVideo() {
        assertDoesNotThrow(() -> getVkUserComponent().uploadVideo(new File("src/test/resources/testFiles/video.mp4")));
    }


}