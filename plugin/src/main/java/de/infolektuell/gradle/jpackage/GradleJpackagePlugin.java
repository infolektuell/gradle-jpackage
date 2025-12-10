package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.ApplicationExtension;
import de.infolektuell.gradle.jpackage.tasks.*;
import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import de.infolektuell.gradle.jpackage.tasks.platform.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFile;
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
        Provider<@NonNull String> osName = project.getProviders().systemProperty("os.name");
        ApplicationExtension application = project.getExtensions().create(ApplicationExtension.EXTENSION_NAME, ApplicationExtension.class);
        application.metadata(data -> {
            data.getName().convention(project.getName());
            data.getVersion().convention(project.getVersion().toString());
            data.getDescription().convention(project.getDescription());
            data.getVendor().convention(project.getGroup().toString());
        });
        application.getLauncher().getLauncherAsService().convention(false);

        project.getPluginManager().withPlugin("java", javaPlugin -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.manifest().getAttributes().put("Main-Class", application.getLauncher().getMainClass());
            FileCollection classpath = java.getSourceSets().getByName("main").getRuntimeClasspath();
            FileCollection modulePath = classpath.filter(this::isModule);
            FileCollection nonModulePath = classpath.minus(modulePath);
            project.getTasks().withType(JDKToolTask.class, task -> {
                var metadata = getJavaToolchainService().launcherFor(java.getToolchain())
                    .orElse(getJavaToolchainService().launcherFor(spec -> {}))
                    .map(JavaLauncher::getMetadata);
                task.getMetadata().convention(metadata);
            });

            TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
            Provider<@NonNull RegularFile> mainJar = jarTask.flatMap(AbstractArchiveTask::getArchiveFile);

            Provider<@NonNull Modularity> modularity = project.getProviders().provider(() -> {
                final var launcher = application.getLauncher();
                if (launcher.getMainModule().isPresent()) {
                    var modular = project.getObjects().newInstance(Modular.class);
                    var module = launcher.getMainModule().zip(launcher.getMainClass(), (m, c) -> String.join("/", m, c));
                    modular.getMainModule().convention(module);
                    return modular;
                } else {
                    var nonModular = project.getObjects().newInstance(NonModular.class);
                    nonModular.getMainJar().convention(mainJar);
                    nonModular.getMainClass().convention(launcher.getMainClass());
                    return nonModular;
                }
            });

            var jdepsTask = project.getTasks().register("jdeps", JdepsTask.class, task -> {
                task.dependsOn("classes");
                task.setGroup("application");
                task.setDescription("Analyzes the project for required modules");
                task.getClassPath().from(nonModulePath);
                task.getModulePath().from(modulePath);
                task.getModularity().convention(modularity);
                task.getRecursive().convention(true);
                task.getPrintModuleDeps().convention(true);
                task.getIgnoreMissingDeps().convention(true);
                task.getMultiRelease().convention(java.getToolchain().getLanguageVersion());
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
                task.getJvmArguments().add(application.getLauncher().getMainModule().orElse(modulesProvider));
                task.getJvmArguments().addAll(application.getLauncher().getJavaOptions());
                task.getMainModule().convention(application.getLauncher().getMainModule());
                task.getMainClass().convention(application.getLauncher().getMainClass());
            });

            var jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a customized runtime image");
                task.getModulePath().from(modulePath);
                task.getAddModules().add(application.getLauncher().getMainModule().orElse(modulesProvider));
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
                task.getAppName().convention(application.getMetadata().getName());
                task.getAppDescription().convention(application.getMetadata().getDescription());
                task.getAppVersion().convention(application.getMetadata().getVersion());
                task.getIcon().convention(application.getMetadata().getIcon());
                task.getCopyright().convention(application.getMetadata().getCopyright());
                task.getVendor().convention(application.getMetadata().getVendor());
            });

            var jpackageImageTask = project.getTasks().register("appImage", JpackageTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a native app image");
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
                task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                task.getInput().convention(prepareInputTask.flatMap(PrepareInputTask::getDestination));
                task.getAppContent().from(application.getContent());
                task.getModularity().convention(modularity);
                application.getLauncher().getLaunchers().all(launcher -> task.getAdditionalLaunchers().add(launcher));
                task.getArguments().convention(application.getLauncher().getArguments());
                task.getJavaOptions().convention(application.getLauncher().getJavaOptions());
                Provider<@NonNull JpackagePlatformOptions> platformOptions = osName.map(os -> {
                    if (isWindows(os)) {
                        var win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                        return win;
                    } else if (isMac(os)) {
                        var mac = project.getObjects().newInstance(JpackageMacOSOptions.class);
                        mac.getMacAppCategory().convention(application.getMac().getAppCategory());
                        mac.getMacPackageName().convention(application.getMac().getPackageName());
                        mac.getMacPackageIdentifier().convention(application.getMac().getPackageID());
                        return mac;
                    } else {
                        var linux = project.getObjects().newInstance(JpackageLinuxOptions.class);
                        return linux;
                    }
                });
                task.getPlatformOptions().convention(platformOptions);
            });

            project.getTasks().register("appInstaller", JpackageTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a native app installer");
                task.getApplicationImage().convention(jpackageImageTask.flatMap(JpackageTask::getDest));
                task.getAboutURL().convention(application.getMetadata().getAboutUrl());
                task.getLicenseFile().convention(application.getMetadata().getLicenseFile());
                task.getFileAssociations().convention(application.getFileAssociations());
                task.getInstallDir().convention(application.getInstallDir());
                task.getResourceDir().convention(application.getResourceDir());
                Provider<@NonNull JpackagePlatformOptions> platformOptions = osName.map(os -> {
                    if (isWindows(os)) {
                        var win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                        return win;
                    } else if (isMac(os)) {
                        var mac = project.getObjects().newInstance(JpackageMacOSOptions.class);
                        mac.getMacDMGContent().convention(application.getMac().getDmgContent());
                        return mac;
                    } else {
                        var linux = project.getObjects().newInstance(JpackageLinuxOptions.class);
                        return linux;
                    }
                });
                task.getPlatformOptions().convention(platformOptions);
                application.getLauncher().getLaunchers().all(launcher -> task.getAdditionalLaunchers().add(launcher));
                task.getLauncherAsService().convention(application.getLauncher().getLauncherAsService());
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
