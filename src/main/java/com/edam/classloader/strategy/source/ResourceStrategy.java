package com.edam.classloader.strategy.source;

import java.io.IOException;
import java.net.URI;

/**
 * Defines a strategy for retrieving bytes from a resource location,
 * independently of the resource's format.
 */
public interface ResourceStrategy {

    /**
     * Checks whether this strategy supports the specified URI.
     *
     * @param uri the resource location
     * @return true if the URI is supported, false otherwise
     */
    boolean supports(URI uri);

    /**
     * Retrieves the bytes of the specified resource.
     *
     * @param uri the resource location
     * @return the resource bytes, or null if they cannot be retrieved
     * @throws IOException if an I/O error is reported by the strategy
     */
    byte[] readBytes(URI uri) throws IOException;
}