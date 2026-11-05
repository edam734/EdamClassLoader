package com.edam.classloader;

import com.edam.classloader.strategy.format.ClassLoadingStrategy;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Custom class loader that loads classes from configurable local or remote
 * resources using a collection of class-loading strategies.
 *
 * <p>Preserves Java's standard parent-first delegation mechanism.
 */
public final class EdamClassLoader extends ClassLoader {
    private final URI[] roots;
    private final List<ClassLoadingStrategy> strategies;

    /**
     * Creates a class loader with the specified roots, parent and strategies.
     *
     * @param roots the locations to search for classes
     * @param parent the parent class loader
     * @param strategies the strategies used to read class bytecode
     */
    public EdamClassLoader(URI[] roots, ClassLoader parent, List<ClassLoadingStrategy> strategies) {
        super(parent);
        this.roots = Objects.requireNonNull(roots, "roots").clone();
        this.strategies = List.copyOf(Objects.requireNonNull(strategies, "strategies"));
    }

    /**
     * Finds and defines a class by searching the configured roots in order.
     *
     * @param className the fully qualified name of the class
     * @return the defined class
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        byte[] bytes = null;
        String classFilePath = className.replace('.', '/') + ".class";

        for (int i = 0; i < roots.length && bytes == null; i++) {
            for (ClassLoadingStrategy strategy : strategies) {
                if (strategy.supports(roots[i])) {
                    bytes = strategy.readClass(roots[i], classFilePath, className);
                    break;
                }
            }
        }

        if (bytes == null) {
            throw new ClassNotFoundException(className);
        }

        return defineClass(className, bytes, 0, bytes.length);
    }
}
