package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.*;
import org.gradle.process.ExecOperations;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

public abstract class RunTask extends DefaultTask {
    @Inject
    protected abstract ExecOperations getExecOperations();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClassPath();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getModulePath();

    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @Input
    public abstract ListProperty<@NonNull String> getAddModules();

    @Input
    public abstract ListProperty<@NonNull String> getJavaOptions();

    @Input
    @Option(option = "args", description = "Command line arguments passed to the main class.")
    public abstract ListProperty<@NonNull String> getArguments();

    @Optional
    @Input
    @Option(option = "debug-jvm", description = "Enable debugging for the process. The process is started suspended and listening on port 5005.")
    public abstract Property<@NonNull Boolean> getDebugJvm();

    @TaskAction
    protected void run() {
        getExecOperations().javaexec(spec -> {
            spec.setDebug(getDebugJvm().getOrElse(false));
            spec.classpath(getClassPath());
            if (!getModulePath().isEmpty()) spec.jvmArgs("--module-path", getModulePath().getAsPath());
            getAddModules().get().forEach(m -> spec.jvmArgs("--add-modules", m));
            spec.jvmArgs(getJavaOptions().get());
            switch (getModularity().get()) {
                case Modular modular -> spec.getMainModule().convention(modular.getMainModule().zip(modular.getMainClass(), (m, c) -> String.join("/", m, c)));
                case NonModular nonModular -> spec.getMainClass().convention(nonModular.getMainClass());
            }
            spec.args(getArguments().get());
        });
    }
}
