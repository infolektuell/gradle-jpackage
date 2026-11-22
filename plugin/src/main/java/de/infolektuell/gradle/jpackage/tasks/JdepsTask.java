package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.toolchain.JdepsTool;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;

@CacheableTask
public abstract class JdepsTask extends DefaultTask {
    @Nested
    public abstract Property<JdepsTool> getJdepsTool();

    /// The class files to be analyzed (entries can be .class files, .jar files, or directories)
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getSource();

    /// Specify where to find class files
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getClassPath();

    /// Specify where to find module files
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getModulePath();

    /// Specify upgrade module path
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getUpgradeModulePath();

    /// Specify the root module for analysis
    @Optional
    @Input
    public abstract Property<@NonNull String> getModule();

    /// Recursively traverse all run-time dependencies.
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getRecursive();

    /// Same as --list-reduced-deps with printing,   a comma-separated list of module dependencies.
    /// This output can be used by jlink --add-modules in order to create a custom image containing those modules and their transitive dependencies.
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

    @Input
    public abstract ListProperty<String> getArguments();

    @Nested
    public abstract ListProperty<@NonNull CommandLineArgumentProvider> getArgumentProviders();

    @Internal
    public Provider<@NonNull String> getResult() {
        return getDestinationFile().map(f -> {
            try {
                final var text = Files.readString(f.getAsFile().toPath());
                return text.trim();
            } catch (Exception ignored) {
                return "ALL_MODULE_PATH";
            }
        });
    }

    @TaskAction
    protected void jdeps() {
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        final var result = getExecOperations().exec(spec -> {
            spec.executable(getJdepsTool().get().getExecutablePath());
            if (!getClassPath().isEmpty()) spec.args("--class-path", getClassPath().getAsPath());
            if (!getModulePath().isEmpty()) spec.args("--module-path", getModulePath().getAsPath());
            if (!getUpgradeModulePath().isEmpty())
                spec.args("--upgrade-module-path", getUpgradeModulePath().getAsPath());
            if (getRecursive().getOrElse(false)) spec.args("--recursive");
            if (getPrintModuleDeps().getOrElse(false)) spec.args("--print-module-deps");
            if (getIgnoreMissingDeps().getOrElse(false)) spec.args("--ignore-missing-deps");
            if (getMultiRelease().isPresent()) spec.args("--multi-release", getMultiRelease().get().asInt());
            if (getArguments().isPresent()) spec.args(getArguments().get());
            spec.getArgumentProviders().addAll(getArgumentProviders().get());
            if (getModule().isPresent()) spec.args("--module", getModule().get());
            else getSource().forEach(spec::args);

            spec.setStandardOutput(s);
        });
        final String text = s.toString().trim();
        if (result.getExitValue() != 0 || !isModuleNameList(text)) {
            throw new RuntimeException(text);
        } else {
            try {
                getLogger().info("Jdeps found these module dependencies: {}", text);
                Files.writeString(getDestinationFile().get().getAsFile().toPath(), text);
            } catch (Exception ignored) {}
        }
    }

    @Inject
    protected abstract ExecOperations getExecOperations();

    private final Pattern moduleNamePattern = Pattern.compile("^[a-z]+(\\.[a-z]+)+(,[a-z]+(\\.[a-z]+)+)*$");

    private boolean isModuleNameList(String text) {
        return moduleNamePattern.matcher(text).find();
    }
}
