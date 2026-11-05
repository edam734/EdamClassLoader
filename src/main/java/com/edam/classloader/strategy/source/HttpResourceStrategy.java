package com.edam.classloader.strategy.source;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Resource strategy that retrieves bytes from HTTP and HTTPS locations.
 *
 * <p>Uses a shared HTTP client and retrieves resource content into memory.
 */
public final class HttpResourceStrategy implements ResourceStrategy {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(URI uri) {
        String scheme = uri.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns null for unsuccessful HTTP responses or connection failures.
     * If interrupted, restores the thread's interruption status.
     */
    @Override
    public byte[] readBytes(URI uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        try {
            HttpResponse<byte[]> response = CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException e) {
            // Unavailable resource: allow the loader to try the next root.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}