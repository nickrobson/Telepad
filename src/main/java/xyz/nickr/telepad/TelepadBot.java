package xyz.nickr.telepad;

import java.util.Objects;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;

@Getter
public final class TelepadBot {

    private final TelegramBot handle;
    private final TelepadListener listener;

    public TelepadBot(String token) {
        this(TelegramBot.login(token));
    }

    public TelepadBot(TelegramBot handle) {
        this.handle = Objects.requireNonNull(handle, "Telegram bot is null. Invalid auth token?");
        this.listener = new TelepadListener(this);

        this.handle.getEventsManager().register(this.listener);
    }

    public final TelegramBot getHandle() {
        return handle;
    }

    public void start(boolean previousUpdates) {
        this.handle.startUpdates(previousUpdates);
    }

}
