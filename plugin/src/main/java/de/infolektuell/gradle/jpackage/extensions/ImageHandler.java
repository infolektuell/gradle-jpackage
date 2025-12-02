package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.file.ConfigurableFileCollection;

public abstract class ImageHandler {
    public abstract ConfigurableFileCollection getContent();
}
