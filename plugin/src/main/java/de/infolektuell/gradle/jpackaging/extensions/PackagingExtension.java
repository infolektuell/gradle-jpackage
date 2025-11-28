package de.infolektuell.gradle.jpackaging.extensions;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ListProperty;
import org.jspecify.annotations.*;

/** The DSL extension to configure the Java packaging plugin */
public abstract class PackagingExtension {
    /** How the extension can be accessed in the build script */
    public static final String EXTENSION_NAME = "packaging";
    public abstract Property<@NonNull JavaToolchainSpec> getToolchain();

    @Nested
    public abstract RuntimeHandler getRuntime();

    public void runtime(Action<@NonNull RuntimeHandler> action) {
        action.execute(getRuntime());
    }
    public abstract ListProperty<@NonNull RegularFile> getArgFiles();
}
