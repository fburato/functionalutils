package com.github.fburato.functionalutils.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TypeSafeChainComparator")
class TypeSafeChainComparatorTest {

    @Test
    @DisplayName("chaining should not mutate previous instances")
    void testImmutable() {
        final var data1 = new TestData("1", "1", 0, 0);
        final var data2 = new TestData("1", "2", 0, 0);
        final var comparator1 = TypeSafeChainComparator.create(TestData.class).chain(TestData::getA, String::compareTo);
        assertThat(data1).usingComparator(comparator1).isEqualTo(data2);
        final var comparator2 = comparator1.chain(TestData::getA1, String::compareTo);
        assertThat(data1).usingComparator(comparator1).isEqualTo(data2);
        assertThat(data1).usingComparator(comparator2).isNotEqualTo(data2);
    }

    @Nested
    @DisplayName("created with create")
    class CreateTest {

        final TypeSafeChainComparator<TestData> testee = TypeSafeChainComparator.create(TestData.class)
                .chain(TestData::getA, String::compareTo).chain(TestData::getA1, String::compareTo)
                .chain(TestData::getB, Integer::compareTo).chain(TestData::getC, Double::compareTo);

        @Test
        @DisplayName("should compare elements in order of chain invocations")
        void testOrder() {
            assertThat(testee.compare(new TestData("1", null, 345, 234), new TestData("2", null, 0, 432)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 0, 23423.0), new TestData("1", "2", 0, 0.0)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 1, 0.0), new TestData("1", "1", 0, 23423.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new TestData("1", "1", 1, 0.0), new TestData("1", "1", 1, 1.0))).isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 1, 1.0), new TestData("1", "1", 1, 1.0))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("created with createNullSafe")
    class TestNullSafe {
        final Comparator<String> stringLengthComparator = Comparator.comparingInt(String::length);

        final TypeSafeChainComparator<TestData> testee = TypeSafeChainComparator.createNullSafe(TestData.class)
                .chain(TestData::getA, stringLengthComparator).chain(TestData::getA1, stringLengthComparator)
                .chain(TestData::getB, (i1, i2) -> stringLengthComparator.compare(i1.toString(), i2.toString()))
                .chain(TestData::getC, (d1, d2) -> stringLengthComparator.compare(d1.toString(), d2.toString()));

        @Test
        @DisplayName("should compare elements in order of chain invocations")
        void testOrder() {
            assertThat(testee.compare(new TestData("1", null, 345, 234), new TestData("11", null, 0, 432)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 0, 23423.0), new TestData("1", "11", 0, 0.0)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 10, 0.0), new TestData("1", "1", 0, 23423.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new TestData("1", "1", 1, 0.0), new TestData("1", "1", 1, 10.0))).isLessThan(0);
            assertThat(testee.compare(new TestData("1", "1", 1, 1.0), new TestData("1", "1", 1, 1.0))).isEqualTo(0);
        }

        @Test
        @DisplayName("should not fail with null values")
        void testNullSafety() {
            assertThat(testee.compare(new TestData(null, null, 345, 234), new TestData("11", null, 0, 432)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData(null, "1", 0, 23423.0), new TestData(null, "11", 0, 0.0)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData(null, null, 10, 0.0), new TestData(null, null, 0, 23423.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new TestData(null, null, 1, 0.0), new TestData(null, null, 1, 10.0)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData(null, null, 1, 1.0), new TestData(null, null, 1, 1.0))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("addComparator")
    class AddComparatorTest {

        final Comparator<String> stringLengthComparator = Comparator.comparingInt(String::length);

        final Comparator<TestData> testee = TypeSafeChainComparator.createNullSafe(TestData.class)
                .addComparator(stringLengthComparator).addComparator(Integer::compareTo)
                .addComparator(Double::compareTo).chain(TestData::getA1).chain(TestData::getA).chain(TestData::getC)
                .chain(TestData::getB);

        @Test
        @DisplayName("should reuse same comparator for same types")
        void testSameComparator() {
            assertThat(testee.compare(new TestData("111", "1", 345, 234), new TestData("11", "11", 0, 432)))
                    .isLessThan(0);
            assertThat(testee.compare(new TestData("", "111", 0, 23423.0), new TestData("a", "11", 0, 0.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new TestData("", "1", 10, 0.0), new TestData(null, null, 0, 23423.0)))
                    .isGreaterThan(0);
            assertThat(testee.compare(new TestData("", null, 100, 0.0), new TestData("", null, 1, 10.0))).isLessThan(0);
            assertThat(testee.compare(new TestData(null, null, 1, 1.0), new TestData(null, null, 1, 1.0))).isEqualTo(0);
        }
    }

}