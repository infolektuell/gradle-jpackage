---
title: Build anatomy
sidebar:
  order: 0
---

Turning a Java application into a native installer involves several build steps, each of them with there intermediate build result.
this documentation section explains these build steps in more detail, their concept and outputs, which tools they rely on, and how the plugin manages these steps and where the outputs are stored.

In general, the process relies on CLI tools that are part of the JDK.
This plugin just integrates them into a Gradle build in a more structured and organized way.
The CLI tools probably were designed to achieve the desired output by issuing one giant shell command.
But this makes the process quite intransparent and makes it difficult to understand what happens.
The plugin makes the steps more explicit and separated, which facilitates comprehension and caching.
