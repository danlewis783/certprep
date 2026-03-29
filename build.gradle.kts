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

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.11.4")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

application {
    // Define the main class for the application.
    mainClass = "certprep.CertPrep"
}

tasks.named<JavaExec>("run") {
    val chapter: Int = 10
    val start: Int = 11
    val end: Int = 20
    val home = System.getProperty("user.home")
    val certprepDir = "$home/.certprep"

    args(
        "--chapter", chapter,
        "--start", start,
        "--end", end,
        "--data", "$certprepDir/data",
        "--session", "$certprepDir/sessions"
    )
}
