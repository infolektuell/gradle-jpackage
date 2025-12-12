package de.infolektuell.gradle.jpackage.tasks.modularity;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.jspecify.annotations.NonNull;

public non-sealed interface Modular extends Modularity {
    @Input
    Property<@NonNull String> getMainModule();
}
