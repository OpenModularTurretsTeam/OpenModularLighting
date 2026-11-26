# CLAUDE.md — Open Modular Lighting, client source set (`src/client/java/omtteam/openmodularlighting/client`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.1 Package layout: the client source set

```text
src/client/java/omtteam/openmodularlighting/client
├── OMLClient                      shared client start-up (init, registerScreens(ScreenRegistrar), appendTooltip)
├── platform/                      the loader client entrypoints: OMLFabricClient (ClientModInitializer), OMLForgeClient
│                                  (constructed by OMLForge); each wires screens,
│                                  tint, render layers, tooltip and the debug renderer to its loader's events
├── screen/                        FloodlightScreen (one status line) → Electric…, CarbonFloodlightScreen
├── render/                        FloodlightTint (dye colour × a brightness factor per block), LightBlockDebugRenderer
│                                  (light debug tool: wireframes around light blocks while held; called from the
│                                  loader's world render event, not a block entity renderer, so it costs nothing while
│                                  not held)
└── compat/                        jade/ (client half of the Jade plugin: OMLJadeClientPlugin, FloodlightJadeComponent)
```

## 5. Compat Helpers: client

| Difference | Helper |
| --- | --- |
| Container screen background, GUI drawing, world lines | `client.compat.ClientCompat`: `renderContainerBackground`, `blit` (256 × 256 GUI textures), `renderTooltip`, `lines()` / `renderLineBox` |
| Client start-up (screens, tooltip lines) | `client.OMLClient` (`init`, `registerScreens(ScreenRegistrar)`, `appendTooltip`); tint `FloodlightTint.color` / `tintedBlocks`; debug wireframes `LightBlockDebugRenderer.render(level, poseStack, buffers, camera)`. The loader client entrypoints wire them to their events. |
| Render layers | Fabric `BlockRenderLayerMap` in `OMLFabricClient`; Forge `"render_type"` in the block model JSON (Fabric ignores it). Keep both in sync. The floodlights (`CamoBlock`s) are cutout because of their lamp overlay (§6.11). |

## Jade (details of the optional dependency)

These are the details of the Jade bullet under "Allowed dependencies" in the top-level file.

Only `compat/jade/` (`OMLJadePlugin`, `FloodlightJadeProvider`: server data) and `client/compat/jade/` (`OMLJadeClientPlugin`, `FloodlightJadeComponent`: the tooltip) may import `snownee.jade`; Jade loads `OMLJadePlugin` through the `jade` entrypoint (Fabric) or the `@WailaPlugin` annotation (Forge), so the mod runs without Jade.
The tooltip half is in the client source set because Jade's tooltip classes are client classes.
Jade creates every plugin it finds on dedicated servers too, where the client class cannot be loaded (Fabric refuses it), so `OMLJadeClientPlugin` is not registered with Jade: `OMLJadePlugin.registerClient`, which Jade calls only on a client, creates it by name and forwards to it.
The Forge `client` source set gets Jade through `clientCompileOnly` extending `compileOnly`.
