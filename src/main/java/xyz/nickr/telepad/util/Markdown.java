package xyz.nickr.telepad.util;

/**
 * Handles escaping of markdown.
 *
 * @author Nick Robson
 */
public class Markdown {

    /**
     * Escapes text so it won't interfere with Markdown formatting.
     * <br><br>
     * Escapes: &nbsp;{@code * _ ` [}
     *
     * @param text The text to escape.
     * @param brackets Whether or not to escape brackets,
     *                 for example you may not want to escape
     *                 brackets in bold or italic tags as they
     *                 will not form links, anyway.
     *
     * @return The escaped text.
     */
    public static String escape(String text, boolean brackets) {
        String res = (text == null) ? "null" : (brackets ? text.replace("[", "\\[") : text);
        return res.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }

}
