# EdamClassLoader

<br>

Small Java library for dynamic class loading through a custom `ClassLoader`.

<br>

## Goal

The purpose of this project is to provide a small reusable Java library that implements the core mechanics of dynamic class loading.

Instead of relying entirely on `URLClassLoader`, the project should use a custom `ClassLoader` so that the class-loading process remains explicit and understandable.

The library should support:

- loading classes dynamically by fully qualified name;
- delegating to a parent `ClassLoader`;
- locating class bytecode from an external source;
- defining classes through `defineClass()`;
- preserving Java's normal lazy class-loading behavior.

The project should remain **small, generic and focused exclusively on class loading**.

<br>

## Responsibilities

`EdamClassLoader` should be responsible for:

- accepting a source from which class bytecode can be obtained;
- accepting a parent `ClassLoader`;
- participating in the normal parent delegation model;
- locating the bytecode of classes that are not available through the parent;
- reading the corresponding `.class` bytes;
- defining classes through `defineClass()`;
- loading classes by fully qualified name;
- allowing additional dependent classes to be loaded later when required by the JVM.

Conceptually:

```text
class name
    ↓
EdamClassLoader
    ↓
delegate to parent
    ↓
if not found
    ↓
locate class bytecode
    ↓
read .class bytes
    ↓
defineClass(...)
    ↓
Class<?>
```

<br>

## Bytecode Sources

A custom ClassLoader is not limited to reading individual `.class` files. Class bytecode can be obtained from several sources:

- Class files: Read .class files directly from the filesystem.
- JAR files: Locate and read class entries inside JAR archives.
- Network: Retrieve class bytecode from a remote server, for example, through HTTP.
- Memory: Load bytecode already available in memory, including dynamically generated or compiled classes.
- Database: Retrieve class bytecode stored in a database.

EdamClassLoader should support loading classes from filesystem directories and JAR archives, while keeping the design flexible enough to accommodate other bytecode sources.

<br>

## Bytecode transformation

A custom `ClassLoader` may also modify bytecode before defining a class.

For example, bytecode instrumentation can introduce additional instructions for logging, monitoring or performance measurement.

Conceptually:

Retrieve class bytecode
        ↓
Optional bytecode transformation
        ↓
defineClass(...)
        ↓
Class<?>

The resulting bytecode must remain valid for the JVM.

Bytecode transformation is not a core requirement of this library, but it is an additional capability worth exploring.

<br>

## What does NOT belong here

This project must not know anything about plugins, dependency injection or application-specific rules.

The following responsibilities belong to other projects:

- requiring the source to be a plugin JAR;
- reading manifest entries;
- validating that a class implements a particular interface;
- instantiating or executing dynamically loaded classes;
- plugin registries;
- plugin lifecycle management;
- directory monitoring;
- `WatchService`;
- cache or shadow-copy management;
- file hashing;
- retry logic;
- dependency injection;
- application-specific validation.

The class loader should simply load classes.

<br>

## Initial API idea

A minimal first version may look conceptually like this:

```java
public final class EdamClassLoader extends ClassLoader {

    private final Path root;

    public EdamClassLoader(Path root, ClassLoader parent) {
        super(parent);
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    protected Class<?> findClass(String className)
            throws ClassNotFoundException {

        // Convert the fully qualified class name into a path,
        // read the corresponding .class bytes,
        // and define the class through defineClass(...).

        throw new UnsupportedOperationException();
    }
}
```

This is only the starting design. The exact source abstraction and public API can be refined during development.

<br>

## Class loading process

When `loadClass()` is called, Java's standard `ClassLoader` implementation normally follows a **parent-first delegation model**.

Conceptually:

```text
EdamClassLoader.loadClass(...)
        ↓
check whether class is already loaded
        ↓
delegate to parent
        ↓
if parent cannot load it
        ↓
EdamClassLoader.findClass(...)
        ↓
locate bytecode
        ↓
defineClass(...)
```

This means the custom loader does not need to reimplement the entire loading algorithm.

Its main responsibility is normally to implement `findClass()` and provide a way to obtain the class bytecode.

<br>

## Parent class loader

The library should support a parent `ClassLoader`.

A dynamically loaded class may depend on classes that are already available elsewhere in the application or Java runtime.

For example:

```text
ReportGenerator.class → loaded by EdamClassLoader
PdfFormatter.class    → loaded by EdamClassLoader
LoggingService.class  → may already exist in the parent
String.class          → provided by the Java runtime
ArrayList.class       → provided by the Java runtime
```

Because delegation is recursive, asking the parent to load a class may cause the request to continue further up the class-loader hierarchy.

Conceptually:

```text
Bootstrap ClassLoader
        ↑
Platform ClassLoader
        ↑
Application ClassLoader
        ↑
EdamClassLoader
```

Each loader delegates to its own parent, producing the complete delegation chain.

<br>

## `loadClass()` vs `findClass()`

The distinction between these methods is central to the project.

`loadClass()` coordinates the normal loading process, including parent delegation.

`findClass()` is responsible for locating and defining a class that could not be loaded by the parent.

The project should normally preserve the existing `loadClass()` implementation provided by `ClassLoader` and customize `findClass()` instead.

Conceptually:

```text
loadClass()
    ↓
delegation logic
    ↓
findClass()
    ↓
custom bytecode lookup
```

Overriding `loadClass()` should only be considered if the project intentionally wants to change Java's normal delegation strategy.

<br>

## Reading and defining classes

When a class cannot be obtained from the parent, `EdamClassLoader` should locate the corresponding `.class` file.

A fully qualified class name such as:

```text
com.example.ReportGenerator
```

corresponds conceptually to:

```text
com/example/ReportGenerator.class
```

The loader then:

```text
locates the .class file
        ↓
reads its bytes
        ↓
passes the bytes to defineClass(...)
        ↓
receives Class<?>
```

`defineClass()` is the point at which the bytecode becomes a JVM class associated with this particular `ClassLoader`.

<br>

## Class identity

In Java, a class is not identified only by its fully qualified name.

Its identity is effectively determined by:

```text
class name + defining ClassLoader
```

Therefore, two loaders may load bytecode representing classes with exactly the same name and still produce different JVM types.

Conceptually:

```text
Loader A → com.example.Widget
Loader B → com.example.Widget
```

These two `Widget` classes may not be assignment-compatible even though their names are identical.

This behavior is one of the main concepts the project should demonstrate through tests.

<br>

## Lazy class loading

Loading one class does not necessarily cause all of its dependencies to be loaded immediately.

For example:

```text
ReportGenerator
    ↓
loaded now

PdfFormatter
    ↓
may only be loaded later when first required
```

The `EdamClassLoader` therefore needs to remain available for as long as dynamically loaded classes may still require additional classes.

<br>

## Class-loader lifecycle

Unlike a `URLClassLoader`, a custom `ClassLoader` does not necessarily own closeable resources.

If the implementation opens files, archives or streams that remain active, their lifecycle must be managed explicitly.

Whenever possible, class bytecode should be read using short-lived resources so that the `ClassLoader` itself does not require an explicit `close()` operation.

If future implementations introduce persistent resources, ownership and closing semantics should remain explicit.

<br>

## Project scope

The project is intentionally small.

The goal is not to build:

- a complete module system;
- a plugin framework;
- a dependency injection container;
- a custom JVM class-loading subsystem.

The goal is to create a focused reusable library while learning and exposing the fundamental mechanics of Java class loading.

<br>

## Tests

The project should include tests that demonstrate both functionality and class-loader behavior.

Useful scenarios include:

- loading a class that exists only in the custom source;
- loading a class that exists only in the parent;
- confirming parent-first delegation;
- loading a class with dependencies;
- attempting to load a missing class;
- loading the same class through two different `EdamClassLoader` instances;
- verifying that classes with the same name but different defining loaders are different JVM types;
- verifying lazy loading of dependent classes.

<br>

## Technology

- Java 25
- IntelliJ IDEA
- Maven

<br>

## Development priorities

1. Create a custom `ClassLoader`.
2. Implement class lookup through `findClass()`.
3. Convert fully qualified class names into `.class` paths.
4. Read class bytecode from an external source.
5. Define classes using `defineClass()`.
6. Preserve Java's parent-first delegation behavior.
7. Add tests for parent delegation.
8. Add tests for class identity across different class loaders.
9. Add tests for lazy dependency loading.
10. Refine the source abstraction if support for multiple source types becomes useful.
11. Package the library as a reusable JAR.

<br>
