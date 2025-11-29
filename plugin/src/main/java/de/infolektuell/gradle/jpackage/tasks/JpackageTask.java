package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

public abstract class JpackageTask extends DefaultTask {
    @Inject
    protected abstract ExecOperations getExecOperations();
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    /**
     * The jpackage executable to run
     */
    @InputFile
    public abstract RegularFileProperty getExecutable();

    @Optional
    @Input
    public abstract Property<@NonNull String> getApplicationName();

    /**
     * Arg files that contain additional options to be passed to jpackage
     */
    @InputFiles
    public abstract ListProperty<@NonNull RegularFile> getArgFiles();

    @OutputDirectory
    public abstract DirectoryProperty getDest();

    protected void exec(Action<@NonNull ExecSpec> action) {
        getFileSystemOperations().delete(spec -> spec.delete(getDest()));

        getExecOperations().exec(spec -> {
            spec.executable(getExecutable().get());
            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));
            System.out.println(getApplicationName().get());
            if (getApplicationName().isPresent()) spec.args("--name", getApplicationName().get());
            if (getDest().isPresent()) spec.args("--dest", getDest().get());
            action.execute(spec);
        });
    }
}
