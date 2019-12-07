package com.github.fburato.functionalutils.api;

import java.util.Comparator;
import java.util.function.Function;

public interface ChainableComparator<T> extends Comparator<T> {
    <S> ChainableComparator<T> chain(Function<T, S> fieldGetter, Comparator<S> sComparator);
}
