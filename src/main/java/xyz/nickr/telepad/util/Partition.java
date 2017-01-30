package xyz.nickr.telepad.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * @author Nick Robson
 */
public class Partition {

    public static <T> List<List<T>> partition(List<T> list, int chunk) {
        Function<List<T>, List<T>> listMaker = list instanceof RandomAccess ? ArrayList::new : LinkedList::new;
        List<List<T>> partition = list instanceof RandomAccess ? new ArrayList<>() : new LinkedList<>();
        int c = 0, l = list.size();
        while (c + chunk < l) {
            partition.add(listMaker.apply(list.subList(c, c + chunk)));
            c += chunk;
        }
        if (c < l) {
            partition.add(listMaker.apply(list.subList(c, l)));
        }
        return partition;
    }

}
