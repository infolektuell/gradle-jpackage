package de.infolektuell.gradle.jpackage.tasks.providers;

import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Classpath;
import org.gradle.process.CommandLineArgumentProvider;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class ModulePathProvider implements CommandLineArgumentProvider {
    @Inject
    protected abstract ObjectFactory getObjects();
    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    /**
     * Specify where to find class files
     */
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @Classpath
    public FileCollection getModulePath() { return getClasspath().filter(this::isModule); }
    @Classpath
    public FileCollection getNonModulePath() { return getClasspath().filter(it -> !isModule(it)); }

    @Override
    public Iterable<String> asArguments() {
        return List.of("--module-path", getModulePath().getAsPath());
    }

    private boolean isModule(File file) {
        if (file.isDirectory()) return isModule(getObjects().fileTree().from(file));
        if (file.isFile() && file.getName().endsWith(".jar")) return isModule(getArchiveOperations().zipTree(file));
        return false;
    }

    private boolean isModule(FileTree tree) {
        if (!tree.matching(spec -> spec.include("**/module-info.class")).isEmpty()) return true;
        try {
            final Path manifestFile = tree.matching(spec -> spec.include("META-INF/MANIFEST.MF")).getSingleFile().toPath();
            if (Files.readAllLines(manifestFile).stream().anyMatch(l -> l.contains("Automatic-Module-Name")))
                return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
