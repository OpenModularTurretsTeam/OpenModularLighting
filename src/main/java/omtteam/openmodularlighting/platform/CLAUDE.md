# CLAUDE.md — Open Modular Lighting, loaders (`platform/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## Loaders

Each Stonecutter node is `<minecraft version>-<loader>`; the loader picks the build script (`build.<loader>.gradle.kts`).
Loader differences are handled in two ways:

- **Whole loader-specific classes** go into a `platform/<loader>/` package (`platform/fabric/`, `platform/neoforge/`, also under `client/` and the test mod).
  `mod-common` leaves the other loader's `platform/` package out of the compilation, so these classes need no Stonecutter comments.
  Examples: entrypoints, capability/API registration, event wiring.
- Shared code reaches the loader through the small service interfaces in OMLib's `platform` (design new ones with the user, see §7 "Designing APIs"; they are added in OMLib).
- **Small differences** inside a shared class use the loader constants: `//? if fabric {` / `//? if neoforge {` (set in `stonecutter.gradle.kts`), with separate import blocks because the packages differ.
  Keep them inside `compat/` and `platform/`, never in gameplay code.
- `core` stays loader-free; it comes from OpenModularCore, whose build checks that.
- **Everything compiles on NeoForge except `datagen`,** which is Fabric-only by design (NeoForge gets the converted Fabric output, §5 "Resources strategy").
- **Start-up:** both entrypoints call `OML.init()` (config, packets) first.
  Fabric then runs `OML.registrationSteps()` in order; NeoForge runs each `RegistrationStep` in the `RegisterEvent` of its registry and throws at common setup if a step never ran.
  A new registry class gets a step in that list, not a call in an entrypoint.
