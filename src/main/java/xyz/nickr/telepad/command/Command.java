package xyz.nickr.telepad.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.Markdown;

/**
 * Represents a command that can be executed by users.
 *
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

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Checks if a user has permission to use this command.
     *
     * @param bot The bot instance
     * @param message The message
     *
     * @return True iff the user has permission to use this command
     */
    public boolean hasPermission(TelepadBot bot, Message message) {
        return (permission == null) || bot.getPermissionManager().hasPermission(message, permission);
    }

    /**
     * Executes this command
     *
     * @param bot The bot instance
     * @param message The message the command was sent in
     * @param args The command arguments (excluding the command name)
     */
    public abstract void exec(TelepadBot bot, Message message, String[] args);

    /**
     * Escapes markdown text, including brackets.
     *
     * @param text The markdown text
     *
     * @return The escaped text
     */
    public static String escape(String text) { return Markdown.escape(text, true); }

    /**
     * Escapes markdown text.
     *
     * @param text The markdown text
     * @param brackets Whether or not to escape brackets
     *
     * @return The escaped text
     */
    public static String escape(String text, boolean brackets) {
        return Markdown.escape(text, brackets);
    }

    /**
     * Sends how to correctly use this command as a reply.
     *
     * @param message The message to reply to
     */
    protected void sendUsage(Message message) {
        reply(message, "*Usage:* /" + escape(names[0]) + " " + escape(usage), ParseMode.MARKDOWN);
    }

    /**
     * Replies to a message with given reply text and parse mode.
     *
     * @param message The message
     * @param string The reply text
     * @param parseMode The parse mode
     *
     * @return The message
     */
    public Message reply(Message message, String string, ParseMode parseMode) {
        return message.getChat().sendMessage(
                SendableTextMessage.builder()
                        .message(string)
                        .parseMode(parseMode != null ? parseMode : ParseMode.NONE)
                        .disableWebPagePreview(true)
                        .replyTo(message)
                        .build()
        );
    }

    /**
     * Edits a sent message to have new reply text and parse mode.
     *
     * @param message The previously-sent message
     * @param string The new reply text
     * @param parseMode The parse mode
     *
     * @return The edited message
     */
    public Message edit(Message message, String string, ParseMode parseMode) {
        return message.getBotInstance().editMessageText(message, string, parseMode, true, null);
    }

}
