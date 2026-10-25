package omtteam.openmodularlighting.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.tileentity.UvLightTileEntity;
import omtteam.openmodularlighting.world.LightBlocks;

/**
 * The UV light block of the UV floodlight (1.7.10 {@code BlockUVLightBlock}): light level 4 while {@link #LIT}, and it
 * damages living entities inside it (see {@link UvLightTileEntity}). Otherwise like a phantom light: no shape,
 * replaceable, removed with its last source, but not air. Visible only while lit and if {@code uvLightRendered} is on.
 */
public class UvLightBlock extends LightBlock {
    /** Not solid, lets light and entities through, replaceable (like air, but not air). */
    private static final Material UV_LIGHT = new MaterialTransparent(MapColor.AIR);

    public UvLightBlock() {
        super(UV_LIGHT);
    }

    @Override
    protected int litLightLevel() {
        return 4;
    }

    /** The UV beam takes over phantom lights (1.7.10 {@code UVSource}). */
    @Override
    public boolean canReplace(IBlockState state) {
        return state.getBlock() == ModBlocks.PHANTOM_LIGHT;
    }

    /**
     * With neighbour updates: a floodlight beam crossing this block passed it without light, so the phantom lights
     * next to it re-cast their sources and fill the gap.
     */
    @Override
    public int removalFlags() {
        return LightBlocks.UPDATE_ALL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return state.getValue(LIT) && OMLConfig.get().general.uvLightRendered
                ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    /** Hide faces between UV light blocks, like glass (1.7.10 {@code shouldSideBeRendered}). */
    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState adjacent = world.getBlockState(pos.offset(side));
        if (adjacent.getBlock() == this && adjacent.getValue(LIT)) return false;
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new UvLightTileEntity();
    }
}
