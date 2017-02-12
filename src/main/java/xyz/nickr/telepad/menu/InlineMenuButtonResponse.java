package xyz.nickr.telepad.menu;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents the response sent by the bot to the user who clicked on the button
 *
 * @author Nick Robson
 */
@Builder
public class InlineMenuButtonResponse {

    @NonNull
    private final String text;

    private final boolean alert;

    /**
     * Gets the text to be sent to the user upon clicking the button.
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets whether or not this button response will create an alert box
     * that needs to be exited.
     *
     * @return Whether or not this is an alert.
     */
    public boolean isAlert() {
        return alert;
    }

}
