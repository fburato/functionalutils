package com.github.fburato.functionalutils.api;

import java.util.function.Function;

@FunctionalInterface
public interface FunctionLike<T1, T2> {
    T2 apply(T1 value);

    default Function<T1, T2> asFunction() {
        return this::apply;
    }
}
