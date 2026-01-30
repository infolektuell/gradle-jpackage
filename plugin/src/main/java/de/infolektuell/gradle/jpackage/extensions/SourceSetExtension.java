package de.infolektuell.gradle.jpackage.extensions;

import de.infolektuell.gradle.jpackage.tasks.providers.ModulePathProvider;
import de.infolektuell.gradle.jpackage.tasks.providers.PatchModuleProvider;
import org.gradle.api.tasks.Nested;

public abstract class SourceSetExtension {
    public static final String EXTEnSION_NAME = "jpackage";

    @Nested
    public abstract ModulePathProvider getModulePath();

    @Nested
    public abstract PatchModuleProvider getPatchModule();
}
