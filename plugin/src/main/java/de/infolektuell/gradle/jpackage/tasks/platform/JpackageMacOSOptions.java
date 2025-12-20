package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public non-sealed interface JpackageMacOSOptions extends JpackagePlatformOptions {
    /**
     * An identifier that uniquely identifies the application for macOS
     * Defaults to the main class name.
     * May only use alphanumeric (A-Z,a-z,0-9), hyphen (-), and period (.) characters.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacPackageIdentifier();

    /**
     * Name of the application as it appears in the Menu Bar
     * This can be different from the application name.
     * This name must be less than 16 characters long and be suitable for displaying in the menu bar and the application Info window.
     * Defaults to the application name.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacPackageName();

    /**
     * When signing the application package, this value is prefixed to all components that need to be signed that don't have an existing package identifier.
     */
    @Optional
    @Input
    Property<@NonNull String> getMacPackageSigningPrefix();

    /**
     * Request that the package or the predefined application image be signed.
     */
    @Optional
    @Input
    Property<@NonNull Boolean> getMacSign();

    /**
     * Name of the keychain to search for the signing identity
     * If not specified, the standard keychains are used.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacSigningKeychain();

    /**
     * Team or username portion of Apple signing identities.
     * For direct control of the signing identity used to sign application images or installers use --mac-app-image-sign-identity and/or --mac-installer-sign-identity.
     * This option cannot be combined with --mac-app-image-sign-identity or --mac-installer-sign-identity.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacSigningKeyUserName();

    /**
     * Identity used to sign application image.
     * This value will be passed directly to --sign option of "codesign" tool.
     * This option cannot be combined with --mac-signing-key-user-name.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacAppImageSignIdentity();

    /**
     * Identity used to sign "pkg" installer.
     * This value will be passed directly to --sign option of "productbuild" tool.
     * This option cannot be combined with --mac-signing-key-user-name.
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacInstallerSignIdentity();

    /**
     * Indicates that the jpackage output is intended for the Mac App Store.
     */
    @Optional
    @Input
    Property<@NonNull Boolean> getMacAppStore();

    /**
     * Path to file containing entitlements to use when signing executables and libraries in the bundle.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getMacEntitlements();

    /**
     * String used to construct LSApplicationCategoryType in application plist.
     * The default value is "utilities".
     *
     */
    @Optional
    @Input
    Property<@NonNull String> getMacAppCategory();

    /**
     * Include all the referenced content in the dmg.
     * This option can be used multiple times.
     *
     */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    DirectoryProperty getMacDMGContent();

    @Override
    default Iterable<String> asArguments() {
        final List<String> args = new ArrayList<>();
        if (getMacPackageIdentifier().isPresent()) {
            args.add("--mac-package-identifier");
            args.add(getMacPackageIdentifier().get());
        }
        if (getMacPackageName().isPresent()) {
            args.add("--mac-package-name");
            args.add(getMacPackageName().get());
        }
        if (getMacPackageSigningPrefix().isPresent()) {
            args.add("--mac-package-signing-prefix");
            args.add(getMacPackageSigningPrefix().get());
        }
        if (getMacSign().getOrElse(false)) args.add("--mac-sign");
        if (getMacSigningKeychain().isPresent()) {
            args.add("--mac-signing-keychain");
            args.add(getMacSigningKeychain().get());
        }
        if (getMacSigningKeyUserName().isPresent()) {
            args.add("--mac-signing-key-user-name");
            args.add(getMacSigningKeyUserName().get());
        }
        if (getMacAppImageSignIdentity().isPresent()) {
            args.add("--mac-app-image-sign-identity");
            args.add(getMacAppImageSignIdentity().get());
        }
        if (getMacInstallerSignIdentity().isPresent()) {
            args.add("--mac-installer-sign-identity");
            args.add(getMacInstallerSignIdentity().get());
        }
        if (getMacAppStore().getOrElse(false)) args.add("--mac-app-store");
        if (getMacEntitlements().isPresent()) {
            args.add("--mac-entitlements");
            args.add(getMacEntitlements().get().getAsFile().getAbsolutePath());
        }
        if (getMacAppCategory().isPresent()) {
            args.add("--mac-app-category");
            args.add(getMacAppCategory().get());
        }
        if (getMacDMGContent().isPresent()) {
            args.add("--dmg-content");
            args.add(getMacDMGContent().get().getAsFile().getAbsolutePath());
        }
        return args;
    }
}
