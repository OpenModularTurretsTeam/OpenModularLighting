# Open Modular Lighting 1.12.2, package layout, core wiring and 1.12.2 specifics

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in the `omtteam.openmodularlighting` packages; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md` is the workspace file, `../../CLAUDE.md` the OMM root file, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.1 Package layout

```text
omtteam.openmodularlighting
├── OML                 @Mod entrypoint (pre-init: config, network, event handlers; init: GUI handler, smelting), MOD_ID, LOGGER, id(path)
├── CommonProxy         server side of the @SidedProxy (client: client.ClientProxy)
├── core/               ── from OpenModularCore (oml-core-j8), not in this repo; NO Minecraft imports ──
├── block/              FloodlightBlock (+ FloodlightInteraction) → Electric…, Carbon…, UvFloodlightBlock, CubeLightBlock,
│                       PanelLightBlock (PanelShapes) → FluorescentLightBlock, GrowLightBlock; LightBlock → PhantomLightBlock,
│                       UvLightBlock; LightproofGlassBlock
├── tileentity/         FloodlightTileEntity (LightBeam) → BeamFloodlightBE → Electric…, Carbon…; UvFloodlightBE,
│                       PanelLightTileEntity → FluorescentLightBE, GrowLightBE, CubeLightBE; LightTileEntity → PhantomLightBE, UvLightBE
├── container/          FloodlightContainer → Electric…, CarbonFloodlightContainer
├── handler/            OMLGuiHandler
├── registry/           ModBlocks, ModItems, ModTileEntities, ModCreativeTabs, ModRecipes, Registration
├── energy/             ElectricPower, FuelPower, LightPower, ClusterEnergy, Batteries
├── world/              LightBlocks, LevelLightWorld, Positions, PosArrays
├── config/             as on the main branch (Java 8)
├── compat/             CommonTags, BlockEnabledCondition
└── client/             ClientProxy, ClientEvents (models, colours, tooltip, debug renderer),
                        camo/ (CamoModel, CamoModels), render/ (FloodlightTint, LightBlockDebugRenderer), gui/
```

Dependency direction: `client → main → core`. `core` depends on nothing. `main` never references `client` (only through the proxy).

## Gameplay (§6.2–§6.11)

The following sections are the main branch's and apply here unchanged in behaviour.
Read them with §5 at hand: "Team Reborn Energy" / "FE" means Forge Energy, "datagen" means the handwritten recipe JSON, "tags" means the ore dictionary, "GameTests" do not exist, Jade is not available, and `fabric`/`neoforge`/`platform` classes have no counterpart (their job is done by `OML`, the proxies and the block entities' capabilities).

## Core Game Logic Source (from §4 "Build")

The game logic (package `omtteam.openmodularlighting.core`: beam shapes, rotation, light-source bookkeeping, the light state machine, formulas, clusters) is free of Minecraft imports and lives in the shared core repo `OpenModularCore` (`../../OpenModularCore`, module `oml-core`), the same code for every Minecraft version.
This branch uses its Java 8 module `oml-core-j8` in the version `core_version` (`gradle.properties`, the same in every mod and workspace), from JitPack.
- The `jar` task copies the module's classes and its JvmDowngrader stubs (`omtteam/openmodularlighting/core/**`, `omtteam/openmodularlighting/jvmdg/**`, through the non-transitive `coreBundle` configuration) into the dev jar, and so into `reobfJar`: players install one jar, and no other mod's core classes are in it.
- Dev runs and the unit tests get the module as a plain library on the classpath.
- Public core signatures use only Java 8 types: randomness is a `LongSupplier` (`world.rand::nextLong`).
- The core's unit tests, its isolation check and its PIT run are in the core repo; this repo tests the game layer and the config.
- When `../../OpenModularCore` exists, `settings.gradle.kts` (and the workspace's `settings.gradle`) includes it, so the build uses its source instead of the JitPack artifact and a core edit is live without a release; `-Pomm.noLocalCore` or `OMM_NO_LOCAL_CORE=1` turns that off, as a build without the folder does (`../../CLAUDE.md`, "Local development").
The main branch's ArchUnit, JaCoCo and PMD setup is not on this branch.

## 5. 1.12.2 specifics

Differences to the main branch, and the 1.12.2 pitfalls the code handles. Keep them in mind when porting a change.

| Area | 1.12.2 |
| --- | --- |
| Registration | `Registration` handles `RegistryEvent.Register<Block/Item>` (fired after pre-init); block entity classes via `GameRegistry.registerTileEntity` in `ModTileEntities`. Blocks and items are created statically in `ModBlocks` / `ModItems` (registry name + translation key there) |
| Blockstate | metadata: facing index in bits 0–2, `LIT` in bit 3 (`FloodlightBlock`); light blocks: `LIT` only. The small fluorescent light's `ROTATED` does not fit and lives in its block entity (`getActualState`) |
| Tile entities | OMLib's `tileentity.TileEntitySynced` (OML's former `CompatTileEntity`, moved to OMLib on 2026-10-25; `readData` / `writeData` / `writeClientData`, update packets, `syncToClient`, `remesh`). **It overrides `shouldRefresh`**: Forge otherwise recreates a mod tile entity on every state change, which would wipe a light whenever `LIT` switches |
| Ticking | `ITickable` on the floodlights and the UV light; the phantom light's block entity does not tick (random ticks on the block) |
| Block flags | `LightBlocks.FLAGS` = 2 \| 16 (clients, no observers, no neighbour notification); `UPDATE_ALL` = 3 for the UV light's removal |
| Phantom light | `Material.AIR` (so `isAir` is true, as in 1.7.10): falling blocks, trees, pistons and explosions treat it as air. Random ticks still work (1.12 counts them per block). OML never asks `Block.isAir` (true for every air-material block, also other mods'): `LightBlocks.isAir` is real air (`BlockAir`), as `isAir()` on the main branch |
| Leaves | `isOpaqueCube` follows the graphics setting (always opaque on a dedicated server); `LevelLightWorld` lets beams pass leaves, as on the main branch |
| UV light | a `MaterialTransparent` (replaceable, not air), `getRenderType` by `LIT` and `uvLightRendered` |
| Lightproof glass | own `LightproofGlassBlock` (no tinted glass in 1.12): glass with light opacity 255 that drops itself |
| Drops | `FloodlightBlock` keeps the block until `harvestBlock` (`removedByPlayer`), so `getDrops` can read the block entity; settings go into the item's `BlockEntityTag`, which vanilla loads on placement |
| Interaction | `FloodlightInteraction` listens to `PlayerInteractEvent.RightClickBlock` (fires before the sneak check, also for wrenches that do not bypass sneaking) |
| Wrenches, wool, dyes | no tags: `CommonTags.isWrench` (OMLib's `ItemMultiTool`, tool class `wrench` or ore dictionary `toolWrench`/`itemWrench`/`wrench`), ore dictionary `blockWool`, Forge `DyeUtils` |
| Energy, items | capabilities on the tile entities: OMLib's `power.EnergyAccessStorage` wraps the core `EnergyAccess` (insert only, `extractable = false`), OMLib's `inventory.AutomationItemHandler` wraps the slot's `ItemStackHandler` (insert what the slot accepts, extract nothing), `util.inventory.InvUtil.dropItems` spills the slot when the block breaks; batteries are items with extractable FE |
| Fuel | `TileEntityFurnace.getItemBurnTime` and the item's container item |
| Menus | `OMLGuiHandler` (`IGuiHandler`, ids `ELECTRIC` / `CARBON`); screens come from `ClientProxy.createGui`. `FloodlightContainer` syncs its long values (clamped to `0..Integer.MAX_VALUE`) through OMLib's `container.WindowValues` (two 16-bit window properties each) |
| Network | no channel of its own: the config goes through OMLib's `config.ConfigSync` (registered in `OML.preInit` as one value: `OMLConfig.toJson` sent on login, `OMLConfig.applyServerConfig` on the client; OMLib applies the local config's JSON again after disconnecting); the camouflage menu uses OMLib's messages |
| Disguise | OMLib's `BlockAbstractCamoTileEntity.RENDERBLOCKSTATE` is an unlisted property in the floodlights' `ExtendedBlockState` (`FloodlightBlock.supportsCamo`); `client.camo.CamoModel` wraps their blockstate models in `ModelBakeEvent` and returns quads per render layer (`MinecraftForgeClient.getRenderLayer()`); camo blocks render in every layer but translucent |
| Damage | `UvLightTileEntity.UV_LIGHT` (`DamageSource("openmodularlighting.uv_light")`) |
| Config | Gson 2.2.4 ships with 1.12: `new JsonParser().parse(...)` (lenient, reads the `//` comments), no `keySet()`; config folder from `FMLPreInitializationEvent` |
| NBT | position lists are flat `int` arrays (`PosArrays`): 1.12 cannot read long arrays back |
