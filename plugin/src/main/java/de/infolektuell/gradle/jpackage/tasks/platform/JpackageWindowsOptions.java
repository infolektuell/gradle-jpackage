package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

public non-sealed interface JpackageWindowsOptions extends JpackagePlatformOptions {
    enum InstallerType implements Serializable {
        EXE, MSI;
        public String toString() { return this.name().toLowerCase(); }
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Optional
    @Input
    Property<@NonNull InstallerType> getType();

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
