package xyz.nickr.telepad.menu;

import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.user.User;

/**
 * Represents a single button on an inline menu
 *
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class InlineMenuButton {

    private final String text;
    private final BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback;

    /**
     * Creates a button that opens a menu when clicked.
     *
     * @param text The button's text
     * @param menu The menu to be displayed when the button is clicked
     *
     * @return The button
     */
    public static InlineMenuButton newOpenMenuButton(String text, InlineMenu menu) {
        return builder()
                .text(text)
                .callback((m, u) -> {
                        m.setMenu(menu);
                        return null;
                })
                .build();
    }

    /**
     * Creates a builder for making a button.
     *
     * @return The builder
     */
    public static InlineMenuButtonBuilder builder() {
        return new InlineMenuButtonBuilder();
    }

    public static class InlineMenuButtonBuilder {

        private String text;
        private BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback;

        /**
         * Sets the text shown on the message by this menu.
         *
         * @param text The text
         *
         * @return This instance
         */
        public InlineMenuButtonBuilder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the callback to be called when a user clicks on the button.
         *
         * @param callback The callback
         *
         * @return This instance
         */
        public InlineMenuButtonBuilder callback(BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Builds a new button from this builder.
         *
         * @return The button
         */
        public InlineMenuButton build() {
            return new InlineMenuButton(text, callback);
        }

    }

}
