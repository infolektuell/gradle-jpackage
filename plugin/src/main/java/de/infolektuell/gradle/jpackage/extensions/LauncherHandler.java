package de.infolektuell.gradle.jpackage.extensions;

import de.infolektuell.gradle.jpackage.tasks.Launcher;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.NonNull;

public abstract class LauncherHandler {
    public abstract ListProperty<@NonNull String> getArguments();
    public abstract ListProperty<@NonNull String> getJavaOptions();
    public abstract Property<@NonNull String> getMainModule();
    public abstract Property<@NonNull String> getMainClass();
    public abstract Property<@NonNull Boolean> getLauncherAsService();

    public abstract NamedDomainObjectContainer<@NonNull Launcher> getLaunchers();
    public void addLauncher(String name, RegularFile file) {
        getLaunchers().register(name, l -> l.getFile().convention(file));
    }
    public void addLauncher(String name, Provider<@NonNull RegularFile> file) {
        getLaunchers().register(name, l -> l.getFile().convention(file));
    }
}
