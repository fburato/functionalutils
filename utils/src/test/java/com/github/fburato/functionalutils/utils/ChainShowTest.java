package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("ChainShow")
class ChainShowTest {

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
        final ChainableShow<Data> mockShow = mock(ChainableShow.class);
        final ChainShow1<Data, String> testee = new ChainShow1<>(mockShow, null);

        @Test
        @DisplayName("should invoke constructor chainable show with arguments")
        void testCall() {
            Function<Data, String> f = Data::getA;
            Show<String> c = String::toString;

            testee.chain(f, c);

            verify(mockShow).chain(f, c);
        }

        @Test
        @DisplayName("should not return itself")
        void testReturn() {
            assertThat(testee.chain(null, null)).isNotSameAs(testee);
        }
    }

    @Nested
    @DisplayName("implicit chain")
    class ImplicitChainTest {
        @SuppressWarnings("unchecked")
        final ChainableShow<Data> mockShow = mock(ChainableShow.class);
        final Show<String> c = String::toString;
        final ChainShow1<Data, String> testee = new ChainShow1<>(mockShow, c);

        @Test
        @DisplayName("should invoke constructor chainable show with construction comparator")
        void testConstruction() {
            @SuppressWarnings("unchecked")
            Function1<Data, String> f = mock(Function1.class);
            Function<Data, String> expected = Data::getA;
            when(f.asFunction()).thenReturn(expected);

            testee.chain(f);

            verify(mockShow).chain(expected, c);
        }

        @Test
        @DisplayName("should not return a copy of itself")
        void testReturn() {
            assertThat(testee.chain(Data::getA)).isNotSameAs(testee);
        }
    }

    @Nested
    @DisplayName("addShow")
    class TestAddShow {
        @SuppressWarnings("unchecked")
        final ChainableShow<Data> mockShow = mock(ChainableShow.class);
        @SuppressWarnings("unchecked")
        final Function1<Data, String> f1 = mock(Function1.class);
        @SuppressWarnings("unchecked")
        final Function2<Data, Integer> f2 = mock(Function2.class);
        final Show<String> cstring = String::toString;
        final Show<Integer> cint = Object::toString;

        final ChainShow1<Data, String> testee = new ChainShow1<>(mockShow, cstring);

        @BeforeEach
        void setUp() {
            when(mockShow.chain(any(), any())).thenReturn(mockShow);
        }

        @Test
        @DisplayName("should return an higher order chain show with all the original shows")
        void testHigherOrder() {
            Function<Data, String> expectedF1 = Data::getA;
            Function<Data, Integer> expectedF2 = Data::getB;
            when(f1.asFunction()).thenReturn(expectedF1);
            when(f2.asFunction()).thenReturn(expectedF2);

            testee.addShow(cint).chain(f1).chain(f2);

            verify(mockShow).chain(expectedF1, cstring);
            verify(mockShow).chain(expectedF2, cint);
        }

        @Test
        @DisplayName("should return higher order chain show which returns a copy of itself on chain")
        void testReturn() {
            ChainShow2<Data, String, Integer> actual = testee.addShow(cint);
            ChainShow2<Data, String, Integer> chain1 = actual.chain(Data::getA);
            ChainShow2<Data, String, Integer> chain2 = chain1.chain(Data::getB);
            ChainShow2<Data, String, Integer> chain3 = chain2.chain(Data::getC, Object::toString);

            assertThat(chain1).isNotSameAs(actual);
            assertThat(chain2).isNotSameAs(chain1);
            assertThat(chain3).isNotSameAs(chain2);
        }

        @Test
        @DisplayName("should return an higher order chain show that has the basic chain")
        void testBasicChain() {
            Function<Data, Object> expectedFbasic = d -> new Object();
            Show<Object> cobj = Object::toString;

            ChainShow2<Data, String, Integer> actual = testee.addShow(cint);
            actual.chain(expectedFbasic, cobj);

            verify(mockShow).chain(expectedFbasic, cobj);
        }

    }

}