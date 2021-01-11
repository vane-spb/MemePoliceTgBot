package io.github.vanespb.meme_police_bot.services;

import io.github.vanespb.meme_police_bot.objects.models.ChatLink;
import io.github.vanespb.meme_police_bot.objects.repositories.ChatLinksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatLinkingService {
    @Autowired
    private ChatLinksRepository repository;

    private Map<Long, Integer> tgToVkMap;
    private Map<Integer, Long> vkToTgMap;

    @PostConstruct
    private void loadMaps() {
        List<ChatLink> all = repository.findAll();
        tgToVkMap = all.stream().collect(Collectors.toMap(ChatLink::getTgChatId, ChatLink::getVkChatId));
        vkToTgMap = all.stream().collect(Collectors.toMap(ChatLink::getVkChatId, ChatLink::getTgChatId));
    }

    public Integer getVkChatId(Long tgChatId) {
        return tgToVkMap.get(tgChatId);
    }

    public Long getTgChatId(Integer vkChatId) {
        return vkToTgMap.get(vkChatId);
    }

    public boolean hasTgLinking(Long tgChatId) {
        return tgToVkMap.containsKey(tgChatId);
    }

    public boolean hasVkLinking(Integer vkChatId) {
        return vkToTgMap.containsKey(vkChatId);
    }

    public boolean createNewLinking(Integer peerId, String tgChatId) {
        try {
            long tgId = Long.parseLong(tgChatId);
            repository.save(new ChatLink(tgId, peerId));
            loadMaps();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
