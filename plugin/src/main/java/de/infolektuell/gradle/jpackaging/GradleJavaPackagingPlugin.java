package de.infolektuell.gradle.jpackaging;
import de.infolektuell.gradle.jpackaging.extensions.PackagingExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/** Gradle plugin to create native installers for java apps */
public class GradleJavaPackagingPlugin implements Plugin<@NotNull Project> {
    /** The plugin ID */
    public static final String PLUGIN_NAME = "de.infolektuell.java-packaging";
    @Override
    public void apply(@NotNull Project project) {
        PackagingExtension extension = project.getExtensions().create(PackagingExtension.EXTENSION_NAME, PackagingExtension.class);
    }
}
