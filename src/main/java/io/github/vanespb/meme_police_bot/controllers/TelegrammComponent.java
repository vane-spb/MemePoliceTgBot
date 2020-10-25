package io.github.vanespb.meme_police_bot.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegrammComponent extends TelegramLongPollingBot {
    @Value("${tgbot.name}")
    private String botUsername;

    @Value("${tgbot.token}")
    private String botToken;

    private Long myId;

    @Override
    public void onUpdateReceived(Update update) {
        myId = update.getMessage().getChatId();
        try {
            execute(new SendMessage().setChatId(update.getMessage().getChatId())
                    .setText("Hi!"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendMessage(String message) {
        try {
            execute(new SendMessage().setChatId(myId)
                    .setText(message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
