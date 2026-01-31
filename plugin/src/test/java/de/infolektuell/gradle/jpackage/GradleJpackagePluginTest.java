package de.infolektuell.gradle.jpackage;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GradleJpackagePluginTest {
    @Test void pluginApliesSucessfully() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(GradleJpackagePlugin.PLUGIN_NAME);
        assertNotNull(project.getExtensions().findByName("jpackage"));
        assertNull(project.getTasks().findByName("appImage"));
    }

    @Test void pluginRegistersAppImageTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("application");
        project.getPlugins().apply(GradleJpackagePlugin.PLUGIN_NAME);
        assertNotNull(project.getTasks().findByName("appImage"));
    }
}
