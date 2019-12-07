package com.github.fburato.functionalutils.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Builder should")
public class BuilderTest {

    private ABuilder baseBuilder() {
        return new ABuilder();
    }

    @Test
    @DisplayName("build with default parameters")
    void testDefault() {
        final var actual = baseBuilder().build();

        verifyAB(actual, null, 0);
    }

    private void verifyAB(A actual, String a, int b) {
        assertThat(actual.getA()).isEqualTo(a);
        assertThat(actual.getB()).isEqualTo(b);
    }

    @Test
    @DisplayName("chain with clause and add new parameters")
    void testBaseChain() {
        final var actual = baseBuilder().with(a -> a.a = "hello").build();

        verifyAB(actual, "hello", 0);
    }

    @Test
    @DisplayName("chain multiple with clauses and add new parameters")
    void testMultiChain() {
        final var actual = baseBuilder().with(a -> a.a = "world").with(a -> a.b = 6).build();

        verifyAB(actual, "world", 6);
    }

    @Test
    @DisplayName("chain multiple with clauses and override latest writes")
    void testMultiChainOverride() {
        final var actual = baseBuilder().with(a -> a.a = "world").with(a -> a.b = 4).with(a -> a.a = "foobar").build();

        verifyAB(actual, "foobar", 4);
    }

    @Test
    @DisplayName("not mutate the state of previous builder in the chain")
    void testImmutability() {
        final var builder1 = baseBuilder().with(a -> a.a = "world");
        final var builder2 = builder1.with(a -> {
            a.a = "hello";
            a.b = 42;
        });

        verifyAB(builder1.build(), "world", 0);
        verifyAB(builder2.build(), "hello", 42);
    }

    @Test
    @DisplayName("produce different objects")
    void differentObjects() {
        final var builder = baseBuilder().with(a -> {
            a.a = "hello";
            a.b = 42;
        });
        final var a1 = builder.build();
        final var a2 = builder.build();

        assertThat(a1).isNotSameAs(a2);
        verifyAB(a1, "hello", 42);
        verifyAB(a2, "hello", 42);
    }

    static class A {
        public final String a;
        private int b;

        public A(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    static class ABuilder extends Builder<A, ABuilder> {
        public String a;
        public int b;

        private ABuilder() {
            super(ABuilder::new);
        }

        @Override
        protected A makeValue() {
            final var result = new A(a);
            result.setB(b);
            return result;
        }
    }
}
