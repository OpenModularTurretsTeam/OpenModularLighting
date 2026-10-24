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
}

plugins {
    // Auto-provisions the Java 8 toolchain (Azul) if it is not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "openmodularlighting"
