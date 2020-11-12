package io.github.vanespb.meme_police_bot;


import io.github.vanespb.meme_police_bot.controllers.VkComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@EnableConfigurationProperties
@EnableAsync(proxyTargetClass = true)
public class MemePoliceTgBotApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MemePoliceTgBotApplication.class, args);
    }

}
