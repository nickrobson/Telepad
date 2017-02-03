package xyz.nickr.telepad.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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

    private final String id, text;
    private final ParseMode parseMode;
    private boolean disableWebPreview;
    private final List<InlineMenuRow> rows;

    public InlineMenu(String text, ParseMode parseMode, boolean disableWebPreview, List<InlineMenuRow> rows) {
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
        if (!message.getSender().getUsername().equals(message.getBotInstance().getBotUsername()))
            throw new IllegalArgumentException("message not sent by the bot!");
        return new InlineMenuMessage(message, this);
    }

    @Accessors(fluent = true)
    public static class InlineMenuBuilder {

        @Setter private String text;
        @Setter private ParseMode parseMode = ParseMode.NONE;
        @Setter private boolean disableWebPreview = false;
        private final List<InlineMenuRow> rows = new ArrayList<>();

        public InlineMenuBuilder newRow(Consumer<InlineMenuRow.InlineMenuRowBuilder> consumer) {
            InlineMenuRow.InlineMenuRowBuilder child = InlineMenuRow.builder();
            consumer.accept(child);
            rows.add(child.build());
            return this;
        }

        public InlineMenu build() {
            return new InlineMenu(text, parseMode, disableWebPreview, rows);
        }

    }

}
