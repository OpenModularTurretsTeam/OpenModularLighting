package omtteam.openmodularlighting.blockentity;

import net.minecraft.util.math.BlockPos;

/**
 * A block entity that lights light blocks (floodlights, fluorescent and grow lights, UV floodlight). Light blocks use
 * it to check their sources and to ask for a re-cast.
 */
public interface LightSourceBlockEntity {
    /** Whether the light is currently on. */
    boolean isLightOn();

    /** Whether {@code pos} is part of this light's current (cached) shape. */
    boolean coversLightAt(BlockPos pos);

    /** Asks the light to re-cast its beam on its next tick (the world along the beam changed). */
    void requestRecast();
}
