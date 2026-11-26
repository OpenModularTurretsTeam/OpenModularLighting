# Open Modular Lighting 1.12.2, power and fuel (`energy/`)

Moved out of the top-level `CLAUDE.md`, so that it is loaded only when you work in this folder; the top-level file has the index.
Section numbers (§) and relative paths (`../CLAUDE.md` is the workspace file, `../../CLAUDE.md` the OMM root file, `../../OpenModularCore`) are those of the top-level file; paths are relative to the repository root.

## 6.5 Power and fuel

- Electric lights have an internal buffer of **50 000 E** (`core.energy.EnergyStore`, from OMLib on this branch; exposed as Forge Energy through OMLib's `power.EnergyAccessStorage`, 1 E = 1 FE; accepting on all sides; fluorescent and grow lights only on the back face, opposite the facing). Up to the whole buffer can be inserted per tick. **Nothing can be extracted**, so cables cannot drain lights.
- **Panel lights share energy** (`tileentity.PanelLightTileEntity`, `energy.ClusterEnergy`; cube lights use the same base but accept energy on every side and cluster only with cube lights on any of their 6 faces): fluorescent and grow lights (any mix) that touch on their sides and face the same way form a `core.cluster.Cluster` (in memory, not saved; joins on the first tick, leaves in `setRemoved`, splits in `refresh()` once per tick). Each light keeps its own buffer. The back-face storage shows the whole cluster: inserting fills the emptiest light first, nothing can be extracted. A light that cannot pay its next tick takes energy from the fullest others (each gives at most half).
- The battery slot accepts any item with extractable Forge Energy (`energy.Batteries`), and is discharged into the buffer each tick while the buffer has room.
- Cost per tick: `energyUsage` for straight, ×2 for cones; `energyUsageSmallFloodlight` (fluorescent), `energyUsageCubeLight`, `energyUsageUVFloodlight`, `energyUsageGrowLight`.
- The carbon floodlight burns one furnace fuel item when `timeRemaining` reaches 0:
  `timeRemaining += carbonTime × burnTime / 1600 × 20` (coal = `carbonTime` seconds of straight light). The mode does not matter when refuelling; instead every tick the light runs uses up 1 (straight) or 2 (both cones, `CarbonFuel.drain`) of it, so cones burn fuel twice as fast. The GUI shows the time left in the current mode (`CarbonFuel.runTicks`). Refuelling happens even while the light is off.
  If the formula gives 0 ticks (tiny burn time, or `carbonTime = 0`), the item is **not** consumed (`FuelTimer.refuel` returns false).
  Fuel is any furnace fuel (`TileEntityFurnace.getItemBurnTime`). The container item stays in the slot like in a furnace (lava bucket → bucket).
- Inventories (1 slot) are exposed through OMLib's `inventory.AutomationItemHandler` (§5). Insertion only accepts valid fuel or energy items. Extraction is disabled.
