package com.edam.classloader;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EdamClassLoaderTest {

    @Test
    void shouldLoadClassFromDirectory() throws ClassNotFoundException {
        Path[] roots = {Path.of("C:/temp/classes")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        assertEquals("com.example.Hello", loadedClass.getName());
        assertSame(edamClassLoader, loadedClass.getClassLoader());
    }

    @Test
    void shouldLoadClassFromJar() throws Exception {
        Path[] roots = {Path.of("test-examples", "edam-classloader-example.jar")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.ReportGenerator");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("generateReport").invoke(instance);

        assertEquals("com.example.ReportGenerator", loadedClass.getName());
        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("[REPORT] Quarterly report", result);
    }

    @Test
    void shouldLoadClassesFromMultipleRoots() throws Exception {
        Path[] roots = {Path.of("C:/temp/classes"), Path.of("test-examples",
                "edam-classloader-example.jar"), Path.of("C:/dir")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        // First root: directory
        Class<?> helloClass = edamClassLoader.loadClass("com.example.Hello");

        // Second root: JAR
        Class<?> reportClass = edamClassLoader.loadClass("com.example.ReportGenerator");

        // Third root: directory
        Class<?> reverserClass = edamClassLoader.loadClass("com.example.TextReverser");

        // Verify the classes were loaded by EdamClassLoader
        assertSame(edamClassLoader, helloClass.getClassLoader());
        assertSame(edamClassLoader, reportClass.getClassLoader());
        assertSame(edamClassLoader, reverserClass.getClassLoader());

        // Verify a method from the third root
        Object instance = reverserClass.getDeclaredConstructor().newInstance();
        Object result = reverserClass.getMethod("reverse", String.class).invoke(instance, "Java");

        assertEquals("avaJ", result);
    }

    @Test
    void shouldLoadClassFromParentClassLoader() throws Exception {
        Path[] roots = {Path.of("test-examples")};

        ClassLoader parent = ClassLoader.getSystemClassLoader();

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots, parent);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.ParentGreeting");

        assertSame(parent, loadedClass.getClassLoader());
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenClassDoesNotExist() {
        Path[] roots = {Path.of("C:/temp/classes"), Path.of("test-examples",
                "edam-classloader-example.jar"), Path.of("C:/dir")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        assertThrows(ClassNotFoundException.class,
                () -> edamClassLoader.loadClass("com.example.NonExistent"));
    }

    @Test
    void shouldLoadClassFromFirstMatchingRoot() throws Exception {
        Path[] roots = {Path.of("C:/temp/classes"), Path.of("test-examples",
                "edam-classloader-example.jar")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from EdamClassLoader!", result);
    }

    @Test
    void shouldLoadDistinctClassesFromDifferentClassLoaders() throws Exception {
        Path[] roots = {Path.of("C:/temp/classes")};

        EdamClassLoader loaderA = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        EdamClassLoader loaderB = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> clazzA = loaderA.loadClass("com.example.Hello");
        Class<?> clazzB = loaderB.loadClass("com.example.Hello");

        assertNotSame(clazzA, clazzB);

        assertSame(loaderA, clazzA.getClassLoader());
        assertSame(loaderB, clazzB.getClassLoader());
    }
}
