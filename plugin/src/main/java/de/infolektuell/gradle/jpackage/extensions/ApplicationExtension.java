package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Nested;
import org.jspecify.annotations.NonNull;

public abstract class ApplicationExtension {
    public static final String EXTENSION_NAME = "application";

    @Nested
    public abstract MetadataHandler getMetadata();
    public void metadata(Action<@NonNull MetadataHandler> action) {
        action.execute(getMetadata());
    }

    @Nested
    public abstract LauncherHandler getLauncher();
    public void launcher(Action<@NonNull LauncherHandler> action) {
        action.execute(getLauncher());
    }

    @Nested
    public abstract WindowsHandler getWindows();
    public void windows(Action<@NonNull WindowsHandler> action) {
        action.execute(getWindows());
    }

    @Nested
    public abstract MacHandler getMac();
    public void mac(Action<@NonNull MacHandler> action) {
        action.execute(getMac());
    }

    @Nested
    public abstract LinuxHandler getLinux();
    public void linux(Action<@NonNull LinuxHandler> action) {
        action.execute(getLinux());
    }

    public abstract ConfigurableFileCollection getContent();
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();
    public abstract DirectoryProperty getInstallDir();
    public abstract DirectoryProperty getResourceDir();

    public Property<@NonNull String> getApplicationName() {
        return getMetadata().getName();
    }

    public Property<@NonNull String> getMainModule() {
        return getLauncher().getMainModule();
    }
    public Property<@NonNull String> getMainClass() {
        return getLauncher().getMainClass();
    }
    public ListProperty<@NonNull String> getApplicationDefaultJvmArgs() {
        return getLauncher().getJavaOptions();
    }
}
