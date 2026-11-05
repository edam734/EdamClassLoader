package com.edam.classloader.strategy.source;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resource strategy that reads bytes from the local filesystem.
 */
public final class LocalResourceStrategy implements ResourceStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(URI uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readBytes(URI uri) throws IOException {
        Path path = Path.of(uri);
        if (!Files.exists(path)) {
            return null;
        }
        return Files.readAllBytes(path);
    }
}
