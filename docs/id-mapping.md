# ID mapping: FloodLights 1.7.10 → Open Modular Lighting

The 1.7.10 mod (`FloodLights`, namespace `floodlights`) used camelCase ids and one block with metadata for both
fluorescent lights. Every variant is now its own block with a snake_case id in the `openmodularlighting` namespace.

There is no world conversion: 1.7.10 worlds cannot be loaded in 1.20.1 / 1.21.1 anyway. The table is for players,
pack makers and anyone porting configs or recipes.

## Blocks

| 1.7.10 id | Metadata | New id | Notes |
| --- | --- | --- | --- |
| `floodlights:electricFloodlight` | – | `openmodularlighting:electric_floodlight` | |
| `floodlights:carbonFloodlight` | – | `openmodularlighting:carbon_floodlight` | "Non-electric Floodlight" |
| `floodlights:uvFloodlight` | – | `openmodularlighting:uv_floodlight` | |
| `floodlights:smallElectricFloodlightMetaBlock` | 0 | `openmodularlighting:small_fluorescent_light` | `rotationState` → blockstate `rotated` |
| `floodlights:smallElectricFloodlightMetaBlock` | 1 | `openmodularlighting:square_fluorescent_light` | |
| `floodlights:growLight` | – | `openmodularlighting:grow_light` | |
| `floodlights:tilePhantomLight` | – | `openmodularlighting:phantom_light` | no item |
| `floodlights:tileUVLight` | – | `openmodularlighting:uv_light` | no item |
| – | – | `openmodularlighting:lightproof_glass` | new; see-through glass that blocks light and floodlight beams |

Block entities use the block id, except that both fluorescent lights share `openmodularlighting:fluorescent_light`.

## Items

| 1.7.10 id | New id |
| --- | --- |
| `floodlights:rawFilament` | `openmodularlighting:raw_filament` |
| `floodlights:glowingFilament` | `openmodularlighting:glowing_filament` |
| `floodlights:electricIncandescentLightBulb` | `openmodularlighting:light_bulb` |
| `floodlights:carbonDissolver` | `openmodularlighting:carbon_dissolver` |
| `floodlights:carbonLantern` | `openmodularlighting:carbon_lantern` |
| `floodlights:mantle` | `openmodularlighting:mantle` |
| `floodlights:lightDebugTool` | `openmodularlighting:light_debug_tool` |

## Other ids

| 1.7.10 | New |
| --- | --- |
| cactus damage from UV light | damage type `openmodularlighting:uv_light` |
| config `config/FloodLights.cfg` (Forge) | `config/openmodularlighting.json` (same option names, see the comments in the file) |
| creative tab `floodlights` | `openmodularlighting:main` |
