package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ApplicationExtension {
    public static final String EXTENSION_NAME = "application";

    public abstract Property<@NonNull String> getApplicationName();
    public abstract Property<@NonNull String> getMainModule();
    public abstract Property<@NonNull String> getMainClass();
}
