package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.jspecify.annotations.NonNull;

public abstract class ImageHandler {
    public abstract ConfigurableFileCollection getContent();
    public abstract ListProperty<@NonNull String> getJvmArgs();
}
