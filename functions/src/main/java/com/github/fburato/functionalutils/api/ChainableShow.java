package com.github.fburato.functionalutils.api;

import java.util.function.Function;

public interface ChainableShow<T> extends Show<T> {
    <S> ChainableShow<T> chain(Function<T, S> fieldGetter, Show<S> sShow);

    <S> ChainableShow<T> standardChain(Function<T, S> fieldGetter);
}
