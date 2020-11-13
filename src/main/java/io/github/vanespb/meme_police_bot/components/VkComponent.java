package io.github.vanespb.meme_police_bot.components;

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
import com.vk.api.sdk.objects.video.Video;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

@Component
public class VkComponent extends CallbackApiLongPoll implements Runnable{
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final GroupActor actor;
    private final TelegrammComponent tgBot;

    @Inject
    public VkComponent(@Value("${vkbot.groupId}") Integer groupId, @Value("${vkbot.groupToken}") String groupToken,
                       TelegrammComponent tgBot) {
        super(new VkApiClient(new HttpTransportClient()), new GroupActor(groupId, groupToken));
        this.tgBot = tgBot;
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
            String telegrammMessageText = text.isEmpty() ?
                    String.format("From %s", author) :
                    String.format("*%s*%n%s", author, text);
            List<MessageAttachment> attachments = message.getAttachments();
            if (attachments.isEmpty())
                tgBot.sendMessage(telegrammMessageText);
            else {
                for (MessageAttachment attachment : attachments) {
                    Photo photo = attachment.getPhoto();
                    if (photo != null) {

                        URL photoUrl = photo.getSizes().stream()
                                .max(Comparator.comparingInt(PhotoSizes::getHeight))
                                .orElse(new PhotoSizes())
                                .getUrl();
                        try {
                            tgBot.sendPhoto(telegrammMessageText, photoUrl.openStream());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                    Video video = attachment.getVideo();
                    //нужно быть доверенным приложением для получения видео
                    if (video != null && video.getFiles() != null) {
                        URL videoUrl = video.getFiles().getMp4720();
                        try {
                            tgBot.sendVideo(telegrammMessageText, videoUrl.openStream());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        vk.messages().send(actor).message(message);
    }

}
