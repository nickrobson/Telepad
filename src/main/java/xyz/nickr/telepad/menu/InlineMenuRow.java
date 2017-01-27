package xyz.nickr.telepad.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
public class InlineMenuRow {

    private final List<InlineMenuButton> buttons;

    public InlineMenuRow(List<InlineMenuButton> buttons) {
        this.buttons = Collections.unmodifiableList(buttons);
    }

    public static InlineMenuRowBuilder builder() {
        return new InlineMenuRowBuilder();
    }

    public static class InlineMenuRowBuilder {

        private final List<InlineMenuButton> buttons = new ArrayList<>();

        public InlineMenuRowBuilder newButton(Consumer<InlineMenuButton.InlineMenuButtonBuilder> consumer) {
            InlineMenuButton.InlineMenuButtonBuilder child = InlineMenuButton.builder();
            consumer.accept(child);
            buttons.add(child.build());
            return this;
        }

        public InlineMenuRow build() {
            return new InlineMenuRow(buttons);
        }

    }

}
