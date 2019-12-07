package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("ChainComparator")
class ChainComparatorsTest {

    static class Data {
        private String a;
        private int b;
        private double c;

        public Data() {
        }

        public Data(String a, int b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        public double getC() {
            return c;
        }

        public void setC(double c) {
            this.c = c;
        }
    }

    @Nested
    @DisplayName("base chain")
    class BaseChainTest {
        @SuppressWarnings("unchecked")
        final ChainableComparator<Data> mockComparator = mock(ChainableComparator.class);
        final ChainComparator1<Data, String> testee = new ChainComparator1<>(mockComparator, null);

        @Test
        @DisplayName("should invoke constructor chainable comparator with arguments")
        void testCall() {
            Function<Data, String> f = Data::getA;
            Comparator<String> c = String::compareTo;

            testee.chain(f, c);

            verify(mockComparator).chain(f, c);
        }

        @Test
        @DisplayName("should return the itself")
        void testReturn() {
            assertThat(testee.chain(null, null)).isSameAs(testee);
        }
    }

    @Nested
    @DisplayName("implicit chain")
    class ImplicitChainTest {
        @SuppressWarnings("unchecked")
        final ChainableComparator<Data> mockComparator = mock(ChainableComparator.class);
        final Comparator<String> c = String::compareTo;
        final ChainComparator1<Data, String> testee = new ChainComparator1<>(mockComparator, c);

        @Test
        @DisplayName("should invoke constructor chainable comparator with construction comparator")
        void testConstruction() {
            @SuppressWarnings("unchecked")
            Function1<Data, String> f = mock(Function1.class);
            Function<Data, String> expected = Data::getA;
            when(f.asFunction()).thenReturn(expected);

            testee.chain(f);

            verify(mockComparator).chain(expected, c);
        }

        @Test
        @DisplayName("should return the itself")
        void testReturn() {
            assertThat(testee.chain(Data::getA)).isSameAs(testee);
        }
    }

    @Nested
    @DisplayName("addComparator")
    class TestAddComparator {
        @SuppressWarnings("unchecked")
        final ChainableComparator<Data> mockComparator = mock(ChainableComparator.class);
        @SuppressWarnings("unchecked")
        final Function1<Data, String> f1 = mock(Function1.class);
        @SuppressWarnings("unchecked")
        final Function2<Data, Integer> f2 = mock(Function2.class);
        final Comparator<String> cstring = String::compareTo;
        final Comparator<Integer> cint = Integer::compareTo;

        final ChainComparator1<Data, String> testee = new ChainComparator1<>(mockComparator, cstring);

        @Test
        @DisplayName("should return an higher order chain comparator with all the original comparators")
        void testHigherOrder() {
            Function<Data, String> expectedF1 = Data::getA;
            Function<Data, Integer> expectedF2 = Data::getB;
            when(f1.asFunction()).thenReturn(expectedF1);
            when(f2.asFunction()).thenReturn(expectedF2);

            ChainComparator2<Data, String, Integer> actual = testee.addComparator(cint);
            actual.chain(f1);
            actual.chain(f2);

            verify(mockComparator).chain(expectedF1, cstring);
            verify(mockComparator).chain(expectedF2, cint);
        }

        @Test
        @DisplayName("should return higher order chain comparator which returns itself on chain")
        void testReturn() {
            ChainComparator2<Data, String, Integer> actual = testee.addComparator(cint);
            ChainComparator2<Data, String, Integer> chain1 = actual.chain(Data::getA);
            ChainComparator2<Data, String, Integer> chain2 = actual.chain(Data::getB);
            ChainComparator2<Data, String, Integer> chain3 = actual.chain(Data::getC, Double::compareTo);

            assertThat(chain1).isSameAs(actual);
            assertThat(chain2).isSameAs(actual);
            assertThat(chain3).isSameAs(actual);
        }

        @Test
        @DisplayName("should return an higher order chain comparator that has the basic chain")
        void testBasicChain() {
            Function<Data, Object> expectedFbasic = d -> new Object();
            Comparator<Object> cobj = (o1, o2) -> -1;

            ChainComparator2<Data, String, Integer> actual = testee.addComparator(cint);
            actual.chain(expectedFbasic, cobj);

            verify(mockComparator).chain(expectedFbasic, cobj);
        }

    }

}