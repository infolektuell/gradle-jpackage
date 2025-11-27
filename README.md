# Gradle Java Packaging Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.java-packaging)](https://plugins.gradle.org/plugin/de.infolektuell.java-packaging)

This Gradle plugin creates native installers for Java applications using jpackage and jlink. 

## Quick Start

```kts
plugins {
    application
    id("de.infolektuell.java-packaging") version "x.y.z"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

Please visit the [Setup guide] on the documentation website for more details.

## Change history

See GitHub Releases or the [changelog file](CHANGELOG.md) for releases and changes.

## License

[MIT License](LICENSE.txt)

[setup guide]: https://infolektuell.github.io/gradle-java-packaging/start/setup/
