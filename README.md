# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin creates native installers for Java-based applications.
It is intended to be used as a replacement for the built-in application plugin.
It brings its own run task and application extension but omits the distribution plugin
The extension's API resembles the application plugin's one, this should simplify migration.

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
  // also offers `applicationName`, `mainClass` and friends for migration
  metadata.name = "SampleApp"
  launcher.mainClass = "org.example.App"
}
```

The plugin relies on the Java plugin to infer sensible defaults, so the build script can be very concise,

## Fundamentally Different

Instead of overriding or augmenting the settings of the java plugins with domain-specific static conventions, it  relies on JDK-included tools like Jlink, Jpackage and Jdeps to figure out the right settings for tasks that compile and run Java applications.
This is the more general and stable approach compared to patching other plugins' settings.

### JavaFX

The JavaFX plugin Adds its modules to the run tasks to make it work “auto-magically.”
Actually, this is not necessary if Jdeps analyzes the app dependencies beforehand.
The Jdeps tool included with the JDK is able to figure out the right modules.
Thus, JavaFX is no special edge case.
This plugin uses the Jdeps result in its Jlink and run tasks.

### Kotlin

Less magic means that some parts have to be a bit more explicit.
A typical Java-based app has a class with a static `main` method that serves as an entry point to make the built Jar file runnable.
In Kotlin, `main` can be a top-level function in a file, this works seamlessly with this plugin.
If `main` is part of the companion object, it must be annotated with `@JvmStatic` to become a “real” static method of its class.
An `org.example.App` class would look like this:

```kotlin
class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
        companion object {
@JvmStatic
fun main(vararg args: String) {
    println(App().greeting)
}
        }
}
```

### Issues

If you run into a situation where this plugin conflicts with another one, you're invited to [create an issue](https://github.com/infolektuell/gradle-jpackage/issues/new) and describe your problem.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)
