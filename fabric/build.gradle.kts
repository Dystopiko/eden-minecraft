import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.loom)
}

val modId = project.property("mod_id").toString()
val modName = project.property("mod_name").toString()
val modVersion = project.property("mod_version").toString()
val minimumMcVersion = project.property("min_minecraft_version").toString()

base.archivesName.set(modId)
version = modVersion
group = project.property("maven_group").toString()

fun DependencyHandlerScope.includeAndImplementation(dep: Any) {
    modImplementation(dep)
    include(dep)
}

val includeImplementation: Configuration by configurations.creating {
    configurations.implementation.configure { extendsFrom(this@creating) }
}

dependencies {
    // To change the versions, see at `libs.versions.toml` file
    // Fabric
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    // Fabric API + Kotlin + Kotlinx
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)
    includeImplementation(libs.kotlinx.datetime)

    // Floodgate
    modImplementation(libs.floodgate.api)

    // LuckPerms
    modImplementation(libs.luckperms.api)

    // Fabric Permissions
    includeAndImplementation(libs.fabric.permissions)

    // ktoml
    includeAndImplementation(libs.ktoml.core)
    includeAndImplementation(libs.ktoml.file)

    // required for ktoml
    includeAndImplementation("com.squareup.okio:okio:3.16.2")
    includeAndImplementation(libs.ktoml.source)

    // okhttp
    includeAndImplementation(libs.okhttp)

    // Adventure
    includeAndImplementation(libs.adventure.api)
    includeAndImplementation(libs.adventure.fabric)

    // cron-utils
    includeAndImplementation(libs.cronutils)
}

tasks.processResources {
    inputs.property("id", modId)
    inputs.property("name", modName)
    inputs.property("version", modVersion)

    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "mod_id" to modId,
            "mod_name" to modName,
            "mod_version" to modVersion,
            "min_minecraft_version" to minimumMcVersion,

            "fabric_loader_version" to libs.versions.fabric.loader.get(),
            "fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
        ))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}
