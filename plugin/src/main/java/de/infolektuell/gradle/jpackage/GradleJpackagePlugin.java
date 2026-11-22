package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.*;
import de.infolektuell.gradle.jpackage.model.Modules;
import de.infolektuell.gradle.jpackage.model.Platform;
import de.infolektuell.gradle.jpackage.tasks.JdepsTask;
import de.infolektuell.gradle.jpackage.tasks.JlinkTask;
import de.infolektuell.gradle.jpackage.tasks.JpackageTask;
import de.infolektuell.gradle.jpackage.tasks.PrepareInputTask;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageLinuxOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageMacOSOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackagePlatformOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageWindowsOptions;
import de.infolektuell.gradle.jpackage.toolchain.ToolFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/// Gradle plugin that creates native application installers using jpackage
public abstract class GradleJpackagePlugin implements Plugin<@NonNull Project> {
    /// The plugin ID
    public static final String PLUGIN_NAME = "de.infolektuell.jpackage";

    @Override
    public void apply(Project project) {
        final NamedDomainObjectProvider<@NonNull Configuration> jlinkConfig = project.getConfigurations().register("jlink", config -> {
            config.setCanBeConsumed(false);
            config.setCanBeResolved(false);
        });
        final NamedDomainObjectProvider<@NonNull Configuration> jlinkElementsConfig = project.getConfigurations().register("jlinkElements", config -> {
            config.setCanBeConsumed(false);
            config.setCanBeResolved(true);
            config.extendsFrom(jlinkConfig.get());
        });

        final JpackageExtension jpackageExtension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        jpackageExtension.metadata(data -> {
            data.getName().convention(project.getName());
            data.getVersion().convention(project.getProviders().provider(() -> project.getVersion().toString()));
            data.getDescription().convention(project.getProviders().provider(project::getDescription));
            data.getVendor().convention(project.getProviders().provider(() -> project.getGroup().toString()));
        });
        jpackageExtension.getLinux().getPackageName().convention(jpackageExtension.getCommon().getPackageName());
        jpackageExtension.getMac().getPackageName().convention(jpackageExtension.getCommon().getPackageName());
        jpackageExtension.getLinux().getShortcut().convention(jpackageExtension.getCommon().getShortcut());
        jpackageExtension.getWindows().getShortcut().convention(jpackageExtension.getCommon().getShortcut());
        jpackageExtension.getLauncher().getLauncherAsService().convention(false);

        project.getPluginManager().withPlugin("java", javaPlugin -> {
            final JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            final var toolFactory = project.getObjects().newInstance(ToolFactory.class);
            javaExtension.getSourceSets().named("main").configure(s -> {
                final String moduleName = Modules.sourceFileModuleName(project.getLayout().getProjectDirectory().file("src/main/java/module-info.java").getAsFile());
                if (Objects.nonNull(moduleName)) jpackageExtension.getLauncher().getMainModule().convention(moduleName);
                final FileCollection modules = s.getRuntimeClasspath().filter(Modules::isModule);
                final FileCollection nonModules = s.getRuntimeClasspath().minus(modules);

                final var mainJar = project.getTasks().named(s.getJarTaskName(), Jar.class).flatMap(AbstractArchiveTask::getArchiveFile);
                final var jdepsTask = project.getTasks().register(s.getTaskName("find", "moduleDeps"), JdepsTask.class, task -> {
                    task.setGroup("application");
                    task.setDescription("Analyzes the source set for required modules");
                    task.getJdepsTool().convention(toolFactory.getJdepsToolFor(javaExtension.getToolchain()));
                    final var modulePath = project.getConfigurations().named(s.getRuntimeClasspathConfigurationName()).map(c -> c.filter(Modules::isModule));
                    task.getModulePath().from(modulePath);
                    final var classpath = project.getConfigurations().named(s.getRuntimeClasspathConfigurationName()).zip(modulePath, FileCollection::minus);
                    task.getClassPath().from(classpath);
                    task.getSource().from(mainJar);
                    task.getRecursive().convention(true);
                    task.getPrintModuleDeps().convention(true);
                    task.getIgnoreMissingDeps().convention(true);
                    task.getMultiRelease().convention(javaExtension.getToolchain().getLanguageVersion());
                    task.getDestinationFile().convention(project.getLayout().getBuildDirectory().file("jdeps/" + s.getName() + "/jdeps-result.txt"));
                });

                final var jlinkTask = project.getTasks().register(s.getTaskName("generate", "runtimeImage"), JlinkTask.class, task -> {
                    task.setGroup("application");
                    task.setDescription("Run Jlink to generate a customized runtime image");
                    task.getJlinkTool().convention(toolFactory.getJlinkToolFor(javaExtension.getToolchain()));
                    final Provider<@NonNull Set<FileSystemLocation>> reduced = s.getRuntimeClasspath().filter(File::isFile).getElements().zip(jlinkElementsConfig.get().getElements(), (elements, jmods) -> {
                        if (jmods.isEmpty()) return elements;
                        final Set<String> jmodNames = jmods.stream().map(f -> Modules.moduleName(f.getAsFile())).collect(Collectors.toSet());
                        return elements.stream()
                            .filter(e -> !jmodNames.contains(Modules.moduleName(e.getAsFile())))
                            .collect(Collectors.toSet());
                    });
                    task.getModulePath().from(mainJar, jlinkElementsConfig.get(), reduced);
                    task.getAddModules().add(jpackageExtension.getLauncher().getMainModule().orElse(jdepsTask.flatMap(JdepsTask::getResult)));
                    task.getNoHeaderFiles().convention(true);
                    task.getNoManPages().convention(true);
                    task.getStripDebug().convention(true);
                    task.getStripNativeCommands().convention(true);
                    task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jlink/runtime"));
                });

                project.getPluginManager().withPlugin("application", p -> {
                    final JavaApplication application = project.getExtensions().getByType(JavaApplication.class);
                    if (Objects.nonNull(moduleName)) application.getMainModule().convention(moduleName);
                    jpackageExtension.getLauncher().getMainModule().convention(application.getMainModule());
                    jpackageExtension.getLauncher().getMainClass().convention(application.getMainClass());
                    jpackageExtension.getMetadata().getName().convention(project.getProviders().provider(application::getApplicationName));
                    jpackageExtension.getLauncher().getJavaOptions().convention(project.getProviders().provider(application::getApplicationDefaultJvmArgs));

                    final var prepareInputTask = project.getTasks().register(s.getTaskName("copy", "jpackageInput"), PrepareInputTask.class, task -> {
                        task.getSource().from(nonModules);
                        task.getMainJar().convention(mainJar);
                        task.getModule().convention(application.getMainModule());
                        task.getDestination().convention(project.getLayout().getBuildDirectory().dir("jpackage/input"));
                    });

                    project.getTasks().withType(JpackageTask.class, task -> {
                        task.getJpackageTool().convention(toolFactory.getJpackageToolFor(javaExtension.getToolchain()));
                        task.getAppName().convention(jpackageExtension.getMetadata().getName());
                        task.getAppDescription().convention(jpackageExtension.getMetadata().getDescription());
                        task.getAppVersion().convention(jpackageExtension.getMetadata().getVersion());
                        task.getIcon().convention(jpackageExtension.getMetadata().getIcon());
                        task.getCopyright().convention(jpackageExtension.getMetadata().getCopyright());
                        task.getVendor().convention(jpackageExtension.getMetadata().getVendor());
                    });

                    final var appImageTask = project.getTasks().register("appImage", JpackageTask.class, task -> {
                        task.setGroup("application");
                        task.setDescription("Generates a native app image");
                        task.getType().convention("app-image");
                        task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
                        task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                        task.getInput().convention(prepareInputTask.flatMap(PrepareInputTask::getDestination));
                        task.getMainJar().convention(mainJar);
                        task.getMainModule().convention(jpackageExtension.getLauncher().getMainModule());
                        task.getMainClass().convention(jpackageExtension.getLauncher().getMainClass());
                        task.getAppContent().from(jpackageExtension.getCommon().getContent());
                        jpackageExtension.getLauncher().getLaunchers().all(it -> task.getAdditionalLaunchers().add(it));
                        task.getArguments().convention(jpackageExtension.getLauncher().getArguments());
                        task.getJavaOptions().convention(jpackageExtension.getLauncher().getJavaOptions());
                        Provider<@NonNull JpackagePlatformOptions> platformOptions = project.getProviders().systemProperty("os.name").map(os -> {
                            if (Platform.isWindows(os)) {
                                final JpackageWindowsOptions win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                                win.getWinConsole().convention(jpackageExtension.getCommon().getIsCommandLineApplication());
                                return win;
                            } else if (Platform.isMac(os)) {
                                final JpackageMacOSOptions mac = project.getObjects().newInstance(JpackageMacOSOptions.class);
                                mac.getMacAppCategory().convention(jpackageExtension.getMac().getAppCategory());
                                mac.getMacPackageName().convention(jpackageExtension.getMac().getPackageName());
                                mac.getMacPackageIdentifier().convention(jpackageExtension.getMac().getPackageID());
                                return mac;
                            } else {
                                return project.getObjects().newInstance(JpackageLinuxOptions.class);
                            }
                        });
                        task.getPlatformOptions().convention(platformOptions);
                    });

                    project.getTasks().register("appInstaller", JpackageTask.class, task -> {
                        task.setGroup("application");
                        task.setDescription("Generates a native app installer");
                        task.getType().convention(project.getProviders().systemProperty("os.name").flatMap(os -> {
                            if (Platform.isWindows(os))
                                return jpackageExtension.getWindows().getInstallerType().map(WindowsHandler.InstallerType::toString);
                            if (Platform.isMac(os))
                                return jpackageExtension.getMac().getInstallerType().map(MacHandler.InstallerType::toString);
                            return jpackageExtension.getLinux().getInstallerType().map(LinuxHandler.InstallerType::toString);
                        }));

                        final Provider<@NonNull String> appImageName = jpackageExtension.getMetadata().getName().zip(project.getProviders().systemProperty("os.name"), (name, os) -> Platform.isMac(os) ? name + ".app" : name);
                        final Provider<@NonNull Directory> appImageProvider = appImageTask.flatMap(t -> t.getDest().dir(appImageName));
                        task.getApplicationImage().convention(appImageProvider);
                        task.getAboutURL().convention(jpackageExtension.getMetadata().getAboutUrl());
                        task.getLicenseFile().convention(jpackageExtension.getMetadata().getLicenseFile());
                        task.getFileAssociations().convention(jpackageExtension.getCommon().getFileAssociations());
                        task.getInstallDir().convention(jpackageExtension.getCommon().getInstallDir());
                        task.getResourceDir().convention(jpackageExtension.getCommon().getResourceDir());
                        Provider<@NonNull JpackagePlatformOptions> platformOptions = project.getProviders().systemProperty("os.name").map(os -> {
                            if (Platform.isWindows(os)) {
                                final JpackageWindowsOptions win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                                win.getWinShortcut().convention(jpackageExtension.getWindows().getShortcut());
                                return win;
                            } else if (Platform.isMac(os)) {
                                final JpackageMacOSOptions mac = project.getObjects().newInstance(JpackageMacOSOptions.class);
                                mac.getMacDMGContent().convention(jpackageExtension.getMac().getDmgContent());
                                return mac;
                            } else {
                                final JpackageLinuxOptions linux = project.getObjects().newInstance(JpackageLinuxOptions.class);
                                linux.getLinuxPackageName().convention(jpackageExtension.getLinux().getPackageName());
                                linux.getLinuxShortcut().convention(jpackageExtension.getLinux().getShortcut());
                                return linux;
                            }
                        });
                        task.getPlatformOptions().convention(platformOptions);
                        jpackageExtension.getLauncher().getLaunchers().all(it -> task.getAdditionalLaunchers().add(it));
                        task.getLauncherAsService().convention(jpackageExtension.getLauncher().getLauncherAsService());
                        task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/install"));
                    });
                });
            });
        });
    }
}
