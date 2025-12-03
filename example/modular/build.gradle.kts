plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("de.infolektuell.jpackage")
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
        languageVersion = JavaLanguageVersion.of(25) // Used by Jlink
    }
}

application {
    mainModule = "example.app"
    mainClass = "org.example.App"
}

jpackage {
    metadata.name = "modular Sample App"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
