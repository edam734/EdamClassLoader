package com.edam.classloader;

import java.nio.file.Path;
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
        // read the corresponding .class bytes,
        // and define the class through defineClass(...).

        throw new UnsupportedOperationException();
    }

}
