plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)
}

val toolsSourceSet = sourceSets.create("tools") {
    java.srcDir("src/tools/java")
    compileClasspath += sourceSets["main"].output + configurations["compileClasspath"]
    runtimeClasspath += output + compileClasspath
}

testing {
    suites {
        // Configure the built-in test suite
        named<JvmTestSuite>("test") {
            useJUnitJupiter(libs.versions.junit.jupiter.get())
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("acme.certprep.CertPrep")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.register<JavaExec>("generateTestData") {
    group = "tools"
    description = "Generate local sample CertPrep data"
    classpath = toolsSourceSet.runtimeClasspath
    mainClass.set("acme.certprep.util.GenerateTestData")
}
