package omtteam.openmodularlighting.core.beam;

/**
 * Which distances along the beam get a light block. Rays are still followed block by block over the skipped
 * distances (so they stop at the same walls); only the light blocks are left out, to save blocks and light updates.
 *
 * @param from  nearest distance (in blocks from the light, in the direction it faces) that may get a light
 * @param step  every {@code step}-th distance gets one
 * @param phase a distance {@code d} gets one if {@code d >= from} and {@code (d - phase)} is a multiple of {@code step}
 */
public record Spacing(int from, int step, int phase) {
    /** Every block of the ray. */
    public static final Spacing ALL = new Spacing(Integer.MIN_VALUE, 1, 0);
    /** Straight beam: lights at 1, 3, 5, … (the second, fourth, … block is skipped). */
    public static final Spacing STRAIGHT = new Spacing(1, 2, 1);
    /** Cones: start at distance 5, offset by one from the straight beam: lights at 6, 8, 10, … */
    public static final Spacing CONE = new Spacing(5, 2, 0);

    public Spacing {
        if (step < 1) throw new IllegalArgumentException("step must be at least 1, got " + step);
    }

    /** Whether a block {@code distance} blocks in front of the light gets a light block. */
    public boolean emitsAt(int distance) {
        return distance >= from && Math.floorMod(distance - phase, step) == 0;
    }
}
