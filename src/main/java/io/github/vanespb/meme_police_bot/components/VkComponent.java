package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.users.UserXtrCounters;
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
        }
    }

    @Override
    public void messageNew(Integer groupId, Message message) {
        lastMessage = message;
        try {
            String author = getAuthor(message);
            String text = message.getText();
            String telegrammMessageText = text.isEmpty() ?
                    String.format("From %s", author) :
                    String.format("<b>%s</b>%n%s", author, text);
            List<MessageAttachment> attachments = message.getAttachments();
            if (attachments.isEmpty())
                tgBot.sendMessage(telegrammMessageText);
            else {
                tgBot.send(telegrammMessageText, attachments.stream()
                        .map(this::getPhotoUrl)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private String getPhotoUrl(MessageAttachment attachment) {
        PhotoSizes image = attachment.getPhoto().getSizes().stream()
                .max(Comparator.comparing(PhotoSizes::getHeight))
                .orElse(null);
        if (image != null)
            return image.getUrl().toString();
        else return null;
    }

    public Integer sendMessage(String message, List<File> photos) {
        try {
            return vk.messages().send(actor)
                    .message(message)
                    .peerId(conferenceId)
                    .randomId(random.nextInt())
                    .execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadFile(List<File> photos) {
//        photos.
//        vk.upload().photoMessage(vk.photos().getMessagesUploadServer(actor), file)
        return null;
    }

}
