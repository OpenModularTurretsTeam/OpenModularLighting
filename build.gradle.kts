import java.time.LocalDate
import java.util.Properties

// Forge 1.12.2 build (branch 1.12): one project, RetroFuturaGradle, Java 8, MCP mappings.
// The multi-loader build of the modern versions (Stonecutter, Loom, ModDevGradle) lives on the main branch.
// plugin versions: settings.gradle.kts (and the OMTTeam 1.12 workspace, which builds this file too)
plugins {
    java
    id("com.gtnewhorizons.retrofuturagradle")
    // Uploads to CurseForge / Modrinth (build-time only)
    id("me.modmuss50.mod-publish-plugin")
    // Formatting checks (./gradlew spotlessCheck / spotlessApply)
    id("com.diffplug.spotless")
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
// jars: <mod_id>-<mod_version>-mc<minecraft_version>-forge[-dev|-sources].jar (name, version, Minecraft, loader, as in
// the other OMTTeam 1.12 mods); the mod version and the release tag stay <mod_version>+<minecraft_version>-forge
tasks.withType<Jar>().configureEach {
    archiveVersion = "${project.property("mod_version")}-mc$minecraftVersion-forge"
}

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

repositories {
    // Hwyla from CurseForge (maven.tehnut.info no longer serves it)
    maven("https://cursemaven.com/") {
        content { includeGroup("curse.maven") }
    }
    // OMLib from its publishToMavenLocal (omtteam:omlib:<minecraft_version>-<omlib_version>)
    mavenLocal {
        content { includeGroup("omtteam") }
    }
}

dependencies {
    // OMLib, the shared library of the OpenModularTurretsTeam mods: in the OMTTeam 1.12 workspace the project of the same
    // build, otherwise its dev jar (MCP names) from mavenLocal; deobfuscating its SRG jar misses the abstract Minecraft
    // methods OMLib declares (e.g. CamoBakedModel.getParticleTexture)
    val omlibProject = findProject(":OMLib")
    if (omlibProject != null) {
        // RetroFuturaGradle marks all of OMLib's configurations, so name the one to use
        implementation(project(omlibProject.path, "runtimeElements"))
    } else {
        implementation("omtteam:omlib:$minecraftVersion-${property("omlib_version")}:dev")
    }
    // Hwyla block info overlay (optional at runtime: compat.hwyla is only loaded by Hwyla)
    compileOnly(rfg.deobf("curse.maven:hwyla-253449:${property("hwyla_curse_file_id")}"))

    // Unit tests for the core game logic and the config (test only, not shipped)
    testImplementation(platform("org.junit:junit-bom:${property("junit_version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // equals/hashCode contracts of the core value classes (3.x: the last line running on Java 8)
    testImplementation("nl.jqno.equalsverifier:equalsverifier:${property("equalsverifier_version")}")
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

// OML.DEPENDENCIES says omlib@[0.0.0,) in the sources (dev runs, where OMLib's version stays unreplaced); the jar gets
// [<omlib_version>,<next major>.0.0-alpha), as in OpenModularTurrets and OpenModularPassiveDefense
val omlibVersion = property("omlib_version") as String
val omlibRange = Regex("""^(\d+)(\.\d+)*(-(alpha|beta)\.[1-9]\d*)?$""").matchEntire(omlibVersion)
    ?.let { "[$omlibVersion,${it.groupValues[1].toInt() + 1}.0.0-alpha)" }
    ?: throw GradleException("omlib_version '$omlibVersion' must look like 4.0.0, 4.0.0-alpha.1 or 4.0.0-beta.2")
val modClass = "${basePackage.replace('.', '/')}/OML.java"

val replaceOmlibRange = tasks.register<Sync>("replaceOmlibRange") {
    description = "Copies OML.java with the OMLib version range of omlib_version"
    from("src/main/java") { include(modClass) }
    into(layout.buildDirectory.dir("generated/sources/omlibRange"))
    inputs.property("omlibRange", omlibRange)
    filter { it.replace("omlib@[0.0.0,)", "omlib@$omlibRange") }
}

tasks {
    compileJava {
        // the copy of OML.java instead of the original
        val original = source
        setSource(original.filter { !it.invariantSeparatorsPath.endsWith("/$modClass") })
        source(replaceOmlibRange)
    }

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
            // OMLib's core is allowed (energy buffer), the rest of OMLib is game code
            val forbidden = Regex("""^\s*import\s+(static\s+)?(net\.minecraft|net\.minecraftforge|com\.mojang|($ownPackage|omtteam\.omlib)\.(?!core\b))""")
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

// ---- Mutation testing (PIT): ./gradlew pitest, on demand ----

// Plants small mistakes in core (flipped conditions, changed boundaries, removed calls) and checks whether a unit test
// fails, as gradle/mod-common.gradle does in the other mods. Report only, not part of check:
// build/reports/pitest/index.html. PIT 1.17 is the last release that runs on Java 8.
val pitest: Configuration by configurations.creating {
    // resolved by the pitest task only; not offered to other projects
    isCanBeConsumed = false
}

dependencies {
    pitest("org.pitest:pitest-command-line:${property("pitest_version")}")
    // without its JUnit Platform: the tests' own (testRuntime) is used, an older one breaks test discovery
    pitest("org.pitest:pitest-junit5-plugin:${property("pitest_junit5_version")}") {
        isTransitive = false
    }
}

val toolchainVersion = javaVersion

tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Mutation testing of the core logic (report in build/reports/pitest)"
    dependsOn("testClasses")
    val reportDir = layout.buildDirectory.dir("reports/pitest")
    outputs.dir(reportDir)
    classpath = pitest + sourceSets.test.get().runtimeClasspath
    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")
    // PIT 1.17 and the tests run on the Java 8 toolchain
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(toolchainVersion)) })
    val corePackage = "$basePackage.core"
    val sourceDirs = sourceSets.main.get().java.srcDirs.joinToString(",")
    doFirst {
        args = listOf(
            "--reportDir", reportDir.get().asFile.path,
            "--targetClasses", "$corePackage.*",
            "--targetTests", "$corePackage.*",
            // the tests and their helpers share the core packages (fake world, 1.7.10 reference beams)
            "--excludedClasses", "*Test,*Test$*,*.Fake*,*.TestWorld*,*.Legacy1710Beams*",
            // boilerplate of the classes that stand in for records (Java 8); equals stays mutated
            "--excludedMethods", "toString,hashCode",
            "--sourceDirs", sourceDirs,
            "--threads", Runtime.getRuntime().availableProcessors().toString(),
            "--outputFormats", "HTML,XML",
            "--timestampedReports=false",
        )
    }
    doLast {
        println("PIT report: ${reportDir.get().file("index.html").asFile.toURI()}")
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
            // CurseForge slugs (as curse_relations of OMT and OMPD); on Modrinth OMLib has no project yet
            requires("omlib")
            optional("hwyla")
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

// ---- OMLib requirement in the release notes ----

/** Runs git in the mod folder: its output, or null if git failed (e.g. no tag yet). */
fun git(vararg args: String): String? {
    val process = ProcessBuilder(listOf("git") + args).directory(projectDir).start()
    val out = process.inputStream.bufferedReader().readText()
    process.errorStream.readAllBytes()
    return if (process.waitFor() == 0) out.trim() else null
}

/**
 * Players have to update OMLib too, so a new OMLib requirement belongs into the release notes: fails if omlib_version
 * differs from the one at the previous release tag and the version's CHANGELOG.md section does not mention
 * "OMLib <omlib_version>". Every upload depends on it (as checkOmlibChangelog in OMT and OMPD).
 */
val releaseVersion = property("mod_version") as String

val checkOmlibChangelog = tasks.register("checkOmlibChangelog") {
    group = "release"
    description = "Fails if omlib_version changed since the previous release tag and the version's CHANGELOG.md section does not mention it"
    val notes = releaseNotes
    doLast {
        val tag = git("describe", "--tags", "--abbrev=0") ?: return@doLast
        val before = git("show", "$tag:gradle.properties")
            ?.let { text -> Properties().apply { load(text.reader()) }.getProperty("omlib_version") }
        if (before != omlibVersion && !notes.get().contains("OMLib $omlibVersion")) {
            throw GradleException(
                "omlib_version changed since $tag (${before ?: "none"} -> $omlibVersion), but the CHANGELOG.md section of " +
                    "$releaseVersion does not say so; add \"Requires OMLib $omlibVersion or newer\" under ### Changed"
            )
        }
    }
}

tasks.matching { it.name in setOf("publishMods", "publishCurseforge", "publishModrinth") }.configureEach {
    dependsOn(checkOmlibChangelog)
}

// ---- Releases (./release.sh in the OMTTeam 1.12 workspace, or ./gradlew release) ----

/** The release tag: the full version (1.0.0-beta.6+1.12.2-forge), so it cannot clash with the 1.20.1+ release tags. */
val releaseTag = project.version.toString()

/** The version set in the gradle.properties of the OMLib checkout next to this mod (the workspace), null if there is none. */
fun siblingOmlibVersion(): String? {
    val file = projectDir.resolveSibling("OMLib/gradle.properties")
    if (!file.isFile) return null
    val properties = Properties().apply { file.reader().use { load(it) } }
    val base = properties.getProperty("mod_version")
    val type = properties.getProperty("release_type") ?: "release"
    return if (type == "release") base else "$base-$type.${properties.getProperty("release_number")}"
}

tasks.register("checkRelease") {
    group = "release"
    description = "Checks that the version, its CHANGELOG.md section, the tokens and the git tree are ready for a release"
    dependsOn(checkOmlibChangelog)
    val notes = releaseNotes
    doLast {
        notes.get() // fails if CHANGELOG.md has no section for mod_version
        if (!publishEnabled) {
            throw GradleException("publish_enabled is false in gradle.properties: the uploads would only be dry runs")
        }
        if (curseforgeProjectId.isNotEmpty() && !curseforgeToken.isPresent) {
            throw GradleException("curseforge_token is not set (~/.gradle/gradle.properties)")
        }
        if (modrinthProjectId.isNotEmpty() && !modrinthToken.isPresent) {
            throw GradleException("modrinth_token is not set (~/.gradle/gradle.properties)")
        }
        val omlib = siblingOmlibVersion()
        if (omlib != null && omlib != omlibVersion) {
            throw GradleException("omlib_version is $omlibVersion, but ../OMLib is set to $omlib")
        }
        if (!git("status", "--porcelain").isNullOrEmpty()) {
            throw GradleException("The git working tree has uncommitted changes; commit them before releasing")
        }
        if (!git("tag", "--list", releaseTag).isNullOrEmpty()) {
            throw GradleException("Tag $releaseTag already exists")
        }
        logger.lifecycle("Releasing $modId $releaseTag to CurseForge and Modrinth")
    }
}

tasks.matching { it.name == "publishMods" }.configureEach {
    mustRunAfter("checkRelease", "build")
}

// a release fails on its checks before it spends time on the build
tasks.named("build") {
    mustRunAfter("checkRelease", checkOmlibChangelog)
}

tasks.register("release") {
    group = "release"
    description = "Builds mod_version, uploads it to CurseForge and Modrinth (publishMods) and tags it as <version>"
    dependsOn("checkRelease", "build", "publishMods")
    doLast {
        git("tag", "-a", releaseTag, "-m", "$modName $releaseTag") ?: throw GradleException("git tag $releaseTag failed")
        logger.lifecycle("Tagged $releaseTag")
    }
}
