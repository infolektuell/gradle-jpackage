package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.types.*;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.jspecify.annotations.NonNull;

/**
 * Generates an application image using Jpackage
 */
@CacheableTask
public abstract class ApplicationImageTask extends JpackageTask {
    public sealed interface Modularity permits Modular, NonModular {}
    public non-sealed interface Modular extends Modularity {
        @Input
        Property<@NonNull String> getModule();
    }
    public non-sealed interface NonModular extends Modularity {
        @InputFile
        @PathSensitive(PathSensitivity.RELATIVE)
        RegularFileProperty getMainJar();

        @Input
        Property<@NonNull String> getMainClass();
    }
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInput();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getAppContent();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @Input
    public abstract ListProperty<@NonNull String> getArguments();

    @Input
    public abstract ListProperty<@NonNull String> getJavaOptions();

    @Nested
    public abstract NamedDomainObjectSet<@NonNull Launcher> getAdditionalLaunchers();

    @TaskAction
    protected void run() {
        exec(spec -> {
            // Content
            spec.args("--type", "app-image");
            spec.args("--input", getInput().get());
            getAppContent().forEach(f -> spec.args("--app-content", f.getAbsolutePath()));

            // Launchers
            switch (getModularity().get()) {
                case Modular modular -> spec.args("--module", modular.getModule().get());
                case NonModular nonModular -> {
                    spec.args("--main-jar", nonModular.getMainJar().get().getAsFile().getName());
                    spec.args("--main-class", nonModular.getMainClass().get());
                }
            }
            getArguments().get().forEach(a -> spec.args("--arguments", a));
            getJavaOptions().get().forEach(a -> spec.args("--java-options", a));
            getAdditionalLaunchers().forEach(launcher -> spec.args("--add-launcher", String.join("=", launcher.getName(), launcher.getFile().get().getAsFile().getAbsolutePath())));

            spec.args("--runtime-image", getRuntimeImage().get());
        });
    }
}
