# DynamicClassLoader

<br>

Small Java library for loading classes dynamically from external JAR files at runtime.

<br>

## Goal

The purpose of this project is to provide a small reusable Java library for loading classes dynamically from external JAR files at runtime.

The library should offer a simple API for creating and managing a dedicated `ClassLoader`, loading classes by name, and controlling the lifecycle of the underlying resources.

The project should remain **small, generic and focused exclusively on dynamic class loading**.

<br>

## Responsibilities

`DynamicClassLoader` should be responsible for:

- receiving the path of an external JAR;
- creating and owning a `URLClassLoader`;
- accepting a parent `ClassLoader`;
- loading a class by its fully qualified name;
- loading classes lazily as required by the JVM;
- exposing a clean lifecycle for the underlying class loader;
- closing the `URLClassLoader` when it is no longer needed.

Conceptually:

```text
external JAR
    ↓
DynamicClassLoader
    ↓
URLClassLoader
    ↓
load class by name
    ↓
Class<?>
```

<br>

## What does NOT belong here

This project must not know anything about plugins or dependency injection.

The following responsibilities belong to other projects:

- reading a `Plugin-Class` manifest entry;
- validating that a class implements a `Plugin` interface;
- instantiating or executing plugins;
- `PluginRegistry`;
- `LoadedPlugin`;
- plugin cache / shadow copies;
- `WatchService`;
- handling `CREATED`, `MODIFIED`, `DELETED` or `OVERFLOW` events;
- file hashing;
- retry logic;
- plugin lifecycle management;
- dependency resolution or dependency injection.

The class loader should simply load classes.

<br>

## Initial API idea

A minimal first version may look conceptually like this:

```java
public final class DynamicClassLoader implements Closeable {

    private final URLClassLoader classLoader;

    public DynamicClassLoader(Path jarPath, ClassLoader parent)
            throws MalformedURLException {
        URL jarUrl = jarPath.toUri().toURL();

        classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                parent
        );
    }

    public Class<?> loadClass(String className)
            throws ClassNotFoundException {
        return Class.forName(
                className,
                false,
                classLoader
        );
    }

    @Override
    public void close() throws IOException {
        classLoader.close();
    }
}
```

This is only the starting design. The API can be refined during development.

<br>

## Parent class loader

The library should support a parent `ClassLoader`.

This is important because a dynamically loaded class may depend on classes that are not inside its own JAR.

For example:

```text
ReportGenerator.class → loaded from the external JAR
PdfFormatter.class    → loaded from the external JAR
LoggingService.class  → may already exist in the parent application
String.class          → provided by the Java runtime
ArrayList.class       → provided by the Java runtime
```

Java normally uses **parent-first delegation**. The dynamically created class loader can therefore reuse classes already available to the parent instead of loading independent copies.

<br>

## Class-loader lifecycle

The class loader must remain open for as long as dynamically loaded classes may still need additional classes or resources from the JAR.

A class may be loaded first and only later require another class from the same JAR.

Therefore:

```text
create DynamicClassLoader
        ↓
load class
        ↓
use dynamically loaded objects/classes
        ↓
keep class loader alive
        ↓
close when no longer needed
```

The library should make this lifecycle explicit and easy to manage, preferably through `Closeable` / try-with-resources when appropriate.

<br>

## Project scope

The project is intentionally small.

The goal is not to build a complete module system or plugin framework. The goal is to create a focused reusable Java library around dynamic class loading.

<br>

## Technology

- Java 25
- IntelliJ IDEA
- Build tool: to be decided; Maven is a likely option if the library is published and consumed as a dependency.

<br>

## Development priorities

1. Define a small and focused public API.
2. Implement dynamic class loading from external JAR files.
3. Support parent `ClassLoader` delegation.
4. Preserve explicit ownership and closing of the underlying `URLClassLoader`.
5. Add unit tests for successful and failed class loading.
6. Package the library as a reusable JAR.
7. Optionally publish the JAR to GitHub Packages, Nexus, or another Maven-compatible repository.

<br>
