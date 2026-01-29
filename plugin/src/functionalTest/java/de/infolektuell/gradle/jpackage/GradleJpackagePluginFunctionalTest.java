package de.infolektuell.gradle.jpackage;

import java.io.File;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleJpackagePluginFunctionalTest {
    private final File projectDir = new File("../example");
    @Test
    void nonModularProject() {
        var runner = createRunner("nonmodular", "9.1.0");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularProject() {
        var runner = createRunner("modular", "9.0.0");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    private GradleRunner createRunner(String project, String gradleVersion) {
        return GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withArguments(project + ":clean", project + ":build", "--stacktrace")
            .forwardOutput();
    }
}
