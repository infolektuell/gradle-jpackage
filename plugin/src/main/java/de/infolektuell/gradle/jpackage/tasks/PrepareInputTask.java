package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

@DisableCachingByDefault
public abstract class PrepareInputTask extends DefaultTask {
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @InputFile
    public abstract RegularFileProperty getMainJar();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getSource();

    @Optional
    @Input
    public abstract Property<@NonNull String> getModule();

    @OutputDirectory
    public abstract DirectoryProperty getDestination();

    @TaskAction
    protected void prepare() {
        getFileSystemOperations().sync(spec -> {
            spec.from(getSource());
            if (!getModule().isPresent()) spec.from(getMainJar());
            spec.include("*.jar");
            spec.into(getDestination());
        });
    }
}
