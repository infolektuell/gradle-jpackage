# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin relies on JDK tools like Jlink, Jpackage and Jdeps to create native installers for Java-based applications.
It is a drop-in replacement for the built-in application plugin.

## Features

- [x] Supports modular and non-modular applications.
- [x] Works with JavaFX out of the box.
- [x] Low-level tasks for tools like Jlink, Jdeps, and Jpackage for any custom needs, offering access to the CLI options in a type-safe way.
- [x] Compatible with Gradle's configuration cache and build cache.
- [ ] Signing macOS apps (coming soon)

## Quick Start

```kts
plugins {
    java //Please omit the application plugin
    id("de.infolektuell.jpackage") version "x.y.z"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
      // The jpackage plugin uses the configured toolchain by default  
      languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
  metadata.name = "SampleApp"
  // Define the main class for the application.
  launcher.mainClass = "org.example.App"
}
```

The plugin relies on the Java plugin to infer sensible defaults, so the build script can be very concise,

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)
