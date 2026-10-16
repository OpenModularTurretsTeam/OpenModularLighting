package omtteam.openmodularlighting.core.state;

/**
 * What a light runs on: an energy buffer (electric lights, implemented in {@code main} on top of Team Reborn Energy)
 * or a {@link FuelTimer} (carbon floodlight).
 */
public interface PowerSource {
    /** Whether this tick's cost can be paid. */
    boolean canRun();

    /** Pays this tick's cost. Only called after {@link #canRun()} returned true. */
    void run();
}
