package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class MacHandler {
    public enum InstallerType {
        dmg, pkg
    }
    public abstract Property<@NonNull InstallerType> getInstallerType();

    public abstract Property<@NonNull String> getAppCategory();
    public abstract Property<@NonNull String> getPackageName();
    public abstract Property<@NonNull String> getPackageID();

    public abstract DirectoryProperty getDmgContent();
}
