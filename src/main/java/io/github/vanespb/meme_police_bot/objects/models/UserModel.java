package io.github.vanespb.meme_police_bot.objects.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, updatable = false)
    Integer id;

    @Column(name = "name", unique = true, nullable = false)
    String name;

    @Column(name = "tg_nickname", unique = true, nullable = false)
    String tgNickname;

    @Column(name = "vk_id", unique = true)
    Integer vkId;
}
