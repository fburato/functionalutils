package com.github.fburato.functionalutils.codegen;

import java.io.PrintWriter;

public class FunctionGenerator implements CodeGenerator {

    private final int suffix;

    public FunctionGenerator(int suffix) {
        this.suffix = suffix;
    }

    @Override
    public void generate(PrintWriter printWriter) {
        printHeader(printWriter);
        printInterfaceDeclaration(printWriter);
        printFooter(printWriter);
    }

    private void printHeader(PrintWriter printWriter) {
        printWriter.println("package com.github.fburato.functionalutils.api;");
    }

    private void printInterfaceDeclaration(PrintWriter printWriter) {
        printWriter.println("@FunctionalInterface");
        printWriter.println("public interface Function" + suffix + " <T1,T2> extends FunctionLike<T1,T2> {");
    }

    private void printFooter(PrintWriter printWriter) {
        printWriter.println("}");
    }
}
