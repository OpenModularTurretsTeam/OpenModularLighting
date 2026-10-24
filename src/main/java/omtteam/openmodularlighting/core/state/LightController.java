package omtteam.openmodularlighting.core.state;

/**
 * On/off state of a light (port of the {@code updateEntity} logic shared by all 1.7.10 floodlights, adapted to the
 * persistent phantom lights of this port).
 * <p>
 * Each server tick, {@code main} first refuels / charges the power source, then calls {@link #tick} and applies the
 * result: re-cast first (new positions get {@link #isOn()}), then switch the cached light blocks.
 */
public final class LightController {
    private boolean inverted;
    private boolean on;
    private int timeout;
    private boolean recastPending;

    /** Change of the light blocks requested by a tick. */
    public enum Switch {
        NONE,
        /** Switch the cached light blocks on and set the light's own {@code LIT}. */
        ON,
        /** Switch the cached light blocks off and clear the light's own {@code LIT}. */
        OFF
    }

    /**
     * Result of a tick: {@link #recast()} re-cast the shape (diff against the cached one), {@link #change()} switch
     * the light blocks on or off, {@link #running()} the light ran (and paid) this tick; the grow light only grows
     * crops then.
     */
    public static final class TickResult {
        private final boolean recast;
        private final Switch change;
        private final boolean running;

        public TickResult(boolean recast, Switch change, boolean running) {
            this.recast = recast;
            this.change = change;
            this.running = running;
        }

        public boolean recast() {
            return recast;
        }

        public Switch change() {
            return change;
        }

        public boolean running() {
            return running;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TickResult)) return false;
            TickResult other = (TickResult) o;
            return recast == other.recast && change == other.change && running == other.running;
        }

        @Override
        public int hashCode() {
            return (Boolean.hashCode(recast) * 31 + change.hashCode()) * 31 + Boolean.hashCode(running);
        }

        @Override
        public String toString() {
            return "TickResult[recast=" + recast + ", change=" + change + ", running=" + running + "]";
        }
    }

    /**
     * Advances one tick.
     *
     * @param powered      whether the light receives a redstone signal
     * @param power        pays the cost of running this tick
     * @param timeoutTicks ticks the light stays off after turning off ({@code timeoutFloodlights}; 0 for the UV
     *                     floodlight, which had no timeout in 1.7.10)
     */
    public TickResult tick(boolean powered, PowerSource power, int timeoutTicks) {
        // Re-casting does not switch anything on or off, so it is not delayed by the timeout
        boolean recast = recastPending;
        recastPending = false;
        if (timeout > 0) {
            timeout--;
            return new TickResult(recast, Switch.NONE, false);
        }
        boolean active = powered ^ inverted;
        if (active && power.canRun()) {
            power.run();
            Switch change = on ? Switch.NONE : Switch.ON;
            on = true;
            return new TickResult(recast, change, true);
        }
        if (on) {
            on = false;
            timeout = timeoutTicks;
            return new TickResult(recast, Switch.OFF, false);
        }
        return new TickResult(recast, Switch.NONE, false);
    }

    /** Whether the light is on (1.7.10 {@code wasActive}). */
    public boolean isOn() {
        return on;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void toggleInverted() {
        inverted = !inverted;
    }

    public int timeout() {
        return timeout;
    }

    /**
     * Requests a re-cast on the next tick: after placing the light, after loading without a cached shape, or when a
     * light block on the beam was replaced or its neighbour changed.
     */
    public void requestRecast() {
        recastPending = true;
    }

    public boolean isRecastPending() {
        return recastPending;
    }

    /** Restores the saved state. */
    public void load(boolean inverted, boolean on, int timeout) {
        this.inverted = inverted;
        this.on = on;
        this.timeout = Math.max(0, timeout);
    }
}
