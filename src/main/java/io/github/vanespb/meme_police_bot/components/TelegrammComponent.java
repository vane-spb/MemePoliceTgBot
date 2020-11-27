package io.github.vanespb.meme_police_bot.components;

import io.github.vanespb.meme_police_bot.objects.MessageDto;
import lombok.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
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
                vkBot.sendMessage(convertTelegrammMessageToMessageDto(message));
            }
        }
    }

    public MessageDto convertTelegrammMessageToMessageDto(Message message) {
        String userName = message.getFrom().getUserName();
        MessageDto messageDto = MessageDto.builder()
                .author(userName)
                .text(message.getText())
                .mediaFiles(getFilesFromMessage(message))
                .build();

        if (message.getForwardFromChat() != null || message.getForwardFrom() != null) {
            MessageDto.MessageDtoBuilder messageDtoBuilder = MessageDto.builder();
            if (message.getForwardFromChat() != null)
                messageDtoBuilder = messageDtoBuilder.author(message.getForwardFromChat().getTitle());
            if (message.getForwardFrom() != null)
                messageDtoBuilder = messageDtoBuilder.author(message.getForwardFrom().getUserName());
            List<String> captions = new ArrayList<>();
            if (message.getCaption() != null)
                captions.add(message.getCaption());
            if (message.getCaptionEntities() != null)
                captions.addAll(message.getCaptionEntities().stream()
                        .map(MessageEntity::getText)
                        .collect(Collectors.toList()));
            messageDtoBuilder = messageDtoBuilder.text(String.join("\n", captions));
            messageDto.setReply(messageDtoBuilder.build());
        }

        return messageDto;
    }

    public List<File> getFilesFromMessage(Message message) {
        List<File> files = new ArrayList<>();
        files.addAll(getFilesFromEntites(message));
        files.addAll(getFilesFromPhoto(message));
        files.addAll(getVideosFromMessage(message));
        return files;
    }

    @SneakyThrows
    private List<File> getFilesFromPhoto(Message message) {
        List<File> files = new ArrayList<>();
        List<PhotoSize> photo = message.getPhoto();
        if (photo != null) {
            PhotoSize photoSize = photo.stream()
                    .max(Comparator.comparingInt(PhotoSize::getHeight))
                    .orElse(null);
            if (photoSize != null) {
                GetFile getFile = new GetFile();
                getFile.setFileId(photoSize.getFileId());
                String filePath = execute(getFile).getFilePath();
                File file = downloadFile(filePath, getOutputFile(".jpg"));
                files.add(file);
            }
        }
        return files;
    }

    private List<File> getFilesFromEntites(Message message) {
        if (message.hasEntities())
            return message.getEntities().stream()
                    .map(e -> {
                        try {
                            InputStream inputStream = new URL(e.getUrl()).openStream();
                            File file = getOutputFile("i.jpg");
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
        else return new ArrayList<>();
    }

    //TODO: proceed big files
    @SneakyThrows
    private List<File> getVideosFromMessage(Message message) {
        List<File> files = new ArrayList<>();
        Video video = message.getVideo();
        if (video != null) {
            GetFile getFile = new GetFile();
            getFile.setFileId(video.getFileId());
            String filePath = execute(getFile).getFilePath();
            File file = downloadFile(filePath, getOutputFile("video.mp4"));
            files.add(file);
        }
        return files;
    }

    private File getOutputFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf("."));
        File file = new File(UUID.randomUUID().toString() + extension);
        file.deleteOnExit();
        return file;
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
