plugins {
    java
}

val appName = "CertPrep"
val appMainClass = "acme.certprep.CertPrep"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)
    implementation(libs.jspecify)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
    testImplementation(libs.assertj)
}

val toolsSourceSet = sourceSets.create("tools") {
    java.srcDir("src/tools/java")
    compileClasspath += sourceSets["main"].output + configurations["compileClasspath"]
    runtimeClasspath += output + compileClasspath + configurations["runtimeClasspath"]
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

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = appMainClass
    }
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run CertPrep"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(appMainClass)
    standardInput = System.`in`
}

tasks.register<JavaExec>("generateTestData") {
    group = "tools"
    description = "Generate local sample CertPrep data"
    classpath = toolsSourceSet.runtimeClasspath + sourceSets["main"].runtimeClasspath
    mainClass.set("acme.certprep.util.GenerateTestData")
}

val jpackageInputDir = layout.buildDirectory.dir("jpackage/input")
val jpackageOutputDir = layout.buildDirectory.dir("jpackage/output")
val appImageDir = jpackageOutputDir.map { it.dir(appName) }

val javaToolchains = extensions.getByType<JavaToolchainService>()
val jpackageExecutable = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(25))
}.map {
    val executableName = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "jpackage.exe"
    } else {
        "jpackage"
    }
    it.executablePath.asFile.parentFile.resolve(executableName)
}

tasks.register<Copy>("prepareJpackageInput") {
    group = "distribution"
    description = "Collect application jars for jpackage"
    dependsOn(tasks.named("jar"))
    into(jpackageInputDir)
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    from(configurations.runtimeClasspath)
}

tasks.register<Delete>("cleanJpackageOutput") {
    delete(jpackageOutputDir)
}

tasks.register<Exec>("jpackageAppImage") {
    group = "distribution"
    description = "Create a copy-and-run Windows application image with jpackage"
    dependsOn(tasks.named("prepareJpackageInput"))
    dependsOn(tasks.named("cleanJpackageOutput"))

    val jpackageArgs = mutableListOf(
        jpackageExecutable.get().absolutePath,
        "--type", "app-image",
        "--name", appName,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--dest", jpackageOutputDir.get().asFile.absolutePath,
        "--main-jar", tasks.named<Jar>("jar").get().archiveFileName.get(),
        "--main-class", appMainClass
    )
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        jpackageArgs.add("--win-console")
    }

    commandLine(jpackageArgs)
}

tasks.register<Copy>("copyPackageDocs") {
    group = "distribution"
    description = "Copy project documentation into the app image root"
    dependsOn(tasks.named("jpackageAppImage"))
    into(appImageDir)
    from("LICENSE", "README.md")
}

tasks.register<Zip>("packageZip") {
    group = "distribution"
    description = "Create a copy-and-run CertPrep zip package"
    dependsOn(tasks.named("copyPackageDocs"))
    archiveFileName.set("certprep.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(appImageDir)
}
