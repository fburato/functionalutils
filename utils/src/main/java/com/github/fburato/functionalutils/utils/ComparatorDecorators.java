package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.ComparatorDecorator;

import java.util.Comparator;

public final class ComparatorDecorators {

    public static final ComparatorDecorator identity = new ComparatorDecorator() {
        @Override
        public <S> Comparator<S> decorate(Comparator<S> comparator) {
            return comparator;
        }
    };

    public static final ComparatorDecorator nullSafe = new ComparatorDecorator() {
        @Override
        public <S> Comparator<S> decorate(Comparator<S> comparator) {
            return (s1, s2) -> {
                if (s1 == null && s2 == null) {
                    return 0;
                } else if (s1 == null) {
                    return -1;
                } else if (s2 == null) {
                    return 1;
                } else {
                    return comparator.compare(s1, s2);
                }
            };
        }
    };
}