package de.infolektuell.gradle.jpackage.tasks.providers;

import de.infolektuell.gradle.jpackage.model.Modules;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ModulePathProvider implements CommandLineArgumentProvider {
    @Input
    public abstract Property<@NonNull Boolean> getIsEnabled();

    @InputFiles
    @CompileClasspath
    public abstract ConfigurableFileCollection getFullClasspath();

    @Optional
    @Input
    public abstract Property<@NonNull String> getMainModule();

    @Internal
    public FileCollection getModulePath() {
        return getFullClasspath().filter(f -> getIsEnabled().get() && Modules.isModule(f));
    }

    @Internal
    public FileCollection getClasspath() {
        return getIsEnabled().getOrElse(false) ? getFullClasspath().filter(f -> !Modules.isModule(f)) : getFullClasspath();
    }

    @Override
    public Iterable<String> asArguments() {
        List<String> args = new ArrayList<>();
        if (getModulePath().isEmpty()) return args;
        args.add("--module-path");
        args.add(getModulePath().getAsPath());
        if (getMainModule().isPresent()) {
            args.add("--module");
            args.add(getMainModule().get());
        }
        return args;
    }
}
