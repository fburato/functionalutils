[![Build Status](https://travis-ci.org/fburato/functionalutils.svg?branch=master)](https://travis-ci.org/fburato/highwheel-modules)
# Functional Utils

Functional utils is a micro-library which contains fluent functional wrapper that can be used to ease the definition
of boiler-plate functionalities in Java.

The latest version of the library contains the following wrappers:

- [Builder](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/com/github/fburato/functionalutils/utils/Builder.java):
  Allows to create advanced, type-safe, fluent and immutable builders for java classes with minimal overhead. The general
  structure of this builder pattern is taken from a [2017 medium post](https://medium.com/beingprofessional/think-functional-advanced-builder-pattern-using-lambda-284714b85ed5) 
  by `beingprofessional`, but the structure has been generalised to reduce the boilerplate to the minimum.
- [TypeSafeChainComparator](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/com/github/fburato/functionalutils/utils/TypeSafeChainComparator.java):
  Allows to create composable, immutable, type-safe, fluent comparators for any type. The generic comparison algorithm
  involves the specification of the fields to add to the comparison as if the fields are lexicographically ordered. 
  Hence, the comparator returned for the type will produce 0 if all comparison return 0 for every field, and will
  return something different from 0 if any of the comparison returns something different from 0. This abstraction
  can be used to externalise the definition of a `Comparator` from any type.
- [TypeSafeChainShow](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/com/github/fburato/functionalutils/utils/TypeSafeChainShow.java):
  Allows to create composable, immutable, type-safe, fluent converter of any type into `String`. The conversion
  to string uses a standard configuration which returns a `toString` representation as `<class name>([field1{,field}])`.
  
## Builder

Using the builder wrapper requires to define an extension of the `Builder` class with the appropriate type
parameters.

For example, let's suppose you have the following class for which you want to define a builder:

```java
class A {
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
```

The builder associated to this class can be defined as follows:

```java
class ABuilder extends Builder<A, ABuilder> {
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
    
    public static final ABuilder baseBuilder() {
        return new ABuilder();
    }
}
```

The definition is constrained as follows:

- The class **MUST** extend the `Builder` class and the type parameters **MUST** be: (1) the type you are building, (2)
  the concrete type of the `Builder`, passed as a self-context bound.
- The class **MUST** define a default constructor which passes the method reference of the constructor itself to the
  super constructor.
- The class **MUST** define the fields which are going to be set in the building type as public, non-final fields.
- The class **MUST** override the `makeValue` method which has to build the designated object using the public fields.

Once the builder is defined, using it requires to chain as many `with` method with consumer on the builder itself as
needed as follows:

```java
class Test {
    static {
        final A defaultA = ABuilder.baseBuilder().build(); // A(null,0)
        final A aWithString = ABuilder.baseBuilder().with(a -> a.a = "foo").build(); // A("foo",0)
        final A aWithStringAndInt = ABuilder.baseBuilder().with(a -> {
          a.a = "bar";
          a.b = 42;
        }); // A("bar", 42)
        final ABuilder builderWithHelloWorld = ABuilder.baseBuilder().with(a -> a.a = "hello world"); // builder which produces always an A instance with "hello world"
        final A helloWorld42 = builderWithHelloWorld.with(a -> a.b = 42).build(); // A("hello world", 42)
        final A helloWorld56 = builderWithHelloWorld.with(a -> a.b = 56).build(); // A("hello world", 56)
    }
}
```

The builder has the following properties:

- **Immutability on with**: every invocation of with causes a new builder to be instantiated with all the consumers
  chained to this point and the new consumers. This means that, once a builder is created, there is no operation 
  (except explicit setting of the builder public fields), that could override a field set by a `with` clause.
- **Override of fields by definition**: the consumers passed with `with` are evaluated in the order they are chained. This
  entails that if you are setting a field in a `with` clause and in a subsequent `with` clause you are setting the same
  field, the last setting will be evaluated last, causing that assignment to take place.

## TypeSafeChainComparator

`TypeSafeChainComparator` allows to define comparators for any type composing together other comparators and defining
the order in which the fields of a class are compared. The class allows to reuse multiple times a certain composed
comparator for a certain type, allowing to minimise the boiler-plate.

For example, let's assume you need to define a comparator for the following class:

```java
public class TestData {
    private String a;
    private String a1;
    private Integer b;
    private double c;

    public TestData() {
    }

    public TestData(String a, String a1, Integer b, double c) {
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

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }
}
```

And let's assume that the comparison you want to perform has the following properties:

- The fields need to be evaluated in the order `a, a1, b, c`
- The strings need to be compared lexicographically but ignoring the case.
- `null` values are allowed for any Object type (`a`, `a1`, `b`)

The type safe comparator can be defined as follows:

```java
class Test{
    static {
        final Comparator<String> stringComparator = String::compareToIgnoreCase;
        final Comparator<TestData> comparator = TypeSafeComparator.createNullSafe(TestData.class)
           .addComparator(stringComparator)
           .addComparator(Integer::compare)
           .chain(TestData::getA)
           .chain(TestData::getA1)
           .chain(TestData::getB)
           .chain(TestData::getC, Double::compare);   
    }
}
```

Here is the explanation of the fluent invocation:

1. `TypeSafeComparator.createNullSafe(TestData.class)` starts the definition of a type safe comparator which applies
   the null-safe comparator decorator to all chained comparators. This means that before applying the comparator for
   any type, during the evaluation the arguments are checked for being null. The decorator establishes that
   null is smaller than any other value and performs the null check before running the actual comparison. If you don't
   need the null-safe decoration, you can simply call `TypeSafeComparator.create(TestData.class)`, and if you want
   to provide a custom `ComparatorDecorator`, you can call `TypeSafeComparator::createWithDecorator`. The decorator
   is applied to the fields of the object, not the object themselves, so if you want to create a null-safe decorator
   for the type itself, you will have to decorate the comparator itself with [ComparatorDecorator::nullSafe](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/com/github/fburato/functionalutils/utils/ComparatorDecorators.java#L16)
2. `.addComparator(stringComparator)` binds the type `String` to the `stringComparator` object meaning that every field
   of type `String` which is accessed will be compared with `stringComparator`. The same semantics applies to `.addComparator(Integer::compare)`
3. `.chain(TestData::getA).chain(TestData::getA1).chain(TestData::getB)` defines the order of the fields to be used in
   the comparison, by indicating how to extract the fields in the required order with a getter. Notice that only the
   getter is provided as argument, because of the invocation of `addComparator` at step 2. If you were to pass
   a getter for a type for which there hasn't been a `addComparator` binding before, the comparator won't compile.
4. `.chain(TestData::getC, Double::compare)` defines the last comparison on the field C using the specific comparator
   `Double::compare`. The two arguments chain method can be used to override previously bound type comparators but it can
   also be used to bind comparator which do not need repetition.

The comparators defined with this wrapper satisfy the following property:

- **Immutability on chaining and addComparator**: every invocation of `chain` and `addComparator` produces a new
  object which contains all the previous binding keeping the previous comparators in the chain unaltered.
- **Type bound up to 55 type parameters**: the fluent API allows to bind comparators to up to 55 types.

## TypeSafeChainShow

`TypeSafeChainShow` allows to externalise the `toString` implementation of any class by chaining `toString` conversions
of an aggregate type, indicating the order and the format of every type conversion.

Using the same `TestData` class defined at [TypeSafeChainComparator](#TypeSafeChainComparator), let's assume
that you want to represent test data as String with the following format:

- Every string must be single-quote wrapped,
- Every integer must be converted in hexadecimal,
- If an object is null, then null must be presented,
- The format has to be `TestData(<a>,<a1>,<b>,<c>)`.

The show instance can be defined as follows:
```java
class Test{
    
    public static <S> Show<S> nullSafeShow(Show<S> show) {
      return s -> {
         if(s == null) {
            return "null";
         } else {
            return show.show(s);
         }
      };
    }

    static {
        final Show<String> quotedStringShow = nullSafeShow(s -> "'" + s + "'");
        final Show<Integer> toHexIntShow = nullSafeShow(Integer::toHexString);
        final Show<TestData> testDataShow =  TypeSafeChainShow.create(TestData.class)
            .addShow(quotedStringShow)
            .chain(TestData::getA)
            .chain(TestData::getA1)
            .chain(TestData::getB, toHexIntShow)
            .standardChain(TestData::getC);
        System.out.println(testDataShow.show(new TestData("a","b",10,1.0))); // prints TestData('a','b',a,1.0)
    }
}
```

Here's the explanation of the fluent invocation:

1. `nullSafeShow` defines a custom [Show](https://github.com/fburato/functionalutils/blob/master/functions/src/main/java/com/github/fburato/functionalutils/api/Show.java) 
   decorator which prevents the show instance to be invoked if the value is null.
2. `quotedString` defines a custom `Show` instance for `String` which puts quotes on any String.
3. `toHexIntShow` defines a custom `Show` instance for `Integer` which converts an integer to its hex representation.
4. `TypeSafeChainShow.create(TestData.class)` starts the definition of a new `TypeSafeChainShow` instance for `TestData`.
   This invocation uses the default configuration for the representation which generates a String in the form: `<class name>(parameters)`
   with the parameters separated by `,`. If you want to customise the representation, you can define you own
   [Configuration](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/coam/github/fburato/functionalutils/utils/TypeSafeChainShow.java#L16)
   and pass it to [TypeSafeChainShow::createWithConfig](https://github.com/fburato/functionalutils/blob/master/utils/src/main/java/coam/github/fburato/functionalutils/utils/TypeSafeChainShow.java#L16).
5. `.addShow(quotedStringShow)` binds the type String to the `quotedStringShow` instance, meaning that when converting
   to string any string, `quotedStringShow` will be used.
6. `.chain(TestData::getA).chain(TestData::getA1)`  defines the order of the fields to be used in
   the string conversion, by indicating how to extract the fields in the required order with a getter. As in the
   `TypeSafeChainShow`, please note that only the getter is passed because the `String` type has been bound to a
   show instance at step (5). If the binding did not occur, this program would not compile.
7. `.chain(TestData::getB, toHexIntShow)` adds the field b to the string conversion and provides the specific
   string conversion to use, in this case `toHexIntShow`.
8. `.standardChain(TestData::getC)` adds the field c to the string conversion using as Show instance `Objects::toString`
   which is null-safe.
   
The show instances defined with this wrapper satisfy the following property:

- **Immutability on chaining and addShow**: every invocation of `chain`, `standardChain` and `addShow` produces a new
  object which contains all the previous binding keeping the previous show in the chain unaltered.
- **Type bound up to 55 type parameters**: the fluent API allows to bind show instances to up to 55 types.