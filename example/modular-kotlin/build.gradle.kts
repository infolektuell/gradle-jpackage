plugins {
    kotlin("jvm") version "2.4.21"
    application
    id("de.infolektuell.jpackage")
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)

    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Used by Jlink
    }
}

application {
    applicationName = "Sample"
    // mainModule = "example.app"
    mainClass = "org.example.App"
}

jpackage {
    common.isCommandLineApplication = true
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
