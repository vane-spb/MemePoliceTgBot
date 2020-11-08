package io.github.vanespb.meme_police_bot;

import io.github.vanespb.meme_police_bot.controllers.VkComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@Slf4j
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@SpringBootApplication
public class MemePoliceTgBotApplication {

    public static void main(String[] args) {
        log.info("Step 0");
        ApiContextInitializer.init();
        log.info("Step 1");
        ConfigurableApplicationContext context = SpringApplication.run(MemePoliceTgBotApplication.class, args);
        log.info("Step 2");
        VkComponent vkBot = context.getBean(VkComponent.class);
        log.info("trying to start vk handler");
        vkBot.run();
    }

}
