package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.jspecify.annotations.NonNull;

public abstract class CommonHandler {
    /**
     * Additional directories to be added to the app payload
     */
    public abstract ConfigurableFileCollection getContent();
    public abstract Property<@NonNull Boolean> getIsCommandLineApplication();

    /**
     * Files to describe file associations
     */
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();

    public abstract DirectoryProperty getInstallDir();

    public abstract DirectoryProperty getResourceDir();

    public abstract Property<@NonNull String> getPackageName();

    public abstract Property<@NonNull Boolean> getShortcut();
}
