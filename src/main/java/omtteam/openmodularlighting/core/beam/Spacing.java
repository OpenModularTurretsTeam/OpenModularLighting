package omtteam.openmodularlighting.core.beam;

/**
 * Which distances along the beam (blocks in front of the light, in the direction it faces) get a light block. Rays are
 * still followed block by block over the skipped distances (so they stop at the same walls); only the light blocks are
 * left out, to save blocks and light updates.
 * <p>
 * {@code step}: every {@code step}-th distance gets a light. {@code phase}: a block beside the centre line gets a
 * light if {@code distance - phase} is a multiple of {@code step}. {@code axisPhase}: the same for blocks on the
 * centre line (straight in front of the light).
 */
public final class Spacing {
    /** Every block of the ray. */
    public static final Spacing ALL = new Spacing(1, 0, 0);
    /** Straight beam: lights at 1, 3, 5, … (the second, fourth, … block is skipped). */
    public static final Spacing STRAIGHT = new Spacing(2, 1, 1);
    /**
     * Cones: the centre line is thinned like the straight beam (1, 3, 5, …), the rest of the cone is offset by one
     * from it (2, 4, 6, …), so that neighbouring blocks of the cone do not both keep their light.
     */
    public static final Spacing CONE = new Spacing(2, 0, 1);

    private final int step;
    private final int phase;
    private final int axisPhase;

    public Spacing(int step, int phase, int axisPhase) {
        if (step < 1) throw new IllegalArgumentException("step must be at least 1, got " + step);
        this.step = step;
        this.phase = phase;
        this.axisPhase = axisPhase;
    }

    public int step() {
        return step;
    }

    public int phase() {
        return phase;
    }

    public int axisPhase() {
        return axisPhase;
    }

    /**
     * Whether a block gets a light block.
     *
     * @param distance blocks in front of the light
     * @param onAxis   whether the block is on the centre line (not beside it)
     */
    public boolean emitsAt(int distance, boolean onAxis) {
        return Math.floorMod(distance - (onAxis ? axisPhase : phase), step) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Spacing)) return false;
        Spacing other = (Spacing) o;
        return step == other.step && phase == other.phase && axisPhase == other.axisPhase;
    }

    @Override
    public int hashCode() {
        return (step * 31 + phase) * 31 + axisPhase;
    }

    @Override
    public String toString() {
        return "Spacing[step=" + step + ", phase=" + phase + ", axisPhase=" + axisPhase + "]";
    }
}
