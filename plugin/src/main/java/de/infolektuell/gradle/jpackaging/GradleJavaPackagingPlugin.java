package de.infolektuell.gradle.jpackaging;

import de.infolektuell.gradle.jpackaging.extensions.PackagingExtension;
import de.infolektuell.gradle.jpackaging.tasks.JPackageTask;
import de.infolektuell.gradle.jpackaging.tasks.JlinkTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jetbrains.annotations.NotNull;
import javax.inject.Inject;
import java.util.List;

/** Gradle plugin to create native installers for java apps */
public abstract class GradleJavaPackagingPlugin implements Plugin<@NotNull Project> {
    /** The plugin ID */
    public static final String PLUGIN_NAME = "de.infolektuell.java-packaging";

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();

    @Override
    public void apply(@NotNull Project project) {
        PackagingExtension extension = project.getExtensions().create(PackagingExtension.EXTENSION_NAME, PackagingExtension.class);
        extension.getRuntime().getModules().convention(List.of("java.base"));
        extension.getRuntime().getJpackage().convention(true);

        var installationPath = extension.getToolchain()
            .flatMap(getJavaToolchainService()::compilerFor)
            .map(it -> it.getMetadata().getInstallationPath());
        project.getTasks().register("runtime", JlinkTask.class, task -> {
            task.getExecutable().convention(installationPath.map(p -> p.file("bin/jlink")));
            task.getAddModules().convention(extension.getRuntime().getModules());
            task.getNoHeaderFiles().convention(extension.getRuntime().getJpackage());
            task.getNoManPages().convention(extension.getRuntime().getJpackage());
            task.getStripDebug().convention(extension.getRuntime().getJpackage());
            task.getOutput().convention(project.getLayout().getBuildDirectory().dir("runtime"));
        });
        project.getTasks().register("jpackage", JPackageTask.class, task -> {
            task.getExecutable().convention(installationPath.map(p -> p.file("bin/jpackage")));
            task.getArgFiles().convention(extension.getArgFiles());
        });
        project.getPluginManager().withPlugin("java", p -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            JavaToolchainSpec defaultToolchain = java.getToolchain();
            extension.getToolchain().convention(defaultToolchain);
        });
    }
}
