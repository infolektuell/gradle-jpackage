# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- The plugin relies on the Java plugin and its configuration to configure itself with sensible defaults.
- Offers a DSL extension to configure app metadata and jpackage settings.
- Platform-specific settings can be configured as well, e.g., the installer type for each platform.
- Offers low-level tasks for Jlink, Jdeps, and Jpackage.
- Every packaging step has its own task with their inputs and outputs. Each intermediate task can be executed without having to run the complete chain.
  - Analyzing module dependencies using Jdeps (for nonmodular apps)
  - Creating the runtime image with the requested modules using Jlink
  - Creating the app image that contains the created runtime image using Jpackage
  - Creating the app installer from the created app image using Jpackage
- The plugin is a drop-in replacement for the application plugin.
  - It creates its own application extension that consistently uses lazy properties. Users shouldn't remark any difference in their build script.
  - It registers its own run task which is compatible with configuration cache and uses Jdeps for more reliable module detection.
