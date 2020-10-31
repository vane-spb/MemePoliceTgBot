package io.github.vanespb.meme_police_bot.controllers;

import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

@Component
public class VkComponent extends CallbackApiLongPoll {
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final GroupActor actor;
    @Autowired
    private TelegrammComponent tgBot;

    @Inject
    public VkComponent(@Value("${vkbot.groupId}") Integer groupId, @Value("${vkbot.groupToken}") String groupToken) {
        super(new VkApiClient(new HttpTransportClient()), new GroupActor(groupId, groupToken));
        actor = new GroupActor(groupId, groupToken);
    }

    @Async
    @SneakyThrows
    @Override
    public void run() {
        super.run();
    }

    @Override
    public void messageNew(Integer groupId, Message message) {
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
        String text = message.getText();
        tgBot.sendMessage(String.format("%s sad in vk conference: %n%s", author, text));
        List<MessageAttachment> attachments = message.getAttachments();
        if (attachments != null) {
            for (MessageAttachment attachment : attachments) {
                Photo photo = attachment.getPhoto();
                URL url = photo.getSizes().stream()
                        .max(Comparator.comparingInt(PhotoSizes::getHeight))
                        .orElse(new PhotoSizes())
                        .getUrl();

                tgBot.sendMessage(url.toString());
            }
        }
    }

    public void sendMessage(String message) {
        vk.messages().send(actor).message(message);
    }

}
