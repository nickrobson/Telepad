package xyz.nickr.telepad.menu;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import xyz.nickr.telepad.util.ConsecutiveId;

/**
 * Represents an inline menu.
 *
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
public class InlineMenu {

    private static final ConsecutiveId consecutiveId = ConsecutiveId.reserve("Telepad::InlineMenu::InlineMenu");

    private final String id;
    private final Supplier<String> text;
    private final ParseMode parseMode;
    private final boolean disableWebPreview;
    private final List<InlineMenuRow> rows;

    public InlineMenu(Supplier<String> text, ParseMode parseMode, boolean disableWebPreview, List<InlineMenuRow> rows) {
        this.id = consecutiveId.next();
        this.text = text;
        this.parseMode = parseMode;
        this.disableWebPreview = disableWebPreview;
        this.rows = Collections.unmodifiableList(rows);
    }

    /**
     * Gets a new {@link InlineMenuMessage} for handling this menu on
     * a new message.
     *
     * @param message The message that the bot sent
     *
     * @return The inline menu message instance
     */
    public InlineMenuMessage getMenuFor(Message message) {
        if (!Collator.getInstance(Locale.US).equals(message.getSender().getUsername(), message.getBotInstance().getBotUsername()))
            throw new IllegalArgumentException("message not sent by the bot!");
        return new InlineMenuMessage(message, this);
    }

    /**
     * Creates a builder for making a menu.
     *
     * @return The builder
     */
    public static InlineMenuBuilder builder() {
        return new InlineMenuBuilder();
    }

    public static class InlineMenuBuilder {

        private Supplier<String> text;
        private ParseMode parseMode = ParseMode.NONE;
        private boolean disableWebPreview = false;

        private final List<InlineMenuRow> rows = new ArrayList<>();

        /**
         * Creates a builder for a new {@link InlineMenuRow}
         *
         * @param consumer A consumer for editing the row that will be added
         *
         * @return This instance
         */
        public InlineMenuBuilder newRow(Consumer<InlineMenuRow.InlineMenuRowBuilder> consumer) {
            InlineMenuRow.InlineMenuRowBuilder child = InlineMenuRow.builder();
            consumer.accept(child);
            rows.add(child.build());
            return this;
        }

        /**
         * Sets a supplier for the text shown on the message by this menu.
         *
         * @param text The text supplier, only called when the page is displayed
         *
         * @return This instance
         */
        public InlineMenuBuilder text(Supplier<String> text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the text shown on the message by this menu.
         *
         * @param text The text
         *
         * @return This instance
         */
        public InlineMenuBuilder text(String text) {
            this.text = () -> text;
            return this;
        }

        /**
         * Sets the {@link ParseMode} for this menu.
         *
         * @param parseMode The parse mode. If null, a {@link NullPointerException}
         *                  will be thrown
         *
         * @return This instance
         */
        public InlineMenuBuilder parseMode(ParseMode parseMode) {
            this.parseMode = Objects.requireNonNull(parseMode, "parse mode cannot be null");
            return this;
        }

        /**
         * Sets whether or not to display a web preview if there are links in the
         * text.
         *
         * @param disableWebPreview Whether or not to display a web preview
         *
         * @return This instance
         */
        public InlineMenuBuilder disableWebPreview(boolean disableWebPreview) {
            this.disableWebPreview = disableWebPreview;
            return this;
        }

        /**
         * Builds a new inline menu from this builder.
         *
         * @return The new menu
         */
        public InlineMenu build() {
            return new InlineMenu(text, parseMode, disableWebPreview, rows);
        }

    }

}
