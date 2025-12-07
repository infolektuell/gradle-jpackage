package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;

public abstract class JdepsTask extends DefaultTask {
    @Inject
    protected abstract ExecOperations getExecOperations();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getExecutable();

    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getClassPath();

    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getModulePath();

    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getRecursive();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getPrintModuleDeps();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getIgnoreMissingDeps();

    @Optional
    @Input
    public abstract Property<@NonNull JavaLanguageVersion> getMultiRelease();

    @OutputFile
    public abstract RegularFileProperty getDestinationFile();

    @TaskAction
    protected void jdeps() {
        try (var s = Files.newOutputStream(getDestinationFile().get().getAsFile().toPath())) {
            getExecOperations().exec(spec -> {
                spec.executable(getExecutable().get());
                if (!getClassPath().isEmpty()) {
                    spec.args("--class-path", String.join(":", getClassPath().getFiles().stream().map(File::getAbsolutePath).toList()));
                }
                if (!getModulePath().isEmpty()) {
                    spec.args("--module-path", String.join(":", getModulePath().getFiles().stream().map(File::getAbsolutePath).toList()));
                }
                if (getRecursive().getOrElse(false)) spec.args("--recursive");
                if (getPrintModuleDeps().getOrElse(false)) spec.args("--print-module-deps");
                if (getIgnoreMissingDeps().getOrElse(false)) spec.args("--ignore-missing-deps");
                if (getMultiRelease().isPresent()) spec.args("--multi-release", getMultiRelease().get().asInt());
                switch (getModularity().get()) {
                    case Modular modular -> spec.args("--module", modular.getMainModule().get());
                    case NonModular nonModular -> {
                        spec.args(nonModular.getMainJar().get().getAsFile().getAbsolutePath());
                        spec.setStandardOutput(s);
                    }
                }
            });
        } catch (Exception ignored) {
            throw new RuntimeException("Couldn't write jdeps result to destination file.");
        }
    }
}
