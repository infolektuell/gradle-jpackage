# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
[unreleased]: https://github.com/infolektuell/gradle-jpackage/compare/v0.2.0...HEAD

## [0.2.0] - 2025-12-20
[0.2.0]: https://github.com/infolektuell/gradle-jpackage/compare/v0.1.0...v0.2.0

### Added

- Adds modular dependencies of non-modular apps to the run task from the Application plugin.
- Run task becomes compatible with configuration cache.

### Removed

- Pass-through CLI options in JpackageTask were replaced with documentation comments.

## [0.1.0] - 2025-12-13
[0.1.0]: https://github.com/infolektuell/gradle-jpackage/tags/v0.1.0

### Added

- Tasks for Jdeps, Jlink, and Jpackage.
- connects the Java and Application plugins with these tools and sets sensible conventions.
- Infers the module dependencies for non-modular apps using Jdeps.
- Configurable via DSL extension `jpackage`.
- Offers a `patchModule` source set extension for easier module patching, e.g., for Kotlin interop.
