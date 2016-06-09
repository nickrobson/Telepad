package xyz.nickr.telepad.command;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;

public class CommandManager implements Listener {

    private final List<CommandListener> commandListeners;
    private final List<InlineListener> inlineListeners;

    private CommandListener commandDef;
    private InlineListener inlineDef;

    public CommandManager(Object... listeners) {
        this.commandListeners = new LinkedList<>();
        this.inlineListeners = new LinkedList<>();

        for (Object listener : listeners) {
            this.registerCommands(listener);
        }
    }

    public void registerCommands(Object object) {
        Class<?> clz = object.getClass();
        for (Method method : clz.getMethods()) {
            Class<?>[] params = method.getParameterTypes();
            Command cmd = method.getAnnotation(Command.class);
            try {
                if (cmd != null) {
                    if (params.length >= 1 && params[0] == CommandMessageReceivedEvent.class) {
                        CommandListener l = null;
                        if (params.length == 2 && params[1] == Matcher.class) {
                            l = new CommandListener(object, method, Pattern.compile(cmd.value()));
                        } else if (params.length == 1) {
                            l = new CommandListener(object, method, cmd.value());
                        }
                        if (l != null) {
                            if (cmd.def()) {
                                this.commandDef = l;
                            }
                            this.commandListeners.add(l);
                        }
                    } else if (params.length >= 1 && params[0] == InlineQueryReceivedEvent.class) {
                        InlineListener l = null;
                        if (params.length == 2 && params[1] == Matcher.class) {
                            l = new InlineListener(object, method, Pattern.compile(cmd.value()));
                        } else if (params.length == 1) {
                            l = new InlineListener(object, method, cmd.value());
                        }
                        if (l != null) {
                            if (cmd.def()) {
                                this.inlineDef = l;
                            }
                            this.inlineListeners.add(l);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        String content = event.getContent().getContent().trim().substring(1);
        boolean run = false;
        for (CommandListener listener : this.commandListeners) {
            try {
                if (listener.regex != null) {
                    Matcher matcher = listener.regex.matcher(content);
                    if (matcher.matches()) {
                        listener.run(event, matcher);
                        run = true;
                    }
                } else if (listener.cmd != null) {
                    if (event.getCommand().equals(listener.cmd)) {
                        listener.run(event, null);
                        run = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (!run && this.commandDef != null) {
            Matcher m = this.commandDef.regex != null ? this.commandDef.regex.matcher(content) : null;
            try {
                this.commandDef.run(event, m);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onInlineQueryReceived(InlineQueryReceivedEvent event) {
        String content = event.getQuery().getQuery().trim();
        String arg0 = content.split(" ")[0];
        boolean run = false;
        for (InlineListener listener : this.inlineListeners) {
            try {
                if (listener.regex != null) {
                    Matcher matcher = listener.regex.matcher(content);
                    if (matcher.matches()) {
                        listener.run(event, matcher);
                        run = true;
                    }
                } else if (listener.cmd != null) {
                    if (!arg0.isEmpty() && arg0.equals(listener.cmd)) {
                        listener.run(event, null);
                        run = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (!run && this.inlineDef != null) {
            Matcher m = this.inlineDef.regex != null ? this.inlineDef.regex.matcher(content) : null;
            try {
                this.inlineDef.run(event, m);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class Listener<T> {

        private Object obj;
        private Method method;
        Pattern regex;
        String cmd;

        private Listener(Object obj, Method method) {
            this.obj = obj;
            this.method = method;
        }

        public Listener(Object obj, Method method, Pattern regex) {
            this(obj, method);
            this.regex = regex;
        }

        public Listener(Object obj, Method method, String cmd) {
            this(obj, method);
            this.cmd = cmd;
        }

        public void run(T event, Matcher matcher) {
            try {
                this.method.setAccessible(true);
                if (this.regex != null) {
                    this.method.invoke(this.obj, event, matcher);
                } else {
                    this.method.invoke(this.obj, event);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private class CommandListener extends Listener<CommandMessageReceivedEvent> {

        public CommandListener(Object obj, Method method, Pattern regex) {
            super(obj, method, regex);
        }

        public CommandListener(Object obj, Method method, String cmd) {
            super(obj, method, cmd);
        }

    }

    private class InlineListener extends Listener<InlineQueryReceivedEvent> {

        public InlineListener(Object obj, Method method, Pattern regex) {
            super(obj, method, regex);
        }

        public InlineListener(Object obj, Method method, String cmd) {
            super(obj, method, cmd);
        }

    }

}
