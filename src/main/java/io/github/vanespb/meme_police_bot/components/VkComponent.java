package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.docs.responses.DocUploadResponse;
import com.vk.api.sdk.objects.docs.responses.SaveResponse;
import com.vk.api.sdk.objects.messages.ForeignMessage;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import io.github.vanespb.meme_police_bot.objects.MessageDto;
import io.github.vanespb.meme_police_bot.objects.exceptions.VideoDownloadingException;
import io.github.vanespb.meme_police_bot.services.ChatLinkingService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Getter
public class VkComponent extends CallbackApiLongPoll implements Runnable {
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final GroupActor actor;
    private final VkUserComponent userComponent;
    private final VkVideoDownloader videoDownloader;
    private final ChatLinkingService linkingService;
    private final Random random = new Random();
    @Setter
    private TelegrammComponent tgBot;
    private Message lastMessage;
    private Exception error;

    @Inject
    public VkComponent(@Value("${vkbot.groupId}") Integer groupId,
                       @Value("${vkbot.groupToken}") String groupToken,
                       ChatLinkingService linkingService,
                       VkUserComponent userComponent,
                       VkVideoDownloader videoDownloader) {
        super(new VkApiClient(new HttpTransportClient()), new GroupActor(groupId, groupToken));
        actor = new GroupActor(groupId, groupToken);
        this.linkingService = linkingService;
        this.userComponent = userComponent;
        this.videoDownloader = videoDownloader;
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            e.printStackTrace();
            error = e;
            run();
        }
    }

    @Override
    public void messageNew(Integer groupId, Message message) {
        lastMessage = message;
        if (proceedCommands(message)) return;
        Integer peerId = message.getPeerId();
        if (linkingService.hasVkLinking(peerId))
            try {
                String author = getAuthor(message);
                String text = message.getText();
                String telegrammMessageText = text.isEmpty() ?
                        String.format("From %s %n", author) :
                        String.format("<b>%s</b>%n%s %n", author, text);
                List<MessageAttachment> attachments = message.getAttachments();
                attachments.addAll(message.getFwdMessages().stream()
                        .map(ForeignMessage::getAttachments)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
                List<String> fileURLs = attachments.stream()
                        .map(this::getMessageAttachmentsUrl)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                tgBot.send(telegrammMessageText, fileURLs, "" + linkingService.getTgChatId(peerId));
                if (attachments.stream().anyMatch(a -> a.getType().equals(MessageAttachmentType.WALL)))
                    sendRepostToTg(telegrammMessageText, attachments, peerId);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private boolean proceedCommands(Message message) {
        String command = message.getText();
        Integer peerId = message.getPeerId();
        if (!command.contains("/")) return false;
        if (command.matches("/wire -*[0-9]*")) {
            String tgChatId = command.split(" ")[1];
            return linkingService.createNewLinking(peerId, tgChatId);
        }
        if (command.matches("/peerid.*")) {
            sendMessage(new MessageDto("Current peerId = " + peerId), peerId);
            return true;
        }
        return false;
    }

    private String getMessageAttachmentsUrl(MessageAttachment attachment) {
        switch (attachment.getType()) {
            case PHOTO:
                return getPhotoUrl(attachment.getPhoto());
            case VIDEO:
                return getVideoUrl(attachment.getVideo());
            default:
                return null;
        }
    }

    //TODO: work with case when attachments number > 10
    public void sendMessage(MessageDto message, Integer chatId) {
        try {
            MessagesSendQuery sendQuery = vk.messages().send(actor)
                    .message(message.getFullText())
                    .attachment(message.getAllMediaFiles().stream().map(f -> {
                        try {
                            return uploadFile(f, chatId);
                        } catch (ClientException | ApiException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.joining(",")))
                    .peerId(chatId)
                    .randomId(random.nextInt());
            sendQuery.execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            error = e;
        }
    }

    private void sendRepostToTg(String telegrammMessageText, List<MessageAttachment> attachments, int peerId) {
        Optional<MessageAttachment> messageAttachment = attachments.stream()
                .filter(a -> a.getType().equals(MessageAttachmentType.WALL))
                .findFirst();
        if (messageAttachment.isPresent()) {
            WallpostFull wallpostFull = messageAttachment.get().getWall();
            String repostText = wallpostFull.getText();
            List<String> urls = wallpostFull.getAttachments().stream()
                    .map(this::getWallpostAttachmentsUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            tgBot.send(telegrammMessageText + repostText, urls, "" + linkingService.getTgChatId(peerId));
        }
    }

    private String getWallpostAttachmentsUrl(WallpostAttachment attachment) {
        switch (attachment.getType()) {
            case PHOTO:
                return getPhotoUrl(attachment.getPhoto());
            case VIDEO:
                return getVideoUrl(attachment.getVideo());
            default:
                return null;
        }
    }

    private String getAuthor(Message message) {
        String author = "Anonimous";
        try {
            List<UserXtrCounters> execute = vk.users().get(actor).userIds(message.getFromId() + "").execute();
            if (execute != null && !execute.isEmpty()) {
                UserXtrCounters user = execute.get(0);
                author = String.format("%s %s", user.getFirstName(), user.getLastName());
            }
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        return author;
    }

    private String getPhotoUrl(Photo photo) {
        PhotoSizes image = photo.getSizes().stream()
                .max(Comparator.comparing(PhotoSizes::getHeight))
                .orElse(null);
        if (image != null)
            return image.getUrl().toString();
        else return null;
    }

    private String getVideoUrl(Video video) {
        String videoId = String.format("video%s_%s", video.getOwnerId(), video.getId());
        try {
            return videoDownloader.getVideoUrl(videoId);
        } catch (IOException | VideoDownloadingException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public String uploadPhoto(File file) throws ClientException, ApiException {
        String uploadUrl = vk.photos().getMessagesUploadServer(actor).execute().getUploadUrl().toString();
        MessageUploadResponse uploadResponse = vk.upload()
                .photoMessage(uploadUrl, file)
                .execute();
        Photo photo = vk.photos().saveMessagesPhoto(actor, uploadResponse.getPhoto())
                .server(uploadResponse.getServer())
                .hash(uploadResponse.getHash())
                .execute().get(0);
        delete(file);
        return String.format("photo%d_%d", photo.getOwnerId(), photo.getId());
    }

    public String uploadDocument(File file, Integer chatId) throws ClientException, ApiException {
        String uploadUrl = vk.docs().getMessagesUploadServer(actor).peerId(chatId).execute().getUploadUrl().toString();
        DocUploadResponse uploadResponse = vk.upload()
                .doc(uploadUrl, file)
                .execute();
        SaveResponse document = vk.docs().save(actor, uploadResponse.getFile())
                .execute();
        delete(file);
        return String.format("doc%d_%d", document.getDoc().getOwnerId(), document.getDoc().getId());
    }

    public String uploadFile(File file, Integer chatId) throws ClientException, ApiException {
        if (file.getName().contains(".jpg")) {
            return uploadPhoto(file);
        }
        if (StringUtils.containsIgnoreCase(file.getName(), ".MP4")) {
            return uploadVideo(file);
        }
        delete(file);
        return uploadDocument(file, chatId);
    }

    private String uploadVideo(File file) throws ClientException, ApiException {
        String video = userComponent.uploadVideo(file);
        delete(file);
        return video;
    }

    private void delete(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

