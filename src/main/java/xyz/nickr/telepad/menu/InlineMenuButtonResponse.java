package xyz.nickr.telepad.menu;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Nick Robson
 */
@Getter
@Builder
public class InlineMenuButtonResponse {

    private final String text;
    private final boolean alert;

}
