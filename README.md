# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin creates native installers for Java-based applications.
It integrates with the Java plugin and with the application plugin if it is applied.

## Documentation

See the [documentation site](https://infolektuell.github.io/gradle-jpackage/) for more in-depth information and usage guides.

## Quick Start

### Configuration

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

### Usage

The plugin adds some new tasks to the project.
These are not triggered by the `build` task, because building apps is quite time-consuming.

- `findModuleDeps` (per source set): Searches for module dependencies required by the source code. If the project is non-modular, the plugin retrieves the modules for the runtime image from this task. In a modular project, this task is skipped. You can run it manually to inspect your code and get a storting point for modularizing your app. The result is written to `build/jdeps/<source set>/jdeps-result.txt`.
- `generateRuntimeImage` (per source set): Generates a custom runtime image that consists of all modules required by the application using jlink.
- `appImage` (main source set): Assembles an app image from the runtime image, non-modular jars and other app-specific assets using jpackage. An app image is a jpackage-specific directory structure which is the foundation for generating different app installers. It is put under `build/jpackage/image` and can be executed without installation. On macOS, this is a `.app` directory which is recognized as an application. This stage is nice for testing, because it runs reliably on all platforms.
- `appInstaller` (main source set): Creates an app installer package from the generated app image for distribution, e.g., exe, msi, dmg, pkg, deb, or rpm. It is put under `build/jpackage/installer`. This could fail due to missing tooling or misconfigured environment on the build system.

Cross-compilation is not possible with jpackage, so on a Windows build system, a Windows app is built, on macOS a Mac app and so on.

## Example Project

See the [example project](https://github.com/infolektuell/gradle-jpackage/tree/main/example) for minimal sample apps using this plugin.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## Issues

If you run into some edge cases or a situation where this plugin conflicts with another one, you're invited to [create an issue](https://github.com/infolektuell/gradle-jpackage/issues/new) and describe your problem.

## License

[MIT License](LICENSE.txt)
