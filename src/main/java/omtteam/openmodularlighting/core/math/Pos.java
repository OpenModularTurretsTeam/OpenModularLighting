package omtteam.openmodularlighting.core.math;

/**
 * A block position or offset. The core counterpart of {@code BlockPos} / {@code Vec3i}.
 */
public final class Pos {
    public static final Pos ORIGIN = new Pos(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    public Pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Pos offset(Pos offset) {
        return new Pos(x + offset.x, y + offset.y, z + offset.z);
    }

    public Pos offset(Facing facing, int distance) {
        return new Pos(x + facing.dx() * distance, y + facing.dy() * distance, z + facing.dz() * distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos)) return false;
        Pos other = (Pos) o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }

    @Override
    public String toString() {
        return "Pos[x=" + x + ", y=" + y + ", z=" + z + "]";
    }
}
