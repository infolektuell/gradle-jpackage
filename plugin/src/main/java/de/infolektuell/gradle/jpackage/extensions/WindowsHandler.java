package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

public abstract class WindowsHandler {
    public enum InstallerType implements Serializable {
        EXE, MSI;
        public String toString() { return this.name().toLowerCase(); }
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public abstract Property<@NonNull InstallerType> getInstallerType();
    public abstract Property<@NonNull Boolean> getShortcut();
}
