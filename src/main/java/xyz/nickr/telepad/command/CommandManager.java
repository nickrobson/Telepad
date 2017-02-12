package xyz.nickr.telepad.command;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.Markdown;
import xyz.nickr.telepad.util.PaginatedData;

/**
 * Handles registration and execution of commands by users.
 *
 * @author Nick Robson
 */
public class CommandManager {

    private final TelepadBot bot;
    private final Map<String, Command> commandMap = new HashMap<>();

    private final Set<String> scriptLocations = new HashSet<>();
    private boolean hasScriptCommands;

    public CommandManager(TelepadBot bot) {
        this.bot = bot;
        this.register(new HelpCommand());
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
     * Gets all registered commands.
     *
     * @return The commands
     */
    public Set<Command> getCommands() {
        return new HashSet<>(commandMap.values());
    }

    /**
     * Registers a command to all of its aliases.
     *
     * @param command The command
     */
    public void register(Command command) {
        Objects.requireNonNull(command, "command cannot be null");
        for (String name : command.getNames()) {
            if (name == null) {
                throw new NullPointerException("command name cannot be null (" + command + ")");
            }
        }
        System.out.println("Registering command: " + command + " : " + Arrays.toString(command.getNames()));
        for (String name : command.getNames()) {
            commandMap.put(name.toLowerCase(bot.getLocale()), command);
        }
    }

    /**
     * Registers all commands in a given package
     *
     * @param packageName The package
     */
    public void registerPackage(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
        for (Class<? extends Command> commandClass : commandClasses) {
            try {
                register(commandClass.newInstance());
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Registers a file as a JavaScript command
     *
     * @param file The file
     *
     * @return Whether or not the file was added successfully.
     */
    public boolean registerScriptFile(File file) {
        this.scriptLocations.add(file.getPath());
        return _registerScriptFile(file);
    }

    /**
     * Registers all JavaScript files in a directory as commands,
     * and recursively searches through subdirectories.
     * <br><br>
     * <i>Note</i>: only files ending in {@code .js} will be registered.
     *
     * @param dir The directory
     *
     * @return Whether or not all commands were successfully registered
     */
    public boolean registerScriptDirectory(File dir) {
        this.scriptLocations.add(dir.getPath());
        return _registerScriptDirectory(dir);
    }

    /**
     * Reloads all loaded scripts, loading any new ones in directories that are
     * already registered.
     *
     * @return Whether or not all scripts were successfully loaded
     */
    public boolean reloadScripts() {
        commandMap.remove("reloadscripts");
        commandMap.remove("listscripts");
        commandMap.values().removeIf(c -> c instanceof ScriptedCommand);

        this.hasScriptCommands = false;

        boolean state = true;
        for (String scriptLocation : this.scriptLocations) {
            File f = new File(scriptLocation);
            if (f.isFile())
                state &= _registerScriptFile(f);
            else if (f.isDirectory())
                state &= _registerScriptDirectory(f);
        }
        return state;
    }

    /**
     * Executes a command from a message.
     *
     * @param msg The message it was sent from
     * @param command The command name and arguments
     */
    public void exec(Message msg, String[] command) {
        if (command.length == 0)
            return;
        Command cmd = commandMap.get(command[0].toLowerCase(bot.getLocale()));
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

    private boolean _registerScriptFile(File file) {
        if (!file.isFile())
            throw new IllegalArgumentException("must be a file");
        try {
            register(new ScriptedCommand(file));
            if (!hasScriptCommands) {
                hasScriptCommands = true;
                register(new Command("reloadscripts") {
                    @Override
                    public void exec(TelepadBot bot, Message message, String[] args) {
                        Message m = reply(message, "Reloading!", ParseMode.NONE);
                        boolean success = bot.getCommandManager().reloadScripts();
                        if (success) {
                            edit(m, "Reloaded!", ParseMode.NONE);
                        } else {
                            edit(m, "Error! Check the console.", ParseMode.NONE);
                        }
                    }
                }.setHelp("reloads all loaded scripts").setPermission("script.reload"));
                register(new Command("listscripts") {
                    @Override
                    public void exec(TelepadBot bot, Message message, String[] args) {
                        Set<ScriptedCommand> commands = commandMap.values()
                                .stream()
                                .filter(c -> c instanceof ScriptedCommand)
                                .map(ScriptedCommand.class::cast)
                                .collect(Collectors.toSet());
                        if (commands.isEmpty()) {
                            reply(message, "There are no scripts loaded!", ParseMode.NONE);
                        } else {
                            List<String> lines = new LinkedList<>();

                            for (ScriptedCommand cmd : commands) {
                                lines.add(
                                        cmd.getFile().toString() + ":\n" +
                                                "   /" + String.join(", /", cmd.getNames())
                                );
                            }

                            PaginatedData paginatedData = new PaginatedData(lines, 10);
                            paginatedData.setParseMode(ParseMode.MARKDOWN);
                            paginatedData.send(0, message);
                        }
                    }
                }.setHelp("lists all loaded scripts").setPermission("script.list"));
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean _registerScriptDirectory(File dir) {
        if (!dir.isDirectory())
            throw new IllegalArgumentException("must be a directory");
        File[] subFiles = dir.listFiles();
        if (subFiles == null)
            throw new NullPointerException("failed to list directory contents of " + dir.getPath());
        boolean state = true;
        for (File file : subFiles) {
            if (file.isDirectory()) {
                state &= _registerScriptDirectory(file);
            } else if (file.isFile() && file.getName().endsWith(".js")) {
                state &= _registerScriptFile(file);
            }
        }
        return state;
    }

}
