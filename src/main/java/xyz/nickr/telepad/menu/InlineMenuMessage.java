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
 * Represents a message containing one or more {@link InlineMenu}s.
 *
 * @author Nick Robson
 */
@Getter
@Accessors(chain = true)
public class InlineMenuMessage {

    /**
     * Represents the different ways that a Back button can be added to the menu.
     */
    public enum BackButtonType {

        /**
         * No back button will be added
         */
        NONE,

        /**
         * Adds a back button to the start of the first row.
         */
        PREPEND_TO_FIRST_ROW,

        /**
         * Adds a back button to the end of the first row.
         */
        APPEND_TO_FIRST_ROW,

        /**
         * Adds a back button to the start of the last row.
         */
        PREPEND_TO_LAST_ROW,

        /**
         * Adds a back button to the end of the last row.
         */
        APPEND_TO_LAST_ROW,

        /**
         * Creates a new row containing just the back button as the
         * first row.
         */
        NEW_FIRST_ROW,

        /**
         * Creates a new row containing just the back button as the
         * last row.
         */
        NEW_LAST_ROW
    }

    /**
     * The callback prefix for menus created using this package.
     */
    public static final String CALLBACK_UNIQUE = "TPDMenu";

    /**
     * The radix used to compress integers into smaller strings than using decimal.
     */
    public static final int RADIX = 36;

    private static final ConsecutiveId consecutiveId = ConsecutiveId.reserve("Telepad::InlineMenu::InlineMenuMessage");
    private static final Map<String, InlineMenuMessage> messages = new HashMap<>();

    /**
     * Gets the message sent by this API with the given ID and menu ID.
     *
     * @param id The ID
     * @param menuId The menu ID
     *
     * @return The message, or null if either ID does not match
     */
    public static InlineMenuMessage getMessage(String id, String menuId) {
        InlineMenuMessage message = messages.get(id);
        if (message != null && Objects.equals(id, message.getId()) && !message.getMenuStack().isEmpty() && Objects.equals(menuId, message.getMenu().getId()))
            return message;
        return null;
    }

    private final String id;
    private Message message;
    private Predicate<User> userPredicate;

    @Getter(AccessLevel.PRIVATE)
    private final Stack<InlineMenu> menuStack = new Stack<>();

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

    /**
     * Gets the current menu
     *
     * @return The menu, or null if there is no menu
     */
    public InlineMenu getMenu() {
        return menuStack.isEmpty() ? null : menuStack.peek();
    }

    /**
     * Sets the menu in this message.
     *
     * @param menu The new menu
     *
     * @return This instance
     */
    public InlineMenuMessage setMenu(InlineMenu menu) {
        if (menuStack.isEmpty() || !Objects.equals(menuStack.peek(), menu)) {
            this.menuStack.push(menu);
            updateMessage();
        }
        return this;
    }

    /**
     * Sets the menu in this message, asynchronously.
     *
     * @param menu The new menu
     *
     * @return This instance
     */
    public InlineMenuMessage setMenuAsync(InlineMenu menu) {
        new Thread(() -> setMenu(menu)).start();
        return this;
    }

    /**
     * Sets the predicate for who can use this menu.
     *
     * @param userPredicate The predicate
     *
     * @return This instance
     */
    public InlineMenuMessage setUserPredicate(Predicate<User> userPredicate) {
        this.userPredicate = userPredicate;
        return this;
    }

    /**
     * Goes back to the previous menu.
     *
     * @param async Whether or not to do this asynchronously
     *
     * @return This instance
     */
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

    private void updateMessage() {
        InlineMenu menu = menuStack.peek();
        if (menu == null || menu.getText() == null || this.message.getContent().getType() != ContentType.TEXT) {
            this.message = this.message.getBotInstance().editMessageReplyMarkup(this.message, this.getMarkup());
        } else {
            this.message = this.message.getBotInstance().editMessageText(this.message, menu.getText().get(), menu.getParseMode(), menu.isDisableWebPreview(), this.getMarkup());
        }
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

}
