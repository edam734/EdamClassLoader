package com.edam.classloader;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class EdamClassLoader extends ClassLoader {
    private final URI[] roots;

    public EdamClassLoader(URI[] roots, ClassLoader parent) {
        super(parent);
        this.roots = Objects.requireNonNull(roots, "root").clone();
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        byte[] bytes = null;
        String classFilePath = className.replace(".", "/") + ".class";
        for (int i = 0; i < roots.length && bytes == null; i++) {
            String scheme = roots[i].getScheme();
            if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                if (!roots[i].getPath().toLowerCase().endsWith(".jar")) {
                    URI fullPath = resolveToFullPath(roots[i], classFilePath);
                    bytes = RemoteClassReader.readRemoteBytes(fullPath);
                } else {
                    byte[] jarBytes = RemoteClassReader.readRemoteBytes(roots[i]);
                    bytes = readJarBytes(className, classFilePath, jarBytes);
                }
            } else if (scheme.equalsIgnoreCase("file")) {
                Path root = Path.of(roots[i]);
                if (Files.isDirectory(root)) {
                    Path filePath = root.resolve(classFilePath);
                    bytes = readClassFile(className, filePath);
                } else if (Files.isRegularFile(root) &&
                        root.getFileName().toString().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(root.toFile())) {
                        JarEntry entry = jarFile.getJarEntry(classFilePath);
                        bytes = readJarEntry(className, entry, jarFile);
                    } catch (IOException e) {
                        // Continue searching in the remaining roots.
                    }
                }
            }
        }
        if (bytes == null) {
            throw new ClassNotFoundException(className);
        }

        return defineClass(className, bytes, 0, bytes.length);
    }

    private URI resolveToFullPath(URI root, String classFilePath) {
        if (!root.toString().endsWith("/")) {
            root = URI.create(root + "/");
        }
        return root.resolve(classFilePath);
    }

    private static byte[] readClassFile(String className, Path filePath) throws
            ClassNotFoundException {
        if (Files.exists(filePath)) {
            try {
                return Files.readAllBytes(filePath);
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load class: " + className, e);
            }
        }
        return null;
    }

    private static byte[] readJarEntry(String className, JarEntry entry, JarFile jarFile) throws
            ClassNotFoundException {
        if (entry != null) {
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                return inputStream.readAllBytes();
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load class: " + className, e);
            }
        }
        return null;
    }

    private static byte[] readJarBytes(String className, String classFilePath,
                                       byte[] jarBytes) throws ClassNotFoundException {
        byte[] bytes = null;
        if (jarBytes != null) {
            try (JarInputStream jarInputStream = new JarInputStream(
                    new ByteArrayInputStream(jarBytes))) {
                boolean found = false;
                JarEntry jarEntry;
                while (!found && (jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    if (jarEntry.getName().equals(classFilePath)) {
                        found = true;
                        try {
                            bytes = jarInputStream.readAllBytes();
                        } catch (IOException e) {
                            throw new ClassNotFoundException("Could not load class: " + className,
                                    e);
                        }
                    }
                }
            } catch (IOException e) {
                // Continue searching in the remaining roots.
            }
        }
        return bytes;
    }
}
