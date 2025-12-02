package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.JpackageExtension;
import de.infolektuell.gradle.jpackage.tasks.*;
import de.infolektuell.gradle.jpackage.tasks.ApplicationImageTask.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
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
import java.util.regex.Pattern;

import de.infolektuell.gradle.jpackage.tasks.types.*;

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
        extension.getInstaller().getLauncherAsService().convention(false);
        extension.getMetadata().getVersion().convention(project.getVersion().toString());
        extension.getMetadata().getDescription().convention(project.getDescription());
        extension.getMetadata().getVendor().convention(project.getGroup().toString());

        project.getPluginManager().withPlugin("application", applicationPlugin -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            JavaApplication application = project.getExtensions().getByType(JavaApplication.class);
            JavaToolchainSpec defaultToolchain = java.getToolchain();
            extension.getToolchain().convention(defaultToolchain);
            extension.getMetadata().getName().convention(application.getApplicationName());

            TaskProvider<@NonNull Sync> installDist = project.getTasks().withType(Sync.class).named("installDist");
            TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
            var input = installDist.map(i -> project.getLayout().getProjectDirectory().dir(i.getDestinationDir().getAbsolutePath()).dir("lib"));
            var mainJar = jarTask.flatMap(AbstractArchiveTask::getArchiveFile);
            Provider<@NonNull Modularity> modularity = project.getProviders().provider(() -> {
                if (application.getMainModule().isPresent()) {
                    var modular = project.getObjects().newInstance(Modular.class);
                    var module = application.getMainModule().zip(application.getMainClass(), (mainModule, mainClass) -> String.join("/", mainModule, mainClass));
                    modular.getModule().convention(module);
                    return modular;
                } else {
                    var nonModular = project.getObjects().newInstance(NonModular.class);
                    nonModular.getMainJar().convention(mainJar);
                    nonModular.getMainClass().convention(application.getMainClass());
                    return nonModular;
                }
            });

            var installationPath = extension.getToolchain()
                .flatMap(getJavaToolchainService()::compilerFor)
                .map(it -> it.getMetadata().getInstallationPath());

            var jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
                task.setGroup("Distribution");
                task.setDescription("Generates a customized runtime image");
                task.getExecutable().convention(installationPath.map(p -> p.file("bin/jlink")));
                task.getModulePath().from(java.getSourceSets().named("main").map(SourceSet::getRuntimeClasspath));
                task.getAddModules().convention(extension.getRuntime().getModules());
                task.getNoHeaderFiles().convention(extension.getRuntime().getJpackage());
                task.getNoManPages().convention(extension.getRuntime().getJpackage());
                task.getStripDebug().convention(extension.getRuntime().getJpackage());
                task.getStripNativeCommands().convention(extension.getRuntime().getJpackage());
                task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jpackage/runtime"));
            });

            project.getTasks().withType(JpackageTask.class, task -> {
                task.getExecutable().convention(installationPath.map(p -> p.file("bin/jpackage")));
                task.getAppName().convention(extension.getMetadata().getName());
                task.getAppDescription().convention(extension.getMetadata().getDescription());
                task.getAppVersion().convention(extension.getMetadata().getVersion());
                task.getIcon().convention(extension.getMetadata().getIcon());
                task.getCopyright().convention(extension.getMetadata().getCopyright());
                task.getVendor().convention(extension.getMetadata().getVendor());
            });

            var jpackageImageTask = project.getTasks().register("appImage", ApplicationImageTask.class, task -> {
                task.setGroup("jpackage");
                task.setDescription("Generates a native app image");
                task.getInput().convention(input);
                task.getAppContent().from(extension.getImage().getContent());
                task.getModularity().convention(modularity);
                extension.getLaunchers().all(launcher -> task.getAdditionalLaunchers().add(launcher));
                task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
            });

            project.getTasks().register("appInstaller", ApplicationPackageTask.class, task -> {
                task.setGroup("jpackage");
                task.setDescription("Generates a native app installer");
                var currentType = project.getProviders().systemProperty("os.name").flatMap((os) -> {
                    if (isWindows(os)) return extension.getInstaller().getType().getWindows().map(Enum::name);
                    if (isMac(os)) return extension.getInstaller().getType().getMac().map(Enum::name);
                    return extension.getInstaller().getType().getLinux().map(Enum::name);
                });
                task.getType().convention(currentType);
                task.getApplicationImage().convention(jpackageImageTask.flatMap(JpackageTask::getDest));
                task.getAboutUrl().convention(extension.getInstaller().getAboutUrl());
                task.getLicenseFile().convention(extension.getInstaller().getLicenseFile());
                task.getFileAssociations().convention(extension.getInstaller().getFileAssociations());
                task.getInstallDir().convention(extension.getInstaller().getInstallDir());
                task.getResourceDir().convention(extension.getInstaller().getResourceDir());
                task.getDMGContent().convention(extension.getInstaller().getDmcContent());
                extension.getLaunchers().all(launcher -> task.getAdditionalLaunchers().add(launcher));
                task.getLauncherAsService().convention(extension.getInstaller().getLauncherAsService());
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/install"));
            });
        });
    }
    private Boolean isWindows(String osName) {
        var pattern = Pattern.compile("windows", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(osName);
        return matcher.find();
    }
    private Boolean isMac(String osName) {
        var pattern = Pattern.compile("mac", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(osName);
        return matcher.find();
    }
}
