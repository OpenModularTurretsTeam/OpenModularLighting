# CLAUDE.md — Floodlights (Fabric, Multi-Version)

This file guides Claude Code when working in this repository. Read it fully before making changes.

---

## 1. Project Overview

**Floodlights (FL)** is a mod being ported from Forge on MC 1.7.10 (`oldmod/`, package `de.keridos.floodlights`, mod ID `FloodLights`) to **Fabric**, targeting **1.20.1 and 1.21.1 from a single codebase** using **Stonecutter**.
A **NeoForge 1.21.1** build is being added on the branch `neoforge-test` (see §4 "Loaders" and §9).
It is a test run for the same setup in OpenModularTurrets (`../OMT1.20.1+`), so keep the build logic and the `platform/` pattern mod-independent.

`oldmod/` is the **read-only reference** for the port. Never edit it and never build it. It is a source of ideas, values and assets, not a specification: the beam thinning, the persistent light blocks and other details differ on purpose. Do not copy its structure (static singletons, metadata, `ForgeDirection`, CoFH API).

Core gameplay:

- **Floodlights** light large areas with invisible light blocks ("phantom lights") along a beam. The beam can be a straight line, a narrow cone or a wide cone. The phantom lights stay in place and are only switched on and off (see §6.3).
- **Grow light**: lights the block in front and gives random growth ticks to a crop two blocks ahead.
- **UV floodlight**: a short beam of UV light blocks that damages living entities standing in it.
- **Fluorescent lights**: small, cheap lights that illuminate their 5 neighbouring blocks.

### Allowed dependencies

- Fabric Loader and **Fabric API**. Fabric API, including Transfer API, Networking, Rendering and Datagen, may be used freely.
- **Team Reborn Energy** for power, as the de facto Fabric energy standard. Jar-in-jar it. It replaces the old RF (CoFH) and IC2 EU support.
- **Jade** (approved by the user) as an **optional** integration only: `modCompileOnly` + `modLocalRuntime` from Modrinth's maven (`maven.modrinth:jade`, version per Minecraft version in `versions/<v>/gradle.properties`). Never bundled and never a hard dependency (`suggests` in `fabric.mod.json`, `type = "optional"` in `neoforge.mods.toml`; NeoForge: `compileOnly` + `localRuntime`). Only `compat/jade/OMLJadePlugin` and `FloodlightJadeProvider` may import `snownee.jade`; Jade loads them through the `jade` entrypoint (Fabric) or the `@WailaPlugin` annotation (NeoForge), so the mod runs without Jade.
  Jade is not on the NeoForge GameTest server: there it sends a packet to the tests' vanilla mock players, which NeoForge refuses.
- **NeoForge** 21.1.x (1.21.1 only), built with **ModDevGradle** (approved by the user; build-time only).
  On NeoForge, the Fabric-only APIs above are replaced by NeoForge's own (capabilities, events, payload registration); see §4 "Loaders".
- Nothing else without asking the user first. No Architectury, no Cloth Config, no OMLib, no Kotlin.
- Old optional integrations (IC2, BuildCraft/CoFH/EnderIO wrenches, EnderIO wireless charging, IGW, NEI, AgriCraft, Colored Light Core) are **not** ported unless the user asks. WAILA is replaced by the Jade plugin; WTHIT is not supported (the user chose Jade only).

---

## 2. Tech Stack

| | 1.20.1 (Fabric) | 1.21.1 (Fabric) | 1.21.1 (NeoForge) |
| --- | --- | --- | --- |
| Java | 17 | 21 | 21 |
| Loader | Fabric Loader 0.19.x | Fabric Loader 0.19.x | NeoForge 21.1.x |
| API | Fabric API `0.92.x+1.20.1` | Fabric API `0.116.x+1.21.1` | – |
| Gradle plugin | Loom 1.18 | Loom 1.18 | ModDevGradle 2.0 |
| Mappings | Mojang official (+ Parchment optional) | Mojang official (+ Parchment optional) | Mojang official (NeoForge runtime names) |

- Build system: Gradle 9.8 (Kotlin DSL) + **Stonecutter 0.9**, with Fabric Loom 1.18 (`net.fabricmc.fabric-loom-remap`, the variant for obfuscated MC versions) on the Fabric nodes and ModDevGradle 2.0 (`net.neoforged.moddev`) on the NeoForge node.
- Shared build logic is the convention plugin `buildSrc/src/main/kotlin/mod-common.gradle.kts` (Java toolchain, metadata templating, JUnit, `verifyCoreIsolation`, licenses in the jar via `bundled_licenses` (default: all three), publishing).
  It is mod-independent (everything comes from `gradle.properties`) and identical to OMT's copy; change both together.
  The loader plugins are not on the `buildSrc` classpath; they are applied by version in `stonecutter.gradle.kts` (`apply false`) and used by the loader scripts.
- Formatting: **Spotless** (in `stonecutter.gradle.kts`). It is deliberately light: tabs → spaces, trailing whitespace, final newline. No full formatter and no import reordering, because those would break the Stonecutter conditional comments. Run `./gradlew spotlessApply` before committing.
- The configure step prints "Stonecutter branch root: should not be a buildable project". This is expected (Spotless on the root project) and harmless.
- **Gradle itself must run on Java 25+** (Loom 1.18 requirement). The compile toolchains (17 / 21) are resolved separately (foojay auto-provisions them if missing).
- **Use Mojang mappings** (`loom.officialMojangMappings()`). All class and method names in code and in this doc are Mojmap names (`BlockEntity`, `ResourceLocation`, `AbstractContainerMenu`, and so on).
- Language: Java only.
- Toolchain: configure the Java toolchain per version (17 for 1.20.1, 21 for 1.21.1) in the shared build script. Use the version-specific `gradle.properties` to switch.

Check exact latest patch versions of Loader, API, Loom and Stonecutter when setting up or bumping. Do not hard-code guesses into the build without verifying.

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
├── build.fabric.gradle.kts     # Fabric nodes (Loom, Fabric API, datagen, GameTests)
├── build.neoforge.gradle.kts   # NeoForge nodes (ModDevGradle; temporary "ported packages" filter)
├── buildSrc/                   # mod-common convention plugin: build logic shared by all loaders
├── stonecutter.gradle.kts      # Stonecutter controller (active version, loader constants, chiseled tasks)
├── settings.gradle.kts         # declares the Stonecutter nodes (<minecraft version>-<loader>)
├── gradle.properties           # shared properties (mod_id, mod_version, maven_group …)
├── docs/id-mapping.md          # 1.7.10 ID → new ID table
├── docs/architecture.md        # overview of the code for developers (layers, flows, how to add things)
├── oldmod/                     # 1.7.10 Forge source, reference only (do not edit)
├── versions/
│   ├── 1.20.1-fabric/
│   │   ├── gradle.properties   # minecraft_version, fabric_api, java_version=17 …
│   │   └── src/main/generated/ # datagen output for 1.20.1 (committed)
│   ├── 1.21.1-fabric/
│   │   ├── gradle.properties   # minecraft_version, fabric_api, java_version=21 …
│   │   └── src/main/generated/ # datagen output for 1.21.1 (committed)
│   └── 1.21.1-neoforge/
│       └── gradle.properties   # neoforge_version, minecraft_dependency (Maven range) …
└── src/
    ├── main/java/omtteam/openmodularlighting/...
    ├── main/resources/         # SHARED assets + fabric.mod.json and META-INF/neoforge.mods.toml (templated)
    ├── client/java/omtteam/openmodularlighting/...   # client-only code (split source sets)
    ├── test/java/omtteam/openmodularlighting/...      # JUnit tests (core/, config)
    └── gametest/                                     # in-world GameTests: separate test mod, never shipped
```

- **Mod ID:** `openmodularlighting`. **Base package:** `omtteam.openmodularlighting`.
- Use Loom **split environment source sets** (`splitEnvironmentSourceSets()`). Client-only code (renderers, screens, models, client networking handlers) goes in `src/client/java`. Never reference client classes from `main`.
  The NeoForge node keeps the same split: `build.neoforge.gradle.kts` creates a `client` source set that sees `main`, and the jar contains both.
- `fabric.mod.json` and `META-INF/neoforge.mods.toml` are templated via `processResources` (`mod-common`); each jar only contains its own loader's file.
  Available keys: `${version}`, `${mod_id}`, `${mod_name}`, `${java_version}`, `${minecraft_dependency}`, `${loader_version}` (Fabric), `${neoforge_version}`, `${neoforge_loader_range}`.
- The mod version is `<mod_version>+<minecraft version>-<loader>` (e.g. `1.0.0-beta.2+1.21.1-fabric`), so the jars of both loaders can be told apart.

---

## 4. Stonecutter

### Setup (`settings.gradle.kts`)

```kotlin
plugins { id("dev.kikugie.stonecutter") version "0.9.8" }

stonecutter {
    create(rootProject) {
        versions("1.20.1-fabric" to "1.20.1", "1.21.1-fabric" to "1.21.1", "1.21.1-neoforge" to "1.21.1")
        mapBuilds { _, node -> "build.${node.project.substringAfterLast('-')}.gradle.kts" }
        vcsVersion = "1.21.1-fabric" // the version the committed source is written for
    }
}
```

The Stonecutter DSL changes between releases. If this does not match the installed version, check the Stonecutter docs and update both this file and the build.

### Rules

- **Committed source must always be in the `vcsVersion` (`1.21.1-fabric`) state.** Before committing, switch back if you switched to another node (`./gradlew "Reset active project"` or `"Set active project to 1.21.1-fabric"`).
- Version-conditional code uses Stonecutter comments:

  ```java
  //? if >=1.21 {
  ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  //?} else {
  /*ResourceLocation id = new ResourceLocation(MOD_ID, path);
  *///?}
  ```

  Inactive branches must be wrapped in `/* … */` exactly as Stonecutter expects. Prefer letting Stonecutter generate this by switching versions over hand-writing it.
- **Minimize conditional blocks.** Hide version differences behind small helpers in `omtteam.openmodularlighting.compat.VersionCompat` (or similar) rather than sprinkling `//? if` across gameplay code. Gameplay logic (beam shapes, energy use, growth) should be version-agnostic.
- Constants can be compared in conditions: `//? if >=1.21 {`, `//? if <1.21 {`, `//? if 1.20.1 {`.
- Version-specific *properties* live in `versions/<v>/gradle.properties`. Read them in the build via `property("…")`.

### Loaders

Each Stonecutter node is `<minecraft version>-<loader>`; the loader picks the build script (`build.<loader>.gradle.kts`).
The rules for loader differences mirror those for version differences, with one addition for whole classes:

- **Whole loader-specific classes** go into a `platform/<loader>/` package (`platform/fabric/`, `platform/neoforge/`, also under `client/`).
  `mod-common` leaves the other loaders' `platform/` packages out of the compilation, so these classes need no Stonecutter comments.
  Examples: entrypoints, capability/API registration, event wiring.
- Shared code reaches them through small interfaces in `platform/` (design them with the user, see §7 "Designing APIs").
- **Small differences** inside a shared class use the loader constants: `//? if fabric {` / `//? if neoforge {` (set in `stonecutter.gradle.kts`).
  Keep them inside `compat/` and `platform/`, never in gameplay code.
- `core/` stays loader-free (`verifyCoreIsolation` also forbids `net.neoforged`).
- **Everything compiles on NeoForge except `datagen`,** which is Fabric-only by design (NeoForge gets the converted Fabric output, §5 "Resources strategy").
- **Start-up:** both entrypoints call `OML.init()` (config, packets) first. Fabric then runs `OML.registrationSteps()` in order; NeoForge runs each `RegistrationStep` in the `RegisterEvent` of its registry and throws at common setup if a step never ran. A new registry class gets a step in that list, not a call in an entrypoint.
- NeoForge is published like the Fabric nodes. `publish_enabled=false` in a node's `gradle.properties` would make that node a dry run even with tokens (`mod-common`).

### Commands

```bash
./gradlew "Set active project to 1.20.1-fabric"   # switch source to 1.20.1
./gradlew "Set active project to 1.21.1-fabric"   # switch back (VCS state)
./gradlew chiseledBuild                           # build ALL nodes (custom task in stonecutter.gradle.kts)
./gradlew :1.20.1-fabric:build                    # build a single node
./gradlew :1.21.1-fabric:runClient                # run client for a node
./gradlew :1.21.1-fabric:runServer
./gradlew spotlessApply                           # fix formatting (spotlessCheck verifies it)
./gradlew :1.21.1-fabric:runGameTest              # in-world GameTests for one node (also part of check/build)
./gradlew :1.20.1-fabric:runDatagen               # regenerate data for a node
./gradlew :1.21.1-fabric:runDatagen
./gradlew :1.21.1-neoforge:runClient              # NeoForge client (run dir versions/1.21.1-neoforge/run)
./gradlew :1.21.1-neoforge:runGameTestServer      # NeoForge GameTests (also part of check/build)
./gradlew chiseledPublish                         # upload all nodes to CurseForge/Modrinth (custom task)
./gradlew :1.21.1-fabric:publishMods              # upload one node
```

### Publishing

- Uses `me.modmuss50.mod-publish-plugin` (approved by the user), configured in `publishMods` in `buildSrc/src/main/kotlin/mod-common.gradle.kts` (the loader scripts only set the jar to upload).
- Settings in `gradle.properties`: `release_type` (release/beta/alpha), `curseforge_project_id` and `modrinth_project_id`. An empty ID skips that site.
  Both are set (CurseForge `268178`, Modrinth `4yAD8NG9`); ask the user before changing them.
  The old 1.7.10 CurseForge page is `224728-floodlights`.
- Tokens are never committed: environment variables `CURSEFORGE_TOKEN` / `MODRINTH_TOKEN`, or `curseforge_token` / `modrinth_token` in `~/.gradle/gradle.properties`. Without a token the upload is a dry run (output in `versions/<v>/build/publishMods/`).
- **Changelog** (`CHANGELOG.md`, Keep a Changelog format): every user-visible change gets a one-line entry under `## [Unreleased]` (Added / Changed / Fixed / Removed), by hand or via `./gradlew changelogEntry --type=fixed --entry="…"`. Add the entry together with the change.
- **Release steps:** set `mod_version` (e.g. `1.0.0-alpha.2`) and `release_type` → `./gradlew releaseChangelog` (moves Unreleased into `## [<mod_version>] - <date>`) → `./gradlew chiseledPublish`. Publishing uploads only that version's section and fails if it is missing.
(Task names may differ slightly depending on Stonecutter's version. Use `./gradlew tasks` if unsure.)

**Definition of done for any change:** `./gradlew chiseledBuild` succeeds for **all** nodes (this includes the JUnit tests and the GameTests), and the source is left in the `1.21.1-fabric` state.

### GameTests (`src/gametest`)

- In-world tests run on a headless GameTest server (Fabric `runGameTest`, NeoForge `runGameTestServer`; both part of `check`). It needs **no EULA**: the run directory gets `eula=false`, and that is fine. Do not accept the EULA on the user's behalf.
- `src/gametest` is a separate test mod (`openmodularlighting_gametest`; Fabric: Loom's `fabricApi.configureTests` + `fabric.mod.json`, NeoForge: the `gametest` source set registered as a second mod + `META-INF/neoforge.mods.toml`). It is not in the release jar. It registers test-only content (`TestBlocks`, `TestBlockEntities`, `TestItems` through `OMLGameTestMod.registrationSteps()`), for example `TestLightSourceBlock`, a fake light source whose on/off state and shape the test sets directly.
- **The test classes are shared by both loaders.** They implement `OMLGameTests` (not `FabricGameTest`) and use `@GameTest(template = EMPTY_STRUCTURE)`: our own 8 × 8 × 8 air structure (`src/gametest/resources/data/openmodularlighting/structure/empty.nbt`, a copy in `structures/` for 1.20.1). Every test class also carries the NeoForge annotations behind a condition:

  ```java
  //? if neoforge {
  /*@GameTestHolder(OML.MOD_ID)
  @PrefixGameTestTemplate(false)
  *///?}
  public class FooGameTest implements OMLGameTests {
  ```

  Add a new test class to the `fabric-gametest` entrypoint in `src/gametest/resources/fabric.mod.json` as well (NeoForge finds it through `@GameTestHolder`).
- Loader-specific test code lives in `gametest/platform/<loader>`: the entrypoints (`OMLFabricGameTests`, `OMLNeoForgeGameTests`), `TestTransfer` (energy and items through the loader's API, `OMLGameTestMod.transfer()`; use it to test the loader adapters) and the energy access of the test battery.
- **No test class may sit in a main-mod package:** NeoForge loads the test mod as its own module, and two modules cannot share a package. Reach protected block entity methods through vanilla's public ones (`BlockEntityData.save` / `load` / `clientData`).
- Tests in the same batch run side by side, and finished tests leave beacons (light 15) next to their structure. A test that checks for darkness needs its own `batch` and must compare against the light level measured before it starts.
- The test mod must not load in normal dev runs: NeoForge's `client` and `server` runs set `loadedMods` to the main mod only (ModDevGradle loads every registered mod by default), and Loom's `runClient` never includes it. Otherwise its range override below applies in play.
- 1.21 encloses every test structure in barriers. Light beams therefore have to stay inside the 8 × 8 × 8 structure: the test mod sets both beam ranges to `OMLGameTestMod.TEST_RANGE` (6) when the server starts. Entrypoints do not run in dependency order, so test setup that depends on the main mod (such as the config or `Platform` services) belongs in a server lifecycle event (`OMLGameTestMod.onServerStarting`), not in the entrypoint.
- The test mod also has a test battery (`TestBatteryItem`, energy in the item's custom data; Team Reborn Energy on Fabric, an item capability on NeoForge).
- The client (screens, models, tint, tooltips) is not covered: Fabric's client GameTests need Minecraft 1.21.4+. Check it by hand with `runClient`.

### Core Game Logic Source

The game logic of the mod lives in the `core` package and is completely free of Minecraft imports. It contains:

1. **Beam shapes.** Pure functions that return the ordered list of offsets for a light, given facing, mode and range (see §6.3). Each beam is a separate ray so that it can stop at the first opaque block.
2. **Rotation.** Shapes are defined along +X and rotated to the facing direction (port of `MathUtil.rotate`).
3. **Light-source bookkeeping.** Per light block: all sources and the active ones, with add / remove / set active. It answers "lit?" (any active source) and "remove block?" (no sources). Plus the shape diff (old positions vs new positions → removed / added) used for mode changes and re-casts.
4. **Light state machine.** Active/inactive, redstone + invert, timeout after turning off, "needs re-cast" flag, per-tick energy or fuel cost.
5. **Formulas.** Carbon burn time, grow-tick interval, energy cost per mode.
6. **Unit tests** for all of the above.

`./gradlew check` runs `verifyCoreIsolation`, which fails if anything under `core/` imports `net.minecraft`, `net.fabricmc`,
`com.mojang`, `team.reborn` or non-core mod packages. Tests live in `src/test/java` (JUnit, test-only dependency).

The core layer works through small interfaces that `main` implements: `LightWorld.cellAt(pos)` classifies a block as a `Cell` (air, phantom light, UV light, opaque, transparent, unloaded; see its Javadoc for the mapping from `BlockState`), and `PowerSource` pays a tick's cost. Positions in core are the `Pos` record, not `BlockPos`; `Facing` has the same ordinal order as `Direction`.

The original beam loops are kept in the test sources as a reference (`core/light/Legacy1710Beams`). `BeamCasterTest` checks that `BeamCaster` produces their positions (filtered by the same `Spacing`) in random worlds, for every facing and many ranges. If you change a shape on purpose, change the reference too and say so.

---

## 5. Known 1.20.1 vs 1.21.1 Differences

Wrap each of these behind a helper where possible.

| Area | 1.20.1 | 1.21.1 |
| --- | --- | --- |
| `ResourceLocation` | `new ResourceLocation(ns, path)` | `ResourceLocation.fromNamespaceAndPath(ns, path)` → use an `OML.id(path)` helper |
| BlockEntity NBT | `load(CompoundTag)`, `saveAdditional(CompoundTag)` | `loadAdditional(CompoundTag, HolderLookup.Provider)`, `saveAdditional(CompoundTag, HolderLookup.Provider)` |
| BE update packet tag | `getUpdateTag()` | `getUpdateTag(HolderLookup.Provider)` |
| ItemStack extra data | `stack.getTag()` / NBT | **Data Components** (register custom `DataComponentType`s, or `CUSTOM_DATA`) |
| ItemStack NBT save | `stack.save(tag)` / `ItemStack.of(tag)` | `stack.save(provider)` / `ItemStack.parse(provider, tag)` (codecs) |
| Block interaction | `Block#use(...)` → `InteractionResult` | `useWithoutItem(...)` and `useItemOn(...)` → `ItemInteractionResult` |
| Block codecs | not required | `BaseEntityBlock` and others require `codec()` returning a `MapCodec` |
| Networking | `ResourceLocation` channel + `FriendlyByteBuf` | `CustomPacketPayload` + `StreamCodec` + `PayloadTypeRegistry` |
| Extended menus | `ExtendedScreenHandlerFactory` writing to a buf | `ExtendedScreenHandlerFactory<D>` with a `StreamCodec` for `D` |
| Entity synced data | `defineSynchedData()` | `defineSynchedData(SynchedEntityData.Builder)` |
| Tooltips | `appendHoverText(stack, Level, list, flag)` | `appendHoverText(stack, Item.TooltipContext, list, flag)` |
| Screen background | `renderBackground(GuiGraphics)` | `renderBackground(GuiGraphics, mouseX, mouseY, partialTick)` |
| VertexConsumer | `.vertex().color().uv()…endVertex()` | `.addVertex().setColor().setUv()…` (no `endVertex`) |
| Data folders | `tags/blocks`, `tags/items`, `loot_tables`, `recipes`, `advancements` | `tags/block`, `tags/item`, `loot_table`, `recipe`, `advancement` |
| Recipe JSON result | `"result": {"item": "...", "count": n}` | `"result": {"id": "...", "count": n}` |

**Same in both** (no conditionals needed): `DamageTypes` / damage-type registry, `GuiGraphics`, `PoseStack`, the general Fabric Transfer API shape, and the `BlockEntityTicker` pattern.

### Existing helpers (use these instead of writing new `//? if` blocks)

| Difference | Helper |
| --- | --- |
| `ResourceLocation` | `OML.id(path)`, `VersionCompat.id(ns, path)` |
| BlockEntity NBT + update tag | extend `compat.CompatBlockEntity`: override `readData` / `writeData` (+ optional `writeClientData`), call `syncToClient()`; data for the block model while meshing: override `renderData()` (Fabric render data, NeoForge `RENDER_DATA` model data), call `remesh()` on the client when it changed |
| ItemStack NBT, slot lists | `VersionCompat.saveStack` / `loadStack`, `saveItems` / `loadItems` |
| ItemStack extra data | `VersionCompat.hasCustomData` / `getCustomData` / `setCustomData` / `updateCustomData` |
| Block interaction | implement `compat.InteractiveBlock#onUse`; `InteractiveBlock.handleUse` is called by the loader's block-use event (Fabric `UseBlockCallback`, NeoForge `PlayerInteractEvent.RightClickBlock`; same in both versions) |
| Creative tab builder | `VersionCompat.creativeTabBuilder()` (Fabric `FabricItemGroup`, NeoForge vanilla builder) |
| Networking | `compat.network.NetworkCompat.registerC2S` / `registerS2C` / `sendToPlayer`; client: `client.compat.ClientNetworkCompat.registerHandler` / `sendToServer`. Packets are plain records with `write` / `read`, listed in `network.OMLPackets`. The facades forward to `platform.Network` / `client.platform.ClientNetwork` (Fabric: `PayloadTypeRegistry`; NeoForge: recorded and applied in `RegisterPayloadHandlersEvent`, optional channels, handlers on the main thread, S2C handlers dispatched from main to the handler the client registered; a packet without client handler is logged and dropped) |
| Extended menus | `compat.MenuCompat.register` / `open` (passes the `BlockPos`; forwards to `platform.Menus`: Fabric extended screen handlers, NeoForge `IMenuTypeExtension` / `openMenu(provider, pos)`) |
| Container screen background, vertices | `client.compat.ClientCompat` |
| Client start-up (screens, tooltip lines) | `client.OMLClient` (`init`, `registerScreens(ScreenRegistrar)`, `appendTooltip`); tint `FloodlightTint.color` / `tintedBlocks`; debug wireframes `LightBlockDebugRenderer.render(level, poseStack, buffers, camera)`. The loader client entrypoints wire them to their events. |
| Render layers | Fabric `BlockRenderLayerMap` in `OMLFabricClient`; NeoForge `"render_type"` in the block model JSON (Fabric ignores it). Keep both in sync. The floodlights (`CamoBlock`s) are cutout because of their lamp overlay (§6.11). |

### Resources strategy

- **`assets/`** (textures, models, blockstates, lang, sounds): identical across versions. Keep them handwritten in shared `src/main/resources/assets/openmodularlighting/`.
  - Textures can be reused from `oldmod/src/main/resources/assets/floodlights/textures/`. Rename them to snake_case.
  - Lang: convert `oldmod/.../lang/*.lang` (en_US, de_DE, ru_RU) to `en_us.json`, `de_de.json` and `ru_ru.json` with the new keys.
  - Connected textures of `lightproof_glass`: `assets/openmodularlighting/optifine/ctm/lightproof_glass/` holds a `ctm_compact` properties file (Continuity / OptiFine format, no dependency) and the 5 tiles `0.png`–`4.png`, cut from `textures/block/lightproof_glass.png` (32 × 32, 2 px frame): 0 unconnected, 1 fully connected, 2 up/down, 3 left/right, 4 only the frame corners. Cut them again if the base texture changes.
  - The old IGW wiki text (`wiki/`) is not ported.
- **`data/`** (recipes, loot tables, tags, advancements): folder names and JSON formats differ. **Generate them with Fabric Datagen.**
  - Run datagen per version.
  - Configure the output directory relative to the version project (`project.file("src/main/generated")` → `versions/<v>/src/main/generated`).
  - Add that directory as a resource source dir.
  - Commit the generated files.
  - **NeoForge has no datagen of its own.** `build.neoforge.gradle.kts` (`convertFabricData`) copies the Fabric output of the node named by `datagen_from` (`1.21.1-fabric`) into the NeoForge resources at build time, turning `fabric:load_conditions` into `neoforge:conditions` (`condition` → `type`).
    Only conditions in this mod's namespace convert (registered under the same id on both loaders); any other condition fails the build.
    So regenerate the Fabric data and the NeoForge jar follows; nothing NeoForge-specific is committed.
- Do not handwrite files into `data/` in shared resources.
- Do not generate new images via AI, f.ex. for textures. If an image is needed, use a placeholder image and tell the user which images are missing.

---

## 6. Architecture

### 6.1 Package layout

```text
omtteam.openmodularlighting
│
├── OML                            shared start-up: init() (config, packets) and registrationSteps() (registry.RegistrationStep:
│                                  registry + call), both used by the loader entrypoints; constants (MOD_ID, LOGGER), id(path)
│
├── core/                          ── pure Java, NO Minecraft imports (enforced by verifyCoreIsolation) ──
│   ├── math/                      Pos (int x,y,z record), Facing (6 directions), Rotation (port of MathUtil.rotate)
│   ├── beam/                      BeamShape (straight, narrow cone, wide cone, fluorescent, grow, UV), Ray,
│   │                              BeamMode (floodlight modes), GrowMode (lighting / black light)
│   ├── light/                     LightWorld (interface) + Cell, LightType (phantom / UV rules), BeamCaster,
│   │                              LightSources (all + active sources of a phantom/UV block), ShapeDiff
│   ├── state/                     LightController (active/inverted/timeout/re-cast state machine),
│   │                              PowerSource (interface: energy buffer or fuel timer), FuelTimer
│   ├── cluster/                   Cluster + ClusterNode (generic connected groups: join / leave / refresh-split, max 256;
│   │                              Minecraft-free, meant to be reused for multiblocks)
│   ├── energy/                    EnergyStore (energy buffer: insert/extract for other blocks, generate/consume/drain
│   │                              for the owner; taken over from OMT), EnergyAccess (what a block entity exposes)
│   └── formula/                   CarbonFuel (burn time), GrowChance (tick interval), EnergyCost
│
├── block/                         FloodlightBlock (base of every light: FACING + LIT, interaction, drops; configure()
│                                  for stick/wrench) → Electric…, Carbon…, UvFloodlightBlock, PanelLightBlock (thin, no
│                                  collision, PanelShapes) → FluorescentLightBlock (small/square), GrowLightBlock;
│                                  LightBlock (base of the light blocks: LIT, random-tick cleanup, re-cast requests,
│                                  canReplace) → PhantomLightBlock, UvLightBlock
├── blockentity/                   FloodlightBlockEntity (base: invert, colour, slot, power, cached beam) →
│                                  BeamFloodlightBE (BeamMode) → ElectricFloodlightBE, CarbonFloodlightBE;
│                                  UvFloodlightBE, GrowLightBE (GrowMode, growth), FluorescentLightBE;
│                                  LightSourceBlockEntity (interface of all lights); LightBlockEntity (sources,
│                                  save/load) → PhantomLightBE, UvLightBE (damage)
├── item/                          crafting items (filament, bulb, mantle …), LightDebugToolItem
├── menu/                          FloodlightMenu (+ disguise slot on CamoBlocks) → ElectricFloodlightMenu, CarbonFloodlightMenu;
│                                  LongContainerData (syncs longs as two shorts)
├── camo/                          disguise (§6.11): CamoBlock (marker), CamoRules (valid disguises), CamoData (state + NBT)
├── inventory/                     ItemAccess (container slot range + insert/extract filters as other blocks see them;
│                                  loader adapters in platform/<loader>)
├── registry/                      ModBlocks, ModItems, ModBlockEntities (+ lights() for the loader lookups), ModMenus, ModCreativeTabs
├── energy/                        ElectricPower (EnergyStore of a light), ClusterEnergy (pull + insert-only View),
│                                  Batteries (battery-slot check and discharge via platform.Energy)
├── world/                         LightBlocks (join / leave / setActive on light blocks, applies LightSources
│                                  effects), Positions (core ↔ Minecraft types), LevelLightWorld (core LightWorld
│                                  on top of Level)
├── network/                       OMLPackets (all packet types), ConfigSyncPacket
├── config/                        OMLConfig (load, validate, save, sync), ConfigData (the fields), @Comment, @Range,
│                                  ConfigWriter, ConfigRanges, ConfigValidator
├── compat/                        every 1.20.1/1.21.1 difference: VersionCompat, CompatBlockEntity, InteractiveBlock,
│                                  MenuCompat, network/ (NetworkCompat, PacketType); jade/ (optional Jade plugin:
│                                  OMLJadePlugin, FloodlightJadeProvider; FloodlightTooltip has the content, no Jade)
├── platform/                      loader-specific code (§4 "Loaders"); each node compiles only its own loader's package.
│   │                              Platform (holder) + PlatformServices (record of all services): installed once,
│   │                              first thing in the entrypoint; a getter before install or a second install throws
│   │                              IllegalStateException. Services: LoaderInfo (configDir, isModLoaded), Fuel (burnTime, remainder), Network, Menus,
│   │                              Energy (isBattery, dischargeItem, pushToBlock)
│   ├── fabric/                    OMLFabric (ModInitializer: install services, OML.init(), Fabric callbacks),
│   │                              FabricBlockLookups (item + energy lookups), FabricItemStorages, FabricEnergyStorages (Team Reborn
│   │                              adapters: SnapshotParticipant per EnergyStore, weak-keyed/-valued cache; cluster view),
│   │                              Fabric… service implementations
│   └── neoforge/                  OMLNeoForge (@Mod entrypoint: services, OML.init, steps per RegisterEvent, capabilities,
│                                  events), NeoForgeBlockEnabledCondition, NeoForge… service implementations;
│                                  FML constructs a mod's common @Mod class before its client one (OMLNeoForgeClient)
└── datagen/                       recipe, loot table, block tag, model providers (runs per version)

src/client/java/omtteam/openmodularlighting/client
├── OMLClient                      shared client start-up (init, registerScreens(ScreenRegistrar), appendTooltip)
├── platform/                      ClientPlatform + ClientPlatformServices (ClientNetwork), like Platform in main;
│   ├── fabric/                    OMLFabricClient (ClientModInitializer: wires Fabric's client callbacks), FabricClientNetwork,
│   │                              FabricCamoModel (disguise model, Renderer API)
│   └── neoforge/                  OMLNeoForgeClient (@Mod(dist = CLIENT): wires NeoForge's client events), NeoForgeClientNetwork,
│                                  NeoForgeCamoModel (disguise model, ModelData)
├── camo/                          CamoModels (which models get wrapped, LAMP_TINT_INDEX)
├── screen/                        FloodlightScreen (one status line) → Electric…, CarbonFloodlightScreen
├── render/                        FloodlightTint (dye colour × a brightness factor per block), LightBlockDebugRenderer
│                                  (light debug tool: wireframes around light blocks while held; called from the
│                                  loader's world render event, not a block entity renderer, so it costs nothing while
│                                  not held)
├── compat/                        ClientCompat, ClientNetworkCompat
└── network/                       ClientPacketHandlers (config sync handler)
```

Dependency direction: `client → main → core`. `core` depends on nothing. `main` never references `client`.

### 6.2 Blocks and items (from 1.7.10)

Every former metadata variant is its own block. Record old → new IDs in `docs/id-mapping.md`.

| Old ID (1.7.10) | New ID | Notes |
| --- | --- | --- |
| `electricFloodlight` | `electric_floodlight` | modes: straight / narrow cone / wide cone; GUI with battery slot |
| `carbonFloodlight` | `carbon_floodlight` | burns furnace fuel instead of energy; GUI with fuel slot |
| `uvFloodlight` | `uv_floodlight` | places UV light blocks that damage entities; no mode, no timeout |
| `growLight` | `grow_light` | modes: normal lighting / black light (no light, growth only); always placed facing down; energy only from the back |
| `smallElectricFloodlightMetaBlock` meta 0 | `small_fluorescent_light` | lights 5 neighbours; 6 px strip, `rotated` blockstate; energy only from the back |
| `smallElectricFloodlightMetaBlock` meta 1 | `square_fluorescent_light` | same, full square, not rotatable; shapeless recipes convert between the two |
| – (new) | `cube_light` | full block, lights its 6 neighbours (`BeamShape.cube`); energy from every side, shared with touching cube lights; `energyUsageCubeLight`, enabled with the fluorescent switch |
| `tilePhantomLight` | `phantom_light` | invisible, no collision, replaceable; `LIT` → light level 15, else 0 (§6.3) |
| `tileUVLight` | `uv_light` | `LIT` → light level 4 and damages living entities, else 0 and harmless; visibility configurable |
| items: `rawFilament`, `glowingFilament`, `electricIncandescentLightBulb`, `carbonDissolver`, `carbonLantern`, `mantle`, `lightDebugTool` | `raw_filament`, `glowing_filament`, `light_bulb`, `carbon_dissolver`, `carbon_lantern`, `mantle`, `light_debug_tool` | crafting items; while the debug tool is held, light blocks get wireframes (phantom yellow, UV magenta, dimmer while unlit), as with the 1.7.10 `TileEntityPhantomLightRenderer` |

Recipes (`datagen.OMLRecipeProvider`): raw filament (glowstone, redstone, gold) smelts into glowing filament → light bulb → electric/UV/grow lights and fluorescent lights. Mantle + carbon dissolver + flint and steel → carbon lantern → carbon floodlight.

- Iron, gold, redstone and glass panes use the common `c:` tags (`compat.CommonTags`); the other ingredients are vanilla items.
- **Config switches:** every recipe carries the resource condition `openmodularlighting:block_enabled` (`compat.BlockEnabledCondition`; registered by `platform/fabric/FabricBlockEnabledCondition`, a different API on each version, and `platform/neoforge/NeoForgeBlockEnabledCondition`) with the registry paths of the blocks that need it. It loads if any of them is enabled (`ConfigData.Blocks.isEnabled`). Datagen adds the condition to the recipe-unlock advancements too.
- Filament, glowing filament and light bulb are needed by electric floodlight, fluorescent lights, UV floodlight **and grow light**. Without the grow light in this list, bulbs could not be crafted with only the grow light enabled.
- Lightproof glass: shapeless, 1 black stained glass + 1 coal (`minecraft:coals`, so charcoal works too) → 1. It has no config switch, so its recipe carries no `block_enabled` condition (`Saver.saveAlways`).
- Also: fluorescent lights (`" G "`, `"GBG"`, `"III"` → 2, plus the shapeless conversion between small and square), UV floodlight (floodlight frame with black stained glass), grow light (`"III"`, `"CBC"`, `"GGG"` with pink stained glass).

**Panel lights (fluorescent, grow).** They are plain JSON block models, not block entity renderers: a 3 px panel on the back face, with a 64 × 32 box texture mapped onto it, and the dye tint as on the floodlights. The models are built facing up and turned per facing like vanilla's `end_rod` (`block.PanelShapes` uses the same table for the outlines). The panels have no collision, only an outline.

### 6.3 Light mechanics

> **Light blocks are not placed and removed on every switch.** Placing and removing blocks (with their block entities) is expensive, so **phantom light blocks stay in the world while their floodlight exists. Turning a light on or off only switches the `LIT` blockstate of its phantom lights (light level 15 ↔ 0).**

Light is made by **phantom light blocks** placed in air along the beam. Each phantom light has a `LIT` blockstate (`lightLevel = LIT ? 15 : 0`) and a block entity that stores which floodlights reach it and which of them are on.

#### Lifecycle

- **Floodlight placed** (or loaded without a cached shape): cast the beam and place a phantom light on every reachable air block, `LIT = false` unless the floodlight is already on. Remember the cast positions in the floodlight BE (see §6.7).
- **Floodlight turns on / off:** walk the cached positions only, without re-walking rays or reading other blocks. Mark this source on/off in each phantom light. A phantom light is `LIT` if at least one of its sources is on, and it changes its blockstate only when that result changes.
- **Mode change:** cast the new shape and **diff** it against the cached positions. Remove this source from positions that drop out, and add it to new ones (placing phantom lights where needed). Leave unchanged positions alone.
- **Floodlight broken** (`onRemove` / `setRemoved`): remove this source from all cached positions. A phantom light with **no sources at all** removes itself (set to air). This is the only time phantom lights are removed in normal play.

#### Casting

- Walk each ray outward. On air: place a phantom light, or reuse one at that position, and add this source. On an existing phantom light: add this source. On an opaque block: stop the ray. Non-opaque blocks (glass, plants) are passed through without placing light. UV light blocks are passed through without placing light.
- **Unloaded chunks:** never place or read blocks in unloaded chunks (avoids cascading chunk loads). Stop the ray instead.

#### Keeping the beam correct when the world changes

- **Re-cast:** when a phantom light is replaced, or a block next to it changes, it flags its sources for a re-cast. The floodlight re-casts at most once per tick, diff-based like a mode change. Never re-cast inside the neighbour-update callback itself.
- **Periodic re-check:** every light also flags itself for a re-cast at random intervals, uniform in 10–30 s (mean 20 s, `core.state.RecheckTimer`, not saved, so lights loaded together are staggered). This catches world changes that sent no block update (chunks edited while unloaded, mods setting blocks without updates).
- **Block updates:** place, switch and remove light blocks only through `world.LightBlocks`, which uses `LightBlocks.FLAGS` = `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`: no neighbour updates and no shape updates. Otherwise every toggle would cause neighbour updates on the next phantom light and start re-cast chains.
- **Orphans and stale state:** phantom light BEs do **not** tick (there can be thousands). Instead the block uses random ticks (`isRandomlyTicking`). On a random tick the phantom light drops sources whose block entity is gone, removes itself if none are left, and corrects `LIT` if it disagrees with its sources. This also fixes phantom lights in chunks that were unloaded while their floodlight toggled. Only query sources in loaded chunks; keep sources in unloaded chunks as they are.

**Phantom light block properties.** It must behave like air for everything except light, because it now also sits in the world while dark:

- no collision, no outline/selection shape, `RenderShape.INVISIBLE`, no drops, not in the creative tab;
- replaceable: `Properties.replaceable()`, and in the `minecraft:replaceable` and `minecraft:replaceable_by_trees` tags (datagen), so players can place blocks into it and trees, fluids and falling blocks treat it as air;
- `PushReaction.DESTROY`; it does not block mob spawning (an unlit phantom light must leave dark areas spawnable);
- strength 0 (`instabreak`), not unbreakable: a high explosion resistance would make phantom lights stop explosions, because explosions only skip real air;
- not `Properties.air()`: air blocks get no random ticks (chunk sections do not count them);
- no item; the block entity sends nothing to clients (the state is the `LIT` blockstate).

Beam shapes (defined along +X, then rotated to the facing):

| Shape | Range | Pattern |
| --- | --- | --- |
| Straight | `rangeStraightFloodlight` | single ray along the facing |
| Narrow cone | `rangeConeFloodlight` | 8 outer rays spreading `i/2` sideways (orthogonal + diagonal), plus 8 inner rays from distance 8 spreading `i/4`. An inner ray is skipped if its outer ray was blocked before distance 8. Aborts if the block in front is blocked. |
| Wide cone | `rangeConeFloodlight / 4` | 8 outer rays spreading `i` sideways, plus 8 inner rays from distance 4 spreading `i/2`. Aborts if the block in front is opaque. |
| Fluorescent | 1 | front + the 4 sideways neighbours |
| Cube light | 1 | all 6 neighbours |
| Grow light | 1 | front block only (only in "normal lighting" mode) |
| UV | `rangeUVFloodlight` | straight ray of `uv_light`. Replaces phantom lights it meets (their sources are flagged for a re-cast). |

**Thinning (`core.beam.Spacing`).** To save phantom lights, `BeamMode.shape` leaves out light blocks at some distances along the facing direction (beam-space x). The rays are still followed over every block, so walls stop them exactly as without thinning. Straight: lights at 1, 3, 5, … (the second block is the first one skipped). Both cones start at the first block and keep their full geometry. Their centre line is thinned like the straight beam (1, 3, 5, …); the rest of the cone is offset by one from it and skips the odd distances (2, 4, 6, …). Fluorescent, grow and UV beams are not thinned. `BeamCasterTest` compares against the reference loops (`Legacy1710Beams`) filtered by the same spacing. Consequence: a block change next to a skipped position may have no phantom light beside it, so the light then notices only at its periodic re-check (§6.3).

Keep the integer division and the "skip inner ray" rule of the beam loops. Cover the shapes with unit tests that compare against the reference loops.

### 6.4 Light state machine (all floodlights)

Implemented by `core.state.LightController#tick`; `main` refuels / charges first, then applies the result (re-cast first, then switch).

Every tick on the server:

1. If a re-cast is flagged (by a light block, a mode/config change or the periodic re-check): re-cast diff-based (§6.3). This happens on or off, because the phantom lights exist in both states, and is not delayed by the timeout (a re-cast does not switch anything).
2. If `timeout > 0`: decrement and return. The timeout (`timeoutFloodlights`) starts when a light turns off, to stop flickering. The UV floodlight, the cube light and the fluorescent lights have no timeout.
   The electric and carbon floodlight GUIs explain it with a "?" hover (`FloodlightMenu.hasTimeoutHint`), and toggling invert during it tells the player when the light turns on (`FloodlightBlockEntity.ticksUntilOn`).
3. `active = redstonePowered XOR inverted`.
4. If active **and** it can pay this tick's cost: on the first active tick switch the cached phantom lights on and set the floodlight's own `LIT` blockstate, then pay the cost. Remember `wasActive`.
5. Otherwise, if `wasActive`: switch the cached phantom lights off, clear `LIT`, start the timeout.

Changing the mode re-casts at once, diff-based. Breaking the block removes this source from its phantom lights, which removes the phantom lights it was the last source of (§6.3).
The timeout still applies: toggling `LIT` is much cheaper than placing blocks, but each change is still a light-engine update for every position.

### 6.5 Power and fuel

- Electric lights have an internal buffer of **50 000 E** (`core.energy.EnergyStore`; exposed as `EnergyAccess` through Team Reborn Energy on Fabric and FE on NeoForge, 1 E = 1 FE; accepting on all sides; fluorescent and grow lights only on the back face, opposite the facing). Up to the whole buffer can be inserted per tick. **Nothing can be extracted**, so cables cannot drain lights.
- **Panel lights share energy** (`blockentity.PanelLightBlockEntity`, `energy.ClusterEnergy`; cube lights use the same base but accept energy on every side and cluster only with cube lights on any of their 6 faces): fluorescent and grow lights (any mix) that touch on their sides and face the same way form a `core.cluster.Cluster` (in memory, not saved; joins on the first tick, leaves in `setRemoved`, splits in `refresh()` once per tick). Each light keeps its own buffer. The back-face storage shows the whole cluster: inserting fills the emptiest light first, nothing can be extracted. A light that cannot pay its next tick takes energy from the fullest others (each gives at most half).
- The battery slot accepts any item with extractable energy in the loader's energy API (`energy.Batteries` → `platform.Energy`), and is discharged into the buffer each tick while the buffer has room.
- Cost per tick: `energyUsage` for straight, ×2 for cones; `energyUsageSmallFloodlight` (fluorescent), `energyUsageCubeLight`, `energyUsageUVFloodlight`, `energyUsageGrowLight`.
- The carbon floodlight burns one furnace fuel item when `timeRemaining` reaches 0:
  `timeRemaining += carbonTime × burnTime / 1600 × 20` (coal = `carbonTime` seconds of straight light). The mode does not matter when refuelling; instead every tick the light runs uses up 1 (straight) or 2 (both cones, `CarbonFuel.drain`) of it, so cones burn fuel twice as fast. The GUI shows the time left in the current mode (`CarbonFuel.runTicks`). Refuelling happens even while the light is off.
  If the formula gives 0 ticks (tiny burn time, or `carbonTime = 0`), the item is **not** consumed (`FuelTimer.refuel` returns false).
  Fuel is any furnace fuel of the loader (`platform.Fuel`: Fabric's `FuelRegistry`, NeoForge's burn time incl. its data map). The remainder stays in the slot like in a furnace (lava bucket → bucket, `Fuel.remainder`, the loader's stack-aware hook).
- Inventories (1 slot, a vanilla `SimpleContainer`) are exposed as `inventory.ItemAccess.insertOnly` (`FloodlightBlockEntity#getItemAccess`): Fabric `ItemStorage.SIDED` (`platform/fabric/FabricItemStorages`, slotted for Jade), NeoForge `IItemHandler` (`NeoForgeItemHandler`). Insertion only accepts valid fuel or energy items. Extraction is disabled.

### 6.6 Interaction

| Action | Effect |
| --- | --- |
| Sneak + right-click, empty hand | toggle redstone invert (chat message "Inverted: true/false"; if the light now waits for its timeout, also "The light will turn on in x seconds") |
| Right-click with a stick or wrench | floodlights and grow light: cycle the mode (chat message "Mode: …"); small fluorescent light: turn it by 90°; square fluorescent and UV floodlight: nothing to set, the GUI opens |
| Sneak + right-click with a wrench | break the block and drop it |
| Right-click with dye | set render colour (dye index); right-click with white wool resets it (colour 16 = no tint) |
| Right-click with the light debug tool | fills the energy buffer of an electric light (debug aid) |
| Right-click (anything else) | open the GUI: carbon GUI for the carbon floodlight, the shared electric GUI (energy + battery slot) for all electric lights; the electric, carbon and UV floodlights also get the disguise slot (§6.11) |

Use the convention tag `compat.CommonTags.WRENCHES` (`c:wrenches` on 1.20.1, `c:tools/wrench` on 1.21) instead of mod-specific wrench checks. A stick always works for the mode, but only a wrench dismantles. Sneaking with any other item passes, so the item can be used (e.g. placing a block). Dye is not consumed; any wool resets the colour. Messages go to chat (`displayClientMessage(…, false)`).
Interaction goes through `compat.InteractiveBlock`, so it is the same on both versions.

### 6.7 Block entity data

Base data (`FloodlightBlockEntity`): `inverted`, `mode`, `color`, `camo` (§6.11), `active`, `wasActive`, `timeout`, `customName`, `owner`. Facing moves to the blockstate (`FACING`, plus `LIT`), not BE data.
Subclasses add the energy buffer, the 1-slot inventory, `timeRemaining` (carbon), `rotationState` (fluorescent) and `nextGrowTick` (grow light).
Every floodlight caches the positions of its current shape (the phantom or UV light blocks it placed or joined) as a `long[]` (`BlockPos.asLong`). It is saved with the BE so that turning on and off after a reload does not need a re-cast. A floodlight saved without it (or with a different mode) re-casts on its first tick.
Phantom and UV light BEs store their sources as two `long` lists, `sources` (all) and `activeSources` (those currently on). `LIT` follows `!activeSources.isEmpty()`, and the block is removed when `sources` is empty.
The client needs `color` and `camo` (and later `rotationState`) for rendering: send them in the update tag. On the client, a colour or disguise change re-renders the block (`CompatBlockEntity.remesh()`), because the tint and the disguise are read from the block entity (`client.render.FloodlightTint`: the dye's map colour × a brightness factor of the block).

**Dropped item.** A broken floodlight keeps mode, invert, colour, disguise and energy / burn time on the item (block entity data, `VersionCompat.setBlockEntityData`). Vanilla loads it back when the item is placed, and the tooltip shows "Configured". Also:
- the data is only added if something differs from a new light, so unconfigured drops still stack;
- the beam, the on/off state and the slot contents are not kept. Slot contents drop as items, so nothing is lost when a block is removed without drops (creative mode, commands).

### 6.8 Grow light

- Target = the block two in front. It grows only if the block in front is air or a phantom light (phantom lights count as air) and the target is a `BonemealableBlock` that `isValidBonemealTarget` (`VersionCompat`). Call `performBonemeal` on it. Growth only happens on ticks the light runs (and pays), also in black-light mode.
- The next grow tick is random: `nextGrowTick = now + rand(20 / chance) + 0.5 × 20 / chance` (one growth per `1 / chanceGrowLight` seconds on average). With `chanceGrowLight = 0` the crop never grows.

### 6.9 UV light damage

UV light blocks follow the same model as phantom lights (§6.3): they stay while their UV floodlight exists, and `LIT` switches them on and off (light level 4 ↔ 0).
The UV beam takes over phantom lights in its way (`LightBlock#canReplace`); the floodlights re-cast and pass the UV light block without light. A UV light block removes itself with neighbour updates (`removalFlags`), so those floodlights re-cast and fill the gap again.
Rendering: only while lit and if `uvLightRendered` is on (`getRenderShape`), cutout render layer, faces between UV light blocks hidden.
Once per second each **lit** `uv_light` block damages every `LivingEntity` in its 1×1×1 box by `damageUVFloodlight`. Use a custom damage type `openmodularlighting:uv_light` (datagen). The old mod used cactus damage.
Unlike phantom lights, UV light BEs have to tick for the damage. They do nothing (no entity query) while unlit, and once per second while lit. There are at most `rangeUVFloodlight` per UV floodlight.

### 6.10 Config

- No config library. Write a minimal Gson-based JSON config at `config/openmodularlighting.json`.
- Load it on init, write defaults if missing, and validate ranges.
- Fields are documented with `@Comment` and range-checked with `@Range` (package `config`). `ConfigWriter` writes them as `//` comments (JSONC) above each key, together with the default, and `ConfigRanges` clamps on load. Add both annotations to every new config field.
- Server-authoritative values that clients need (e.g. for tooltips) are synced on join.
- Read values with `OMLConfig.get()` (for example `OMLConfig.get().general.rangeStraightFloodlight`). Never cache them: on a client connected to a server, `get()` returns the server's config, and after disconnecting it returns the local one again.
- **Versioning** (as in OpenModularTurrets): the file records `configVersion`; files without it are version 1. On load, `ConfigMigration` replaces values that still equal an old default with the new one, then saves the file with the current version, so it runs once per file and values an owner chose stay.
  **When you change a default, raise `CURRENT_VERSION` and add a step** (with a test in `ConfigMigrationTest`).
- New fields go into `ConfigData` (sections `blocks` and `general`). Only `int`, `double`, `boolean` and `List<String>` are supported. Unknown keys are removed and wrong types reset to the default on load.

Fields (defaults and ranges are only in `ConfigData`, in `@Range` and the field initialisers; do not repeat them in docs):

| Field | Meaning |
| --- | --- |
| `electricFloodlightEnabled`, `smallElectricFloodlightEnabled`, `carbonFloodlightEnabled`, `uvFloodlightEnabled`, `growLightEnabled` | enable block (recipes) |
| `configVersion` | layout version of the file, for `ConfigMigration`; not meant to be edited |
| `energyUsage` | E/t electric floodlight (×2 for cones) |
| `energyUsageSmallFloodlight` | E/t fluorescent lights |
| `energyUsageCubeLight` | E/t cube light |
| `energyUsageUVFloodlight` | E/t UV floodlight |
| `energyUsageGrowLight` | E/t grow light |
| `carbonTime` | seconds one coal lasts (straight) |
| `rangeStraightFloodlight` | straight beam range |
| `rangeConeFloodlight` | cone range (wide cone = ¼) |
| `rangeUVFloodlight` | UV beam range |
| `chanceGrowLight` | growth chance per second |
| `timeoutFloodlights` | ticks before a light can turn on again |
| `damageUVFloodlight` | UV damage per second |
| `uvLightRendered` | client: show UV light blocks |

`IGWNotifierEnabled` is dropped.

### 6.11 Disguise (camouflage)

Taken over from OpenModularTurrets (`../OMT1.20.1+`, `camo/` and `FabricCamoModel` / `NeoForgeCamoModel`); keep the two in step where they share code.

- **Which lights:** the electric, carbon and UV floodlights (`camo.CamoBlock`). Not the panel lights (thin, no full face to disguise) and not the cube light (no front, it would vanish completely).
- **Valid disguises** (`CamoRules.isValidDisguise`): full, opaque blocks only (`isSolidRender`), drawn from a normal model, without a block entity. A floodlight hides its neighbours' faces like any solid block, so a see-through disguise would show holes into the world. The disguise is the default state of the item's block. An invalid disguise read from NBT is dropped.
- **Setting it:** the phantom slot in the floodlight GUI (`FloodlightMenu.CAMO_SLOT`, only on `CamoBlock`s). Clicking it with a block sets the disguise (the block stays in the cursor), with an empty cursor removes it; shift-clicking a block that does not fit the light's slot sets it too. Only the server changes it; anyone who may open the GUI may set it.
- **Data:** `FloodlightBlockEntity.camo` (`CamoData`), part of the settings: saved, sent in the update tag, kept on the dropped item.
- **Rendering:** the client wraps the models of the `CamoBlock`s (`client.camo.CamoModels`; Fabric `FabricCamoModel` through the Renderer API, NeoForge `NeoForgeCamoModel` through `ModelData`). With a disguise they emit the disguise's model (its own render layer, ambient occlusion and tint) and, of the light's own model, only the **lamp quads**: an extra front-face element in `models/block/floodlight.json` with `tintindex: 1` and the `#lamp` texture (`<light>_lamp[_on].png`, the ring and disc cut out of the front texture with the rest transparent). Without a disguise the overlay lies over the identical pixels of the front face, so the floodlights are in the cutout layer. Cut the lamp textures again if a front texture changes.
- **Tint:** the lamp keeps the dye colour (`FloodlightTint`). Fabric bakes the disguise's tint into its quads; on NeoForge `FloodlightTint` forwards every tint index other than 1 to the disguise's colour.
- Jade, the pick-block item and the break particles still show the real floodlight (the disguise is cosmetic, unlike OMT's hidden turrets).

---

## 7. Change Workflow

1. Implement gameplay logic in `core/` (with unit tests) where possible; keep Minecraft-facing code thin.
2. Wrap version differences in `compat/`.
3. Add assets, lang entries and datagen entries; regenerate data for **both** versions.
4. Add a `CHANGELOG.md` entry for user-visible changes.
5. Run `./gradlew chiseledBuild`.

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

Parts of the code exist almost identically in OpenModularTurrets (`../OMT1.20.1+`, branch `neoforge-test`), which has the same section:

- the platform layer (`platform/`, `client/platform/`, `NetworkCompat`, `PacketType`, `MenuCompat`), `ItemAccess`, `RegistrationStep`, `ScreenRegistrar`, `LongContainerData`, `@Comment` / `@Range`, `ConfigSyncPacket`, and `buildSrc` (`mod-common`);
- energy (`core.energy.EnergyStore`, `EnergyAccess`, `FabricEnergyStorages`, `NeoForgeEnergyHandler`);
- `CompatBlockEntity` (identical) and camouflage (`camo/CamoBlock`, `CamoData`, `CamoRules`, `client/camo/CamoModels`, `FabricCamoModel`, `NeoForgeCamoModel`; on the branch `worktree-floodlight-camo` until merged).
  OpenModularTurrets also has a `CamoHost`, a camo menu and screen, camo light properties and a camouflage trap.

The camo copies differ on purpose in three points only: the disguise rule (OpenModularTurrets: any full block; here: full opaque blocks only), extra quads on top of the disguise (the lamp overlay here), and the NBT key (`"camo"` here, `"Camo"` there; it must stay per mod, or saved disguises are lost).

Until there is a shared library:

- When you change one of these files, check whether the same change belongs in the other mod, and tell the user (do not edit the other repository unasked).
- Keep the camo differences limited to the three points above, so that they can later become parameters (a disguise rule, an overlay hook, the NBT key); shaping those is API design (§7 "Designing APIs").

Shared library (assessment from 2026-10-20, a recommendation; building it is the user's decision and a new dependency, §1; it would not be the old OMLib):

- Not yet: about 2,000 lines are shared, and copying the small service interfaces costs little.
- Extract it at the next Minecraft version bump, when both camo models have to be reworked anyway, or earlier if a third mod needs the code.
  Start with camouflage, `CompatBlockEntity` and the platform services.
- Bundle it jar-in-jar, so players do not download anything extra; it must then stay binary-compatible within a major version, because the loader picks the highest bundled version.
- The platform services need a redesign for a library: `Platform.install` allows one install per JVM, and the network registration uses the mod's own id.

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

Update this checklist as work progresses.

### Infrastructure

- [x] Gradle + Loom + Stonecutter skeleton; both versions build with an empty mod
- [x] `fabric.mod.json` templating, split source sets, datagen per version
- [x] `verifyCoreIsolation` task + JUnit setup
- [x] Spotless
- [x] GameTests (`src/gametest`, headless server, part of `check`)
- [x] `compat/` helpers (id, NBT save/load, item data, block entity base, block interaction, menus, networking, client rendering)
- [x] Config system (incl. server → client sync on join; sync not yet tested with a real client/server pair)
- [x] Publishing + `CHANGELOG.md` (CurseForge and Modrinth project IDs set)

### Core (`core/`, with tests)

- [x] `Pos`, `Facing`, `Rotation` (port of `MathUtil.rotate`)
- [x] Beam shapes: straight, narrow cone, wide cone, fluorescent, grow, UV
- [x] `BeamCaster` + `LightWorld` interface (opaque stop, unloaded-chunk stop, cone inner-ray rule)
- [x] `LightSources` bookkeeping (all + active sources) and `ShapeDiff`
- [x] `LightController` state machine (redstone/invert, timeout, diff re-cast, cost; toggles `LIT` instead of placing/removing)
- [x] Formulas: carbon burn time, grow tick interval, energy cost per mode

### Content

- [x] Phantom light block + BE (persistent, `LIT` toggle, replaceable/air-like, random-tick cleanup, neighbour re-cast); `LightBlocks` operations; GameTests
- [x] Electric floodlight (3 modes, energy, battery slot, GUI, dye tint, settings kept on the drop); GameTests
- [x] Carbon floodlight (fuel slot, burn time, GUI); GameTests
- [x] Fluorescent lights: small + square (rotation state, JSON panel models instead of BERs); GameTests
- [x] UV floodlight + UV light block (persistent with `LIT` toggle, damage type, `uvLightRendered`); GameTests
- [x] Grow light (normal/black-light mode, bonemeal ticks, JSON panel model); GameTests
- [x] Crafting items + recipes (datagen, config condition), loot tables, creative tab; recipes of the fluorescent, UV and grow lights come with those blocks
- [x] Interactions: invert, mode cycle / rotation with stick or wrench tag, wrench break, dye colour (`FloodlightBlock`)
- [x] Team Reborn Energy integration (jar-in-jar)
- [x] Transfer API (`ItemStorage.SIDED`) for the 1-slot inventories
- [x] Assets: textures from `oldmod`, models, blockstates, lang (en_us, de_de, ru_ru)
- [x] `docs/id-mapping.md`

### NeoForge 1.21.1 (branch `neoforge-test`, reusable for OMT)

- [x] Build: Stonecutter nodes `<version>-<loader>`, `buildSrc` convention plugin, `build.neoforge.gradle.kts` (ModDevGradle), `neoforge.mods.toml` template, client source set, `platform/<loader>` exclusion, loader constants, publishing (enabled from 1.0.0-beta.3)
- [x] `@Mod` entrypoint loads in a NeoForge GameTest server run
- [x] `platform/` shape (§7): wiring from the entrypoints; services via `Platform` / `PlatformServices` (+ `ClientPlatform` / `ClientPlatformServices`)
- [x] Entrypoints split: `OML.init()` shared, `OMLFabric` / `OMLFabricClient` in `platform/fabric`
- [x] Service `LoaderInfo` (`configDir`, `isModLoaded`)
- [x] Service `Fuel` (`burnTime`, `remainder`)
- [x] Services `Network` + `ClientNetwork` (`ClientPlatform` / `ClientPlatformServices`, NeoForge client entrypoint `OMLNeoForgeClient`)
- [x] Service `Menus` (`createBlockMenuType`, `openBlockMenu`; `BlockPos` only)
- [x] `core.energy.EnergyStore` + `EnergyAccess`; service `Energy` (`isBattery`, `dischargeItem`, `pushToBlock`); Fabric adapters + lookups in `platform/fabric`; NeoForge `NeoForgeEnergyHandler` (FE adapter)
- [x] Item transfer: `inventory.ItemAccess`; Fabric `FabricItemStorages`, NeoForge `NeoForgeItemHandler`
- [x] Registration: `OML.init()` + `OML.registrationSteps()` (NeoForge: per `RegisterEvent`, safety check at common setup); capabilities (items, energy); block use, config sync on join, recipe condition codec, creative tab builder, plain `c:` tags; all main code except `datagen` compiles on NeoForge, unit tests run there too
- [x] Checked by hand in a NeoForge client: blocks, interaction, menus, tint, render types, debug renderer, tooltip
- [x] Client: `OMLClient` + `ScreenRegistrar`; screens, colour handler, render types (model JSON), debug renderer, tooltip, disconnect hook wired in `OMLNeoForgeClient` (check by hand with `:1.21.1-neoforge:runClient`)
- [x] Data on NeoForge: converted from the Fabric 1.21.1 datagen output at build time (`convertFabricData`, `datagen_from`); loading it on NeoForge is checked by the NeoForge GameTests (`RecipeGameTest`)
- [x] GameTests on NeoForge: shared test classes (`OMLGameTests`, own empty structure, NeoForge annotations behind a condition), `TestTransfer`, loader-neutral test battery; all 54 run on NeoForge
- [x] Jade on NeoForge: `@WailaPlugin` on `OMLJadePlugin` (Fabric keeps the entrypoint); plugin load checked once in a NeoForge run; overlay itself: check by hand with `:1.21.1-neoforge:runClient`

### Later / optional (ask first)

- [x] Jade tooltip (replaces WAILA: invert and mode; optional dependency, the user chose Jade only, no WTHIT); GameTests
- [x] Light debug tool function (wireframe renderer; client only, check with `runClient`)
- [x] Disguise for the electric, carbon and UV floodlights (taken over from OMT; disguise slot, lamp overlay, kept on the drop; §6.11); GameTests. Rendering not yet checked by hand in `runClient` (both loaders)

---

## 10. Further Information

1. **Energy API:** Team Reborn Energy is acceptable (jar-in-jar)
2. **Mappings:** Mojang official
3. **Package name:** keep `omtteam.openmodularlighting`
4. **Reference source:** `oldmod/` (FloodLights for Forge 1.7.10, by Keridos). Key classes: `tileentity/TileEntityMetaFloodlight` (beam shapes), `tileentity/TileEntityPhantomLight` (source tracking), `tileentity/TileEntityFLElectric` (energy), `handler/ConfigHandler`, `handler/RecipeHandler`.
5. The 1.7.10 CurseForge page is `224728-floodlights`.
