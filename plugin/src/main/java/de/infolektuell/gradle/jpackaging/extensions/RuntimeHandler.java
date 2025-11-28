package de.infolektuell.gradle.jpackaging.extensions;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class RuntimeHandler {
    public abstract ListProperty<@NonNull String> getModules();
    public abstract Property<@NonNull Boolean> getJpackage();
}
