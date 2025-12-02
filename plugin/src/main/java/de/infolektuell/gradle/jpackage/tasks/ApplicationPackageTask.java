package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.jspecify.annotations.NonNull;

@CacheableTask
public abstract class ApplicationPackageTask extends PackageTask {
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getApplicationImage();

    @Nested
    public abstract NamedDomainObjectSet<@NonNull Launcher> getAdditionalLaunchers();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getLauncherAsService();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();

    @TaskAction
    protected void run() {
        exec(spec -> {
            getApplicationImage().get().getAsFileTree().visit(f -> {
                if (f.isDirectory()) {
                    spec.args("--app-image", f.getFile());
                    f.stopVisiting();
                }
            });

            // Launchers
            if (getLauncherAsService().getOrElse(false)) spec.args("--launcher-as-service");
            getAdditionalLaunchers().forEach(launcher -> spec.args("--add-launcher", String.join("=", launcher.getName(), launcher.getFile().get().getAsFile().getAbsolutePath())));
            getFileAssociations().get().forEach(f -> spec.args("--file-associations", f));
        });
    }
}
