package xyz.nickr.telepad.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.Markdown;

/**
 * @author Nick Robson
 */
@Accessors(chain = true)
public abstract class Command {

    @Getter private final String[] names;

    @Getter @Setter @NonNull private String help = "", usage = "";
    @Getter @Setter private String permission;

    public Command(String name, String... names) {
        this.names = new String[names.length + 1];
        this.names[0] = name;
        System.arraycopy(names, 0, this.names, 1, names.length);
    }

    public boolean hasPermission(TelepadBot bot, Message message) {
        return (permission == null) || bot.getPermissionManager().hasPermission(message, permission);
    }

    public abstract void exec(TelepadBot bot, Message message, String[] args);

    public static String escape(String text) { return Markdown.escape(text, true); }
    public static String escape(String text, boolean brackets) {
        return Markdown.escape(text, brackets);
    }

    protected void sendUsage(Message message) {
        message.getChat().sendMessage(SendableTextMessage.markdown("*Usage:* /" + escape(names[0]) + " " + escape(usage)).replyTo(message).build());
    }

}
