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

/**
 * The Gradle DSL extension to configure the jpackage plugin
 */
public abstract class ApplicationExtension {
    /** The plugin uses this constant as extension name */
    public static final String EXTENSION_NAME = "application";

    /** General application metadata */
    @Nested
    public abstract MetadataHandler getMetadata();
    public void metadata(Action<@NonNull MetadataHandler> action) {
        action.execute(getMetadata());
    }

    /**
     * These options are used for creating the application launcher.
     */
    @Nested
    public abstract LauncherHandler getLauncher();
    public void launcher(Action<@NonNull LauncherHandler> action) {
        action.execute(getLauncher());
    }

    /** Windows-specific options */
    @Nested
    public abstract WindowsHandler getWindows();
    public void windows(Action<@NonNull WindowsHandler> action) {
        action.execute(getWindows());
    }

    /** Mac-specific options */
    @Nested
    public abstract MacHandler getMac();
    public void mac(Action<@NonNull MacHandler> action) {
        action.execute(getMac());
    }

    /** Linux-specific options */
    @Nested
    public abstract LinuxHandler getLinux();
    public void linux(Action<@NonNull LinuxHandler> action) {
        action.execute(getLinux());
    }

    /** Additional directories to be added to the app payload */
    public abstract ConfigurableFileCollection getContent();
    /** Files to describe file associations */
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();
    public abstract DirectoryProperty getInstallDir();
    public abstract DirectoryProperty getResourceDir();

    /**
     * The application name.
     * @return Forwards to `getMetadata().getName()`.
     * @deprecated This exists for compatibility with the application plugin.
     */
    @Deprecated
    public Property<@NonNull String> getApplicationName() {
        return getMetadata().getName();
    }

    /**
     * The main module of the application.
     * @return Forwards to `getLauncher().getMainModule()`.
     * @deprecated This exists for compatibility with the application plugin.
     */
    @Deprecated
    public Property<@NonNull String> getMainModule() {
        return getLauncher().getMainModule();
    }

    /**
     * The main class of the application.
     * @return Forwards to `getLauncher().getMainClass()`.
     * @deprecated This exists for compatibility with the application plugin.
     */
    @Deprecated
    public Property<@NonNull String> getMainClass() {
        return getLauncher().getMainClass();
    }

    /**
     * Additional JVM options for the Java launcher
     * @return Forwards to `getLauncher().getJavaOptions()`.
     * @deprecated This exists for compatibility with the application plugin.
     */
    @Deprecated
    public ListProperty<@NonNull String> getApplicationDefaultJvmArgs() {
        return getLauncher().getJavaOptions();
    }
}
