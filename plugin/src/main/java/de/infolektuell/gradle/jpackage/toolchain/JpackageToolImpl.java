package de.infolektuell.gradle.jpackage.toolchain;

import javax.inject.Inject;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;

public abstract class JpackageToolImpl implements JpackageTool {
    private final JavaInstallationMetadata metadata;

    @Inject
    public JpackageToolImpl(JavaInstallationMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public JavaInstallationMetadata getMetadata() {
        return metadata;
    }

    @Override
    public RegularFile getExecutablePath() {
        final var executablePath = metadata.getInstallationPath().dir("bin").getAsFileTree().matching(m -> m.include("jpackage", "jpackage.exe")).getSingleFile();
        return getLayout().getProjectDirectory().file(executablePath.getAbsolutePath());
    }

    @Inject
    protected abstract ProjectLayout getLayout();
}
