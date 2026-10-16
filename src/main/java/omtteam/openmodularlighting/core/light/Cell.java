package omtteam.openmodularlighting.core.light;

/**
 * What a beam finds at a block position. See {@link LightWorld} for how {@code main} classifies blocks.
 */
public enum Cell {
    /** Air: a light block can be placed here. */
    AIR,
    /** A phantom light block (of any floodlight). */
    PHANTOM_LIGHT,
    /** A UV light block (of any UV floodlight). */
    UV_LIGHT,
    /** A block that stops light (1.7.10: {@code isOpaqueCube}). */
    OPAQUE,
    /** Any other block (glass, plants, fluids, …): light passes, but no light block is placed. */
    TRANSPARENT,
    /** In a chunk that is not loaded: stops the beam, so no chunk is loaded by casting. */
    UNLOADED
}
