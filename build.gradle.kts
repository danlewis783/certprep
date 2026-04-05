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
    mainClass.set("certprep.CertPrep")
}

tasks.named<JavaExec>("run") {
//    val chapter: Int = 10
//    val start: Int = 21
//    val end: Int = 40
//    val home = System.getProperty("user.home")
//    val certprepDir = "$home/.certprep"
//
//    args(
//        "--chapter", chapter,
//        "--start", start,
//        "--end", end,
//        "--data", "$certprepDir/data",
//        "--session", "$certprepDir/sessions"
//    )

    val home = System.getProperty("user.home")
    val certprepDir = "$home/.certprep"

    args(
        "--review-session", "$certprepDir/sessions/session-20260329-001.csv",
        "--data", "$certprepDir/data",
    )

//    val home = System.getProperty("user.home")
//    val certprepDir = "$home/.certprep"
//
//    args(
//        "--grade", "$certprepDir/sessions/session-20260329-001.csv",
//        "--data", "$certprepDir/data",
//    )
}
