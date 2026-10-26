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
     * Burn time one fuel item adds, in ticks of straight light: {@code carbonTime × burnTime / 1600 × 20}, evaluated
     * left to right in integer arithmetic as in 1.7.10. Does not depend on the mode; the cone modes use the time up
     * faster ({@link #drain}). Computed in {@code long} and capped at {@link Integer#MAX_VALUE}, so modded fuels with
     * huge burn times cannot overflow into a negative time.
     *
     * @param burnTime   furnace burn time of the item in ticks
     * @param carbonTime config {@code carbonTime} (seconds per coal)
     */
    public static int ticks(int burnTime, int carbonTime) {
        return (int) Math.min(Integer.MAX_VALUE, (long) carbonTime * burnTime / COAL_BURN_TIME * 20);
    }

    /** Burn time used per tick: 1 for straight light, 2 for both cones (twice the "power"). */
    public static int drain(BeamMode mode) {
        return mode == BeamMode.STRAIGHT ? 1 : 2;
    }

    /** Ticks the light runs on {@code stored} burn time in this mode (rounded up, 0 only when empty). */
    public static int runTicks(int stored, BeamMode mode) {
        int drain = drain(mode);
        return (stored + drain - 1) / drain;
    }
}
