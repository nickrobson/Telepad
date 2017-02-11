package xyz.nickr.telepad.util;

/**
 * @author Nick Robson
 */
@FunctionalInterface
public interface TriFunction<A, B, C, D> {

    D apply(A a, B b, C c);

}