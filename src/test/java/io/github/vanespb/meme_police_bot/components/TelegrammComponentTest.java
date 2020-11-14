package io.github.vanespb.meme_police_bot.components;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TelegrammComponentTest {

    @Test
    void sendMessage() {
    }

    @Test
    void sendMediaGroup() {
        TelegrammComponent tg = new TelegrammComponent();
        Map<String, String> env = System.getenv();
        tg.setBotToken(env.get("TG_BOT_TOKEN"));
        tg.setBotUsername("MEME_POLICE_BOT");
        tg.setChannelId("-1001261743202");

        List<String> urls = new ArrayList<>();
        urls.add("https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/Justinian_mosaik_ravenna.jpg/175px-Justinian_mosaik_ravenna.jpg");
        tg.sendMediaGroup("unit testing", urls);
    }
}