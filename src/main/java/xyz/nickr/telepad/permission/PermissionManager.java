package xyz.nickr.telepad.permission;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import xyz.nickr.telepad.TelepadBot;

/**
 * Manages permission checking for command usage
 * and otherwise.
 *
 * @author Nick Robson
 */
public class PermissionManager {

    private final TelepadBot bot;
    private final List<BiPredicate<Message, String>> predicates;

    public PermissionManager(TelepadBot bot) {
        this.bot = bot;
        this.predicates = new LinkedList<>();
    }

    /**
     * Gets the bot that this instance belongs to.
     *
     * @return The bot
     */
    public TelepadBot getBotInstance() {
        return bot;
    }

    /**
     * Adds a permission predicate for checking if a user has
     * a specific permission.
     *
     * @param predicate The predicate
     */
    public void addPredicate(BiPredicate<Message, String> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        predicates.add(predicate);
    }

    /**
     * Checks if a user has a given permission.
     *
     * @param message The message
     * @param permission The permission
     *
     * @return True iff the user has the permission
     */
    public boolean hasPermission(Message message, String permission) {
        for (BiPredicate<Message, String> predicate : predicates) {
            if (predicate.test(message, permission)) {
                return true;
            }
        }
        return false;
    }

}
