package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.Action;
import org.gradle.api.tasks.Nested;
import org.jspecify.annotations.NonNull;

/**
 * The Gradle DSL extension to configure the jpackage plugin
 */
public abstract class JpackageExtension {
    /**
     * The plugin uses this constant as extension name
     */
    public static final String EXTENSION_NAME = "jpackage";

    /**
     * General application metadata
     */
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

    /**
     * Options shared across all platforms (if applicable)
     */
    @Nested
    public abstract CommonHandler getCommon();

    public void common(Action<@NonNull CommonHandler> action) {
        action.execute(getCommon());
    }

    /**
     * Windows-specific options
     */
    @Nested
    public abstract WindowsHandler getWindows();

    public void windows(Action<@NonNull WindowsHandler> action) {
        action.execute(getWindows());
    }

    /**
     * Mac-specific options
     */
    @Nested
    public abstract MacHandler getMac();

    public void mac(Action<@NonNull MacHandler> action) {
        action.execute(getMac());
    }

    /**
     * Linux-specific options
     */
    @Nested
    public abstract LinuxHandler getLinux();

    public void linux(Action<@NonNull LinuxHandler> action) {
        action.execute(getLinux());
    }
}
