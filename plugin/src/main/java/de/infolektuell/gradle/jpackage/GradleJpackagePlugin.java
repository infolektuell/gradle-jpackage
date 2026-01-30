package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.JpackageExtension;
import de.infolektuell.gradle.jpackage.extensions.SourceSetExtension;
import de.infolektuell.gradle.jpackage.model.Modules;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

/**
 * Gradle plugin that creates native application installers using Jpackage
 */
public abstract class GradleJpackagePlugin implements Plugin<@NonNull Project> {
    /**
     * The plugin ID
     */
    public static final String PLUGIN_NAME = "de.infolektuell.jpackage";

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();

    @Override
    public void apply(Project project) {
        final JpackageExtension jpackageExtension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        project.getPluginManager().withPlugin("java", javaPlugin -> {
            final JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            javaExtension.getSourceSets().configureEach(s -> {
                final SourceSetExtension sourceSetExtension = project.getObjects().newInstance(SourceSetExtension.class);
                s.getExtensions().add(SourceSetExtension.EXTEnSION_NAME, sourceSetExtension);
                final Provider<@NonNull Boolean> isModule = s.getAllJava().getSourceDirectories().filter(Modules::isModule).getElements().map(e -> !e.isEmpty());
                    sourceSetExtension.getModulePath().getInferModulePath().convention(isModule);
                final FileCollection fullClasspath = s.getCompileClasspath();
                sourceSetExtension.getModulePath().getFullClasspath().from(fullClasspath);
                s.setCompileClasspath(sourceSetExtension.getModulePath().getClasspath());
                project.getTasks().withType(JavaCompile.class).named(s.getCompileTaskName("Java"), task -> {
                    task.getModularity().getInferModulePath().set(sourceSetExtension.getModulePath().getInferModulePath().map(x -> !x));
                    task.getOptions().getCompilerArgumentProviders().add(sourceSetExtension.getModulePath());
                    task.getOptions().getCompilerArgumentProviders().add(sourceSetExtension.getPatchModule());
                });
            });
        });
    }
}
