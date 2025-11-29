# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin utilizes jpackage and jlink to create native installers for Java applications. 

## Features

- Supports modular and non-modular applications.
- High-level convenience plugin and DSL extension that generates native installers for apps built with the Java Application plugin.
- Low-level tasks for Jlink and Jpackage for any custom needs, offering access to the CLI options in a type-safe way.
- Compatible with Gradle's configuration cache and build cache.

## Quick Start

```kts
plugins {
    application
    id("de.infolektuell.java-packaging") version "x.y.z"
}

jpackage {
  // Configuration for the generated runtime image
  runtime {
    // Modules to include in the runtime image
    modules = listOf("java.base")
  }
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
```

## Background

Jpackage is the application packaging tool that comes with JDK 14 and above.
Its packaging process consists of three stages:

1. Runtime image: It runs Jlink to generates a custom runtime image that contains only the necessary modules for your app.
2. Application image: Jpackage composes a standardized directory structure containing the generated runtime, application files and other resources.
3. Packaging: Jpackage creates a platform-native installer from the application image. It can even create installable custom JRE by omitting any app-specific configuration.

Jpackage tool combines these three stages in one CLI, so one could achieve everything with one single command.
It runs Jlink implicitly and makes many implicit decisions.
In this plugin, I made these stages more explicit, because I found that “one CLI for everything” approach quite confusing.
Besides, splitting the process into multiple steps with intermediate inputs and outputs benefits more from Gradle's caching capabilities.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)
