package io.github.vanespb.meme_police_bot.components;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VkVideoDownloaderTest {
    VkVideoDownloader testDownloader;

    public static String getInfoFromTester(String message) {
        return JOptionPane.showInputDialog(message);
    }

    @BeforeEach
    void initialiseVkVideoDownloader() {
        testDownloader = new VkVideoDownloader();
    }

    @Test
    void isLoggedInTestNegative() throws IOException {
        testDownloader.loadLoginPage();
        testDownloader.setCookies();
        assertFalse(testDownloader.isLoggedIn());
    }

    @Test
    void isLoggedInTestNoCookies() {
        assertFalse(testDownloader.isLoggedIn());
    }

    @Test
    @Disabled("Requires user for 2 factor auth")
    void isLoggedInHandTest() throws IOException {
        testDownloader.loadLoginPage();
        String captchaSid1 = testDownloader.getCaptchaSid1();
        if (StringUtils.isNotBlank(captchaSid1)) {
            System.out.printf("Captcha http://vk.com/captcha.php?sid=%s needed%n", captchaSid1);
            testDownloader.setCaptcha1(getInfoFromTester("captcha1"));
        }
        testDownloader.firstStepAuthorisation(getInfoFromTester("email/phone"), getInfoFromTester("password"));
        String captchaSid2 = testDownloader.getCaptchaSid2();
        if (StringUtils.isNotBlank(captchaSid2)) {
            System.out.printf("Captcha http://vk.com/captcha.php?sid=%s needed%n", captchaSid2);
            testDownloader.setCaptcha2(getInfoFromTester("captcha1"));
        }
        testDownloader.secondAuthorisationStep(getInfoFromTester("secret code"));
        testDownloader.setCookies();
        assertTrue(testDownloader.isLoggedIn());
    }
}