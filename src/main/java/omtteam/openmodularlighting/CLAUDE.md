# CLAUDE.md — Open Modular Lighting, game layer (`src/main/java/omtteam/openmodularlighting`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.1 Package layout

Only what is in this repository; the platform layer, the compat helpers, `core.energy`, item transfer, the config machinery, the camouflage menu and models and the client platform are OMLib's (`omtteam.omlib`, §4 "OMLib").

```text
omtteam.openmodularlighting
│
├── OML                            shared start-up: init() (packets, config) and registrationSteps() (OMLib's RegistrationStep:
│                                  registry + call), both used by the loader entrypoints; constants (MOD_ID, LOGGER), id(path)
│
├── core/                          ── pure Java, NO Minecraft imports; from OpenModularCore (oml-core), not in this repo ──
│   ├── math/                      Pos (int x,y,z record), Facing (6 directions), Rotation (port of MathUtil.rotate)
│   ├── beam/                      BeamShape (straight, narrow cone, wide cone, fluorescent, cube, grow, UV), Ray, Spacing
│   │                              (thinning), BeamMode (floodlight modes), GrowMode (lighting / black light)
│   ├── light/                     LightWorld (interface) + Cell, LightType (phantom / UV rules), BeamCaster,
│   │                              LightSources (all + active sources of a phantom/UV block), ShapeDiff
│   ├── state/                     LightController (active/inverted/timeout/re-cast state machine), RecheckTimer,
│   │                              PowerSource (interface: energy buffer or fuel timer), FuelTimer
│   ├── cluster/                   Cluster + ClusterNode (generic connected groups: join / leave / refresh-split, max 256;
│   │                              Minecraft-free, meant to be reused for multiblocks)
│   └── formula/                   CarbonFuel (burn time), GrowChance (tick interval), EnergyCost
│
├── block/                         FloodlightBlock (base of every light: FACING + LIT, drops; configure() for stick/wrench;
│                                  right-click in FloodlightInteraction) → ElectricFloodlightBlock, CarbonFloodlightBlock,
│                                  UvFloodlightBlock, CubeLightBlock, PanelLightBlock (thin, no collision, PanelShapes) →
│                                  FluorescentLightBlock (small/square), GrowLightBlock;
│                                  LightBlock (base of the light blocks: LIT, random-tick cleanup, re-cast requests,
│                                  canReplace) → PhantomLightBlock, UvLightBlock
├── blockentity/                   FloodlightBlockEntity (base: invert, colour, disguise, slot, power; a CamoHost; LightBeam:
│                                  cached beam, re-cast) → BeamFloodlightBE (BeamMode) → ElectricFloodlightBE,
│                                  CarbonFloodlightBE; UvFloodlightBE; PanelLightBE (energy cluster) → FluorescentLightBE,
│                                  GrowLightBE (GrowMode, growth), CubeLightBE; LightSourceBlockEntity (interface of all
│                                  lights); LightBlockEntity (sources, save/load) → PhantomLightBE, UvLightBE (damage)
├── menu/                          FloodlightMenu (status, Camouflage button on CamoBlocks) → ElectricFloodlightMenu,
│                                  CarbonFloodlightMenu
├── camo/                          CamoRules (valid disguises: full opaque blocks, §6.11)
├── registry/                      ModBlocks, ModItems (crafting items and the light debug tool are plain Items), ModBlockEntities
│                                  (+ transferTypes() for the loader lookups), ModMenus, ModCreativeTabs, ModDamageTypes
├── energy/                        LightPower (what a light runs on, with its slot) → ElectricPower (EnergyStore + battery
│                                  slot), FuelPower (FuelTimer + fuel slot); ClusterEnergy (pull + insert-only View);
│                                  Batteries (battery-slot check and discharge via OMLib's Energy service)
├── world/                         LightBlocks (join / leave / setActive on light blocks, applies LightSources
│                                  effects), Positions (core ↔ Minecraft types), LevelLightWorld (core LightWorld
│                                  on top of Level)
├── network/                       OMLPackets (the mod's packet types; empty now, all packets are OMLib's)
├── config/                        OMLConfig (OMLib ConfigManager of ConfigData), ConfigData (the fields),
│                                  ConfigMigrationSteps
├── compat/                        BlockEnabledCondition (the recipe condition's logic); jade/ (optional Jade plugin, server
│                                  half: OMLJadePlugin, FloodlightJadeProvider; FloodlightTooltip has the content, no Jade)
├── platform/                      loader-specific code (§4 "Loaders"); each node compiles only its own loader's package.
│   ├── fabric/                    OMLFabric (ModInitializer: OML.init(), registration steps, FabricBlockLookups,
│   │                              FabricBlockEnabledCondition)
│   └── neoforge/                  OMLNeoForge (@Mod entrypoint: OML.init, steps per RegisterEvent, capabilities,
│                                  payloads), NeoForgeBlockEnabledCondition; FML constructs a mod's common @Mod class
│                                  before its client one (OMLNeoForgeClient)
└── datagen/                       recipe, loot table, block tag, damage type providers (Fabric only)
```

Dependency direction: `client → main → core`.
`core` depends on nothing.
`main` never references `client`.

## 5. Compat Helpers

The game layer is ported between the version branches by hand.
OMLib's compat helpers (below) keep the call sites the same on every branch, so use them instead of calling the Minecraft APIs that differ between versions directly.

### Existing helpers (use these instead of calling version-dependent Minecraft APIs directly)

| Difference | Helper |
| --- | --- |
| `Identifier` | `OML.id(path)`, `VersionCompat.id(ns, path)` |
| BlockEntity NBT + update tag | extend `compat.CompatBlockEntity`: override `readData` / `writeData` (+ optional `writeClientData`), call `syncToClient()`; data for the block model while meshing: override `renderData()` (Fabric render data, NeoForge `RENDER_DATA` model data), call `remesh()` on the client when it changed. It passes the whole compound through `ValueInput` / `ValueOutput`, so `readData` / `writeData` keep their `CompoundTag` and the saved format stays the same |
| Reading NBT (the `CompoundTag` getters return `Optional`) | `compat.nbt.NbtCompat.getInt` / `getLong` / `getBoolean` / `getString` (with a fallback), `getIntArray` / `getLongArray` / `getCompound` (empty if missing), `readBlockState`; never call the `CompoundTag` getters directly. It depends on Minecraft only, so packages outside the `compat` cycle (e.g. `camo`) may use it |
| Block state queries, interaction result | `compat.block.BlockCompat.isSolidRender` / `lightBlock` (cached per state), `sidedSuccess` (`InteractionResult.SUCCESS`); Minecraft only like `NbtCompat`. `neighborChanged` takes an `Orientation` (`LightBlock`) |
| Block broken or replaced | override `CompatBlockEntity.onBlockRemoved(Level)` (server only, before the block entity is removed); vanilla's `preRemoveSideEffects` calls it |
| ItemStack NBT, slot lists | `VersionCompat.saveStack` / `loadStack`, `saveItems` / `loadItems` |
| ItemStack extra data | `VersionCompat.hasCustomData` / `getCustomData` / `setCustomData` / `updateCustomData` |
| Block interaction | implement OMLib's `InteractiveBlock#onUse`; `InteractiveBlock.handleUse` is called by the loader's block-use event, which OMLib registers once (Fabric `UseBlockCallback`, NeoForge `PlayerInteractEvent.RightClickBlock`) |
| Registering blocks, items, block entity types | create blocks and items with `VersionCompat.blockProperties` / `itemProperties` / `blockItemProperties(properties, id)` (the id is needed before construction; block items keep the `block.` name), block entity types with `VersionCompat.blockEntityType(factory, blocks…)` (there is no `BlockEntityType.Builder`). Also in the test mod |
| Damaging entities, singleplayer host | `VersionCompat.hurt(entity, source, amount)` (`hurtServer` / `hurtOrSimulate`), `VersionCompat.isSingleplayerOwner(player)`; damage types through `registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)` |
| Creative tab builder | `VersionCompat.creativeTabBuilder()` (Fabric `FabricItemGroup`, NeoForge vanilla builder) |
| Networking | `compat.network.NetworkCompat.registerC2S` / `registerS2C` / `sendToPlayer`; client: `client.compat.ClientNetworkCompat.registerHandler` / `sendToServer`. Packets are plain records with `write` / `read`; the mod's own would be listed in `network.OMLPackets`, which is empty now (the config sync and camouflage packets are OMLib's, `omtteam.omlib.network.OMLibPackets`). The facades forward to `platform.Network` / `client.platform.ClientNetwork` (Fabric: `PayloadTypeRegistry`; NeoForge: recorded and applied in `RegisterPayloadHandlersEvent`, optional channels, handlers on the main thread, S2C handlers dispatched from main to the handler the client registered; a packet without client handler is logged and dropped) |
| Extended menus | `compat.MenuCompat.register` / `open` (passes the `BlockPos`; forwards to `platform.Menus`: Fabric extended screen handlers, NeoForge `IMenuTypeExtension` / `openMenu(provider, pos)`) |

## OMLib (the library, `omlib/`)

Requested by the user on 2026-10-27: the code that OpenModularLighting and OpenModularTurrets used to share lives in the library OMLib (`../OMLib`, a mod of its own with the id `omlib`, package `omtteam.omlib`), which both mods bundle jar-in-jar.
Its README and `CLAUDE.md` describe the layout and the rules (no mod knowledge in the library, public API only where a mod needs it).

- **Where things are:** the platform layer (`Platform`, `LoaderInfo`, `Fuel`, `Network`, `Menus`, `Energy` and their loader classes), the compat helpers (`VersionCompat`, `NbtCompat`, `BlockCompat`, `NetworkCompat`, `MenuCompat`, `CompatBlockEntity`, `CommonTags`, `ClientCompat`; they stay as the porting layer between the version branches, with the same call sites on every branch), `core.energy`, `inventory.ItemAccess`, `TransferHost`, the config machinery (`@Comment`, `@Range`, `ConfigWriter`, `ConfigRanges`, `ConfigValidator`, `ConfigMigration`), `RegistrationStep`, the block base `CompatEntityBlock` with `BlockCodec`, the single right-click handling `InteractiveBlock` (with interceptors), `ConfigManager` (with the config sync) and the camouflage menu with its screen, packets and models are in the `omtteam.omlib` packages.
  Wherever the rest of this file names those classes under `omtteam.openmodularlighting`, read `omtteam.omlib`; they kept their names and package layout.
- **Build:** the settings script runs `omlib/tools/bootstrap.sh` first (inside `pluginManagement`, because Loom resolves mod dependencies while it configures, so a task is too late; details and `OMLIB_SKIP_BUILD=1` in the workspace `CLAUDE.md`, "OMLib in the mods").
  `mod-common` adds the `omlib/build/maven` repository and exposes `project.extra["omlibCoordinates"]` / `["omlibVersion"]`; the loader scripts declare the dependency and bundle it, and the metadata templates declare it (`${omlib_version}`).
- **Runtime:** the library's services (`Platform`, `ClientPlatform`) are created on first use, so it does not matter which mod's entrypoint runs first; the library's own entrypoints register the camo menu type and packets.
  The library's entrypoints also do the wiring every mod needs: block use (`InteractiveBlock`), the config sync on join and the disconnect hook, and on the client the wrapping of the `CamoBlock` models.
  The mod passes its transfer block entity types to the registrars (`FabricBlockLookups.register(ModBlockEntities.transferTypes())`, `NeoForgeCapabilities.register(event, ModBlockEntities.transferTypes())` in its own event) and applies its own NeoForge packets in its own `RegisterPayloadHandlersEvent` (`NeoForgeNetwork.applyRegistrations(event, MOD_ID)`).
- **Camouflage:** `CamoBlock` (with `overlayTintIndex()`), `CamoHost` (with `disguiseRules()`) and `DisguiseRules` (this mod: `CamoRules.RULES`, opaque blocks only) are the hooks; the screen's extra buttons come from `CamoScreenExtras` (not used here).

## Core Game Logic Source

The game logic of the mod lives in the `core` package (`omtteam.openmodularlighting.core`) and is completely free of Minecraft imports.
It is not in this repository: since 2026-11-18 it comes from the shared repository OpenModularCore (`../../OpenModularCore`, module `oml-core`; the same core on every Minecraft version, see the root `CLAUDE.md`, "Shared core logic").
`gradle.properties` names `core_module=oml-core` and `core_version` (the same in every mod and workspace); `mod-common` (OMLib's build logic) gets the module from JitPack and extracts its classes into a classes folder of the main source set, so every jar bundles them and the dev runs, game tests and unit tests load them as part of the mod on every loader.
A core change is made in OpenModularCore (never as a copy here: `verifyNoCoreSources`, part of `check`, fails on any file in the core package), released there by the user, and then `core_version` is raised in every mod.
A `../../OpenModularCore` checkout replaces the JitPack artifact (workspace `CLAUDE.md`, "Builds"; `OMM_NO_LOCAL_CORE=1` as on CI).
It contains:

1. **Beam shapes.** Pure functions that return the ordered list of offsets for a light, given facing, mode and range (see §6.3).
   Each beam is a separate ray so that it can stop at the first opaque block.
2. **Rotation.** Shapes are defined along +X and rotated to the facing direction (port of `MathUtil.rotate`).
3. **Light-source bookkeeping.** Per light block: all sources and the active ones, with add / remove / set active.
   It answers "lit?" (any active source) and "remove block?" (no sources).
   Plus the shape diff (old positions vs new positions → removed / added) used for mode changes and re-casts.
4. **Light state machine.** Active/inactive, redstone + invert, timeout after turning off, "needs re-cast" flag, per-tick energy or fuel cost.
5. **Formulas.** Carbon burn time, grow-tick interval, energy cost per mode.
6. **Unit tests** for all of the above (in OpenModularCore, with its isolation check, architecture rules, coverage and mutation testing).

The core layer works through small interfaces that `main` implements: `LightWorld.cellAt(pos)` classifies a block as a `Cell` (air, phantom light, UV light, opaque, transparent, unloaded; see its Javadoc for the mapping from `BlockState`), and `PowerSource` pays a tick's cost.
Positions in core are the `Pos` record, not `BlockPos`; `Facing` has the same ordinal order as `Direction`.

The original beam loops are kept in OpenModularCore's test sources as a reference (`core/light/BeamTest`).
`BeamCasterTest` checks that `BeamCaster` produces their positions (filtered by the same `Spacing`) in random worlds, for every facing and many ranges.
If you change a shape on purpose, change the reference too and say so.
