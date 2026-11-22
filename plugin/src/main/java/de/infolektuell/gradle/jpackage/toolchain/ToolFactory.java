package de.infolektuell.gradle.jpackage.toolchain;

import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public abstract class ToolFactory {
    public Provider<JdepsTool> getJdepsToolFor(JavaToolchainSpec spec) {
        return getJavaToolchainService().launcherFor(spec).map(launcher -> getObjects().newInstance(JdepsToolImpl.class, launcher.getMetadata()));
    }

    public Provider<JlinkTool> getJlinkToolFor(JavaToolchainSpec spec) {
        return getJavaToolchainService().launcherFor(spec).map(launcher -> getObjects().newInstance(JlinkToolImpl.class, launcher.getMetadata()));
    }

    public Provider<JpackageTool> getJpackageToolFor(JavaToolchainSpec spec) {
        return getJavaToolchainService().launcherFor(spec).map(launcher -> getObjects().newInstance(JpackageToolImpl.class, launcher.getMetadata()));
    }

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();
}
