package io.github.vanespb.meme_police_bot.objects.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cookies")
public class CookieModel {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column
    @Size(min = 1, max = 100)
    String key;

    @Column
    String value;

    public CookieModel(@Size(min = 1, max = 100) String key, String value) {
        this.key = key;
        this.value = value;
    }
}
