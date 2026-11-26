# CLAUDE.md — Floodlights (Forge 1.12.2, branch `1.12`)

This file guides Claude Code when working on this branch. Read it fully before making changes.
This branch is the **Forge 1.12.2** port; the modern versions (built with Stonecutter) live on the branches `1.20.1` (Fabric, Forge), `1.21.1` and `1.21.11` (Fabric, NeoForge; the default branch), each with its own `CLAUDE.md` (`../../OMTTeam<version>/OpenModularLighting`).
"The main branch" below means them (they were split from the now frozen `1.20.1+` on 2026-11-21).

---

## 1. Project Overview

**Floodlights (FL)** is a port of the Forge 1.7.10 mod *FloodLights* (`oldmod/`, package `de.keridos.floodlights`, mod ID `FloodLights`; gitignored, the only copy is `../../OMTTeam1.20.1+/OpenModularLighting/oldmod`).
This branch builds it for **Minecraft 1.12.2 on Forge** (requested by the user on 2026-10-24) and reuses the main branch's code: the game rules (`core`) come from the shared core `OpenModularCore` (§4 "Core Game Logic Source"), `config/` is the main branch's (converted to Java 8), and the Minecraft-facing code was rewritten for 1.12.2 with the same structure, names and behaviour.
Exception in `config/`: the JSON config framework (`Comment`, `Range`, `ConfigRanges`, `ConfigValidator`, `ConfigWriter`, `ConfigMigration`, with their tests) lives in OMLib on the main branch; OMLib 1.12 has Forge config only, so this branch keeps its own copies.

`oldmod/` is the **read-only reference**. Never edit it and never build it.
It is a source of ideas, values and assets, not a specification: the beam thinning, the persistent light blocks and other details differ on purpose.
Key classes: `tileentity/TileEntityMetaFloodlight` (beam shapes), `tileentity/TileEntityPhantomLight` (source tracking), `tileentity/TileEntityFLElectric` (energy), `handler/ConfigHandler`, `handler/RecipeHandler`.

Core gameplay (the same as on the main branch):

- **Floodlights** light large areas with invisible light blocks ("phantom lights") along a beam: a straight line, a narrow cone or a wide cone. The phantom lights stay in place and are only switched on and off (see §6.3).
- **Grow light**: lights the block in front and gives random growth ticks to a crop two blocks ahead.
- **UV floodlight**: a short beam of UV light blocks that damages living entities standing in it.
- **Fluorescent lights** and the **cube light**: small, cheap lights that illuminate their neighbouring blocks.

### Keeping the branches together

- `core` is not in this repo any more: every Minecraft version uses the same released `OpenModularCore` (`oml-core`, here its Java 8 module `oml-core-j8`), so the rules cannot drift (`../../CLAUDE.md`, "Shared core logic").
  A core change is made there, never here.
- `config/` (and its unit tests) should stay identical to the main branch except for Java 8 syntax (no records, switch expressions, `instanceof` patterns, `List.of`).
  When a change lands on one branch, port it to the other, and tell the user.
  `core.energy` (`EnergyStore`, `EnergyAccess`) is OMLib's core (`omtteam.omlib.core.energy`), which OMLib takes from `omlib-core-j8`.
- Gameplay changes on the main branch usually belong here too; the Minecraft-facing classes have the same names and packages, except that this branch uses the MCP-style names of the OMTTeam 1.12 workspace (decided by the user, 2026-10-25): `blockentity` → `tileentity` (`…BlockEntity` → `…TileEntity`, `ModBlockEntities` → `ModTileEntities`, `CompatBlockEntity` → OMLib's `tileentity.TileEntitySynced`), `menu` → `container` (`…Menu` → `…Container`, `OMLGuiHandler` in `handler`), `client.screen` → `client.gui` (`…Screen` → `…Gui`).
- Never merge this branch into the main branch or the other way round: the build and most of `main` differ completely. Cherry-pick or port by hand.

### Allowed dependencies

- **Forge 1.12.2** (14.23.5.x, chosen by RetroFuturaGradle) and **Forge Energy** (`CapabilityEnergy`, part of Forge; replaces Team Reborn Energy / NeoForge FE, 1 E = 1 FE).
- **OMLib** (required, `required-after:omlib`): the shared library of the OpenModularTurretsTeam mods (`../OMLib` in the OMTTeam 1.12 workspace). Code it already has is used from there, not duplicated: the energy buffer (`core.energy`) and its Forge Energy adapter (`power.EnergyAccessStorage`), its wrench (`ItemMultiTool`, accepted by `CommonTags.isWrench`), the chunk-safe tile entity lookup (`util.world.WorldUtil.getTileEntity`) the camouflage (`ICamoSupport`, the camouflage menu, `CamoRules`, §6.11) the server → client config sync (`config.ConfigSync`, §5) the tile entity base (`tileentity.TileEntitySynced`, §5) the window property sync of the GUIs (`container.WindowValues`, §5), the item capability wrapper (`inventory.AutomationItemHandler`) and the drops on break (`InvUtil.dropItems`).
- Build-time only: **RetroFuturaGradle** (`com.gtnewhorizons.retrofuturagradle`, the maintained 1.12.2 toolchain, from the GTNH maven), the mod publish plugin, Spotless and JUnit 5 (5.13: the last line that runs on Java 8).
- **OpenModularCore** (`com.github.OpenModularTurretsTeam.OpenModularCore:oml-core-j8:<core_version>`, from JitPack, which the build asks only for that group): the shared game rules, downgraded to Java 8 with their Java 9+ API as stubs under `omtteam/openmodularlighting/jvmdg/`, bundled into the jar (§4).
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
- **OMLib** comes from mavenLocal (`omtteam:omlib:1.12.2-<omlib_version>`, `omlib_version` in `gradle.properties`): run `./gradlew publishToMavenLocal` in OMLib first (the version there and in `~/.m2`: `../../CLAUDE.md`, migration step 2). `OML.DEPENDENCIES` says `omlib@[0.0.0,)` in the sources (dev runs, where OMLib's version stays unreplaced); `replaceOmlibRange` compiles a copy of `OML.java` with `[<omlib_version>,<next major>.0.0-alpha)`.
- **Workspace build:** in the OMTTeam 1.12 workspace (the folder above) this `build.gradle.kts` is a subproject of the workspace build, against the OMLib project instead of its mavenLocal jar, and runs with the other mods in `runenv`.
- **RFG and the shared MCP cache:** only matters when switching mappings; see the workspace `CLAUDE.md`, "Workspace gotchas".
- Formatting: Spotless (tabs → spaces, trailing whitespace, final newline), as on the main branch. Run `./gradlew spotlessApply` before committing.
- Language: Java only.

---

## 3. Repository Layout

```text
.
├── CLAUDE.md, CHANGELOG.md, README.md, CONTRIBUTING.md, CLA.md, LICENSE*   # as on the main branch
├── build.gradle.kts            # RetroFuturaGradle, shared core (bundled), tests, Spotless, changelog tasks, publishing
├── settings.gradle.kts
├── gradle.properties           # mod_id, mod_version, mapping version, publishing settings …
├── docs/                       # id-mapping.md, architecture.md (the code of this branch)
└── src/
    ├── main/java/omtteam/openmodularlighting/...   # common and client code (one source set)
    ├── main/resources/         # mcmod.info (templated) and assets/ (models, blockstates, lang, textures, recipes)
    └── test/java/omtteam/openmodularlighting/...   # JUnit tests of the game layer and the config
```

- **Mod ID:** `openmodularlighting`. **Base package:** `omtteam.openmodularlighting` (keep it, as on the main branch).
- 1.12.2 has no split source sets. Client-only code lives in the `client` package and is reached only through the `@SidedProxy` (`CommonProxy` / `client.ClientProxy`). **Main code never references `client` classes.**
- The mod version is `<mod_version>+1.12.2-forge`; `mcmod.info` is templated from `gradle.properties` (`${version}`, `${mod_id}`, `${mod_name}`, `${minecraft_version}`).

---

## 4. Build

### Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk   # Gradle needs 17+
./gradlew build                  # compile, unit tests, spotlessCheck, reobfuscated jar
./gradlew setupDecompWorkspace   # decompiled MC + Forge sources in build/rfg/minecraft-src
./gradlew runClient              # dev client (run dir run/)
./gradlew runServer              # dev server; RetroFuturaGradle asks for the EULA: never accept it for the user
./gradlew spotlessApply
./gradlew release                # checkRelease, build, publishMods, tag <version> (or ./release.sh in the workspace)
```

- The released jar is `build/libs/openmodularlighting-<mod_version>-mc1.12.2-forge.jar` (`<mod_id>-<version>-mc<minecraft_version>-<loader>`, as in the other OMTTeam 1.12 mods), made by `reobfJar` (SRG names). `-dev.jar` is the deobfuscated one, `-sources.jar` the sources. The mod version and the release tag stay `<mod_version>+1.12.2-forge`.
  The release title on CurseForge and Modrinth is the jar name without `.jar` (`publishMods.displayName`, as on the main branch); the published version number is the mod version.
- **Definition of done for any change:** `./gradlew build` succeeds (this includes the unit tests).
- There are **no GameTests** on 1.12.2 (the framework does not exist). Check in-game behaviour by hand with `runClient` (single player needs no EULA), and say what was and was not checked.
- The dedicated server checks the EULA before any mod loads, so it cannot be used for a smoke test without accepting the EULA. Do not accept it on the user's behalf.

### Publishing

- Same plugin, project IDs, token handling and changelog workflow as on the main branch (`CHANGELOG.md`, `./gradlew changelogEntry`, `./gradlew releaseChangelog`).
- The old 1.7.10 CurseForge page is `224728-floodlights`.
- Releases go with the other OMTTeam mods: `./release.sh` in the workspace (after OMLib, whose release OML builds against from mavenLocal) or `./gradlew release` here: `checkRelease` (CHANGELOG section of `mod_version`, `publish_enabled`, tokens, `omlib_version` = `../OMLib`, clean git tree, tag absent, `checkOmlibChangelog`) → `build` → `publishMods` → annotated tag `<mod_version>+1.12.2-forge`.
- `publish_enabled=true` since 1.0.0-beta.6 (decided by the user, 2026-10-24); `-Ppublish_enabled=false` makes a single run a dry run. CurseForge relations: OMLib required, Hwyla optional; Modrinth none until OMLib has a project there.
- `checkOmlibChangelog` runs before every publish task (also dry runs): if `omlib_version` differs from the one at the previous release tag, the version's `CHANGELOG.md` section must contain `OMLib <omlib_version>` ("Requires OMLib <version> or newer" under *Changed*, see the workspace `CLAUDE.md`).
- Changelog entries of this branch describe 1.12.2 changes; the main branch has its own `CHANGELOG.md`.

---

## 6. Where the rest lives (nested `CLAUDE.md` files)

The sections that explain one package or subsystem (§5, §6, "Core Game Logic Source" of §4) were moved into a `CLAUDE.md` in the folder of that code.
A nested `CLAUDE.md` is loaded only when you read files in its folder; read the one for the area you work in before changing it.
The § numbers and headings of the moved sections are kept, so references such as "§6.3" in the text and in the code comments still resolve.

| `CLAUDE.md` in | What it covers (former sections) |
| --- | --- |
| `src/main/java/omtteam/openmodularlighting/` | §6.1 package layout and dependency direction, the intro to §6.2–§6.11; §4 "Core Game Logic Source" (how `oml-core-j8` is bundled and wired in); §5 "1.12.2 specifics" (the table: registration, blockstate, tile entities, ticking, flags, phantom light, drops, interaction, energy, menus, network, disguise, config, NBT) |
| `src/main/java/omtteam/openmodularlighting/block/` | §6.2 blocks and items (id table, recipes, config switches, panel lights), §6.6 interaction |
| `src/main/java/omtteam/openmodularlighting/tileentity/` | §6.3 light mechanics (phantom lights, casting, re-cast, beam shapes), §6.4 light state machine, §6.7 tile entity data, §6.8 grow light, §6.9 UV light damage |
| `src/main/java/omtteam/openmodularlighting/energy/` | §6.5 power and fuel |
| `src/main/java/omtteam/openmodularlighting/config/` | §6.10 config (fields, versioning) |
| `src/main/java/omtteam/openmodularlighting/client/camo/` | §6.11 disguise (camouflage), host in `FloodlightTileEntity`, rendering, tint |
| `src/main/` | §5 "Resources" (assets, blockstates, lang, recipes without datagen, no AI images) |

---

## 7. Change Workflow

1. Implement gameplay logic in the shared core (`OpenModularCore`, with unit tests, for every Minecraft version at once) where possible; keep Minecraft-facing code thin.
2. Check whether the change belongs on the main branch too (§1 "Keeping the branches together").
3. Add assets, lang entries (all three `.lang` files) and recipe JSON by hand.
4. Add a `CHANGELOG.md` entry for user-visible changes.
5. Run `./gradlew build`; check in-game behaviour with `runClient` and say what was checked.

- Every variant of subblocks is an **individual registered block/item** with a lowercase snake_case ID, the same IDs as on the main branch. `docs/id-mapping.md` maps the 1.7.10 IDs to them.

### Designing APIs

As in the workspace `CLAUDE.md` ("Designing APIs"): 2–3 approaches with trade-offs and a recommendation, wait for the user's choice, then repeat per package, class and public signature.

### Code shared with OpenModularTurrets

The camo model and the tile entity base (OMLib's `TileEntitySynced`, the main branch's `CompatBlockEntity`) follow OpenModularTurrets (`../../OMTTeam1.21.11/OpenModularTurrets`) on the main branch; on 1.12.2 they are rewritten for the 1.12 APIs, and the rest of the camouflage is OMLib's (§6.11).
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
- Use `world.isRemote` checks properly; never load client classes on the server.
- All user-facing text goes through translation keys (the 1.12 key forms are in §5, "Resources").
- Prefer small, focused classes.
- Add a short Javadoc to any non-obvious formula.
- Markdown files: one sentence per line, never a line break inside a sentence.
  The sentences of a paragraph go on consecutive lines (no blank line between them), so they render as one paragraph.
  For a visible line break inside a paragraph, end the line with two spaces.
  Exception: `CHANGELOG.md` entries stay one line each.
- Don't add new features or change balance values unless the user asks.

---

## 9. Roadmap / Status

The port is complete (all features of the main branch; build, registration, light blocks, energy, GUIs, disguise, assets, recipes, Hwyla overlay), released with the other 1.12 mods.
Open:

- [ ] Further OMLib reuse: OMLib's `core.machine.MachineMode` instead of the invert toggle is postponed (decided by the user: not how the lights work on the main branch)
- [ ] Checked by hand in a world (`runClient`): placing, beams on/off, modes, GUIs, energy from a mod cable, carbon fuel, UV damage, grow light, disguise rendering, tint, debug tool, dropped items, joining a dedicated server (config sync), the Hwyla overlay
- [ ] The One Probe overlay: ask the user first
