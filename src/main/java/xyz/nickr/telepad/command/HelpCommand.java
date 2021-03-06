package xyz.nickr.telepad.command;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.PaginatedData;

/**
 * Collates all commands that a user can used, and displays
 * them nicely in pages.
 *
 * @author Nick Robson
 */
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "start");
        this.setHelp("retrieves help information for each command");
    }

    @Override
    public boolean hasPermission(TelepadBot bot, Message message) {
        return true;
    }

    @Override
    public void exec(TelepadBot bot, Message message, String[] args) {
        List<Command> commands = new LinkedList<>(bot.getCommandManager().getCommands());
        commands.removeIf(c -> !c.hasPermission(bot, message));
        commands.sort(Comparator.comparing(a -> bot.getCollator().getCollationKey(a.getNames()[0])));

        List<String> lines = commands.stream()
                .map(c -> "/" + String.join(", /", c.getNames()) + " " + c.getUsage() + "\n  - " + c.getHelp())
                .collect(Collectors.toList());

        PaginatedData paginatedData = new PaginatedData(lines, 5);
        paginatedData.setParseMode(ParseMode.NONE);
        paginatedData.send(0, message);
    }

}
