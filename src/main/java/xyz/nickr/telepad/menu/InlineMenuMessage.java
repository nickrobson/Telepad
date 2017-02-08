package xyz.nickr.telepad.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
public class InlineMenuMessage {

    public enum BackButtonType { NONE, PREPEND_TO_FIRST_ROW, APPEND_TO_FIRST_ROW, PREPEND_TO_LAST_ROW, APPEND_TO_LAST_ROW, NEW_FIRST_ROW, NEW_LAST_ROW }

    public static final String CALLBACK_UNIQUE = "TPDMenu";
    public static final int RADIX = 36;

    private static final ConsecutiveId consecutiveId = ConsecutiveId.reserve("Telepad::InlineMenu::InlineMenuMessage");
    private static final Map<String, InlineMenuMessage> messages = new HashMap<>();

    public static InlineMenuMessage getMessage(String id, String menuId) {
        InlineMenuMessage message = messages.get(id);
        if (message != null && Objects.equals(id, message.getId()) && !message.getMenuStack().isEmpty() && Objects.equals(menuId, message.getMenu().getId()))
            return message;
        return null;
    }

    private final String id;
    private Message message;
    @Getter(AccessLevel.PRIVATE) private Stack<InlineMenu> menuStack = new Stack<>();
    private Predicate<User> userPredicate;

    @Setter @NonNull private BackButtonType backButtonType = BackButtonType.NONE;

    public InlineMenuMessage(Message message, InlineMenu menu) {
        this.id = consecutiveId.next();
        this.message = message;
        if (menu != null)
            this.menuStack.add(menu);

        InlineMenuMessage.messages.put(id, this);

        if (menu != null)
            updateMessage();
    }

    private InlineKeyboardMarkup getMarkup() {
        if (this.menuStack.isEmpty())
            return null;
        InlineMenu menu = menuStack.peek();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int extraFirst = this.menuStack.size() > 1 && (this.backButtonType == BackButtonType.PREPEND_TO_FIRST_ROW || this.backButtonType == BackButtonType.APPEND_TO_FIRST_ROW) ? 1 : 0;
        int extraLast = this.menuStack.size() > 1 && (this.backButtonType == BackButtonType.PREPEND_TO_LAST_ROW || this.backButtonType == BackButtonType.APPEND_TO_LAST_ROW) ? 1 : 0;

        if (this.menuStack.size() > 1 && this.backButtonType == BackButtonType.NEW_FIRST_ROW) {
            builder.addRow(getBackButton());
        }

        int i = 0, max = menu.getRows().size();
        for (InlineMenuRow row : menu.getRows()) {
            String ROW_IDENT = Integer.toString(i, RADIX);
            List<InlineMenuButton> buttonList = row.getButtons();
            InlineKeyboardButton[] buttons = new InlineKeyboardButton[buttonList.size() + (i == 0 ? extraFirst : 0) + (i + 1 == max ? extraLast : 0)];
            int j = 0;
            if (this.menuStack.size() > 1 && ((i == 0 && backButtonType == BackButtonType.PREPEND_TO_FIRST_ROW) || (i + 1 == max && backButtonType == BackButtonType.PREPEND_TO_LAST_ROW))) {
                buttons[j] = getBackButton();
                j++;
            }
            for (InlineMenuButton button : buttonList) {
                String COL_IDENT = Integer.toString(j, RADIX);
                buttons[j] = InlineKeyboardButton.builder()
                        .text(button.getText())
                        .callbackData(CALLBACK_UNIQUE + "[" + id + "[" + menu.getId() + "[" + ROW_IDENT + "[" + COL_IDENT)
                        .build();
                j++;
            }
            if (this.menuStack.size() > 1 && ((i == 0 && backButtonType == BackButtonType.APPEND_TO_FIRST_ROW) || (i + 1 == max && backButtonType == BackButtonType.APPEND_TO_LAST_ROW))) {
                buttons[j] = getBackButton();
            }
            builder.addRow(buttons);
            ++i;
        }
        if (this.menuStack.size() > 1 && this.backButtonType == BackButtonType.NEW_LAST_ROW) {
            builder.addRow(getBackButton());
        }
        return builder.build();
    }

    private InlineKeyboardButton getBackButton() {
        InlineMenu menu = menuStack.peek();
        return InlineKeyboardButton.builder()
                .text("← Back")
                .callbackData(CALLBACK_UNIQUE + "[" + id + "[" + menu.getId() + "[[BACK")
                .build();
    }

    public InlineMenu getMenu() {
        return menuStack.isEmpty() ? null : menuStack.peek();
    }

    public InlineMenuMessage setMenu(InlineMenu menu) {
        if (menuStack.isEmpty() || !Objects.equals(menuStack.peek(), menu)) {
            this.menuStack.push(menu);
            updateMessage();
        }
        return this;
    }

    private void updateMessage() {
        InlineMenu menu = menuStack.peek();
        if (menu == null || menu.getText() == null || this.message.getContent().getType() != ContentType.TEXT) {
            this.message = this.message.getBotInstance().editMessageReplyMarkup(this.message, this.getMarkup());
        } else {
            this.message = this.message.getBotInstance().editMessageText(this.message, menu.getText().get(), menu.getParseMode(), menu.isDisableWebPreview(), this.getMarkup());
        }
    }

    public InlineMenuMessage setUserPredicate(Predicate<User> userPredicate) {
        this.userPredicate = userPredicate;
        return this;
    }

    public InlineMenuMessage setMenuAsync(InlineMenu menu) {
        new Thread(() -> setMenu(menu)).start();
        return this;
    }

    public InlineMenuMessage back(boolean async) {
        if (menuStack.size() > 1) {
            menuStack.pop();
            if (async) {
                new Thread(this::updateMessage).start();
            } else {
                updateMessage();
            }
            return this;
        } else {
            throw new IllegalStateException("I can't go back, I'm already at my root!");
        }
    }

}
