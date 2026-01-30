package de.infolektuell.gradle.jpackage.tasks.providers;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.process.CommandLineArgumentProvider;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class PatchModuleProvider implements CommandLineArgumentProvider {
    public interface PatchModuleDefinition {
        @Input
        Property<@NonNull String> getModule();

        @CompileClasspath
        ConfigurableFileCollection getClasses();
    }

    @Inject
    protected abstract ObjectFactory getObjects();

    @Nested
    public abstract ListProperty<@NonNull PatchModuleDefinition> getModuleDefinitions();

    public PatchModuleDefinition add(Action<@NonNull PatchModuleDefinition> action) {
        PatchModuleDefinition definition = getObjects().newInstance(PatchModuleDefinition.class);
        action.execute(definition);
        getModuleDefinitions().add(definition);
        return definition;
    }

    @Override
    public Iterable<String> asArguments() {
        List<String> args = new ArrayList<>();
        getModuleDefinitions().get().forEach(it -> {
            args.add("--patch-module");
            args.add(String.join("=", it.getModule().get(), it.getClasses().getAsPath()));
        });
        return args;
    }
}
