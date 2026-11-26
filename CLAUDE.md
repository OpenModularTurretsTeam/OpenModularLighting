# CLAUDE.md — Open Modular Lighting (Fabric, NeoForge; Minecraft 1.21.11)

This file guides Claude Code when working in this repository.
Read it fully before making changes.

---

## 1. Project Overview

**Open Modular Lighting (OML)** is the port of Keridos' Forge 1.7.10 mod *FloodLights* (`oldmod/`, package `de.keridos.floodlights`, mod ID `FloodLights`) to **Fabric and NeoForge**, targeting **Minecraft 1.21.11** using **Stonecutter** (two nodes, see §4).
This is the branch `1.21.11` (split off the frozen `1.20.1+` on 2026-11-21); the sibling branches are `1.20.1` (Fabric and Forge) and `1.21.1` (Fabric and NeoForge), the 1.12.2 port is on `1.12`.
Branches, pushing and the workspace build are in the workspace `CLAUDE.md` (`../CLAUDE.md`).
It was the test run for the same setup in OpenModularTurrets (`../OpenModularTurrets`), which follows its patterns; the build logic and the code both mods share now live in OMLib (§4 "OMLib").

`oldmod/` is the **read-only reference** for the port.
It is a local, untracked folder (in `.gitignore`); this clone has none, the copy in this OMM tree is `../../OMTTeam1.20.1+/OpenModularLighting/oldmod/` (the frozen folder).
Never edit it and never build it.
It is a source of ideas, values and assets, not a specification: the beam thinning, the persistent light blocks and other details differ on purpose.
Do not copy its structure (static singletons, metadata, `ForgeDirection`, CoFH API).
`oldmod/` is *FloodLights* for Forge 1.7.10 by Keridos; key classes: `tileentity/TileEntityMetaFloodlight` (beam shapes), `tileentity/TileEntityPhantomLight` (source tracking), `tileentity/TileEntityFLElectric` (energy), `handler/ConfigHandler`, `handler/RecipeHandler`.

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
- **Jade** (approved by the user) as an **optional** integration only: `modCompileOnly` + `modLocalRuntime` from Modrinth's maven (`maven.modrinth:jade`, version in `versions/<node>/gradle.properties`).
  Never bundled and never a hard dependency (`suggests` in `fabric.mod.json`, `type = "optional"` in `neoforge.mods.toml`; NeoForge: `compileOnly` + `localRuntime`).
- **NeoForge** 21.11.x, built with **ModDevGradle** (approved by the user; build-time only).
  On NeoForge, the Fabric-only APIs above are replaced by NeoForge's own (capabilities, events, payload registration); see §4 "Loaders".
- Build and test tools only, never in the jar (approved by the user, as in OMT): **ArchUnit** (test dependency), **JaCoCo**, **PIT** (plain `JavaExec`, no Gradle plugin) and **PMD**; versions in `gradle.properties`.
- **OpenModularCore** (the shared game rules, `../../OpenModularCore`, approved by the user with JitPack on 2026-11-16): module `oml-core` from JitPack (`com.github.OpenModularTurretsTeam.OpenModularCore`), bundled into the jar; see §4 "Core Game Logic Source".
- **OMLib** (the user's own library, `../OMLib`, branch `1.21.11`), mounted as the git submodule `omlib` and bundled into every jar (jar-in-jar; Fabric `include`, NeoForge `jarJar`; requested by the user on 2026-10-27).
  It holds the code both mods used to share (platform services, version compat, energy and item transfer, config, the camouflage menu) and the build logic.
  The library is a mod of its own (`omlib`), declared as a required dependency in the mod metadata, and built as a nested step of the mod build, see §4 "OMLib".
- Nothing else without asking the user first.
  No Architectury, no Cloth Config, no Kotlin.
- Old optional integrations (IC2, BuildCraft/CoFH/EnderIO wrenches, EnderIO wireless charging, IGW, NEI, AgriCraft, Colored Light Core) are **not** ported unless the user asks.
  WAILA is replaced by the Jade plugin; WTHIT is not supported (the user chose Jade only).

---

## 2. Tech Stack

| | 1.21.11 (Fabric) | 1.21.11 (NeoForge) |
| --- | --- | --- |
| Java | 21 | 21 |
| Loader | Fabric Loader 0.19.x | NeoForge 21.11.x |
| API | Fabric API `0.141.x+1.21.11` | – |
| Gradle plugin | Loom 1.18 | ModDevGradle 2.0 |
| Mappings | Mojang official | Mojang official (NeoForge runtime names) |

- Build system: Gradle 9.8 (Kotlin DSL) + **Stonecutter 0.9**, with Fabric Loom 1.18 (`net.fabricmc.fabric-loom-remap`, the variant for obfuscated MC versions) on the Fabric node and ModDevGradle 2.0 (`net.neoforged.moddev`) on the NeoForge node.
- Shared build logic is the convention plugin `omlib/build-logic/src/main/kotlin/mod-common.gradle.kts` (Java toolchain, metadata templating, JUnit, the shared core from OpenModularCore, `verifyNoCoreSources`, `checkRecipes`, licenses in the jar via `bundled_licenses` (default: all three), publishing).
  It is mod-independent (everything comes from `gradle.properties`) and lives in the OMLib repository, mounted as the git submodule `omlib` (an included build in `pluginManagement` of `settings.gradle.kts`); change it in `../OMLib`, never inside `omlib/` (workspace `CLAUDE.md`, "OMLib in the mods", also for the shared sources); clone a mod with `--recurse-submodules` (or run `git submodule update --init`).
  The quality tools (ArchUnit, JaCoCo, PIT, PMD) stay in `build.fabric.gradle.kts`, as in OMT.
  The loader plugins are not on the build logic's classpath; they are applied by version in `stonecutter.gradle.kts` (`apply false`) and used by the loader scripts.
- Formatting: **Spotless** (in `stonecutter.gradle.kts`).
  It is deliberately light: tabs → spaces, trailing whitespace, final newline.
  No full formatter and no import reordering, because those would break the Stonecutter conditional comments.
  Run `./gradlew spotlessApply` before committing.
- The configure step prints "Stonecutter branch root: should not be a buildable project".
  This is expected (Spotless on the root project) and harmless.
- **Gradle itself must run on Java 25+** (Loom 1.18 requirement).
  The compile toolchain (21) is resolved separately (foojay auto-provisions it if missing).
- **Use Mojang mappings** (`loom.officialMojangMappings()`).
  All class and method names in code and in this doc are Mojmap names (`BlockEntity`, `Identifier`, `AbstractContainerMenu`, and so on).
- Language: Java only.
- Toolchain: Java 21 (`java_version` in the node's `gradle.properties`, read by the shared build script).

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
├── build.neoforge.gradle.kts   # NeoForge node (ModDevGradle)
├── omlib/                      # git submodule (OMLib, branch 1.21.11): the library (src/) every mod bundles, and build-logic/ (the mod-common convention plugin); see §4 "OMLib"
├── stonecutter.gradle.kts      # Stonecutter controller (active version, loader constants, chiseled tasks)
├── settings.gradle.kts         # declares the Stonecutter nodes (<minecraft version>-<loader>)
├── gradle.properties           # shared properties (mod_id, mod_version, maven_group …)
├── docs/id-mapping.md          # 1.7.10 ID → new ID table
├── docs/architecture.md        # overview of the code for developers (layers, flows, how to add things)
├── oldmod/                     # 1.7.10 Forge source, reference only (do not edit; untracked, missing in this clone, see §1)
├── versions/
│   ├── 1.21.11-fabric/
│   │   ├── gradle.properties   # minecraft_version, fabric_api, java_version=21 …
│   │   └── src/main/generated/ # datagen output (committed; NeoForge converts it, datagen_from)
│   └── 1.21.11-neoforge/
│       └── gradle.properties   # neoforge_version, minecraft_dependency (Maven range) …
└── src/
    ├── main/java/omtteam/openmodularlighting/...
    ├── main/resources/         # SHARED assets + fabric.mod.json and META-INF/neoforge.mods.toml (templated)
    ├── client/java/omtteam/openmodularlighting/...   # client-only code (split source sets)
    ├── test/java/omtteam/openmodularlighting/...      # JUnit tests (config, architecture); the core's tests are in OpenModularCore
    └── gametest/                                     # in-world GameTests: separate test mod, never shipped
```

- **Mod ID:** `openmodularlighting`.
  **Base package:** `omtteam.openmodularlighting` (keep it).
- Use Loom **split environment source sets** (`splitEnvironmentSourceSets()`).
  Client-only code (renderers, screens, models, client networking handlers) goes in `src/client/java`.
  Never reference client classes from `main`.
  The NeoForge node keeps the same split: `build.neoforge.gradle.kts` creates a `client` source set that sees `main`, and the jar contains both.
- `fabric.mod.json` and `META-INF/neoforge.mods.toml` are templated via `processResources` (`mod-common`); each jar only contains its own loader's file (also in the test mod).
  Available keys: `${version}`, `${mod_id}`, `${mod_name}`, `${java_version}`, `${minecraft_dependency}`, `${loader_version}` (Fabric), `${neoforge_version}`, `${neoforge_loader_range}`, `${omlib_version}`.
- The mod version is `<mod_version>+<minecraft version>-<loader>` (e.g. `1.0.0-beta.2+1.21.11-fabric`), so the jars of both loaders can be told apart.
  The jar files are named `<mod_id>-<mod_version>-mc<minecraft version>-<loader>.jar` (`archiveVersion` in `mod-common`); the release title on CurseForge and Modrinth is the jar name without `.jar` (`displayName`), while the mod metadata and the published version number keep the `+` version.

---

## 4. Stonecutter

### Setup (`settings.gradle.kts`)

```kotlin
plugins { id("dev.kikugie.stonecutter") version "0.9.8" }

stonecutter {
    create(rootProject) {
        versions("1.21.11-fabric" to "1.21.11", "1.21.11-neoforge" to "1.21.11")
        mapBuilds { _, node -> "build.${node.project.substringAfterLast('-')}.gradle.kts" }
        vcsVersion = "1.21.11-fabric" // the version the committed source is written for
    }
}
```

The Stonecutter DSL changes between releases.
If this does not match the installed version, check the Stonecutter docs and update both this file and the build.

### Rules

- **Committed source must always be in the `vcsVersion` (`1.21.11-fabric`) state.** Before committing, switch back if you switched to the NeoForge node (`./gradlew "Reset active project"` or `"Set active project to 1.21.11-fabric"`).
- Stonecutter's only axis on this branch is the loader (`fabric` or `neoforge`); Minecraft-version differences are not conditions here: each Minecraft version has its own branch.
  Loader-conditional code uses Stonecutter comments (see "Loaders" below for the constants):

  ```java
  //? if fabric {
  return new ClientPlatformServices(new FabricClientNetwork());
  //?} else {
  /*return new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network()));
  *///?}
  ```

  (from OMLib's `ClientPlatform`.)
  Inactive branches must be wrapped in `/* … */` exactly as Stonecutter expects.
  Prefer letting Stonecutter generate this by switching nodes over hand-writing it.
- **Minimize conditional blocks.** Hide differences behind small helpers in OMLib's `omtteam.omlib.compat.VersionCompat` (or similar) or behind the service interfaces in OMLib's `platform`, rather than sprinkling `//? if` across gameplay code; the compat helpers keep the call sites the same on every version branch, which is what makes porting between the branches by hand cheap.
  Gameplay logic (beam shapes, energy use, growth) should be loader-agnostic.
- Constants can be compared in conditions: `//? if fabric {`, `//? if neoforge {`.
- The source is written for Minecraft 1.21.11 and uses its names (`Identifier`, `noCollision`, `SLOWNESS`, `animal.equine`, `needsSync`, `snapTo`, `net.minecraft.util.Util`).
- Node-specific *properties* live in `versions/<node>/gradle.properties`.
  Read them in the build via `property("…")`.

### Loaders: publishing

- NeoForge is published like the Fabric node.
  `publish_enabled=false` in a node's `gradle.properties` would make that node a dry run even with tokens (`mod-common`).

### Commands

```bash
./gradlew "Set active project to 1.21.11-neoforge"  # switch the source to the NeoForge node
./gradlew "Set active project to 1.21.11-fabric"    # switch back (VCS state)
./gradlew chiseledBuild                             # build both nodes (custom task in stonecutter.gradle.kts)
./gradlew :1.21.11-fabric:build                     # build a single node
./gradlew :1.21.11-fabric:runClient                 # run client for a node (run dir versions/<node>/run, one per node)
./gradlew :1.21.11-fabric:runServer
./gradlew spotlessApply                             # fix formatting (spotlessCheck verifies it)
./gradlew :1.21.11-fabric:runGameTest               # in-world GameTests for the Fabric node (also part of check/build)
./gradlew :1.21.11-fabric:runDatagen                # regenerate data (the NeoForge node converts this output)
./gradlew :1.21.11-neoforge:runClient               # NeoForge client (run dir versions/1.21.11-neoforge/run)
./gradlew :1.21.11-neoforge:runGameTestServer       # NeoForge GameTests (also part of check/build)
./gradlew :1.21.11-fabric:pitest                    # mutation testing of the config classes -> versions/1.21.11-fabric/build/reports/pitest/index.html
./gradlew chiseledPublish                           # upload both nodes to CurseForge/Modrinth (custom task)
./gradlew :1.21.11-fabric:publishMods               # upload one node
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
- **Release steps:** set `mod_version` (e.g. `1.0.1`) and `release_type` → `./gradlew releaseChangelog` (moves Unreleased into `## [<mod_version>] - <date>`) → `./gradlew chiseledBuild` → commit `gradle.properties` and `CHANGELOG.md` as `feat: version <mod_version>` → tag that commit with a lightweight tag named exactly `<mod_version>+1.21.11` (e.g. `1.0.1+1.21.11`; no `v` prefix; the same `mod_version` is released from the branches `1.20.1`, `1.21.1` and `1.21.11`, so the tag names the Minecraft version; older tags stay as they are; the 1.12.2 tags carry `+1.12.2-forge`, and Modrinth version numbers must not clash with them, see the workspace `CLAUDE.md`) → `./gradlew chiseledPublish` → push the branch and the tag.
  Publishing uploads only that version's section and fails if it is missing.
  The user releases and pushes; never run `chiseledPublish` or push from here.
(Task names may differ slightly depending on Stonecutter's version. Use `./gradlew tasks` if unsure.)

**Definition of done for any change:** `./gradlew chiseledBuild` succeeds for **both** nodes (this includes the JUnit tests and the GameTests), and the source is left in the `1.21.11-fabric` state.

### Docs and checks

The mod's own unit tests (config, architecture rules) live in `src/test/java` (JUnit, test-only dependency).
`./gradlew check` also runs `checkRecipes` (from `mod-common`): every item (item model) of the mod needs a recipe on that node, unless `recipe-exempt.txt` lists it as `<id> # <reason>`, and every recipe result of the mod's namespace must be one of its items.

Quality tools (taken over from OMT; details in `docs/architecture.md`, "Rules the build checks" and "Measuring code quality"):
`ArchitectureTest` (`src/test/.../architecture`, ArchUnit, Fabric node only) checks that main never uses client code, and package cycles against its `KNOWN_CYCLES` list (a ratchet: update the list when a cycle shrinks; never add to it without asking the user).
PMD (`gradle/pmd/ruleset.xml`, report only, active version) measures complexity and size: `versions/1.21.11-fabric/build/reports/pmd/`.
JaCoCo records coverage in the unit tests and on the game test server; `check` prints the line coverage of the mod's own classes (without the core) per top-level package (active version only).
PIT (`./gradlew :1.21.11-fabric:pitest`, on demand, not part of `check`) runs mutation testing on the config classes (`pitestTargets`); surviving mutations show behaviour the tests do not pin down.
It fails when the results get worse than the thresholds in `gradle.properties` (`pitest_*`); see `docs/architecture.md`.

## Where the rest lives (nested `CLAUDE.md` files)

The sections of this file that explain one subsystem were moved into a `CLAUDE.md` in the folder of that subsystem's code.
A nested `CLAUDE.md` is loaded only when you read files in its folder; read the one for the area you work in before changing it.
The § numbers and headings of the moved sections (§5, §6.x) are kept, so references such as "§6.3" in the text still resolve.

| `CLAUDE.md` in | What it covers (former sections) |
| --- | --- |
| `src/main/java/omtteam/openmodularlighting/` | §6.1 package layout of `src/main`; §4 "OMLib (the library)"; §4 "Core Game Logic Source" (how the core is wired in, its interfaces, the reference beam loops); §5 compat helpers (the porting layer) |
| `src/main/java/omtteam/openmodularlighting/platform/` | §4 "Loaders": loader-specific classes, services, start-up |
| `src/main/java/omtteam/openmodularlighting/block/` | §6.2 blocks and items (id table, recipes, config switches, panel lights), §6.6 interaction |
| `src/main/java/omtteam/openmodularlighting/blockentity/` | §6.3 light mechanics (phantom lights, casting, re-cast, beam shapes), §6.4 light state machine, §6.7 block entity data, §6.8 grow light, §6.9 UV light damage |
| `src/main/java/omtteam/openmodularlighting/energy/` | §6.5 power and fuel |
| `src/main/java/omtteam/openmodularlighting/config/` | §6.10 config (fields, versioning) |
| `src/main/java/omtteam/openmodularlighting/camo/` | §6.11 disguise (camouflage), differences from OpenModularTurrets' camouflage |
| `src/main/` | §5 "Resources strategy" (assets, datagen, no AI images) |
| `src/client/java/omtteam/openmodularlighting/client/` | §6.1 client package layout, client compat helpers, Jade details |
| `src/gametest/` | §4 "GameTests" |

---

## 7. Change Workflow

1. Implement gameplay logic in the core (with unit tests) where possible, in OpenModularCore (root `CLAUDE.md`, "Changing the core"); keep Minecraft-facing code thin.
2. Wrap loader differences in `platform/` and Minecraft-version differences in OMLib's `compat` helpers.
3. Add assets, lang entries and datagen entries; regenerate the data (`:1.21.11-fabric:runDatagen`; the NeoForge node converts it).
4. Add a `CHANGELOG.md` entry for user-visible changes.
5. Update the documentation the change touches.
   "Updating the docs" always means everything in the `docs/` folder (`architecture.md`, `id-mapping.md`, …) plus `README.md` and `CONTRIBUTING.md`: check each of them against the change, not only the one that comes to mind.
6. Run `./gradlew chiseledBuild`.

- Every variant of subblocks is an **individual registered block/item** with a lowercase snake_case ID.
  `docs/id-mapping.md` maps the 1.7.10 IDs to the current ones.

### Designing APIs

The user wants close control over every API (public interfaces for other mods, but also new cross-cutting interfaces inside the mod).
Design them step by step, together with the user:

1. **Overall shape first:** present a few ideas (e.g. 2–3 approaches with their trade-offs and a recommendation) and wait for the user's choice before writing code.
2. **Then each level in turn:** repeat this for each package, then each class or interface, then each public method or callback signature (names, parameters, return values, nullability, when it is called).
   Propose, wait for the decision, then implement that piece.
3. Do not implement a whole API in one go, and do not pick a signature on your own because it seems obvious; a small choice (e.g. which parameters a callback gets) is still the user's call.

### Code shared with OpenModularTurrets

The code both mods share is in OMLib (§4 "OMLib"), which both bundle.
What is still duplicated in both repositories is listed in `omlib/shared-sources.txt` (the game test stand-ins and their registration classes, the game test `neoforge.mods.toml`, the PMD ruleset, the Gradle wrapper properties); `python3 -I omlib/tools/shared_sources.py check . ../OpenModularTurrets` compares them.
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
- Markdown files: one sentence per line, never a line break inside a sentence.
  The sentences of a paragraph go on consecutive lines (no blank line between them), so they render as one paragraph.
  For a visible line break inside a paragraph, end the line with two spaces.
  Exception: `CHANGELOG.md` entries stay one line each.
- Don't add new features or change balance values unless the user asks.

---

## 9. Roadmap / Status

The port (all blocks and items, Jade, the light debug tool, the disguise, NeoForge and 1.21.11) is done and released; its history is in git and `CHANGELOG.md`.
Optional work beyond it (old integrations, client GameTests) only when the user asks.

### Known issues

- **Commands skip the removal hook:** `/setblock`, `/fill` and `/clone` place with `UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS` (256), so `CompatBlockEntity.onBlockRemoved` (OMLib) does not run.
  The slot item is not dropped (as with vanilla containers on 1.21.5+), and the light's phantom lights stay lit until random ticks remove them (UV lights check at once, see §6.9).
- **OMLib's own Fabric version ranges** are still `~1.21.11`; the mods' exact ranges keep a wrong Minecraft version from loading, so it only matters for OMLib alone.
- **Phantom lights are not air** for vanilla checks (§6.3): sugar cane, cactus and bamboo do not grow into them, a nether portal frame with one inside does not light.
