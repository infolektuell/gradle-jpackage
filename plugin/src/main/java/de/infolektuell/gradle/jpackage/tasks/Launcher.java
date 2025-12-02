package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.Named;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

public interface Launcher extends Named {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getFile();
}
