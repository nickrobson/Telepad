package xyz.nickr.telepad.menu;

import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.user.User;

/**
 * @author Nick Robson
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class InlineMenuButton {

    private final String text;
    private final BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback;

    public static InlineMenuButton newOpenMenuButton(String text, InlineMenu menu) {
        return builder()
                .text(text)
                .callback((m, u) -> {
                        m.setMenu(menu);
                        return null;
                })
                .build();
    }

    public static InlineMenuButtonBuilder builder() {
        return new InlineMenuButtonBuilder();
    }

    public static class InlineMenuButtonBuilder {

        private String text;
        private BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback;

        public InlineMenuButtonBuilder text(String text) {
            this.text = text;
            return this;
        }

        public InlineMenuButtonBuilder callback(BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback) {
            this.callback = callback;
            return this;
        }

        public InlineMenuButton build() {
            return new InlineMenuButton(text, callback);
        }

    }

}
