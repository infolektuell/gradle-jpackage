import org.gradle.plugin.compatibility.compatibility

plugins {
    signing
    id("com.gradle.plugin-publish") version "2.2.1"
}

group = "de.infolektuell"
description = "Gradle plugin that uses Jpackage to create native installers for java apps"

val releaseVersion = releaseVersion()
val releaseNotes = releaseNotes()
version = releaseVersion.get()

gradlePlugin {
    website = "https://infolektuell.github.io/gradle-jpackage/"
    vcsUrl = "https://github.com/infolektuell/gradle-jpackage.git"
    plugins.register("jpackagePlugin") {
        id = "de.infolektuell.jpackage"
        displayName = "Gradle Jpackage Plugin"
        description = releaseNotes.get()
        tags = listOf("jpackage", "jlink", "application", "installer", "native", "jdeps")
        implementationClass = "de.infolektuell.gradle.jpackage.GradleJpackagePlugin"
        compatibility {
            features {
                configurationCache = true
            }
        }
    }
}

signing {
    // Get credentials from env variables for better CI compatibility
    val signingKeyId: String? = providers.gradleProperty("signingKeyId").orNull
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
}

tasks.withType<Sign>().configureEach {
    val isReleaseVersion = releaseVersion.map { !it.endsWith("-SNAPSHOT") }
    onlyIf { isReleaseVersion.get() }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType(Javadoc::class).named("javadoc") {
    javadocTool = javaToolchains.javadocToolFor { languageVersion = JavaLanguageVersion.of(25) }
    options.destinationDirectory = rootProject.layout.projectDirectory.dir("docs/public/reference").asFile
    options.quiet()
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest = tasks.register("functionalTest", Test::class) {
    description = "Runs the functional tests for the plugin"
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}

fun releaseVersion(): Provider<String> {
    val releaseVersionFile = rootProject.layout.projectDirectory.file("release/version.txt")
    return providers.fileContents(releaseVersionFile).asText.map(String::trim)
}

fun releaseNotes(): Provider<String> {
    val releaseNotesFile = rootProject.layout.projectDirectory.file("release/changes.md")
    return providers.fileContents(releaseNotesFile).asText.map(String::trim)
}
