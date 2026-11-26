# CLAUDE.md — Open Modular Lighting (Fabric, Forge; Minecraft 1.20.1)

This file guides Claude Code when working in this repository.
Read it fully before making changes; the workspace `../CLAUDE.md` (git, OMLib submodule, core, CI, releases) and the root `../../CLAUDE.md` (cross-version rules) apply on top.

---

## 1. Project Overview

**Open Modular Lighting (OML)** is the port of Keridos' Forge 1.7.10 mod *FloodLights* (package `de.keridos.floodlights`, mod ID `FloodLights`) to **Fabric and Forge**, targeting **Minecraft 1.20.1** using **Stonecutter** (two nodes, `1.20.1-fabric` and `1.20.1-forge`, see §4).
This is the branch `1.20.1`; the sibling branches are `1.21.1` and `1.21.11` (Fabric and NeoForge), the 1.12.2 port is on `1.12`, and the old multi-version `1.20.1+` is frozen.
OML was the test run for the multi-loader setup that OpenModularTurrets (`../OpenModularTurrets`) follows; the build logic and the code both mods share now live in OMLib (§4 "OMLib").

`oldmod/` is the **read-only reference** for the port: the 1.7.10 source, git-ignored and not in this clone (a copy is in `../../OMTTeam1.20.1+/OpenModularLighting/oldmod`).
Never edit it and never build it.
It is a source of ideas, values and assets, not a specification: the beam thinning, the persistent light blocks and other details differ on purpose.
Do not copy its structure (static singletons, metadata, `ForgeDirection`, CoFH API).
Key classes: `tileentity/TileEntityMetaFloodlight` (beam shapes), `tileentity/TileEntityPhantomLight` (source tracking), `tileentity/TileEntityFLElectric` (energy), `handler/ConfigHandler`, `handler/RecipeHandler`.

Core gameplay:

- **Floodlights** light large areas with invisible light blocks ("phantom lights") along a beam.
  The beam can be a straight line, a narrow cone or a wide cone.
  The phantom lights stay in place and are only switched on and off (see §6.3).
- **Grow light**: lights the block in front and gives random growth ticks to a crop two blocks ahead.
- **UV floodlight**: a short beam of UV light blocks that damages living entities standing in it.
- **Fluorescent lights**: small, cheap lights that illuminate their 5 neighbouring blocks.

### Allowed dependencies

- Fabric Loader and **Fabric API**.
  Fabric API, including Transfer API, Networking, Rendering and Datagen, may be used freely.
- **Team Reborn Energy** for power, as the de facto Fabric energy standard.
  It replaces the old RF (CoFH) and IC2 EU support.
  OMLib bundles it (jar-in-jar) and holds the adapters; OML's own Fabric build only uses it in the game tests (test battery).
- **Jade** (approved by the user) as an **optional** integration only: `modCompileOnly` + `modLocalRuntime` from Modrinth's maven (`maven.modrinth:jade`, version in `versions/<node>/gradle.properties`), on both loaders.
  Never bundled and never a hard dependency (`suggests` in `fabric.mod.json`, optional (`mandatory = false`) in `mods.toml`).
- **NEEPMeat** (Fabric, Modrinth `maven.modrinth:neepmeat`, version in `versions/1.20.1-fabric/gradle.properties`; requested by the user on 2026-11-04): its vascular network charges the electric lights through OMLib (`platform/fabric/FabricNeepMeatPower`, registered by `FabricBlockLookups` when NEEPMeat is loaded; 1 eJ = 1 E, a full buffer takes nothing), so the mod has no code of its own for it.
  Never bundled (`suggests` in `fabric.mod.json`); `modCompileOnly` for the game test `gametest/platform/fabric/NeepMeatGameTest`, and `modLocalRuntime` with the mods nested in its jar (Loom leaves them out of its remapped copy; OMLib's `nestedModJars`) and **GeckoLib**, which it requires, for the dev runs and the game test server.
- **Forge** 47.x, built with the legacy variant of ModDevGradle (`net.neoforged.moddev.legacyforge`; build-time only, approved by the user), requested by the user.
  On Forge, the Fabric-only APIs above are replaced by Forge's own (capabilities, events, event network channels); see §4 "Loaders" and "Forge 1.20.1".
- Build and test tools only, never in the jar (approved by the user, as in OMT): **ArchUnit** (test dependency), **JaCoCo**, **PIT** (plain `JavaExec`, no Gradle plugin) and **PMD**; versions in `gradle.properties`.
- **OpenModularCore** (the shared game rules, `../../OpenModularCore`, approved by the user with JitPack on 2026-11-16): module `oml-core` from JitPack, bundled into the jar; see §4 "Core Game Logic Source".
- **OMLib** (the user's own library, `../OMLib`, branch `1.20.1`), mounted as the git submodule `omlib` and bundled into every jar (jar-in-jar; requested by the user on 2026-10-27); see §4 "OMLib".
- Nothing else without asking the user first.
  No Architectury, no Cloth Config, no Kotlin.
- Old optional integrations (IC2, BuildCraft/CoFH/EnderIO wrenches, EnderIO wireless charging, IGW, NEI, AgriCraft, Colored Light Core) are **not** ported unless the user asks.
  WAILA is replaced by the Jade plugin; WTHIT is not supported (the user chose Jade only).

---

## 2. Tech Stack

| | 1.20.1 (Fabric) | 1.20.1 (Forge) |
| --- | --- | --- |
| Java | 17 | 17 |
| Loader | Fabric Loader 0.19.x | Forge 47.4.x |
| API | Fabric API `0.92.x+1.20.1` | – |
| Gradle plugin | Loom 1.18 | ModDevGradle 2.0 (legacy) |
| Mappings | Mojang official (+ Parchment optional) | Mojang official (jar reobfuscated to SRG) |

- Build system: Gradle 9.8 (Kotlin DSL) + **Stonecutter 0.9**, with Fabric Loom 1.18 (`net.fabricmc.fabric-loom-remap`, the variant for obfuscated MC versions) on the Fabric node and ModDevGradle 2.0's legacy variant (`net.neoforged.moddev.legacyforge`) on the Forge node.
  **Gradle itself must run on Java 25+** (Loom 1.18); the compile toolchain (17, from `java_version` in the node's `gradle.properties`) is auto-provisioned by foojay.
- Shared build logic is the convention plugin `omlib/build-logic/src/main/kotlin/mod-common.gradle.kts` (Java toolchain, metadata templating, JUnit, the shared core from OpenModularCore, `verifyNoCoreSources`, `checkRecipes`, licenses in the jar via `bundled_licenses` (default: all three), publishing), from the `omlib` submodule; change it in `../OMLib`, never inside `omlib/` (workspace `CLAUDE.md`, "OMLib in the mods").
  The quality tools (ArchUnit, JaCoCo, PIT, PMD) stay in `build.fabric.gradle.kts`, as in OMT.
  The loader plugins are not on the build logic's classpath; they are applied by version in `stonecutter.gradle.kts` (`apply false`) and used by the loader scripts.
- Formatting: **Spotless** (in `stonecutter.gradle.kts`).
  It is deliberately light: tabs → spaces, trailing whitespace, final newline.
  No full formatter and no import reordering, because those would break the Stonecutter conditional comments.
  Run `./gradlew spotlessApply` before committing.
- The configure step prints "Stonecutter branch root: should not be a buildable project".
  This is expected (Spotless on the root project) and harmless.
- **Use Mojang mappings** (`loom.officialMojangMappings()`).
  All class and method names in code and in this doc are Mojmap names (`BlockEntity`, `ResourceLocation`, `AbstractContainerMenu`, and so on).
- Language: Java only.

Check exact latest patch versions of Loader, API, Loom and Stonecutter when setting up or bumping.
Do not hard-code guesses into the build without verifying.

---

## 3. Repository Layout

```text
.
├── CLAUDE.md
├── CHANGELOG.md
├── README.md                   # player-facing overview; ends with the LICENSE-DUAL text
├── CONTRIBUTING.md             # contributor guide (adapted from OpenModularTurrets)
├── CLA.md                      # contributor licence agreement (same terms as OpenModularTurrets)
├── LICENSE                     # MIT, source code
├── LICENSE-ART                 # CC BY-NC-SA 4.0, art (textures, models, sounds)
├── LICENSE-DUAL                # which license covers what; all three are bundled into the jar
├── build.fabric.gradle.kts     # Fabric node (Loom, Fabric API, datagen, GameTests, ArchUnit/JaCoCo/PIT/PMD)
├── gradle/pmd/ruleset.xml      # PMD rules (complexity and size, report only; same as OMT's)
├── build.forge.gradle.kts      # Forge node (legacy ModDevGradle: reobfJar, remapped mod* dependencies, pack.mcmeta)
├── omlib/                      # git submodule (OMLib, branch 1.20.1): the library (src/) every mod bundles, and build-logic/ (the mod-common convention plugin); see §4 "OMLib"
├── stonecutter.gradle.kts      # Stonecutter controller (active version, loader constants, chiseled tasks, Spotless)
├── settings.gradle.kts         # declares the Stonecutter nodes (<minecraft version>-<loader>)
├── gradle.properties           # shared properties (mod_id, mod_version, maven_group, core_version …)
├── docs/id-mapping.md          # 1.7.10 ID → new ID table
├── docs/architecture.md        # overview of the code for developers (layers, flows, how to add things)
├── oldmod/                     # 1.7.10 Forge source, reference only (git-ignored; not in this clone, see §1)
├── versions/
│   ├── 1.20.1-fabric/
│   │   ├── gradle.properties   # minecraft_version, fabric_api, java_version=17 …
│   │   └── src/main/generated/ # datagen output (committed; Forge converts it, datagen_from)
│   └── 1.20.1-forge/
│       └── gradle.properties   # forge_version, forge_dependency, minecraft_dependency (Maven range) …
└── src/
    ├── main/java/omtteam/openmodularlighting/...
    ├── main/resources/         # SHARED assets + fabric.mod.json and META-INF/mods.toml (templated)
    ├── client/java/omtteam/openmodularlighting/...   # client-only code (split source sets)
    ├── test/java/omtteam/openmodularlighting/...      # JUnit tests (config, architecture); the core's tests are in OpenModularCore
    └── gametest/                                     # in-world GameTests: separate test mod, never shipped
```

- **Mod ID:** `openmodularlighting`.
  **Base package:** `omtteam.openmodularlighting` (keep it).
- Use Loom **split environment source sets** (`splitEnvironmentSourceSets()`).
  Client-only code (renderers, screens, models, client networking handlers) goes in `src/client/java`.
  Never reference client classes from `main`.
  The Forge node keeps the same split: `build.forge.gradle.kts` creates a `client` source set that sees `main`, and the jar contains both.
- `fabric.mod.json` and `META-INF/mods.toml` (Forge) are templated via `processResources` (`mod-common`); each jar only contains its own loader's file (also in the test mod).
  Available keys: `${version}`, `${mod_id}`, `${mod_name}`, `${java_version}`, `${minecraft_dependency}`, `${loader_version}` (Fabric), `${forge_dependency}`, `${forge_loader_range}`, `${omlib_version}`.
- Version and jar names (`<mod_version>+1.20.1-<loader>`, `<mod_id>-<mod_version>-mc1.20.1-<loader>.jar`): workspace `CLAUDE.md`, "Versions and releases".

---

## 4. Stonecutter

### Setup (`settings.gradle.kts`)

```kotlin
plugins { id("dev.kikugie.stonecutter") version "0.9.8" }

stonecutter {
    create(rootProject) {
        versions("1.20.1-fabric" to "1.20.1", "1.20.1-forge" to "1.20.1")
        mapBuilds { _, node -> "build.${node.project.substringAfterLast('-')}.gradle.kts" }
        vcsVersion = "1.20.1-fabric" // the version the committed source is written for
    }
}
```

The Stonecutter DSL changes between releases.
If this does not match the installed version, check the Stonecutter docs and update both this file and the build.

### Rules

- **Committed source must always be in the `vcsVersion` (`1.20.1-fabric`) state.** Before committing, switch back if you switched to the Forge node (`./gradlew "Reset active project"` or `"Set active project to 1.20.1-fabric"`).
- Stonecutter's only axis on this branch is the loader (`fabric` or `forge`); Minecraft-version differences are not conditions here: each Minecraft version has its own branch, and a gameplay change made on one is ported to the others by hand.
  Loader-conditional code uses Stonecutter comments (constants `//? if fabric {`, `//? if forge {`):

  ```java
  //? if fabric {
  return new ClientPlatformServices(new FabricClientNetwork());
  //?} else {
  /*return new ClientPlatformServices(new ForgeClientNetwork((ForgeNetwork) Platform.network()));
  *///?}
  ```

  (from OMLib's `ClientPlatform`.)
  Inactive branches must be wrapped in `/* … */` exactly as Stonecutter expects.
  Prefer letting Stonecutter generate this by switching nodes over hand-writing it.
- **Minimize conditional blocks.** Hide differences behind small helpers in OMLib's `omtteam.omlib.compat.VersionCompat` (or similar) or behind the service interfaces in OMLib's `platform`, rather than sprinkling `//? if` across gameplay code; the compat helpers keep the call sites the same on every version branch, which is what makes porting between the branches by hand cheap.
  Gameplay logic (beam shapes, energy use, growth) should be loader-agnostic.
- Node-specific *properties* live in `versions/<node>/gradle.properties`.
  Read them in the build via `property("…")`.

### Loaders: publishing

- Forge is published like the Fabric node (Forge uploads `reobfJar`).
  `publish_enabled=false` in a node's `gradle.properties` would make that node a dry run even with tokens (`mod-common`).

### Commands

```bash
./gradlew "Set active project to 1.20.1-forge"    # switch the source to the Forge node
./gradlew "Set active project to 1.20.1-fabric"   # switch back (VCS state)
./gradlew chiseledBuild                           # build both nodes (custom task in stonecutter.gradle.kts)
./gradlew :1.20.1-fabric:build                    # build a single node
./gradlew :1.20.1-fabric:runClient                # run client for a node (run dir versions/<node>/run, one per node)
./gradlew :1.20.1-fabric:runServer
./gradlew spotlessApply                           # fix formatting (spotlessCheck verifies it)
./gradlew :1.20.1-fabric:runGameTest              # in-world GameTests for the Fabric node (also part of check/build)
./gradlew :1.20.1-fabric:runDatagen               # regenerate data (the Forge node converts this output)
./gradlew :1.20.1-forge:runClient                 # Forge client (run dir versions/1.20.1-forge/run)
./gradlew :1.20.1-forge:runGameTestServer         # Forge GameTests (also part of check/build)
./gradlew :1.20.1-fabric:pitest                   # mutation testing of the config classes -> versions/1.20.1-fabric/build/reports/pitest/index.html
./gradlew chiseledPublish                         # upload both nodes to CurseForge/Modrinth (the user only)
./gradlew :1.20.1-fabric:publishMods              # upload one node (the user only)
```

### Publishing

- Uses `me.modmuss50.mod-publish-plugin` (approved by the user), configured in `publishMods` in `omlib/build-logic/src/main/kotlin/mod-common.gradle.kts` (the loader scripts only set the jar to upload).
- Settings in `gradle.properties`: `release_type` (release/beta/alpha), `curseforge_project_id` and `modrinth_project_id`.
  An empty ID skips that site.
  Both are set (CurseForge `268178`, Modrinth `4yAD8NG9`); ask the user before changing them.
  The old 1.7.10 CurseForge page is `224728-floodlights`.
- Tokens are never committed: environment variables `CURSEFORGE_TOKEN` / `MODRINTH_TOKEN`, or `curseforge_token` / `modrinth_token` in `~/.gradle/gradle.properties`.
  Without a token the upload is a dry run (output in `versions/<node>/build/publishMods/`).
- **Changelog** (`CHANGELOG.md`, Keep a Changelog format): every user-visible change gets a one-line entry under `## [Unreleased]` (Added / Changed / Fixed / Removed), by hand or via `./gradlew changelogEntry --type=fixed --entry="…"`.
  Add the entry together with the change.
- **Release steps** (the user releases and pushes; never run `chiseledPublish` or push from here): set `mod_version` (e.g. `1.0.1`) and `release_type` → `./gradlew releaseChangelog` (moves Unreleased into `## [<mod_version>] - <date>`) → `./gradlew chiseledBuild` → commit `gradle.properties` and `CHANGELOG.md` as `feat: version <mod_version>` → tag that commit `<mod_version>+1.20.1` (workspace `CLAUDE.md`, "Release tags"; Modrinth version numbers must not clash with the 1.12.2 builds) → `./gradlew chiseledPublish` → push the branch and the tag.
  Publishing uploads only that version's section and fails if it is missing.

**Definition of done for any change:** `./gradlew chiseledBuild` succeeds for **both** nodes (this includes the JUnit tests and the GameTests), and the source is left in the `1.20.1-fabric` state.

### Docs and checks

The mod's own unit tests (config, architecture rules) live in `src/test/java` (JUnit, test-only dependency).
`./gradlew check` also runs `checkRecipes` (from `mod-common`): every item (item model) of the mod needs a recipe on that node, unless `recipe-exempt.txt` lists it as `<id> # <reason>`, and every recipe result of the mod's namespace must be one of its items.

Quality tools (taken over from OMT; details in `docs/architecture.md`, "Rules the build checks" and "Measuring code quality"):
`ArchitectureTest` (`src/test/.../architecture`, ArchUnit, Fabric node only) checks that main never uses client code, and package cycles against its `KNOWN_CYCLES` list (a ratchet: update the list when a cycle shrinks; never add to it without asking the user).
PMD (`gradle/pmd/ruleset.xml`, report only, active version) measures complexity and size: `versions/1.20.1-fabric/build/reports/pmd/`.
JaCoCo records coverage in the unit tests and on the game test server; `check` prints the line coverage of the mod's own classes (without the core) per top-level package (active version only).
PIT (`./gradlew :1.20.1-fabric:pitest`, on demand, not part of `check`) runs mutation testing on the config classes (`pitestTargets`); surviving mutations show behaviour the tests do not pin down.
It fails when the results get worse than the thresholds in `gradle.properties` (`pitest_*`); see `docs/architecture.md`.

## Where the rest lives (nested `CLAUDE.md` files)

The sections of this file that explain one subsystem were moved into a `CLAUDE.md` in the folder of that subsystem's code.
A nested `CLAUDE.md` is loaded only when you read files in its folder; read the one for the area you work in before changing it.
The § numbers and headings of the moved sections (§5, §6.x) are kept, so references such as "§6.3" in the text still resolve.

| `CLAUDE.md` in | What it covers (former sections) |
| --- | --- |
| `src/main/java/omtteam/openmodularlighting/` | §6.1 package layout of `src/main`; §4 "OMLib (the library)"; §4 "Core Game Logic Source" (how the core is wired in, its interfaces, the reference beam loops); §5 compat helpers (the porting layer) |
| `src/main/java/omtteam/openmodularlighting/platform/` | §4 "Loaders" (loader-specific classes, services, start-up) and "Forge 1.20.1" (build, data, entrypoints, services, capabilities, recipe condition, client) |
| `src/main/java/omtteam/openmodularlighting/block/` | §6.2 blocks and items (id table, recipes, config switches, panel lights), §6.6 interaction |
| `src/main/java/omtteam/openmodularlighting/blockentity/` | §6.3 light mechanics (phantom lights, casting, re-cast, beam shapes), §6.4 light state machine, §6.7 block entity data, §6.8 grow light, §6.9 UV light damage |
| `src/main/java/omtteam/openmodularlighting/energy/` | §6.5 power and fuel |
| `src/main/java/omtteam/openmodularlighting/config/` | §6.10 config (fields, versioning) |
| `src/main/java/omtteam/openmodularlighting/camo/` | §6.11 disguise (camouflage), differences from OpenModularTurrets' camouflage |
| `src/main/` | §5 "Resources strategy" (assets, datagen, no generated art) |
| `src/client/java/omtteam/openmodularlighting/client/` | §6.1 client package layout, client compat helpers, Jade details |
| `src/gametest/` | §4 "GameTests" and the Forge 1.20.1 game test server (its flat test world and game rules) |

---

## 7. Change Workflow

1. Implement gameplay logic in the core (with unit tests) where possible, in OpenModularCore (root `CLAUDE.md`, "Changing the core"); keep Minecraft-facing code thin.
2. Wrap loader differences in `platform/` and Minecraft-version differences in OMLib's `compat` helpers.
3. Add assets, lang entries and datagen entries; regenerate the data (`:1.20.1-fabric:runDatagen`; the Forge node converts it).
4. Add a `CHANGELOG.md` entry for user-visible changes.
5. Update the documentation the change touches.
   "Updating the docs" always means everything in the `docs/` folder (`architecture.md`, `id-mapping.md`, …) plus `README.md` and `CONTRIBUTING.md`: check each of them against the change, not only the one that comes to mind.
6. Run `./gradlew chiseledBuild`.

- Every variant of subblocks is an **individual registered block/item** with a lowercase snake_case ID.
  `docs/id-mapping.md` maps the 1.7.10 IDs to the current ones.
- New APIs (public interfaces for other mods and new cross-cutting interfaces inside the mod) are designed step by step with the user: workspace `CLAUDE.md`, "Designing APIs".

### Code shared with OpenModularTurrets

The code both mods share is in OMLib (§4 "OMLib"), which both bundle.
What is still duplicated in both repositories is listed in `omlib/shared-sources.txt` (the game test `mods.toml`, the PMD ruleset, the Gradle wrapper properties); after changing one, run `python3 -I omlib/tools/shared_sources.py sync --from . --write . ../OpenModularTurrets` and then `check`.
OpenModularTurrets also has camo light properties, a camouflage trap, the memory card handling of camouflage and its own `ScreenTexts`.
---

## 8. Coding Conventions

- Java, 4-space indent, no wildcard imports.
- Registry IDs and resource names: lowercase `snake_case`.
- Keep gameplay logic **server-side**.
  Clients only render and send requests.
- No static mutable state for per-world data.
  Light state lives in block entities.
- Use `Level#isClientSide()` checks properly; never load client classes on the server.
- All user-facing text goes through translation keys (`block.openmodularlighting.x`, `item.openmodularlighting.x`, `gui.openmodularlighting.x`, `tooltip.openmodularlighting.x`).
- Prefer small, focused classes.
- Add a short Javadoc to any non-obvious formula.
- Markdown files: one sentence per line, the sentences of a paragraph on consecutive lines; `CHANGELOG.md` entries stay one line each.
- Don't add new features or change balance values unless the user asks.

---

## 9. Open items

The port, the Forge node, the Jade tooltip, the light debug tool and the disguise are done, with GameTests.
Still open:

- [ ] Checked by hand in a Forge client (`:1.20.1-forge:runClient`): screens, tint, camouflage rendering, debug renderer, Jade, the config sync over the event channels (joining and placing every block are covered by the workspace's `checkJoin`).
- [ ] Disguise rendering not yet checked by hand.
- **OMLib's own Fabric version range** is still `~1.20.1`; the mods' exact ranges keep a wrong Minecraft version from loading, so it only matters for OMLib alone.
- **Phantom lights are not air** for vanilla checks (§6.3): sugar cane, cactus and bamboo do not grow into them, a nether portal frame with one inside does not light.
