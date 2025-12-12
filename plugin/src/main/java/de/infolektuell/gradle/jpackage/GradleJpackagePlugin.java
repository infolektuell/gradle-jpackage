package de.infolektuell.gradle.jpackage;

import de.infolektuell.gradle.jpackage.extensions.*;
import de.infolektuell.gradle.jpackage.tasks.*;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageLinuxOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageMacOSOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackagePlatformOptions;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackageWindowsOptions;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gradle plugin that creates native application installers using Jpackage
 */
public abstract class GradleJpackagePlugin implements Plugin<@NonNull Project> {
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
    public void apply(Project project) {
        final JpackageExtension jpackageExtension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        final LauncherHandler launcher = jpackageExtension.getLauncher();
        jpackageExtension.metadata(data -> {
            data.getName().convention(project.getName());
            data.getVersion().convention(project.getVersion().toString());
            data.getDescription().convention(project.getDescription());
            data.getVendor().convention(project.getGroup().toString());
        });
        jpackageExtension.getLinux().getPackageName().convention(jpackageExtension.getPackageName());
        jpackageExtension.getMac().getPackageName().convention(jpackageExtension.getPackageName());
        jpackageExtension.getLinux().getShortcut().convention(jpackageExtension.getShortcut());
        jpackageExtension.getWindows().getShortcut().convention(jpackageExtension.getShortcut());
        jpackageExtension.getLauncher().getLauncherAsService().convention(false);

        final Provider<@NonNull String> osName = project.getProviders().systemProperty("os.name");
        project.getPluginManager().withPlugin("java", javaPlugin -> {
            final JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.manifest().getAttributes().put("Main-Class", jpackageExtension.getLauncher().getMainClass());
            java.getSourceSets().configureEach(s -> {
                final SourceSetExtension patchModule = s.getExtensions().create(SourceSetExtension.EXTEnSION_NAME, SourceSetExtension.class, project.getObjects());
                project.getTasks().withType(JavaCompile.class).named(s.getCompileTaskName("Java"), task -> task.getOptions().getCompilerArgumentProviders().add(patchModule));
            });

            project.getTasks().withType(JDKToolTask.class, task -> {
                final Provider<@NonNull JavaInstallationMetadata> metadata = getJavaToolchainService().launcherFor(java.getToolchain())
                    .orElse(getJavaToolchainService().launcherFor(spec -> {
                    }))
                    .map(JavaLauncher::getMetadata);
                task.getMetadata().convention(metadata);
            });

            final TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
            final Provider<@NonNull RegularFile> mainJar = jarTask.flatMap(AbstractArchiveTask::getArchiveFile);

            final Provider<@NonNull Modularity> modularity = project.getProviders().provider(() -> {
                if (launcher.getMainModule().isPresent()) {
                    final Modular modular = project.getObjects().newInstance(Modular.class);
                    modular.getMainModule().convention(launcher.getMainModule());
                    modular.getMainClass().convention(launcher.getMainClass());
                    return modular;
                } else {
                    final NonModular nonModular = project.getObjects().newInstance(NonModular.class);
                    nonModular.getMainJar().convention(mainJar);
                    nonModular.getMainClass().convention(launcher.getMainClass());
                    return nonModular;
                }
            });

            final TaskProvider<@NonNull JdepsTask> jdepsTask = project.getTasks().register("jdeps", JdepsTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Analyzes the project for required modules");
                task.getModularity().convention(modularity);
                task.getRecursive().convention(true);
                task.getPrintModuleDeps().convention(true);
                task.getIgnoreMissingDeps().convention(true);
                task.getMultiRelease().convention(java.getToolchain().getLanguageVersion());
                task.getDestinationFile().convention(project.getLayout().getBuildDirectory().file("jpackage/jdeps/jdeps-result.txt"));
            });

            final Provider<@NonNull String> modulesProvider = jdepsTask.flatMap(task -> task.getDestinationFile().map(f -> {
                try {
                    return Files.readString(f.getAsFile().toPath()).trim();
                } catch (Exception ignored) {
                    return "ALL-MODULE-PATH";
                }
            }));

            final TaskProvider<@NonNull JlinkTask> jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a customized runtime image");
                task.getAddModules().add(jpackageExtension.getLauncher().getMainModule().orElse(modulesProvider));
                task.getNoHeaderFiles().convention(true);
                task.getNoManPages().convention(true);
                task.getStripDebug().convention(true);
                task.getStripNativeCommands().convention(true);
                task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jpackage/runtime"));
            });

            final TaskProvider<@NonNull PrepareInputTask> prepareInputTask = project.getTasks().register("prepareInput", PrepareInputTask.class, task -> {
                task.getModularity().convention(modularity);
                task.getDestination().convention(project.getLayout().getBuildDirectory().dir("jpackage/input"));
            });

            project.getTasks().withType(JpackageTask.class, task -> {
                task.getAppName().convention(jpackageExtension.getMetadata().getName());
                task.getAppDescription().convention(jpackageExtension.getMetadata().getDescription());
                task.getAppVersion().convention(jpackageExtension.getMetadata().getVersion());
                task.getIcon().convention(jpackageExtension.getMetadata().getIcon());
                task.getCopyright().convention(jpackageExtension.getMetadata().getCopyright());
                task.getVendor().convention(jpackageExtension.getMetadata().getVendor());
            });

            final TaskProvider<@NonNull JpackageTask> appImageTask = project.getTasks().register("appImage", JpackageTask.class, task -> {
                task.setGroup("application");
                task.setDescription("Generates a native app image");
                task.getType().convention("app-image");
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
                task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
                task.getInput().convention(prepareInputTask.flatMap(PrepareInputTask::getDestination));
                task.getAppContent().from(jpackageExtension.getContent());
                task.getModularity().convention(modularity);
                launcher.getLaunchers().all(it -> task.getAdditionalLaunchers().add(it));
                task.getArguments().convention(jpackageExtension.getLauncher().getArguments());
                task.getJavaOptions().convention(jpackageExtension.getLauncher().getJavaOptions());
                Provider<@NonNull JpackagePlatformOptions> platformOptions = osName.map(os -> {
                    if (isWindows(os)) {
                        return project.getObjects().newInstance(JpackageWindowsOptions.class);
                    } else if (isMac(os)) {
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
                task.getType().convention(osName.flatMap(os -> {
                    if (isWindows(os))
                        return jpackageExtension.getWindows().getInstallerType().map(WindowsHandler.InstallerType::toString);
                    if (isMac(os))
                        return jpackageExtension.getMac().getInstallerType().map(MacHandler.InstallerType::toString);
                    return jpackageExtension.getLinux().getInstallerType().map(LinuxHandler.InstallerType::toString);
                }));

                final Provider<@NonNull String> appImageName = jpackageExtension.getMetadata().getName().zip(osName, (name, os) -> isMac(os) ? name + ".app" : name);
                final Provider<@NonNull Directory> appImageProvider = appImageTask.flatMap(t -> t.getDest().dir(appImageName));
                task.getApplicationImage().convention(appImageProvider);
                task.getAboutURL().convention(jpackageExtension.getMetadata().getAboutUrl());
                task.getLicenseFile().convention(jpackageExtension.getMetadata().getLicenseFile());
                task.getFileAssociations().convention(jpackageExtension.getFileAssociations());
                task.getInstallDir().convention(jpackageExtension.getInstallDir());
                task.getResourceDir().convention(jpackageExtension.getResourceDir());
                Provider<@NonNull JpackagePlatformOptions> platformOptions = osName.map(os -> {
                    if (isWindows(os)) {
                        final JpackageWindowsOptions win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                        win.getWinShortcut().convention(jpackageExtension.getWindows().getShortcut());
                        return win;
                    } else if (isMac(os)) {
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
                launcher.getLaunchers().all(it -> task.getAdditionalLaunchers().add(it));
                task.getLauncherAsService().convention(jpackageExtension.getLauncher().getLauncherAsService());
                task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/install"));
            });

            java.getSourceSets().named("main", main -> {
                final ConfigurableFileCollection classpath = project.getObjects().fileCollection();
                classpath.from(mainJar, main.getRuntimeClasspath().filter(File::isFile));
                final FileCollection modulePath = classpath.filter(this::isModule);
                final FileCollection nonModulePath = classpath.filter(f -> !isModule(f));
                jdepsTask.configure(task -> {
                    task.getModulePath().from(modulePath);
                    task.getClassPath().from(nonModulePath);
                });
                jlinkTask.configure(task -> task.getModulePath().from(modulePath));
                prepareInputTask.configure(task -> task.getSource().from(nonModulePath));
            });
        });

        project.getPluginManager().withPlugin("application", p -> {
            final JavaApplication application = project.getExtensions().getByType(JavaApplication.class);
            jpackageExtension.getMetadata().getName().convention(application.getApplicationName());
            launcher.getMainModule().convention(application.getMainModule());
            launcher.getMainClass().convention(application.getMainClass());
            launcher.getJavaOptions().convention(application.getApplicationDefaultJvmArgs());
        });
    }

    private Boolean isWindows(String osName) {
        final Pattern pattern = Pattern.compile("windows", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(osName);
        return matcher.find();
    }

    private Boolean isMac(String osName) {
        final Pattern pattern = Pattern.compile("mac", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(osName);
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
            if (Files.readAllLines(manifestFile).stream().anyMatch(l -> l.contains("Automatic-Module-Name")))
                return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
