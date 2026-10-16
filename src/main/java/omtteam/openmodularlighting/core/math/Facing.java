package omtteam.openmodularlighting.core.math;

/**
 * The six block directions. Same order as Minecraft's {@code Direction} (and 1.7.10 {@code ForgeDirection}), so
 * {@code ordinal()} maps between them.
 */
public enum Facing {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int dx;
    private final int dy;
    private final int dz;

    Facing(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }

    public Facing opposite() {
        return values()[ordinal() ^ 1];
    }

    /** The facing with this ordinal; unknown values fall back to {@link #SOUTH} (the 1.7.10 default). */
    public static Facing byOrdinal(int ordinal) {
        Facing[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SOUTH;
    }
}
