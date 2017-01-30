package xyz.nickr.telepad.util;

/**
 * @author Nick Robson
 */
public class Markdown {

    // escapes *, _, [
    public static String escape(String text) {
        StringBuilder sb = new StringBuilder(text);
        for (String c : new String[]{ "*", "_" }) {
            int index = sb.indexOf(c);
            while (index >= 0) {
                int nextIndex = sb.indexOf(c, index + 1);
                if (nextIndex != -1) {
                    sb.insert(index, '\\');
                    nextIndex++;
                }
                index = nextIndex;
            }
        }
        return sb.toString().replace("[", "\\[");
    }

}
