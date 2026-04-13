plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.loom) apply false
    id("maven-publish")
}

val mavenGroup = project.property("maven_group").toString()
val modVersion = project.property("mod_version").toString()

subprojects {
    group = mavenGroup
    version = modVersion

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 25
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "central-snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        maven {
            url = uri("https://repo.opencollab.dev/main/")
        }
    }
}
