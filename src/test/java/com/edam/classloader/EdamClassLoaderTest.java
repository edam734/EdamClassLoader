package com.edam.classloader;

import com.edam.classloader.strategy.format.ClassFileStrategy;
import com.edam.classloader.strategy.format.ClassLoadingStrategy;
import com.edam.classloader.strategy.format.JarStrategy;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EdamClassLoaderTest {

    private final List<ClassLoadingStrategy> strategies = List.of(new ClassFileStrategy(),
            new JarStrategy());

    @Test
    void shouldLoadClassFromDirectory() throws ClassNotFoundException {
        URI[] roots = {URI.create("file:///C:/temp/classes/")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        assertEquals("com.example.Hello", loadedClass.getName());
        assertSame(edamClassLoader, loadedClass.getClassLoader());
    }

    @Test
    void shouldLoadClassFromJar() throws Exception {
        URI[] roots = {Path.of("test-examples", "edam-classloader-example.jar").toUri()};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.ReportGenerator");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("generateReport").invoke(instance);

        assertEquals("com.example.ReportGenerator", loadedClass.getName());
        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("[REPORT] Quarterly report", result);
    }

    @Test
    void shouldLoadClassesFromMultipleRoots() throws Exception {
        URI[] roots = {URI.create("file:///C:/temp/classes/"), Path.of("test-examples",
                "edam-classloader-example.jar").toUri(), URI.create("file:///C:/dir/")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

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
        URI[] roots = {URI.create("file:///C:/temp/classes/")};

        ClassLoader parent = ClassLoader.getSystemClassLoader();

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots, parent, strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.ParentGreeting");

        assertSame(parent, loadedClass.getClassLoader());
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenClassDoesNotExist() {
        URI[] roots = {URI.create("file:///C:/temp/classes/"), URI.create(
                "file:///C:/temp/jars/edam-classloader-example.jar"), URI.create(
                "file:///C:/dir/")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        assertThrows(ClassNotFoundException.class,
                () -> edamClassLoader.loadClass("com.example.NonExistent"));
    }

    @Test
    void shouldLoadClassFromFirstMatchingRoot() throws Exception {
        URI[] roots = {URI.create("file:///C:/temp/classes/"), URI.create(
                "file:///C:/temp/jars/edam-classloader-example.jar")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.Hello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from EdamClassLoader!", result);
    }

    @Test
    void shouldLoadDistinctClassesWithDifferentClassLoaders() throws Exception {
        URI[] roots = {URI.create("file:///C:/temp/classes/")};

        EdamClassLoader loaderA = new EdamClassLoader(roots, ClassLoader.getSystemClassLoader(),
                strategies);

        EdamClassLoader loaderB = new EdamClassLoader(roots, ClassLoader.getSystemClassLoader(),
                strategies);

        Class<?> clazzA = loaderA.loadClass("com.example.Hello");
        Class<?> clazzB = loaderB.loadClass("com.example.Hello");

        assertNotSame(clazzA, clazzB);

        assertSame(loaderA, clazzA.getClassLoader());
        assertSame(loaderB, clazzB.getClassLoader());
    }

    @Test
    void shouldLoadClassFromRemoteServer() throws Exception {
        URI[] roots = {URI.create("http://localhost:8000/classes/")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.RemoteHello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from the remote server!", result);
    }

    @Test
    void shouldLoadClassFromRemoteJar() throws Exception {
        URI[] roots = {URI.create("http://localhost:8000/jars/remote-hello-test.jar")};

        EdamClassLoader edamClassLoader = new EdamClassLoader(roots,
                ClassLoader.getSystemClassLoader(), strategies);

        Class<?> loadedClass = edamClassLoader.loadClass("com.example.RemoteHello");

        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Object result = loadedClass.getMethod("sayHello").invoke(instance);

        assertSame(edamClassLoader, loadedClass.getClassLoader());
        assertEquals("Hello from the remote server!", result);
    }
}
