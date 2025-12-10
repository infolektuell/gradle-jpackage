package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jspecify.annotations.NonNull;

public non-sealed interface JpackageLinuxOptions extends JpackagePlatformOptions {
    @Optional
    @Input
    Property<@NonNull String> getLinuxAppCategory();

    @Optional
    @Input
    Property<@NonNull Boolean> getLinuxShortcut();
}
