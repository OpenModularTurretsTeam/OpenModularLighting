# Changelog

All notable changes to Open Modular Lighting (Fabric and NeoForge). The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed),
by hand or with `./gradlew changelogEntry --type=added --entry="…"`. Before a release, `./gradlew releaseChangelog`
turns *Unreleased* into the release section; `chiseledPublish` uploads exactly that section.

## [Unreleased]

### Added

- Disguise for the electric, carbon and UV floodlights: put a full, opaque block into the new slot of the GUI and the light looks like that block, with only its lamp showing on the front; the disguise stays on the dropped light
- A "?" in the electric and carbon floodlight GUIs explains the delay before a light can turn back on
- Toggling invert on a light that is still in that delay tells you in chat how many seconds until it turns on

### Changed

- Cube and fluorescent lights turn back on at once, without the 2-second timeout after turning off
- The config file now has a version: config files from older versions get the new default energy use and grow chance, unless the value was changed by hand

## [1.0.0-beta.3] - 2026-10-19

### Added

- NeoForge 1.21.1 version, with the same blocks, energy (FE), item handlers and optional Jade tooltip as the Fabric version
- Cube light: a full block that lights all 6 neighbouring blocks, takes energy from every side and shares it with touching cube lights; crafted from glass, a light bulb and iron, enabled together with the fluorescent lights

### Changed

- File names and the mod version name the loader (e.g. "+1.21.1-fabric"), in preparation for NeoForge builds

## [1.0.0-beta.2] - 2026-10-17

### Added

- Lightproof glass: see-through glass that lets no light through and stops floodlight beams (placeholder texture), crafted from black stained glass and coal; connected textures with Continuity or another OptiFine-compatible CTM mod

### Changed

- Floodlight beams use fewer phantom lights: the straight beam skips every second block (starting with the second); cones thin out too: the centre line like the straight beam, the rest of the cone offset by one from it

### Fixed

- Non-electric floodlight: cone modes use up fuel twice as fast as straight light, and the GUI shows the time left in the current mode

## [1.0.0-beta.1] - 2026-10-17

### Added

- Fluorescent and grow lights that sit directly next to each other and face the same way share their energy: cables fill the whole group, and a light that runs low takes energy from its neighbours

## [1.0.0-alpha.1] - 2026-10-16

### Added

- Config file config/openmodularlighting.json with the 1.7.10 settings, documented in the file and synced from the server on join
- Electric floodlight: straight, narrow cone and wide cone beam, Team Reborn Energy, battery slot, dye colour
- Non-electric (carbon) floodlight burning furnace fuel
- Crafting items: raw and glowing filament, light bulb, fuel dissolver, mantle, non-electric lantern, light debug tool
- Recipes for the crafting items and both floodlights; recipes of blocks disabled in the config are not loaded
- UV floodlight: a beam of UV light that damages living entities (custom damage type)
- Small and square fluorescent lights (the small one can be turned with a stick or wrench)
- Grow light: grows the crop two blocks below it, with normal lighting or black light
- Jade support: lights show invert and mode in the Jade overlay (optional, Jade is not required)
- Light debug tool: while held, light blocks are shown as wireframes (yellow phantom lights, magenta UV lights)

### Changed

- Floodlights keep their light blocks while off and only switch them, instead of placing and removing them
- Lights re-check their beam every 20 seconds on average (random), so it follows world changes that sent no block update

### Fixed

- Light bulbs can be crafted when only the grow light is enabled
