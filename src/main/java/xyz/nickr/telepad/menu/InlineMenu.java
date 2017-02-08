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
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
public class InlineMenu {

    private static final ConsecutiveId consecutiveId = ConsecutiveId.reserve("Telepad::InlineMenu::InlineMenu");

    private final String id;
    private final Supplier<String> text;
    private final ParseMode parseMode;
    private boolean disableWebPreview;
    private final List<InlineMenuRow> rows;

    public InlineMenu(Supplier<String> text, ParseMode parseMode, boolean disableWebPreview, List<InlineMenuRow> rows) {
        this.id = consecutiveId.next();
        this.text = text;
        this.parseMode = parseMode;
        this.disableWebPreview = disableWebPreview;
        this.rows = Collections.unmodifiableList(rows);
    }

    public static InlineMenuBuilder builder() {
        return new InlineMenuBuilder();
    }

    public InlineMenuMessage getMenuFor(Message message) {
        if (!Collator.getInstance(Locale.US).equals(message.getSender().getUsername(), message.getBotInstance().getBotUsername()))
            throw new IllegalArgumentException("message not sent by the bot!");
        return new InlineMenuMessage(message, this);
    }

    public static class InlineMenuBuilder {

        private Supplier<String> text;
        private ParseMode parseMode = ParseMode.NONE;
        private boolean disableWebPreview = false;

        private final List<InlineMenuRow> rows = new ArrayList<>();

        public InlineMenuBuilder newRow(Consumer<InlineMenuRow.InlineMenuRowBuilder> consumer) {
            InlineMenuRow.InlineMenuRowBuilder child = InlineMenuRow.builder();
            consumer.accept(child);
            rows.add(child.build());
            return this;
        }

        public InlineMenuBuilder text(Supplier<String> text) {
            this.text = text;
            return this;
        }

        public InlineMenuBuilder text(String text) {
            this.text = () -> text;
            return this;
        }

        public InlineMenuBuilder parseMode(ParseMode parseMode) {
            this.parseMode = Objects.requireNonNull(parseMode, "parse mode cannot be null");
            return this;
        }

        public InlineMenuBuilder disableWebPreview(boolean disableWebPreview) {
            this.disableWebPreview = disableWebPreview;
            return this;
        }

        public InlineMenu build() {
            return new InlineMenu(text, parseMode, disableWebPreview, rows);
        }

    }

}
