package omtteam.openmodularlighting.core.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.beam.BeamMode;
import org.junit.jupiter.api.Test;

import java.util.Random;

class FormulaTest {
    @Test
    void coalAddsCarbonTimeSecondsAndConesUseItUpTwiceAsFast() {
        int stored = CarbonFuel.ticks(1600, 300);
        assertEquals(300 * 20, stored);
        assertEquals(300 * 20, CarbonFuel.runTicks(stored, BeamMode.STRAIGHT));
        assertEquals(300 * 10, CarbonFuel.runTicks(stored, BeamMode.NARROW_CONE));
        assertEquals(300 * 10, CarbonFuel.runTicks(stored, BeamMode.WIDE_CONE));
        assertEquals(1, CarbonFuel.drain(BeamMode.STRAIGHT));
        assertEquals(2, CarbonFuel.drain(BeamMode.WIDE_CONE));
        assertEquals(1, CarbonFuel.runTicks(1, BeamMode.NARROW_CONE)); // rounded up
        assertEquals(0, CarbonFuel.runTicks(0, BeamMode.NARROW_CONE));
    }

    @Test
    void carbonFuelUsesIntegerDivisionLikeTheOldMod() {
        // 300 * 100 / 1600 = 18 (not 18.75), then * 20
        assertEquals(360, CarbonFuel.ticks(100, 300));
        assertEquals(0, CarbonFuel.ticks(100, 1));
        assertEquals(0, CarbonFuel.ticks(1600, 0));
    }

    @Test
    void hugeBurnTimesAreCappedInsteadOfOverflowing() {
        // 1000 * 3_000_000 does not fit in an int
        assertEquals(1000L * 3_000_000 / 1600 * 20, CarbonFuel.ticks(3_000_000, 1000));
        assertEquals(Integer.MAX_VALUE, CarbonFuel.ticks(Integer.MAX_VALUE, 1000));
    }

    @Test
    void growDelayStaysInTheOldBounds() {
        Random random = new Random(1);
        long sum = 0;
        int samples = 20_000;
        for (int i = 0; i < samples; i++) {
            long delay = GrowChance.nextDelay(0.05, random);
            assertTrue(delay >= 200 && delay < 600, "delay " + delay);
            sum += delay;
        }
        double mean = (double) sum / samples;
        assertTrue(Math.abs(mean - 400) < 5, "mean " + mean); // 20 / chance ticks = one growth per 20 s
    }

    @Test
    void growDelayAtFullChance() {
        Random random = new Random(2);
        for (int i = 0; i < 1000; i++) {
            long delay = GrowChance.nextDelay(1, random);
            assertTrue(delay >= 10 && delay < 30, "delay " + delay);
        }
    }

    @Test
    void nextGrowTickIsNowPlusTheDelayAndSaturates() {
        long delay = GrowChance.nextDelay(1, new Random(4));
        assertEquals(1000 + delay, GrowChance.nextGrowTick(1000, 1, new Random(4)));
        assertEquals(Long.MAX_VALUE, GrowChance.nextGrowTick(Long.MAX_VALUE - 5, 1, new Random(4)));
    }

    @Test
    void zeroChanceNeverGrows() {
        Random random = new Random(3);
        assertEquals(Long.MAX_VALUE, GrowChance.nextDelay(0, random));
        assertEquals(Long.MAX_VALUE, GrowChance.nextDelay(Double.NaN, random));
        assertEquals(Long.MAX_VALUE, GrowChance.nextGrowTick(123_456L, 0, random));
    }

    @Test
    void floodlightCostDoublesInConeModes() {
        assertEquals(20, EnergyCost.floodlight(20, BeamMode.STRAIGHT));
        assertEquals(40, EnergyCost.floodlight(20, BeamMode.NARROW_CONE));
        assertEquals(40, EnergyCost.floodlight(20, BeamMode.WIDE_CONE));
    }
}
