pluginManagement {
    repositories {
        // RetroFuturaGradle (the Forge 1.12.2 toolchain)
        maven("https://nexus.gtnewhorizons.com/repository/public/") {
            name = "GTNH"
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroup("com.gtnewhorizons.retrofuturagradle")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
    // also declared by the OMTTeam 1.12 workspace, which builds build.gradle.kts as a subproject
    plugins {
        id("com.gtnewhorizons.retrofuturagradle") version "2.0.6"
        id("me.modmuss50.mod-publish-plugin") version "2.2.1"
        id("com.diffplug.spotless") version "8.10.4"
    }
}

plugins {
    // Auto-provisions the Java 8 toolchain (Azul) if it is not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "openmodularlighting"
