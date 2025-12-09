package de.infolektuell.gradle.jpackage.tasks;

import de.infolektuell.gradle.jpackage.tasks.modularity.Modular;
import de.infolektuell.gradle.jpackage.tasks.modularity.Modularity;
import de.infolektuell.gradle.jpackage.tasks.modularity.NonModular;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.*;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.options.OptionValues;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.List;
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

    @Option(option = "type", description = "The type of package to create")
    @Optional
    @Input
    public abstract Property<@NonNull String> getType();

    @OptionValues("type")
    protected List<String> getTypes() {
        return List.of("app-image", "exe", "msi", "dmg", "pkg", "deb", "rpm");
    }

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
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getRuntimeImage();

    // Options for creating the application image
    @Option(option = "input", description = """
          Path of the input directory that contains the files to be packaged
          (absolute path or relative to the current directory)
          All files in the input directory will be packaged into the application image.
        """)
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

    @Option(option = "mac-package-identifier", description = """
          An identifier that uniquely identifies the application for macOS
          Defaults to the main class name.
          May only use alphanumeric (A-Z,a-z,0-9), hyphen (-), and period (.) characters.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacPackageIdentifier();

    @Option(option = "mac-package-name", description = """
          Name of the application as it appears in the Menu Bar
          This can be different from the application name.
          This name must be less than 16 characters long and be suitable for displaying in the menu bar and the application Info window.
          Defaults to the application name.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacPackageName();

    @Option(option = "mac-package-signing-prefix", description = "When signing the application package, this value is prefixed to all components that need to be signed that don't have an existing package identifier.")
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacPackageSigningPrefix();

    @Option(option = "mac-sign", description = "Request that the package or the predefined application image be signed.")
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getMacSign();

    @Option(option = "mac-signing-keychain", description = """
          Name of the keychain to search for the signing identity
          If not specified, the standard keychains are used.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacSigningKeychain();

    @Option(option = "mac-signing-key-user-name", description = """
          Team or user name portion of Apple signing identities.
          For direct control of the signing identity used to sign application images or installers use --mac-app-image-sign-identity and/or --mac-installer-sign-identity.
          This option cannot be combined with --mac-app-image-sign-identity or --mac-installer-sign-identity.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacSigningKeyUserName();

    @Option(option = "mac-app-image-sign-identity", description = """
          Identity used to sign application image.
          This value will be passed directly to --sign option of "codesign" tool.
          This option cannot be combined with --mac-signing-key-user-name.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacAppImageSignIdentity();

    @Option(option = "mac-installer-sign-identity", description = """
          Identity used to sign "pkg" installer.
          This value will be passed directly to --sign option of "productbuild" tool.
          This option cannot be combined with --mac-signing-key-user-name.
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacInstallerSignIdentity();

    @Option(option = "mac-app-store", description = "Indicates that the jpackage output is intended for the Mac App Store.")
    @Optional
    @Input
    public abstract Property<@NonNull Boolean> getMacAppStore();

    @Option(option = "mac-entitlements", description = "Path to file containing entitlements to use when signing executables and libraries in the bundle.")
    @Optional
    @InputFile
    public abstract RegularFileProperty getMacEntitlements();

    @Option(option = "mac-app-category", description = """
          String used to construct LSApplicationCategoryType in application plist.
          The default value is "utilities".
        """)
    @Optional
    @Input
    public abstract Property<@NonNull String> getMacAppCategory();

    // Options for creating the application package
    @Option(option = "about-url", description = "URL of the application's home page")
    @Optional
    @Input
    public abstract Property<@NonNull String> getAboutUrl();

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

    @Option(option = "mac-dmg-content", description = """
          Include all the referenced content in the dmg.
          This option can be used multiple times.\s
        """)
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getMacDMGContent();

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
            if (getMacPackageIdentifier().isPresent()) spec.args("--mac-package-identifier", getMacPackageIdentifier().get());
            if (getMacPackageName().isPresent()) spec.args("--mac-package-name", getMacPackageName().get());
            if (getMacPackageSigningPrefix().isPresent()) spec.args("--mac-package-signing-prefix", getMacPackageSigningPrefix().get());
            if (getMacSign().getOrElse(false)) spec.args("--mac-sign");
            if (getMacSigningKeychain().isPresent()) spec.args("--mac-signing-keychain", getMacSigningKeychain().get());
            if (getMacSigningKeyUserName().isPresent()) spec.args("--mac-signing-key-user-name", getMacSigningKeyUserName().get());
            if (getMacAppImageSignIdentity().isPresent()) spec.args("--mac-app-image-sign-identity", getMacAppImageSignIdentity().get());
            if (getMacInstallerSignIdentity().isPresent()) spec.args("--mac-installer-sign-identity", getMacInstallerSignIdentity().get());
            if (getMacAppStore().isPresent()) spec.args("--mac-app-store");
            if (getMacEntitlements().isPresent()) spec.args("--mac-entitlements", getMacEntitlements().get());
            if (getMacAppCategory().isPresent()) spec.args("--mac-app-category", getMacAppCategory().get());

            if (getApplicationImage().isPresent()) spec.args("--app-image", getApplicationImage().get());
            if (getAboutUrl().isPresent()) spec.args("--about-url", getAboutUrl().get());
            getFileAssociations().get().forEach(f -> spec.args("--file-associations", f));
            if (getInstallDir().isPresent()) spec.args("--install-dir", getInstallDir().get());
            if (getLicenseFile().isPresent()) spec.args("--license-file", getLicenseFile().get());
            if (getResourceDir().isPresent()) spec.args("--resource-dir", getResourceDir().get());
            if (getLauncherAsService().getOrElse(false)) spec.args("--launcher-as-service");
            if (getMacDMGContent().isPresent()) spec.args("--dmg-content", getMacDMGContent().get());
        });
    }
}
