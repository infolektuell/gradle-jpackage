package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Optional
    @Input
    @Option(option = "print-modules", description = "Print the Jdeps result")
    public abstract Property<@NonNull Boolean> getPrintModules();

    @OutputFile
    public abstract RegularFileProperty getDestinationFile();

    @TaskAction
    protected void jdeps() {
        Path dest = getDestinationFile().get().getAsFile().toPath();
        try (var s = Files.newOutputStream(dest)) {
            getExecOperations().exec(spec -> {
                spec.executable(getExecutable().get());
                if (!getClassPath().isEmpty()) {
                    spec.args("--class-path", getClassPath().getAsPath());
                }
                if (!getModulePath().isEmpty()) {
                    spec.args("--module-path", getModulePath().getAsPath());
                }
                if (getRecursive().getOrElse(false)) spec.args("--recursive");
                if (getPrintModuleDeps().getOrElse(false)) spec.args("--print-module-deps");
                if (getIgnoreMissingDeps().getOrElse(false)) spec.args("--ignore-missing-deps");
                if (getMultiRelease().isPresent()) spec.args("--multi-release", getMultiRelease().get().asInt());
                switch (getModularity().get()) {
                    case Modular modular -> spec.args("--module", modular.getMainModule().get());
                    case NonModular nonModular -> spec.args(nonModular.getMainJar().get());
                }
                spec.setStandardOutput(s);
            });
        } catch (Exception ignored) {
            throw new RuntimeException("Couldn't write jdeps result to destination file.");
        }
        if (getPrintModules().getOrElse(false)) {
            try {
                String result = Files.readString(dest);
                System.out.println(result);
            } catch(Exception ignored) {}
        }
    }
}
