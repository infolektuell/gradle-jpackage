package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

public non-sealed interface JpackageLinuxOptions extends JpackagePlatformOptions {
    enum InstallerType implements Serializable {
        DEB, RPM;
        public String toString() { return this.name().toLowerCase(); }
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Optional
    @Input
    Property<@NonNull InstallerType> getType();

    @Optional
    @Input
    Property<@NonNull String> getLinuxAppCategory();

    @Optional
    @Input
    Property<@NonNull Boolean> getLinuxShortcut();
}
