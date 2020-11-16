package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Getter
public class VkComponent extends CallbackApiLongPoll implements Runnable {
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final GroupActor actor;
    private final Integer conferenceId;
    private final Random random = new Random();
    @Setter
    private TelegrammComponent tgBot;
    private Message lastMessage;
    private String error;

    @Inject
    public VkComponent(@Value("${vkbot.groupId}") Integer groupId, @Value("${vkbot.groupToken}") String groupToken,
                       @Value("${vkbot.conferenceId}") Integer conferenceId) {
        super(new VkApiClient(new HttpTransportClient()), new GroupActor(groupId, groupToken));
        actor = new GroupActor(groupId, groupToken);
        this.conferenceId = conferenceId;
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage() + Arrays.toString(e.getStackTrace());
            run();
        }
    }

    @Override
    public void messageNew(Integer groupId, Message message) {
        lastMessage = message;
        try {
            String author = getAuthor(message);
            String text = message.getText();
            String telegrammMessageText = text.isEmpty() ?
                    String.format("From %s %n", author) :
                    String.format("<b>%s</b>%n%s %n", author, text);
            List<MessageAttachment> attachments = message.getAttachments();
            if (attachments.isEmpty())
                tgBot.sendMessage(telegrammMessageText);
            else {
                if (attachments.stream().anyMatch(a -> a.getType().equals(MessageAttachmentType.PHOTO)))
                    tgBot.send(telegrammMessageText, attachments.stream()
                            .filter(a -> a.getType().equals(MessageAttachmentType.PHOTO))
                            .map(this::getPhotoUrl)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                if (attachments.stream().anyMatch(a -> a.getType().equals(MessageAttachmentType.WALL)))
                    sendRepostToTg(telegrammMessageText, attachments);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRepostToTg(String telegrammMessageText, List<MessageAttachment> attachments) {
        WallpostFull wallpostFull = attachments.stream()
                .filter(a -> a.getType().equals(MessageAttachmentType.WALL))
                .findFirst().get().getWall();
        String repostText = wallpostFull.getText();
        List<String> urls = wallpostFull.getAttachments().stream()
                .filter(a -> a.getType().equals(WallpostAttachmentType.PHOTO))
                .map(a -> a.getPhoto().getSizes().stream().max(Comparator.comparingInt(PhotoSizes::getHeight)))
                .map(size -> size.get().getUrl().toString())
                .collect(Collectors.toList());
        tgBot.send(telegrammMessageText + repostText, urls);
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

    private String getPhotoUrl(MessageAttachment attachment) {
        PhotoSizes image = attachment.getPhoto().getSizes().stream()
                .max(Comparator.comparing(PhotoSizes::getHeight))
                .orElse(null);
        if (image != null)
            return image.getUrl().toString();
        else return null;
    }

    public Integer sendMessage(String message, List<File> files) {
        try {
            MessagesSendQuery sendQuery = vk.messages().send(actor)
                    .message(message)
                    .peerId(conferenceId)
                    .randomId(random.nextInt());
            for (File photo : files) {
                sendQuery = sendQuery.attachment(uploadFile(photo));
            }
            return sendQuery.execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadFile(File file) throws ClientException, ApiException {
        String uploadUrl = vk.photos().getMessagesUploadServer(actor).execute().getUploadUrl().toString();
        MessageUploadResponse uploadResponse = vk.upload()
                .photoMessage(uploadUrl, file)
                .execute();
        Photo photo = vk.photos().saveMessagesPhoto(actor, uploadResponse.getPhoto())
                .server(uploadResponse.getServer())
                .hash(uploadResponse.getHash())
                .execute().get(0);
        return String.format("photo%d_%d", photo.getOwnerId(), photo.getId());
    }

}
