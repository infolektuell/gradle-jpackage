package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.ApplicationExtension;
import de.infolektuell.gradle.jpackage.extensions.JpackageExtension;
import de.infolektuell.gradle.jpackage.tasks.*;
import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.jvm.toolchain.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Gradle plugin that connects jpackage with the application plugin
 */
public abstract class GradleJpackagePlugin implements Plugin<@NotNull Project> {
    /**
     * The plugin ID
     */
    public static final String PLUGIN_NAME = "de.infolektuell.jpackage";

    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();

    @Override
    public void apply(@NotNull Project project) {
        ApplicationExtension application = project.getExtensions().create(ApplicationExtension.EXTENSION_NAME, ApplicationExtension.class);
        JpackageExtension extension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        extension.getInstaller().getLauncherAsService().convention(false);
        extension.getMetadata().getVersion().convention(project.getVersion().toString());
        extension.getMetadata().getDescription().convention(project.getDescription());
        extension.getMetadata().getVendor().convention(project.getGroup().toString());
        var osName = project.getProviders().systemProperty("os.name");

        project.getPluginManager().withPlugin("java", javaPlugin -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            JavaToolchainSpec defaultToolchain = java.getToolchain();
            extension.getToolchain().convention(defaultToolchain);

            var installationPath = extension.getToolchain()
                .flatMap(spec -> getJavaToolchainService().compilerFor(spec))
                .orElse(getJavaToolchainService().compilerFor(spec -> {}))
                .map(it -> it.getMetadata().getInstallationPath());

            extension.getMetadata().getName().convention(application.getApplicationName());
            TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
            var mainJar = jarTask.flatMap(AbstractArchiveTask::getArchiveFile);
            // var classpath =project.getObjects().fileCollection().from(mainJar, java.getSourceSets().named("main").map(s -> s.getRuntimeClasspath()/*.filter(f -> f.isFile() && f.getName().endsWith(".jar"))*/));
            var classpath = java.getSourceSets().getByName("main").getRuntimeClasspath();
            var modulePath = classpath.filter(this::isModule);
            var nonModulePath = classpath.minus(modulePath);
            java.manifest().getAttributes().put("Main-Class", application.getMainClass());

            Provider<@NonNull Modularity> modularity = project.getProviders().provider(() -> {
                if (application.getMainModule().isPresent()) {
                    var modular = project.getObjects().newInstance(Modular.class);
                    modular.getMainModule().convention(application.getMainModule());
                    modular.getMainClass().convention(application.getMainClass());
                    return modular;
                } else {
                    var nonModular = project.getObjects().newInstance(NonModular.class);
                    nonModular.getMainJar().convention(mainJar);
                    nonModular.getMainClass().convention(application.getMainClass());
                    return nonModular;
                }
            });

            var jdepsTask = project.getTasks().register("jdeps", JdepsTask.class, task -> {
                task.dependsOn("classes");
                task.setGroup("application");
                task.setDescription("Analyzes the project for required modules");
                task.getExecutable().convention(installationPath.zip(osName, (p, os) -> p.file(isWindows(os) ? "bin/jdeps.exe" : "bin/jdeps")));
                task.getClassPath().from(nonModulePath);
                task.getModulePath().from(modulePath);
                task.getModularity().convention(modularity);
                task.getRecursive().convention(true);
                task.getPrintModuleDeps().convention(true);
                task.getIgnoreMissingDeps().convention(true);
                task.getMultiRelease().convention(extension.getToolchain().flatMap(JavaToolchainSpec::getLanguageVersion));
                task.getDestinationFile().convention(project.getLayout().getBuildDirectory().file("jpackage/jdeps/jdeps-result.txt"));
            });

            var modulesProvider = jdepsTask.flatMap(task -> task.getDestinationFile().map(f -> {
                try {
                    return Files.readString(f.getAsFile().toPath()).trim();
                } catch (Exception ignored) {
                    return "ALL-MODULE-PATH";
                }
            }));

            project.getTasks().register("run", JavaExec.class, task -> {
                task.dependsOn("classes");
                task.setGroup("application");
                task.setDescription("Runs this project as a JVM application");
                task.classpath(nonModulePath);
                if (!modulePath.isEmpty()) task.jvmArgs("--module-path", modulePath.getAsPath());
                task.jvmArgs("--add-modules");
                task.getJvmArguments().add(application.getMainModule().orElse(modulesProvider));
                task.getJvmArguments().addAll(application.getApplicationDefaultJvmArgs());
                task.getMainModule().convention(application.getMainModule());
                task.getMainClass().convention(application.getMainClass());
            });

            var jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a customized runtime image");
                task.getExecutable().convention(installationPath.zip(osName, (p, os) -> p.file(isWindows(os) ? "bin/jlink.exe" : "bin/jlink")));
                task.getModulePath().from(modulePath);
                task.getAddModules().add(application.getMainModule().orElse(modulesProvider));
                task.getNoHeaderFiles().convention(true);
                task.getNoManPages().convention(true);
                task.getStripDebug().convention(true);
                task.getStripNativeCommands().convention(true);
                task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jpackage/runtime"));
            });

            TaskProvider<@NonNull PrepareInputTask> prepareInputTask = project.getTasks().register("prepareInput", PrepareInputTask.class, task -> {
                task.getSource().from(nonModulePath);
                task.getModularity().convention(modularity);
                task.getDestination().convention(project.getLayout().getBuildDirectory().dir("jpackage/input"));
            });

            project.getTasks().withType(JpackageTask.class, task -> {
                task.getExecutable().convention(installationPath.zip(osName, (p, os) -> p.file(isWindows(os) ? "bin/jpackage.exe" : "bin/jpackage")));
                task.getAppName().convention(extension.getMetadata().getName());
                task.getAppDescription().convention(extension.getMetadata().getDescription());
                task.getAppVersion().convention(extension.getMetadata().getVersion());
                task.getIcon().convention(extension.getMetadata().getIcon());
                task.getCopyright().convention(extension.getMetadata().getCopyright());
                task.getVendor().convention(extension.getMetadata().getVendor());
            });

            var jpackageImageTask = project.getTasks().register("appImage", ApplicationImageTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a native app image");
                task.getInput().convention(prepareInputTask.flatMap(PrepareInputTask::getDestination));
                task.getAppContent().from(extension.getImage().getContent());
                task.getModularity().convention(modularity);
                task.getJavaOptions().convention(extension.getImage().getJvmArgs());
                extension.getLaunchers().all(launcher -> task.getAdditionalLaunchers().add(launcher));
                task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
            });

            project.getTasks().register("appInstaller", ApplicationPackageTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a native app installer");
                var currentType = osName.flatMap((os) -> {
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

    private boolean isModule(File file) {
        if (file.isDirectory()) return isModule(getObjects().fileTree().from(file));
        if (file.isFile() && file.getName().endsWith(".jar")) return isModule(getArchiveOperations().zipTree(file));
        return false;
    }
    private boolean isModule(FileTree tree) {
        if (!tree.matching(spec -> spec.include("**/module-info.class")).isEmpty()) return true;
        try {
            final Path manifestFile = tree.matching(spec -> spec.include("META-INF/MANIFEST.MF")).getSingleFile().toPath();
            if (Files.readAllLines(manifestFile).stream().anyMatch(l -> l.contains("Automatic-Module-Name"))) return true;
        } catch (Exception ignored) {}
        return false;
    }
}
