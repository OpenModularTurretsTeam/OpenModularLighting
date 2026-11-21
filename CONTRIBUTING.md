# Contributing to Open Modular Lighting

Thanks for helping out!
Open Modular Lighting is the port of FloodLights from Minecraft 1.7.10.
This branch, `1.12`, is the **Forge 1.12.2** version: one Gradle project built with [RetroFuturaGradle](https://github.com/GTNewHorizons/RetroFuturaGradle), Java 8 and MCP names.
The versions for Minecraft 1.20.1, 1.21.1 and 1.21.11 are built from the branch `1.20.1+`, which has its own contributing guide.

Questions are welcome on our [Discord](https://discord.gg/SEG695u).

## Reporting issues

Open an issue on the project's issue tracker and pick one of the templates:

- **Bug report** for crashes and broken behaviour.
  Say that you play on 1.12.2, include the mod, Forge and OMLib versions, and your log or crash report.
- **Missing or different from 1.7.10** for anything that is missing or works differently than in the old mod.
- **Feature request** for new ideas.

## Development setup

You need:

- **JDK 25** to run Gradle (RetroFuturaGradle 2.0.6 needs it).
  This repository does not pin a JDK for Gradle, so set `JAVA_HOME` to a JDK 25, and in IntelliJ IDEA set the Gradle JVM to 25.
- The **Java 8** toolchain (Azul Zulu) for compiling, testing and running the game.
  Gradle downloads it on the first build if no Azul JDK 8 is installed; another vendor's JDK 8 is not used.
- Git and an IDE with Gradle support (IntelliJ IDEA is recommended).
  Gradle itself comes with the wrapper (`./gradlew`, Gradle 9.8.1).

### Getting the code

Clone the mod and OMLib side by side, both on the branch `1.12`.
The `-b 1.12` matters: OpenModularLighting's default branch is `1.20.1+`, and OMLib's default branch can change too.

```bash
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OMLib.git
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OpenModularLighting.git
git clone https://github.com/OpenModularTurretsTeam/OpenModularCore.git   # optional: only to work on the core (see below)
```

### OMLib

The mod compiles and runs against OMLib's dev jar (MCP names) from your local Maven repository, `omtteam:omlib:1.12.2-<omlib_version>:dev`.
Build and publish OMLib first, and again after every change you make to OMLib:

```bash
cd OMLib
./gradlew publishToMavenLocal   # builds and tests OMLib, then publishes omtteam:omlib:1.12.2-<its version> to ~/.m2 (mod_version, plus -beta.N or -alpha.N for a pre-release)
```

The OMLib version the mod looks for is `omlib_version` in this repository's `gradle.properties`.
It is the oldest OMLib the released jar accepts, so it can be older than the OMLib you just published.
Today it is `4.0.0`, while OMLib's `mod_version` is `4.0.1`.
In that case pass the published version on every Gradle command in this repository:

```bash
./gradlew build -Pomlib_version=4.0.1   # build against the OMLib you published
```

The override also changes the OMLib range written into the jar (`omlib@[4.0.1,5.0.0-alpha)`), so keep it out of `gradle.properties` unless the mod really needs that OMLib.
Publishing an unreleased OMLib replaces the released OMLib of the same version in `~/.m2`.

For the IDE import (and so you can drop `-P` on the command line), set the published version outside the repository.
Either put `omlib_version=4.0.1` into `~/.gradle/gradle.properties`, or set the environment variable `ORG_GRADLE_PROJECT_omlib_version=4.0.1`.
Both override this repository's `gradle.properties`; the file in `~/.gradle` applies to every Gradle build on your machine, OMT and OMPD included.
Remove it again when you build a jar meant for players, because it also changes the OMLib range in the jar.

### The shared core

The game rules (package `omtteam.openmodularlighting.core`: beam shapes, light bookkeeping, the on/off state machine, fuel and growth formulas) are not in this repository.
They come from [OpenModularCore](https://github.com/OpenModularTurretsTeam/OpenModularCore), the same rules for every Minecraft version.
On 1.12.2 the mod uses its Java 8 module `oml-core-j8`, in the version `core_version` of `gradle.properties`, from [JitPack](https://jitpack.io/#OpenModularTurretsTeam/OpenModularCore).
The build copies the core classes into the mod's jars, so players still install one jar.

To change the core, clone OpenModularCore next to this repository (the layout above).
When any folder above this repository holds an `OpenModularCore/` checkout, Gradle builds the mod against that source instead of the JitPack artifact, with nothing published in between.
Gradle uses the checkout whatever version it is on, not `core_version`.
To build against exactly `core_version`, check out its tag in OpenModularCore (`git checkout 2.0.0`), or turn the local core off as shown below.
The build then prints:

```text
OpenModularCore: local source at <path> (OMM_NO_LOCAL_CORE=1 for the JitPack artifact)
```

The mod uses OMLib's part of the core too (`omtteam.omlib.core`), and that part comes inside OMLib's jar.
After a change in `omlib-core`, publish OMLib again so that the mod sees it:

```bash
cd ../OMLib && ./gradlew publishToMavenLocal   # OMLib's build picks up the same local OpenModularCore
```

To build against the JitPack artifact of `core_version` again (as for a release), turn the local core off:

```bash
OMM_NO_LOCAL_CORE=1 ./gradlew build -Pomlib_version=4.0.1   # ignore a local OpenModularCore checkout
./gradlew build -Pomm.noLocalCore -Pomlib_version=4.0.1     # the same, as a Gradle property
```

Publish OMLib with the local core off as well (`OMM_NO_LOCAL_CORE=1 ./gradlew publishToMavenLocal` in OMLib), or its jar still carries the local `omlib-core` classes.

The core's own tests run in OpenModularCore (`./gradlew check` there); see its README.
A core change is a pull request to OpenModularCore, and it must work on every Minecraft version.

### Building and testing

```bash
./gradlew build -Pomlib_version=4.0.1           # jars, unit tests and the formatting check
./gradlew test -Pomlib_version=4.0.1            # unit tests only
./gradlew check -Pomlib_version=4.0.1           # unit tests and spotlessCheck
./gradlew setupDecompWorkspace -Pomlib_version=4.0.1   # decompile Minecraft and Forge for reading in the IDE (build/rfg/minecraft-src)
```

The unit tests (JUnit 5) in `src/test/java` cover the game layer and the config.
There are no GameTests on 1.12.2.

The jars end up in `build/libs/`:

- `openmodularlighting-<version>-mc1.12.2-forge.jar`: the reobfuscated jar that players install.
- `openmodularlighting-<version>-mc1.12.2-forge-dev.jar`: the same with MCP names, for development.
- `openmodularlighting-<version>-mc1.12.2-forge-sources.jar`: the sources.

### Running the game

```bash
./gradlew runClient -Pomlib_version=4.0.1   # start a test client, logged in as "Developer"
./gradlew runServer -Pomlib_version=4.0.1   # start a dedicated test server
```

Both run in the folder `run/`, which git ignores.
OMLib and the core are on the run's classpath; Hwyla is not, since the mod only compiles against it.
The runs turn on Java assertions for the mod's own code.

On the first `runServer`, the build asks two questions in the terminal before the server starts.
The first is whether to use online mode: answer `n`, so the test client, which is not logged in to Mojang, can join; this writes `online-mode=false` to `run/server.properties`.
The second is whether you accept the [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula): answer `y`; this writes `eula=true` to `run/eula.txt`.
If there is no terminal to answer in (for example in the IDE's Gradle tool window), the task fails with "Minecraft EULA not accepted."
In that case create `run/eula.txt` with `eula=true` and `run/server.properties` with `online-mode=false` yourself, then start it again.
If `run/eula.txt` already says `eula=false`, Minecraft stops right away; change it to `eula=true`.

### Useful commands

```bash
./gradlew spotlessApply                                                   # fix the formatting
./gradlew changelogEntry --type=fixed --entry="Grow lights no longer grow crops while out of energy"   # add a CHANGELOG.md line
./gradlew tasks --all                                                     # list every task
```

## Code layout

- `src/main/java` holds all code.
  Client-only code (GUIs, renderers, the disguise models) is in `omtteam.openmodularlighting.client` and is reached only through `ClientProxy`.
  Never reference client classes from common code.
- `omtteam.openmodularlighting.compat.hwyla` is the optional Hwyla overlay, loaded only by Hwyla.
- Recipes are written by hand as JSON in `src/main/resources/assets/openmodularlighting/recipes`.
  An item without a recipe on purpose goes into `recipe-exempt.txt` with the reason.
- Textures, models, blockstates and the `.lang` files are in `src/main/resources/assets/openmodularlighting`.
- `src/test/java` holds the unit tests.

## Code style

- Java 8: no `var`, records, switch expressions or Java 9+ API in this repository.
- 4-space indent, no wildcard imports.
- The project uses Spotless for code styling (tabs to spaces, trailing whitespace, final newline); `./gradlew check` fails on a violation.
- MCP names, `stable_39` (`TileEntity`, `ResourceLocation`, `Container`, …).
- Registry IDs and resource names are lowercase `snake_case`.
- All user-facing text goes through translation keys (`tile.openmodularlighting.…`, `item.openmodularlighting.…`, `gui.openmodularlighting.…`, `tooltip.openmodularlighting.…`) in `en_us.lang`.
- Gameplay logic runs on the server.
  Clients only render and send requests.
- Add a short Javadoc to any non-obvious formula, noting where it came from in the old mod.
- Dependencies are limited to Forge, OMLib, the shared core and Hwyla (optional, only in `compat/hwyla`).
  Ask before adding anything else.

## Pull requests

1. Create a branch from `1.12` and keep each pull request focused on one change.
2. Add a one-line entry for every user-visible change under `## [Unreleased]` in `CHANGELOG.md`, by hand or with:

   ```bash
   ./gradlew changelogEntry --type=fixed --entry="Grow lights no longer grow crops while out of energy"
   ```

   Types are `added`, `changed`, `fixed` and `removed`.
3. Make sure `./gradlew build -Pomlib_version=4.0.1` passes (or with the OMLib version you published; it runs the unit tests and `spotlessCheck`).
4. If `spotlessCheck` fails, run `./gradlew spotlessApply` to fix the formatting automatically.
5. If you can, test in game with `runClient`, and with `runServer` for anything involving networking or GUIs (with the same `-Pomlib_version`).
6. The 1.12.2 version follows the features of the other Minecraft versions.
   Say in the pull request whether the change also applies to the branch `1.20.1+`.
7. Describe what you changed and how you tested it in the pull request.

## License

By contributing, you agree that your code is licensed under the [MIT license](LICENSE) and your art (textures, models, sounds) under [CC BY-NC-SA 4.0](LICENSE-ART).
You also agree to the terms of the [CLA](CLA.md).
