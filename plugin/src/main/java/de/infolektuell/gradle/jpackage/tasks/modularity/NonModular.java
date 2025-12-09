package de.infolektuell.gradle.jpackage.tasks.modularity;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.*;
import org.jspecify.annotations.NonNull;

public non-sealed interface NonModular extends Modularity {
    @Input
    Property<@NonNull String> getMainClass();
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getMainJar();
}
