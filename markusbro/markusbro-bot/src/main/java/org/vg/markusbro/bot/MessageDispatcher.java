package org.vg.markusbro.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.entity.UserEntity;
import org.vg.markusbro.core.repository.UserRepository;
import org.vg.markusbro.core.service.plugins.Plugin;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MessageDispatcher {

    @Autowired
    private List<Plugin> plugins;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void handle(TelegramBot bot, Message message) {
        handle(new TelegramContext(bot, message, findOrRegisterUser(message.from())));
    }

    @Transactional
    public void handle(TelegramBot bot, CallbackQuery callbackQuery) {
        handle(new TelegramContext(bot, callbackQuery, findOrRegisterUser(callbackQuery.from())));
    }

    private void handle(Context context) {
        log.info("Processing message from {}: {}", context.getUserId(), context.getMessage());

        double bestScore = 0.0;
        Plugin bestPlugin = null;

        for (Plugin plugin : plugins) {
            try {
                double score = plugin.getScore(context);
                log.debug("Plugin {} scored {}", plugin.getClass().getSimpleName(), score);
                if (score > bestScore) {
                    bestScore = score;
                    bestPlugin = plugin;
                }
            } catch (Exception e) {
                log.error("Plugin {} failed", plugin.getClass().getSimpleName(), e);
            }
        }

        if (bestPlugin != null) {
            log.info("Best plugin: {} (score {})", bestPlugin.getClass().getSimpleName(), bestScore);
            bestPlugin.handle(context);
        } else {
            context.reply("\uD83D\uDC08"); // cat emoji
        }
    }


    public UserEntity findOrRegisterUser(User user) {
        UserEntity u = userRepository.findByUserId(user.id());
        if (u == null) {
            u = userRepository.save(UserEntity.builder()
                    .userId(user.id())
                    .userNickname(user.username())
                    .accessGeneral(false)
                    .accessLlm(false)
                    .lastActive(new Date())
                    .totalMessages(1)
                    .build());
        } else {
            userRepository.incrementCounters(user.id());
        }

        return u; // as it is BEFORE counters update
    }
}
