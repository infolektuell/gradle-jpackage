# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin creates native installers for apps built with the Java Application plugin. 

## Features

- Supports modular and non-modular applications.
- Low-level tasks for tools like Jlink, Jdeps, and Jpackage for any custom needs, offering access to the CLI options in a type-safe way.
- Compatible with Gradle's configuration cache and build cache.

## Quick Start

```kts
plugins {
    application
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
  // Define the main class for the application.
  mainClass = "org.example.App"
}

jpackage {
  metadata.name = "Nonmodular Sample App"
}
```

In many cases, configuring the application plugin and some jpackage metadata is sufficient.
The plugin grabs as much information as possible from the java and application plugins,
and it guesses the modules needed by nonmodular apps using the jdeps tool.

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
