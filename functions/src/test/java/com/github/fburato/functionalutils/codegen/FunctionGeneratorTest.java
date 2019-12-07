package com.github.fburato.functionalutils.codegen;

import com.github.fburato.functionalutils.api.FunctionLike;
import com.github.fburato.functionalutils.codegen.compiler.InMemoryJavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FunctionGenerator should")
public class FunctionGeneratorTest {

    private final InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

    @Test
    @DisplayName("compile")
    void compile() throws Exception {
        compileFunctionWithSuffix(1);
    }

    private Class<?> compileFunctionWithSuffix(int suffix) throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final FunctionGenerator testee = new FunctionGenerator(suffix);
        testee.generate(writer);
        return compiler.compile(String.format("com.github.fburato.functionalutils.api.Function%d", suffix),
                stringWriter.toString());
    }

    @Test
    @DisplayName("produce extension of FunctionLike")
    void extendFunctionLike() throws Exception {
        final var compiled = compileFunctionWithSuffix(1);
        assertThat(compiled.getInterfaces()).containsExactly(FunctionLike.class);
    }

    @Test
    @DisplayName("produce multiple classes")
    void multipleInstances() throws Exception {
        final var function1 = compileFunctionWithSuffix(1);
        final var function2 = compileFunctionWithSuffix(2);

        assertThat(function1.getInterfaces()).containsExactly(FunctionLike.class);
        assertThat(function2.getInterfaces()).containsExactly(FunctionLike.class);
    }

    @Test
    @DisplayName("produce interface")
    void produceInterface() throws Exception {
        final var compiled = compileFunctionWithSuffix(1);

        assertThat(compiled.isInterface()).isTrue();
    }

    @Test
    @DisplayName("produce interface named Function+prefix")
    void name() throws Exception {
        final var compiled = compileFunctionWithSuffix(23);

        assertThat(compiled.getSimpleName()).isEqualTo("Function23");
    }

    @Test
    @DisplayName("produce interface that does not contain declared methods")
    void methods() throws Exception {
        final var compiled = compileFunctionWithSuffix(22);

        assertThat(compiled.getDeclaredMethods()).isEmpty();
    }

    @Test
    @DisplayName("produce interface which is annotated with @FunctionalInterface")
    void annotated() throws Exception {
        final var compiled = compileFunctionWithSuffix(31);

        assertThat(compiled).hasAnnotations(FunctionalInterface.class);
    }
}
