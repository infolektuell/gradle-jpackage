package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class LinuxHandler {
    public enum InstallerType {
        deb, rpm
    }
    public abstract Property<@NonNull InstallerType> getInstallerType();
}
