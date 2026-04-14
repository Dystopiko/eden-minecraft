import net.fabricmc.loom.task.DownloadTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.loom)
}

val modId = project.property("mod_id").toString()
val modName = project.property("mod_name").toString()
val modVersion = project.property("mod_version").toString()
base.archivesName.set(modId)

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

    // EdenMC API
    api(project(":api"))

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

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.register<DownloadTask>("downloadFloodgate") {
    url = "https://cdn.modrinth.com/data/bWrNNfkb/versions/81EuNxeZ/Floodgate-Fabric-2.2.6-b60.jar"
    output = file("run/mods/Floodgate.jar")
}

tasks.register<DownloadTask>("downloadGeyser") {
    url = "https://cdn.modrinth.com/data/wKkoqHrH/versions/4Ij9rDq0/geyser-fabric-Geyser-Fabric-2.9.5-b1113.jar"
    output = file("run/mods/Geyser.jar")
}

tasks.register<DownloadTask>("downloadLuckPerms") {
    url = "https://cdn.modrinth.com/data/Vebnzrzj/versions/CzCJJMuo/LuckPerms-Fabric-5.5.21.jar"
    output = file("run/mods/LuckPerms.jar")
}

tasks.register("downloadModDependencies") {
    dependsOn("downloadFloodgate")
    dependsOn("downloadGeyser")
    dependsOn("downloadLuckPerms")
}

tasks.named("runServer").configure {
    dependsOn("downloadModDependencies")
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
            "target_mc_version" to "1.21.11",

            "fabric_loader_version" to libs.versions.fabric.loader.get(),
            "fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
        ))
    }
}

