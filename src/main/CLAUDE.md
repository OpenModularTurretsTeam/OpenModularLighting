# Open Modular Lighting 1.12.2, resources and data (`src/main/resources`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md` is the workspace file, `../../CLAUDE.md` the OMM root file, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## Resources (from §5)

- `assets/` is the main branch's, adapted: blockstate `model` names are relative to `models/block/` (`"openmodularlighting:electric_floodlight"`); variant keys list every property; light blocks use `lit=…`, the glass `normal`; the cube light uses a state mapper that ignores `facing`.
- Block textures must be square: the panel textures are 64 × 64 with a transparent lower half (the main branch's are 64 × 32), and the panel models use half the main branch's v coordinates.
- A texture variable must not point to another variable of the same model (1.12 cannot resolve it): name the texture directly.
- `pack.mcmeta` (`pack_format: 3`) must stay: without it Forge substitutes format 2, and Minecraft then looks for `en_US.lang` and ignores the lowercase lang files.
- Lang files are `.lang` (`key=value`): blocks `tile.openmodularlighting.<id>.name`, items `item.openmodularlighting.<id>.name`, the rest as on the main branch.
- **No datagen:** recipes are handwritten JSON in `assets/openmodularlighting/recipes/` (ore dictionary `ingotIron`, `ingotGold`, `dustRedstone`, `paneGlass`; metadata for stained glass and coal/charcoal). They carry the condition `openmodularlighting:block_enabled` (registered in `recipes/_factories.json`, class `compat.BlockEnabledCondition`); recipes load once at start-up from the local config. Smelting is code (`ModRecipes`). There are no recipe-unlock advancements and no loot tables (blocks drop through `getDrops`).
- Do not generate new images via AI. If an image is needed, use a placeholder and tell the user which images are missing.
