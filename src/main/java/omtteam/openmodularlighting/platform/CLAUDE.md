# CLAUDE.md — Open Modular Lighting, loaders (`platform/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## Loaders

Each Stonecutter node is `<minecraft version>-<loader>`; the loader picks the build script (`build.<loader>.gradle.kts`).
Loader differences are handled in two ways:

- **Whole loader-specific classes** go into a `platform/<loader>/` package (`platform/fabric/`, `platform/forge/`, also under `client/` and the test mod).
  `mod-common` leaves the other loader's `platform/` package out of the compilation, so these classes need no Stonecutter comments.
  Examples: entrypoints, capability/API registration, event wiring.
- Shared code reaches the loader through the small service interfaces in OMLib's `platform` (design new ones with the user, workspace `CLAUDE.md`, "Designing APIs"; they are added in OMLib).
- **Small differences** inside a shared class use the loader constants: `//? if fabric {` / `//? if forge {` (set in `stonecutter.gradle.kts`), with separate import blocks because the packages differ.
  Keep them inside `compat/` and `platform/`, never in gameplay code.
- `core` stays loader-free; it comes from OpenModularCore, whose build checks that.
- **Everything compiles on Forge except `datagen`,** which is Fabric-only by design (Forge gets the converted Fabric output, §5 "Resources strategy").
- **Start-up:** both entrypoints call `OML.init()` (config, packets) first.
  Fabric then runs `OML.registrationSteps()` in order; Forge runs each `RegistrationStep` in the `RegisterEvent` of its registry and throws at common setup if a step never ran.
  A new registry class gets a step in that list, not a call in an entrypoint.

## Forge 1.20.1

The node `1.20.1-forge` is the second loader of this branch; its specifics:

- **Build:** `build.forge.gradle.kts` uses `net.neoforged.moddev.legacyforge` (`legacyForge { version = forge_version }`).
  Forge 1.20.1 runs on SRG names: the released jar is `reobfJar` (what `publishMods` uploads), and other mods (Jade) come through legacy ModDevGradle's remapping configurations: dependencies are declared on a `mod…` configuration (`modCompileOnly`, built in; `modLocalRuntime`, created by `obfuscation.createRemappingConfiguration(localRuntime)`), and their remapped jars land in its parent (`compileOnly`, `localRuntime`, which `runtimeClasspath` extends).
  Declaring them on the parent instead skips the remapping (Jade would be missing from the Forge dev runs).
- **Data:** `convertFabricData` (a function in `omlib/build-logic`) copies the Fabric output with `"conditions"` (Forge 1.20.1 reads them on recipes and advancements).
  The node also generates `pack.mcmeta` (pack format 15) for the mod and the test mod: without one, Forge 1.20.1 loads neither's data.
- **Entrypoints:** `OMLForge` (`@Mod`, mod bus from `FMLJavaModLoadingContext`); Forge 1.20.1 has no client-only `@Mod` classes, so it constructs `client.platform.forge.OMLForgeClient` by name on a client (main cannot see the client source set).
- **Services** (OMLib's): `ForgeNetwork` uses one `EventNetworkChannel` per packet type (named by its id, missing remote accepted, decoded on the network thread, handled on the main thread; S2C handlers dispatched to the handler the client registered).
  `ForgeMenus` uses `IForgeMenuType` / `NetworkHooks.openScreen`.
- **Capabilities:** no registration event in 1.20.1; OMLib's entrypoint attaches `ForgeBlockCapabilities` to every `TransferHost` (the lights) in `AttachCapabilitiesEvent`.
  It asks the light on every query (attached during construction; the exposed sides can change) and keeps a side's `LazyOptional` while the light returns the same access object.
- **Recipe condition:** `ForgeBlockEnabledCondition` (`ICondition` + `IConditionSerializer`, registered with `CraftingHelper.register` in the recipe serializer `RegisterEvent`).
- **Client:** screens are registered with `MenuScreens.register` in `FMLClientSetupEvent` (no screen event); OMLib's `ForgeCamoModel` wraps the disguise through `ModelData`, but ambient occlusion stays the block's own (Forge 1.20.1 does not pass model data there).
- `DamageSources#source` is private in vanilla 1.20.1 (only Fabric API widens it): build a `DamageSource` from the damage type holder instead.
