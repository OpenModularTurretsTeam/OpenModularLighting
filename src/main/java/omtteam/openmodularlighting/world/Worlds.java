package omtteam.openmodularlighting.world;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

/**
 * Helpers for block access that 1.12.2 makes awkward.
 */
public final class Worlds {
    private Worlds() {
    }

    /**
     * The tile entity at {@code pos} without creating one: {@link ChunkCache} (the chunk renderer's view) would
     * otherwise create a missing tile entity off the main thread.
     */
    @Nullable
    public static TileEntity getTileEntity(IBlockAccess world, BlockPos pos) {
        if (world instanceof ChunkCache) {
            return ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        }
        return world.getTileEntity(pos);
    }
}
