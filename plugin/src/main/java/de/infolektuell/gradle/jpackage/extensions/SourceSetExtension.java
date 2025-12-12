package de.infolektuell.gradle.jpackage.extensions;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.process.CommandLineArgumentProvider;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.List;

public abstract class SourceSetExtension implements CommandLineArgumentProvider {
    public static final String EXTEnSION_NAME = "patchModule";

    public interface PatchModuleDefinition {
        @Input
        Property<@NonNull String> getModule();
        @CompileClasspath
        ConfigurableFileCollection getClasses();
    }

    private final ObjectFactory objects;
    private final DomainObjectSet<@NonNull PatchModuleDefinition> moduleDefinitions;

    @Inject
    public SourceSetExtension(@NonNull ObjectFactory objects) {
        super();
        this.objects = objects;
        this.moduleDefinitions = objects.domainObjectSet(PatchModuleDefinition.class);
    }

    public PatchModuleDefinition define(Action<@NonNull PatchModuleDefinition> action) {
        PatchModuleDefinition definition = objects.newInstance(PatchModuleDefinition.class);
        action.execute(definition);
        moduleDefinitions.add(definition);
        return definition;
    }

    @Override
    public Iterable<String> asArguments() {
        List<String> modules = new java.util.ArrayList<>();
        moduleDefinitions.all(it -> {
            modules.add("--patch-module");
            modules.add(String.join("=", it.getModule().get(), it.getClasses().getAsPath()));
        });
        return modules;
    }
}
