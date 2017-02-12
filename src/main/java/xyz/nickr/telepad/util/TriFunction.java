package xyz.nickr.telepad.util;

/**
 * Converts three values into a single value.
 *
 * Similar to a {@link java.util.function.Function} and
 * {@link java.util.function.BiFunction}.
 *
 * @param <A> The first value's type.
 * @param <B> The second value's type.
 * @param <C> The third value's type.
 * @param <D> The return type.
 *
 * @author Nick Robson
 */
@FunctionalInterface
public interface TriFunction<A, B, C, D> {

    /**
     * Converts three values into a single value.
     *
     * @param a The first value.
     * @param b The second value.
     * @param c The third value.
     *
     * @return The returned value.
     */
    D apply(A a, B b, C c);

}
