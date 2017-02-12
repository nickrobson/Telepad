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

/**
 * The base class for Telepad, providing access to managers and caches.
 *
 * @author Nick Robson
 */
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

    /**
     * Creates a Telepad bot based on a Telegram Authentication Token
     * from @BotFather.
     *
     * @param token The authentication token
     */
    public TelepadBot(String token) {
        this(TelegramBot.login(token));
    }

    /**
     * Creates a Telepad bot based on a {@link TelegramBot}.
     *
     * @param handle The telegram bot instance.
     */
    public TelepadBot(TelegramBot handle) {
        this.handle = Objects.requireNonNull(handle, "Telegram bot is null. Invalid auth token?");
        this.userCache = new UserCache();
        this.listener = new TelepadListener(this);
        this.commandManager = new CommandManager(this);
        this.permissionManager = new PermissionManager(this);

        this.handle.getEventsManager().register(this.listener);
    }

    /**
     * Sets the locale of this bot, used for string checks.
     *
     * This updates both the locale and the collator of the bot.
     *
     * @param locale The new locale.
     */
    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale cannot be null");
        this.collator = Collator.getInstance(locale);
    }

    /**
     * Registers a listener.
     *
     * @param listener The listener to be registered.
     */
    public void registerListener(Listener listener) {
        this.handle.getEventsManager().register(listener);
    }

    /**
     * Starts receiving updates.
     *
     * @param previousUpdates Whether or not to receive updates
     *                        sent while the bot was offline.
     */
    public void start(boolean previousUpdates) {
        this.handle.startUpdates(previousUpdates);
    }

}
