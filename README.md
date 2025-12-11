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

<details>
<summary>Main class not found</summary>

Gradle init generates a Kotlin app where the `main` method is a top-level function in an `App.kt` file.
this works out of the box, but if the `main` method is part of a companion object, e.g., `App.main` it won't be found by Jdeps or the run task.

Less magic means that some parts have to be a bit more explicit.
If a method like `main` is part of the companion object, it needs the `@JvmStatic` annotation to become a “real” static class member.

```kotlin
// org.example.App.kt
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

</details>

<details>
<summary>Java module with Kotlin sources</summary>

Modularizing a Kotlin JVM project is **still challenging**.
Such projects contain Kotlin sources and a  `module-info.java` module descriptor that exports packages from the Kotlin sources.
The build fails, because the Java compiler is unable to find the packages to export.
This is a Kotlin problem and independent of this plugin, but modularization is its key point, so the [solution][kotlin-jpms] is documented here.

```kts
// build.gradle.kts
tasks.compileJava {
    options.compilerArgumentProviders.add(object : CommandLineArgumentProvider {
        @CompileClasspath
        val kotlinClasses = kotlin.sourceSets.main.flatMap { it.kotlin.classesDirectory }
        @Input
        val moduleName = application.mainModule

        override fun asArguments() = listOf(
            "--patch-module",
            "${moduleName.get()}=${kotlinClasses.get().asFile.absolutePath}"
        )
    })
}
```

[kotlin-jpms]: https://discuss.gradle.org/t/mixing-kotlin-and-java-in-a-jpms-module-gradle-project/48011

</details>

### Fat Jars

**you probably don't want fat jars for an application.**

Fat jars or uber jars can be a reasonable solution for libraries.
They bundle the project's classes and all dependencies in one jar file.
Jpackage is an alternative approach that is more suited for applications.
So this plugin doesn't support them out of the box, and you would have to reconfigure its tasks.
For many projects with much library-like code, the following strategy is advisable:

1. Move that code into a separate Gradle subproject next to the application subproject.
2. Apply plugins like [Java Library] and [Shadow] to the project to build the lib as a fat jar.
3. Add a dependency on that library subproject to the application's subproject.
4. Even better, modularize the lib, so Jlink will include it in the JRE and actually, the fat jar step is dismissable.

This is a main reason why Gradle projects should consist of subprojects from the beginning instead of using the root project.

[java library]: https://docs.gradle.org/current/userguide/java_library_plugin.html
[shadow]: https://gradleup.com/shadow/

### Issues

If you run into some edge cases or a situation where this plugin conflicts with another one, you're invited to [create an issue](https://github.com/infolektuell/gradle-jpackage/issues/new) and describe your problem.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)
