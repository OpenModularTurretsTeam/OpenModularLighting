# CLAUDE.md — Open Modular Lighting, blocks, items and interaction (`block/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.2 Blocks and items (from 1.7.10)

Every former metadata variant is its own block.
Record old → new IDs in `docs/id-mapping.md`.

| Old ID (1.7.10) | New ID | Notes |
| --- | --- | --- |
| `electricFloodlight` | `electric_floodlight` | modes: straight / narrow cone / wide cone; GUI with battery slot |
| `carbonFloodlight` | `carbon_floodlight` | burns furnace fuel instead of energy; GUI with fuel slot |
| `uvFloodlight` | `uv_floodlight` | places UV light blocks that damage entities; no mode, no timeout |
| `growLight` | `grow_light` | modes: normal lighting / black light (no light, growth only); always placed facing down; energy only from the back |
| `smallElectricFloodlightMetaBlock` meta 0 | `small_fluorescent_light` | lights 5 neighbours; 6 px strip, `rotated` blockstate; energy only from the back |
| `smallElectricFloodlightMetaBlock` meta 1 | `square_fluorescent_light` | same, full square, not rotatable; shapeless recipes convert between the two |
| – (new) | `cube_light` | full block, lights its 6 neighbours (`BeamShape.cube`); energy from every side, shared with touching cube lights; `energyUsageCubeLight`, enabled with the fluorescent switch |
| – (new) | `lightproof_glass` | see-through glass that lets no light through and stops beams (opaque `Cell`); connected textures (§5 "Resources strategy"); no config switch |
| `tilePhantomLight` | `phantom_light` | invisible, no collision, replaceable; `LIT` → light level 15, else 0 (§6.3) |
| `tileUVLight` | `uv_light` | `LIT` → light level 4 and damages living entities, else 0 and harmless; visibility configurable |
| items: `rawFilament`, `glowingFilament`, `electricIncandescentLightBulb`, `carbonDissolver`, `carbonLantern`, `mantle`, `lightDebugTool` | `raw_filament`, `glowing_filament`, `light_bulb`, `carbon_dissolver`, `carbon_lantern`, `mantle`, `light_debug_tool` | crafting items; the debug tool is creative only (no recipe); while it is held, light blocks get wireframes (phantom yellow, UV magenta, dimmer while unlit), as with the 1.7.10 `TileEntityPhantomLightRenderer` |

Recipes (`datagen.OMLRecipeProvider`): raw filament (glowstone, redstone, gold) smelts into glowing filament → light bulb → electric/UV/grow lights, fluorescent lights and the cube light.
Mantle + carbon dissolver + flint and steel → carbon lantern → carbon floodlight.

- Iron, gold, redstone and glass panes use the common `c:` tags (`compat.CommonTags`); the other ingredients are vanilla items.
- **Config switches:** every recipe carries the resource condition `openmodularlighting:block_enabled` (`compat.BlockEnabledCondition`; registered by `platform/fabric/FabricBlockEnabledCondition`, `platform/neoforge/NeoForgeBlockEnabledCondition`; a different API on each loader) with the registry paths of the blocks that need it.
  It loads if any of them is enabled (`ConfigData.Blocks.isEnabled`).
  Datagen adds the condition to the recipe-unlock advancements too.
- Filament, glowing filament and light bulb are needed by electric floodlight, fluorescent lights, cube light, UV floodlight **and grow light**.
  Without the grow light in this list, bulbs could not be crafted with only the grow light enabled.
- Lightproof glass: shapeless, 1 black stained glass + 1 coal (`minecraft:coals`, so charcoal works too) → 1. It has no config switch, so its recipe carries no `block_enabled` condition (`Saver.saveAlways`).
- Also: fluorescent lights (`" G "`, `"GBG"`, `"III"` → 2, plus the shapeless conversion between small and square), UV floodlight (floodlight frame with black stained glass), grow light (`"III"`, `"CBC"`, `"GGG"` with pink stained glass).

**Panel lights (fluorescent, grow).** They are plain JSON block models, not block entity renderers: a 3 px panel on the back face, with a 64 × 32 box texture mapped onto it, and the dye tint as on the floodlights.
The models are built facing up and turned per facing like vanilla's `end_rod` (`block.PanelShapes` uses the same table for the outlines).
The panels have no collision, only an outline.

## 6.6 Interaction

| Action | Effect |
| --- | --- |
| Sneak + right-click, empty hand | toggle redstone invert (chat message "Inverted: true/false"; if the light now waits for its timeout, also "The light will turn on in x seconds") |
| Right-click with a stick or wrench | floodlights and grow light: cycle the mode (chat message "Mode: …"); small fluorescent light: turn it by 90°; square fluorescent light, UV floodlight and cube light: nothing to set, the GUI opens |
| Sneak + right-click with a wrench | break the block and drop it |
| Right-click with dye | set render colour (dye index); right-click with any wool resets it (colour 16 = no tint) |
| Right-click with the light debug tool | fills the energy buffer of an electric light (debug aid) |
| Right-click (anything else) | open the GUI: carbon GUI for the carbon floodlight, the shared electric GUI (energy + battery slot) for all electric lights; the electric, carbon and UV floodlights also get a "Camouflage" button that opens the camouflage menu (§6.11) |

Use the convention tag `compat.CommonTags.WRENCHES` (`c:tools/wrench`) instead of mod-specific wrench checks.
A stick always works for the mode, but only a wrench dismantles.
Sneaking with any other item passes, so the item can be used (e.g. placing a block).
Dye is not consumed; any wool resets the colour.
Messages go to chat (`displayClientMessage(…, false)`).
Interaction goes through OMLib's `InteractiveBlock`, so it is the same on every loader and version.
