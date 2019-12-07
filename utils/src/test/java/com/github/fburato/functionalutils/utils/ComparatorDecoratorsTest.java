package com.github.fburato.functionalutils.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class ComparatorDecoratorsTest {
    @Nested
    @DisplayName("identity decorator")
    class IdentityTest {
        @Test
        @DisplayName("should return the comparator itself")
        void testIdentity() {
            Comparator<String> stringComparator = String::compareTo;
            assertThat(ComparatorDecorators.identity.decorate(stringComparator)).isSameAs(stringComparator);
        }
    }

    @Nested
    @DisplayName("null safe decorator")
    class NullSafeTest {

        final Comparator<String> comparator = Comparator.comparingInt(String::length);

        @Test
        @DisplayName("should allow null-null comparisons")
        void testNullNull() {
            assertThat(ComparatorDecorators.nullSafe.decorate(comparator).compare(null, null)).isEqualTo(0);
        }

        @Test
        @DisplayName("should allow null-notNull comparisons")
        void testNullNotNull() {
            assertThat(ComparatorDecorators.nullSafe.decorate(comparator).compare(null, "foo")).isLessThan(0);
        }

        @Test
        @DisplayName("should allow notNull-null comparisons")
        void testNotNullNull() {
            assertThat(ComparatorDecorators.nullSafe.decorate(comparator).compare("foo", null)).isGreaterThan(0);
        }

        @Test
        @DisplayName("should allow notNull-notNull comparisons and apply real comparator")
        void testNotNullNotNull() {
            assertThat(ComparatorDecorators.nullSafe.decorate(comparator).compare("foobar", "fo")).isGreaterThan(0);
        }
    }
}