package omtteam.openmodularlighting.core.state;

/**
 * Burn time of the carbon floodlight: one tick is used per tick the light is on.
 * <p>
 * As in 1.7.10, {@code main} burns the next fuel item as soon as the timer is empty, even while the light is off;
 * the time is kept until the light uses it (see {@link omtteam.openmodularlighting.core.formula.CarbonFuel}).
 */
public final class FuelTimer implements PowerSource {
    private int ticksRemaining;

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
        ticksRemaining--;
    }
}
