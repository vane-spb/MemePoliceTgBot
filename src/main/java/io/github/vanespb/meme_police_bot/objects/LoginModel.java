package io.github.vanespb.meme_police_bot.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginModel {
    String isLoggedIn;
    String login;
    String password;
    String code;
    String captcha1;
    String captcha2;
    String captcha1Url;
    String captcha2Url;
    String frame;
}
