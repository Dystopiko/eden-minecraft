import org.jreleaser.gradle.plugin.tasks.AbstractJReleaserTask
import org.jreleaser.model.Active
import org.jreleaser.model.Signing

plugins {
    alias(libs.plugins.jreleaser)
    id("java-library")
    id("maven-publish")
    id("signing")
}

val mavenGroup = project.property("maven_group").toString()
val modId = project.property("mod_id").toString()
base.archivesName = "$modId-api"
project.version = project.property("mod_api_version").toString()

dependencies {
    api("org.checkerframework:checker-qual:3.49.3")
    api("org.jetbrains:annotations:26.0.2")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "$mavenGroup.api")
    }
}

///////////////////////////////////////////////////////////////////////////////////////
val jreleaserRequiredVars = setOf(
    "JRELEASER_MAVENCENTRAL_USERNAME",
    "JRELEASER_MAVENCENTRAL_TOKEN",
)

val canPublish: Boolean = jreleaserRequiredVars.all { System.getenv(it) != null }
if (canPublish) {
    fun MavenPom.populate() {
        name = "EdenMC API"
        description = "Public API library for the EdenMC mod"
        url = "https://github.com/Dystopiko/eden-minecraft"

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "memothelemo"
                name = "memothelemo"
                email = "dev@memothelemo.xyz"
            }
        }

        scm {
            connection = "scm:git:https://github.com/Dystopiko/eden-minecraft.git"
            developerConnection = "scm:git:git@github.com:Dystopiko/eden-minecraft.git"
            url = "https://github.com/Dystopiko/eden-minecraft"
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/Dystopiko/eden-minecraft/issues"
        }
    }

    val stagingDirectory = layout.buildDirectory.dir("staging-deploy").get()

    publishing {
        publications {
            register<MavenPublication>("Release") {
                from(components["java"])

                artifactId = project.name
                groupId = mavenGroup
                version = project.version as String

                pom.populate()
            }
        }

        repositories.maven {
            url = stagingDirectory.asFile.toURI()
        }
    }

    jreleaser {
        project {
            versionPattern = "CUSTOM"
        }

        release {
            github {
                enabled = false
            }
        }

        signing {
            // All the files are already signed needed for publishing
            active = Active.ALWAYS

            // Trusting jreleaser to have a passphrase in an environment variable
            // using memothelemo's actual secret key is something he can't do.
            pgp {
                mode = Signing.Mode.COMMAND
                verify = true
            }
        }

        deploy {
            maven {
                mavenCentral {
                    register("sonatype") {
                        active = Active.RELEASE
                        url = "https://central.sonatype.com/api/v1/publisher"
                        stagingRepository(stagingDirectory.asFile.relativeTo(projectDir).path)
                    }
                }
            }
        }
    }

    tasks.withType<AbstractJReleaserTask>().configureEach {
        mustRunAfter(tasks.named("publish"))
    }
}
