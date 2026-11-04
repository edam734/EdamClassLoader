# EdamClassLoader

Small Java library for dynamic class loading through a custom `ClassLoader`.

## Goal

The purpose of this project is to provide a small reusable Java library that implements the core mechanics of dynamic class loading.

Instead of relying entirely on `URLClassLoader`, the project uses a custom `ClassLoader` so that the class-loading process remains explicit and understandable.

The library supports:

- loading classes dynamically by fully qualified name;
- delegating to a parent `ClassLoader`;
- locating class bytecode from multiple sources;
- loading classes from local directories and JAR archives;
- retrieving classes and JAR archives through HTTP/HTTPS;
- defining classes through `defineClass()`;
- supporting on-demand class loading.

The project should remain **small, generic and focused exclusively on class loading**.

## Responsibilities

`EdamClassLoader` is responsible for:

- accepting multiple bytecode sources through `URI[]`;
- accepting a parent `ClassLoader`;
- participating in the normal parent delegation model;
- searching configured sources in the specified order;
- locating the bytecode of classes unavailable through the parent;
- retrieving class bytes from local or remote sources;
- defining classes through `defineClass()`;
- loading classes by fully qualified name;
- allowing dependent classes to be loaded later when required by the JVM.

Conceptually:

```text
Class name
    ↓
EdamClassLoader
    ↓
Delegate to parent
    ↓
If not found
    ↓
Search configured sources
    ↓
Retrieve class bytecode
    ↓
defineClass(...)
    ↓
Class<?>
```

## Bytecode Sources

A custom `ClassLoader` is not limited to reading individual `.class` files. Class bytecode can be obtained from several sources:

- **Class files:** Read `.class` files directly from the filesystem.
- **JAR files:** Locate and read class entries inside JAR archives.
- **Network:** Retrieve class bytecode or JAR archives from remote servers through HTTP/HTTPS.
- **Memory:** Load bytecode already available in memory, including dynamically generated or compiled classes.
- **Database:** Retrieve class bytecode stored in a database.

`EdamClassLoader` supports local filesystem sources and HTTP/HTTPS locations through a common `URI[]` configuration.

### Local sources

Local sources may refer to directories containing `.class` files or to JAR archives.

The loader uses:

- `Files` to read individual class files;
- `JarFile` and `JarEntry` to locate classes inside JAR archives.

### Remote sources

Remote sources are accessed using Java's `HttpClient`.

The `RemoteClassReader` component is responsible for making HTTP requests and retrieving byte arrays through `readRemoteBytes()`.

Two scenarios are supported:

**Individual class files**

The loader constructs the class URL from the configured remote directory and the requested class name.

For example:

```text
Root:
https://example.com/classes/

Class:
com.example.Hello

Request:
https://example.com/classes/com/example/Hello.class
```

The received bytecode can be passed directly to `defineClass()`.

**Remote JAR archives**

The loader downloads the JAR as a byte array and reads its contents in memory using `ByteArrayInputStream` and `JarInputStream`.

It then searches for the requested class entry and extracts its bytecode.

```text
Remote JAR URL
    ↓
HTTP request
    ↓
JAR bytes in memory
    ↓
JarInputStream
    ↓
Find requested JarEntry
    ↓
Read class bytes
    ↓
defineClass(...)
```

The JAR does not need to be saved permanently to the filesystem.

### Bytecode transformation

A custom `ClassLoader` may also modify bytecode before defining a class.

For example, bytecode instrumentation can introduce additional instructions for logging, monitoring or performance measurement.

Conceptually:

```text
Retrieve class bytecode
        ↓
Optional bytecode transformation
        ↓
defineClass(...)
        ↓
Class<?>
```

The resulting bytecode must remain valid for the JVM.

Bytecode transformation is not a core requirement of this library, but it is an additional capability worth exploring.

## What does NOT belong here

This project must not know anything about plugins, dependency injection or application-specific rules.

The following responsibilities belong to other projects:

- requiring the source to be a plugin JAR;
- reading plugin-specific manifest entries;
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

## API Design

The library exposes a small API based on a custom `ClassLoader`.

Its constructor accepts multiple class-loading locations:

```java
public EdamClassLoader(URI[] roots, ClassLoader parent)
```

Using `URI[]` allows the same API to represent local and remote locations.

For example:

```text
file:///C:/temp/classes/
file:///C:/temp/jars/example.jar
https://example.com/classes/
https://example.com/library.jar
```

The main responsibilities are:

- receiving the bytecode sources and parent `ClassLoader`;
- implementing `findClass()` to locate and define classes;
- relying on the inherited `loadClass()` implementation for normal parent-first delegation.

Bytecode retrieval is separated into helper methods where appropriate, keeping `findClass()` focused on coordinating the loading process.

## Class loading process

When `loadClass()` is called, Java's standard `ClassLoader` implementation normally follows a **parent-first delegation model**.

Conceptually:

```text
EdamClassLoader.loadClass(...)
        ↓
Check whether class is already loaded
        ↓
Delegate to parent
        ↓
If parent cannot load it
        ↓
EdamClassLoader.findClass(...)
        ↓
Search configured sources
        ↓
Retrieve bytecode
        ↓
defineClass(...)
```

This means the custom loader does not need to reimplement the entire loading algorithm.

Its main responsibility is to implement `findClass()` and provide a way to obtain the class bytecode.

## Parent class loader

The library supports a parent `ClassLoader`.

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

## `loadClass()` vs `findClass()`

The distinction between these methods is central to the project.

`loadClass()` coordinates the normal loading process, including parent delegation.

`findClass()` is responsible for locating and defining a class that could not be loaded by the parent.

The project preserves the existing `loadClass()` implementation provided by `ClassLoader` and customizes `findClass()` instead.

Conceptually:

```text
loadClass()
    ↓
Delegation logic
    ↓
findClass()
    ↓
Custom bytecode lookup
```

Overriding `loadClass()` should only be considered if the project intentionally wants to change Java's normal delegation strategy.

## Reading and defining classes

When a class cannot be obtained from the parent, `EdamClassLoader` searches its configured sources.

A fully qualified class name such as:

```text
com.example.ReportGenerator
```

corresponds conceptually to:

```text
com/example/ReportGenerator.class
```

This path may identify a file inside a filesystem directory, an entry inside a JAR archive or a resource available through HTTP/HTTPS.

The retrieval mechanism depends on the source:

- Local class files are read from the filesystem.
- Local JAR entries are read through `JarFile`.
- Remote class files are retrieved through HTTP.
- Remote JAR entries are extracted from the downloaded archive in memory.

Regardless of the source, the final process is the same:

```text
Locate class bytecode
        ↓
Retrieve its bytes
        ↓
Pass the bytes to defineClass(...)
        ↓
Receive Class<?>
```

`defineClass()` is the point at which the bytecode becomes a JVM class associated with this particular `ClassLoader`.

## Class identity

In Java, a class is not identified only by its fully qualified name.

Its identity is effectively determined by:

```text
Class name + defining ClassLoader
```

Therefore, two loaders may load bytecode representing classes with exactly the same name and still produce different JVM types.

Conceptually:

```text
Loader A → com.example.Widget
Loader B → com.example.Widget
```

These two `Widget` classes may not be assignment-compatible even though their names are identical.

This behavior is demonstrated through tests.

## Lazy class loading

Loading one class does not necessarily cause all of its dependencies to be loaded immediately.

For example:

```text
ReportGenerator
    ↓
Loaded now

PdfFormatter
    ↓
May only be loaded later when required
```

The `EdamClassLoader` therefore needs to remain available for as long as dynamically loaded classes may still require additional classes.

The library does not impose an eager or lazy loading strategy on its consumers. Applications may request classes in advance or only when needed.

## Class-loader lifecycle

Unlike a `URLClassLoader`, a custom `ClassLoader` does not necessarily own closeable resources.

If the implementation opens files, archives, streams or network connections, their lifecycle must be managed correctly.

The library uses short-lived resources where possible:

- Local JAR files are closed after reading.
- Input streams are closed after use.
- Remote bytecode can be held in memory without permanent local files.

If future implementations introduce persistent resources, ownership and closing semantics should remain explicit.

## Project scope

The project is intentionally small.

The goal is not to build:

- a complete module system;
- a plugin framework;
- a dependency injection container;
- an HTTP server;
- a custom JVM class-loading subsystem.

The goal is to create a focused reusable library while learning and exposing the fundamental mechanics of Java class loading.

## Tests

The project includes tests that demonstrate both functionality and class-loader behavior.

Test scenarios include:

- loading a class from an external `.class` file;
- loading a class from a local JAR archive;
- loading classes from multiple configured sources;
- verifying source search order;
- loading a class that exists only in the parent;
- confirming parent-first delegation;
- loading a class with dependencies;
- handling missing classes through `ClassNotFoundException`;
- loading the same class through different `EdamClassLoader` instances;
- verifying that classes with the same name but different defining loaders are different JVM types;
- observing lazy loading of dependent classes;
- loading an individual `.class` file through HTTP;
- loading a class from a remote JAR archive.

### Remote Class Loading Tests

Remote class loading tests use the `jwebserver` utility included in JDK 18 and later.

The test HTTP server must be started before running the remote integration tests.

Example directory structure:

```text
C:\temp\
├── classes\
│   └── com\
│       └── example\
│           └── RemoteHello.class
└── jars\
    └── remote-hello-test.jar
```

Start the server:

```powershell
jwebserver -p 8000 -d "C:\temp"
```

If `jwebserver` is not available in the system `PATH`, execute it from the `bin` directory of a compatible JDK installation.

The server exposes the following resources:

```text
http://localhost:8000/classes/com/example/RemoteHello.class
http://localhost:8000/jars/remote-hello-test.jar
```

The tests verify that `EdamClassLoader` retrieves and defines `com.example.RemoteHello` from both sources.

These are integration tests and require the HTTP server to be running.

### Lazy Loading Behavioral Test

A separate behavioral test demonstrates when the JVM requests dependent classes.

The test loads `ReportGenerator`, creates an instance and invokes `generateReport()`, which depends on `ReportFormatter`.

The exact moment when the JVM loads dependencies is implementation-dependent and should not be treated as a guaranteed execution order.

## Technology

- Java 25
- IntelliJ IDEA
- Maven
- JUnit 5
- Java HttpClient

## Development priorities

1. Implement a custom `ClassLoader` using `findClass()`.
2. Preserve Java's parent-first delegation behavior.
3. Support multiple bytecode sources through `URI[]`.
4. Load classes from filesystem directories and local JAR archives.
5. Retrieve classes and JAR archives through HTTP/HTTPS.
6. Separate bytecode retrieval from class definition.
7. Define classes using `defineClass()`.
8. Test local and remote class-loading behavior.
9. Verify parent delegation, class identity and lazy dependency loading.
10. Keep the library small and free from application-specific logic.
11. Package the library as a reusable JAR.
