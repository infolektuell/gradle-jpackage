package de.infolektuell.gradle.jpackage.extensions;

import de.infolektuell.gradle.jpackage.tasks.providers.PatchModuleProvider;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.jspecify.annotations.NonNull;

public abstract class SourceSetExtension {
    public static final String EXTEnSION_NAME = "jpackage";

    @Input
    public abstract Property<@NonNull Boolean> getInferModulePath();

    @Nested
    public abstract PatchModuleProvider getPatchModule();
}
