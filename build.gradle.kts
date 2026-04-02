import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.loom)
    id("maven-publish")
}

val modId = project.property("mod_id").toString()
val modName = project.property("mod_name").toString()
val modVersion = project.property("mod_version").toString()
val mavenGroup = project.property("maven_group").toString()
val minimumMcVersion = project.property("min_minecraft_version").toString()

base.archivesName.set(modId)
version = modVersion
group = mavenGroup

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "central-snapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

fun DependencyHandlerScope.includeAndImplementation(dep: Any) {
    implementation(dep)
    include(dep)
}

val includeImplementation: Configuration by configurations.creating {
    configurations.implementation.configure { extendsFrom(this@creating) }
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

    // Luckperms
    implementation(libs.luckperms.api)

    // Fabric Permissions
    includeAndImplementation(libs.fabric.permissions)

    // ktoml
    includeAndImplementation(libs.ktoml.core)
    includeAndImplementation(libs.ktoml.file)

    // okhttp
    includeAndImplementation(libs.okhttp)

    // Adventure
    includeAndImplementation(libs.adventure.api)
    includeAndImplementation(libs.adventure.fabric)
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

tasks.jar {
    from("LICENSE")
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(25)
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

// Configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // Select the repositories you want to publish to
    repositories {}
}

// Copied from: https://github.com/QuiltServerTools/Ledger/blob/bf8192bad56c58e4914d3806958276e13f3b55cd/build.gradle.kts
afterEvaluate {
    dependencies {
        handleIncludes(includeImplementation)
    }
}

/* Thanks to https://github.com/jakobkmar for original script */
fun DependencyHandlerScope.includeTransitive(
    dependencies: Set<ResolvedDependency>,
    minecraftLibs: Set<ResolvedDependency>,
    kotlinDependency: ResolvedDependency,
    checkedDependencies: MutableSet<ResolvedDependency> = HashSet()
) {
    dependencies.forEach {
        if (checkedDependencies.contains(it) || it.moduleGroup == "org.jetbrains.kotlin" || it.moduleGroup == "org.jetbrains.kotlinx") return@forEach

        if (kotlinDependency.children.any { dep -> dep.name == it.name }) {
            println("Skipping -> ${it.name} (already in fabric-language-kotlin)")
        } else if (minecraftLibs.any { dep -> dep.moduleGroup == it.moduleGroup && dep.moduleName == it.moduleName }) {
            println("Skipping -> ${it.name} (already in minecraft)")
        } else {
            include(it.name)
            println("Including -> ${it.name}")
        }
        checkedDependencies += it

        includeTransitive(it.children, minecraftLibs, kotlinDependency, checkedDependencies)
    }
}

fun DependencyHandlerScope.handleIncludes(configuration: Configuration) {
    includeTransitive(
        configuration.resolvedConfiguration.firstLevelModuleDependencies,
        configurations.minecraftLibraries.get().resolvedConfiguration.firstLevelModuleDependencies,
        configurations.runtimeClasspath.get().resolvedConfiguration.firstLevelModuleDependencies
            .first { it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-language-kotlin" },
    )
}

// end of copy
