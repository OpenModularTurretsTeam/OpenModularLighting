// Fabric node (Loom). Shared logic (Java, metadata templating, tests, publishing) is in omlib/build-logic (mod-common).
plugins {
    id("mod-common")
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
    // Code quality report (gradle/pmd/ruleset.xml): complexity and size, report only
    pmd
    // Test coverage of the mod's own classes, from the unit tests and the game tests
    jacoco
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
        // versions/<node>/run: one per node, so mods added by hand to run/mods and worlds stay with their node
        runDirectory = project.file("run")
    }
}

fabricApi {
    configureDataGeneration {
        // The providers need no client classes (block models are handwritten), so datagen runs without the client
        // source set; it also works while a new Minecraft version's client code is not ported yet
        client = false
        // versions/<node>/src/main/generated, committed
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

    // The Fabric game tests use the energy API directly; the mod itself gets it, bundled, through OMLib
    modImplementation("teamreborn:energy:${property("team_reborn_energy_version")}")

    // OMLib (git submodule omlib, built before this project is configured): a mod of its own, jar-in-jar. In the dev
    // environment its own dependencies (Fabric API, Team Reborn Energy) come through it; only the library itself is
    // bundled (it bundles the energy API itself).
    val omlib = project.extra["omlibCoordinates"] as String
    modImplementation(omlib)
    "include"(omlib) { isTransitive = false }

    // Jade plugin (compat/jade, client/compat/jade): optional, not bundled; present in dev runs to try it
    modCompileOnly("maven.modrinth:jade:${property("jade_version")}")
    modLocalRuntime("maven.modrinth:jade:${property("jade_version")}")

    // Architecture rules (src/test/.../architecture): they read the compiled main and client classes
    testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunit_version")}")
    testRuntimeOnly(sourceSets["client"].output)
}

// ---- Test coverage (JaCoCo): check prints the line coverage of the mod's own classes ----
// Both the unit tests and the game test server record coverage. Active node only (the nodes share the source).
// The core (omtteam.openmodularlighting.core, bundled from OpenModularCore) is measured in the core repository.
// Report: versions/<node>/build/reports/jacoco/test/html/index.html
val coverageActive = stonecutter.current.isActive
jacoco {
    toolVersion = property("jacoco_version") as String
}
tasks.test {
    extensions.configure<JacocoTaskExtension> { isEnabled = coverageActive }
}
tasks.named<JavaExec>("runGameTest") {
    jacoco.applyTo(this)
    extensions.configure<JacocoTaskExtension> { isEnabled = coverageActive }
}
tasks.jacocoTestReport {
    enabled = coverageActive
    dependsOn(tasks.test, tasks.named("runGameTest"))
    executionData.setFrom(layout.buildDirectory.files("jacoco/test.exec", "jacoco/runGameTest.exec"))
    sourceDirectories.setFrom(sourceSets.main.get().java.srcDirs)
    // only the mod's own classes: main's classes folders also hold the bundled OpenModularCore classes (mod-common)
    classDirectories.setFrom(sourceSets.main.get().java.classesDirectory)
    reports {
        csv.required = true
        xml.required = true
        html.required = true
    }
    val csv = reports.csv.outputLocation
    val html = reports.html.outputLocation
    doLast {
        // line coverage per top-level package (below omtteam.openmodularlighting; "(root)" for the package itself)
        val rows = csv.get().asFile.readLines().drop(1).map { it.split(',') }
        val byArea = sortedMapOf<String, IntArray>()
        for (row in rows) {
            val name = row[1].removePrefix("omtteam.openmodularlighting").removePrefix(".")
            val lines = byArea.getOrPut(name.substringBefore('.').ifEmpty { "(root)" }) { IntArray(2) }
            lines[0] += row[7].toInt() // LINE_MISSED
            lines[1] += row[8].toInt() // LINE_COVERED
        }
        fun percent(l: IntArray) = "%5.1f%%  (%d/%d lines)".format(100.0 * l[1] / maxOf(1, l[0] + l[1]), l[1], l[0] + l[1])
        val total = IntArray(2).also { t -> byArea.values.forEach { t[0] += it[0]; t[1] += it[1] } }
        println("Test coverage (unit and game tests, without the core):")
        println("  %-24s %s".format("all", percent(total)))
        byArea.forEach { (area, lines) -> println("    %-22s %s".format(area, percent(lines))) }
        println("  Report: ${html.get().asFile.resolve("index.html").toURI()}")
    }
}
tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

// ---- Mutation testing (PIT): ./gradlew :1.21.1-fabric:pitest, on demand ----
// Plants small mistakes in the config classes (flipped conditions, changed boundaries, removed calls) and checks whether
// a unit test fails. Surviving mistakes show behaviour the tests do not pin down. Report only, not part of check.
// Report: versions/<node>/build/reports/pitest/index.html
// The core (omtteam.openmodularlighting.core) is mutation-tested in OpenModularCore; OMLConfig needs the running game.
val pitestTargets = listOf(
    "omtteam.openmodularlighting.config.ConfigData*",
    "omtteam.openmodularlighting.config.ConfigMigrationSteps*",
)
val pitest = configurations.create("pitest") {
    isCanBeConsumed = false
}
dependencies {
    pitest("org.pitest:pitest-command-line:${property("pitest_version")}")
    pitest("org.pitest:pitest-junit5-plugin:${property("pitest_junit5_version")}")
}
tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Mutation testing of the config classes (report in build/reports/pitest)"
    dependsOn(tasks.testClasses)
    classpath = pitest + sourceSets.test.get().runtimeClasspath
    mainClass = "org.pitest.mutationtest.commandline.MutationCoverageReport"
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
    val reportDir = layout.buildDirectory.dir("reports/pitest")
    val sources = sourceSets.main.get().java.srcDirs
    outputs.dir(reportDir)
    // fail on a degradation: % of mutations detected, % line coverage of the mutated classes, mutations not detected
    // (survived or not reached by any test); the values are in gradle.properties
    val thresholds = listOf(
        "--mutationThreshold", project.property("pitest_mutation_threshold").toString(),
        "--coverageThreshold", project.property("pitest_coverage_threshold").toString(),
        "--maxSurviving", project.property("pitest_max_surviving").toString(),
    )
    inputs.property("thresholds", thresholds)
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "--reportDir", reportDir.get().asFile.path,
            "--targetClasses", pitestTargets.joinToString(","),
            // the config tests only: ArchitectureTest (ArchUnit) crashes PIT's coverage minion and tests nothing here
            "--targetTests", "omtteam.openmodularlighting.config.*",
            "--excludedClasses", "*Test,*Test\$*",
            "--sourceDirs", sources.joinToString(","),
            "--threads", Runtime.getRuntime().availableProcessors().toString(),
            "--outputFormats", "HTML,XML",
            "--timestampedReports=false",
        ) + thresholds
    })
    doLast {
        println("PIT report: ${reportDir.get().asFile.resolve("index.html").toURI()}")
    }
}

// Report only for now: the limits in gradle/pmd/ruleset.xml are not enforced yet. Runs for the active node only
// (the nodes share the source); reports in versions/<node>/build/reports/pmd/{main,client}.html
pmd {
    toolVersion = property("pmd_version") as String
    sourceSets = listOf(project.sourceSets["main"], project.sourceSets["client"])
    ruleSets = listOf()
    ruleSetFiles = rootProject.files("gradle/pmd/ruleset.xml")
    isIgnoreFailures = true
    isConsoleOutput = false
}
tasks.withType<Pmd>().configureEach {
    enabled = stonecutter.current.isActive
    reports {
        html.required = true
        xml.required = true
    }
}

publishMods {
    file = tasks.remapJar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
}
