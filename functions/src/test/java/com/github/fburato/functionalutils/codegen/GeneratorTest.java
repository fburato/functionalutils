package com.github.fburato.functionalutils.codegen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Generator should")
public class GeneratorTest {

    @Test
    @DisplayName("generate Function files in destination directory")
    void testFunction() throws IOException {
        var tempDir = Files.createTempDirectory("destination");
        Generator.main(new String[]{tempDir.toAbsolutePath().toString(), "3"});
        assertThat(getGenerationDirectory(tempDir)).exists()
                .isDirectoryContaining(f -> f.getName().equals("Function1.java"))
                .isDirectoryContaining(f -> f.getName().equals("Function2.java"))
                .isDirectoryContaining(f -> f.getName().equals("Function3.java"));
    }

    private File getGenerationDirectory(Path baseDirectory) {
        return Paths
                .get(baseDirectory.toAbsolutePath().toString(), "com", "github", "fburato", "functionalutils", "api")
                .toFile();
    }

    @Test
    @DisplayName("generate ChainComparator files in destination directory")
    void testChainComparator() throws IOException {
        var tempDir = Files.createTempDirectory("destination");
        Generator.main(new String[]{tempDir.toAbsolutePath().toString(), "3"});
        assertThat(getGenerationDirectory(tempDir)).exists()
                .isDirectoryContaining(f -> f.getName().equals("ChainComparator1.java"))
                .isDirectoryContaining(f -> f.getName().equals("ChainComparator2.java"))
                .isDirectoryContaining(f -> f.getName().equals("ChainComparator3.java"));
    }
}
