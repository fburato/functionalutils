package com.github.fburato.functionalutils.utils;

import com.github.fburato.functionalutils.api.ChainableShow;
import com.github.fburato.functionalutils.api.Show;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TypeSafeChainShow")
class TypeSafeChainShowTest {

    final TestData data = new TestData("1", "2", 10, 23423.0);
    final Show<String> stringWithQuotes = s -> "\"" + s + "\"";

    @Test
    @DisplayName("chaining should not mutate previous instances")
    void testImmutable() {
        final var show1 = TypeSafeChainShow.create(TestData.class).chain(TestData::getA, Objects::toString);
        assertThat(show1.show(data)).isEqualTo("TestData(1)");
        final var show2 = show1.chain(TestData::getA1, Objects::toString);
        assertThat(show1.show(data)).isEqualTo("TestData(1)");
        assertThat(show2.show(data)).isEqualTo("TestData(1,2)");
    }

    @Nested
    @DisplayName("created with create")
    class CreateTest {

        final ChainableShow<TestData> testee = TypeSafeChainShow.create(TestData.class)
                .chain(TestData::getA, String::toString).chain(TestData::getA1, String::toString)
                .chain(TestData::getB, Object::toString).chain(TestData::getC, Object::toString);

        @Test
        @DisplayName("should include only fields in the chaining order")
        void testFields() {
            final var testee1 = TypeSafeChainShow.create(TestData.class).chain(TestData::getA, Objects::toString)
                    .chain(TestData::getA1, Objects::toString);
            final var testee2 = TypeSafeChainShow.create(TestData.class).chain(TestData::getA1, Objects::toString)
                    .chain(TestData::getA, Objects::toString);

            assertThat(testee1.show(data)).isEqualTo("TestData(1,2)");
            assertThat(testee2.show(data)).isEqualTo("TestData(2,1)");
        }

        @Test
        @DisplayName("should use standard configuration")
        void testOrder() {
            assertThat(testee.show(data)).isEqualTo("TestData(1,2,10,23423.0)");
        }
    }

    @Nested
    @DisplayName("created with configuration")
    class TestNullSafe {

        final ChainableShow<TestData> testee = TypeSafeChainShow
                .createWithConfig(TestData.class, new TypeSafeChainShow.Configuration<>(c -> "MyClass", "{", "}", "/"))
                .chain(TestData::getA, Objects::toString).chain(TestData::getA1, Objects::toString);

        @Test
        @DisplayName("should print based on configuration")
        void testOrder() {
            assertThat(testee.show(data)).isEqualTo("MyClass{1/2}");
        }
    }

    @Nested
    @DisplayName("addShow")
    class AddComparatorTest {
        final Show<Integer> integerInHex = Integer::toHexString;
        final Show<TestData> testee = TypeSafeChainShow.create(TestData.class).addShow(stringWithQuotes)
                .addShow(integerInHex).chain(TestData::getA).chain(TestData::getA1).chain(TestData::getB)
                .chain(TestData::getC, Objects::toString);

        @Test
        @DisplayName("should reuse same comparator for same types")
        void testSameComparator() {
            assertThat(testee.show(data)).isEqualTo("TestData(\"1\",\"2\",a,23423.0)");
        }
    }

    @Nested
    @DisplayName("standardChain")
    class StandardChainTest {
        final Show<TestData> testee = TypeSafeChainShow.create(TestData.class).addShow(stringWithQuotes)
                .chain(TestData::getA).chain(TestData::getA1).standardChain(TestData::getB)
                .standardChain(TestData::getC);

        @Test
        @DisplayName("should invoke toString null safe")
        void testNullSafe() {
            assertThat(testee.show(new TestData("a", "b", null, 1.0))).isEqualTo("TestData(\"a\",\"b\",null,1.0)");
        }
    }

}