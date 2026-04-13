plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.loom)
    id("maven-publish")
}

group = project.property("maven_group").toString()
project.version = project.property("mod_version").toString()

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    compileOnly("org.checkerframework:checker-qual:3.49.3")
    compileOnly("org.jetbrains:annotations:26.0.2")
}
