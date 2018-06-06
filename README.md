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

## How To

1. Add Maven Dependency

```xml
<dependency>
    <groupId>com.github.panga</groupId>
    <artifactId>java-beans-lite</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Find and replace imports

Find all ocurrences of `import java.beans` and replace with `import lite.beans`.

3. Done!

## Contributors

* Leonardo Zanivan <panga@apache.org>

## License

[Apache License 2.0](LICENSE)