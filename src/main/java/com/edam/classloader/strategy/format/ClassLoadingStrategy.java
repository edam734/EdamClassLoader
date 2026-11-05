package com.edam.classloader.strategy.format;

import java.net.URI;

/**
 * Defines a strategy for locating class bytecode within a resource format.
 */
public interface ClassLoadingStrategy {

    /**
     * Checks whether this strategy supports the specified location.
     *
     * @param uri the resource location
     * @return true if the location is supported, false otherwise
     */
    boolean supports(URI uri);

    /**
     * Reads the bytecode of a class from the specified resource.
     *
     * @param uri the resource location
     * @param classPath the path of the class within the resource
     * @param className the fully qualified name of the class
     * @return the class bytecode, or null if the class is not found
     * @throws ClassNotFoundException if the class cannot be read
     */
    byte[] readClass(URI uri, String classPath, String className) throws ClassNotFoundException;

    /**
     * Checks whether the URI represents a JAR according to the configured
     * naming convention: resource paths without a trailing slash are
     * treated as JARs.
     *
     * @param uri the resource location
     * @return true if the URI is treated as a JAR
     */
    default boolean isJar(URI uri) {
        String path = uri.getPath();
        return path != null && !path.endsWith("/");
    }
}