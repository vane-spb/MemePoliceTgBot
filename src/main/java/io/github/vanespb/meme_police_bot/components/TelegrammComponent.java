package io.github.vanespb.meme_police_bot.components;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
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

    @Value("${tgbot.channelName}")
    private String channelName;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String title = message.getChat().getTitle();
            if (title != null && title.contains(channelName)) {
                List<File> files = message.getEntities().stream()
                        .map(e -> {
                            try {
                                InputStream inputStream = new URL(e.getUrl()).openStream();
                                File file = new File("tmp.jpg");
                                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                                    IOUtils.copy(inputStream, outputStream);

                                }
                                return file;
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                vkBot.sendMessage(String.format("%s: %n%s",
                        message.getFrom().getUserName(), message.getText()), files);
            }
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

    public void send(String message, List<String> fileURLs) {
        if (fileURLs.isEmpty()) {
            sendMessage(message);
        } else {
            if (fileURLs.size() == 1) {
                try {
                    sendPhoto(message, fileURLs.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sendMediaGroup(message, fileURLs);
            }
        }
    }

    public void sendPhoto(String message, String fileUrl) throws IOException {
        InputStream inputStream = new URL(fileUrl).openStream();

        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(channelId)
                .photo(new InputFile(inputStream, "meme.jpg"))
                .parseMode(ParseMode.HTML)
                .caption(message)
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    protected void sendMediaGroup(String message, List<String> fileURLs) {
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
