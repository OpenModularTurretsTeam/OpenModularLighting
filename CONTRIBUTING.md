# Contributing to Open Modular Lighting

Thanks for helping out!
Open Modular Lighting is a port of FloodLights from Minecraft 1.7.10.
This branch (`1.21.11`) targets Minecraft 1.21.11 with **Fabric** and **NeoForge** from a single codebase, using the loader axis of [Stonecutter](https://stonecutter.kikugie.dev/).
[`docs/architecture.md`](docs/architecture.md) explains how the code is organised.

Questions are welcome on our [Discord](https://discord.gg/SEG695u).

## Reporting issues

Open an issue on the project's issue tracker and pick one of the templates:

- **Bug report** for crashes and broken behaviour.
  Include the Minecraft, mod and loader versions (Fabric Loader and Fabric API, or NeoForge), and your log or crash report.
- **Missing or different from 1.7.10** for anything that is missing or works differently than in the old mod.
- **Feature request** for new ideas.

## Development setup

You need:

- **Java 25 or newer** to run Gradle (Fabric Loom 1.18 requires it).
  Gradle runs on the JDK in `JAVA_HOME`, or on the Gradle JVM set in your IDE; the build does not pin one.
  The toolchain the nodes compile with, Java 21, is downloaded automatically if it is missing.
- Git.
- A POSIX `sh` with `find`, `sort`, `xargs` and `sha256sum` on the `PATH`: the build runs the shell script `omlib/tools/bootstrap.sh` (on Windows, for example from Git Bash).
- An IDE with Gradle support (IntelliJ IDEA is recommended).

The Gradle wrapper (Gradle 9.8.1) is part of the repository, so always use `./gradlew`.

### Getting the code

```bash
git clone --recurse-submodules -b 1.21.11 https://github.com/OpenModularTurretsTeam/OpenModularLighting.git
cd OpenModularLighting
git submodule update --init    # only if you cloned without --recurse-submodules
```

The branch `1.21.11` holds the code for Minecraft 1.21.11; the branches `1.20.1` (Fabric and Forge), `1.21.1` (Fabric and NeoForge) and `1.12` (Minecraft 1.12.2, with a build of its own) hold the other versions.
The library [OMLib](https://github.com/OpenModularTurretsTeam/OMLib) is the git submodule `omlib`, checked out at the commit this repository records.
Its URL is relative (`../OMLib.git`): it resolves to the OMLib repository of the GitHub account you cloned from (HTTPS or SSH).
If you clone your fork, fork OMLib as well, or clone without `--recurse-submodules` and point the submodule at the team's repository before you initialise it:

```bash
git config submodule.omlib.url https://github.com/OpenModularTurretsTeam/OMLib.git   # fork clones only
git submodule update --init
```

Without the submodule Gradle stops while it reads the settings, with "Included build '.../omlib/build-logic' does not exist"; run `git submodule update --init`.

### Opening the project in an IDE

Open the repository folder in IntelliJ IDEA as a Gradle project.
The first sync builds OMLib and sets up Minecraft for both nodes, so it takes a while.
The Fabric node creates IDE run configurations for its client and server runs, which use the same `versions/<node>/run` folders as the Gradle tasks.

### Building

```bash
./gradlew chiseledBuild               # build both nodes (unit tests and GameTests included)
./gradlew :1.21.11-fabric:build       # build one node
./gradlew check                       # both nodes: unit tests, GameTests, recipe and core checks, spotlessCheck
```

Every Stonecutter node is named `<minecraft version>-<loader>`: `1.21.11-fabric` and `1.21.11-neoforge`.
Before anything else is configured, Gradle builds OMLib from the `omlib` submodule for both nodes into `omlib/build/maven`.
That is a no-op while the library's sources and the core it uses are unchanged.
The first run also sets up Minecraft for both nodes, so it takes a while.

The jars end up in `versions/<minecraft version>-<loader>/build/libs/`, named `openmodularlighting-<mod version>-mc<minecraft version>-<loader>.jar`.
Each jar bundles OMLib (jar-in-jar) and the classes of the core module `oml-core`.
The folder keeps the jars of older builds too, so check the version in the name.

### Running and testing locally

```bash
./gradlew :1.21.11-fabric:runClient            # start a client (the other node: :1.21.11-neoforge:runClient)
./gradlew :1.21.11-fabric:runServer            # start a dedicated server (either node)
./gradlew :1.21.11-fabric:test                 # unit tests of one node
./gradlew :1.21.11-fabric:runGameTest          # in-world GameTests, Fabric node
./gradlew :1.21.11-neoforge:runGameTestServer  # in-world GameTests, NeoForge node
./gradlew :1.21.11-fabric:runDatagen           # regenerate recipes, loot tables, tags (Fabric node only)
./gradlew :1.21.11-fabric:pitest               # mutation tests of the config classes, on demand
./gradlew spotlessApply                        # fix the formatting
./gradlew "Set active project to 1.21.11-neoforge"  # switch the source to the NeoForge node
./gradlew "Set active project to 1.21.11-fabric"    # switch back
```

- Every node runs the client and server in its own folder `versions/<node>/run` (worlds, `mods/`, `logs/`, `config/`), so worlds and mods you add by hand stay with their version.
- The NeoForge client and server runs load the mod without the GameTest mod.
- Jade is on the classpath of the dev runs (not bundled), so you can try the Jade plugin there.
- The build writes no `eula.txt`: after the first `runServer`, set `eula=true` in `versions/<node>/run/eula.txt`.
  To join that server from a dev client, also set `online-mode=false` in `versions/<node>/run/server.properties`.
- The GameTests need no EULA.
  On NeoForge, `runGameTestServer` also fails unless `versions/<node>/run/logs/latest.log` says that the required tests passed.
  The Fabric GameTests run in `versions/<node>/build/run/gameTest`; their logs and crash reports are there.
- The unit tests run on both nodes; the architecture rules (ArchUnit) only on the Fabric node.
- Reports (Fabric node only): JaCoCo and PMD for the active node only, PIT for the node you run `pitest` on:
  - JaCoCo test coverage (printed by `check`): `versions/<node>/build/reports/jacoco/test/html/index.html`
  - PMD (report only, nothing fails): `versions/<node>/build/reports/pmd/main.html` and `client.html`
  - PIT: `versions/<node>/build/reports/pitest/index.html`
- CI (`.github/workflows/check.yml`) runs `./gradlew check` on every push and pull request.
  It has no local OpenModularCore, so it builds against the JitPack artifact of `core_version`.

### Working on the shared core (OpenModularCore)

The game logic in `omtteam.openmodularlighting.core` is the module `oml-core` of [OpenModularCore](https://github.com/OpenModularTurretsTeam/OpenModularCore).
By default the build takes it from JitPack, in the version that `core_version` in `gradle.properties` names.
To build the mod against your own copy of the core, clone OpenModularCore next to this repository's folder:

```bash
cd ..                                                                    # the folder that holds OpenModularLighting/
git clone https://github.com/OpenModularTurretsTeam/OpenModularCore.git  # gives ../OpenModularCore next to it
```

The build looks for `OpenModularCore/settings.gradle.kts` in this repository's folder and in every folder above it, and the first one it finds replaces the JitPack modules (a Gradle composite build), whatever `core_version` says.
It then prints `OpenModularCore: local source at <path>`; with the JitPack artifact it prints nothing.

```bash
cd OpenModularCore && ./gradlew check                    # from the folder that holds both clones: the core's tests (on Java 17 and on Java 8) and checks
cd ../OpenModularLighting && ./gradlew chiseledBuild     # the mod against the local core
OMM_NO_LOCAL_CORE=1 ./gradlew check                      # the mod against the JitPack core, as CI builds it (or -Pomm.noLocalCore)
```

- Edits to `oml-core` reach the mod at the next build.
- Edits to `omlib-core` reach it through the library: the next Gradle run rebuilds `omlib` with the local core.
  Switching between the local and the JitPack core rebuilds the library too.
- The core is shared by all mods and all Minecraft versions; changes to it go to OpenModularCore as a pull request, see its README.
  The mod uses such a change once a new core version is released and `core_version` is raised.
- `check` fails if this repository holds sources in the core package (`verifyNoCoreSources`).

### Working on OMLib

The mod builds against the `omlib` submodule only, never against another OMLib clone.

- To try a library change, edit it in `omlib/`.
  The next Gradle run rebuilds the library from it and drops Loom's cached copy of the old one.
- `omlib/` is checked out on a detached HEAD, so create a branch there before you commit:

  ```bash
  git -C omlib switch -c 1.21.11-<feature>   # a branch for your OMLib change
  ```

- To keep the change, commit it to OMLib (branch `1.21.11`, a pull request there), then move the submodule to that commit and commit the new pointer here:

  ```bash
  git -C omlib fetch origin                # or: git -C omlib fetch <your OMLib clone> <branch>
  git -C omlib checkout --detach <commit>  # the OMLib commit to use
  git add omlib                            # record the new pointer, then commit
  ```

  CI checks out the recorded commit, so it must be pushed to OMLib first.
- `git submodule update` checks out the recorded commit again, and leaves `omlib/` on a detached HEAD, so commit your work in `omlib/` on a branch before you run it.
- Set `OMLIB_SKIP_BUILD` (any value) to skip the library build, for example when the jars in `omlib/build/maven` are current; then library and `omlib-core` edits are not picked up.
- Don't run two builds at the same time on one `omlib/` copy: its nodes publish into one local Maven repository.

## Working with two loaders

- The committed source is always in the **1.21.11-fabric** state.
  If you switched to the NeoForge node, switch back before you commit.
- Loader-specific code uses Stonecutter comments:

  ```java
  //? if fabric {
  return new ClientPlatformServices(new FabricClientNetwork());
  //?} else {
  /*return new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network()));
  *///?}
  ```

  Keep these blocks out of gameplay code.
  Put loader differences behind the small service interfaces in OMLib's `omtteam.omlib.platform` instead.
  The helpers in OMLib's `omtteam.omlib.compat` (`VersionCompat` and others) keep the call sites the same on every version branch, which makes porting a change between the branches easier; this mod's own `compat` package only holds the recipe condition (`BlockEnabledCondition`) and the Jade plugin (`compat/jade`).
- Loader-specific code goes into the `platform/fabric` and `platform/neoforge` packages, and shared code reaches it through the small service interfaces in OMLib's `omtteam.omlib.platform`.
- `assets/` (textures, models, blockstates, lang, sounds) are shared and written by hand in `src/main/resources`.
  Every item needs an item model definition in `assets/openmodularlighting/items/`.
- `data/` (recipes, loot tables, tags) is **generated** with Fabric Datagen into `versions/1.21.11-fabric/src/main/generated`.
  The NeoForge node converts that output at build time.
  Don't write data files by hand.
  Change the datagen providers, run datagen on the Fabric node and commit the output.

## Code layout

- `src/main/java` holds common and server code.
  `src/client/java` holds client-only code (renderers, screens).
  Never reference client classes from `main`.
- `omtteam.openmodularlighting.core` holds the game logic (beam shapes, light source bookkeeping, the on/off state machine, fuel and growth formulas).
  It is plain Java without Minecraft or loader classes and lives in the shared repository [OpenModularCore](https://github.com/OpenModularTurretsTeam/OpenModularCore) (module `oml-core`, the same on every Minecraft version), together with its unit tests; the build bundles it into the jar.
  Changes to it go there, not into this repository (`./gradlew check` fails on sources in the core package).
- Unit tests for the rest (config, architecture rules) live in `src/test/java`.
- `src/gametest` is a separate test mod with in-world GameTests, shared by both loaders.
  It is never shipped.
  Add a GameTest for new in-world behaviour where you can.

## Code style

- Java only, 4-space indent, no wildcard imports.
- The project uses Spotless for code styling.
- Mojang mappings (`BlockEntity`, `Identifier`, `AbstractContainerMenu`, …).
- Registry IDs and resource names are lowercase `snake_case`.
- All user-facing text goes through translation keys (`block.openmodularlighting.…`, `gui.openmodularlighting.…`, `tooltip.openmodularlighting.…`) in the lang files.
- Gameplay logic runs on the server.
  Clients only render and send requests.
- Add a short Javadoc to any non-obvious formula, noting where it came from in the old mod.
- Dependencies are limited to the loaders and their APIs (Fabric API on Fabric), OMLib (bundled jar-in-jar; the git submodule `omlib`, so clone with `--recurse-submodules`), Team Reborn Energy (bundled by OMLib, which holds the adapters) and Jade (optional, only in `compat/jade` and `client/compat/jade`).
  Ask before adding anything else.

## Pull requests

1. Create a branch and keep each pull request focused on one change.
2. Add a one-line entry for every user-visible change under `## [Unreleased]` in `CHANGELOG.md`, by hand or with:

   ```bash
   ./gradlew changelogEntry --type=fixed --entry="Grow lights no longer grow crops while out of energy"
   ```

   Types are `added`, `changed`, `fixed` and `removed`.
3. Update the documentation a change touches: `docs/`, `README.md` and this file.
4. Make sure `./gradlew chiseledBuild`, `./gradlew check` and `./gradlew spotlessCheck` pass and the source is back in the 1.21.11-fabric state.
5. If `spotlessCheck` fails, run `./gradlew spotlessApply` to fix the formatting automatically.
6. If you can, test in game on the loaders you touched, and on a dedicated server for anything involving networking or GUIs.
7. Describe what you changed and how you tested it in the pull request.

## License

By contributing, you agree that your code is licensed under the [MIT license](LICENSE) and your art (textures, models, sounds) under [CC BY-NC-SA 4.0](LICENSE-ART).
You also agree to the terms of the [CLA](CLA.md).
