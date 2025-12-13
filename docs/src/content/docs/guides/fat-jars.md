---
title: What about fat JARs and the Shadow plugin?
sidebar:
  order: 5
  label: Fat JARs
---

**You probably don't want Fat JARs.**

Fat JARs can be a reasonable solution for libraries.
They bundle the project's classes and all dependencies in one JAR file.
Jpackage is an alternative approach that is more suited for applications.
So this plugin doesn't support them out of the box, and you would have to reconfigure its tasks.

For many projects with much library-like code, the following strategy is advisable:

1. Move that code into a separate Gradle subproject next to the application subproject.
2. Apply plugins like [Java Library] and [Shadow] to the project to build the lib as a Fat JAR.
3. Add a dependency on that library subproject to the application's subproject.
4. Even better, modularize the lib, so Jlink will include it in the JRE and actually, the fat jar step is dismissable.

This is a main reason why Gradle projects should consist of subprojects from the beginning instead of using the root project.

[java library]: https://docs.gradle.org/current/userguide/java_library_plugin.html
[shadow]: https://gradleup.com/shadow/
