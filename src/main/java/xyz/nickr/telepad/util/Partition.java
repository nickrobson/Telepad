package xyz.nickr.telepad.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * Partitions a list into sub-lists of sized chunks.
 *
 * @author Nick Robson
 */
public class Partition {

    /**
     * Partitions a list into sub-lists of sized chunks.
     *
     * @param list The list to be partitioned.
     * @param chunk The size of each chunk.
     * @param <T> The type of the list.
     *
     * @return A list of the partitions of the list.
     */
    public static <T> List<List<T>> partition(List<T> list, int chunk) {
        Function<List<T>, List<T>> listMaker = (list instanceof RandomAccess) ? ArrayList::new : LinkedList::new;
        List<List<T>> partition = (list instanceof RandomAccess) ? new ArrayList<>() : new LinkedList<>();
        int c = 0, l = list.size();
        while ((c + chunk) < l) {
            partition.add(listMaker.apply(list.subList(c, c + chunk)));
            c += chunk;
        }
        if (c < l) {
            partition.add(listMaker.apply(list.subList(c, l)));
        }
        return partition;
    }

}
