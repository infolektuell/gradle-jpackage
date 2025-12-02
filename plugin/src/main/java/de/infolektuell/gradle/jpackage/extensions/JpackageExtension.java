package de.infolektuell.gradle.jpackage.extensions;

import de.infolektuell.gradle.jpackage.tasks.Launcher;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jspecify.annotations.NonNull;

/**
 * The DSL extension to configure the jpackage plugin
 */
public abstract class JpackageExtension {
    /**
     * How the extension can be accessed in the build script
     */
    public static final String EXTENSION_NAME = "jpackage";

    public abstract Property<@NonNull JavaToolchainSpec> getToolchain();

    @Nested
    public abstract MetadataHandler getMetadata();
    public void metadata(Action<@NonNull MetadataHandler> action) {
        action.execute(getMetadata());
    }

    @Nested
    public abstract RuntimeHandler getRuntime();
    public void runtime(Action<@NonNull RuntimeHandler> action) {
        action.execute(getRuntime());
    }

    @Nested
    public abstract ImageHandler getImage();
    public void image(Action<@NonNull ImageHandler> action) {
        action.execute(getImage());
    }

    @Nested
    public abstract InstallerHandler getInstaller();
    void installer(Action<@NonNull InstallerHandler> action) {
        action.execute(getInstaller());
    }

    public abstract ListProperty<@NonNull RegularFile> getArgFiles();

    public abstract NamedDomainObjectContainer<@NonNull Launcher> getLaunchers();
    public void addLauncher(String name, RegularFile file) {
        getLaunchers().register(name, l -> l.getFile().convention(file));
    }
    public void addLauncher(String name, Provider<@NonNull RegularFile> file) {
        getLaunchers().register(name, l -> l.getFile().convention(file));
    }
}
