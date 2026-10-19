package com.edam.classloader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class EdamClassLoader extends ClassLoader {
    private final Path root;

    public EdamClassLoader(Path root, ClassLoader parent) {
        super(parent);
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {

        // Convert the fully qualified class name into a path,
        Path filename = Paths.get(root.toString(), className.replace(".", "/") + ".class");

        // read the corresponding .class bytes,
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(filename);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class: " + className, e);
        }

        // and define the class through defineClass(...).
        return defineClass(className, bytes, 0, bytes.length);
    }

}
