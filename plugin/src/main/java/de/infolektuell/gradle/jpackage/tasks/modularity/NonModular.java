package de.infolektuell.gradle.jpackage.tasks.modularity;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.jspecify.annotations.NonNull;

public non-sealed interface NonModular extends Modularity {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getMainJar();

    @Input
    Property<@NonNull String> getMainClass();
}
