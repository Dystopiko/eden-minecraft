plugins {
    alias(libs.plugins.loom)
    id("java")
    id("maven-publish")
    id("signing")
}

val modId = project.property("mod_id").toString()

group = project.property("maven_group").toString()
project.version = project.property("mod_version").toString()
base.archivesName = "$modId-api"

// Due to limitations of Kotlin DSL, repeating code is the only choice
dependencies {
    // To change the versions, see at `libs.versions.toml` file
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    compileOnly("org.checkerframework:checker-qual:3.49.3")
    compileOnly("org.jetbrains:annotations:26.0.2")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// End of repeating code

