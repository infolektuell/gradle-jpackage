package de.infolektuell.gradle.jpackage.toolchain;

import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;

public interface JlinkTool {
    @Nested
    JavaInstallationMetadata getMetadata();

    @Internal
    RegularFile getExecutablePath();
}
