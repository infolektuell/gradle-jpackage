package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.jspecify.annotations.NonNull;

public non-sealed interface JpackageMacOSOptions extends JpackagePlatformOptions {
    @Option(option = "mac-package-identifier", description = """
          An identifier that uniquely identifies the application for macOS
          Defaults to the main class name.
          May only use alphanumeric (A-Z,a-z,0-9), hyphen (-), and period (.) characters.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacPackageIdentifier();

    @Option(option = "mac-package-name", description = """
          Name of the application as it appears in the Menu Bar
          This can be different from the application name.
          This name must be less than 16 characters long and be suitable for displaying in the menu bar and the application Info window.
          Defaults to the application name.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacPackageName();

    @Option(option = "mac-package-signing-prefix", description = "When signing the application package, this value is prefixed to all components that need to be signed that don't have an existing package identifier.")
    @Optional
    @Input
    Property<@NonNull String> getMacPackageSigningPrefix();

    @Option(option = "mac-sign", description = "Request that the package or the predefined application image be signed.")
    @Optional
    @Input
    Property<@NonNull Boolean> getMacSign();

    @Option(option = "mac-signing-keychain", description = """
          Name of the keychain to search for the signing identity
          If not specified, the standard keychains are used.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacSigningKeychain();

    @Option(option = "mac-signing-key-user-name", description = """
          Team or user name portion of Apple signing identities.
          For direct control of the signing identity used to sign application images or installers use --mac-app-image-sign-identity and/or --mac-installer-sign-identity.
          This option cannot be combined with --mac-app-image-sign-identity or --mac-installer-sign-identity.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacSigningKeyUserName();

    @Option(option = "mac-app-image-sign-identity", description = """
          Identity used to sign application image.
          This value will be passed directly to --sign option of "codesign" tool.
          This option cannot be combined with --mac-signing-key-user-name.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacAppImageSignIdentity();

    @Option(option = "mac-installer-sign-identity", description = """
          Identity used to sign "pkg" installer.
          This value will be passed directly to --sign option of "productbuild" tool.
          This option cannot be combined with --mac-signing-key-user-name.
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacInstallerSignIdentity();

    @Option(option = "mac-app-store", description = "Indicates that the jpackage output is intended for the Mac App Store.")
    @Optional
    @Input
    Property<@NonNull Boolean> getMacAppStore();

    @Option(option = "mac-entitlements", description = "Path to file containing entitlements to use when signing executables and libraries in the bundle.")
    @Optional
    @InputFile
    RegularFileProperty getMacEntitlements();

    @Option(option = "mac-app-category", description = """
          String used to construct LSApplicationCategoryType in application plist.
          The default value is "utilities".
        """)
    @Optional
    @Input
    Property<@NonNull String> getMacAppCategory();

    @Option(option = "mac-dmg-content", description = """
          Include all the referenced content in the dmg.
          This option can be used multiple times.\s
        """)
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    DirectoryProperty getMacDMGContent();
}
