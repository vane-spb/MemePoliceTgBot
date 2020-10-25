package io.github.vanespb.meme_police_bot.controllers;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;


@Component
public class VkComponent {
    @Value("${vkbot.token}")
    private String token;
    @Value("${vkbot.appid}")
    private Integer appid;
    @Value("${vkbot.groupToken}")
    private String groupToken;
    @Value("${vkbot.groupId}")
    private Integer groupId;
    @Autowired
    TelegrammComponent tgBot;
    VkApiClient vk;
    GroupActor actor;
    int ts = 1;

    @PostConstruct
    public void initialise() throws ClientException, ApiException {
        TransportClient transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(groupId, groupToken);
        ts = vk.messages().getLongPollServer(actor).execute().getTs();

    }

    @Scheduled(fixedDelay = 1*1000)
    public void getMessage() throws ClientException, ApiException {
        MessagesGetLongPollHistoryQuery eventsQuery = vk.messages()
                .getLongPollHistory(actor)
                .ts(ts);

        List<Message> messages = eventsQuery
                .execute()
                .getMessages()
                .getMessages();
        ts = vk.messages().getLongPollServer(actor).execute().getTs();
        messages.forEach(m->tgBot.sendMessage(m.getBody()));
    }
}
