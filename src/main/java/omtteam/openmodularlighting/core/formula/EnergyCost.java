package omtteam.openmodularlighting.core.formula;

import omtteam.openmodularlighting.core.beam.BeamMode;

/**
 * Energy cost per tick. Fluorescent, UV and grow lights cost their config value as is.
 */
public final class EnergyCost {
    private EnergyCost() {
    }

    /** Electric floodlight: {@code energyUsage} in straight mode, twice that in both cone modes (1.7.10). */
    public static int floodlight(int energyUsage, BeamMode mode) {
        return mode == BeamMode.STRAIGHT ? energyUsage : energyUsage * 2;
    }
}
