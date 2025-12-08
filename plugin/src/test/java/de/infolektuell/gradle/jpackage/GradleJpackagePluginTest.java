package de.infolektuell.gradle.jpackage;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GradleJpackagePluginTest {
    @Test void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(GradleJpackagePlugin.PLUGIN_NAME);

        // Verify the result
        assertNotNull(project.getExtensions().findByName("application"));
    }
}
