---
title: How the build process works
sidebar:
  label: Background
  order: 3
---

Jpackage is the application packaging tool that comes with JDK 14 and above.
It creates native installers for self-contained Java-based desktop applications with their customized JRE.
So users don't need to install any JRE before using your apps.

Jlink is the tool that generates a custom downsized JRE and comes with JDK 11 and above.
This is possible since JDK 9 introduced the concept of modules (JPMS) and modularized the JDK itself.
The custom JRE includes only the modules used in the application code.

### How Jpackage works

The build process consists of several stages:

1. Compilation: The source code is compiled to bytecode and packed in a jar file. It can be a module but doesn't have to.
2. Runtime image: Jlink generates a custom JRE that contains only the necessary modules for your app. If your app is a module, it will be included as well in this runtime image.
3. Application image: Jpackage composes a standardized directory structure containing the generated runtime, application executable and other resources.
4. Packaging: Jpackage creates a platform-native compressed installer from the application image.

Jpackage combines these three stages in one CLI, so one could achieve everything with one single magic spell.
It runs Jlink implicitly and makes many implicit decisions.
In this plugin, these stages are more explicit and sequential to make the process clearer and more understandable.
Besides, splitting the process into multiple steps with intermediate inputs and outputs offers some advantages:

- Separation of stages offers more fine-grained customization.
- Because the app image can be created without packaging, developers can inspect their app without installation.
- It benefits more from Gradle's caching capabilities.
- The build becomes quicker, especially if the installer packaging step can often be omitted in the development process.
