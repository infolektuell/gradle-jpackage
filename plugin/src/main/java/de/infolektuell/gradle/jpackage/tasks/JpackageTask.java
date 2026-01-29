package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import de.infolektuell.gradle.jpackage.tasks.platform.JpackagePlatformOptions;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.regex.Pattern;

@CacheableTask
public abstract class JpackageTask extends JDKToolTask {
    private static final Pattern versionPattern = Pattern.compile("^\\d+([.]\\d+){0,2}$");

    private static Boolean isVersion(String version) {
        var matcher = versionPattern.matcher(version);
        return matcher.find();
    }

    private final NamedDomainObjectSet<@NonNull Launcher> additionalLaunchers;

    public JpackageTask() {
        super();
        this.additionalLaunchers = getObjects().domainObjectContainer(Launcher.class);
    }

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    // Generic Options:

    /**
     * Arg files that contain additional options to be passed to jpackage
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ListProperty<@NonNull RegularFile> getArgFiles();

    @Optional
    @Input
    public abstract Property<@NonNull String> getType();

    /** Version of the application and/or package */
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppVersion();

    /** Copyright for the application */
    @Optional
    @Input
    public abstract Property<@NonNull String> getCopyright();

    /** Description of the application */
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppDescription();

    /**
     *         Path of the icon of the application package
     *             (absolute path or relative to the current directory)
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getIcon();

    /** Name of the application and/or package */
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppName();

    /**
     *         Path where generated output file is placed
     *         (absolute path or relative to the current directory)
     *         Defaults to the current working directory.
     */
    @OutputDirectory
    public abstract DirectoryProperty getDest();

    /** Vendor of the application */
    @Optional
    @Input
    public abstract Property<@NonNull String> getVendor();

    /** Enables verbose output */
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getVerbose();

    /**
     *           Path of the predefined runtime image that will be copied into the application image
     *           (absolute path or relative to the current directory)
     *           If --runtime-image is not specified, jpackage will run jlink to create the runtime image using options:
     *           --strip-debug, --no-header-files, --no-man-pages, and --strip-native-commands.
     *           Option is required when creating a runtime package.
     */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    // Options for creating the application image

    /**
     *           Path of the input directory that contains the files to be packaged
     *           (absolute path or relative to the current directory)
     *           All files in the input directory will be packaged into the application image.
     */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInput();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getAppContent();

    // Options for creating the application launcher(s):

    @Input
    public NamedDomainObjectSet<@NonNull Launcher> getAdditionalLaunchers() { return additionalLaunchers; }

    /**
     *           Command line arguments to pass to the main class if no command line arguments are given to the launcher
     *           This option can be used multiple times.
     */
    @Optional
    @Input
    public abstract ListProperty<@NonNull String> getArguments();

    /**
     *           Options to pass to the Java runtime
     *           This option can be used multiple times.
     */
    @Optional
    @Input
    public abstract ListProperty<@NonNull String> getJavaOptions();

    @Optional
    @Nested
    public abstract Property<@NonNull Modularity> getModularity();

    @Optional
    @Nested
    public abstract Property<@NonNull JpackagePlatformOptions> getPlatformOptions();

    // Options for creating the application package

    /** URL of the application's home page */
    @Optional
    @Input
    public abstract Property<@NonNull String> getAboutURL();

    /** Location of the predefined application image that is used to build an installable package or to sign the predefined application image */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getApplicationImage();

    @Optional
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract SetProperty<@NonNull RegularFile> getFileAssociations();

    @Option(option = "install-dir", description = "Absolute path of the installation directory of the application")
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInstallDir();

    /**
     *           Path to the license file
     *           (absolute path or relative to the current directory)
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getLicenseFile();

    /**
     *           Path to override jpackage resources
     *           Icons, template files, and other resources of jpackage can be over-ridden by adding replacement resources to this directory.
     *           (absolute path or relative to the current directory)
     */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getResourceDir();

    /** Request to create an installer that will register the main application launcher as a background service-type application. */
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getLauncherAsService();

    @TaskAction
    protected void jpackage() {
        getFileSystemOperations().delete(spec -> spec.delete(getDest()));

        exec("jpackage", spec -> {
            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));
            if (getType().isPresent()) spec.args("--type", getType().get());
            if (getAppVersion().isPresent() && isVersion(getAppVersion().get()))
                spec.args("--app-version", getAppVersion().get());
            if (getCopyright().isPresent()) spec.args("--copyright", getCopyright().get());
            if (getAppDescription().isPresent()) spec.args("--description", getAppDescription().get());
            if (getIcon().isPresent()) spec.args("--icon", getIcon().get());
            if (getAppName().isPresent()) spec.args("--name", getAppName().get());
            spec.args("--dest", getDest().get());
            if (getVendor().isPresent()) spec.args("--vendor", getVendor().get());
            if (getVerbose().getOrElse(false)) spec.args("--verbose");
            if (getRuntimeImage().isPresent()) spec.args("--runtime-image", getRuntimeImage().get());
            if (getInput().isPresent()) spec.args("--input", getInput().get());
            getAppContent().forEach(f -> spec.args("--app-content", f.getAbsolutePath()));

            getAdditionalLaunchers().forEach(launcher -> spec.args("--add-launcher", String.join("=", launcher.getName(), launcher.getFile().get().getAsFile().getAbsolutePath())));
            if (getArguments().isPresent()) getArguments().get().forEach(a -> spec.args("--arguments", a));
            if (getJavaOptions().isPresent()) getJavaOptions().get().forEach(a -> spec.args("--java-options", a));
            switch (getModularity().getOrNull()) {
                case Modular modular ->
                    spec.args("--module", String.join("/", modular.getMainModule().get(), modular.getMainClass().get()));
                case NonModular nonModular -> {
                    spec.args("--main-class", nonModular.getMainClass().get());
                    spec.args("--main-jar", nonModular.getMainJar().get().getAsFile().getName());
                }
                case null -> {
                }
            }

            if (getApplicationImage().isPresent()) spec.args("--app-image", getApplicationImage().get());
            if (getAboutURL().isPresent()) spec.args("--about-url", getAboutURL().get());
            getFileAssociations().get().forEach(f -> spec.args("--file-associations", f));
            if (getInstallDir().isPresent()) spec.args("--install-dir", getInstallDir().get());
            if (getLicenseFile().isPresent()) spec.args("--license-file", getLicenseFile().get());
            if (getResourceDir().isPresent()) spec.args("--resource-dir", getResourceDir().get());
            if (getLauncherAsService().getOrElse(false)) spec.args("--launcher-as-service");

            if (getPlatformOptions().isPresent()) spec.getArgumentProviders().add(getPlatformOptions().get());
            if (getVerbose().getOrElse(false)) spec.args("--verbose");
        });
    }
}
