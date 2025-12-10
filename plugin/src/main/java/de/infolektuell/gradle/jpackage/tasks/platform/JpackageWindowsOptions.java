package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jspecify.annotations.NonNull;

public non-sealed interface JpackageWindowsOptions extends JpackagePlatformOptions {
    @Optional
    @Input
    Property<@NonNull Boolean> getWinConsole();

    @Optional
    @Input
    Property<@NonNull Boolean> getWinDirChooser();

    @Optional
    @Input
    Property<@NonNull String> getWinHelpURL();

    @Optional
    @Input
    Property<@NonNull Boolean> getWinMenu();

    @Optional
    @Input
    Property<@NonNull String> getWinMenuGroup();

    @Optional
    @Input
    Property<@NonNull Boolean> getWinPerUserInstall();

    @Optional
    @Input
    Property<@NonNull Boolean> getWinShortcut();

    @Optional
    @Input
    Property<@NonNull Boolean> getWinShortcutPrompt();

    @Optional
    @Input
    Property<@NonNull String> getWinUpdateURL();

    @Optional
    @Input
    Property<@NonNull String> getWinUpgradeUUID();
}
