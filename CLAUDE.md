# CLAUDE.md — Floodlights (Forge 1.12.2, branch `1.12`)

This file guides Claude Code when working on this branch. Read it fully before making changes.
This branch is the **Forge 1.12.2** port; the modern versions (Fabric 1.20.1/1.21.1, NeoForge 1.21.1, Forge 1.20.1, built with Stonecutter) live on the main branch `1.20.1+`, whose `CLAUDE.md` describes them.

---

## 1. Project Overview

**Floodlights (FL)** is a port of the Forge 1.7.10 mod *FloodLights* (`oldmod/`, package `de.keridos.floodlights`, mod ID `FloodLights`; gitignored, read it from the main checkout).
This branch builds it for **Minecraft 1.12.2 on Forge** (requested by the user on 2026-10-24) and reuses the main branch's code: `core/` and `config/` are the same classes (converted to Java 8), and the Minecraft-facing code was rewritten for 1.12.2 with the same structure, names and behaviour.

`oldmod/` is the **read-only reference**. Never edit it and never build it.
It is a source of ideas, values and assets, not a specification: the beam thinning, the persistent light blocks and other details differ on purpose.
Key classes: `tileentity/TileEntityMetaFloodlight` (beam shapes), `tileentity/TileEntityPhantomLight` (source tracking), `tileentity/TileEntityFLElectric` (energy), `handler/ConfigHandler`, `handler/RecipeHandler`.

Core gameplay (the same as on the main branch):

- **Floodlights** light large areas with invisible light blocks ("phantom lights") along a beam: a straight line, a narrow cone or a wide cone. The phantom lights stay in place and are only switched on and off (see §6.3).
- **Grow light**: lights the block in front and gives random growth ticks to a crop two blocks ahead.
- **UV floodlight**: a short beam of UV light blocks that damages living entities standing in it.
- **Fluorescent lights** and the **cube light**: small, cheap lights that illuminate their neighbouring blocks.

### Keeping the branches together

- `core/` and `config/` (and their unit tests) should stay identical to the main branch except for Java 8 syntax (no records, switch expressions, `instanceof` patterns, `List.of`, `RandomGenerator`; core takes `java.util.Random`). When a change lands on one branch, port it to the other, and tell the user.
  Exception: `core.energy` (`EnergyStore`, `EnergyAccess`) comes from OMLib (`omtteam.omlib.core.energy`, the same classes) and is not part of this mod.
- Gameplay changes on the main branch usually belong here too; the Minecraft-facing classes have the same names and packages, except that this branch uses the MCP-style names of the OMTTeam 1.12 workspace (decided by the user, 2026-10-25): `blockentity` → `tileentity` (`…BlockEntity` → `…TileEntity`, `ModBlockEntities` → `ModTileEntities`, `CompatBlockEntity` → `CompatTileEntity`), `menu` → `container` (`…Menu` → `…Container`, `OMLGuiHandler` in `handler`), `client.screen` → `client.gui` (`…Screen` → `…Gui`).
- Never merge this branch into `1.20.1+` or the other way round: the build and most of `main` differ completely. Cherry-pick or port by hand.

### Allowed dependencies

- **Forge 1.12.2** (14.23.5.x, chosen by RetroFuturaGradle) and **Forge Energy** (`CapabilityEnergy`, part of Forge; replaces Team Reborn Energy / NeoForge FE, 1 E = 1 FE).
- **OMLib** (required, `required-after:omlib`): the shared library of the OpenModularTurretsTeam mods (`../OMLib` in the OMTTeam 1.12 workspace). Code it already has is used from there, not duplicated: the energy buffer (`core.energy`) and its Forge Energy adapter (`power.EnergyAccessStorage`), its wrench (`ItemMultiTool`, accepted by `CommonTags.isWrench`), the chunk-safe tile entity lookup (`util.world.WorldUtil.getTileEntity`) the camouflage (`ICamoSupport`, the camouflage menu, `CamoRules`, §6.11) and the server → client config sync (`config.ConfigSync`, §5).
- Build-time only: **RetroFuturaGradle** (`com.gtnewhorizons.retrofuturagradle`, the maintained 1.12.2 toolchain, from the GTNH maven), the mod publish plugin, Spotless, JUnit 5 (5.13: the last line that runs on Java 8), EqualsVerifier (3.x: the last line that runs on Java 8) and PIT (1.17, likewise).
- **Hwyla** (optional, compile-only; the mod from CurseMaven, maven.tehnut.info no longer serves it): the block info overlay instead of Jade, which does not exist on 1.12.2 (requested by the user on 2026-10-24). `compat.hwyla.OMLWailaPlugin` (`@WailaPlugin`, only loaded by Hwyla) registers `FloodlightWailaProvider`; the content is `FloodlightTooltip` (invert and mode, as the main branch's Jade tooltip), switchable in Hwyla's config (`option.openmodularlighting.light`).
- Nothing else without asking the user first. No mixins, no Jabel, no CTM/TOP dependency. A The One Probe integration would be optional and is not done; ask the user first.

---

## 2. Tech Stack

| | Forge 1.12.2 |
| --- | --- |
| Java | 8 (Azul toolchain, auto-provisioned) |
| Loader | Forge 14.23.5.2847 |
| Gradle plugin | RetroFuturaGradle 2.0.x |
| Mappings | MCP `stable_39` (the final 1.12 names, such as `TileEntity`, `IBlockState`, `EntityPlayer`, `World`), the same as OMLib and the OMTTeam 1.12 workspace: one dev run has one set of names |

- Gradle 9.8 (Kotlin DSL), one project: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`. No Stonecutter, no `buildSrc`.
- **Gradle itself must run on Java 17+** (Gradle 9). In the user's shell `JAVA_HOME` points to Java 8, so run Gradle with `JAVA_HOME=/usr/lib/jvm/java-25-openjdk` (or any JDK 17+); the compile and test toolchain is Java 8 regardless.
- The decompiled Minecraft + Forge sources are in `build/rfg/minecraft-src/java` after `./gradlew setupDecompWorkspace`. Look up exact MCP names there instead of guessing.
- **OMLib** comes from mavenLocal (`omtteam:omlib:1.12.2-<omlib_version>`, `omlib_version` in `gradle.properties`): run `./gradlew -b build.gradle publishToMavenLocal` in OMLib first. `OML.DEPENDENCIES` says `omlib@[0.0.0,)` in the sources (dev runs, where OMLib's version stays unreplaced); `replaceOmlibRange` compiles a copy of `OML.java` with `[<omlib_version>,<next major>.0.0-alpha)`.
- **Workspace build:** in the OMTTeam 1.12 workspace (the folder above) this mod is also built by `gradle/workspace/OpenModularLighting.gradle` (ForgeGradle 3, Gradle 4.9) against the OMLib sources and runs with the other mods in `runenv`; that build ignores `build.gradle.kts` and sets up the tests and `verifyCoreIsolation` itself.
- **RFG and the shared MCP cache:** RFG unpacks the MCP data (`joined.srg`, `joined.exc`, patches) into `~/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_<channel>/<version>` only if that folder does not exist yet; a folder created by ForgeGradle 2 holds only the CSVs and makes `generateForgeSrgMappings` fail ("joined.srg doesn't exist"). `mcp_stable/39` was created by RFG itself, so this only matters when switching mappings: copy those files from an RFG mapping folder (they do not depend on the names).
- Formatting: Spotless (tabs → spaces, trailing whitespace, final newline), as on the main branch. Run `./gradlew spotlessApply` before committing.
- Language: Java only.

---

## 3. Repository Layout

```text
.
├── CLAUDE.md, CHANGELOG.md, README.md, CONTRIBUTING.md, CLA.md, LICENSE*   # as on the main branch
├── build.gradle.kts            # RetroFuturaGradle, tests, verifyCoreIsolation, Spotless, changelog tasks, publishing
├── settings.gradle.kts
├── gradle.properties           # mod_id, mod_version, mapping version, publishing settings …
├── docs/                       # id-mapping.md; architecture.md describes the main branch
└── src/
    ├── main/java/omtteam/openmodularlighting/...   # common and client code (one source set)
    ├── main/resources/         # mcmod.info (templated) and assets/ (models, blockstates, lang, textures, recipes)
    └── test/java/omtteam/openmodularlighting/...   # JUnit tests (core/, config)
```

- **Mod ID:** `openmodularlighting`. **Base package:** `omtteam.openmodularlighting` (keep it, as on the main branch).
- 1.12.2 has no split source sets. Client-only code lives in the `client` package and is reached only through the `@SidedProxy` (`CommonProxy` / `client.ClientProxy`). **Main code never references `client` classes.**
- The mod version is `<mod_version>+1.12.2-forge`; `mcmod.info` is templated from `gradle.properties` (`${version}`, `${mod_id}`, `${mod_name}`, `${minecraft_version}`).

---

## 4. Build

### Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk   # Gradle needs 17+
./gradlew build                  # compile, unit tests, verifyCoreIsolation, spotlessCheck, reobfuscated jar
./gradlew setupDecompWorkspace   # decompiled MC + Forge sources in build/rfg/minecraft-src
./gradlew runClient              # dev client (run dir run/)
./gradlew runServer              # dev server; RetroFuturaGradle asks for the EULA: never accept it for the user
./gradlew spotlessApply
./gradlew release                # checkRelease, build, publishMods, tag <version> (or ./release.sh in the workspace)
```

- The released jar is `build/libs/openmodularlighting-<mod_version>-mc1.12.2-forge.jar` (`<mod_id>-<version>-mc<minecraft_version>-<loader>`, as in the other OMTTeam 1.12 mods), made by `reobfJar` (SRG names). `-dev.jar` is the deobfuscated one, `-sources.jar` the sources. The mod version and the release tag stay `<mod_version>+1.12.2-forge`.
- **Definition of done for any change:** `./gradlew build` succeeds (this includes the unit tests).
- There are **no GameTests** on 1.12.2 (the framework does not exist). Check in-game behaviour by hand with `runClient` (single player needs no EULA), and say what was and was not checked.
- The dedicated server checks the EULA before any mod loads, so it cannot be used for a smoke test without accepting the EULA. Do not accept it on the user's behalf.

### Publishing

- Same plugin, project IDs, token handling and changelog workflow as on the main branch (`CHANGELOG.md`, `./gradlew changelogEntry`, `./gradlew releaseChangelog`).
- The old 1.7.10 CurseForge page is `224728-floodlights`.
- Releases go with the other OMTTeam mods: `./release.sh` in the workspace (after OMLib, whose release OML builds against from mavenLocal) or `./gradlew release` here: `checkRelease` (CHANGELOG section of `mod_version`, `publish_enabled`, tokens, `omlib_version` = `../OMLib`, clean git tree, tag absent, `checkOmlibChangelog`) → `build` → `publishMods` → annotated tag `<mod_version>+1.12.2-forge` (the 1.20.1+ tags are plain `1.0.0-beta.N`).
- `publish_enabled=true` since 1.0.0-beta.6 (decided by the user, 2026-10-24); `-Ppublish_enabled=false` makes a single run a dry run. CurseForge relations: OMLib required, Hwyla optional; Modrinth none until OMLib has a project there.
- `checkOmlibChangelog` runs before every publish task (also dry runs): if `omlib_version` differs from the one at the previous release tag, the version's `CHANGELOG.md` section must contain `OMLib <omlib_version>` ("Requires OMLib <version> or newer" under *Changed*, see the workspace `CLAUDE.md`).
- Changelog entries of this branch describe 1.12.2 changes; the main branch has its own `CHANGELOG.md`.

### Core Game Logic Source

The game logic lives in the `core` package and is free of Minecraft imports, exactly as on the main branch: beam shapes, rotation, light-source bookkeeping, the light state machine, formulas, clusters and the energy store, with unit tests.
`./gradlew check` runs `verifyCoreIsolation`, which fails if anything under `core/` imports `net.minecraft`, `net.minecraftforge`, `com.mojang` or non-core mod packages.
The original beam loops are kept in the test sources as a reference (`core/light/Legacy1710Beams`); `BeamCasterTest` checks `BeamCaster` against them.
`./gradlew pitest` (on demand, not part of `check`) runs PIT mutation testing of `core`, as `mod-common.gradle` does in the other mods; it skips the test helpers in the core packages (`TestWorld`, `Legacy1710Beams`). Report: `build/reports/pitest/index.html`.
`core.EqualsContractTest` checks equals and hashCode of the core value classes with EqualsVerifier.
The main branch's ArchUnit, JaCoCo and PMD setup is not on this branch.

---

## 5. 1.12.2 specifics

Differences to the main branch, and the 1.12.2 pitfalls the code handles. Keep them in mind when porting a change.

| Area | 1.12.2 |
| --- | --- |
| Registration | `Registration` handles `RegistryEvent.Register<Block/Item>` (fired after pre-init); block entity classes via `GameRegistry.registerTileEntity` in `ModTileEntities`. Blocks and items are created statically in `ModBlocks` / `ModItems` (registry name + translation key there) |
| Blockstate | metadata: facing index in bits 0–2, `LIT` in bit 3 (`FloodlightBlock`); light blocks: `LIT` only. The small fluorescent light's `ROTATED` does not fit and lives in its block entity (`getActualState`) |
| Tile entities | `CompatTileEntity` (`readData` / `writeData` / `writeClientData`, update packets, `syncToClient`, `remesh`). **It overrides `shouldRefresh`**: Forge otherwise recreates a mod tile entity on every state change, which would wipe a light whenever `LIT` switches |
| Ticking | `ITickable` on the floodlights and the UV light; the phantom light's block entity does not tick (random ticks on the block) |
| Block flags | `LightBlocks.FLAGS` = 2 \| 16 (clients, no observers, no neighbour notification); `UPDATE_ALL` = 3 for the UV light's removal |
| Phantom light | `Material.AIR` (so `isAir` is true, as in 1.7.10): falling blocks, trees, pistons and explosions treat it as air. Random ticks still work (1.12 counts them per block). `LevelLightWorld` and `LightBlocks.join` check the light blocks **before** `isAir` |
| UV light | a `MaterialTransparent` (replaceable, not air), `getRenderType` by `LIT` and `uvLightRendered` |
| Lightproof glass | own `LightproofGlassBlock` (no tinted glass in 1.12): glass with light opacity 255 that drops itself |
| Drops | `FloodlightBlock` keeps the block until `harvestBlock` (`removedByPlayer`), so `getDrops` can read the block entity; settings go into the item's `BlockEntityTag`, which vanilla loads on placement |
| Interaction | `FloodlightInteraction` listens to `PlayerInteractEvent.RightClickBlock` (fires before the sneak check, also for wrenches that do not bypass sneaking) |
| Wrenches, wool, dyes | no tags: `CommonTags.isWrench` (OMLib's `ItemMultiTool`, tool class `wrench` or ore dictionary `toolWrench`/`itemWrench`/`wrench`), ore dictionary `blockWool`, Forge `DyeUtils` |
| Energy, items | capabilities on the tile entities: OMLib's `power.EnergyAccessStorage` wraps the core `EnergyAccess` (insert only, `extractable = false`), `InsertOnlyItemHandler` wraps the slot's `ItemStackHandler`; batteries are items with extractable FE |
| Fuel | `TileEntityFurnace.getItemBurnTime` and the item's container item |
| Menus | `OMLGuiHandler` (`IGuiHandler`, ids `ELECTRIC` / `CARBON`); screens come from `ClientProxy.createGui`. `FloodlightContainer` syncs long values as two 16-bit window properties |
| Network | no channel of its own: the config goes through OMLib's `config.ConfigSync` (registered in `OML.preInit` as one value: `OMLConfig.toJson` sent on login, `OMLConfig.applyServerConfig` on the client; OMLib applies the local config's JSON again after disconnecting); the camouflage menu uses OMLib's messages |
| Disguise | OMLib's `BlockAbstractCamoTileEntity.RENDERBLOCKSTATE` is an unlisted property in the floodlights' `ExtendedBlockState` (`FloodlightBlock.supportsCamo`); `client.camo.CamoModel` wraps their blockstate models in `ModelBakeEvent` and returns quads per render layer (`MinecraftForgeClient.getRenderLayer()`); camo blocks render in every layer but translucent |
| Damage | `UvLightTileEntity.UV_LIGHT` (`DamageSource("openmodularlighting.uv_light")`) |
| Config | Gson 2.2.4 ships with 1.12: `new JsonParser().parse(...)` (lenient, reads the `//` comments), no `keySet()`; config folder from `FMLPreInitializationEvent` |
| NBT | position lists are flat `int` arrays (`PosArrays`): 1.12 cannot read long arrays back |

### Resources

- `assets/` is the main branch's, adapted: blockstate `model` names are relative to `models/block/` (`"openmodularlighting:electric_floodlight"`); variant keys list every property; light blocks use `lit=…`, the glass `normal`; the cube light uses a state mapper that ignores `facing`.
- Block textures must be square: the panel textures are 64 × 64 with a transparent lower half (the main branch's are 64 × 32), and the panel models use half the main branch's v coordinates.
- A texture variable must not point to another variable of the same model (1.12 cannot resolve it): name the texture directly.
- `pack.mcmeta` (`pack_format: 3`) must stay: without it Forge substitutes format 2, and Minecraft then looks for `en_US.lang` and ignores the lowercase lang files.
- Lang files are `.lang` (`key=value`): blocks `tile.openmodularlighting.<id>.name`, items `item.openmodularlighting.<id>.name`, the rest as on the main branch.
- **No datagen:** recipes are handwritten JSON in `assets/openmodularlighting/recipes/` (ore dictionary `ingotIron`, `ingotGold`, `dustRedstone`, `paneGlass`; metadata for stained glass and coal/charcoal). They carry the condition `openmodularlighting:block_enabled` (registered in `recipes/_factories.json`, class `compat.BlockEnabledCondition`); recipes load once at start-up from the local config. Smelting is code (`ModRecipes`). There are no recipe-unlock advancements and no loot tables (blocks drop through `getDrops`).
- Do not generate new images via AI. If an image is needed, use a placeholder and tell the user which images are missing.

---

## 6. Architecture

### 6.1 Package layout

```text
omtteam.openmodularlighting
├── OML                 @Mod entrypoint (pre-init: config, network, event handlers; init: GUI handler, smelting), MOD_ID, LOGGER, id(path)
├── CommonProxy         server side of the @SidedProxy (client: client.ClientProxy)
├── core/               ── pure Java 8, NO Minecraft imports; the same classes as on the main branch ──
├── block/              FloodlightBlock (+ FloodlightInteraction) → Electric…, Carbon…, UvFloodlightBlock, CubeLightBlock,
│                       PanelLightBlock (PanelShapes) → FluorescentLightBlock, GrowLightBlock; LightBlock → PhantomLightBlock,
│                       UvLightBlock; LightproofGlassBlock
├── tileentity/         FloodlightTileEntity (LightBeam) → BeamFloodlightBE → Electric…, Carbon…; UvFloodlightBE,
│                       PanelLightTileEntity → FluorescentLightBE, GrowLightBE, CubeLightBE; LightTileEntity → PhantomLightBE, UvLightBE
├── container/          FloodlightContainer → Electric…, CarbonFloodlightContainer
├── handler/            OMLGuiHandler
├── inventory/          InsertOnlyItemHandler
├── registry/           ModBlocks, ModItems, ModTileEntities, ModCreativeTabs, ModRecipes, Registration
├── energy/             ElectricPower, FuelPower, LightPower, ClusterEnergy, Batteries
├── world/              LightBlocks, LevelLightWorld, Positions, PosArrays
├── config/             as on the main branch (Java 8)
├── compat/             CompatTileEntity, CommonTags, BlockEnabledCondition
└── client/             ClientProxy, ClientEvents (models, colours, tooltip, debug renderer),
                        camo/ (CamoModel, CamoModels), render/ (FloodlightTint, LightBlockDebugRenderer), gui/
```

Dependency direction: `client → main → core`. `core` depends on nothing. `main` never references `client` (only through the proxy).

### Gameplay (§6.2–§6.11)

The following sections are the main branch's and apply here unchanged in behaviour.
Read them with §5 at hand: "Team Reborn Energy" / "FE" means Forge Energy, "datagen" means the handwritten recipe JSON, "tags" means the ore dictionary, "GameTests" do not exist, Jade is not available, and `fabric`/`neoforge`/`platform` classes have no counterpart (their job is done by `OML`, the proxies and the block entities' capabilities).

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
   The electric and carbon floodlight GUIs explain it with a "?" hover (`FloodlightContainer.hasTimeoutHint`), and toggling invert during it tells the player when the light turns on (`FloodlightTileEntity.ticksUntilOn`).
3. `active = redstonePowered XOR inverted`.
4. If active **and** it can pay this tick's cost: on the first active tick switch the cached phantom lights on and set the floodlight's own `LIT` blockstate, then pay the cost. Remember `wasActive`.
5. Otherwise, if `wasActive`: switch the cached phantom lights off, clear `LIT`, start the timeout.

Changing the mode re-casts at once, diff-based. Breaking the block removes this source from its phantom lights, which removes the phantom lights it was the last source of (§6.3).
The timeout still applies: toggling `LIT` is much cheaper than placing blocks, but each change is still a light-engine update for every position.

### 6.5 Power and fuel

- Electric lights have an internal buffer of **50 000 E** (`core.energy.EnergyStore`, from OMLib on this branch; exposed as `EnergyAccess` through Team Reborn Energy on Fabric and FE on NeoForge, 1 E = 1 FE; accepting on all sides; fluorescent and grow lights only on the back face, opposite the facing). Up to the whole buffer can be inserted per tick. **Nothing can be extracted**, so cables cannot drain lights.
- **Panel lights share energy** (`blockentity.PanelLightTileEntity`, `energy.ClusterEnergy`; cube lights use the same base but accept energy on every side and cluster only with cube lights on any of their 6 faces): fluorescent and grow lights (any mix) that touch on their sides and face the same way form a `core.cluster.Cluster` (in memory, not saved; joins on the first tick, leaves in `setRemoved`, splits in `refresh()` once per tick). Each light keeps its own buffer. The back-face storage shows the whole cluster: inserting fills the emptiest light first, nothing can be extracted. A light that cannot pay its next tick takes energy from the fullest others (each gives at most half).
- The battery slot accepts any item with extractable energy in the loader's energy API (`energy.Batteries` → `platform.Energy`), and is discharged into the buffer each tick while the buffer has room.
- Cost per tick: `energyUsage` for straight, ×2 for cones; `energyUsageSmallFloodlight` (fluorescent), `energyUsageCubeLight`, `energyUsageUVFloodlight`, `energyUsageGrowLight`.
- The carbon floodlight burns one furnace fuel item when `timeRemaining` reaches 0:
  `timeRemaining += carbonTime × burnTime / 1600 × 20` (coal = `carbonTime` seconds of straight light). The mode does not matter when refuelling; instead every tick the light runs uses up 1 (straight) or 2 (both cones, `CarbonFuel.drain`) of it, so cones burn fuel twice as fast. The GUI shows the time left in the current mode (`CarbonFuel.runTicks`). Refuelling happens even while the light is off.
  If the formula gives 0 ticks (tiny burn time, or `carbonTime = 0`), the item is **not** consumed (`FuelTimer.refuel` returns false).
  Fuel is any furnace fuel of the loader (`platform.Fuel`: Fabric's `FuelRegistry`, NeoForge's burn time incl. its data map). The remainder stays in the slot like in a furnace (lava bucket → bucket, `Fuel.remainder`, the loader's stack-aware hook).
- Inventories (1 slot, a vanilla `SimpleContainer`) are exposed as `inventory.ItemAccess.insertOnly` (`FloodlightTileEntity#getItemAccess`): Fabric `ItemStorage.SIDED` (`platform/fabric/FabricItemStorages`, slotted for Jade), NeoForge `IItemHandler` (`NeoForgeItemHandler`). Insertion only accepts valid fuel or energy items. Extraction is disabled.

### 6.6 Interaction

| Action | Effect |
| --- | --- |
| Sneak + right-click, empty hand | toggle redstone invert (chat message "Inverted: true/false"; if the light now waits for its timeout, also "The light will turn on in x seconds") |
| Right-click with a stick or wrench | floodlights and grow light: cycle the mode (chat message "Mode: …"); small fluorescent light: turn it by 90°; square fluorescent and UV floodlight: nothing to set, the GUI opens |
| Sneak + right-click with a wrench | break the block and drop it |
| Right-click with dye | set render colour (dye index); right-click with white wool resets it (colour 16 = no tint) |
| Right-click with the light debug tool | fills the energy buffer of an electric light (debug aid) |
| Right-click (anything else) | open the GUI: carbon GUI for the carbon floodlight, the shared electric GUI (energy + battery slot) for all electric lights; the electric, carbon and UV floodlights also get a Camouflage button (§6.11) |

Use the convention tag `compat.CommonTags.WRENCHES` (`c:wrenches` on 1.20.1, `c:tools/wrench` on 1.21) instead of mod-specific wrench checks. A stick always works for the mode, but only a wrench dismantles. Sneaking with any other item passes, so the item can be used (e.g. placing a block). Dye is not consumed; any wool resets the colour. Messages go to chat (`displayClientMessage(…, false)`).
Interaction goes through `compat.InteractiveBlock`, so it is the same on both versions.

### 6.7 Block entity data

Base data (`FloodlightTileEntity`): `inverted`, `mode`, `color`, the disguise (§6.11), `active`, `wasActive`, `timeout`, `customName`, `owner`. Facing moves to the blockstate (`FACING`, plus `LIT`), not BE data.
Subclasses add the energy buffer, the 1-slot inventory, `timeRemaining` (carbon), `rotationState` (fluorescent) and `nextGrowTick` (grow light).
Every floodlight caches the positions of its current shape (the phantom or UV light blocks it placed or joined) as a `long[]` (`BlockPos.asLong`). It is saved with the BE so that turning on and off after a reload does not need a re-cast. A floodlight saved without it (or with a different mode) re-casts on its first tick.
Phantom and UV light BEs store their sources as two `long` lists, `sources` (all) and `activeSources` (those currently on). `LIT` follows `!activeSources.isEmpty()`, and the block is removed when `sources` is empty.
The client needs `color` and the disguise (and later `rotationState`) for rendering: send them in the update tag. On the client, a colour or disguise change re-renders the block (`CompatTileEntity.remesh()`), because the tint and the disguise are read from the block entity (`client.render.FloodlightTint`: the dye's map colour × a brightness factor of the block).

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
- Server-authoritative values that clients need (e.g. for tooltips) are synced on join (on this branch through OMLib's `ConfigSync`, §5).
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

Taken over from OpenModularTurrets (`../OMT1.20.1+`, `camo/` and `FabricCamoModel` / `NeoForgeCamoModel`); on this branch the lights use OMLib's camouflage (since 2026-10-25, requested by the user), like OMT's turret bases and OMPD's camouflage trap.

- **Which lights:** the electric, carbon and UV floodlights (`FloodlightBlock.supportsCamo`). Not the panel lights (thin, no full face to disguise) and not the cube light (no front, it would vanish completely).
- **Host:** `FloodlightTileEntity` implements OMLib's `ICamoSupport`: camouflage enabled on those lights, no light sliders (`supportsLight` false), anyone who may open the light's GUI may set the disguise (`canSetCamo` true; the lights have no owner); `getCamoState` is the disguise or the light's own default state, and `setCamoState` with the light's own block removes the disguise.
- **Valid disguises:** OMLib's `util.camo.CamoRules` (a full block, drawn from a normal model, without a tile entity; `isFullBlock` is the default state's `isOpaqueCube`, so see-through blocks such as glass, which would show holes into the world, are refused). An invalid disguise read from NBT is dropped.
- **Setting it:** the Camouflage button in the floodlight GUI (`FloodlightGui`, only if `FloodlightContainer.hasCamoButton`) sends OMLib's `MessageOpenCamoMenu`; the server opens OMLib's camouflage menu (`ContainerCamo` / `CamoGui`): clicking its slot with a block sets the disguise, with an empty cursor removes it, shift-clicking a block sets it too. The menu has no Back button (the floodlight GUI is not on OMLib's `GUIBackSystem` stack).
- **Data:** part of the settings (saved, sent in the update tag, kept on the dropped item) in OMLib's format (`camoBlockRegName`, `camoBlockMeta`, `BlockUtil`); the `camo` compound of 1.0.0-beta.6 and older is still read.
- **Rendering:** the client wraps the models of the lights that support camouflage (`client.camo.CamoModels`; Fabric `FabricCamoModel` through the Renderer API, NeoForge `NeoForgeCamoModel` through `ModelData`). With a disguise they emit the disguise's model (its own render layer, ambient occlusion and tint) and, of the light's own model, only the **lamp quads**: an extra front-face element in `models/block/floodlight.json` with `tintindex: 1` and the `#lamp` texture (`<light>_lamp[_on].png`, the ring and disc cut out of the front texture with the rest transparent). Without a disguise the overlay lies over the identical pixels of the front face, so the floodlights are in the cutout layer. Cut the lamp textures again if a front texture changes.
  OMLib's `CamoBakedModel` is not used: it draws the disguise only, without the lamp on top (it would need an overlay hook in OMLib).
- **Tint:** the lamp keeps the dye colour (`FloodlightTint`). Fabric bakes the disguise's tint into its quads; on NeoForge `FloodlightTint` forwards every tint index other than 1 to the disguise's colour.
- Jade, the pick-block item and the break particles still show the real floodlight (the disguise is cosmetic, unlike OMT's hidden turrets).

---

## 7. Change Workflow

1. Implement gameplay logic in `core/` (with unit tests) where possible; keep Minecraft-facing code thin.
2. Check whether the change belongs on the main branch too (§1 "Keeping the branches together").
3. Add assets, lang entries (all three `.lang` files) and recipe JSON by hand.
4. Add a `CHANGELOG.md` entry for user-visible changes.
5. Run `./gradlew build`; check in-game behaviour with `runClient` and say what was checked.

- Every variant of subblocks is an **individual registered block/item** with a lowercase snake_case ID, the same IDs as on the main branch. `docs/id-mapping.md` maps the 1.7.10 IDs to them.

### Designing APIs

The user wants close control over every API (public interfaces for other mods, but also new cross-cutting interfaces inside the mod).
Design them step by step, together with the user:

1. **Overall shape first:** present a few ideas (e.g. 2–3 approaches with their trade-offs and a recommendation) and wait for the user's choice before writing code.
2. **Then each level in turn:** repeat this for each package, then each class or interface, then each public method or callback signature.
   Propose, wait for the decision, then implement that piece.
3. Do not implement a whole API in one go, and do not pick a signature on your own because it seems obvious.

### Code shared with OpenModularTurrets

The camo model and `CompatTileEntity` follow OpenModularTurrets (`../OMT1.20.1+`) on the main branch; on 1.12.2 they are rewritten for the 1.12 APIs, and the rest of the camouflage is OMLib's (§6.11).
The camo differences to OpenModularTurrets stay limited to the lamp overlay.
When you change one of these, tell the user whether the same change belongs in the other mod or the main branch (do not edit other repositories unasked).

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

### Forge 1.12.2 (branch `1.12`)

- [x] Build: RetroFuturaGradle 2.0, Java 8 toolchain, MCP `stable_39` (`snapshot_20171212` for a few hours on 2026-10-24, while the other mods still used it), reobfuscated jar, Spotless, `verifyCoreIsolation`, changelog tasks, publishing (dry run: `publish_enabled=false`)
- [x] `core/` and `config/` from the main branch, converted to Java 8; all unit tests pass (77; the energy tests are OMLib's)
- [x] Depends on OMLib, built in the OMTTeam 1.12 workspace and run there with OMLib, OMT and OMPD (2026-10-24); `core.energy` and the wrench check use OMLib
- [x] OMLib's camouflage (`ICamoSupport`, camouflage menu, `CamoRules`) instead of `camo/` and the disguise slot (2026-10-25); `client/camo/` stays for the lamp overlay
- [ ] Further OMLib reuse: OMLib's `core.machine.MachineMode` instead of the invert toggle is postponed (decided by the user: not how the lights work on 1.20.1+)
- [x] Blocks, block entities, registration, creative tab, interaction, drops with settings
- [x] Phantom and UV light blocks (persistent, `LIT` toggle, random-tick cleanup, neighbour re-cast), light debug tool
- [x] Forge Energy (insert only, cluster sharing of panel and cube lights), batteries, fuel, insert-only item capability
- [x] GUIs (`IGuiHandler`, container sync, Camouflage button), config sync on login (OMLib's `ConfigSync` since 2026-10-25)
- [x] Disguise model (`CamoModel`, extended state), dye tint, "Configured" tooltip
- [x] Assets adapted (blockstates, square panel textures, `.lang` files), recipes as JSON with the `block_enabled` condition, smelting in code
- [x] Dev client starts with no model, texture or recipe errors (checked 2026-10-24)
- [ ] Checked by hand in a world (`runClient`): placing, beams on/off, modes, GUIs, energy from a mod cable, carbon fuel, UV damage, grow light, disguise rendering, tint, debug tool, dropped items, joining a dedicated server (config sync)
- [x] Block info overlay with Hwyla instead of Jade (invert, mode; config switch); not checked in a client with Hwyla yet
- [ ] The One Probe overlay: ask the user first
