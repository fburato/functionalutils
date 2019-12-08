package com.github.fburato.functionalutils.api;

@FunctionalInterface
public interface ShowLike<T> {
    String show(T t);

    default Show<T> asShow() {
        return this::show;
    }
}
