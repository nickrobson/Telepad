package xyz.nickr.telepad.command;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import xyz.nickr.telepad.TelepadBot;
import xyz.nickr.telepad.util.TriFunction;

/**
 * @author Nick Robson
 */
@Getter
public final class ScriptedCommand extends Command {

    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private File file;
    private ScriptEngine engine;
    private Invocable invocable;
    private Bindings bindings;
    private String[] names;

    public ScriptedCommand(File file) {
        super(null);
        this.file = file;
        this.reload();
    }

    public void reload() {
        if (!this.file.isFile())
            throw new IllegalArgumentException(file.toString() + " has to be a file");

        this.engine = scriptEngineManager.getEngineByName("JavaScript");
        this.invocable = (Invocable) engine;

        this.bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        this.bindings.put("escape", (BiFunction<String, Boolean, String>) Command::escape);

        this.bindings.put("sendUsage", (Consumer<Message>) this::sendUsage);
        this.bindings.put("hasPermission", (BiPredicate<TelepadBot, Message>) this::hasPermission);

        this.bindings.put("setHelp", (Function<String, Command>) this::setHelp);
        this.bindings.put("setPermission", (Function<String, Command>) this::setPermission);
        this.bindings.put("setUsage", (Function<String, Command>) this::setUsage);

        this.bindings.put("reply", (TriFunction<Message, String, ParseMode, Message>) this::reply);
        this.bindings.put("edit", (TriFunction<Message, String, ParseMode, Message>) this::edit);

        try {
            FileReader reader = new FileReader(this.file);
            engine.eval(reader);

            Object names = engine.get("names");
            if (names == null)
                throw new IllegalStateException("script must declare a `names` array");
            engine.eval("names = Java.to(names, 'java.lang.String[]')");
            Object onExecute = engine.get("onExecute");
            if (onExecute == null)
                throw new IllegalStateException("script must declare an `onExecute` function");

            setupEngine();

            this.names = (String[]) engine.get("names");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setupEngine() throws ScriptException {
        engine.eval("System = Java.type('java.lang.System')");
        engine.eval("String = Java.type('java.lang.String')");
        engine.eval("ParseMode = Java.type('" + ParseMode.class.getName() + "')");
    }

    @Override
    public String toString() {
        return String.format("%s{file=%s}", super.toString(), file);
    }

    @Override
    public String[] getNames() {
        return this.names;
    }

    @Override
    public void exec(TelepadBot bot, Message message, String[] args) {
        try {
            invocable.invokeFunction("onExecute", bot, message, args);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }
}
