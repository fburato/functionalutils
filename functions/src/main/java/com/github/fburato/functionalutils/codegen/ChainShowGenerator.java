package com.github.fburato.functionalutils.codegen;

import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChainShowGenerator implements CodeGenerator {

    private final String CLASS_NAME = "ChainShow%d";

    private final int index;
    private final boolean isTerminal;

    public ChainShowGenerator(int index, boolean isTerminal) {
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
            showMethod();
            basicChainMethod();
            standardChainMethod();
            if (!isTerminal) {
                addComparatorMethod();
            }
            chainMethods();
            footer();
        }

        private void header() {
            writer.println("package com.github.fburato.functionalutils.api;");
            writer.println("import java.util.function.Function;");
        }

        private void classDeclaration() {
            writer.println(
                    String.format("public final class %s<T,%s> implements Show<T> {", className, typeDeclaration));
        }

        private void fields() {
            writer.println("private final ChainableShow<T> chainableShow;");
            writer.println(IntStream.rangeClosed(1, index)
                    .mapToObj(i -> String.format("private final Show<T%d> show%d;", i, i))
                    .collect(Collectors.joining("\n")));
        }

        private void constructor() {
            final String parameterList = IntStream.rangeClosed(1, index)
                    .mapToObj(i -> String.format("Show<T%d> show%d", i, i)).collect(Collectors.joining(", "));
            writer.println(String.format("public %s(ChainableShow<T> chainableShow, %s){",
                    className,
                    parameterList));
            writer.println("this.chainableShow = chainableShow;");
            writer.println(IntStream.rangeClosed(1, index).mapToObj(i -> String.format("this.show%d = show%d;", i, i))
                    .collect(Collectors.joining("\n")));
            writer.println("}");
        }

        private void showMethod() {
            writer.println("@Override");
            writer.println("public String show(T t) { return this.chainableShow.show(t); }");
        }

        private void basicChainMethod() {
            writer.println(String.format("public <S> %s<T,%s> chain(Function<T,S> fieldGetter, Show<S> show){",
                    className, typeDeclaration));
            writer.println(String.format("return new %s<>(this.chainableShow.chain(fieldGetter, show), %s);",
                    className,
                    IntStream.rangeClosed(1, index).mapToObj(i -> String.format("this.show%d", i))
                            .collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void standardChainMethod() {
            writer.println(String.format("public <S> %s<T,%s> standardChain(Function<T,S> fieldGetter){",
                    className,
                    typeDeclaration));
            writer.println(String.format("return new %s<>(this.chainableShow.standardChain(fieldGetter), %s);",
                    className,
                    IntStream.rangeClosed(1, index).mapToObj(i -> String.format("this.show%d", i))
                            .collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void addComparatorMethod() {
            writer.println(String.format("public <T%d> %s<T,%s,T%d> addShow(Show<T%d> show){",
                    index + 1,
                    String.format(CLASS_NAME, index + 1),
                    typeDeclaration,
                    index + 1,
                    index + 1));
            writer.println(String.format("return new %s<>(this.chainableShow,%s,show);",
                    String.format(CLASS_NAME, index + 1),
                    IntStream.rangeClosed(1, index)
                            .mapToObj(i -> String.format("this.show%d", i)).collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void chainMethods() {
            IntStream.rangeClosed(1, index).forEach(this::chainMethod);
        }

        private void chainMethod(int chainMethodIndex) {
            writer.println(String.format("public %s<T,%s> chain(Function%d<T,T%d> fieldGetter){",
                    className,
                    typeDeclaration,
                    chainMethodIndex,
                    chainMethodIndex));
            writer.println(
                    String.format("return new %s<>(this.chainableShow.chain(fieldGetter.asFunction(), show%d), %s);",
                            className,
                            chainMethodIndex,
                            IntStream.rangeClosed(1, index)
                                    .mapToObj(i -> String.format("this.show%d", i)).collect(Collectors.joining(", "))));
            writer.println("}");
        }

        private void footer() {
            writer.println("}");
        }
    }
}
