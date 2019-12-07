package com.github.fburato.functionalutils.codegen;

import com.github.fburato.functionalutils.api.ChainableComparator;
import com.github.fburato.functionalutils.codegen.compiler.InMemoryJavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChainComparatorGenerator compiled comparators should")
public class ChainComparatorGeneratorTest {

    private final InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

    private Map<String, Class<?>> compileNComparators(int n) {
        try {
            for (int i = 1; i <= n; i++) {
                compiler.addSource(String.format("com.github.fburato.functionalutils.api.Function%d", i),
                        generateFunctionSource(i));
                compiler.addSource(String.format("com.github.fburato.functionalutils.api.ChainComparator%d", i),
                        generateChainComparatorSource(i, i == n));
            }
            return getChainableComparators(compiler.compileAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Class<?>> getChainableComparators(Map<String, Class<?>> allCompiled) {
        final Map<String, Class<?>> result = new HashMap<>();
        allCompiled.entrySet().stream().filter(entry -> entry.getKey().contains("ChainComparator"))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return Map.copyOf(result);
    }

    private String generateChainComparatorSource(int index, boolean isTerminal) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final ChainComparatorGenerator generator = new ChainComparatorGenerator(index, isTerminal);
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
        compileNComparators(3);
    }

    @Test
    @DisplayName("extend comparator interface")
    void extendComparator() {
        assertThat(compileNComparators(5).values())
                .allSatisfy(c -> assertThat(c.getInterfaces()).containsExactly(Comparator.class))
                .allSatisfy(c -> assertThat(c.getSuperclass()).isEqualTo(Object.class));
    }

    @Test
    @DisplayName("contain the appropriate number of comparator fields")
    void fields() {
        compileNComparators(7).forEach(this::verifyFieldsFor);
    }

    private void verifyFieldsFor(String className, Class<?> chainedComparator) {
        final var number = Integer
                .parseInt(className.replace("com.github.fburato.functionalutils.api.ChainComparator", ""));
        IntStream.rangeClosed(1, number).forEach(i -> rethrow(() -> {
            final var field = chainedComparator.getDeclaredField(String.format("comparator%d", i));
            assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
            assertThat(Modifier.isPrivate(field.getModifiers())).isTrue();
            assertThat(field.getType()).isEqualTo(Comparator.class);
        }));
    }

    private <E extends Throwable> void rethrow(RunnableWithException<E> runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("contain a chainable comparator field")
    void chainableComparatorField() {
        compileNComparators(3).values().forEach(c -> rethrow(() -> {
            final var chainableComparatorField = c.getDeclaredField("chainableComparator");
            assertThat(Modifier.isFinal(chainableComparatorField.getModifiers())).isTrue();
            assertThat(chainableComparatorField.getType()).isEqualTo(ChainableComparator.class);
            assertThat(Modifier.isPrivate(chainableComparatorField.getModifiers())).isTrue();
        }));
    }

    @Test
    @DisplayName("define compare")
    void compareExists() {
        compileNComparators(2).values().forEach(c -> rethrow(() -> {
            final var compare = c.getDeclaredMethod("compare", Object.class, Object.class);
            assertThat(Modifier.isPublic(compare.getModifiers())).isTrue();
            assertThat(compare.getReturnType()).isEqualTo(int.class);
            assertThat(compare.getParameterTypes()).containsExactly(Object.class, Object.class);
        }));
    }

    @Test
    @DisplayName("define constructor with many comparator arguments and one chainableComparator")
    void constructor() {
        compileNComparators(7).forEach(this::verifyConstructorFor);
    }

    private void verifyConstructorFor(String className, Class<?> chainedComparator) {
        final var number = Integer
                .parseInt(className.replace("com.github.fburato.functionalutils.api.ChainComparator", ""));
        final var constructors = chainedComparator.getConstructors();
        assertThat(constructors).hasSize(1);
        final var constructor = constructors[0];
        final var parameters = constructor.getParameters();
        assertThat(parameters).hasSize(number + 1);
        assertThat(parameters[0].getType()).isEqualTo(ChainableComparator.class);
        final var otherParameters = new LinkedList<>(Arrays.asList(parameters));
        otherParameters.remove(0);
        assertThat(otherParameters).allSatisfy(p -> assertThat(p.getType()).isEqualTo(Comparator.class));
    }

    @Test
    @DisplayName("define a basic chain method")
    void basicChain() {
        compileNComparators(2).values().forEach(c -> rethrow(() -> {
            final var basicChainMethod = c.getDeclaredMethod("chain", Function.class, Comparator.class);
            assertThat(basicChainMethod).isNotNull();
            assertThat(basicChainMethod.getReturnType()).isEqualTo(c);
        }));
    }

    @Test
    @DisplayName("define add comparator for non terminal comparators")
    void addComparatorNonTerminal() {
        final var comparators = compileNComparators(3).entrySet();
        comparators.stream().filter(entry -> !entry.getKey().contains("3"))
                .forEach(entry -> rethrow(() -> verifyNonTerminalAddComparator(entry.getKey(), entry.getValue())));
    }

    private void verifyNonTerminalAddComparator(String className, Class<?> chainComparator) throws Exception {
        final var number = Integer
                .parseInt(className.replace("com.github.fburato.functionalutils.api.ChainComparator", ""));
        final var addComparator = chainComparator.getMethod("addComparator", Comparator.class);
        assertThat(addComparator.getReturnType().getCanonicalName())
                .isEqualTo(String.format("com.github.fburato.functionalutils.api.ChainComparator%d", number + 1));
    }

    @Test
    @DisplayName("not define add comparator to terminal comparator")
    void noAddComparatorTerminal() {
        final var comparators = compileNComparators(3).entrySet();
        comparators.stream().filter(entry -> entry.getKey().contains("3")).forEach(entry -> rethrow(() -> {
            final var addComparator = Arrays.stream(entry.getValue().getDeclaredMethods())
                    .filter(m -> m.getName().equals("addComparator"));
            assertThat(addComparator).isEmpty();
        }));
    }

    @Test
    @DisplayName("generate as many chain method as indexes")
    void chainMethods() {
        compileNComparators(2).values().forEach(c -> rethrow(() -> {
            final var basicChainMethod = c.getDeclaredMethod("chain", Function.class, Comparator.class);
            assertThat(basicChainMethod).isNotNull();
            assertThat(basicChainMethod.getReturnType()).isEqualTo(c);
        }));
    }

    private void verifyChainMethod(String name, Class<?> chainedComparator) {
        final var index = extractIndex(name);

    }

    private int extractIndex(String className) {
        return Integer.parseInt(className.replace("com.github.fburato.functionalutils.api.ChainComparator", ""));
    }

    @FunctionalInterface
    private interface RunnableWithException<E extends Throwable> {
        void run() throws E;
    }
}
