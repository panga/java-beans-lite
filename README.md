# Java Beans Lite

Lightweight and fast `java.beans.Introspector` reimplementation used to remove dependency on `java.desktop` module for Bean Introspection.

## Problem

The [java.desktop](https://docs.oracle.com/javase/9/docs/api/java.desktop-summary.html) module introduced in JDK 9 encapsulates all AWT, Swing, Image and Sound packages from Java standard libraries.

In addition to that, it contains the `java.beans` package with helper classes to interact with *Java Beans*, specifically doing introspection.

Due to its tight dependency with AWT, it cannot be easily removed from `java.desktop` module, causing any thirdparty library that uses `java.beans` package to be dependent of all `java.desktop` classes, adding extra `12mb` of size overhead into the JVM installation and some extra memory usage.

## Solution

Reimplemented `java.beans.Introspector` class to do analysis of Java Beans in a very lightweight and super fast way (analysis results are cached automatically) with only `java.base` module packages dependency.

The initial implementation is a small subset of `Introspection` class methods, it doesn't implement the full interface.

The total size of this library is less than `10kb`!

However, it adds enought data to be used by Object Mapping (xml, json) and Dependency Injection libraries.

## Usage

### Using as a `java.beans` replacement

1. Add a Maven Dependency

```xml
<dependency>
    <groupId>com.github.panga</groupId>
    <artifactId>java-beans-lite</artifactId>
    <version>1.0.1</version>
</dependency>
```

2. Find all ocurrences of `import java.beans` and replace with `import lite.beans`.

### Using as a module patch (hacking JPMS)

1. Add a Maven Dependency or download the jar (notice the classifier)

```xml
<dependency>
    <groupId>com.github.panga</groupId>
    <artifactId>java-beans-lite</artifactId>
    <version>1.0.1</version>
    <classifier>patch</classifier>
</dependency>
```

2. Run your application in a minimal JRE without `java.desktop` module without any change into any library using `java.beans` package:

```bash
java \
    --patch-module java.base=java-beans-lite-1.0.1-patch.jar \
    --add-exports java.base/java.beans=acme.mymodule \
    --module-path target/modules --module acme.mymodule \
```

_See https://github.com/panga/hammock-jpms example._

## Contributors

* Leonardo Zanivan <panga@apache.org>

## License

[Apache License 2.0](LICENSE)