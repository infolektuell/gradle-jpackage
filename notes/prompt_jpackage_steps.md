# Jpackage options per step

Jpackage is able to perform four build steps in sequence.

1. Creating a runtime image using Jlink
2. Creating an application image, the application's file structure with executables and assets
3. Signing the application on macOS
4. Packaging the image as an installer

Instead of running Jpackage to perform all steps at once, Jpackage is run separately for each step.

- Jlink creates a runtime image containing all used Java modules.
- Jpackage creates an app image using the created runtime image and app-specific files and settings.
- Jpackage optionally signs the created app image with given credentials.
- Jpackage packages the signed app image as an os-specific installer.

For each step, list the options for the Jpackage CLI tool that are considered in that build step.
