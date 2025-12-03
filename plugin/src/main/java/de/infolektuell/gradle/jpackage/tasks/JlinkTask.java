package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;
import java.io.Serial;
import java.io.Serializable;

@CacheableTask
public abstract class JlinkTask extends DefaultTask {
    public enum Endian implements Serializable {
        BIG, LITTLE;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Inject
    protected abstract ExecOperations getExecOperations();
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    /**
     * The jpackage executable to run
     */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getExecutable();

    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getModulePath();

    @Input
    public abstract ListProperty<@NonNull String> getAddModules();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getBindServices();

    @Optional
    @Input
    public abstract Property<@NonNull Endian> getEndian();

    @Optional
    @Input
    public abstract Property<@NonNull Integer> getCompress();

    @Optional
    @Input
    public abstract Property<@NonNull String> getDisablePlugin();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getNoHeaderFiles();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getNoManPages();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getStripDebug();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getStripNativeCommands();

    @OutputDirectory
    public abstract DirectoryProperty getOutput();

    @TaskAction
    protected void run() {
        getFileSystemOperations().delete(spec -> spec.delete(getOutput()));
        getExecOperations().exec(spec -> {
            spec.executable(getExecutable().get());
            if (!getModulePath().isEmpty()) {
                spec.args("--module-path", String.join(":", getModulePath().getFiles().stream().map(File::getAbsolutePath).toList()));
            }
            spec.args("--add-modules", String.join(",", getAddModules().get()));
            if (getBindServices().getOrElse(false)) spec.args("--bind-services");
            if (getCompress().isPresent()) {
                var level = Integer.max(Integer.min(getCompress().get(), 9), 0);
                spec.args("--compress", String.format("zip-%d", level));
            }
            if (getDisablePlugin().isPresent()) {
                spec.args("--disable-plugin", getDisablePlugin().get());
            }
            if (getEndian().isPresent()) {
                spec.args("--endian", getEndian().get().name().toLowerCase());
            }
            if (getNoHeaderFiles().getOrElse(false)) spec.args("--no-header-files");
            if (getNoManPages().getOrElse(false)) spec.args("--no-man-pages");
            if (getStripDebug().getOrElse(false)) spec.args("--strip-debug");
            if (getStripNativeCommands().getOrElse(false)) spec.args("--strip-native-commands");
            spec.args("--output", getOutput().get());
        });
    }
}
