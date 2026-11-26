# CLAUDE.md — Open Modular Lighting, resources and data (`src/main/resources`, datagen)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## Resources strategy

- **`assets/`** (textures, models, blockstates, lang, sounds): shared by both loaders.
  Keep them handwritten in shared `src/main/resources/assets/openmodularlighting/`.
  - Textures can be reused from `oldmod/src/main/resources/assets/floodlights/textures/`.
    Rename them to snake_case.
  - Lang: convert `oldmod/.../lang/*.lang` (en_US, de_DE, ru_RU) to `en_us.json`, `de_de.json` and `ru_ru.json` with the new keys.
  - Connected textures of `lightproof_glass`: `assets/openmodularlighting/optifine/ctm/lightproof_glass/` holds a `ctm_compact` properties file (Continuity / OptiFine format, no dependency) and the 5 tiles `0.png`–`4.png`, cut from `textures/block/lightproof_glass.png` (32 × 32, 2 px frame): 0 unconnected, 1 fully connected, 2 up/down, 3 left/right, 4 only the frame corners.
    Cut them again if the base texture changes.
  - Every item needs an item model definition in `items/<id>.json` that points at its `models/item/<id>.json`; without one, the item shows the missing texture and logs nothing.
    A new item gets both files.
  - The old IGW wiki text (`wiki/`) is not ported.
- **`data/`** (recipes, loot tables, tags, advancements): folder names and JSON formats differ.
  **Generate them with Fabric Datagen.**
  - Run datagen on the Fabric node (`:1.21.11-fabric:runDatagen`).
    It runs in the server environment (`client = false`): the providers need no client classes, so a new version's data can be generated before its client code is ported.
  - The recipe content is the same on every version (`OMLRecipeProvider.Recipes`); the builders that need the item registry (`shaped`, `shapeless`, `has`) come from the vanilla `RecipeProvider`, which `Recipes` extends.
  - Configure the output directory relative to the node project (`project.file("src/main/generated")` → `versions/1.21.11-fabric/src/main/generated`).
  - Add that directory as a resource source dir.
  - Commit the generated files.
  - **NeoForge has no datagen of its own.** Its build script calls `convertFabricData` (in `omlib/build-logic`), which copies the Fabric output of the node named by `datagen_from` (`1.21.11-fabric`) into the loader's resources at build time, turning `fabric:load_conditions` into `neoforge:conditions` (`condition` → `type`).
    Only conditions in this mod's namespace convert (registered under the same id on both loaders); any other condition fails the build.
    So regenerate the Fabric data and the NeoForge jar follows; nothing loader-specific is committed.
- Do not handwrite files into `data/` in shared resources.
- Do not generate new images via AI, f.ex. for textures.
  If an image is needed, use a placeholder image and tell the user which images are missing.
