package de.infolektuell.gradle.jpackage.tasks.platform;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.process.CommandLineArgumentProvider;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public non-sealed interface JpackageLinuxOptions extends JpackagePlatformOptions, CommandLineArgumentProvider {
    @Optional
    @Input
    Property<@NonNull String> getLinuxPackageName();

    @Optional
    @Input
    Property<@NonNull String> getLinuxDebMaintainer();

    @Optional
    @Input
    Property<@NonNull String> getLinuxMenuGroup();

    @Optional
    @Input
    Property<@NonNull String> getLinuxPackageDeps();

    @Optional
    @Input
    Property<@NonNull String> getLinuxRPMLicenseType();

    @Optional
    @Input
    Property<@NonNull String> getLinuxAppRelease();

    @Optional
    @Input
    Property<@NonNull String> getLinuxAppCategory();

    @Optional
    @Input
    Property<@NonNull Boolean> getLinuxShortcut();

    @Override
    default Iterable<String> asArguments() {
        final List<String> args = new ArrayList<>();
        if (getLinuxPackageName().isPresent()) {
            args.add("--linux-package-name");
            args.add(getLinuxPackageName().get());
        }
        if (getLinuxDebMaintainer().isPresent()) {
            args.add("--linux-deb-maintainer");
            args.add(getLinuxDebMaintainer().get());
        }
        if (getLinuxMenuGroup().isPresent()) {
            args.add("--linux-menu-group");
            args.add(getLinuxMenuGroup().get());
        }
        if (getLinuxPackageDeps().isPresent()) {
            args.add("--linux-package-deps");
            args.add(getLinuxPackageDeps().get());
        }
        if (getLinuxRPMLicenseType().isPresent()) {
            args.add("--linux-rpm-license-type");
            args.add(getLinuxRPMLicenseType().get());
        }
        if (getLinuxAppRelease().isPresent()) {
            args.add("--linux-app-release");
            args.add(getLinuxAppRelease().get());
        }
        if (getLinuxAppCategory().isPresent()) {
            args.add("--linux-app-category");
            args.add(getLinuxAppCategory().get());
        }
        if (getLinuxShortcut().getOrElse(false)) args.add("--linux-shortcut");
        return args;
    }
}
