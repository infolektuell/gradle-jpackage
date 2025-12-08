package de.infolektuell.gradle.jpackage.extensions;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public abstract class MetadataHandler {
    public abstract Property<@NonNull String> getName();
    public abstract Property<@NonNull String> getVersion();
    public abstract RegularFileProperty getIcon();
    public abstract Property<@NonNull String> getDescription();
    public abstract Property<@NonNull String> getCopyright();
    public abstract Property<@NonNull String> getVendor();
    public abstract Property<@NonNull URI> getAboutUrl();
    public abstract RegularFileProperty getLicenseFile();
}
