package xyz.nickr.telepad.permission;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import xyz.nickr.telepad.TelepadBot;

/**
 * @author Nick Robson
 */
public class PermissionManager {

    private final TelepadBot bot;
    private final List<BiPredicate<Message, String>> predicates;

    public PermissionManager(TelepadBot bot) {
        this.bot = bot;
        this.predicates = new LinkedList<>();
    }

    public void addPredicate(BiPredicate<Message, String> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        predicates.add(predicate);
    }

    public boolean hasPermission(Message message, String permission) {
        for (BiPredicate<Message, String> predicate : predicates) {
            if (predicate.test(message, permission)) {
                return true;
            }
        }
        return false;
    }

}
