---
title: Without Application plugin
sidebar:
  order: 6
  label: No Application Plugin
---

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
