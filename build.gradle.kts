import java.time.LocalDate

// Forge 1.12.2 build (branch 1.12.2): one project, RetroFuturaGradle, Java 8, MCP mappings.
// The multi-loader build of the modern versions (Stonecutter, Loom, ModDevGradle) lives on the main branch.
plugins {
    java
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.6"
    // Uploads to CurseForge / Modrinth (build-time only)
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    // Formatting checks (./gradlew spotlessCheck / spotlessApply)
    id("com.diffplug.spotless") version "8.10.4"
}

val modId = property("mod_id") as String
val modName = property("mod_name") as String
val minecraftVersion = property("minecraft_version") as String
val javaVersion = (property("java_version") as String).toInt()
/** The base package, derived from maven_group (they are the same in our mods). */
val basePackage = property("maven_group") as String

version = "${property("mod_version")}+$minecraftVersion-forge"
group = basePackage
base.archivesName = property("archives_base_name") as String

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        // Azul covers the most platforms for Java 8 toolchains
        vendor = JvmVendorSpec.AZUL
    }
}

minecraft {
    mcVersion = minecraftVersion
    mcpMappingChannel = property("mapping_channel") as String
    mcpMappingVersion = property("mapping_version") as String
    username = "Developer"
    // assertions on for the mod's own code in dev runs
    extraRunJvmArguments.add("-ea:$basePackage")
}

dependencies {
    // Unit tests for the core game logic and the config (test only, not shipped)
    testImplementation(platform("org.junit:junit-bom:${property("junit_version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Deliberately lightweight, like on the main branch: tabs to spaces, trailing whitespace, final newline.
spotless {
    java {
        target("src/**/*.java")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.gradle.kts", ".gitignore", "src/**/*.json", "src/**/*.lang", "src/**/*.info")
        leadingTabsToSpaces(2)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "mod_id" to modId,
            "mod_name" to modName,
            "minecraft_version" to minecraftVersion,
        )
        inputs.properties(props)
        filesMatching("mcmod.info") { expand(props) }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    // The core game logic must stay independent of Minecraft so it can be unit-tested in isolation
    val verifyCoreIsolation = register("verifyCoreIsolation") {
        group = "verification"
        description = "Fails if the core package imports Minecraft, Forge or other game classes"
        val coreDir = file("src/main/java/${basePackage.replace('.', '/')}/core")
        val root = projectDir
        inputs.dir(coreDir)
        doLast {
            val ownPackage = Regex.escape(basePackage)
            val forbidden = Regex("""^\s*import\s+(static\s+)?(net\.minecraft|net\.minecraftforge|com\.mojang|$ownPackage\.(?!core\b))""")
            val violations = coreDir.walkTopDown().filter { it.extension == "java" }.flatMap { file ->
                file.readLines().withIndex().filter { forbidden.containsMatchIn(it.value) }
                    .map { "${file.relativeTo(root)}:${it.index + 1}: ${it.value.trim()}" }
            }.toList()
            if (violations.isNotEmpty()) {
                throw GradleException("Core logic must not depend on the game:\n" + violations.joinToString("\n"))
            }
        }
    }

    check {
        dependsOn(verifyCoreIsolation)
    }

    jar {
        // License files bundled into the jar
        from(files("LICENSE", "LICENSE-ART", "LICENSE-DUAL")) {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

// ---- Changelog (CHANGELOG.md, Keep a Changelog format) ----

/** Matches a version heading like "## [4.0.0] - 2026-10-12" or "## [Unreleased]". */
val versionHeading = Regex("""(?m)^## \[([^\]]+)]""")

/**
 * Adds a one-line entry under "Unreleased":
 * ./gradlew changelogEntry --type=fixed --entry="Grow light texture"
 */
abstract class ChangelogEntryTask : DefaultTask() {
    @get:Input
    @get:Option(option = "type", description = "added, changed, fixed or removed")
    abstract val type: Property<String>

    @get:Input
    @get:Option(option = "entry", description = "The change, one line")
    abstract val entry: Property<String>

    @get:Internal
    abstract val changelog: RegularFileProperty

    @TaskAction
    fun addEntry() {
        val section = type.get().lowercase().replaceFirstChar { it.uppercase() }
        require(section in listOf("Added", "Changed", "Fixed", "Removed")) { "--type must be added, changed, fixed or removed" }
        val line = "- " + entry.get().trim()
        val file = changelog.get().asFile
        val text = file.readText()
        val start = text.indexOf("## [Unreleased]")
        require(start >= 0) { "CHANGELOG.md has no '## [Unreleased]' section" }
        val headings = Regex("""(?m)^## \[""").findAll(text).map { it.range.first }.filter { it > start }
        val end = headings.firstOrNull() ?: text.length
        var block = text.substring(start, end).trimEnd()
        val heading = "### $section"
        block = if (block.contains(heading)) {
            // insert after the last entry of that subsection
            val sectionStart = block.indexOf(heading)
            val nextSection = block.indexOf("\n### ", sectionStart + heading.length).let { if (it < 0) block.length else it }
            block.substring(0, nextSection).trimEnd() + "\n" + line +
                    (if (nextSection < block.length) "\n" + block.substring(nextSection) else "")
        } else {
            "$block\n\n$heading\n\n$line"
        }
        file.writeText(text.substring(0, start) + block + "\n\n" + text.substring(end).trimStart().let { if (it.isEmpty()) "" else it })
        println("Added to $section: ${entry.get().trim()}")
    }
}

tasks.register<ChangelogEntryTask>("changelogEntry") {
    group = "release"
    description = "Adds an entry under Unreleased in CHANGELOG.md (--type=added|changed|fixed|removed --entry=\"...\")"
    changelog = layout.projectDirectory.file("CHANGELOG.md")
}

/**
 * Turns "Unreleased" into "## [<mod_version>] - <today>" and starts a new empty "Unreleased" section.
 * Run once before publishing a release.
 */
tasks.register("releaseChangelog") {
    group = "release"
    description = "Moves the Unreleased entries of CHANGELOG.md into a section for mod_version"
    val file = layout.projectDirectory.file("CHANGELOG.md").asFile
    val version = providers.gradleProperty("mod_version").get()
    doLast {
        val text = file.readText()
        val headings = versionHeading.findAll(text).toList()
        val unreleased = headings.firstOrNull { it.groupValues[1] == "Unreleased" }
            ?: throw GradleException("CHANGELOG.md has no '## [Unreleased]' section")
        if (headings.any { it.groupValues[1] == version }) {
            throw GradleException("CHANGELOG.md already has a section for $version; raise mod_version first")
        }
        val end = headings.firstOrNull { it.range.first > unreleased.range.first }?.range?.first ?: text.length
        val body = text.substring(unreleased.range.last + 1, end)
        // drop subsections without entries
        val sections = body.split(Regex("""(?m)^(?=### )""")).map { it.trim() }.filter { it.isNotEmpty() }
            .filter { section -> section.lines().any { it.startsWith("- ") } }
        if (sections.isEmpty()) throw GradleException("Nothing to release: Unreleased has no entries")
        val released = "## [$version] - ${LocalDate.now()}\n\n" + sections.joinToString("\n\n")
        val newText = text.substring(0, unreleased.range.first) + "## [Unreleased]\n\n" + released + "\n" +
                text.substring(end).let { if (it.isBlank()) "" else "\n" + it.trimStart() }
        file.writeText(newText)
        println("CHANGELOG.md: released ${sections.sumOf { s -> s.lines().count { it.startsWith("- ") } }} entries as $version")
    }
}

// ---- Publishing to CurseForge / Modrinth ----

/** A token from the environment (CI) or ~/.gradle/gradle.properties (local); never from the repository. */
fun token(environmentVariable: String, gradleProperty: String): Provider<String> =
    providers.environmentVariable(environmentVariable).orElse(providers.gradleProperty(gradleProperty))

val curseforgeToken = token("CURSEFORGE_TOKEN", "curseforge_token")
val modrinthToken = token("MODRINTH_TOKEN", "modrinth_token")
val curseforgeProjectId = (findProperty("curseforge_project_id") as String?).orEmpty()
val modrinthProjectId = (findProperty("modrinth_project_id") as String?).orEmpty()
/** publish_enabled=false keeps every upload a dry run, tokens or not. */
val publishEnabled = (findProperty("publish_enabled") as String?)?.toBoolean() ?: true

/** True if this site would upload without a token (an empty project id skips the site entirely). */
fun missingToken(projectId: String, token: Provider<String>): Provider<Boolean> =
    if (projectId.isEmpty()) providers.provider { false } else token.map { false }.orElse(true)

/**
 * The CHANGELOG.md section of this mod_version (without its heading). Run ./gradlew releaseChangelog first;
 * publishing fails if the section is missing.
 */
val releaseNotes: Provider<String> = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
    .map { text ->
        val version = property("mod_version") as String
        val headings = Regex("""(?m)^## \[([^\]]+)].*$""").findAll(text).toList()
        val heading = headings.firstOrNull { it.groupValues[1] == version }
            ?: throw GradleException("CHANGELOG.md has no section for $version; run ./gradlew releaseChangelog first")
        val end = headings.firstOrNull { it.range.first > heading.range.first }?.range?.first ?: text.length
        text.substring(heading.range.last + 1, end).trim()
    }

publishMods {
    // the reobfuscated (SRG) jar is the one players install
    file = tasks.named<org.gradle.jvm.tasks.Jar>("reobfJar").flatMap { it.archiveFile }
    displayName = "$modName ${project.version}"
    version = project.version.toString()
    changelog = releaseNotes
    type = when (property("release_type")) {
        "release" -> STABLE
        "beta" -> BETA
        else -> ALPHA
    }
    modLoaders.add("forge")
    // Uploads only run for real when publishing is enabled and the tokens of all configured sites are present
    dryRun = missingToken(curseforgeProjectId, curseforgeToken)
        .zip(missingToken(modrinthProjectId, modrinthToken)) { curseforgeDry, modrinthDry ->
            !publishEnabled || curseforgeDry || modrinthDry
        }

    if (curseforgeProjectId.isNotEmpty()) {
        curseforge {
            projectId = curseforgeProjectId
            accessToken = curseforgeToken
            minecraftVersions.add(minecraftVersion)
            javaVersions.add(JavaVersion.toVersion(javaVersion))
            client = true
            server = true
        }
    }
    if (modrinthProjectId.isNotEmpty()) {
        modrinth {
            projectId = modrinthProjectId
            accessToken = modrinthToken
            minecraftVersions.add(minecraftVersion)
            environment = CLIENT_AND_SERVER
        }
    }
}
