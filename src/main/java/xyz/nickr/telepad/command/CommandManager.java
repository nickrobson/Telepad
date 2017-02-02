package xyz.nickr.telepad.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.Markdown;

/**
 * @author Nick Robson
 */
public class CommandManager {

    private final Map<String, Command> commandMap = new HashMap<>();
    private final TelepadBot bot;

    public CommandManager(TelepadBot bot) {
        this.bot = bot;
        this.register(new HelpCommand());
    }

    public void register(Command command) {
        for (String name : command.getNames()) {
            if (name != null) {
                commandMap.put(name.toLowerCase(), command);
            }
        }
    }

    public Set<Command> getCommands() {
        return new HashSet<>(commandMap.values());
    }

    public void exec(Message msg, String[] command) {
        if (command.length == 0)
            return;
        Command cmd = commandMap.get(command[0].toLowerCase());
        if (cmd != null) {
            String[] args = Arrays.copyOfRange(command, 1, command.length);
            try {
                if (cmd.hasPermission(bot, msg)) {
                    cmd.exec(bot, msg, args);
                } else {
                    msg.getChat().sendMessage(SendableTextMessage.markdown("You don't have permission to use that command!").replyTo(msg).build());
                }
            } catch (Exception ex) {
                msg.getChat().sendMessage(SendableTextMessage.markdown("*Error!* " + Markdown.escape(ex.toString(), true)).replyTo(msg).build());
                ex.printStackTrace();
            }
        }
    }

}
