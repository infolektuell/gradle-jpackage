package de.infolektuell.gradle.jpackage;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleJpackagePluginFunctionalTest {
    private final File projectDir = new File("../example");
    private final String gradleVersion = "8.5";

    @Test
    void nonModularProjectShouldBuild() {
        final var project = "nonmodular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":build", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularProjectShouldBuild() {
        final var project = "modular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":build", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void nonModularProjectShouldFindModuleDeps() {
        final var project = "nonmodular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":findModuleDeps", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularProjectShouldFindModuleDeps() {
        final var project = "modular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":findModuleDeps", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void nonModularProjectShouldGenerateRuntimeImage() {
        final var project = "nonmodular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":generateRuntimeImage", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularProjectShouldGenerateRuntimeImage() {
        final var project = "modular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":generateRuntimeImage", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void nonModularProjectShouldgenerateAppImage() {
        final var project = "nonmodular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":appImage", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularProjectShouldgenerateAppImage() {
        final var project = "modular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":appImage", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void nonModularAppShouldRun() {
        final var project = "nonmodular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":run", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    void modularAppShouldRun() {
        final var project = "modular";
        var runner = createRunner()
            .withArguments(project + ":clean", project + ":run", "--stacktrace");
        var result = runner.build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    private GradleRunner createRunner() {
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion);
    }
}
