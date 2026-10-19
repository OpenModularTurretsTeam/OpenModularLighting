// Fabric nodes (Loom). Shared logic (Java, metadata templating, tests, publishing) is in buildSrc/mod-common.
plugins {
    id("mod-common")
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
}

val modId = property("mod_id") as String
val minecraftVersion = property("minecraft_version") as String

loom {
    splitEnvironmentSourceSets()

    mods {
        register(modId) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runConfigs.all {
        generateRunConfig = true
        runDirectory = rootProject.file("run") // shared run directory for all Fabric versions
    }
}

fabricApi {
    configureDataGeneration {
        client = true
        // versions/<v>/src/main/generated, committed per version
        outputDirectory = project.file("src/main/generated")
    }
    // In-world tests (src/gametest, a separate test mod that is not shipped). `check` runs them on a headless
    // GameTest server; it needs no EULA. Client game tests are not used.
    configureTests {
        createSourceSet = true
        modId = "${modId}_gametest"
        enableGameTests = true
        enableClientGameTests = false
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    // Energy API (jar-in-jar)
    modImplementation("teamreborn:energy:${property("team_reborn_energy_version")}")
    include("teamreborn:energy:${property("team_reborn_energy_version")}")
    // Jade plugin (compat/jade): optional, not bundled; present in dev runs to try it
    modCompileOnly("maven.modrinth:jade:${property("jade_version")}")
    modLocalRuntime("maven.modrinth:jade:${property("jade_version")}")
}

publishMods {
    file = tasks.remapJar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
}
