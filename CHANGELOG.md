# Changelog

All notable changes to Open Modular Lighting (Fabric). The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed),
by hand or with `./gradlew changelogEntry --type=added --entry="…"`. Before a release, `./gradlew releaseChangelog`
turns *Unreleased* into the release section; `chiseledPublish` uploads exactly that section.

## [Unreleased]

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
