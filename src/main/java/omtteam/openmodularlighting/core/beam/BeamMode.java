package omtteam.openmodularlighting.core.beam;

/**
 * Modes of the electric and carbon floodlights. The ordinal is the 1.7.10 {@code mode} value (0, 1, 2).
 */
public enum BeamMode {
    STRAIGHT,
    NARROW_CONE,
    WIDE_CONE;

    /** The next mode when cycling with a wrench or stick. */
    public BeamMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /**
     * The shape of this mode for the configured ranges ({@code rangeStraightFloodlight}, {@code rangeConeFloodlight}).
     * The wide cone reaches a quarter of the cone range. Not every block of a ray gets a light block, see
     * {@link Spacing#STRAIGHT} and {@link Spacing#CONE}.
     */
    public BeamShape shape(int straightRange, int coneRange) {
        switch (this) {
            case NARROW_CONE:
                return BeamShape.narrowCone(coneRange).withSpacing(Spacing.CONE);
            case WIDE_CONE:
                return BeamShape.wideCone(coneRange / 4).withSpacing(Spacing.CONE);
            default:
                return BeamShape.straight(straightRange).withSpacing(Spacing.STRAIGHT);
        }
    }

    /** The mode with this ordinal; unknown values fall back to {@link #STRAIGHT}. */
    public static BeamMode byOrdinal(int ordinal) {
        BeamMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : STRAIGHT;
    }
}
