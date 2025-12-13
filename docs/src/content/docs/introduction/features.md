---
title: Features
description: The goals, features, and requirements of the Gradle Jpackage plugin
sidebar:
  order: 1
---

Jpackage is the tool that creates native installers for Java-based applications.
This Gradle plugin adds Jpackage & friends to the build process.
It seamlessly integrates with the Java plugin and with the application plugin if it is applied.

## Goals

- Streamlining the development of Java-based cross-platform apps
- Encouraging the adoption of Java modules (JPMS)
- Providing general solutions instead of edge case implementations by combining all the useful tools included in the JDK

## Features

- Supports modular and non-modular applications.
- Uses Jdeps to support the modularization process by analyzing the code for needed modules
- Helps with patching modules in the compilation process. This enables developers to create modules with other JVM-compatible languages like Kotlin.
- Works with JavaFX out of the box.
- Supports signing macOS applications.
- Modern implementation adhering to Gradle's [best practices].
- Compatible with [Configuration Cache], and tasks are [cacheable][build cache].

## Requirements

- At least JDK 21 to run the plugin, of course any Java toolchain that comes with Jpackage can be used in the project.
- Gradle v8.8 or above.

[configuration cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[build cache]: https://docs.gradle.org/current/userguide/build_cache.html
[best practices]: https://docs.gradle.org/current/userguide/best_practices.html
