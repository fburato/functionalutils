package com.github.fburato.functionalutils.codegen;

import java.io.File;
import java.io.PrintWriter;
import java.util.stream.IntStream;

public class Generator {

    private static final String apiPackage = String.join(File.separator,
            "com.github.fburato.functionalutils.api".split("\\."));

    public static void main(String[] argv) {
        final String baseDir = argv[0];
        final int types = Integer.parseInt(argv[1]);
        final File baseDirectory = new File(baseDir + File.separator + apiPackage);
        baseDirectory.mkdirs();
        IntStream.rangeClosed(1, types).forEach(i -> {
            final String functionFile = fileName(baseDirectory, "Function", i);
            final String chainComparatorFile = fileName(baseDirectory, "ChainComparator", i);
            final String chainShowFile = fileName(baseDirectory, "ChainShow", i);
            generateFile(functionFile, new FunctionGenerator(i));
            generateFile(chainComparatorFile, new ChainComparatorGenerator(i, i == types));
            generateFile(chainShowFile, new ChainShowGenerator(i, i == types));
        });
    }

    private static String fileName(File basePath, String baseName, int suffix) {
        return basePath.getAbsolutePath() + File.separator + baseName + suffix + ".java";
    }

    private static void generateFile(String fileName, CodeGenerator generator) {
        try (final PrintWriter writer = new PrintWriter(new File(fileName))) {
            generator.generate(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
