package io.github.vanespb.meme_police_bot.objects.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_linking")
public class ChatLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "tg_chat_id", nullable = false)
    Long tgChatId;

    @Column(name = "vk_chat_id", nullable = false)
    Integer vkChatId;

    public ChatLink(Long tgChatId, Integer vkChatId) {
        this.tgChatId = tgChatId;
        this.vkChatId = vkChatId;
    }
}
