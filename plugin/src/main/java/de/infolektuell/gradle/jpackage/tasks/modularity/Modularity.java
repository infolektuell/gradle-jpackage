package de.infolektuell.gradle.jpackage.tasks.modularity;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.jspecify.annotations.NonNull;

public sealed interface Modularity permits Modular, NonModular {
    @Input
    Property<@NonNull String> getMainClass();
}
