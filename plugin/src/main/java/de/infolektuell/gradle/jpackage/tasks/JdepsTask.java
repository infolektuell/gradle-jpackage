package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.process.ExecResult;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CacheableTask
public abstract class JdepsTask extends JDKToolTask {
    /**
     * Specify where to find class files
     */
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getClassPath();

    /**
     * Specify module path
     */
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getModulePath();

    /**
     * Specify upgrade module path
     */
    @Optional
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getUpgradeModulePath();

    /**
     * Specify options for either modular or nonmodular target, but not for both
     */
    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    /** Recursively traverse all run-time dependencies. */
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getRecursive();

    /**
     *         Same as --list-reduced-deps with printing,   a comma-separated list of module dependencies.
     *         This output can be used by jlink --add-modules in order to create a custom image containing those modules and their transitive dependencies.
     */
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
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        final ExecResult result = exec("jdeps", spec -> {
            spec.setIgnoreExitValue(true);
            spec.setStandardOutput(s);
            if (!getClassPath().isEmpty()) spec.args("--class-path", getClassPath().getAsPath());
            if (!getModulePath().isEmpty()) spec.args("--module-path", getModulePath().getAsPath());
            if (!getUpgradeModulePath().isEmpty())
                spec.args("--upgrade-module-path", getUpgradeModulePath().getAsPath());
            if (getRecursive().getOrElse(false)) spec.args("--recursive");
            if (getPrintModuleDeps().getOrElse(false)) spec.args("--print-module-deps");
            if (getIgnoreMissingDeps().getOrElse(false)) spec.args("--ignore-missing-deps");
            if (getMultiRelease().isPresent()) spec.args("--multi-release", getMultiRelease().get().asInt());
            switch (getModularity().get()) {
                case Modular modular -> spec.args("--module", modular.getMainModule().get());
                case NonModular nonModular -> spec.args(nonModular.getMainJar().get());
            }
        });
        final String output = s.toString();
        if (result.getExitValue() != 0) {
            getLogger().error("Jdeps failed: {}", output);
        } else {
            getLogger().info("Jdeps found these module dependencies: {}", output);
        }
        result.assertNormalExitValue();
        final Path dest = getDestinationFile().get().getAsFile().toPath();
        try {
            Files.writeString(dest, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
