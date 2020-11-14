package io.github.vanespb.meme_police_bot.components;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegrammComponent extends TelegramLongPollingBot {
    @Autowired
    VkComponent vkBot;

    @Value("${tgbot.name}")
    private String botUsername;

    @Value("${tgbot.token}")
    private String botToken;

    @Value("${tgbot.channel}")
    private String channelId;

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
            execute(SendMessage
                    .builder()
                    .chatId(channelId)
                    .parseMode(ParseMode.HTML)
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMediaGroup(String message, List<String> fileURLs) {
        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(channelId)
                .medias(fileURLs.stream()
                        .map(url -> getInputMedia(url, message))
                        .collect(Collectors.toList()))
                .build();
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InputMedia getInputMedia(String url, String message) {
        if (url.contains(".jpg"))
            return InputMediaPhoto.builder()
                    .caption(message)
                    .media(url)
                    .parseMode(ParseMode.HTML)
                    .build();
        if (url.contains(".mp4"))
            return InputMediaVideo.builder()
                    .caption(message)
                    .media(url)
                    .parseMode(ParseMode.HTML)
                    .build();
        if (url.contains(".gif"))
            return InputMediaAnimation.builder()
                    .caption(message)
                    .media(url)
                    .parseMode(ParseMode.HTML)
                    .build();
        return InputMediaDocument.builder()
                .caption(message)
                .media(url)
                .parseMode(ParseMode.HTML)
                .build();
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
