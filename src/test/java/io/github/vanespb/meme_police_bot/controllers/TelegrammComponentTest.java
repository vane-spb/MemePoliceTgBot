package io.github.vanespb.meme_police_bot.controllers;

import org.junit.jupiter.api.Test;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegrammComponentTest {

    @Test
    void onUpdateReceived() {
        long startTime = currentTimeMillis();
        long experimentDuration = 5 * 60 * 1000;

        TelegrammComponent telegrammComponent = TelegrammComponent.builder()
                .botToken("")
                .botUsername("")
                .channelId(0L)
                .build();

        while (currentTimeMillis() - startTime < experimentDuration) {

        }

    }

    @Test
    void sendMessage() {
    }

    @Test
    void sendPhoto() {
    }

    @Test
    void sendVideo() {
    }

    @Test
    void sendMediaGroup() {
    }

    @Test
    void getBotToken() {
        String expected = "someToken";

        TelegrammComponent telegrammComponent = TelegrammComponent.builder().botToken(expected).build();
        assertEquals(expected, telegrammComponent.getBotToken());
    }

    @Test
    void getBotUsername() {
        String expected = "someBotName";

        TelegrammComponent telegrammComponent = TelegrammComponent.builder().botUsername(expected).build();
        assertEquals(expected, telegrammComponent.getBotUsername());
    }
}