package xyz.nickr.telepad.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides case-sensitive unique identifiers.
 *
 * The characters used are: [a-zA-Z0-9]
 *
 * @author Nick Robson
 */
public class ConsecutiveId {

    private static final Set<String> reserved = new HashSet<>();
    private static final List<Character> chars = new ArrayList<>();

    static {
        for (char i = 'a', j = 'z'; i <= j; i++) {
            ConsecutiveId.chars.add(i);
        }
        for (char i = 'A', j = 'Z'; i <= j; i++) {
            ConsecutiveId.chars.add(i);
        }
        for (char i = '0', j = '9'; i <= j; i++) {
            ConsecutiveId.chars.add(i);
        }
    }

    /**
     * Reserves a namespace for a new ConsecutiveId instance.
     *
     * @param identifier The namespace identifier.
     *
     * @return The ConsecutiveId instance, if the identifier had not been reserved.
     *          Otherwise returns null.
     */
    public static ConsecutiveId reserve(String identifier) {
        return reserved.contains(identifier) ? null : new ConsecutiveId(identifier);
    }

    private String curr;

    private ConsecutiveId(String identifier) {
        reserved.add(identifier);
    }

    /**
     * Gets the next unique identifier in the sequence.
     *
     * For example:
     * <table>
     *     <tr>
     *         <td>a</td>
     *         <td>-&gt;</td>
     *         <td>b</td>
     *     </tr>
     *     <tr>
     *         <td>z</td>
     *         <td>-&gt;</td>
     *         <td>A</td>
     *     </tr>
     *     <tr>
     *         <td>Z</td>
     *         <td>-&gt;</td>
     *         <td>0</td>
     *     </tr>
     *     <tr>
     *         <td>9</td>
     *         <td>-&gt;</td>
     *         <td>aa</td>
     *     </tr>
     * </table>
     *
     * @return The next identifier.
     */
    public String next() {
        if (curr == null) {
            return curr = Character.toString(chars.get(0));
        }
        char[] currChars = curr.toCharArray();
        int last = currChars.length - 1;
        int idx = chars.indexOf(currChars[last]);
        if (idx != chars.size() - 1) {
            currChars[last] = chars.get(idx + 1);
            return curr = new String(currChars);
        }
        boolean allSame = true;
        char lastChar = chars.get(chars.size() - 1);
        int lastNotLastChar = -1;
        for (int i = currChars.length; i > 0; i--) {
            if (currChars[i - 1] != lastChar) {
                allSame = false;
                lastNotLastChar = i - 1;
                break;
            }
        }
        if (allSame) {
            currChars = new char[currChars.length + 1];
            Arrays.fill(currChars, chars.get(0));
            return curr = new String(currChars);
        }
        idx = chars.indexOf(currChars[lastNotLastChar]);
        currChars[lastNotLastChar] = chars.get(idx + 1);
        Arrays.fill(currChars, lastNotLastChar + 1, currChars.length, chars.get(0));
        return curr = new String(currChars);
    }

}
