package omtteam.openmodularlighting.core.formula;

import omtteam.openmodularlighting.core.beam.BeamMode;

/**
 * Burn time of a fuel item in the carbon floodlight.
 */
public final class CarbonFuel {
    /** Furnace burn time of coal; one coal lasts {@code carbonTime} seconds in straight mode. */
    private static final int COAL_BURN_TIME = 1600;

    private CarbonFuel() {
    }

    /**
     * Ticks of light from one fuel item: {@code carbonTime × burnTime / 1600 × (straight ? 20 : 10)}, evaluated left
     * to right in integer arithmetic as in 1.7.10. Coal lasts {@code carbonTime} seconds in straight mode and half of
     * that in both cone modes. The mode at the time of refuelling counts.
     *
     * @param burnTime   furnace burn time of the item in ticks
     * @param carbonTime config {@code carbonTime} (seconds per coal)
     */
    public static int ticks(int burnTime, int carbonTime, BeamMode mode) {
        return carbonTime * burnTime / COAL_BURN_TIME * (mode == BeamMode.STRAIGHT ? 20 : 10);
    }
}
