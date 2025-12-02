package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.regex.Pattern;

public abstract class JpackageTask extends DefaultTask {
    private static final Pattern versionPattern = Pattern.compile("^\\d+([.]\\d+){0,2}$");
    private static Boolean isVersion(String version) {
        var matcher = versionPattern.matcher(version);
        return matcher.find();
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

    @Optional
    @Input
    public abstract Property<@NonNull String> getAppName();

    @Optional
    @Input
    public abstract Property<@NonNull String> getAppVersion();

    @Optional
    @Input
    public abstract Property<@NonNull String> getAppDescription();

    @Optional
    @Input
    public abstract Property<@NonNull String> getVendor();

    @Optional
    @Input
    public abstract Property<@NonNull String> getCopyright();

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getIcon();

    /**
     * Arg files that contain additional options to be passed to jpackage
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ListProperty<@NonNull RegularFile> getArgFiles();

    @OutputDirectory
    public abstract DirectoryProperty getDest();

    protected void exec(Action<@NonNull ExecSpec> action) {
        getFileSystemOperations().delete(spec -> spec.delete(getDest()));

        getExecOperations().exec(spec -> {
            spec.executable(getExecutable().get());
            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));

            // Metadata
            if (getAppName().isPresent()) spec.args("--name", getAppName().get());
            if (getAppDescription().isPresent()) spec.args("--description", getAppDescription().get());
            if (getAppVersion().isPresent() && isVersion(getAppVersion().get())) spec.args("--app-version", getAppVersion().get());
            if (getIcon().isPresent()) spec.args("--icon", getIcon().get());
            if (getCopyright().isPresent()) spec.args("--copyright", getCopyright().get());
            if (getVendor().isPresent()) spec.args("--vendor", getVendor().get());

            if (getDest().isPresent()) spec.args("--dest", getDest().get());
            action.execute(spec);
        });
    }
}
