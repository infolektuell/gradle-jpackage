package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;

public abstract class RunTask extends DefaultTask {
    @Inject
    protected abstract ExecOperations getExecOperations();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getExecutable();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getModulePath();

    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @TaskAction
    protected void run() {
        getExecOperations().javaexec(spec -> {
            if (!getClasspath().isEmpty()) spec.setClasspath(getClasspath());
            if (!getModulePath().isEmpty()) {
                spec.jvmArgs("--module-path", String.join(":", getModulePath().getFiles().stream().map(File::getAbsolutePath).toList()));
            }
            switch (getModularity().get()) {
                case Modular modular -> {
                    spec.getMainModule().convention(modular.getMainModule());
                    // spec.getMainClass().convention(modular.getMainClass());
                }
                case NonModular nonModular -> {
                    spec.getMainClass().convention(nonModular.getMainClass());
                    spec.jvmArgs("-jar", nonModular.getMainJar().get().getAsFile().getAbsolutePath());
                }
            }
        });
    }
}
