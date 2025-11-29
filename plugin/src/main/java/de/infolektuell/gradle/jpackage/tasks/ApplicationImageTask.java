package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Generates an application image using Jpackage
 */
@CacheableTask
public abstract class ApplicationImageTask extends JpackageTask {
    private static final Pattern versionPattern = Pattern.compile("^\\d+([.]\\d+){0,2}$");

    public interface JpackageAppMetadata {
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
        @PathSensitive(PathSensitivity.RELATIVE)
        RegularFileProperty getIcon();
    }

    public sealed interface JpackageModularityOptions permits JpackageModularOptions, JpackageNonModularOptions {}

    public non-sealed interface JpackageModularOptions extends JpackageModularityOptions {
        @Input
        Property<@NonNull String> getModuleName();

        @Optional
        @Input
        Property<@NonNull String> getClassName();
    }

    public non-sealed interface JpackageNonModularOptions extends JpackageModularityOptions {
        @InputDirectory
        @PathSensitive(PathSensitivity.RELATIVE)
        DirectoryProperty getInput();

        @InputFile
        @PathSensitive(PathSensitivity.RELATIVE)
        RegularFileProperty getMainJar();

        @Input
        Property<@NonNull String> getMainClass();
    }

    @Nested
    public abstract Property<@NonNull JpackageModularityOptions> getModularity();

    @Nested
    public abstract Property<@NonNull JpackageAppMetadata> getMetadata();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    @TaskAction
    protected void run() {
        exec(spec -> {
            spec.args("--type", "app-image");
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

            // Metadata
            final JpackageAppMetadata metadata = getMetadata().get();
            if (metadata.getDescription().isPresent()) spec.args("--description", metadata.getDescription().get());
            if (metadata.getAppVersion().isPresent()) {
                var version = metadata.getAppVersion().get();
                var matcher = versionPattern.matcher(version);
                if (matcher.find()) spec.args("--app-version", version);
            }
            if (metadata.getCopyright().isPresent()) spec.args("--copyright", metadata.getCopyright().get());
            if (metadata.getVendor().isPresent()) spec.args("--vendor", metadata.getVendor().get());
            if (metadata.getIcon().isPresent()) spec.args("--icon", metadata.getIcon().get());

            spec.args("--runtime-image", getRuntimeImage().get());
        });
    }
}
