package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.ChainComparator1;
import com.github.fburato.functionalutils.api.ChainableComparator;
import com.github.fburato.functionalutils.api.ComparatorDecorator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class TypeSafeChainComparator<T> implements ChainableComparator<T> {

    private final ComparatorDecorator comparatorDecorator;
    private final List<BiFunction<T, T, Integer>> comparisons = new ArrayList<>();

    private TypeSafeChainComparator(ComparatorDecorator decorator) {
        this.comparatorDecorator = decorator;
    }

    public static <S> TypeSafeChainComparator<S> create() {
        return createWithDecorator(ComparatorDecorators.identity);
    }

    public static <S> TypeSafeChainComparator<S> createNullSafe() {
        return createWithDecorator(ComparatorDecorators.nullSafe);
    }

    public static <S> TypeSafeChainComparator<S> createWithDecorator(ComparatorDecorator decorator) {
        return new TypeSafeChainComparator<>(decorator);
    }

    @Override
    public <S> TypeSafeChainComparator<T> chain(final Function<T, S> fieldGetter, final Comparator<S> comparator) {
        final Comparator<S> decorated = comparatorDecorator.decorate(comparator);
        comparisons.add((o1, o2) -> decorated.compare(fieldGetter.apply(o1), fieldGetter.apply(o2)));
        return this;
    }

    @Override
    public int compare(T o1, T o2) {
        for (BiFunction<T, T, Integer> i : comparisons) {
            final Integer value = i.apply(o1, o2);
            if (value != 0) {
                return value;
            }
        }
        return 0;
    }

    public <T1> ChainComparator1<T, T1> addComparator(Comparator<T1> comparator1) {
        return new ChainComparator1<>(this, comparator1);
    }
}