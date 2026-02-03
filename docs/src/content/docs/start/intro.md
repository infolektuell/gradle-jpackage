---
title: About this Plugin
description: The goals, features, and requirements of the Gradle Jpackage plugin
sidebar:
  label: About
---

This Gradle plugin turns Java applications into self-contained executables and installers for distribution.
It uses the official tools coming with the JDK like jpackage, jlink, and jdeps.

## Goals

- Streamlining the development of Java-based cross-platform apps
- Encouraging the adoption of Java modules (JPMS)
- Providing general solutions instead of edge case implementations by combining all the useful tools included in the JDK

## Features

- [x] Supports modular and non-modular applications.
- [x] Searches for required modules in non-modular apps to take advantage of modularization as much as possible.
- [x] Works with JavaFX out of the box.
- [x] Modern implementation adhering to Gradle's [best practices].
- [x] Compatible with [Configuration Cache], and tasks are [cacheable][build cache].
- [ ] Supports signing macOS applications (coming soon).

## Requirements

- At least JDK 21 to run the plugin, of course any Java toolchain that comes with Jpackage can be used in the project.
- Gradle v8.8 or above.

[configuration cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[build cache]: https://docs.gradle.org/current/userguide/build_cache.html
[best practices]: https://docs.gradle.org/current/userguide/best_practices.html
