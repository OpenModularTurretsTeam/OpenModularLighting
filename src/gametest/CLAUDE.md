# CLAUDE.md — Open Modular Lighting, game tests (`src/gametest`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## GameTests (`src/gametest`)

- In-world tests run on a headless GameTest server (Fabric `runGameTest`, NeoForge `runGameTestServer`; both part of `check`).
  It needs **no EULA**: the run directory gets `eula=false`, and that is fine.
  Do not accept the EULA on the user's behalf.
- `src/gametest` is a separate test mod (`openmodularlighting_gametest`; Fabric: Loom's `fabricApi.configureTests` + `fabric.mod.json`, NeoForge: the `gametest` source set registered as a second mod + `META-INF/neoforge.mods.toml`).
  It is not in the release jar.
  It registers test-only content (`TestBlocks`, `TestBlockEntities`, `TestItems` through `OMLGameTestMod.registrationSteps()`), for example `TestLightSourceBlock`, a fake light source whose on/off state and shape the test sets directly.
- **The test classes are shared by both loaders.** They implement `OMLGameTests` (not `FabricGameTest`) and use `@GameTest(template = EMPTY_STRUCTURE)`: our own 8 × 8 × 8 air structure (`src/gametest/resources/data/openmodularlighting/structure/empty.nbt`).
  Vanilla's `@GameTest` annotation is gone since 1.21.5 (a test is a registered test function plus a data-driven test instance, batched by test environment), so the test package has a stand-in `@GameTest` with the same name and attributes (`src/gametest/java/omtteam/openmodularlighting/gametest/GameTest.java`), and the test classes need no import and no holder annotations:

  ```java
  public class FooGameTest implements OMLGameTests
  ```

  Add a new test class to the `fabric-gametest` entrypoint in `src/gametest/resources/fabric.mod.json` as well (NeoForge finds it through its `@GameTest` methods).
- **Registration:** `GameTestCases` turns the `@GameTest` methods into test functions and instances `openmodularlighting_gametest:<class>/<method>` and each `batch` into an empty test environment (`timeoutTicks`, default 100).
  The test server runs its ticks as fast as it can, so a test that waits for the light engine may need a longer `timeoutTicks` when several test servers run at once (`chiseledBuild`).
  The loader entrypoints register them: `FabricGameTestRegistration` (the `fabric-gametest` entrypoints; `DynamicRegistrySetupCallback` for the instances) and `NeoForgeGameTestRegistration` (the test mod's classes with `@GameTest` methods from the mod file scan; `RegisterGameTestsEvent`).
  Fabric's own test registration still reads the entrypoints and logs that it found no methods with its annotation in them; that warning is expected.
  The game test server also runs vanilla's `minecraft:always_pass`, so it reports 66 tests (the mod has 65).
- Loader-specific test code lives in `gametest/platform/<loader>`: the entrypoints (`OMLFabricGameTests`, `OMLNeoForgeGameTests`), `TestTransfer` (energy and items through the loader's API, `OMLGameTestMod.transfer()`; use it to test the loader adapters) and the energy access of the test battery.
  `NeoForgeTestTransfer` and `NeoForgeTestBattery` use NeoForge's transfer API.
- **No test class may sit in a main-mod package:** NeoForge loads the test mod as its own module, and two modules cannot share a package.
  Reach protected block entity methods through vanilla's public ones (`BlockEntityData.save` / `load` / `clientData`).
- Tests in the same batch run side by side, and every test structure gets a beacon (light 15) next to it when the test starts (light gray while it runs, lime or red after it).
  The beacon's light can arrive after the test has begun (the light engine runs beside the server thread), so a light level read at the start is no reference.
  A test that checks for darkness closes the measured spot in with opaque blocks and expects 0 (`PhantomLightGameTest.switchingOffRemovesTheLight`).
- The test server keeps random ticks on (it only turns off mob spawning and weather), so crops in a test also grow on their own.
  A random tick adds 1 age to wheat, the grow light's bonemeal 2 to 5, so the grow light tests count growth only from age 2 (`GrowLightGameTest.grownByLight`; otherwise `doesNotGrowThroughABlock` fails now and then).
- The test mod must not load in normal dev runs: NeoForge's `client` and `server` runs set `loadedMods` to the main mod only (ModDevGradle loads every registered mod by default), and Loom's `runClient` never includes it.
  Otherwise its range override below applies in play.
- Every test structure is enclosed in barriers.
  Light beams therefore have to stay inside the 8 × 8 × 8 structure: the test mod sets both beam ranges to `OMLGameTestMod.TEST_RANGE` (6) when the server starts.
  Entrypoints do not run in dependency order, so test setup that depends on the main mod (such as the config or `Platform` services) belongs in a server lifecycle event (`OMLGameTestMod.onServerStarting`), not in the entrypoint.
- The test mod also has a test battery (`TestBatteryItem`, energy in the item's custom data; Team Reborn Energy on Fabric, an item capability on NeoForge).
- The client (screens, models, tint, tooltips) is not covered: client GameTests are possible on this Minecraft version (Fabric's need 1.21.4+), but none are written yet.
  Check it by hand with `runClient`.
