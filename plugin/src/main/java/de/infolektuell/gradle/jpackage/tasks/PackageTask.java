package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public abstract class PackageTask extends JpackageTask {
    @Optional
    @Input
    public abstract Property<@NonNull String> getType();

    @Optional
    @Input
    public abstract Property<@NonNull URI> getAboutUrl();

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getLicenseFile();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInstallDir();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getResourceDir();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getDMGContent();

    @Override
    protected void exec(Action<@NonNull ExecSpec> action) {
        super.exec(spec -> {
            if (getType().isPresent()) spec.args("--type", getType().get());
            if (getAboutUrl().isPresent()) spec.args("--about-url", getAboutUrl().get());
            if (getLicenseFile().isPresent()) spec.args("--license-file", getLicenseFile().get());
            if (getInstallDir().isPresent()) spec.args("--install-dir", getInstallDir().get());
            if (getResourceDir().isPresent()) spec.args("--resource-dir", getResourceDir().get());
            if (getDMGContent().isPresent()) spec.args("--dmg-content", getDMGContent().get());
            action.execute(spec);
        });
    }
}
