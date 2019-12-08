package com.github.fburato.functionalutils.codegen;

import com.github.fburato.functionalutils.api.*;
import com.github.fburato.functionalutils.codegen.compiler.InMemoryJavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChainShowGenerator compiled comparators should")
public class ChainShowGeneratorTest {

    private final InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

    private Map<String, Class<?>> compileNShow(int n) {
        try {
            for (int i = 1; i <= n; i++) {
                compiler.addSource(String.format("com.github.fburato.functionalutils.api.Function%d", i),
                        generateFunctionSource(i));
                compiler.addSource(String.format("com.github.fburato.functionalutils.api.ChainShow%d", i),
                        generateChainShowSource(i, i == n));
            }
            return getChainShows(compiler.compileAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Class<?>> getChainShows(Map<String, Class<?>> allCompiled) {
        final Map<String, Class<?>> result = new HashMap<>();
        allCompiled.entrySet().stream().filter(entry -> entry.getKey().contains("ChainShow"))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return Map.copyOf(result);
    }

    private String generateChainShowSource(int index, boolean isTerminal) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final ChainShowGenerator generator = new ChainShowGenerator(index, isTerminal);
        generator.generate(writer);
        return stringWriter.toString();
    }

    private String generateFunctionSource(int index) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final FunctionGenerator generator = new FunctionGenerator(index);
        generator.generate(writer);
        return stringWriter.toString();
    }

    @Test
    @DisplayName("not throw exception")
    void success() {
        compileNShow(3);
    }

    @Test
    @DisplayName("extend show interface")
    void extendComparator() {
        assertThat(compileNShow(5).values()).allSatisfy(c -> assertThat(c.getInterfaces()).containsExactly(Show.class))
                .allSatisfy(c -> assertThat(c.getSuperclass()).isEqualTo(Object.class));
    }

    @Test
    @DisplayName("contain the appropriate number of show fields")
    void fields() {
        compileNShow(7).forEach(this::verifyFieldsFor);
    }

    private void verifyFieldsFor(String className, Class<?> chainedShow) {
        final var number = extractIndex(className);
        IntStream.rangeClosed(1, number).forEach(i -> rethrow(() -> {
            final var field = chainedShow.getDeclaredField(String.format("show%d", i));
            assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
            assertThat(Modifier.isPrivate(field.getModifiers())).isTrue();
            assertThat(field.getType()).isEqualTo(Show.class);
        }));
    }

    private int extractIndex(String className) {
        return Integer.parseInt(className.replace("com.github.fburato.functionalutils.api.ChainShow", ""));
    }

    private <E extends Throwable> void rethrow(RunnableWithException<E> runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("contain a chainable show field")
    void chainableComparatorField() {
        compileNShow(3).values().forEach(c -> rethrow(() -> {
            final var chainableShowField = c.getDeclaredField("chainableShow");
            assertThat(Modifier.isFinal(chainableShowField.getModifiers())).isTrue();
            assertThat(chainableShowField.getType()).isEqualTo(ChainableShow.class);
            assertThat(Modifier.isPrivate(chainableShowField.getModifiers())).isTrue();
        }));
    }

    @Test
    @DisplayName("define show")
    void compareExists() {
        compileNShow(2).values().forEach(c -> rethrow(() -> {
            final var showMethod = c.getDeclaredMethod("show", Object.class);
            assertThat(Modifier.isPublic(showMethod.getModifiers())).isTrue();
            assertThat(showMethod.getReturnType()).isEqualTo(String.class);
            assertThat(showMethod.getParameterTypes()).containsExactly(Object.class);
        }));
    }

    @Test
    @DisplayName("define constructor with many show arguments and one chainableShow")
    void constructor() {
        compileNShow(7).forEach(this::verifyConstructorFor);
    }

    private void verifyConstructorFor(String className, Class<?> chainedShow) {
        final var number = extractIndex(className);
        final var constructors = chainedShow.getConstructors();
        assertThat(constructors).hasSize(1);
        final var constructor = constructors[0];
        final var parameters = constructor.getParameters();
        assertThat(parameters).hasSize(number + 1);
        assertThat(parameters[0].getType()).isEqualTo(ChainableShow.class);
        final var otherParameters = new LinkedList<>(Arrays.asList(parameters));
        otherParameters.remove(0);
        assertThat(otherParameters).allSatisfy(p -> assertThat(p.getType()).isEqualTo(Show.class));
    }

    @Test
    @DisplayName("define a basic chain method")
    void basicChain() {
        compileNShow(2).values().forEach(c -> rethrow(() -> {
            final var basicChainMethod = c.getDeclaredMethod("chain", Function.class, Show.class);
            assertThat(basicChainMethod).isNotNull();
            assertThat(basicChainMethod.getReturnType()).isEqualTo(c);
        }));
    }

    @Test
    @DisplayName("define a standard chain method")
    void standardChain() {
        compileNShow(2).values().forEach(c -> rethrow(() -> {
            final var basicChainMethod = c.getDeclaredMethod("standardChain", Function.class);
            assertThat(basicChainMethod).isNotNull();
            assertThat(basicChainMethod.getReturnType()).isEqualTo(c);
        }));
    }

    @Test
    @DisplayName("define addShow for non terminal comparators")
    void addComparatorNonTerminal() {
        final var comparators = compileNShow(3).entrySet();
        comparators.stream().filter(entry -> !entry.getKey().contains("3"))
                .forEach(entry -> rethrow(() -> verifyNonTerminalAddComparator(entry.getKey(), entry.getValue())));
    }

    private void verifyNonTerminalAddComparator(String className, Class<?> chainShow) throws Exception {
        final var number = extractIndex(className);
        final var addComparator = chainShow.getMethod("addShow", Show.class);
        assertThat(addComparator.getReturnType().getCanonicalName())
                .isEqualTo(String.format("com.github.fburato.functionalutils.api.ChainShow%d", number + 1));
    }

    @Test
    @DisplayName("not define addShow to terminal comparator")
    void noAddComparatorTerminal() {
        final var comparators = compileNShow(3).entrySet();
        comparators.stream().filter(entry -> entry.getKey().contains("3")).forEach(entry -> rethrow(() -> {
            final var addComparator = Arrays.stream(entry.getValue().getDeclaredMethods())
                    .filter(m -> m.getName().equals("addShow"));
            assertThat(addComparator).isEmpty();
        }));
    }

    @Test
    @DisplayName("generate as many show method as indexes")
    void chainMethods() {
        compileNShow(5).forEach(this::verifyChainMethods);
    }

    private void verifyChainMethods(String name, Class<?> chainedShow) {
        final var index = extractIndex(name);
        final var chainMethods = Arrays.stream(chainedShow.getMethods())
                .filter(m -> m.getName().equals("chain") && m.getParameterCount() == 1).collect(Collectors.toList());
        assertThat(chainMethods).hasSize(index).allSatisfy(m -> {
            assertThat(Arrays.stream(m.getParameterTypes()).map(Class::getName))
                    .allMatch(s -> s.startsWith("com.github.fburato.functionalutils.api.Function"));
            assertThat(Arrays.stream(m.getParameterTypes()))
                    .allMatch(t -> Arrays.asList(t.getInterfaces()).contains(FunctionLike.class));
            assertThat(m.getReturnType()).isEqualTo(chainedShow);
        });
    }

    @FunctionalInterface
    private interface RunnableWithException<E extends Throwable> {
        void run() throws E;
    }
}
