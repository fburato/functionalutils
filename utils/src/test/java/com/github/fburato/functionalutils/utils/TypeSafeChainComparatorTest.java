package com.github.fburato.functionalutils.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TypeSafeChainComparator")
class TypeSafeChainComparatorTest {
    static class Data {
        private String a;
        private String a1;
        private int b;
        private double c;

        public Data() {
        }

        public Data(String a, String a1, int b, double c) {
            this.a = a;
            this.a1 = a1;
            this.b = b;
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getA1() {
            return a1;
        }

        public void setA1(String a1) {
            this.a1 = a1;
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
    @DisplayName("created with create")
    class CreateTest {

        final TypeSafeChainComparator<Data> testee = TypeSafeChainComparator.create();

        @BeforeEach
        void setUp() {
            testee.chain(Data::getA, String::compareTo).chain(Data::getA1, String::compareTo)
                    .chain(Data::getB, Integer::compareTo).chain(Data::getC, Double::compareTo);
        }

        @Test
        @DisplayName("should compare elements in order of chain invocations")
        void testOrder() {
            assertThat(testee.compare(new Data("1", null, 345, 234), new Data("2", null, 0, 432))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 0, 23423.0), new Data("1", "2", 0, 0.0))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 1, 0.0), new Data("1", "1", 0, 23423.0))).isGreaterThan(0);
            assertThat(testee.compare(new Data("1", "1", 1, 0.0), new Data("1", "1", 1, 1.0))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 1, 1.0), new Data("1", "1", 1, 1.0))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("created with createNullSafe")
    class TestNullSafe {
        final TypeSafeChainComparator<Data> testee = TypeSafeChainComparator.createNullSafe();

        final Comparator<String> stringLengthComparator = Comparator.comparingInt(String::length);

        @BeforeEach
        void setUp() {
            testee.chain(Data::getA, stringLengthComparator).chain(Data::getA1, stringLengthComparator)
                    .chain(Data::getB, (i1, i2) -> stringLengthComparator.compare(i1.toString(), i2.toString()))
                    .chain(Data::getC, (d1, d2) -> stringLengthComparator.compare(d1.toString(), d2.toString()));
        }

        @Test
        @DisplayName("should compare elements in order of chain invocations")
        void testOrder() {
            assertThat(testee.compare(new Data("1", null, 345, 234), new Data("11", null, 0, 432))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 0, 23423.0), new Data("1", "11", 0, 0.0))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 10, 0.0), new Data("1", "1", 0, 23423.0))).isGreaterThan(0);
            assertThat(testee.compare(new Data("1", "1", 1, 0.0), new Data("1", "1", 1, 10.0))).isLessThan(0);
            assertThat(testee.compare(new Data("1", "1", 1, 1.0), new Data("1", "1", 1, 1.0))).isEqualTo(0);
        }

        @Test
        @DisplayName("should not fail with null values")
        void testNullSafety() {
            assertThat(testee.compare(new Data(null, null, 345, 234), new Data("11", null, 0, 432))).isLessThan(0);
            assertThat(testee.compare(new Data(null, "1", 0, 23423.0), new Data(null, "11", 0, 0.0))).isLessThan(0);
            assertThat(testee.compare(new Data(null, null, 10, 0.0), new Data(null, null, 0, 23423.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new Data(null, null, 1, 0.0), new Data(null, null, 1, 10.0))).isLessThan(0);
            assertThat(testee.compare(new Data(null, null, 1, 1.0), new Data(null, null, 1, 1.0))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("addComparator")
    class AddComparatorTest {

        final Comparator<String> stringLengthComparator = Comparator.comparingInt(String::length);

        final Comparator<Data> testee = TypeSafeChainComparator.<Data>createNullSafe()
                .addComparator(stringLengthComparator).addComparator(Integer::compareTo)
                .addComparator(Double::compareTo).chain(Data::getA1).chain(Data::getA).chain(Data::getC)
                .chain(Data::getB);

        @Test
        @DisplayName("should reuse same comparator for same types")
        void testSameComparator() {
            assertThat(testee.compare(new Data("111", "1", 345, 234), new Data("11", "11", 0, 432))).isLessThan(0);
            assertThat(testee.compare(new Data("", "111", 0, 23423.0), new Data("a", "11", 0, 0.0))).isGreaterThan(0);
            assertThat(testee.compare(new Data("", "1", 10, 0.0), new Data(null, null, 0, 23423.0))).isGreaterThan(0);
            assertThat(testee.compare(new Data("", null, 100, 0.0), new Data("", null, 1, 10.0))).isLessThan(0);
            assertThat(testee.compare(new Data(null, null, 1, 1.0), new Data(null, null, 1, 1.0))).isEqualTo(0);
        }
    }

}