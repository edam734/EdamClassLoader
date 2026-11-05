package com.edam.classloader.strategy.format;

import com.edam.classloader.strategy.ResourceStrategyResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Strategy for loading classes from JAR archives.
 *
 * <p>Retrieves the JAR bytes through a resource strategy and searches
 * the archive for the requested class entry.
 */
public final class JarStrategy implements ClassLoadingStrategy {
    private final ResourceStrategyResolver resolver;

    /**
     * Creates a strategy with the standard resource strategies.
     */
    public JarStrategy() {
        this(new ResourceStrategyResolver());
    }

    /**
     * Creates a strategy using the specified resource strategy resolver.
     *
     * @param resolver the resolver used to select a resource strategy
     */
    public JarStrategy(ResourceStrategyResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(URI uri) {
        return isJar(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readClass(URI uri, String classPath, String className)
            throws ClassNotFoundException {
        var resourceStrategy = resolver.findSupportingStrategy(uri);
        if (resourceStrategy.isEmpty()) {
            return null;
        }

        byte[] jarBytes;
        try {
            jarBytes = resourceStrategy.get().readBytes(uri);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class: " + className, e);
        }
        if (jarBytes == null) {
            return null;
        }

        try (JarInputStream jarInputStream = new JarInputStream(
                new ByteArrayInputStream(jarBytes))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().equals(classPath)) {
                    return jarInputStream.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class: " + className, e);
        }
        return null;
    }
}