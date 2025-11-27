package de.infolektuell.gradle.jpackaging;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GradleJavaPackagingPluginTest {
    @Test void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(GradleJavaPackagingPlugin.PLUGIN_NAME);

        // Verify the result
        assertNotNull(project.getExtensions().findByName("packaging"));
    }
}
