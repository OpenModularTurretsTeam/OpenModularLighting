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

// OpenModularCore from source (../../CLAUDE.md, "Local development"): when a folder above this one holds
// OpenModularCore/, that build replaces the JitPack modules com.github.OpenModularTurretsTeam.OpenModularCore:<module>
// (its group is the JitPack group, its project names are the module names, the -j8 ones included). -Pomm.noLocalCore
// or OMM_NO_LOCAL_CORE=1 turns this off: the build then takes the JitPack artifact of core_version, as CI does.
// The same block is in the other 1.12 mods' settings and in the workspace's settings.gradle.
val localCoreOff = providers.gradleProperty("omm.noLocalCore").getOrElse("false") != "false" ||
    !(System.getenv("OMM_NO_LOCAL_CORE") ?: "0").matches(Regex("0?"))
if (!localCoreOff) {
    generateSequence(settingsDir) { it.parentFile }
        .map { File(it, "OpenModularCore") }
        .firstOrNull { File(it, "settings.gradle.kts").isFile }
        ?.let { localCore ->
            logger.lifecycle("OpenModularCore: local source at $localCore (OMM_NO_LOCAL_CORE=1 for the JitPack artifact)")
            includeBuild(localCore)
        }
}
