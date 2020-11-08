package io.github.vanespb.meme_police_bot;

import io.github.vanespb.meme_police_bot.controllers.VkComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@SpringBootApplication
public class MemePoliceTgBotApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        ConfigurableApplicationContext context = SpringApplication.run(MemePoliceTgBotApplication.class, args);
        VkComponent vkBot = context.getBean(VkComponent.class);
        vkBot.run();
    }

}
