package de.infolektuell.gradle.jpackage;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GradleJpackagePluginTest {
    @Test
    void pluginApliesSucessfully() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(GradleJpackagePlugin.PLUGIN_NAME);
        assertNotNull(project.getExtensions().findByName("jpackage"));
        assertNull(project.getTasks().findByName("appImage"));
    }

    @Test
    void pluginRegistersAppImageTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("application");
        project.getPlugins().apply(GradleJpackagePlugin.PLUGIN_NAME);
        assertNotNull(project.getTasks().findByName("appImage"));
    }
}
