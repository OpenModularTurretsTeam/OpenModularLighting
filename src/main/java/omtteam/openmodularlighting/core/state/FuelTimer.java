package omtteam.openmodularlighting.core.state;

import java.util.function.IntSupplier;

/**
 * Burn time of the carbon floodlight: each tick the light is on uses {@code drain} of it (1 straight, 2 in cone
 * modes, see {@link omtteam.openmodularlighting.core.formula.CarbonFuel#drain}).
 * <p>
 * As in 1.7.10, {@code main} burns the next fuel item as soon as the timer is empty, even while the light is off;
 * the time is kept until the light uses it (see {@link omtteam.openmodularlighting.core.formula.CarbonFuel}).
 */
public final class FuelTimer implements PowerSource {
    private final IntSupplier drain;
    private int ticksRemaining;

    /** @param drain burn time used per tick the light runs (read every tick, so a mode change applies at once) */
    public FuelTimer(IntSupplier drain) {
        this.drain = drain;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    /** Restores the saved value. */
    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = Math.max(0, ticksRemaining);
    }

    public boolean needsFuel() {
        return ticksRemaining <= 0;
    }

    /**
     * Adds the burn time of one fuel item.
     *
     * @return false if the item gives no time (then it must not be consumed; otherwise a tiny burn time or
     * {@code carbonTime = 0} would use up a whole stack in as many ticks)
     */
    public boolean refuel(int ticks) {
        if (ticks <= 0) return false;
        ticksRemaining += ticks;
        return true;
    }

    @Override
    public boolean canRun() {
        return ticksRemaining > 0;
    }

    @Override
    public void run() {
        ticksRemaining = Math.max(0, ticksRemaining - drain.getAsInt());
    }
}
