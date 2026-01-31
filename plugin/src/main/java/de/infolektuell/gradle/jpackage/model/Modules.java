package de.infolektuell.gradle.jpackage.model;

import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Modules {
    public static String moduleName(File file) {
        String moduleName = fileModuleName(file);
        if (Objects.isNull(moduleName)) moduleName = directoryModuleName(file);
        if (Objects.isNull(moduleName)) moduleName = archiveModuleName(file);
        return moduleName;
    }

    public static boolean isModule(File file) {
        if (file.isFile()) {
            if (file.getName().endsWith(".jmod")) return true;
            return isJarModule(file);
        } else {
            return Files.exists(file.toPath().resolve("module-info.class")) || Files.exists(file.toPath().resolve("module-info.java"));
        }
    }

    private static boolean isJarModule(File file) {
        if (!file.isFile() || !file.exists() || !file.getName().endsWith(".jar")) return false;
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

    private static String directoryModuleName(File file) {
        if (!file.isDirectory()) return null;
        final Path modulePath = file.toPath().resolve("module-info.class");
        if (Files.isDirectory(modulePath) || !Files.exists(modulePath)) return null;
        try (var in = Files.newInputStream(modulePath)) {
            final ModuleDescriptor module = ModuleDescriptor.read(in);
            return module.name();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String fileModuleName(File file) {
        if (!file.isFile() || !file.getName().equals("module-info.class")) return null;
        final var modulePath = file.toPath();
        try (var in = Files.newInputStream(modulePath)) {
            final var module = ModuleDescriptor.read(in);
            return module.name();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String archiveModuleName(File file) {
        if (!file.isFile()) return null;
        if (!(file.getName().endsWith(".jar") || file.getName().endsWith(".jmod"))) return null;
        try (ZipFile zip = new ZipFile(file)) {
            final Optional<? extends ZipEntry> info = zip.stream().filter(e -> e.getName().endsWith("module-info.class")).findFirst();
            if (info.isPresent()) {
                try (var in = zip.getInputStream(info.get())) {
                    final var module = ModuleDescriptor.read(in);
                    return module.name();
                }
            }
            final ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF");
            if (Objects.isNull(manifest)) return null;
            try (var input = zip.getInputStream(manifest)) {
                var scanner = new Scanner(input);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("Automatic-Module-Name")) {
                        final String[] parts = line.split("\\w");
                        return parts[parts.length - 1];
                    }
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }
}
