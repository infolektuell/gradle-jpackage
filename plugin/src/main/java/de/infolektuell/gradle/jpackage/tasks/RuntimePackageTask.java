package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;

@CacheableTask
public abstract class RuntimePackageTask extends PackageTask {
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    @TaskAction
    protected void run() {
        exec(spec -> spec.args("--runtime-image", getRuntimeImage().get()));
    }
}
