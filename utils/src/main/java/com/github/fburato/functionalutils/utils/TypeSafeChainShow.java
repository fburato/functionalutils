package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.ChainShow1;
import com.github.fburato.functionalutils.api.ChainableShow;
import com.github.fburato.functionalutils.api.Show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TypeSafeChainShow<T> implements ChainableShow<T> {

    public static final class Configuration<S> {
        public final Function<Class<S>, String> typeShow;
        public final String fieldHeader;
        public final String fieldFooter;
        public final String fieldSeparator;

        public Configuration(Function<Class<S>, String> typeShow, String fieldHeader, String fieldFooter,
                String fieldSeparator) {
            this.typeShow = typeShow;
            this.fieldHeader = fieldHeader;
            this.fieldFooter = fieldFooter;
            this.fieldSeparator = fieldSeparator;
        }
    }

    public static <S> Configuration<S> standardConfiguration() {
        return new Configuration<>(Class::getSimpleName, "(", ")", ",");
    }

    private final Configuration<T> configuration;
    private final List<Function<T, String>> sequencer;
    private final Class<T> clazz;

    private TypeSafeChainShow(Class<T> clazz, Configuration<T> configuration, List<Function<T, String>> sequencer) {
        this.clazz = clazz;
        this.configuration = configuration;
        this.sequencer = List.copyOf(sequencer);
    }

    public static <S> TypeSafeChainShow<S> create(Class<S> clazz) {
        return new TypeSafeChainShow<>(clazz, standardConfiguration(), Collections.emptyList());
    }

    public static <S> TypeSafeChainShow<S> createWithConfig(Class<S> clazz, Configuration<S> configuration) {
        return new TypeSafeChainShow<>(clazz, configuration, Collections.emptyList());
    }

    @Override
    public <S> ChainableShow<T> chain(Function<T, S> fieldGetter, Show<S> sShow) {
        final List<Function<T, String>> copy = new ArrayList<>(sequencer);
        copy.add(t -> sShow.show(fieldGetter.apply(t)));
        return new TypeSafeChainShow<>(clazz, configuration, copy);
    }

    @Override
    public <S> ChainableShow<T> standardChain(Function<T, S> fieldGetter) {
        return chain(fieldGetter, Objects::toString);
    }

    @Override
    public String show(final T t) {
        final String content = sequencer.stream().map(fn -> fn.apply(t))
                .collect(Collectors.joining(configuration.fieldSeparator));
        return String.format("%s%s%s%s", configuration.typeShow.apply(clazz), configuration.fieldHeader, content,
                configuration.fieldFooter);
    }

    public <T1> ChainShow1<T, T1> addShow(final Show<T1> t1Show) {
        return new ChainShow1<>(this, t1Show);
    }
}
