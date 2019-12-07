package com.github.fburato.functionalutils.codegen.compiler;

import javax.tools.*;
import java.util.*;

public class InMemoryJavaCompiler {
    private final JavaCompiler javac;
    private final Map<String, SourceCode> sourceCodes = new HashMap<>();
    boolean ignoreWarnings = false;
    private DynamicClassLoader classLoader;
    private Iterable<String> options;

    private InMemoryJavaCompiler() {
        this.javac = ToolProvider.getSystemJavaCompiler();
        this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
    }

    public static InMemoryJavaCompiler newInstance() {
        return new InMemoryJavaCompiler();
    }

    public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
        this.classLoader = new DynamicClassLoader(parent);
        return this;
    }

    public ClassLoader getClassloader() {
        return classLoader;
    }

    public InMemoryJavaCompiler useOptions(String... options) {
        this.options = Arrays.asList(options);
        return this;
    }

    public InMemoryJavaCompiler ignoreWarnings() {
        ignoreWarnings = true;
        return this;
    }

    public Map<String, Class<?>> compileAll() throws Exception {
        if (sourceCodes.size() == 0) {
            throw new CompilationException("No source code to compile");
        }
        Collection<SourceCode> compilationUnits = sourceCodes.values();
        CompiledCode[] code;

        code = new CompiledCode[compilationUnits.size()];
        Iterator<SourceCode> iter = compilationUnits.iterator();
        for (int i = 0; i < code.length; i++) {
            code[i] = new CompiledCode(iter.next().getClassName());
        }
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(
                javac.getStandardFileManager(null, null, null), classLoader);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null,
                compilationUnits);
        boolean result = task.call();
        if (!result || collector.getDiagnostics().size() > 0) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Unable to compile the source");
            boolean hasWarnings = false;
            boolean hasErrors = false;
            for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
                switch (d.getKind()) {
                case NOTE:
                case MANDATORY_WARNING:
                case WARNING:
                    hasWarnings = true;
                    break;
                case OTHER:
                case ERROR:
                default:
                    hasErrors = true;
                    break;
                }
                exceptionMsg.append("\n").append("[kind=").append(d.getKind());
                exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
                exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
            }
            if (hasWarnings && !ignoreWarnings || hasErrors) {
                throw new CompilationException(exceptionMsg.toString());
            }
        }

        Map<String, Class<?>> classes = new HashMap<>();
        for (String className : sourceCodes.keySet()) {
            classes.put(className, classLoader.loadClass(className));
        }
        return classes;
    }

    public Class<?> compile(String className, String sourceCode) throws Exception {
        return addSource(className, sourceCode).compileAll().get(className);
    }

    public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
        sourceCodes.put(className, new SourceCode(className, sourceCode));
        return this;
    }
}
