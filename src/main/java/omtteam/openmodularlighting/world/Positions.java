package omtteam.openmodularlighting.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

    /** {@link Facing} has the same order as {@link EnumFacing}. */
    public static Facing toFacing(EnumFacing direction) {
        return Facing.byOrdinal(direction.ordinal());
    }

    public static EnumFacing toDirection(Facing facing) {
        return EnumFacing.values()[facing.ordinal()];
    }
}
