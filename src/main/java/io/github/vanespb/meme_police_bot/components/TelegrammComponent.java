package io.github.vanespb.meme_police_bot.components;

import io.github.vanespb.meme_police_bot.objects.MessageDto;
import io.github.vanespb.meme_police_bot.objects.models.UserModel;
import io.github.vanespb.meme_police_bot.objects.repositories.UserRepository;
import io.github.vanespb.meme_police_bot.services.ChatLinkingService;
import lombok.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
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

    @Autowired
    UserRepository userRepository;

    private final Set<String> usersWhoWantsToChangeTheirNicknames = new HashSet<>();

    @Value("${tgbot.name}")
    private String botUsername;

    @Value("${tgbot.token}")
    private String botToken;
    @Autowired
    ChatLinkingService linkingService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (proceedCommands(message)) return;
            if (usersWhoWantsToChangeTheirNicknames.contains(message.getFrom().getUserName())) {
                changeNickname(message);
                return;
            }
            Long chatId = message.getChatId();
            if (linkingService.hasTgLinking(chatId)) {
                vkBot.sendMessage(convertTelegrammMessageToMessageDto(message), linkingService.getVkChatId(chatId));
            }
        }
    }

    private boolean proceedCommands(Message message) {
        String command = message.getText();
        if (command == null || !command.contains("/")) return false;
        if (command.matches("/setnickname.*")) {
            usersWhoWantsToChangeTheirNicknames.add(message.getFrom().getUserName());
            return true;
        }
        if (command.matches("/getchatid.*")) {
            Long chatId = message.getChatId();
            sendMessage("Current chat id " + chatId, chatId + "");
        }
        return false;
    }

    private void changeNickname(Message message) {
        String userName = message.getFrom().getUserName();
        Optional<UserModel> oneByTgNickname = userRepository.getOneByTgNickname(userName);
        UserModel userModel;
        if (oneByTgNickname.isPresent()) {
            userModel = oneByTgNickname.get();
        } else {
            userModel = new UserModel();
            userModel.setTgNickname(userName);
        }
        userModel.setName(message.getText());
        userRepository.save(userModel);
        usersWhoWantsToChangeTheirNicknames.remove(userName);
    }

    public MessageDto convertTelegrammMessageToMessageDto(Message message) {
        String userName = message.getFrom().getUserName();
        Optional<UserModel> oneByTgNickname = userRepository.getOneByTgNickname(userName);
        if (oneByTgNickname.isPresent())
            userName = oneByTgNickname.get().getName();
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
        files.addAll(getFilesFromEntities(message));
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

    private List<File> getFilesFromEntities(Message message) {
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

    public void sendMessage(String message, String chatId) {
        if (message == null || message.isEmpty()) return;
        try {
            execute(SendMessage
                    .builder()
                    .chatId(chatId)
                    .parseMode(ParseMode.HTML)
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(String message, List<String> fileURLs, String chatId) {
        try {
            switch (fileURLs.size()) {
                case 0:
                    sendMessage(message, chatId);
                    break;
                case 1:
                    String fileUrl = fileURLs.get(0);
                    if (StringUtils.containsIgnoreCase(fileUrl, ".jpg")) {
                        sendPhoto(message, fileUrl, chatId);
                        break;
                    }
                    if (StringUtils.containsIgnoreCase(fileUrl, ".mp4")) {
                        sendVideo(message, fileUrl, chatId);
                        break;
                    }
                    sendFile(message, fileUrl, chatId);
                    break;
                default:
                    sendMediaGroup(message, fileURLs, chatId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String message, String fileUrl, String chatId) throws IOException {
        InputStream inputStream = new URL(fileUrl).openStream();

        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(inputStream, FilenameUtils.getName(fileUrl)))
                .parseMode(ParseMode.HTML)
                .caption(message)
                .build();
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendVideo(String message, String fileUrl, String chatId) throws IOException {
        InputStream inputStream = new URL(fileUrl).openStream();

        SendVideo sendVideo = SendVideo.builder()
                .chatId(chatId)
                .video(new InputFile(inputStream, FilenameUtils.getName(fileUrl)))
                .parseMode(ParseMode.HTML)
                .caption(message)
                .build();
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(String message, String fileUrl, String chatId) throws IOException {
        InputStream inputStream = new URL(fileUrl).openStream();

        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
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

    protected void sendMediaGroup(String message, List<String> fileURLs, String chatId) {
        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId)
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
