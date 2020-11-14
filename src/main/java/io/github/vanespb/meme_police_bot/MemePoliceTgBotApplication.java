package io.github.vanespb.meme_police_bot;

import io.github.vanespb.meme_police_bot.components.TelegrammComponent;
import io.github.vanespb.meme_police_bot.components.VkComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableAsync(proxyTargetClass = true)
public class MemePoliceTgBotApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext context = SpringApplication.run(MemePoliceTgBotApplication.class, args);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");

        //initialise telegramm bot
        TelegrammComponent telegrammComponent = context.getBean(TelegrammComponent.class);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegrammComponent);

        //initialise vk bot
        VkComponent vkComponent = context.getBean(VkComponent.class);
        vkComponent.setTgBot(telegrammComponent);
        taskExecutor.execute(vkComponent);
    }

}
