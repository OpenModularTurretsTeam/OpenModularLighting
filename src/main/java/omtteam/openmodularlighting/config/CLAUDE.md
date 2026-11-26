# CLAUDE.md — Open Modular Lighting, config (`config/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.10 Config

- No config library.
  Write a minimal Gson-based JSON config at `config/openmodularlighting.json`.
- Load it on init, write defaults if missing, and validate ranges.
- Fields are documented with `@Comment` and range-checked with `@Range` (package `config`).
  `ConfigWriter` writes them as `//` comments (JSONC) above each key, together with the default, and `ConfigRanges` clamps on load.
  Add both annotations to every new config field.
- Server-authoritative values that clients need (e.g. for tooltips) are synced on join.
- Read values with `OMLConfig.get()` (for example `OMLConfig.get().general.rangeStraightFloodlight`).
  Never cache them: on a client connected to a server, `get()` returns the server's config, and after disconnecting it returns the local one again.
  Client settings that each player chooses (`uvLightRendered`) are read with `OMLConfig.local()`, the instance's own file, so a server's value never overrides them.
- **Versioning** (as in OpenModularTurrets): the file records `configVersion`; files without it are version 1. On load, `ConfigMigration` (OMLib, steps in `ConfigMigrationSteps`) replaces values that still equal an old default with the new one, then saves the file with the current version, so it runs once per file and values an owner chose stay.
  **When you change a default, add a step to `ConfigMigrationSteps`** (the current version follows the last step; test it in `ConfigMigrationStepsTest`).
- New fields go into `ConfigData` (sections `blocks` and `general`).
  Only `int`, `double`, `boolean` and `List<String>` are supported.
  Unknown keys are removed and wrong types reset to the default on load.

Fields (defaults and ranges are only in `ConfigData`, in `@Range` and the field initialisers; do not repeat them in docs):

| Field | Meaning |
| --- | --- |
| `electricFloodlightEnabled`, `smallElectricFloodlightEnabled`, `carbonFloodlightEnabled`, `uvFloodlightEnabled`, `growLightEnabled` | enable block (recipes) |
| `configVersion` | layout version of the file, for `ConfigMigration`; not meant to be edited |
| `energyUsage` | E/t electric floodlight (×2 for cones) |
| `energyUsageSmallFloodlight` | E/t fluorescent lights |
| `energyUsageCubeLight` | E/t cube light |
| `energyUsageUVFloodlight` | E/t UV floodlight |
| `energyUsageGrowLight` | E/t grow light |
| `carbonTime` | seconds one coal lasts (straight) |
| `rangeStraightFloodlight` | straight beam range |
| `rangeConeFloodlight` | cone range (wide cone = ¼) |
| `rangeUVFloodlight` | UV beam range |
| `chanceGrowLight` | growth chance per second |
| `timeoutFloodlights` | ticks before a light can turn on again |
| `damageUVFloodlight` | UV damage per second |
| `uvLightRendered` | client: show UV light blocks (each player's own setting, read with `OMLConfig.local()`) |

`IGWNotifierEnabled` is dropped.
