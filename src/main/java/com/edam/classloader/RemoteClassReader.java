package com.edam.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RemoteClassReader {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static byte[] searchRemote(URI uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        try {
            HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("Erro ao buscar dados. Código HTTP: " + response.statusCode());
            }
        } catch (IOException e) {
            System.err.println("Não encontrado o ficheiro de destino: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
