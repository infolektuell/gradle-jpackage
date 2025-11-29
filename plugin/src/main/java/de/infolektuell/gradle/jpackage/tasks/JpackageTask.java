package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Runs jpackage tool with given options
 */
public abstract class JpackageTask extends DefaultTask {
    private static final Pattern versionPattern = Pattern.compile("^\\d+([.]\\d+){0,2}$");

    public enum Type implements Serializable {
        APP_IMAGE, DMG, PKG, EXE, MSI, DEB, RPM;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public interface JpackageAppMetadata {
        @Optional
        @Input
        Property<@NonNull String> getName();

        @Optional
        @Input
        Property<@NonNull String> getAppVersion();

        @Optional
        @Input
        Property<@NonNull String> getDescription();

        @Optional
        @Input
        Property<@NonNull String> getVendor();

        @Optional
        @Input
        Property<@NonNull String> getCopyright();

        @Optional
        @InputFile
        RegularFileProperty getIcon();
    }

    public sealed interface JpackageModularityOptions permits JpackageModularOptions, JpackageNonModularOptions {
    }

    public non-sealed interface JpackageModularOptions extends JpackageModularityOptions {
        @Input
        Property<@NonNull String> getModuleName();

        @Optional
        @Input
        Property<@NonNull String> getClassName();
    }

    public non-sealed interface JpackageNonModularOptions extends JpackageModularityOptions {
        @InputDirectory
        DirectoryProperty getInput();

        @InputFile
        RegularFileProperty getMainJar();

        @Input
        Property<@NonNull String> getMainClass();
    }

    private final ExecOperations execOperations;

    /**
     * The jpackage executable to run
     */
    @InputFile
    public abstract RegularFileProperty getExecutable();

    @Nested
    public abstract Property<@NonNull JpackageModularityOptions> getModularity();

    @Optional
    @Input
    public abstract Property<@NonNull Type> getType();

    @Nested
    public abstract Property<@NonNull JpackageAppMetadata> getMetadata();

    @InputDirectory
    public abstract DirectoryProperty getRuntimeImage();

    @OutputDirectory
    public abstract DirectoryProperty getDest();

    /**
     * Arg files that contain additional options to be passed to jpackage
     */
    @InputFiles
    public abstract ListProperty<@NonNull RegularFile> getArgFiles();

    @Inject
    public JpackageTask(
        ExecOperations execOperations
    ) {
        this.execOperations = execOperations;
    }

    @TaskAction
    protected void run() {
        execOperations.exec(spec -> {
            spec.executable(getExecutable().get());
            switch (getModularity().get()) {
                case JpackageModularOptions modular -> {
                    if (modular.getClassName().isPresent()) {
                        spec.args("--module", String.join("/", modular.getModuleName().get(), modular.getClassName().get()));
                    } else {
                        spec.args("--module", modular.getModuleName().get());
                    }
                }
                case JpackageNonModularOptions nonModular -> {
                    spec.args("--input", nonModular.getInput().get());
                    spec.args("--main-jar", nonModular.getMainJar().get().getAsFile().getName());
                    spec.args("--main-class", nonModular.getMainClass().get());
                }
            }

            if (getType().isPresent()) spec.args("--type", getType().get().name().toLowerCase().replace("_", "-"));
            if (getRuntimeImage().isPresent()) spec.args("--runtime-image", getRuntimeImage().get());
            if (getDest().isPresent()) spec.args("--dest", getDest().get());

            // Metadata
            final JpackageAppMetadata metadata = getMetadata().get();
            if (metadata.getName().isPresent()) spec.args("--name", metadata.getName().get());
            if (metadata.getDescription().isPresent()) spec.args("--description", metadata.getDescription().get());
            if (metadata.getAppVersion().isPresent()) {
                var version = metadata.getAppVersion().get();
                var matcher = versionPattern.matcher(version);
                if (matcher.find()) spec.args("--app-version", version);
            }
            if (metadata.getCopyright().isPresent()) spec.args("--copyright", metadata.getCopyright().get());
            if (metadata.getVendor().isPresent()) spec.args("--vendor", metadata.getVendor().get());
            if (metadata.getIcon().isPresent()) spec.args("--icon", metadata.getIcon().get());

            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));
        });
    }
}
