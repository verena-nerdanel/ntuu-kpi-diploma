package org.vg.markusbro.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Getter;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.entity.UserEntity;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class TelegramContext implements Context {

    private final TelegramBot bot;
    @Getter
    private final long userId;
    @Getter
    private final UserEntity user;
    @Getter
    private final String userName;
    @Getter
    private final String message;

    public TelegramContext(TelegramBot bot, Message message, UserEntity user) {
        this.bot = bot;
        this.userId = message.from().id();
        this.userName = message.from().username();
        this.user = user;
        this.message = message.text();
    }

    public TelegramContext(TelegramBot bot, CallbackQuery callbackQuery, UserEntity user) {
        this.bot = bot;
        this.userId = callbackQuery.from().id();
        this.userName = callbackQuery.from().username(); // TODO: check
        this.user = user;
        this.message = callbackQuery.data();
    }

    @Override
    public void reply(String text) {
        ParseMode markdown = ParseMode.HTML;
        bot.execute(new SendMessage(userId, text).parseMode(markdown));
    }

    @Override
    public void replyWithOptions(String text, List<String> options) {
        final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(new String[0]);
        keyboardMarkup.resizeKeyboard(true);

        options.forEach(value -> {
            keyboardMarkup.addRow(new KeyboardButton(value));
        });

        bot.execute(new SendMessage(userId, text).replyMarkup(keyboardMarkup));
    }

    @Override
    public void removeOptions(String message) {
        bot.execute(new SendMessage(userId, message).replyMarkup(new ReplyKeyboardRemove()));
    }

    @Override
    public void sendFile(File file, String userFileName, boolean removeKeyboard) {
        final SendDocument request = new SendDocument(userId, file).fileName(userFileName);
        if (removeKeyboard) {
            request.replyMarkup(new ReplyKeyboardRemove());
        }

        bot.execute(request);
    }

    @Override
    public boolean contains(String keyword) {
        if (message != null) {
            final String lowerCased = message.toLowerCase();
            return lowerCased.contains(keyword.toLowerCase());
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(String... keyword) {
        if (message != null) {
            final String lowerCased = message.toLowerCase();
            return Stream.of(keyword).map(String::toLowerCase).allMatch(lowerCased::contains);
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAny(String... keyword) {
        if (message != null) {
            final String lowerCased = message.toLowerCase();
            return Stream.of(keyword).map(String::toLowerCase).anyMatch(lowerCased::contains);
        } else {
            return false;
        }
    }
}
