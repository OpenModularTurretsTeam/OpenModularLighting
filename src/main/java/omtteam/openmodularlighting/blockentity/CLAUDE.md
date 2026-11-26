# CLAUDE.md — Open Modular Lighting, light mechanics and block entities (`blockentity/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md`, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.3 Light mechanics

> **Light blocks are not placed and removed on every switch.** Placing and removing blocks (with their block entities) is expensive, so **phantom light blocks stay in the world while their floodlight exists. Turning a light on or off only switches the `LIT` blockstate of its phantom lights (light level 15 ↔ 0).**

Light is made by **phantom light blocks** placed in air along the beam.
Each phantom light has a `LIT` blockstate (`lightLevel = LIT ? 15 : 0`) and a block entity that stores which floodlights reach it and which of them are on.

### Lifecycle

- **Floodlight placed** (or loaded without a cached shape): cast the beam and place a phantom light on every reachable air block, `LIT = false` unless the floodlight is already on.
  Remember the cast positions in the floodlight BE (see §6.7).
- **Floodlight turns on / off:** walk the cached positions only, without re-walking rays or reading other blocks.
  Mark this source on/off in each phantom light.
  A phantom light is `LIT` if at least one of its sources is on, and it changes its blockstate only when that result changes.
- **Mode change:** cast the new shape and **diff** it against the cached positions.
  Remove this source from positions that drop out, and add it to new ones (placing phantom lights where needed).
  Leave unchanged positions alone.
- **Floodlight broken** (`CompatBlockEntity.onBlockRemoved`; not `setRemoved`, which also runs on chunk unload): remove this source from all cached positions.
  A phantom light with **no sources at all** removes itself (set to air).
  This is the only time phantom lights are removed in normal play.

### Casting

- Walk each ray outward.
  On air: place a phantom light, or reuse one at that position, and add this source.
  On an existing phantom light: add this source.
  On an opaque block: stop the ray.
  Non-opaque blocks (glass, plants) are passed through without placing light.
  UV light blocks are passed through without placing light.
- **Unloaded chunks:** never place or read blocks in unloaded chunks (avoids cascading chunk loads).
  Stop the ray instead.

### Keeping the beam correct when the world changes

- **Re-cast:** when a phantom light is replaced, or a block next to it changes, it flags its sources for a re-cast.
  The floodlight re-casts at most once per tick, diff-based like a mode change.
  Never re-cast inside the neighbour-update callback itself.
  The floodlight also listens to its own neighbours (`FloodlightBlock.onNeighborChanged`): the block in front may hold no light block that would notice a change (a beam blocked right in front, thinning), so it re-casts when one of its six neighbours is no longer what it was at the last cast (`LightBeam.neighboursChanged`, by `Cell`; a redstone signal changes nothing, so lights on redstone do not re-cast on every switch).
- **Periodic re-check:** every light also flags itself for a re-cast at random intervals, uniform in 10–30 s (mean 20 s, `core.state.RecheckTimer`, not saved, so lights loaded together are staggered).
  This catches world changes that sent no block update (chunks edited while unloaded, mods setting blocks without updates).
- **Block updates:** place, switch and remove light blocks only through `world.LightBlocks`, which uses `LightBlocks.FLAGS` = `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`: no neighbour updates and no shape updates.
  Otherwise every toggle would cause neighbour updates on the next phantom light and start re-cast chains.
- **Orphans and stale state:** phantom light BEs do **not** tick (there can be thousands).
  Instead the block uses random ticks (`isRandomlyTicking`).
  On a random tick the phantom light drops sources whose block entity is gone, removes itself if none are left, and corrects `LIT` if it disagrees with its sources (`LightBlocks.refresh`, also when the sources did not change but the blockstate was set by a command).
  This also fixes phantom lights in chunks that were unloaded while their floodlight toggled.
  Only query sources in loaded chunks; keep sources in unloaded chunks as they are.

**Phantom light block properties.** It must behave like air for everything except light, because it now also sits in the world while dark:

- no collision, no outline/selection shape, `RenderShape.INVISIBLE`, no drops, not in the creative tab;
- replaceable: `Properties.replaceable()`, and in the `minecraft:replaceable` and `minecraft:replaceable_by_trees` tags (datagen), so players can place blocks into it and trees, fluids and falling blocks treat it as air;
- `PushReaction.DESTROY`; it does not block mob spawning (an unlit phantom light must leave dark areas spawnable);
- strength 0 (`instabreak`), not unbreakable: a high explosion resistance would make phantom lights stop explosions, because explosions only skip real air;
- not `Properties.air()`: air blocks get no random ticks (chunk sections do not count them);
  so it is not `isAir()` either, and vanilla checks that ask for air see a block: sugar cane, cactus and bamboo do not grow into a phantom light, and a nether portal frame with one inside does not light (a known limitation while the floodlight exists, found on 2026-11-02);
- no item; the block entity sends nothing to clients (the state is the `LIT` blockstate).

Beam shapes (defined along +X, then rotated to the facing):

| Shape | Range | Pattern |
| --- | --- | --- |
| Straight | `rangeStraightFloodlight` | single ray along the facing |
| Narrow cone | `rangeConeFloodlight` | 8 outer rays spreading `i/2` sideways (orthogonal + diagonal), plus 8 inner rays from distance 8 spreading `i/4`. An inner ray is skipped if its outer ray was blocked before distance 8. Aborts if the block in front is blocked. |
| Wide cone | `rangeConeFloodlight / 4`, at least 1 | 8 outer rays spreading `i` sideways, plus 8 inner rays from distance 4 spreading `i/2`. Aborts if the block in front is opaque. |
| Fluorescent | 1 | front + the 4 sideways neighbours |
| Cube light | 1 | all 6 neighbours |
| Grow light | 1 | front block only (only in "normal lighting" mode) |
| UV | `rangeUVFloodlight` | straight ray of `uv_light`. Replaces phantom lights it meets (their sources are flagged for a re-cast). |

**Thinning (`core.beam.Spacing`).** To save phantom lights, `BeamMode.shape` leaves out light blocks at some distances along the facing direction (beam-space x).
The rays are still followed over every block, so walls stop them exactly as without thinning.
Straight: lights at 1, 3, 5, … (the second block is the first one skipped).
Both cones start at the first block and keep their full geometry.
Their centre line is thinned like the straight beam (1, 3, 5, …); the rest of the cone is offset by one from it and skips the odd distances (2, 4, 6, …).
Fluorescent, grow and UV beams are not thinned.
`BeamCasterTest` compares against the reference loops (`BeamTest`) filtered by the same spacing.
Consequence: a block change next to a skipped position may have no phantom light beside it, so the light then notices only at its periodic re-check (§6.3).

Keep the integer division and the "skip inner ray" rule of the beam loops.
Cover the shapes with unit tests that compare against the reference loops.

## 6.4 Light state machine (all floodlights)

Implemented by `core.state.LightController#tick`; `main` refuels / charges first, then applies the result (re-cast first, then switch).

Every tick on the server:

1. If a re-cast is flagged (by a light block, a mode/config change or the periodic re-check): re-cast diff-based (§6.3).
   This happens on or off, because the phantom lights exist in both states, and is not delayed by the timeout (a re-cast does not switch anything).
2. If `timeout > 0`: decrement and return.
   The timeout (`timeoutFloodlights`) starts when a light turns off, to stop flickering.
   The UV floodlight, the cube light and the fluorescent lights have no timeout.
   The GUIs of the electric and carbon floodlights and the grow light explain it with a "?" hover (`FloodlightMenu.hasTimeoutHint`), and toggling invert during it tells the player when the light turns on (`FloodlightBlockEntity.ticksUntilOn`).
3. `active = redstonePowered XOR inverted`.
4. If active **and** it can pay this tick's cost: on the first active tick switch the cached phantom lights on and set the floodlight's own `LIT` blockstate, then pay the cost.
   Remember `wasActive`.
5. Otherwise, if `wasActive`: switch the cached phantom lights off, clear `LIT`, start the timeout.

Changing the mode re-casts at once, diff-based.
Breaking the block removes this source from its phantom lights, which removes the phantom lights it was the last source of (§6.3).
The timeout still applies: toggling `LIT` is much cheaper than placing blocks, but each change is still a light-engine update for every position.

## 6.7 Block entity data

Base data (`FloodlightBlockEntity`): `inverted`, `color`, `camo` (§6.11), the power (energy buffer, or `timeRemaining` of the carbon floodlight), `on`, `timeout`, the 1-slot inventory and the cached shape.
Facing moves to the blockstate (`FACING`, plus `LIT`; the small fluorescent light's rotation is the `rotated` blockstate), not BE data.
The lights have no owner and no custom name.
Subclasses add `mode` (`BeamFloodlightBlockEntity`, `GrowLightBlockEntity`); the grow light's `nextGrowTick` is not saved (as in 1.7.10).
Every floodlight caches the positions of its current shape (the phantom or UV light blocks it placed or joined) as a `long[]` (`BlockPos.asLong`).
It is saved with the BE so that turning on and off after a reload does not need a re-cast.
A floodlight saved without it (or with a different mode) re-casts on its first tick.
The cache also records the position and facing it was cast from (`shape_origin`, `shape_facing`; taken as matching when missing, as in saves from before 2026-11-02), so a copy of the data elsewhere (`/clone`, structures, pick-block with data) or a facing changed without a new block entity (debug stick) re-casts too; `FloodlightBlock` and `FluorescentLightBlock` turn with rotated and mirrored structures (`rotate` / `mirror`; a quarter turn of a small fluorescent light on the floor or ceiling turns its strip).
Phantom and UV light BEs store their sources as two `long` lists, `sources` (all) and `activeSources` (those currently on).
`LIT` follows `!activeSources.isEmpty()`, and the block is removed when `sources` is empty.
The client needs `color` and `camo` for rendering: send them in the update tag.
On the client, a colour or disguise change re-renders the block (`CompatBlockEntity.remesh()`), because the tint and the disguise are read from the block entity (`client.render.FloodlightTint`: the dye's map colour × a brightness factor of the block).

**Dropped item.** A broken floodlight keeps mode, invert, colour, disguise and energy / burn time on the item (block entity data, `VersionCompat.setBlockEntityData`).
Vanilla loads it back when the item is placed, and the tooltip shows "Configured".
Also:
- the data is only added if something differs from a new light, so unconfigured drops still stack;
- the beam, the on/off state and the slot contents are not kept.
  Slot contents drop as items, so nothing is lost when a block is removed without drops (creative mode, commands).

## 6.8 Grow light

- Target = the block two in front.
  It grows only if the block in front is air or a phantom light (phantom lights count as air) and the target is a `BonemealableBlock` that `isValidBonemealTarget` (`VersionCompat`).
  Call `performBonemeal` on it.
  Growth only happens on ticks the light runs (and pays), also in black-light mode.
- The next grow tick is random: `nextGrowTick = now + rand(20 / chance) + 0.5 × 20 / chance` (one growth per `1 / chanceGrowLight` seconds on average).
  With `chanceGrowLight = 0` the crop never grows; a chance so small that the delay does not fit in a `long` saturates, so the crop practically never grows either (before the shared core, 2026-11-18, the delay overflowed and the crop grew every tick).
  `nextGrowTick` is not saved: it is drawn on the first tick the light runs after being placed or loaded, so placing or loading gives no growth at once.

## 6.9 UV light damage

UV light blocks follow the same model as phantom lights (§6.3): they stay while their UV floodlight exists, and `LIT` switches them on and off (light level 4 ↔ 0).
The UV beam takes over phantom lights in its way (`LightBlock#canReplace`); the floodlights re-cast and pass the UV light block without light.
A UV light block removes itself with neighbour updates (`removalFlags`), so those floodlights re-cast and fill the gap again; where the beam is thinned (§6.3), no phantom light may be next to the gap, and the floodlight fills it at its periodic re-check.
Rendering: only while lit and if `uvLightRendered` is on (`getRenderShape`), cutout render layer, faces between UV light blocks hidden.
Once per second each **lit** `uv_light` block first refreshes its sources like a random tick (`LightBlocks.refresh`), then damages every `LivingEntity` in its 1×1×1 box by `damageUVFloodlight` if it is still lit: a UV light whose floodlight went without its removal hook removes itself instead of hurting.
Use a custom damage type `openmodularlighting:uv_light` (datagen).
The old mod used cactus damage.
Unlike phantom lights, UV light BEs have to tick for the damage.
They do nothing (no entity query) while unlit, and once per second while lit.
There are at most `rangeUVFloodlight` per UV floodlight.
