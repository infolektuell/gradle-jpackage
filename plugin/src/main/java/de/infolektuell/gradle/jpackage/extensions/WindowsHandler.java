package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class WindowsHandler {
    public enum InstallerType {
        exe, msi
    }
    public abstract Property<@NonNull InstallerType> getInstallerType();
}
