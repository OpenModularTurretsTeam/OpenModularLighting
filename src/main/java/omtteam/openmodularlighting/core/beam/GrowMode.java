package omtteam.openmodularlighting.core.beam;

/**
 * Modes of the grow light. The ordinal is the 1.7.10 {@code mode} value (0, 1).
 */
public enum GrowMode {
    /** Lights the block in front and grows the crop. */
    LIGHTING,
    /** Only grows the crop, without light. */
    BLACK_LIGHT;

    public GrowMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public BeamShape shape() {
        return this == LIGHTING ? BeamShape.grow() : BeamShape.NONE;
    }

    /** The mode with this ordinal; unknown values fall back to {@link #LIGHTING}. */
    public static GrowMode byOrdinal(int ordinal) {
        GrowMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LIGHTING;
    }
}
