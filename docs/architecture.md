# Architecture

A guide to the code of Open Modular Lighting (OML) for people working on it: how it is layered, what lives where, how the main flows run, and how to add the common things.
It describes the code as it is; where it falls short of its own rules, the last section says so.

**OMLib.** Since 2026-10-27 the code that both Open Modular mods used to share is in the library OMLib (git submodule `omlib`, bundled jar-in-jar): the platform layer, the compat helpers, energy and item transfer, the config machinery and the camouflage menu.
This document marks them as OMLib's where they come up (package `omtteam.omlib`, with the same names and sub-packages as before the move); the README of OMLib lists them.
OMLib's own entrypoints also do the wiring every mod needs, see "Startup".

Building, running and the Stonecutter commands are in the README and in `CLAUDE.md` (section 4).
The gameplay rules (beam shapes, state machine, power) are specified in `CLAUDE.md` (sections 6.3 to 6.10); this document shows where they live in the code.

## The idea in one paragraph

A **light** (floodlight, grow light, fluorescent light, cube light, UV floodlight) is a block with a block entity that owns the state: on or off, inverted, power, mode and the cached shape of its beam.
The beam itself is made of **light blocks** (invisible phantom lights, or UV lights) that the light places in the air along the beam and then leaves in place.
Turning a light on or off only switches the `LIT` blockstate of those light blocks; they are placed and removed only when the beam changes shape or the light is broken.
All game rules (beam shapes, which positions a ray reaches, which sources keep a light block alive, the on/off state machine, energy and fuel formulas) are plain Java in `core`, without any Minecraft class, so they are unit-tested in milliseconds.
The core is not in this repository: it comes from the shared repository [OpenModularCore](https://github.com/OpenModularTurretsTeam/OpenModularCore) (module `oml-core`, the same on every Minecraft version, version `core_version` in `gradle.properties`), which also holds its unit tests, and `mod-common` bundles its classes into the jar.
The Minecraft-facing code around it translates between the game and the core and is kept as thin as possible.

## Layers

```mermaid
flowchart TB
    client["client<br/>screens, tint, debug renderer, tooltips<br/><i>client only</i>"]
    game["game layer (main)<br/>blocks, block entities, items, menus, network,<br/>registry, config, world, energy"]
    compat["compat (OMLib)<br/>porting helpers"]
    platform["platform (OMLib)<br/>service interfaces (Platform, Network, Menus, Energy, Fuel, LoaderInfo),<br/>implemented per loader in OMLib's platform/&lt;loader&gt;"]
    core["core<br/>game rules, plain Java"]
    client --> game
    client --> compat
    client --> platform
    game --> compat
    game --> platform
    game --> core
    compat --> platform
```

An arrow means "may use".
The game layer and `compat` reach the loader only through the service interfaces in OMLib's `platform`; OMLib's `platform/<loader>` implements them, and `Platform` creates them on first use, so no mod has to install them.
The one exception to the arrows are OML's own entrypoints in `platform/<loader>`: they start the mod and wire the game layer to the loader (registration steps, capabilities, the recipe condition), so they reference game classes such as `ModBlockEntities`.
The other directions are forbidden:

- **core** uses only Java and itself: no Minecraft, loader or other OML packages.
  OpenModularCore's build checks this; here, the `verifyNoCoreSources` task (part of `check`) fails the build if the sources have a file in the core package, so a core change is never made as a copy in this repository.
- **main** code never uses **client** code: the client classes do not exist on a dedicated server.
  The split source sets make it a compile error on both nodes (the `client` source set sees `main`, not the other way round), and `ArchitectureTest.mainCodeNeverDependsOnClientCode` checks it on the compiled classes.

### How core talks to the game

The core defines small interfaces for what it needs from the world; the game layer implements them.
This is the only place where the two meet, and the reason the core can be tested with fakes (`TestWorld` in OpenModularCore's `oml-core` tests).

| Core interface (`core`) | Implemented by (game layer) | What it gives the core |
| --- | --- | --- |
| `light.LightWorld` | `world.LevelLightWorld` | classifies a block position as a `Cell`: air, phantom light, UV light, opaque, transparent or unloaded |
| `state.PowerSource` | `energy.ElectricPower`, `energy.FuelPower` (through `energy.LightPower`) | whether the light can run this tick, and paying for it |
| `cluster.ClusterNode` | `blockentity.PanelLightBlockEntity` | who a light is connected to, for the shared energy of touching lights |
| `energy.EnergyAccess` (OMLib's `omtteam.omlib.core.energy`) | the lights' `getEnergyAccess(side)` (the buffer of `energy.ElectricPower`, or `energy.ClusterEnergy.View` for panel lights, an OMLib `core.energy.EnergyGroup`) | what other blocks may do with a light's energy (insert; extract is never allowed) |

Positions in core are the `Pos` record and directions the `Facing` enum (same ordinal order as `Direction`); `world.Positions` converts both ways.
The block entities own one core object each and forward to it: `FloodlightBlockEntity` → `LightController`, `LightBlockEntity` → `LightSources`, `PanelLightBlockEntity` → `Cluster`.
A light's beam is the game-layer class `blockentity.LightBeam`, which calls `BeamCaster` and `ShapeDiff` and keeps the cached shape.

## Where things live

Source sets (Loom split source sets on Fabric, the same split built by hand on Forge, plus the two test sets):

| Source set | Contents | Shipped |
| --- | --- | --- |
| `src/main` | everything that runs on both sides, including `fabric.mod.json` and `mods.toml` (both templated) | yes |
| `src/client` | screens, tint, debug renderer, the client half of the Jade plugin (the disguise models and the client networking are OMLib's) | yes, client only |
| `src/test` | JUnit tests of the config, and the architecture rules (`ArchitectureTest`); the core's tests are in OpenModularCore | no |
| `src/gametest` | in-world GameTests as a separate test mod (own mod id), shared by both loaders | no |

Packages below `omtteam.openmodularlighting` (main; the `core` packages come from OpenModularCore); what they use from OMLib is named after the table:

| Package | Responsibility |
| --- | --- |
| `OML` | `MOD_ID`, `OML.id(path)`, `init()` and `registrationSteps()`, used by every loader entrypoint |
| `core.math` | `Pos`, `Facing`, `Rotation` (rotates the shapes to the facing) |
| `core.beam` | `BeamShape` (the ray patterns), `BeamMode` and `GrowMode`, `Spacing` (the thinning of light blocks along a beam), `Ray` |
| `core.light` | `BeamCaster` (walks the rays), `LightWorld` and `Cell`, `LightType`, `LightSources` (all and active sources of a light block), `ShapeDiff` |
| `core.state` | `LightController` (the on/off state machine), `PowerSource`, `FuelTimer`, `RecheckTimer` |
| `core.cluster` | `Cluster` and `ClusterNode`: connected groups with join, leave and split, generic enough to reuse for multiblocks |
| `core.formula` | `CarbonFuel`, `GrowChance`, `EnergyCost` |
| `block` | `FloodlightBlock` (base of every light: `FACING`, `LIT`, drops) with its subclasses; `FloodlightInteraction` (right-click); `LightBlock` (base of the phantom and UV light blocks); `PanelShapes` |
| `blockentity` | `FloodlightBlockEntity` (base of every light) and subclasses, `LightBeam` (its cached beam and the re-cast); `LightBlockEntity` (sources of a light block) with `PhantomLightBlockEntity` and `UvLightBlockEntity`; `LightSourceBlockEntity` |
| `world` | `LightBlocks` (place, switch and remove light blocks), `LevelLightWorld`, `Positions` |
| `energy` | `LightPower` with `ElectricPower` and `FuelPower`, `ClusterEnergy`, `Batteries` |
| `menu` | the container menus: `FloodlightMenu` (the light's slot and the status values; on a `CamoBlock` its screen shows a "Camouflage" button) with `ElectricFloodlightMenu` and `CarbonFloodlightMenu` |
| `camo` | `CamoRules`: which blocks are valid disguises of the floodlights (`CamoRules.RULES`, the `DisguiseRules` OMLib's camouflage menu asks for) |
| `registry` | `ModBlocks`, `ModItems` (the crafting items and the light debug tool are plain `Item`s; what the tool does is in `FloodlightInteraction` and `client.render.LightBlockDebugRenderer`), `ModBlockEntities` (with `transferTypes()` for the loader lookups), `ModMenus`, `ModCreativeTabs`, `ModDamageTypes` |
| `network` | `OMLPackets` (the mod's own packet types; none at the moment, the config sync and camouflage packets are OMLib's) |
| `config` | the JSON config: `ConfigData` (fields), `ConfigMigrationSteps`, `OMLConfig` (an OMLib `ConfigManager` of `ConfigData`: load, save, sync) |
| `compat` | `BlockEnabledCondition` (the logic of the recipe condition), and the server half of the optional Jade plugin (`compat/jade`; the tooltip is in `client.compat.jade`, because Jade's tooltip classes are client classes) |
| `platform` | `platform.fabric` and `platform.forge`: the entrypoints and the loader's registration of the recipe condition; each only compiles for its loader |
| `datagen` | recipes, loot tables, tags, damage types (Fabric only, generated in the server environment; the recipes are in `OMLRecipeProvider.Recipes`) |

From OMLib (`omtteam.omlib`) the mod builds on the platform services (`platform.Platform` holding `PlatformServices`: `LoaderInfo`, `Fuel`, `Network`, `Menus`, `Energy`, with their loader classes), the version helpers (`compat`: `VersionCompat`, `CompatBlockEntity`, `CompatEntityBlock`, `BlockCodec`, `MenuCompat`, `CommonTags`, `nbt.NbtCompat`, `block.BlockCompat`, `network.NetworkCompat`; `client.compat`: `ClientCompat`, `ClientNetworkCompat`), `core.energy` (`EnergyStore`, `EnergyAccess`, `EnergyGroup`), `inventory.ItemAccess` and `blockentity.TransferHost`, `block.InteractiveBlock`, `registry.RegistrationStep`, the config machinery (`@Comment`, `@Range`, `ConfigWriter`, `ConfigRanges`, `ConfigValidator`, `ConfigMigration`, `ConfigManager`), `menu.LongContainerData`, and the camouflage (`camo.CamoBlock`, `CamoHost`, `CamoData`, `DisguiseRules`, `menu.CamoMenu` with its screen and packets, and the disguise models in `client.camo` and the loader packages).

The block and block entity hierarchies mirror each other:

```mermaid
flowchart TB
    subgraph blocks
        FB[FloodlightBlock] --> EFB[ElectricFloodlightBlock]
        FB --> CFB[CarbonFloodlightBlock]
        FB --> UFB[UvFloodlightBlock]
        FB --> CLB[CubeLightBlock]
        FB --> PLB[PanelLightBlock]
        PLB --> FLB[FluorescentLightBlock]
        PLB --> GLB[GrowLightBlock]
        LB[LightBlock] --> PHB[PhantomLightBlock]
        LB --> UVB[UvLightBlock]
    end
    subgraph block entities
        FBE[FloodlightBlockEntity] --> BFBE[BeamFloodlightBlockEntity]
        BFBE --> EFBE[ElectricFloodlightBlockEntity]
        BFBE --> CFBE[CarbonFloodlightBlockEntity]
        FBE --> UFBE[UvFloodlightBlockEntity]
        FBE --> PLBE[PanelLightBlockEntity]
        PLBE --> FLBE[FluorescentLightBlockEntity]
        PLBE --> GLBE[GrowLightBlockEntity]
        PLBE --> CLBE[CubeLightBlockEntity]
        LBE[LightBlockEntity] --> PHBE[PhantomLightBlockEntity]
        LBE --> UVBE[UvLightBlockEntity]
    end
```

The upper tree in each half is the **lights** (they have a `FACING`, a GUI and a tick); the lower one is the **light blocks** they place (no GUI, no tick except the UV damage).

## How the main flows run

### Startup

The loader entrypoint (`OMLFabric`, `OMLForge`) first calls `OML.init()`; it installs no platform services, because OMLib creates them on first use (`Platform`), whichever mod starts first.
The order matters:

1. `OMLPackets.init()`: the mod's own packet types (none at the moment).
2. `OMLConfig.init()`: blocks and recipes read the config, so it comes before any registration.

Then the registrations of `OML.registrationSteps()`: blocks, items, block entities, menus, creative tab.
Fabric runs them in this order; Forge runs each in the `RegisterEvent` of its registry and fails at common setup if one never ran.
A new registry class gets a `RegistrationStep` in that list, not a call in an entrypoint.

Last, the entrypoint wires its loader to the shared code: the item and energy access of the lights (OMLib's `FabricBlockLookups.register(ModBlockEntities.transferTypes())` on Fabric; on Forge OMLib attaches them, see below) and the recipe condition.
OMLib's own entrypoints do the rest of the wiring every mod needs: block use (`InteractiveBlock.handleUse`), the config sync on join, the disconnect hook, the camouflage menu type and packets, and on Forge the item and energy capabilities (`ForgeBlockCapabilities`, attached in `AttachCapabilitiesEvent` to every `TransferHost`).

On the client, the loader client entrypoint (`OMLFabricClient`, `OMLForgeClient`; Forge 1.20.1 has no client-only `@Mod` classes, so `OMLForge` constructs it by name) calls `OMLClient.init()` (empty at the moment: the mod has no client packet handlers).
It then registers what `OMLClient` lists with its loader: screens, the dye tint, tooltips and the debug renderer.
OMLib's client entrypoints wrap the models of the `CamoBlock`s in the loader's disguise model (`FabricCamoModel`, `ForgeCamoModel`; both wrap the baked model).
Render layers are the one thing kept twice: `BlockRenderLayerMap` on Fabric, `"render_type"` in the block model JSON on Forge.

### A light's tick (server)

`FloodlightBlockEntity.serverTick`:

1. `beforePower`: panel lights keep their cluster up to date and pull energy from it.
2. `power().prepare`: charge from the battery slot, or burn the next fuel item.
3. A re-cast is flagged if the shape key (mode, ranges), the position or the facing differ from those the cached shape was cast with (`LightBeam.needsRecast`), if a neighbour of the light is no longer what it was at the last cast (`FloodlightBlock.onNeighborChanged` → `LightBeam.neighboursChanged`, between ticks), or if the periodic `RecheckTimer` fires (every 10 to 30 s).
4. `LightController.tick(redstone, power, timeout)` returns a `TickResult`: re-cast or not, switch on, off or neither, and whether the light ran (and paid) this tick.
5. A re-cast runs first (`LightBeam.recast`), then the switch (`LightBeam.switchLights`), then the light's own `LIT` is set.
6. `afterTick` is the hook of subclasses (the grow light's growth ticks).

The state machine itself, with the timeout against flicker, is `core.state.LightController`; it knows nothing about blocks.

### Casting a beam

`LightBeam.recast`, called from the tick and at once from `FloodlightBlockEntity.shapeChanged` (mode change); the beam reads the shape, facing, light type and on/off state from its light:

1. `BeamCaster.cast(shape, origin, facing, lightType, new LevelLightWorld(level))` walks every ray of the shape and returns the positions that should hold a light block.
   A ray stops at an opaque or unloaded cell, passes through transparent ones without a position, and the cone rules (the inner ray is skipped if its outer ray was blocked early) live here.
2. `ShapeDiff.between(cached, cast)` gives the positions that dropped out.
   For each, `LightBlocks.leave` removes this light as a source; a light block with no sources left removes itself.
3. Every cast position is joined through `LightBlocks.join`: it places a light block in air (or where `LightBlock.canReplace` allows), then adds this light as a source.
   Joining positions that are already covered is a no-op, so this also repairs light blocks lost while a chunk was unloaded.
4. The positions that joined become the cached shape (a `long[]` saved with the block entity, so a reload does not need a cast).

`BeamCasterTest` checks `BeamCaster` against reference loops in the test sources (`BeamTest`, filtered by the same thinning) in random worlds.

### Light blocks and their sources

A `LightBlockEntity` stores two lists: `sources` (every light that reaches this block) and `activeSources` (those that are on), in `core.light.LightSources`.
Every change returns an `Effect` (`NONE`, `TURN_ON`, `TURN_OFF`, `REMOVE`) which `LightBlocks.apply` carries out on the block: `LIT` is `!activeSources.isEmpty()`, and the block goes away when `sources` is empty.
So several floodlights can share one block, and it stays lit as long as one of them is on.

`LightBlocks` always uses `LightBlocks.FLAGS` (clients are told, no neighbour or shape updates).
Without that, every toggle would update the next light block and start re-cast chains.

Light blocks do not tick (there can be thousands).
Instead they react to the world:

- `neighborChanged` and `LightBlockEntity.onBlockRemoved` (the removal hook of `CompatBlockEntity`) call `requestRecastOfSources()`, which only flags the sources; the light re-casts on its next tick, never inside the neighbour callback.
- `randomTick` calls `LightBlockEntity.refresh()`: sources whose block entity is gone, or no longer covers this position, are dropped, `LIT` is corrected and the block removes itself if nothing is left.
  Sources in unloaded chunks are left alone (`SourceStatus.UNKNOWN`).

The UV light block is the exception that ticks: while lit it hurts every `LivingEntity` inside it once a second (`UvLightBlockEntity.serverTick`).

### Power and fuel

A light's `LightPower` is chosen by its block entity: `ElectricPower` (an `EnergyStore` of 50 000 E plus a battery slot) or `FuelPower` (a `FuelTimer` plus a fuel slot).
`prepare` runs before the logic each tick, `PowerSource.canRun()` / `run()` is what `LightController` calls.
The numbers are in `core.formula` (`EnergyCost`, `CarbonFuel`, `GrowChance`).

Outside access goes through the loader; the adapters are all OMLib's:

- Energy: `core.energy.EnergyAccess`, exposed as Team Reborn Energy on Fabric (`FabricEnergyStorages`) and as an FE capability on Forge (`ForgeEnergyHandler`).
  Only inserting is possible; there is no extraction.
- Items: `inventory.ItemAccess.insertOnly`, exposed as `ItemStorage.SIDED` on Fabric (`FabricItemStorages`) and as `IItemHandler` on Forge (`ForgeItemHandler`).
- Battery slot and fuel values: `platform.Energy` and `platform.Fuel`.

### Shared energy of panel lights

Fluorescent, grow and cube lights (`PanelLightBlockEntity`) that touch and face the same way form a `core.cluster.Cluster` (cube lights: any face, cube lights only).
The cluster is in memory only: a light joins on its first tick, leaves in `setRemoved`, and a possible split is found in `refresh()` once per tick, so breaking a wall of lights costs one flood fill.
Each light keeps its own buffer; `ClusterEnergy` shows the whole cluster to the outside (inserting fills the emptiest light first) and lets a light that cannot pay take from the fullest others.

### A click on a light

Right-click handling does not use vanilla's `use` methods, because they differ between the Minecraft versions.
The loader's block-use event (registered once by OMLib) calls `InteractiveBlock.handleUse`, which calls `FloodlightBlock.onUse`; that hands it to `FloodlightInteraction`, one method per action:

- sneak with an empty hand: toggle invert; stick or wrench: `configure` (cycle the mode, turn the small fluorescent light); wrench plus sneak: dismantle; dye or wool: set the colour;
- anything else opens the menu: `FloodlightBlock.openMenu` → `MenuCompat.open` → `platform.Menus` → `ElectricFloodlightMenu` or `CarbonFloodlightMenu`.
  On a `CamoBlock`, the floodlight screen shows a "Camouflage" button: it sends OMLib's `OMLibPackets.OPEN_CAMO_MENU`, and the server opens OMLib's `CamoMenu` for the light (a `CamoHost`, rules `CamoRules.RULES`).
  Its phantom slot calls `FloodlightBlockEntity.setCamo` on the server; "Back" (`OMLibPackets.CAMO_BACK`) reopens the floodlight GUI through `FloodlightBlockEntity.openParentMenu`.

Changes that affect the beam (`setMode`) end in `shapeChanged()`, which re-casts at once.
The server is the only place that changes state; the screens only show it (the menus sync energy and burn time with OMLib's `LongContainerData`) and the colour and the disguise reach the client in the block entity's update tag.
The disguise is drawn by OMLib's model wrapper of the loader: the block entity hands it over as render data (`CompatBlockEntity.renderData`), and the wrapper emits the disguise's quads plus the lamp quads of the light's own model (tint index 1, a cutout overlay on the front face).

### Config

`OMLConfig.get()` is the only way to read a value, and the value is never cached by the caller: on a client connected to a server it returns the server's config (OMLib's `ConfigManager` receives it when the player joins).
See "A config value" below for adding one.

## How to add …

### A beam shape or mode

Add the shape to `core.beam.BeamShape` / `BeamMode` in OpenModularCore, with a unit test in `core/beam` and, if the new shape belongs in the comparison, an entry in `BeamTest` that `BeamCasterTest` compares against; then release the core and raise `core_version` (the root `CLAUDE.md` of the workspaces, "Shared core logic").
The block entity returns it from `currentShape()` and puts everything it depends on into `shapeKey()`, so a change triggers a re-cast.

### A light

1. A block extending `FloodlightBlock` (or `PanelLightBlock`) and a block entity extending `FloodlightBlockEntity` (or `PanelLightBlockEntity`); the block entity chooses `power()`, `currentShape()` and `shapeKey()`.
2. Register block, item and block entity in `ModBlocks`, `ModItems`, `ModBlockEntities`; add it to the lookups of every loader if it has energy or item access (`FabricBlockLookups` uses `ModBlockEntities.transferTypes()`; on Forge, OMLib attaches its capabilities to every `TransferHost`, which `FloodlightBlockEntity` implements).
3. Blockstate, model, item model (`assets/openmodularlighting/models/item/<id>.json`), lang entries (`en_us`, `de_de`, `ru_ru`), and the recipe, loot table and tags in `datagen`.
4. A GameTest in `src/gametest` (and its entry in `src/gametest/resources/fabric.mod.json`), and an entry in `docs/id-mapping.md` if it replaces a block of the old mod.

### A config value

Add a field to the right section of `config.ConfigData` with `@Comment` and `@Range`; the file, its comments and the range check follow from them.
Only `int`, `double`, `boolean` and `List<String>` are supported.

### A packet

Define the payload (a record with `write` and `read`), list it in `network.OMLPackets` through OMLib's `NetworkCompat`, and handle it on the other side (client handlers are registered through `ClientNetworkCompat`, from `OMLClient.init()`).
A packet from the client must be validated on the server before it changes anything.

### A loader service

The loader services are OMLib's: if shared code needs something only a loader can do, the interface goes into OMLib's `platform` (design it first, see `CLAUDE.md` section 7), is added to `PlatformServices` (`Platform` then fails to compile until every loader supplies one) and is implemented in OMLib's `platform/fabric` and `platform/forge`; the change is made in the OMLib repository, not in `omlib/` (`CLAUDE.md` section 4, "OMLib").
Whole loader-specific classes of this mod (entrypoints, event wiring) go into its own `platform/<loader>` packages; a small difference inside a shared class uses `//? if fabric {` / `//? if forge {`, but only in `compat/` and `platform/`.

### Porting a change between the version branches

Every Minecraft version has its own branch (`1.20.1`, `1.21.1`, `1.21.11`), and the game layer is ported between them by hand; the core comes from OpenModularCore and is the same on all of them.
The helpers in OMLib's `compat` (for example `VersionCompat`, `CompatBlockEntity`, `NbtCompat`) keep the call sites the same on every branch, so a gameplay change usually ports without edits to the call sites.
Block entities extend OMLib's `compat.CompatBlockEntity` (override `readData` / `writeData`).
The table of the helpers is in `CLAUDE.md`, section 5.

### Recipes, loot tables, tags

Never handwrite files under `data/`: add them to the providers in `datagen` and run `./gradlew :1.20.1-fabric:runDatagen`.
The output is committed (`versions/1.20.1-fabric/src/main/generated`).
The Forge build converts the Fabric output at build time (`convertFabricData` in `omlib/build-logic`), so nothing loader-specific is committed.

## Rules the build checks

`./gradlew chiseledBuild` (or `check` for one node) fails when one of these is broken:

| Rule | Checked by |
| --- | --- |
| the core package has no sources here (it comes from OpenModularCore) | the `verifyNoCoreSources` task |
| main code never uses client code | `ArchitectureTest.mainCodeNeverDependsOnClientCode` |
| no new package cycles, and the known ones only shrink | `ArchitectureTest.noNewPackageCycles` (list `KNOWN_CYCLES`) |
| every item has a recipe (unless `recipe-exempt.txt` names it with a reason), and every recipe makes an item of the mod | the `checkRecipes` task on both nodes (`omlib/build-logic/src/main/kotlin/RecipeCoverage.kt`), on the node's processed resources |
| the config behaves as specified | unit tests in `src/test` (they run on the Forge node too) |
| lights, light blocks, energy, items, recipes and Jade work in a real world | GameTests in `src/gametest`: Fabric `runGameTest`, Forge `runGameTestServer` |
| formatting (tabs, trailing whitespace, final newline) | `spotlessCheck` |
| the changelog has a section for the version being published | `publishMods` |

`ArchitectureTest` (ArchUnit, `src/test/.../architecture`) reads the compiled main and client classes, so it runs on the Fabric node only.
The client (screens, models, tint, tooltips) is not covered by any test; check it by hand with `runClient`.

`KNOWN_CYCLES` is a ratchet: a new cycle, or a package joining one, fails the test, and so does a cycle that shrank or disappeared until the list is updated.
When the test fails it prints the cycle, the class dependencies inside it (fewest first, the cheapest to cut) and the updated list.
Never add to it without asking the user.

## Measuring code quality

These tools measure the code but do not fail the build:

| Tool | Command | Output |
| --- | --- | --- |
| PMD: complexity, size, nesting, parameters, coupling (rules in `gradle/pmd/ruleset.xml`, the same as OpenModularTurrets') | `./gradlew :1.20.1-fabric:pmdMain :1.20.1-fabric:pmdClient` (also part of `check`) | `versions/1.20.1-fabric/build/reports/pmd/main.html`, `client.html` (and `.xml`) |
| Test coverage of the mod's own classes, without the core (JaCoCo, from the unit tests and the GameTests) | part of `check`, which prints the line coverage per top-level package | `versions/1.20.1-fabric/build/reports/jacoco/test/html/index.html` |
| Mutation testing of the config classes `ConfigData` and `ConfigMigrationSteps` (PIT, with the unit tests; the list is `pitestTargets` in `build.fabric.gradle.kts`) | `./gradlew :1.20.1-fabric:pitest` (on demand, not part of `check`) | `versions/1.20.1-fabric/build/reports/pitest/index.html` (and `mutations.xml`) |

They run on the active Fabric node only (both nodes share the source); code in `platform/forge` is covered by its GameTests, not by coverage or mutation testing.
The core's coverage and mutation testing run in OpenModularCore.
The tool versions are in `gradle.properties` (`pmd_version`, `jacoco_version`, `pitest_version`, `archunit_version`).

PMD reports only for now; it does not fail the build.
Once the limits are agreed, switching off `isIgnoreFailures` in `build.fabric.gradle.kts` makes new code above them fail.
A method is reported from a cyclomatic or cognitive complexity of 15, an NPath complexity of 200, 60 statements, 8 parameters or ifs nested three deep; a class from 500 statements or when it uses more than 20 other types.

Coverage says which lines the tests run; mutation testing says whether they check the results.
PIT plants small mistakes in the mutated classes (a flipped condition, `<` instead of `<=`, a removed call, a changed return value) and runs the unit tests against each one.
A mistake no test notices ("survived") marks behaviour the tests do not pin down, often an edge case: a boundary value, the `false` case of a getter, a method no test calls.
Not every survivor is a gap: where `>` and `>=` behave the same for every possible value (an "equivalent mutant"), the change cannot be noticed, and the code should not be bent to make it noticeable.
A mutant that makes the tests loop forever counts as detected ("timed out").

When a tool reports something:

- **A PIT survivor:** add the missing assertion to the unit test of that class; if it is equivalent, leave it and say why.
  The `pitest` task fails when the results get worse than the thresholds in `gradle.properties` (`pitest_mutation_threshold`, `pitest_coverage_threshold`, `pitest_max_surviving`, the state of 2026-11-18); raise `pitest_max_surviving` only for an equivalent survivor listed in the PIT row below.
- **A PMD finding:** propose a fix (usually extracting a method or a small class) and let the user decide, or accept it with a reason under "Known weak spots".
  Coupling counts the types of fields, parameters, local variables and return types, so overriding vanilla methods alone can push a block class above 20.
- **A new package cycle:** cut the cheapest dependency the test prints, rather than extending `KNOWN_CYCLES`.

State on 2026-10-21:

| Measure | Value |
| --- | --- |
| Line coverage of the mod's own classes, without the core (2026-11-18, unit tests and GameTests) | 75 % (908 of 1205 lines; `datagen`, which runs only in `runDatagen`, has 0 %) |
| PIT (2026-11-18, the config classes; the core's mutation testing is in OpenModularCore since then) | 2 of 2 mutations detected, 19 of 19 lines covered |
| PMD | 2 findings, both accepted (see below) |
| Package cycles | 1 (see below) |

## Known weak spots

Things that make the code harder to work with than they should be.
They are known and measured, not hidden:

- **One large package cycle.** The root package (`OML`), `block`, `blockentity`, `energy`, `menu`, `registry` and `world` all depend on each other (registry ↔ blocks ↔ block entities, as in most Minecraft mods); `noNewPackageCycles` keeps it from growing.
- **2 PMD findings**, both accepted: the coupling of `FloodlightBlock` (23; the types of the vanilla block methods it overrides) and of `FloodlightBlockEntity` (23).
- **`FloodlightBlockEntity` does a lot** (about 400 lines): ticking, settings, the colour, the disguise, the slot and saving.
  The beam (cast, diff and shape cache) is already in `LightBeam`; the slot would be the next candidate to move out.
- **Phantom lights beside skipped positions.** Because of the thinning (`core.beam.Spacing`), a block change next to a position without a light block may reach no light block at all, so the beam notices it only at the periodic re-check (10 to 30 s).
  Right in front of the light this cannot happen: the floodlight watches its own neighbours.
- **The client is untested** except by hand, since Fabric's client GameTests need Minecraft 1.21.4 or newer.
- **Render layers are kept twice** (Fabric in code, Forge in the model JSON) and must be changed together.
