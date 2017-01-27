package xyz.nickr.telepad.menu;

import java.util.function.BiFunction;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.user.User;

/**
 * @author Nick Robson
 */
@Getter
@Builder
@EqualsAndHashCode
public class InlineMenuButton {

    private final String text;
    private final BiFunction<InlineMenuMessage, User, InlineMenuButtonResponse> callback;

}