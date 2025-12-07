# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- Offers low-level tasks for Jlink, Jdeps, and Jpackage.
- Offers a DSL extension to configure app metadata and jpackage settings.
- Platform-specific settings can be configured as well, e.g., the installer type for each platform.
- The plugin registers and configures the tasks using the DSL extensions from Java, Application, and this plugin.
  - The plugin uses the `application.mainModule` property to decide whether the app is modular or nonmodular.
  - Module dependencies of nonmodular apps are inferred using the Jdeps tool.
  - Nonmodular jar files (main and dependencies) are added to the app. Modules are included in the runtime image.
  - The Java toolchain configured in the Java plugin is used by default, but can be customized.
  - The classpath and module path where the dependencies can be found are inferred from the main source set's runtime classpath.
- Every packaging step has its own task with their inputs and outputs. Each intermediate task can be executed without having to run the complete chain.
  - Analyzing module dependencies (for nonmodular apps)
  - Creating the runtime image with the requested modules
  - Creating the app image that contains the created runtime image
  - Creating the app installer from the created app image
