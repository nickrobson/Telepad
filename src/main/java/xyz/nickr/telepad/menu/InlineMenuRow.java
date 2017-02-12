package xyz.nickr.telepad.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a row of buttons on an inline menu
 *
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
public class InlineMenuRow {

    private final List<InlineMenuButton> buttons;

    public InlineMenuRow(List<InlineMenuButton> buttons) {
        this.buttons = Collections.unmodifiableList(buttons);
    }

    /**
     * Creates a builder for making a button.
     *
     * @return The builder
     */
    public static InlineMenuRowBuilder builder() {
        return new InlineMenuRowBuilder();
    }

    public static class InlineMenuRowBuilder {

        private final List<InlineMenuButton> buttons = new ArrayList<>();

        /**
         * Creates a builder for a new {@link InlineMenuButton}
         *
         * @param consumer A consumer for editing the button that will be added
         *
         * @return This instance
         */
        public InlineMenuRowBuilder newButton(Consumer<InlineMenuButton.InlineMenuButtonBuilder> consumer) {
            InlineMenuButton.InlineMenuButtonBuilder child = InlineMenuButton.builder();
            consumer.accept(child);
            buttons.add(child.build());
            return this;
        }

        /**
         * Builds a new row from this builder.
         *
         * @return The row
         */
        public InlineMenuRow build() {
            return new InlineMenuRow(buttons);
        }

    }

}
