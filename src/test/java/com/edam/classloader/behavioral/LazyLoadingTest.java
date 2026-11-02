package com.edam.classloader.behavioral;

import com.edam.classloader.EdamClassLoader;

import java.nio.file.Path;

public class LazyLoadingTest {

    /**
     * Demonstrates the lazy loading of class dependencies.
     *
     * <p>Loads and instantiates ReportGenerator, then invokes generateReport()
     * to observe when the JVM requests its dependency, ReportFormatter.
     *
     * <p>To observe these requests, temporarily add the following statement
     * to the EdamClassLoader.findClass() method:
     *
     * <pre>
     * System.out.println("Loading class: " + className);
     * </pre>
     *
     * <p>This is a behavioral test. The exact moment when dependencies
     * are loaded is determined by the JVM and is not guaranteed.
     */
    static void main(String[] args) throws Exception {
        Path[] roots = {Path.of("test-examples", "edam-classloader-example.jar")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        String className = "com.example.ReportGenerator";
        System.out.println("Loading class: " + className);
        Class<?> loadedClass = edamClassLoader.loadClass(className);
        System.out.println("Class " + loadedClass + " loaded.");
        Object reportGenerator = loadedClass.getDeclaredConstructor().newInstance();
        System.out.println("Instance " + reportGenerator + " created.");
        System.out.println("About to invoke generateReport().");
        Object result = loadedClass.getMethod("generateReport").invoke(reportGenerator);

        System.out.println(result);
    }
}
