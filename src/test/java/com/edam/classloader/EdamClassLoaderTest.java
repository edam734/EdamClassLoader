package com.edam.classloader;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EdamClassLoaderTest {

    @Test
    void shouldLoadClassFromDirectory() throws ClassNotFoundException {
        URI[] roots = {Path.of("C:/temp/classes").toUri()};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        assertEquals("com.example.Hello", loadedClass.getName());
        assertSame(edamClassLoader, loadedClass.getClassLoader());
    }

    @Test
    void shouldLoadClassFromJar() throws Exception {
        URI[] roots = {Path.of("test-examples", "edam-classloader-example.jar").toUri()};

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
        URI[] roots = {Path.of("C:/temp/classes").toUri(), Path.of("test-examples",
                "edam-classloader-example.jar").toUri(), Path.of("C:/dir").toUri()};

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
        URI[] roots = {Path.of("test-examples").toUri()};

        ClassLoader parent = ClassLoader.getSystemClassLoader();

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots, parent);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.ParentGreeting");

        assertSame(parent, loadedClass.getClassLoader());
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenClassDoesNotExist() {
        URI[] roots = {Path.of("C:/temp/classes").toUri(), Path.of("test-examples/jars",
                "edam-classloader-example.jar").toUri(), Path.of("C:/dir").toUri()};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        assertThrows(ClassNotFoundException.class,
                () -> edamClassLoader.loadClass("com.example.NonExistent"));
    }

    @Test
    void shouldLoadClassFromFirstMatchingRoot() throws Exception {
        URI[] roots = {Path.of("C:/temp/classes").toUri(), Path.of("test-examples",
                "edam-classloader-example.jar").toUri()};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from EdamClassLoader!", result);
    }

    @Test
    void URI() throws Exception {
        URI[] roots = {Path.of("C:/temp/classes").toUri()};

        EdamClassLoader loaderA = new EdamClassLoader(roots, ClassLoader.getSystemClassLoader());

        EdamClassLoader loaderB = new EdamClassLoader(roots, ClassLoader.getSystemClassLoader());

        Class<?> clazzA = loaderA.loadClass("com.example.Hello");
        Class<?> clazzB = loaderB.loadClass("com.example.Hello");

        assertNotSame(clazzA, clazzB);

        assertSame(loaderA, clazzA.getClassLoader());
        assertSame(loaderB, clazzB.getClassLoader());
    }

    @Test
    void shouldLoadClassFromRemoteServer() throws Exception {
        URI[] roots = new URI[]{URI.create("http://localhost:8000/classes")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader());

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.RemoteHello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from the remote server!", result);
    }

    @Test
    void shouldLoadClassFromRemoteJar() throws Exception {
        URI[] roots = {
                URI.create("http://localhost:8000/jars/remote-hello-test.jar")
        };

        EdamClassLoader edamClassLoader = new EdamClassLoader(
                roots, ClassLoader.getSystemClassLoader()
        );

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.RemoteHello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from the remote server!", result);
    }
}
