# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin creates native installers for Java-based applications.
It integrates with the Java plugin and with the application plugin if it is applied.

## Features

- [x] Supports modular and non-modular applications.
- [x] Works with JavaFX out of the box.
- [x] Low-level tasks for tools like Jlink, Jdeps, and Jpackage for any custom needs, offering access to the CLI options in a type-safe way.
- [x] Compatible with Gradle's configuration cache and build cache.
- [ ] Signing macOS apps (coming soon)

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
  applicationName = "SampleApp"
  mainClass = "org.example.App"
}
```

The plugin retrieves its conventions from the Java and Application plugin, so the build script can be very concise,

## Use-Cases

### JavaFX

JavaFX is no special edge case.
The powerful Jdeps tool included with the JDK is able to figure out the right modules for Jlink.
In general, users shouldn't need to manually supply the modules to be included in the runtime image.

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
<summary>Patching Java module with Kotlin sources</summary>

Some projects contain Kotlin sources and a  `module-info.java` module descriptor that exports packages from the Kotlin sources.
The build fails, because the Java compiler is unable to find the packages to export.
The compiler needs the information where the packages and classes for that module can be found (module patching).

The Java plugin doesn't offer a convenient way to declare module patches.
But this plugin adds a source set extension where module patches can be defined and will be passed to the compiler.

```kts
// build.gradle.kts
sourceSets.named("main") {
  patchModule.define {
    module = application.mainModule
    classes.from(kotlin.classesDirectory)
  }
}
```

The inspiration came from [this forum thread][kotlin-jpms].

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

### No Application Plugin

The plugin also works without the application plugin.
It adds the main class to the manifest file and brings it own configurable DSL extension.
But there will be no run task without the Application plugin.

```kts
plugins {
    java
    id("de.infolektuell.jpackage")
}

jpackage {
  metadata.name = "Sample"
  launcher {
    mainModule = "example.app"
    mainClass = "org.example.App"
  }
}
```
### Issues

If you run into some edge cases or a situation where this plugin conflicts with another one, you're invited to [create an issue](https://github.com/infolektuell/gradle-jpackage/issues/new) and describe your problem.

## Example Project

See the [example project](https://github.com/infolektuell/gradle-jpackage/tree/main/example) for minimal sample apps using this plugin.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)
