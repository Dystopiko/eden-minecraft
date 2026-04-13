plugins {
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "xyz.memothelemo.edenmc.api"
            artifactId = "edenmc-api"
            version = project.property("mod_api_version").toString()

            from(components["java"])

            pom {
                name = "EdenMC"
                description = "Public API library for the EdenMC mod"
                url = "https://github.com/Dystopiko/eden-minecraft"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/Dystopiko/eden-minecraft/blob/master/LICENSE.txt"
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
                    connection = "scm:git:git://github.com/Dystopiko/eden-minecraft.git"
                    developerConnection = "scm:git:git://github.com/Dystopiko/eden-minecraft.git"
                    url = "http://github.com/Dystopiko/eden-minecraft"
                }
            }
        }
    }
}
