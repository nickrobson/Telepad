package xyz.nickr.telepad;

import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import xyz.nickr.telepad.command.CommandManager;

public class TelepadBot {

    @Getter
    private final TelegramBot telegramBot;

    public TelepadBot(String auth, CommandManager commands) {
        this.telegramBot = TelegramBot.login(auth);
        this.telegramBot.getEventsManager().register(commands);
        this.telegramBot.startUpdates(true);
    }

}
