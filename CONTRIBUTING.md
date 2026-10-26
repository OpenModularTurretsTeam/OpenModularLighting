# Contributing to Open Modular Lighting

Thanks for helping out!
Open Modular Lighting is a port of FloodLights from Minecraft 1.7.10.
It targets **Fabric** (1.20.1, 1.21.1, 1.21.11), **NeoForge** (1.21.1, 1.21.11) and **Forge** (1.20.1) from a single codebase using [Stonecutter](https://stonecutter.kikugie.dev/).
[`docs/architecture.md`](docs/architecture.md) explains how the code is organised.

Questions are welcome on our [Discord](https://discord.gg/SEG695u).

## Reporting issues

Open an issue on the project's issue tracker and pick one of the templates:

- **Bug report** for crashes and broken behaviour.
  Include the Minecraft, mod and loader versions (Fabric Loader and Fabric API, NeoForge or Forge), and your log or crash report.
- **Missing or different from 1.7.10** for anything that is missing or works differently than in the old mod.
- **Feature request** for new ideas.
  The port comes first, so new features may take a while.

## Development setup

You need:

- **Java 25 or newer** to run Gradle (a Fabric Loom 1.18 requirement).
  The Java 17 and 21 compile toolchains are downloaded automatically if they are missing.
- Git and an IDE with Gradle support (IntelliJ IDEA is recommended).

### Building the mod for all versions and loaders

Clone the repository, then run in its directory:

```bash
./gradlew chiseledBuild
```

The jars end up in `versions/<minecraft version>-<loader>/build/libs/`.

### Useful commands

Every Stonecutter node is named `<minecraft version>-<loader>`: `1.20.1-fabric`, `1.20.1-forge`, `1.21.1-fabric`, `1.21.1-neoforge`, `1.21.11-fabric`, `1.21.11-neoforge`.

```bash
./gradlew chiseledBuild                          # build all nodes (unit tests and GameTests included)
./gradlew :1.21.1-fabric:runClient               # start a client (also :runServer; any node works)
./gradlew check                                  # unit tests, GameTests and the core isolation check
./gradlew :1.21.1-fabric:runGameTest             # in-world GameTests, Fabric nodes
./gradlew :1.21.1-neoforge:runGameTestServer     # in-world GameTests, NeoForge and Forge nodes
./gradlew :1.21.1-fabric:runDatagen              # regenerate recipes, loot tables, tags (run for every Fabric node)
./gradlew spotlessApply                          # fix the formatting
./gradlew "Set active project to 1.20.1-fabric"  # switch the source to 1.20.1
./gradlew "Set active project to 1.21.1-fabric"  # switch back
```

## Working with several versions and loaders

- The committed source is always in the **1.21.1-fabric** state.
  If you switched to another node, switch back before you commit.
- Version-specific code uses Stonecutter comments:

  ```java
  //? if >=1.21 {
  ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  //?} else {
  /*ResourceLocation id = new ResourceLocation(MOD_ID, path);
  *///?}
  ```

  Keep these blocks out of gameplay code.
  Put version differences behind small helpers in `omtteam.openmodularlighting.compat` instead.
  A class that differs completely between versions goes into a version source folder (`src/<source set>/mc<first>-<last>/java`), once per range.
- Loader-specific code goes into the `platform/fabric`, `platform/neoforge` and `platform/forge` packages, and shared code reaches it through the small interfaces in `platform`.
- `assets/` (textures, models, blockstates, lang, sounds) are shared and written by hand in `src/main/resources`.
  Every item needs an item model definition in `assets/openmodularlighting/items/`.
- `data/` (recipes, loot tables, tags) is **generated** with Fabric Datagen into `versions/<version>/src/main/generated`.
  NeoForge and Forge convert that output at build time.
  Don't write data files by hand.
  Change the datagen providers, run datagen for every Fabric node and commit the output.

## Code layout

- `src/main/java` holds common and server code.
  `src/client/java` holds client-only code (renderers, screens).
  Never reference client classes from `main`.
- `omtteam.openmodularlighting.core` holds the game logic (beam shapes, light source bookkeeping, the on/off state machine, fuel and growth formulas).
  It must not import Minecraft or loader classes; `./gradlew check` fails if it does.
  Unit tests for it live in `src/test/java`.
- `src/gametest` is a separate test mod with in-world GameTests, shared by all loaders.
  It is never shipped.
  Add a GameTest for new in-world behaviour where you can.

## Code style

- Java only, 4-space indent, no wildcard imports.
- The project uses Spotless for code styling.
- Mojang mappings (`BlockEntity`, `ResourceLocation`, `AbstractContainerMenu`, …).
- Registry IDs and resource names are lowercase `snake_case`.
- All user-facing text goes through translation keys (`block.openmodularlighting.…`, `gui.openmodularlighting.…`, `tooltip.openmodularlighting.…`) in the lang files.
- Gameplay logic runs on the server.
  Clients only render and send requests.
- Add a short Javadoc to any non-obvious formula, noting where it came from in the old mod.
- Dependencies are limited to the loaders and their APIs (Fabric API on Fabric), Team Reborn Energy and Jade (optional, only in `compat/jade` and `client/compat/jade`).
  Ask before adding anything else.

## Pull requests

1. Create a branch and keep each pull request focused on one change.
2. Add a one-line entry for every user-visible change under `## [Unreleased]` in `CHANGELOG.md`, by hand or with:

   ```bash
   ./gradlew changelogEntry --type=fixed --entry="Grow lights no longer grow crops while out of energy"
   ```

   Types are `added`, `changed`, `fixed` and `removed`.
3. Update the documentation a change touches: `docs/`, `README.md` and this file.
4. Make sure `./gradlew chiseledBuild`, `./gradlew check` and `./gradlew spotlessCheck` pass and the source is back in the 1.21.1-fabric state.
5. If `spotlessCheck` fails, run `./gradlew spotlessApply` to fix the formatting automatically.
6. If you can, test in game on the loaders and versions you touched, and on a dedicated server for anything involving networking or GUIs.
7. Describe what you changed and how you tested it in the pull request.

## License

By contributing, you agree that your code is licensed under the [MIT license](LICENSE) and your art (textures,
models, sounds) under [CC BY-NC-SA 4.0](LICENSE-ART).
You also agree to the terms of the [CLA](CLA.md).
