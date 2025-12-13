# Gradle Jpackage Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.jpackage)](https://plugins.gradle.org/plugin/de.infolektuell.jpackage)

This Gradle plugin creates native installers for Java-based applications.
It integrates with the Java plugin and with the application plugin if it is applied.

## Documentation

See the [documentation site](https://infolektuell.github.io/gradle-jpackage/) for more in-depth information and usage guides.

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

## Example Project

See the [example project](https://github.com/infolektuell/gradle-jpackage/tree/main/example) for minimal sample apps using this plugin.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## Issues

If you run into some edge cases or a situation where this plugin conflicts with another one, you're invited to [create an issue](https://github.com/infolektuell/gradle-jpackage/issues/new) and describe your problem.

## License

[MIT License](LICENSE.txt)
