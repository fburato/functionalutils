package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.ChainComparator1;
import com.github.fburato.functionalutils.api.ChainableComparator;
import com.github.fburato.functionalutils.api.ComparatorDecorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class TypeSafeChainComparator<T> implements ChainableComparator<T> {

    private final ComparatorDecorator comparatorDecorator;
    private final List<BiFunction<T, T, Integer>> comparisons;

    private TypeSafeChainComparator(ComparatorDecorator decorator, List<BiFunction<T, T, Integer>> comparisons) {
        this.comparatorDecorator = decorator;
        this.comparisons = List.copyOf(comparisons);
    }

    public static <S> TypeSafeChainComparator<S> create(final Class<S> clazz) {
        return createWithDecorator(clazz, ComparatorDecorators.identity);
    }

    public static <S> TypeSafeChainComparator<S> createNullSafe(final Class<S> clazz) {
        return createWithDecorator(clazz, ComparatorDecorators.nullSafe);
    }

    public static <S> TypeSafeChainComparator<S> createWithDecorator(final Class<S> clazz,
            ComparatorDecorator decorator) {
        return new TypeSafeChainComparator<>(decorator, Collections.emptyList());
    }

    @Override
    public <S> TypeSafeChainComparator<T> chain(final Function<T, S> fieldGetter, final Comparator<S> comparator) {
        final Comparator<S> decorated = comparatorDecorator.decorate(comparator);
        final List<BiFunction<T, T, Integer>> copy = new ArrayList<>(comparisons);
        copy.add((o1, o2) -> decorated.compare(fieldGetter.apply(o1), fieldGetter.apply(o2)));
        return new TypeSafeChainComparator<>(comparatorDecorator, copy);
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