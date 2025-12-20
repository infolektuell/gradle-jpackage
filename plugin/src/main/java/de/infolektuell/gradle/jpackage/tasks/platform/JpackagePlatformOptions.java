package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.process.CommandLineArgumentProvider;

public sealed interface JpackagePlatformOptions extends CommandLineArgumentProvider permits JpackageLinuxOptions, JpackageMacOSOptions, JpackageWindowsOptions {
}
