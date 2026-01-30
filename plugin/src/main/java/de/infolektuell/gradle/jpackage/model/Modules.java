package de.infolektuell.gradle.jpackage.model;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Modules {
    public static boolean isModule(File file) {
        if (file.isFile()) {
            if (file.getName().endsWith(".jmod")) return true;
            return isJarModule(file);
        } else {
            return Files.exists(file.toPath().resolve("module-info.class"));
        }
    }

    private static boolean isJarModule(File file) {
        if (!file.getName().endsWith(".jar")) return false;
        try (ZipFile zip = new ZipFile(file)) {
            if (zip.stream().anyMatch(e -> e.getName().endsWith("module-info.class"))) return true;
            final ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF");
            if (Objects.isNull(manifest)) return false;
            try (var input = zip.getInputStream(manifest)) {
                var scanner = new Scanner(input);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("Automatic-Module-Name")) return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }
}
