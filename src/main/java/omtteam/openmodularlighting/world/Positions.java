package omtteam.openmodularlighting.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import omtteam.openmodularlighting.core.math.Facing;
import omtteam.openmodularlighting.core.math.Pos;

/**
 * Conversions between core types and Minecraft types.
 */
public final class Positions {
    private Positions() {
    }

    public static Pos toPos(BlockPos pos) {
        return new Pos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toBlockPos(Pos pos) {
        return new BlockPos(pos.x(), pos.y(), pos.z());
    }

    /** {@link Facing} has the same order as {@link Direction}. */
    public static Facing toFacing(Direction direction) {
        return Facing.byOrdinal(direction.ordinal());
    }

    public static Direction toDirection(Facing facing) {
        return Direction.values()[facing.ordinal()];
    }
}
