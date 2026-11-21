# Open Modular Lighting

Floodlights for **Fabric** (Minecraft 1.20.1, 1.21.1 and 1.21.11), **NeoForge** (Minecraft 1.21.1 and 1.21.11) and **Forge** (Minecraft 1.20.1 and 1.12.2).
This branch (`1.12`) builds it for **Forge** on **Minecraft 1.12.2**; the other versions are built from the branch `1.20.1+`.
On 1.12.2 lights use Forge Energy, recipes use the ore dictionary, and the block info overlay is Hwyla instead of Jade.
Open Modular Lighting is a port of the Forge 1.7.10 mod *FloodLights* by Keridos.
It lights up large areas with invisible light blocks placed along a beam, so you can light a cave, a farm or a base without placing torches everywhere.

> Status: stable since 1.0.0.

## Features

### Lights

| Block | What it does | Power |
| --- | --- | --- |
| **Electric Floodlight** | Casts a beam of light in the direction it faces. Three modes: **straight** (64 blocks), **narrow cone** (32 blocks) and **wide cone** (8 blocks). | Energy: 10 E/t straight, 20 E/t in the cone modes |
| **Non-electric Floodlight** | Like the electric floodlight, but burns furnace fuel instead. One coal lasts 300 s in straight mode and half as long in the cone modes. | Any furnace fuel (vanilla or modded) |
| **UV Floodlight** | Casts a short beam (8 blocks) of dim UV light that hurts living things standing in it, 4 damage per second. | Energy: 60 E/t |
| **Grow Light** | Lights the block below it and makes the crop two blocks below grow faster (about one growth step every 10 s). A black-light mode gives the growth without the light. | Energy: 25 E/t |
| **Small / Square Fluorescent Light** | Cheap little panels that light up the 5 blocks next to them. The small one can be turned by 90°, and a shapeless recipe converts between the two. | Energy: 1 E/t |
| **Cube Light** | A full block that lights all 6 blocks around it. | Energy: 2 E/t, accepted from every side |
| **Lightproof Glass** | See-through glass that lets no light through and stops light beams, with connected textures when OptiFine is installed. Made from black stained glass and coal or charcoal. | – |

All lights turn on with a **redstone signal**.
The signal can be inverted, so that a light is on without redstone and turns off when powered.
After a light turns off, it waits 2 seconds before it can turn on again, to prevent flickering.

### Using the lights

| Action | Effect |
| --- | --- |
| Right-click | Opens the GUI: energy and a battery slot, or the fuel slot and remaining burn time |
| Right-click with a stick or wrench | Cycles the mode (floodlights, grow light) or turns the small fluorescent light |
| Sneak + right-click with an empty hand | Inverts the redstone signal |
| Sneak + right-click with a wrench | Picks the light up |
| Right-click with dye | Tints the light in that colour; wool removes the tint |

The electric, non-electric and UV floodlights can be **disguised**: click the disguise slot in their GUI with a full, opaque block (stone, planks, bricks...) and the light looks like that block, with only its lamp showing on the front.
Click the slot with an empty hand to remove the disguise.

A light you break keeps its settings (mode, invert, colour, disguise, stored energy or burn time) on the item, and the tooltip shows "Configured".
The beam follows changes in the world.
It stops at solid blocks and shines through glass and plants.
When you build into a beam or remove a block from it, the beam adjusts.

### Power

- Electric lights take **Forge Energy** (FE, 1 FE = 1 E) from cables and generators of other tech mods.
  Each light stores 50 000 E.
  Fluorescent and grow lights only accept energy from the back, cube lights from every side.
- Fluorescent and grow lights that touch and face the same way share their energy, and so do touching cube lights.
- Batteries in the battery slot charge the light: any item that gives off Forge Energy.
- Hoppers and pipes can insert fuel and batteries, but can't take anything out.

### Crafting

Smelt **Raw Filament** (glowstone, redstone, gold) into **Glowing Filament**.
Craft it into an **Electric Incandescent Light Bulb**, the part needed for every electric light.
The non-electric floodlight needs a **Non-electric Lantern**, made from a Mantle, a Fuel Dissolver and flint and steel.
The **Cube Light** is made from glass, a light bulb and iron.
All recipes are shown in the recipe book or in a recipe viewer.

The **Lightblock Debug Tool** (creative only) shows the invisible light blocks while you hold it: yellow for normal light, magenta for UV light.

### Config

The settings are in `config/openmodularlighting.json`, with a comment above each one.
Ranges, energy use, burn time, grow chance, UV damage and the timeout can be changed, and each light can be disabled (its recipe is not loaded).
On a server, the server's settings are sent to the players.

### Optional: Hwyla

With [Hwyla](https://www.curseforge.com/minecraft/mc-mods/hwyla) installed, looking at a light shows its mode and whether it is inverted.
Hwyla is not required, and the overlay can be turned off in Hwyla's settings.

## Requirements

- Minecraft 1.12.2 (Java 8) with [Forge](https://files.minecraftforge.net/) for 1.12.2 (the mod is built against 14.23.5.2847)
- [OMLib](https://www.curseforge.com/minecraft/mc-mods/omlib) 4.0.0 or newer, below 5.0.0

The requirements of the other Minecraft versions are in the README of the branch `1.20.1+`.

## Building from source

Gradle has to run on **JDK 25** (RetroFuturaGradle 2.0.6 needs it): set `JAVA_HOME` to a JDK 25 before you run `./gradlew`.
The Java 8 toolchain for compiling, testing and running the game is downloaded automatically if it is missing.
The mod builds against OMLib's dev jar from your local Maven repository, so OMLib is built and published first.

```bash
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OMLib.git
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OpenModularLighting.git
(cd OMLib && ./gradlew publishToMavenLocal)   # OMLib 4.0.1 into ~/.m2
cd OpenModularLighting
./gradlew build -Pomlib_version=4.0.1        # jars, unit tests and the formatting check
./gradlew runClient -Pomlib_version=4.0.1    # start a test client
```

`-Pomlib_version` names the OMLib version you published, when it differs from `omlib_version` in `gradle.properties`.
The jars end up in `build/libs/`; `openmodularlighting-<version>-mc1.12.2-forge.jar` is the one players install.
See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the full setup, the shared core and the tests, and `CHANGELOG.md` for the changes.

## License

The following is the content of [`LICENSE-DUAL`](LICENSE-DUAL):

```text
The source code of this project is licensed under the MIT license [1].
The projects art is licenced under the CC-BY-NC-SA 4.0 license [2].

[1]https://mit-license.org/
[2]https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt
```

The full license texts are in [`LICENSE`](LICENSE) (MIT, source code) and [`LICENSE-ART`](LICENSE-ART) (CC BY-NC-SA 4.0, textures, models and other art).
