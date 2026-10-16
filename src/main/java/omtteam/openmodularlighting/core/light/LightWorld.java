package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Pos;

/**
 * Read-only view of the world for casting beams. {@code main} implements it on top of {@code Level}:
 * {@link Cell#UNLOADED} if the chunk is not loaded (check before reading the block), {@link Cell#AIR} for
 * {@code isAir()}, the two light blocks by block, {@link Cell#OPAQUE} for {@code isSolidRender} (the 1.7.10
 * {@code isOpaqueCube}) and {@link Cell#TRANSPARENT} for everything else.
 */
@FunctionalInterface
public interface LightWorld {
    Cell cellAt(Pos pos);
}
