plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.loom) apply false

    id("maven-publish")
    id("signing")
}

val mavenGroup = project.property("maven_group").toString()
val modApiVersion = project.property("mod_api_version").toString()
val modVersion = project.property("mod_version").toString()

subprojects {
    group = mavenGroup
    version = modVersion

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
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

    if (project.name == "api") {
        tasks.register<Zip>("packageAsZip") {
            dependsOn(tasks.named("jar"))
            dependsOn(tasks.named("javadocJar"))
            dependsOn(tasks.named("sourcesJar"))

            dependsOn(tasks.named("remapJar"))
            dependsOn(tasks.named("remapSourcesJar"))

            archiveBaseName.set("edenmc-api")
            archiveVersion.set(modApiVersion)
            archiveExtension.set("zip")

            from("build/libs")
        }

        project.afterEvaluate {
            project.publishing {
                publications {
                    create<MavenPublication>("zipArtifact") {
                        artifact(tasks.named("packageAsZip"))
                        from(project.components["java"])

                        pom {
                            name.set("EdenMC API")
                            description.set("Public API library for the EdenMC mod")
                            url.set("https://github.com/Dystopiko/eden-minecraft")

                            licenses {
                                license {
                                    name.set("MIT")
                                    url.set("https://opensource.org/licenses/MIT")
                                }
                            }

                            developers {
                                developer {
                                    id.set("memothelemo")
                                    name.set("memothelemo")
                                    email.set("dev@memothelemo.xyz")
                                }
                            }

                            scm {
                                connection.set("scm:git:https://github.com/Dystopiko/eden-minecraft.git")
                                developerConnection.set("scm:git:git@github.com:Dystopiko/eden-minecraft.git")
                                url.set("https://github.com/Dystopiko/eden-minecraft")
                            }

                            issueManagement {
                                system.set("GitHub")
                                url.set("https://github.com/Dystopiko/eden-minecraft/issues")
                            }
                        }
                    }
                }
            }

            project.signing {
                useGpgCmd()
                sign(project.publishing.publications["zipArtifact"])
            }
        }
    }
}

// Used to handle Sonatype staging repositories.
group = mavenGroup
