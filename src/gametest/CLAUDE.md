# CLAUDE.md — Open Modular Lighting, game tests (`src/gametest`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## GameTests (`src/gametest`)

- In-world tests run on a headless GameTest server (Fabric `runGameTest`, Forge `runGameTestServer`; all part of `check`).
  It needs **no EULA**: the run directory gets `eula=false`, and that is fine.
  Do not accept the EULA on the user's behalf.
- `src/gametest` is a separate test mod (`openmodularlighting_gametest`; Fabric: Loom's `fabricApi.configureTests` + `fabric.mod.json`, Forge: the `gametest` source set registered as a second mod + `META-INF/mods.toml`).
  It is not in the release jar.
  It registers test-only content (`TestBlocks`, `TestBlockEntities`, `TestItems` through `OMLGameTestMod.registrationSteps()`), for example `TestLightSourceBlock`, a fake light source whose on/off state and shape the test sets directly.
- **The test classes are shared by both loaders.** They implement `OMLGameTests` (not `FabricGameTest`) and use `@GameTest(template = EMPTY_STRUCTURE)`: our own 8 × 8 × 8 air structure (`src/gametest/resources/data/openmodularlighting/structures/empty.nbt`).
  They import vanilla's `@GameTest`, and the Forge test classes also carry Forge's annotations (imported per loader):

  ```java
  //? if forge {
  /*@GameTestHolder(OML.MOD_ID)
  @PrefixGameTestTemplate(false)
  *///?}
  public class FooGameTest implements OMLGameTests
  ```

  Add a new test class to the `fabric-gametest` entrypoint in `src/gametest/resources/fabric.mod.json` as well (Forge finds it through `@GameTestHolder`).
- Loader-specific test code lives in `gametest/platform/<loader>`: the entrypoints (`OMLFabricGameTests`, `OMLForgeGameTests`), `TestTransfer` (energy and items through the loader's API, `OMLGameTestMod.transfer()`; use it to test the loader adapters) and the energy access of the test battery.
- **No test class may sit in a main-mod package:** the loader loads the test mod as its own module, and two modules cannot share a package.
  Reach protected block entity methods through vanilla's public ones (`BlockEntityData.save` / `load` / `clientData`).
- Tests in the same batch run side by side, and every test structure gets a beacon (light 15) next to it when the test starts (light gray while it runs, lime or red after it; at (-1, -1, -1) of the structure).
  The beacon's light can arrive after the test has begun (the light engine runs beside the server thread), so a light level read at the start is no reference.
  A test that checks for darkness closes the measured spot in with opaque blocks and expects 0 (`PhantomLightGameTest.switchingOffRemovesTheLight`).
- The test server keeps random ticks on (it only turns off mob spawning and weather), so crops in a test also grow on their own.
  A random tick adds 1 age to wheat, the grow light's bonemeal 2 to 5, so the grow light tests count growth only from age 2 (`GrowLightGameTest.grownByLight`).
- The test mod must not load in normal dev runs: the Forge node's `client` and `server` runs set `loadedMods` to the main mod only (ModDevGradle loads every registered mod by default), and Loom's `runClient` never includes it.
  Otherwise its range override below applies in play.
- Light beams have to stay inside the 8 × 8 × 8 test structure, so that they neither leave it nor reach tests running next to it: the test mod sets both beam ranges to `OMLGameTestMod.TEST_RANGE` (6) when the server starts.
  Entrypoints do not run in dependency order, so test setup that depends on the main mod (such as the config or `Platform` services) belongs in a server lifecycle event (`OMLGameTestMod.onServerStarting`), not in the entrypoint.
- The test mod also has a test battery (`TestBatteryItem`, energy in the item's custom data; Team Reborn Energy on Fabric, an item capability on Forge).
- The client (screens, models, tint, tooltips) is not covered: Fabric's client GameTests need a newer Minecraft.
  Check it by hand with `runClient`.

## Forge 1.20.1: game tests (from "Forge 1.20.1")

- **GameTests:** `runGameTestServer` with `forge.enabledGameTestNamespaces`; the test classes carry `@GameTestHolder` and `@PrefixGameTestTemplate(false)` (`net.minecraftforge.gametest`).
  Vanilla 1.20.1's `makeMockServerPlayerInLevel` gives the connection no netty channel, which Forge's login handling needs: use `OMLGameTests.mockPlayer(helper)` instead (an `EmbeddedChannel` on Forge).
  The library jar the client and game test source sets use is the one with the dev names (classifier `dev`); a Forge game test server whose mod fails to load still exits normally, so `check` also requires the "All N required tests passed" line in `build/run/gameTest/logs/latest.log`.
  The flat test world (own folder `versions/<node>/build/run/gameTest`, a `server.properties` written before every run, old world deleted) works as in OMT (`../OpenModularTurrets/src/gametest/CLAUDE.md`, "Forge 1.20.1: game tests").
  The Forge test world keeps the default game rules (weather cycle and mob spawning on, unlike vanilla's test server on Fabric); in practice it stays clear, because the server sets clear weather for 20,000,000 ticks at start, and no mob spawns without a player nearby.
  OpenModularTurrets turns both rules off at server start in its test mod's Forge entrypoint (`OMTForgeGameTests`); do the same here if a test comes to depend on them.
