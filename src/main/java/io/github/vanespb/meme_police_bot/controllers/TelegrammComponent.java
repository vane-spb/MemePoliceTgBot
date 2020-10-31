package io.github.vanespb.meme_police_bot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegrammComponent extends TelegramLongPollingBot {
    static {
        ApiContextInitializer.init();
    }

    @Autowired
    VkComponent vkBot;

    @Value("${tgbot.name}")
    private String botUsername;

    @Value("${tgbot.token}")
    private String botToken;

    @Value("${tgbot.channel}")
    private Long channelId;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.getChatId().equals(channelId)) {
            String username = message.getForwardSenderName();
            String text = message.getText();
            vkBot.sendMessage(String.format("%s сказал в Telegramm: %n%s", username, text));
        }
    }

    public void sendMessage(String message) {
        try {
            execute(new SendMessage().setChatId(channelId)
                    .setText(message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
