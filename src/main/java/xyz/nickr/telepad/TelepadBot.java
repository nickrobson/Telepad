package xyz.nickr.telepad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.Collator;
import java.util.Locale;
import java.util.Objects;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.event.Listener;
import xyz.nickr.telepad.command.CommandManager;
import xyz.nickr.telepad.permission.PermissionManager;
import xyz.nickr.telepad.util.UserCache;

@Getter
public final class TelepadBot {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final TelegramBot handle;
    private final TelepadListener listener;
    private final CommandManager commandManager;
    private final PermissionManager permissionManager;
    private final UserCache userCache;

    private Locale locale = Locale.US;
    private Collator collator = Collator.getInstance(locale);

    public TelepadBot(String token) {
        this(TelegramBot.login(token));
    }

    public TelepadBot(TelegramBot handle) {
        this.handle = Objects.requireNonNull(handle, "Telegram bot is null. Invalid auth token?");
        this.userCache = new UserCache();
        this.listener = new TelepadListener(this);
        this.commandManager = new CommandManager(this);
        this.permissionManager = new PermissionManager(this);

        this.handle.getEventsManager().register(this.listener);
    }

    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale cannot be null");
        this.collator = Collator.getInstance(locale);
    }

    public void registerListener(Listener listener) {
        this.handle.getEventsManager().register(listener);
    }

    public void start(boolean previousUpdates) {
        this.handle.startUpdates(previousUpdates);
    }

}
