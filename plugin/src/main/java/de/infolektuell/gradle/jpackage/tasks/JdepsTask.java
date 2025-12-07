package de.infolektuell.gradle.jpackage.tasks;

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

    @Input
    public abstract Property<@NonNull JavaLanguageVersion> getMultiRelease();

    @InputFile
    public abstract RegularFileProperty getMainJar();
    @OutputFile
    public abstract RegularFileProperty getDestinationFile();

    @TaskAction
    protected void jdeps() {
        try (var s = Files.newOutputStream(getDestinationFile().get().getAsFile().toPath())) {
            getExecOperations().exec(spec -> {
                spec.executable(getExecutable().get());
                spec.args("--print-module-deps");
                if (!getClassPath().isEmpty()) {
                    spec.args("--class-path", String.join(":", getClassPath().getFiles().stream().map(File::getAbsolutePath).toList()));
                }
                if (!getModulePath().isEmpty()) {
                    spec.args("--module-path", String.join(":", getModulePath().getFiles().stream().map(File::getAbsolutePath).toList()));
                }
                spec.args("--multi-release", getMultiRelease().get().asInt());
                spec.args(getMainJar().get().getAsFile().getAbsolutePath());
                spec.setStandardOutput(s);
            });
        } catch (Exception ignored) {
            throw new RuntimeException("Couldn't write jdeps result to destination file.");
        }
    }
}
