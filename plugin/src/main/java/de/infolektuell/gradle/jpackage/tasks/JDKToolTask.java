package de.infolektuell.gradle.jpackage.tasks;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;

public abstract class JDKToolTask extends DefaultTask {
    @Inject
    protected abstract ExecOperations getExecOperations();

    /**
     * Describes the Java installation where the tool is located
     */
    @Nested
    public abstract Property<@NonNull JavaInstallationMetadata> getMetadata();

    /**
     * Queries a certain JDK tool
     * @param toolName A valid name of a JDK tool
     * @return The tool's executable file
     */
    protected Provider<@NonNull RegularFile> getExecutable(String toolName) {
        return getMetadata().map(data -> {
            var root = data.getInstallationPath();
            var executable = root.getAsFileTree().matching(it -> it.include("bin/" + toolName + "*")).getSingleFile();
            return root.file(executable.getAbsolutePath());
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    protected ExecResult exec(String toolName, Action<@NonNull ExecSpec> action) {
        return getExecOperations().exec(spec -> {
            spec.executable(getExecutable(toolName).get());
            action.execute(spec);
        });
    }
}
