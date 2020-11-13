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
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class VkComponent extends CallbackApiLongPoll implements Runnable {
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final GroupActor actor;
    @Setter
    private TelegrammComponent tgBot;

    @Inject
    public VkComponent(@Value("${vkbot.groupId}") Integer groupId, @Value("${vkbot.groupToken}") String groupToken) {
        super(new VkApiClient(new HttpTransportClient()), new GroupActor(groupId, groupToken));
        actor = new GroupActor(groupId, groupToken);
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            e.printStackTrace();
            this.run();
        }
    }

    @Override
    public void messageNew(Integer groupId, Message message) {
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
                tgBot.sendMediaGroup(telegrammMessageText, attachments.stream()
                        .map(this::getUrl)
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

    private String getUrl(MessageAttachment attachment) {
        PhotoSizes image = attachment.getPhoto().getSizes().stream()
                .max(Comparator.comparing(PhotoSizes::getHeight))
                .orElse(null);
        if (image != null)
            return image.getUrl().toString();
        else return null;
    }

    public void sendMessage(String message) {
        vk.messages().send(actor).message(message);
    }

}
