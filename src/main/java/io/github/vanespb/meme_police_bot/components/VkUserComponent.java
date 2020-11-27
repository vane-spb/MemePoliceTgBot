package io.github.vanespb.meme_police_bot.components;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.responses.VideoUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

@Component
public class VkUserComponent {
    private final VkApiClient vk = new VkApiClient(new HttpTransportClient());
    private final Integer userId;
    private final Integer groupId;
    private final UserActor actor;

    public VkUserComponent(@Value("${vk-user.id}") Integer userId,
                           @Value("${vkbot.groupId}") Integer groupId,
                           @Value("${vk-user.token}") String userToken) {
        this.userId = userId;
        this.groupId = groupId;
        actor = new UserActor(userId, userToken);
    }


    public String uploadVideo(File file) throws ClientException, ApiException {
        URL uploadUrl = vk.videos().save(actor)
                .groupId(groupId)
                .execute().getUploadUrl();
        VideoUploadResponse uploadResponse = vk.upload().video(uploadUrl.toString(), file).execute();
        file.delete();
        return String.format("video-%s_%s", groupId, uploadResponse.getVideoId());
    }

    //TODO: video downloading
}
