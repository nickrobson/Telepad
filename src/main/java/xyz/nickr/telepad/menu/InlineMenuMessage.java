package xyz.nickr.telepad.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.content.ContentType;
import pro.zackpollard.telegrambot.api.keyboards.InlineKeyboardButton;
import pro.zackpollard.telegrambot.api.keyboards.InlineKeyboardMarkup;
import pro.zackpollard.telegrambot.api.user.User;
import xyz.nickr.telepad.util.ConsecutiveId;

/**
 * @author Nick Robson
 */
@Getter
public class InlineMenuMessage {

    public static final String CALLBACK_UNIQUE = "TPDMenu";
    public static final int RADIX = 36;

    private static final ConsecutiveId consecutiveId = ConsecutiveId.reserve("Telepad::InlineMenu::InlineMenuMessage");
    private static final Map<String, InlineMenuMessage> messages = new HashMap<>();

    public static InlineMenuMessage getMessage(String id, String menuId) {
        InlineMenuMessage message = messages.get(id);
        if (message != null && Objects.equals(id, message.getId()) && message.getMenu() != null && Objects.equals(menuId, message.getMenu().getId()))
            return message;
        return null;
    }

    private final String id;
    private Message message;
    private InlineMenu menu;
    private Predicate<User> userPredicate;

    public InlineMenuMessage(Message message, InlineMenu menu) {
        this.id = consecutiveId.next();
        this.message = message;
        this.menu = menu;

        InlineMenuMessage.messages.put(id, this);
    }

    private InlineKeyboardMarkup getMarkup() {
        if (this.menu == null)
            return null;
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int i = 0;
        for (InlineMenuRow row : this.menu.getRows()) {
            String ROW_IDENT = Integer.toString(i, RADIX);
            List<InlineMenuButton> buttonList = row.getButtons();
            InlineKeyboardButton[] buttons = new InlineKeyboardButton[buttonList.size()];
            int j = 0;
            for (InlineMenuButton button : buttonList) {
                String COL_IDENT = Integer.toString(j, RADIX);
                buttons[j] = InlineKeyboardButton.builder()
                        .text(button.getText())
                        .callbackData(CALLBACK_UNIQUE + "[" + id + "[" + menu.getId() + "[" + ROW_IDENT + "[" + COL_IDENT)
                        .build();
                j++;
            }
            builder.addRow(buttons);
            i++;
        }
        return builder.build();
    }

    public InlineMenuMessage setMenu(InlineMenu menu) {
        if (!Objects.equals(this.menu, menu)) {
            this.menu = menu;
            if (this.menu == null || this.menu.getText() == null || this.message.getContent().getType() != ContentType.TEXT) {
                this.message = this.message.getBotInstance().editMessageReplyMarkup(this.message, this.getMarkup());
            } else {
                this.message = this.message.getBotInstance().editMessageText(this.message, this.menu.getText(), this.menu.getParseMode(), this.menu.isDisableWebPreview(), this.getMarkup());
            }
        }
        return this;
    }

    public InlineMenuMessage setUserPredicate(Predicate<User> userPredicate) {
        this.userPredicate = userPredicate;
        return this;
    }

    public InlineMenuMessage setMenuAsync(InlineMenu menu) {
        new Thread(() -> setMenu(menu)).start();
        return this;
    }

}
