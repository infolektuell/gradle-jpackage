package de.infolektuell.gradle.jpackage.tasks.modularity;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

public non-sealed interface NonModular extends Modularity {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getMainJar();
}
