package omtteam.openmodularlighting.core.math;

/**
 * Rotates beam-space offsets into world space. Port of 1.7.10 {@code MathUtil.rotate}.
 * <p>
 * Beam space: {@code x} is the distance forward (along the facing), {@code y} and {@code z} are the two sideways
 * axes. {@link Facing#EAST} is the identity.
 */
public final class Rotation {
    private Rotation() {
    }

    public static Pos rotate(Pos offset, Facing facing) {
        int a = offset.x();
        int b = offset.y();
        int c = offset.z();
        return switch (facing) {
            case DOWN -> new Pos(b, -a, c);
            case UP -> new Pos(-b, a, c);
            case NORTH -> new Pos(c, b, -a);
            case SOUTH -> new Pos(-c, b, a);
            case WEST -> new Pos(-a, b, c);
            case EAST -> new Pos(a, b, c);
        };
    }
}
