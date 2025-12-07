package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

@DisableCachingByDefault
public abstract class PrepareInputTask extends DefaultTask {
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getDestination();

    @TaskAction
    protected void prepare() {
        getFileSystemOperations().sync(spec -> {
            spec.from(getSource());
            spec.include("*.jar");
            spec.into(getDestination());
        });
    }
}
