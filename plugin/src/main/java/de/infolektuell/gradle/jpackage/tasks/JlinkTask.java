package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.toolchain.JlinkTool;
import java.io.ByteArrayOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;

@CacheableTask
public abstract class JlinkTask extends DefaultTask {
    public enum Endian implements Serializable {
        BIG, LITTLE;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Nested
    public abstract Property<JlinkTool> getJlinkTool();

    @Input
    public abstract ListProperty<@NonNull String> getAddModules();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getBindServices();

    @Optional
    @Input
    public abstract Property<@NonNull Integer> getCompress();

    @Optional
    @Input
    public abstract Property<@NonNull String> getDisablePlugin();

    @Optional
    @Input
    public abstract Property<@NonNull Endian> getEndian();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getIgnoreSigningInformation();

    @Optional
    @Input
    public abstract Property<@NonNull String> getLauncher();

    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getLimitModules();

    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getModulePath();

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

    @Input
    public abstract ListProperty<String> getArguments();

    @Nested
    public abstract ListProperty<CommandLineArgumentProvider> getArgumentProviders();

    @OutputDirectory
    public abstract DirectoryProperty getOutput();

    @TaskAction
    protected void run() {
        getFileSystemOperations().delete(spec -> spec.delete(getOutput()));
        final var out = new ByteArrayOutputStream();
        final var result = getExecOperations().exec(spec -> {
            spec.executable(getJlinkTool().get().getExecutablePath());
            spec.args("--add-modules", String.join(",", getAddModules().get()));
            if (getBindServices().getOrElse(false)) spec.args("--bind-services");
            if (getCompress().isPresent()) {
                var level = Integer.max(Integer.min(getCompress().get(), 9), 0);
                spec.args("--compress", String.format("zip-%d", level));
            }
            if (getDisablePlugin().isPresent()) spec.args("--disable-plugin", getDisablePlugin().get());
            if (getEndian().isPresent()) spec.args("--endian", getEndian().get().name().toLowerCase());
            if (getIgnoreSigningInformation().getOrElse(false)) spec.args("--ignore-signing-information");
            if (getLauncher().isPresent()) spec.args("--launcher", getLauncher().get());
            if (getLimitModules().getOrElse(false)) spec.args("--limit-modules");
            if (!getModulePath().isEmpty()) spec.args("--module-path", getModulePath().getAsPath());
            if (getNoHeaderFiles().getOrElse(false)) spec.args("--no-header-files");
            if (getNoManPages().getOrElse(false)) spec.args("--no-man-pages");
            if (getStripDebug().getOrElse(false)) spec.args("--strip-debug");
            if (getStripNativeCommands().getOrElse(false)) spec.args("--strip-native-commands");
            spec.args("--output", getOutput().get());
            if (getArguments().isPresent()) spec.args(getArguments().get());
            spec.getArgumentProviders().addAll(getArgumentProviders().get());

            if (getLogger().isInfoEnabled()) spec.args("--verbose");
            spec.setIgnoreExitValue(true);
            spec.setStandardOutput(out);
        });
        if (result.getExitValue() != 0) {
            throw new RuntimeException(out.toString().trim());
        }
    }

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();
}
