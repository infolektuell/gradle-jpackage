package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

@DisableCachingByDefault
public abstract class PrepareInputTask extends DefaultTask {
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getSource();

    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @OutputDirectory
    public abstract DirectoryProperty getDestination();

    @TaskAction
    protected void prepare() {
        getFileSystemOperations().sync(spec -> {
            switch (getModularity().get()) {
                case Modular ignored -> spec.from(getSource());
                case NonModular nonModular -> spec.from(nonModular.getMainJar(), getSource());
            }

            spec.include("*.jar");
            spec.into(getDestination());
        });
    }
}
