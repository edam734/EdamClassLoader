package com.edam.classloader.strategy.format;

import com.edam.classloader.strategy.ResourceStrategyResolver;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Strategy for loading individual class files from a directory-like
 * resource location.
 */
public final class ClassFileStrategy implements ClassLoadingStrategy {
    private final ResourceStrategyResolver resolver;

    /**
     * Creates a strategy with the standard resource strategies.
     */
    public ClassFileStrategy() {
        this(new ResourceStrategyResolver());
    }

    /**
     * Creates a strategy using the specified resource strategy resolver.
     *
     * @param resolver the resolver used to select a resource strategy
     */
    public ClassFileStrategy(ResourceStrategyResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(URI uri) {
        String path = uri.getPath();
        return path != null && path.endsWith("/");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readClass(URI uri, String classPath, String className)
            throws ClassNotFoundException {
        URI classUri = uri.resolve(classPath);
        var resourceStrategy = resolver.findSupportingStrategy(classUri);
        if (resourceStrategy.isEmpty()) {
            return null;
        }
        try {
            return resourceStrategy.get().readBytes(classUri);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class: " + className, e);
        }
    }
}