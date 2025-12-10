package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.*;
import de.infolektuell.gradle.jpackage.tasks.platform.*;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.*;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.regex.Pattern;

public abstract class JpackageTask extends JDKToolTask {
    private static final Pattern versionPattern = Pattern.compile("^\\d+([.]\\d+){0,2}$");

    private static Boolean isVersion(String version) {
        var matcher = versionPattern.matcher(version);
        return matcher.find();
    }

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

    @Option(option = "app-version", description = "Version of the application and/or package")
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppVersion();

    @Option(option = "copyright", description = "Copyright for the application")
    @Optional
    @Input
    public abstract Property<@NonNull String> getCopyright();

    @Option(option = "description", description = "Description of the application")
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppDescription();

    @Option(option = "icon", description = """
        Path of the icon of the application package
            (absolute path or relative to the current directory)""\")
        """)
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getIcon();

    @Option(option = "name", description = "Name of the application and/or package")
    @Optional
    @Input
    public abstract Property<@NonNull String> getAppName();

    @Option(option = "dest", description = """
        Path where generated output file is placed
        (absolute path or relative to the current directory)
        Defaults to the current working directory.
        """)
    @OutputDirectory
    public abstract DirectoryProperty getDest();

    @Option(option = "vendor", description = "Vendor of the application")
    @Optional
    @Input
    public abstract Property<@NonNull String> getVendor();

    @Option(option = "verbose", description = "Enables verbose output")
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getVerbose();

    @Option(option = "runtime-image", description = """
          Path of the predefined runtime image that will be copied into the application image
          (absolute path or relative to the current directory)
          If --runtime-image is not specified, jpackage will run jlink to create the runtime image using options:
          --strip-debug, --no-header-files, --no-man-pages, and --strip-native-commands.
          Option is required when creating a runtime package.
        """)
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    // Options for creating the application image
    @Option(option = "input", description = """
          Path of the input directory that contains the files to be packaged
          (absolute path or relative to the current directory)
          All files in the input directory will be packaged into the application image.
        """)
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInput();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getAppContent();

    // Options for creating the application launcher(s):
    @Input
    public abstract NamedDomainObjectSet<@NonNull Launcher> getAdditionalLaunchers();

    @Option(option = "arguments", description = """
          Command line arguments to pass to the main class if no command line arguments are given to the launcher
          This option can be used multiple times.
        """)
    @Optional
    @Input
    public abstract ListProperty<@NonNull String> getArguments();

    @Option(option = "java-options", description = """
          Options to pass to the Java runtime
          This option can be used multiple times.
        """)
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
    @Option(option = "about-url", description = "URL of the application's home page")
    @Optional
    @Input
    public abstract Property<@NonNull String> getAboutURL();

    @Option(option = "app-image", description = "Location of the predefined application image that is used to build an installable package or to sign the predefined application image")
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

    @Option(option = "license-file", description = """
          Path to the license file
          (absolute path or relative to the current directory)
        """)
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getLicenseFile();

    @Option(option = "resource-dir", description = """
          Path to override jpackage resources
          Icons, template files, and other resources of jpackage can be over-ridden by adding replacement resources to this directory.
          (absolute path or relative to the current directory)
        """)
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getResourceDir();

    @Option(option = "launcher-as-service", description = "Request to create an installer that will register the main application launcher as a background service-type application.")
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getLauncherAsService();

    @TaskAction
    protected void jpackage() {
        getFileSystemOperations().delete(spec -> spec.delete(getDest()));

        exec("jpackage", spec -> {
            getArgFiles().get().forEach(f -> spec.args("@" + f.getAsFile().getAbsolutePath()));
            if (getType().isPresent()) spec.args("--type", getType().get());
            if (getAppVersion().isPresent() && isVersion(getAppVersion().get())) spec.args("--app-version", getAppVersion().get());
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
                case Modular modular -> spec.args("--module", modular.getMainModule().get());
                case NonModular nonModular -> {
                    spec.args("--main-class", nonModular.getMainClass().get());
                    spec.args("--main-jar", nonModular.getMainJar().get().getAsFile().getName());
                }
                case null -> {}
            }

            if (getApplicationImage().isPresent()) spec.args("--app-image", getApplicationImage().get());
            if (getAboutURL().isPresent()) spec.args("--about-url", getAboutURL().get());
            getFileAssociations().get().forEach(f -> spec.args("--file-associations", f));
            if (getInstallDir().isPresent()) spec.args("--install-dir", getInstallDir().get());
            if (getLicenseFile().isPresent()) spec.args("--license-file", getLicenseFile().get());
            if (getResourceDir().isPresent()) spec.args("--resource-dir", getResourceDir().get());
            if (getLauncherAsService().getOrElse(false)) spec.args("--launcher-as-service");

            switch (getPlatformOptions().getOrNull()) {
                case JpackageLinuxOptions linux -> {
                    if (linux.getLinuxPackageName().isPresent()) spec.args("--linux-package-name", linux.getLinuxPackageName().get());
                    if (linux.getLinuxDebMaintainer().isPresent()) spec.args("--linux-deb-maintainer", linux.getLinuxDebMaintainer().get());
                    if (linux.getLinuxMenuGroup().isPresent()) spec.args("--linux-menu-group", linux.getLinuxMenuGroup().get());
                    if (linux.getLinuxPackageDeps().isPresent()) spec.args("--linux-package-deps", linux.getLinuxPackageDeps().get());
                    if (linux.getLinuxRPMLicenseType().isPresent()) spec.args("--linux-rpm-license-type", linux.getLinuxRPMLicenseType().get());
                    if (linux.getLinuxAppRelease().isPresent()) spec.args("--linux-app-release", linux.getLinuxAppRelease().get());
                    if (linux.getLinuxAppCategory().isPresent()) spec.args("--linux-app-category", linux.getLinuxAppCategory().get());
                    if (linux.getLinuxShortcut().getOrElse(false)) spec.args("--linux-shortcut");
                }
                case JpackageMacOSOptions mac -> {
                    if (mac.getMacPackageIdentifier().isPresent()) spec.args("--mac-package-identifier", mac.getMacPackageIdentifier().get());
                    if (mac.getMacPackageName().isPresent()) spec.args("--mac-package-name", mac.getMacPackageName().get());
                    if (mac.getMacPackageSigningPrefix().isPresent()) spec.args("--mac-package-signing-prefix", mac.getMacPackageSigningPrefix().get());
                    if (mac.getMacSign().getOrElse(false)) spec.args("--mac-sign");
                    if (mac.getMacSigningKeychain().isPresent()) spec.args("--mac-signing-keychain", mac.getMacSigningKeychain().get());
                    if (mac.getMacSigningKeyUserName().isPresent()) spec.args("--mac-signing-key-user-name", mac.getMacSigningKeyUserName().get());
                    if (mac.getMacAppImageSignIdentity().isPresent()) spec.args("--mac-app-image-sign-identity", mac.getMacAppImageSignIdentity().get());
                    if (mac.getMacInstallerSignIdentity().isPresent()) spec.args("--mac-installer-sign-identity", mac.getMacInstallerSignIdentity().get());
                    if (mac.getMacAppStore().isPresent()) spec.args("--mac-app-store");
                    if (mac.getMacEntitlements().isPresent()) spec.args("--mac-entitlements", mac.getMacEntitlements().get());
                    if (mac.getMacAppCategory().isPresent()) spec.args("--mac-app-category", mac.getMacAppCategory().get());
                    if (mac.getMacDMGContent().isPresent()) spec.args("--dmg-content", mac.getMacDMGContent().get());
                }
                case JpackageWindowsOptions win -> {
                    if (win.getWinConsole().getOrElse(false)) spec.args("--win-console");
                    if (win.getWinDirChooser().getOrElse(false)) spec.args("--win-dir-chooser");
                    if (win.getWinHelpURL().isPresent()) spec.args("--win-help-url", win.getWinHelpURL().get());
                    if (win.getWinMenu().getOrElse(false)) spec.args("win-menu");
                    if (win.getWinMenuGroup().isPresent()) spec.args("--win-menu-group", win.getWinMenuGroup().get());
                    if (win.getWinPerUserInstall().getOrElse(false)) spec.args("--win-per-user-install");
                    if (win.getWinShortcut().getOrElse(false)) spec.args("--win-shortcut");
                    if (win.getWinShortcutPrompt().getOrElse(false)) spec.args("--win-shortcut-prompt");
                    if (win.getWinUpdateURL().isPresent()) spec.args("--win-update-url", win.getWinUpdateURL().get());
                    if (win.getWinUpgradeUUID().isPresent()) spec.args("--win-upgrade-uuid", win.getWinUpgradeUUID().get());
                }
                case null -> {}
            }
        });
    }
}
