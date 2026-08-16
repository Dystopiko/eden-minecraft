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
    include(dep)
    implementation(dep)
}

val includeImplementation: Configuration = configurations.create("includeImplementation") {
    configurations.implementation.configure { extendsFrom(this@create) }
}

dependencies {
    // To change the versions, see at `libs.versions.toml` file
    // Fabric
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    // Fabric API + Kotlin + Kotlinx
    implementation(libs.fabric.api)
    implementation(libs.fabric.kotlin)
    includeImplementation(libs.kotlinx.datetime)

    // EdenMC API
    api(project(":api"))
    include(project(":api"))

    // Floodgate
    implementation(libs.floodgate.api)

    // LuckPerms
    implementation(libs.luckperms.api)

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
    includeAndImplementation(libs.okhttp.tls)

    // Adventure
    includeAndImplementation(libs.adventure.api)
    includeAndImplementation(libs.adventure.fabric)

    // cron-utils
    includeAndImplementation(libs.cronutils)
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.register<DownloadTask>("downloadFloodgate") {
    url = "https://cdn.modrinth.com/data/bWrNNfkb/versions/urOFTrVX/Floodgate-Fabric-2.2.6-b67.jar"
    output = file("run/mods/Floodgate.jar")
}

tasks.register<DownloadTask>("downloadGeyser") {
    url = "https://cdn.modrinth.com/data/wKkoqHrH/versions/SansJdt3/Geyser-Fabric-2.11.1-b1223.jar"
    output = file("run/mods/Geyser.jar")
}

tasks.register<DownloadTask>("downloadLuckPerms") {
    url = "https://cdn.modrinth.com/data/Vebnzrzj/versions/UHUghDkV/LuckPerms-Fabric-5.5.57.jar"
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
            "target_mc_version" to libs.versions.minecraft.get(),

            "fabric_loader_version" to libs.versions.fabric.loader.get(),
            "fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
        ))
    }
}

