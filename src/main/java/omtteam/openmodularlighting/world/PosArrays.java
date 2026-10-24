package omtteam.openmodularlighting.world;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Block positions as flat {@code int} arrays ({@code x, y, z, x, y, z, …}) for NBT: 1.12.2 has long arrays in NBT,
 * but no way to read them back from a compound.
 */
public final class PosArrays {
    private PosArrays() {
    }

    public static int[] write(Collection<BlockPos> positions) {
        int[] array = new int[positions.size() * 3];
        int i = 0;
        for (BlockPos pos : positions) {
            array[i++] = pos.getX();
            array[i++] = pos.getY();
            array[i++] = pos.getZ();
        }
        return array;
    }

    /** Reads what {@link #write} wrote; a trailing incomplete position is ignored. */
    public static List<BlockPos> read(int[] array) {
        List<BlockPos> positions = new ArrayList<>(array.length / 3);
        for (int i = 0; i + 2 < array.length; i += 3) {
            positions.add(new BlockPos(array[i], array[i + 1], array[i + 2]));
        }
        return positions;
    }
}
