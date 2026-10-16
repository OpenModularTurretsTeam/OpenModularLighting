package omtteam.openmodularlighting.core.formula;

import java.util.random.RandomGenerator;

/**
 * Timing of the grow light's growth ticks (1.7.10 {@code RandomUtil.getRandomTickTimeoutFromFloatChance}).
 */
public final class GrowChance {
    private GrowChance() {
    }

    /**
     * Ticks until the next growth tick: {@code random(round(20 / chance)) + round(0.5 × 20 / chance)}, i.e. uniform in
     * {@code [10 / chance, 30 / chance)} with a mean of {@code 20 / chance} ticks, one growth tick per {@code 1 / chance}
     * seconds on average. With {@code chance <= 0} the crop never grows ({@link Long#MAX_VALUE}).
     */
    public static long nextDelay(double chance, RandomGenerator random) {
        if (!(chance > 0)) return Long.MAX_VALUE;
        long bound = Math.max(1, Math.round(20 / chance));
        long base = Math.round(0.5 * 20 / chance);
        return random.nextLong(bound) + base;
    }

    /** The game time of the next growth tick, saturating instead of overflowing. */
    public static long nextGrowTick(long now, double chance, RandomGenerator random) {
        long delay = nextDelay(chance, random);
        return delay > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + delay;
    }
}
