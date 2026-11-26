# CLAUDE.md — Open Modular Lighting, disguise (camouflage) (`camo/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.11 Disguise (camouflage)

Taken over from OpenModularTurrets; the shared part (menu, screen, packets, `CamoHost`, `CamoData`, the disguise models) is in OMLib since 2026-10-27, and the differences are parameters (see §7 "Code shared with OpenModularTurrets").

- **Which lights:** the electric, carbon and UV floodlights (`camo.CamoBlock`).
  Not the panel lights (thin, no full face to disguise) and not the cube light (no front, it would vanish completely).
- **Valid disguises** (`CamoRules.isValidDisguise`): full, opaque blocks only (`isSolidRender`), drawn from a normal model, without a block entity.
  A floodlight hides its neighbours' faces like any solid block, so a see-through disguise would show holes into the world.
  The disguise is the default state of the item's block.
  An invalid disguise read from NBT is dropped.
- **Setting it:** the "Camouflage" button of the floodlight GUI (only on `CamoBlock`s) opens the camouflage menu, the same menu and screen as in OpenModularTurrets (`CamoMenu`, `CamoScreen`, requested by the user 2026-10-27 so the GUI looks the same as there and in 1.12.2): a phantom slot for the disguise, "Back" to the floodlight GUI.
  Clicking the slot with a block sets the disguise (the block stays in the cursor), with an empty cursor removes it; shift-clicking a block in the inventory sets it too.
  Only the server changes it; anyone who may open the GUI may set it.
  `FloodlightBlockEntity` is a `CamoHost` without light sliders (`supportsLight()` is false: the lights have their own light logic) whose parent menu is the floodlight GUI; the packets are OMLib's `OMLibPackets.OPEN_CAMO_MENU`, `CAMO_BACK` (and `CAMO_LIGHT`, never used here).
- **Data:** `FloodlightBlockEntity.camo` (`CamoData`), part of the settings: saved, sent in the update tag, kept on the dropped item.
- **Rendering:** the client wraps the models of the `CamoBlock`s (all OMLib: `client.camo.CamoModels`; Fabric `FabricCamoModel` through the Renderer API, NeoForge `NeoForgeCamoModel` through `ModelData`).
  OMLib's client entrypoints do the wrapping, by block state.
  Both models are block state models: Fabric's `WrapperBlockStateModel` with per-quad render layers, NeoForge's `DelegateBlockStateModel` with the disguise's parts.
  With a disguise they emit the disguise's model (its own render layer, ambient occlusion and tint) and, of the light's own model, only the quads with the block's `CamoBlock.overlayTintIndex()` (the floodlights return `FloodlightBlock.LAMP_TINT_INDEX`, 1; blocks that return -1 show nothing of their own), the **lamp quads**: an extra front-face element in `models/block/floodlight.json` with `tintindex: 1` and the `#lamp` texture (`<light>_lamp[_on].png`, the ring and disc cut out of the front texture with the rest transparent).
  Without a disguise the overlay lies over the identical pixels of the front face, so the floodlights are in the cutout layer.
  Cut the lamp textures again if a front texture changes.
- **Tint:** the lamp keeps the dye colour (`FloodlightTint`).
  Fabric bakes the disguise's tint into its quads; on NeoForge `FloodlightTint` forwards every tint index other than 1 to the disguise's colour.
- Jade, the pick-block item and the break particles still show the real floodlight (the disguise is cosmetic, unlike OMT's hidden turrets).

## Differences from OpenModularTurrets' camouflage (from "Code shared with OpenModularTurrets")

The camouflage differences are parameters of the library classes: the disguise rule (`CamoRules.RULES`: OpenModularTurrets any full block, here full opaque blocks only), the quads of the block's own model on top of the disguise (`CamoBlock.overlayTintIndex()`: here the lamp), the NBT key and read validation (`new CamoData("camo", CamoRules::isValidDisguise)` here, `"Camo"` there; the key must stay per mod, or saved disguises are lost) and the buttons of the screen (`CamoHost.hasParentMenu()` for "Back"; `CamoScreenExtras` for hosts with more).
