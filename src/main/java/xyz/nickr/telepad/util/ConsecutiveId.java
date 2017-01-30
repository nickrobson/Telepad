package xyz.nickr.telepad.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConsecutiveId {

    private static Set<String> reserved = new HashSet<>();
    private static List<Character> chars = new LinkedList<>();

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

    public static ConsecutiveId reserve(String identifier) {
        if (reserved.contains(identifier))
            return null;
        return new ConsecutiveId(identifier);
    }

    private String curr;

    private ConsecutiveId(String identifier) {
        reserved.add(identifier);
    }

    public String next() {
        if (curr == null) {
            return curr = chars.get(0).toString();
        }
        int idx = chars.indexOf(curr.charAt(curr.length() - 1));
        if (idx == -1) {
            throw new IllegalStateException("char '" + curr.charAt(curr.length() - 1) + "' is not in chars list");
        } else if (idx == chars.size() - 1) {
            curr += chars.get(0);
        } else {
            curr = curr.substring(0, curr.length() - 1) + chars.get(idx + 1);
        }
        return curr;
    }

}
