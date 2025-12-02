package de.infolektuell.gradle.jpackage.extensions;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class TypeHandler {
    public enum WindowsTypes {
        exe, msi
    }
    public enum MacTypes {
        dmg, pkg
    }
    public enum LinuxTypes {
        deb, rpm
    }

    public abstract Property<@NonNull WindowsTypes> getWindows();
    public abstract Property<@NonNull MacTypes> getMac();
    public abstract Property<@NonNull LinuxTypes> getLinux();
}
