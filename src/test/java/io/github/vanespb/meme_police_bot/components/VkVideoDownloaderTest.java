package io.github.vanespb.meme_police_bot.components;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class VkVideoDownloaderTest {

    public static VkVideoDownloader getVkVideoDownloader() {
        Map<String, String> env = System.getenv();
        try {
            VkVideoDownloader downloader = new VkVideoDownloader(env.get("VK_USER_EMAIL"), env.get("VK_USER_PASSWORD"));
            String code = JOptionPane.showInputDialog("Enter code");
            downloader.secondAuthorisationStep(code);
            if (downloader.getCaptchaSid() != null) {
                File captchaImage = new File("captcha.jpg");
                captchaImage.deleteOnExit();
                String captchaUrl = String.format("http://vk.com/captcha.php?sid=%s", downloader.getCaptchaSid());
                log.info(captchaUrl);
                downloader.proceedCaptcha(JOptionPane.showInputDialog("Enter code"));
            }
            return downloader;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Test
    void getVideoUrl() throws IOException {
        VkVideoDownloader downloader = getVkVideoDownloader();

        String result = downloader.getVideoUrl("video31224679_456239541");
        log.info(result);
        assertNotNull(result);
    }
}