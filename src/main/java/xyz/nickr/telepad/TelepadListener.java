package xyz.nickr.telepad;

import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.chat.CallbackQuery;
import pro.zackpollard.telegrambot.api.chat.inline.InlineCallbackQuery;
import pro.zackpollard.telegrambot.api.chat.message.MessageCallbackQuery;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.CallbackQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.ParticipantJoinGroupChatEvent;
import pro.zackpollard.telegrambot.api.event.chat.ParticipantLeaveGroupChatEvent;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineCallbackQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.MessageCallbackQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.MessageReceivedEvent;
import pro.zackpollard.telegrambot.api.user.User;
import xyz.nickr.telepad.menu.InlineMenuButtonResponse;
import xyz.nickr.telepad.menu.InlineMenuMessage;

/**
 * Handles all events that Telepad needs to function.
 *
 * @author Nick Robson
 */
@Getter
@AllArgsConstructor
public class TelepadListener implements Listener {

    private final TelepadBot bot;

    @Override
    public void onMessageCallbackQueryReceivedEvent(MessageCallbackQueryReceivedEvent event) {
        MessageCallbackQuery query = event.getCallbackQuery();
        handleCallback(query.getData(), query.getFrom(), query);
    }

    @Override
    public void onInlineCallbackQueryReceivedEvent(InlineCallbackQueryReceivedEvent event) {
        InlineCallbackQuery query = event.getCallbackQuery();
        handleCallback(query.getData(), query.getFrom(), query);
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        bot.getUserCache().store(event.getMessage().getSender());

        if (!event.isBotMentioned())
            return;

        String[] args = event.getArgs();
        String[] command = new String[args.length + 1];
        command[0] = event.getCommand();
        System.arraycopy(args, 0, command, 1, args.length);

        bot.getCommandManager().exec(event.getMessage(), command);
    }

    /// Handles inline callbacks

    private void handleCallback(String callback, User user, CallbackQuery query) {
        bot.getUserCache().store(user);

        try {
            if (callback.startsWith(InlineMenuMessage.CALLBACK_UNIQUE)) {
                String[] split = callback.split("\\[");
                if (split.length == 5) {
                    InlineMenuMessage message = InlineMenuMessage.getMessage(split[1], split[2]);
                    if (message != null) {
                        if ((message.getUserPredicate() == null) || message.getUserPredicate().test(user)) {
                            if (split[3].isEmpty() && bot.getCollator().equals(split[4], "BACK")) {
                                message.back(false);
                            } else {
                                int row = Integer.parseInt(split[3], InlineMenuMessage.RADIX);
                                int col = Integer.parseInt(split[4], InlineMenuMessage.RADIX);
                                BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> func = message.getMenu().getRows().get(row).getButtons().get(col).getCallback();
                                if (func != null) {
                                    try {
                                        InlineMenuButtonResponse response = func.apply(message, user);
                                        if (response != null) {
                                            query.answer(response.getText(), response.isAlert());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            query.answer("You are not allowed to use that!", true);
                        }
                    }
                }
            } else {
                System.err.println("received unexpected callback from " + user.getUsername() + ": " + callback);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /// Begin events that are only listened to so that our UserCache is as up to date as possible. ////

    @Override
    public void onCallbackQueryReceivedEvent(CallbackQueryReceivedEvent event) {
        bot.getUserCache().store(event.getCallbackQuery().getFrom());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        bot.getUserCache().store(event.getMessage().getSender());
    }

    @Override
    public void onParticipantJoinGroupChat(ParticipantJoinGroupChatEvent event) {
        bot.getUserCache().store(event.getParticipant());
    }

    @Override
    public void onParticipantLeaveGroupChat(ParticipantLeaveGroupChatEvent event) {
        bot.getUserCache().store(event.getParticipant());
    }

    @Override
    public void onInlineQueryReceived(InlineQueryReceivedEvent event) {
        bot.getUserCache().store(event.getQuery().getSender());
    }

}
