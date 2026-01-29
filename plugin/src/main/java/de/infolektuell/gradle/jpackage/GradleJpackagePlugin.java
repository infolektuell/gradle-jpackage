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
import de.infolektuell.gradle.jpackage.tasks.providers.ModulePathProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.JavaExec;
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
    protected abstract JavaToolchainService getJavaToolchainService();

    @Override
    public void apply(Project project) {
        final Provider<@NonNull String> osName = project.getProviders().systemProperty("os.name");
        final JpackageExtension jpackageExtension = project.getExtensions().create(JpackageExtension.EXTENSION_NAME, JpackageExtension.class);
        final LauncherHandler launcher = jpackageExtension.getLauncher();
        final ModulePathProvider modulePathProvider = project.getObjects().newInstance(ModulePathProvider.class);

        jpackageExtension.metadata(data -> {
            data.getName().convention(project.getName());
            data.getVersion().convention(project.getVersion().toString());
            data.getDescription().convention(project.getDescription());
            data.getVendor().convention(project.getGroup().toString());
        });
        jpackageExtension.getLinux().getPackageName().convention(jpackageExtension.getCommon().getPackageName());
        jpackageExtension.getMac().getPackageName().convention(jpackageExtension.getCommon().getPackageName());
        jpackageExtension.getLinux().getShortcut().convention(jpackageExtension.getCommon().getShortcut());
        jpackageExtension.getWindows().getShortcut().convention(jpackageExtension.getCommon().getShortcut());
        jpackageExtension.getLauncher().getLauncherAsService().convention(false);

        final TaskProvider<@NonNull JdepsTask> jdepsTask = project.getTasks().register("jdeps", JdepsTask.class, task -> {
            task.setGroup("application");
            task.setDescription("Analyzes the project for required modules");
            task.getRecursive().convention(true);
            task.getPrintModuleDeps().convention(true);
            task.getIgnoreMissingDeps().convention(true);
            task.getDestinationFile().convention(project.getLayout().getBuildDirectory().file("jpackage/jdeps/jdeps-result.txt"));
        });

        final Provider<@NonNull String> jdepsOutput = jdepsTask.flatMap(JdepsTask::getDestinationFile).map(f -> {
            try {
                return Files.readString(f.getAsFile().toPath()).trim();
            } catch (Exception ignored) {
                return "ALL_MODULE_PATH";
            }
        });
        final Provider<@NonNull String> modules = launcher.getMainModule().orElse(jdepsOutput);

        final TaskProvider<@NonNull JlinkTask> jlinkTask = project.getTasks().register("jlink", JlinkTask.class, task -> {
            task.setGroup("application");
            task.setDescription("Generates a customized runtime image");
            task.getModulePath().from(modulePathProvider.getModulePath());
            task.getAddModules().add(modules);
            task.getNoHeaderFiles().convention(true);
            task.getNoManPages().convention(true);
            task.getStripDebug().convention(true);
            task.getStripNativeCommands().convention(true);
            task.getOutput().convention(project.getLayout().getBuildDirectory().dir("jpackage/runtime"));
        });

        final TaskProvider<@NonNull PrepareInputTask> prepareInputTask = project.getTasks().register("prepareInput", PrepareInputTask.class, task -> {
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
            task.getVerbose().convention(project.getLogger().isInfoEnabled());
            task.getType().convention("app-image");
            task.getDest().convention(project.getLayout().getBuildDirectory().dir("jpackage/image"));
            task.getRuntimeImage().convention(jlinkTask.flatMap(JlinkTask::getOutput));
            task.getInput().convention(prepareInputTask.flatMap(PrepareInputTask::getDestination));
            task.getAppContent().from(jpackageExtension.getCommon().getContent());
            launcher.getLaunchers().all(it -> task.getAdditionalLaunchers().add(it));
            task.getArguments().convention(jpackageExtension.getLauncher().getArguments());
            task.getJavaOptions().convention(jpackageExtension.getLauncher().getJavaOptions());
            Provider<@NonNull JpackagePlatformOptions> platformOptions = osName.map(os -> {
                if (isWindows(os)) {
                    final JpackageWindowsOptions win = project.getObjects().newInstance(JpackageWindowsOptions.class);
                    win.getWinConsole().convention(jpackageExtension.getCommon().getIsCommandLineApplication());
                    return win;
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
            task.getVerbose().convention(project.getLogger().isInfoEnabled());
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
            task.getFileAssociations().convention(jpackageExtension.getCommon().getFileAssociations());
            task.getInstallDir().convention(jpackageExtension.getCommon().getInstallDir());
            task.getResourceDir().convention(jpackageExtension.getCommon().getResourceDir());
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

        project.getPluginManager().withPlugin("java", javaPlugin -> {
            final JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            javaExtension.manifest().getAttributes().put("Main-Class", jpackageExtension.getLauncher().getMainClass());
            javaExtension.getSourceSets().configureEach(s -> {
                final SourceSetExtension patchModule = s.getExtensions().create(SourceSetExtension.EXTEnSION_NAME, SourceSetExtension.class, project.getObjects());
                project.getTasks().withType(JavaCompile.class).named(s.getCompileTaskName("Java"), task -> task.getOptions().getCompilerArgumentProviders().add(patchModule));
            });

            project.getTasks().withType(JDKToolTask.class, task -> {
                final Provider<@NonNull JavaInstallationMetadata> metadata = getJavaToolchainService().launcherFor(javaExtension.getToolchain())
                    .orElse(getJavaToolchainService().launcherFor(spec -> {
                    }))
                    .map(JavaLauncher::getMetadata);
                task.getMetadata().convention(metadata);
            });

            final TaskProvider<@NonNull Jar> jarTask = project.getTasks().withType(Jar.class).named("jar");
            final Provider<@NonNull RegularFile> mainJar = jarTask.flatMap(AbstractArchiveTask::getArchiveFile);
            modulePathProvider.getClasspath().from(mainJar, javaExtension.getSourceSets().named("main").map(s -> s.getRuntimeClasspath().filter(File::isFile)));

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

            jdepsTask.configure(task -> {
                task.getModulePath().from(modulePathProvider.getModulePath());
                task.getClassPath().from(modulePathProvider.getNonModulePath());
                task.getModularity().convention(modularity);
                task.getMultiRelease().convention(javaExtension.getToolchain().getLanguageVersion());
            });

            prepareInputTask.configure(task -> {
                task.getSource().from(modulePathProvider.getNonModulePath());
                task.getModularity().convention(modularity);
            });
            appImageTask.configure(task -> {
                task.getModularity().convention(modularity);
            });
        });

        project.getPluginManager().withPlugin("application", p -> {
            final JavaApplication application = project.getExtensions().getByType(JavaApplication.class);
            jpackageExtension.getMetadata().getName().convention(application.getApplicationName());
            launcher.getMainModule().convention(application.getMainModule());
            launcher.getMainClass().convention(application.getMainClass());
            project.getTasks().withType(JavaExec.class).named("run").configure(task -> {
                launcher.getJavaOptions().convention(application.getApplicationDefaultJvmArgs());
                task.setClasspath(modulePathProvider.getNonModulePath());
                task.getJvmArgumentProviders().add(modulePathProvider);
                task.getJvmArguments().add("--add-modules");
                task.getJvmArguments().add(modules);
                task.getJvmArguments().addAll(launcher.getJavaOptions());
            });
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
}
