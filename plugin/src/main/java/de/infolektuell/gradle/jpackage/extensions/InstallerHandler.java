package de.infolektuell.gradle.jpackage.extensions;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Nested;
import org.jspecify.annotations.NonNull;
import java.net.URI;

public abstract class InstallerHandler {
    @Nested
    public abstract TypeHandler getType();
    public abstract Property<@NonNull URI> getAboutUrl();
    public abstract RegularFileProperty getLicenseFile();
    public abstract Property<@NonNull Boolean> getLauncherAsService();
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();
    public abstract DirectoryProperty getInstallDir();
    public abstract DirectoryProperty getResourceDir();
    public abstract DirectoryProperty getDmcContent();
}
