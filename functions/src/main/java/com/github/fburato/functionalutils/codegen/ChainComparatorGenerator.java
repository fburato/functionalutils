package com.github.fburato.functionalutils.codegen;

import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChainComparatorGenerator implements CodeGenerator {

    private final String CLASS_NAME = "ChainComparator%d";

    private final int index;
    private final boolean isTerminal;

    public ChainComparatorGenerator(int index, boolean isTerminal) {
        this.index = index;
        this.isTerminal = isTerminal;
    }

    @Override
    public void generate(PrintWriter printWriter) {
        final var internal = new InternalGenerator(printWriter);
        internal.generate();
    }

    private class InternalGenerator {
        private final PrintWriter writer;
        private final String className;
        private final String typeDeclaration;

        private InternalGenerator(PrintWriter writer) {
            this.writer = writer;
            this.className = String.format(CLASS_NAME, index);
            this.typeDeclaration = IntStream.rangeClosed(1, index).mapToObj(i -> "T" + i)
                    .collect(Collectors.joining(","));
        }

        private void generate() {
            header();
            classDeclaration();
            fields();
            constructor();
            compareMethod();
            basicChainMethod();
            if (!isTerminal) {
                addComparatorMethod();
            }
            chainMethods();
            footer();
        }

        private void header() {
            writer.println("package com.github.fburato.functionalutils.api;");
            writer.println("import java.util.Comparator;");
            writer.println("import java.util.function.Function;");
        }

        private void classDeclaration() {
            writer.println(String.format("public final class %s<T,%s> implements Comparator<T> {", className,
                    typeDeclaration));
        }

        private void fields() {
            writer.println("private final ChainableComparator<T> chainableComparator;");
            writer.println(IntStream.rangeClosed(1, index)
                    .mapToObj(i -> String.format("private final Comparator<T%d> comparator%d;", i, i))
                    .collect(Collectors.joining("\n")));
        }

        private void constructor() {
            final String parameterList = IntStream.rangeClosed(1, index)
                    .mapToObj(i -> String.format("Comparator<T%d> comparator%d", i, i))
                    .collect(Collectors.joining(", "));
            writer.println(String.format("public %s(ChainableComparator<T> chainableComparator, %s){", className,
                    parameterList));
            writer.println("this.chainableComparator = chainableComparator;");
            writer.println(IntStream.rangeClosed(1, index)
                    .mapToObj(i -> String.format("this.comparator%d = comparator%d;", i, i))
                    .collect(Collectors.joining("\n")));
            writer.println("}");
        }

        private void compareMethod() {
            writer.println("@Override");
            writer.println("public int compare(T t1, T t2) { return this.chainableComparator.compare(t1, t2); }");
        }

        private void basicChainMethod() {
            writer.println(
                    String.format("public <S> %s<T,%s> chain(Function<T,S> fieldGetter, Comparator<S> comparator){",
                            className, typeDeclaration));
            writer.println(String.format(
                    "return new %s<>(this.chainableComparator.chain(fieldGetter, comparator), %s);", className,
                    IntStream.rangeClosed(1, index).mapToObj(i -> String.format("this.comparator%d", i))
                            .collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void addComparatorMethod() {
            writer.println(String.format("public <T%d> %s<T,%s,T%d> addComparator(Comparator<T%d> comparator){",
                    index + 1, String.format(CLASS_NAME, index + 1), typeDeclaration, index + 1, index + 1));
            writer.println(String.format("return new %s<>(this.chainableComparator,%s,comparator);",
                    String.format(CLASS_NAME, index + 1), IntStream.rangeClosed(1, index)
                            .mapToObj(i -> String.format("this.comparator%d", i)).collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void chainMethods() {
            IntStream.rangeClosed(1, index).forEach(this::chainMethod);
        }

        private void chainMethod(int chainMethodIndex) {
            writer.println(String.format("public %s<T,%s> chain(Function%d<T,T%d> fieldGetter){", className,
                    typeDeclaration, chainMethodIndex, chainMethodIndex));
            writer.println(String.format(
                    "return new %s<>(this.chainableComparator.chain(fieldGetter.asFunction(), comparator%d), %s);",
                    className, chainMethodIndex, IntStream.rangeClosed(1, index)
                            .mapToObj(i -> String.format("this.comparator%d", i)).collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void footer() {
            writer.println("}");
        }
    }
}
