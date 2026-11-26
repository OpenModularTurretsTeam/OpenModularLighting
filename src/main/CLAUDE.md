# CLAUDE.md — Open Modular Lighting, resources and data (`src/main/resources`, datagen)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## Resources strategy

- **`assets/`** (textures, models, blockstates, lang, sounds): shared by both loaders.
  Keep them handwritten in shared `src/main/resources/assets/openmodularlighting/`.
  - Textures can be reused from `oldmod/src/main/resources/assets/floodlights/textures/`.
    Rename them to snake_case.
  - Lang: `en_us.json`, `de_de.json` and `ru_ru.json` (converted from the old `.lang` files).
  - Connected textures of `lightproof_glass`: `assets/openmodularlighting/optifine/ctm/lightproof_glass/` holds a `ctm_compact` properties file (Continuity / OptiFine format, no dependency) and the 5 tiles `0.png`–`4.png`, cut from `textures/block/lightproof_glass.png` (32 × 32, 2 px frame): 0 unconnected, 1 fully connected, 2 up/down, 3 left/right, 4 only the frame corners.
    Cut them again if the base texture changes.
  - Every item needs its model in `models/item/<id>.json`.
  - The old IGW wiki text (`wiki/`) is not ported.
- **`data/`** (recipes, loot tables, tags, advancements): folder names and JSON formats differ.
  **Generate them with Fabric Datagen.**
  - Run datagen on the Fabric node (`:1.20.1-fabric:runDatagen`).
    It runs in the server environment (`client = false`): the providers need no client classes.
  - The recipe content is in `OMLRecipeProvider.Recipes`, a plain class with stand-ins for the `shaped` and `shapeless` builders (it inherits `has` from the Fabric provider).
  - Configure the output directory relative to the node project (`project.file("src/main/generated")` → `versions/1.20.1-fabric/src/main/generated`).
  - Add that directory as a resource source dir.
  - Commit the generated files.
  - **Forge has no datagen of its own.** Its build script calls `convertFabricData` (in `omlib/build-logic`), which copies the Fabric output of the node named by `datagen_from` (`1.20.1-fabric`) into the loader's resources at build time, turning `fabric:load_conditions` into Forge's `conditions`.
    Only conditions in this mod's namespace convert (registered under the same id on both loaders); any other condition fails the build.
    So regenerate the Fabric data and the Forge jar follows; nothing loader-specific is committed.
- Do not handwrite files into `data/` in shared resources.
- Never generate textures or other art (only GUI textures may be generated); use a placeholder image and tell the user which images are missing.
