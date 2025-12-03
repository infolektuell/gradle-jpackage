plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("de.infolektuell.jpackage")
}

jpackage {
    metadata.name = "Sample"
runtime {
    // modules = listOf("java.base")
}
    // argFiles = listOf(layout.projectDirectory.file("src/args.txt"))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.App"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
