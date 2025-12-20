package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    default Iterable<String> asArguments() {
        final List<String> args = new ArrayList<>();
        if (getWinConsole().getOrElse(false)) args.add("--win-console");
        if (getWinDirChooser().getOrElse(false)) args.add("--win-dir-chooser");
        if (getWinHelpURL().isPresent()) {
            args.add("--win-help-url");
            args.add(getWinHelpURL().get());
        }
        if (getWinMenu().getOrElse(false)) args.add("win-menu");
        if (getWinMenuGroup().isPresent()) {
            args.add("--win-menu-group");
            args.add(getWinMenuGroup().get());
        }
        if (getWinPerUserInstall().getOrElse(false)) args.add("--win-per-user-install");
        if (getWinShortcut().getOrElse(false)) args.add("--win-shortcut");
        if (getWinShortcutPrompt().getOrElse(false)) args.add("--win-shortcut-prompt");
        if (getWinUpdateURL().isPresent()) {
            args.add("--win-update-url");
            args.add(getWinUpdateURL().get());
        }
        if (getWinUpgradeUUID().isPresent()) {
            args.add("--win-upgrade-uuid");
            args.add(getWinUpgradeUUID().get());
        }
        return args;
    }
}
