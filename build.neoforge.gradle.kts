// NeoForge nodes (ModDevGradle). Shared logic (Java, metadata templating, tests, publishing) is in buildSrc/mod-common.
plugins {
    id("mod-common")
    id("net.neoforged.moddev")
}

val modId = property("mod_id") as String
val packagePath = (property("maven_group") as String).replace('.', '/')

// Client-only code keeps its own source set (src/client), like Loom's split source sets on Fabric:
// main cannot see client classes, and the jar contains both.
val client: SourceSet = sourceSets.create("client") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

// PORTING IN PROGRESS: only these packages (or single files) compile on NeoForge so far. Each is added once it no
// longer uses Fabric API or Team Reborn Energy directly; the filter goes away when everything compiles.
val ported = listOf("core", "platform", "inventory", "compat/network", "compat/MenuCompat.java")
sourceSets.main { java { ported.forEach { include("$packagePath/$it/**") } } }
client.java { include("$packagePath/client/platform/**") }
sourceSets.test { java { include("$packagePath/core/**") } }

neoForge {
    version = property("neoforge_version") as String
    addModdingDependenciesTo(client)

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(client)
        }
    }

    runs {
        // Run directory: versions/<v>/run (kept apart from the Fabric runs, whose worlds would not load here)
        register("client") { client() }
        register("server") {
            server()
            programArgument("--nogui")
        }
        // Headless in-world tests (no EULA needed); runs the GameTests of this mod's namespace and exits
        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
    }
}

// Dev-run-only dependencies, never in the published metadata (Loom's modLocalRuntime; same as the NeoForge MDK)
val localRuntime: Configuration = configurations.create("localRuntime")
configurations.runtimeClasspath { extendsFrom(localRuntime) }

dependencies {
    // Jade plugin (compat/jade): optional, not bundled; present in dev runs to try it
    compileOnly("maven.modrinth:jade:${property("jade_version")}")
    localRuntime("maven.modrinth:jade:${property("jade_version")}")
}

tasks.jar { from(client.output) }
tasks.named<Jar>("sourcesJar") { from(client.allSource) }

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
}
