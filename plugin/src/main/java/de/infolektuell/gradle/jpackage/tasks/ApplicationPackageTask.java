package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

@CacheableTask
public abstract class ApplicationPackageTask extends JpackageTask {
    public enum Type implements Serializable {
        DMG, PKG, EXE, MSI, DEB, RPM;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Optional
    @Input
    public abstract Property<@NonNull Type> getType();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getApplicationImage();

    @TaskAction
    protected void run() {
        exec(spec -> {
            if (getType().isPresent()) spec.args("--type", getType().get().name().toLowerCase());
            getApplicationImage().get().getAsFileTree().visit(f -> {
                if (f.isDirectory()) {
                    spec.args("--app-image", f.getFile());
                    f.stopVisiting();
                }
            });
        });
    }
}
