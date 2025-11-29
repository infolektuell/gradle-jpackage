package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jspecify.annotations.NonNull;

import static de.infolektuell.gradle.jpackage.tasks.ApplicationImageTask.JpackageAppMetadata;

/**
 * The DSL extension to configure the jpackage plugin
 */
public abstract class JpackageExtension {
    /**
     * How the extension can be accessed in the build script
     */
    public static final String EXTENSION_NAME = "jpackage";

    public abstract Property<@NonNull JavaToolchainSpec> getToolchain();
    public abstract Property<@NonNull String> getApplicationName();

    @Nested
    public abstract JpackageAppMetadata getMetadata();

    public void metadata(Action<@NonNull JpackageAppMetadata> action) {
        action.execute(getMetadata());
    }

    @Nested
    public abstract RuntimeHandler getRuntime();

    public void runtime(Action<@NonNull RuntimeHandler> action) {
        action.execute(getRuntime());
    }

    public abstract ListProperty<@NonNull RegularFile> getArgFiles();
}
