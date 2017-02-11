package xyz.nickr.telepad.util;

/**
 * @author Nick Robson
 */
public class Markdown {

    // escapes *, _, [
    public static String escape(String text, boolean brackets) {
        String res = (text == null) ? "null" : (brackets ? text.replace("[", "\\[") : text);
        return res.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }

}
