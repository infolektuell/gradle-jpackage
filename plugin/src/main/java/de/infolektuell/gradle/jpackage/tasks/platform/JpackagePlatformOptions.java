package de.infolektuell.gradle.jpackage.tasks.platform;

public sealed interface JpackagePlatformOptions permits JpackageLinuxOptions, JpackageMacOSOptions, JpackageWindowsOptions {
}
