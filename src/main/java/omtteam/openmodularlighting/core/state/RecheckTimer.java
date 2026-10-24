package omtteam.openmodularlighting.core.state;

import java.util.Random;

/**
 * Random countdown to the next periodic re-cast of a light, which repairs its beam after world changes that sent no
 * block update (e.g. blocks changed while a chunk was unloaded, or by other mods with no neighbour updates).
 * <p>
 * Not saved: after loading, the first delay is random too, so lights loaded together do not re-cast in the same tick.
 */
public final class RecheckTimer {
    /** Mean ticks between re-casts (20 seconds). */
    public static final int MEAN_TICKS = 400;

    private long remaining = -1;

    /**
     * Ticks until the next re-cast: uniform in {@code [MEAN_TICKS / 2, 3 × MEAN_TICKS / 2)}, mean {@code MEAN_TICKS}.
     */
    public static long nextDelay(Random random) {
        return MEAN_TICKS / 2 + random.nextInt(MEAN_TICKS);
    }

    /** Advances one tick; true when the light should re-cast now. */
    public boolean tick(Random random) {
        if (remaining < 0) {
            remaining = nextDelay(random);
        }
        if (remaining-- > 0) return false;
        remaining = nextDelay(random);
        return true;
    }
}
