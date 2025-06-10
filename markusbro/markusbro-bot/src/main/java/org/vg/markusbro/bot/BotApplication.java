package org.vg.markusbro.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.vg.markusbro"})
@EnableJpaRepositories(basePackages = "org.vg.markusbro")
@EntityScan("org.vg.markusbro")
public class BotApplication implements CommandLineRunner {

    @Autowired
    private MessageDispatcher messageDispatcher;

    @Value("${app.telegram.apiKey}")
    private String telegramApiKey;

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting bot...");
        if (StringUtils.isBlank(telegramApiKey)) {
            log.error("Please provide Telegram API key");
            return;
        }

        final TelegramBot bot = new TelegramBot(telegramApiKey);

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    if (update.message() != null) {
                        messageDispatcher.handle(bot, update.message());
                    } else {
                        log.error("Can't handle update: {}", update);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                log.error("Unexpected error: code {}: {}", e.response().errorCode(), e.response().description());
            } else {
                log.error(e.getMessage(), e);
            }
        });

        log.info("Bot started");
    }
}
