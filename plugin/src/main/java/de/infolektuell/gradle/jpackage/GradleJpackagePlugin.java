package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.JpackageExtension;
import de.infolektuell.gradle.jpackage.tasks.JlinkTask;
import de.infolektuell.gradle.jpackage.tasks.JpackageTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.List;

import static de.infolektuell.gradle.jpackage.tasks.JpackageTask.JpackageModularOptions;
import static de.infolektuell.gradle.jpackage.tasks.JpackageTask.JpackageNonModularOptions;

/**
 * Gradle plugin that connects jpackage with the application plugin
 */
public abstract class GradleJpackagePlugin implements Plugin<@NotNull Project> {
    /**
     * The plugin ID
     */
    public static final String PLUGIN_NAME = "de.infolektuell.jpackage";

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();

    @Override
    public void apply(@NotNull Project project) {
        JpackageExtension extension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        extension.getRuntime().getModules().convention(List.of("java.base"));
        extension.getRuntime().getJpackage().convention(true);

        project.getPluginManager().withPlugin("application", applicationPlugin -> {
            JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
            if (java != null) {
                JavaToolchainSpec defaultToolchain = java.getToolchain();
                extension.getToolchain().convention(defaultToolchain);
            }
            var installationPath = extension.getToolchain()
                .flatMap(getJavaToolchainService()::compilerFor)
                .map(it -> it.getMetadata().getInstallationPath());
            var jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
                task.setGroup("Distribution");
                task.setDescription("Generates a customized runtime image");
                task.getExecutable().convention(installationPath.map(p -> p.file("bin/jlink")));
                if (java != null) {
                    task.getModulePath().from(java.getSourceSets().named("main").map(SourceSet::getRuntimeClasspath));
                }
                task.getAddModules().convention(extension.getRuntime().getModules());
                task.getNoHeaderFiles().convention(extension.getRuntime().getJpackage());
                task.getNoManPages().convention(extension.getRuntime().getJpackage());
                task.getStripDebug().convention(extension.getRuntime().getJpackage());
                task.getStripNativeCommands().convention(extension.getRuntime().getJpackage());
                task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jpackage/runtime"));
            });
            project.getTasks().register("jpackage", JpackageTask.class, task -> {
                task.setGroup("Distribution");
                task.setDescription("Generates a native app installer");
                task.getMetadata().convention(extension.getMetadata());
                task.getExecutable().convention(installationPath.map(p -> p.file("bin/jpackage")));
                task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/install"));
            });

            JavaApplication application = project.getExtensions().findByType(JavaApplication.class);
            if (application != null) {
                extension.getMetadata().getName().convention(application.getApplicationName());
                extension.getMetadata().getAppVersion().convention(project.getVersion().toString());
                extension.getMetadata().getDescription().convention(project.getDescription());
                extension.getMetadata().getVendor().convention(project.getGroup().toString());
                project.getTasks().withType(JpackageTask.class, task -> {
                    if (application.getMainModule().isPresent()) {
                        var modular = project.getObjects().newInstance(JpackageModularOptions.class);
                        modular.getModuleName().convention(application.getMainModule());
                        modular.getClassName().convention(application.getMainClass());
                        task.getModularity().convention(modular);
                    } else {
                        var nonModular = project.getObjects().newInstance(JpackageNonModularOptions.class);
                        TaskProvider<@NonNull Sync> installDist = project.getTasks().withType(Sync.class).named("installDist");
                        var input = installDist.map(i -> project.getLayout().getProjectDirectory().dir(i.getDestinationDir().getAbsolutePath()).dir("lib"));
                        nonModular.getInput().convention(input);
                        TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
                        nonModular.getMainJar().convention(jarTask.flatMap(AbstractArchiveTask::getArchiveFile));
                        nonModular.getMainClass().convention(application.getMainClass());
                        task.getModularity().convention(nonModular);
                    }
                });
            }
        });
    }
}
