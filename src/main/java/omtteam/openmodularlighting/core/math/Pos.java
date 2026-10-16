package omtteam.openmodularlighting.core.math;

/**
 * A block position or offset. The core counterpart of {@code BlockPos} / {@code Vec3i}.
 */
public record Pos(int x, int y, int z) {
    public static final Pos ORIGIN = new Pos(0, 0, 0);

    public Pos offset(Pos offset) {
        return new Pos(x + offset.x, y + offset.y, z + offset.z);
    }

    public Pos offset(Facing facing, int distance) {
        return new Pos(x + facing.dx() * distance, y + facing.dy() * distance, z + facing.dz() * distance);
    }
}
