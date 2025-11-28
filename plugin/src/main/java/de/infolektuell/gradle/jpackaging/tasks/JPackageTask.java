package de.infolektuell.gradle.jpackaging.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/** Runs jpackage tool with given options */
public abstract class JPackageTask extends DefaultTask {
    private final ExecOperations execOperations;

    /** The jpackage executable to run */
    @InputFile
    @NotNull
    public abstract RegularFileProperty getExecutable();

    /** Arg files that contain additional options to be passed to jpackage */
    @InputFiles
    @NotNull
    public abstract ListProperty<@NotNull RegularFile> getArgFiles();

    @Inject
    public JPackageTask(
        ExecOperations execOperations
    ) {
        this.execOperations = execOperations;
    }

    @TaskAction
    protected void run() {
        execOperations.exec(spec -> {
            spec.executable(getExecutable().get());
            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));
        });
    }
}
