package omtteam.openmodularlighting.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import omtteam.omlib.util.world.WorldUtil;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.FluorescentLightTileEntity;

import java.util.Map;

/**
 * Small and square fluorescent lights (1.7.10 {@code BlockSmallElectricFloodlight}, metadata 0 and 1). The small
 * light is a 6 px wide strip that a stick or wrench turns by 90° ({@link #ROTATED}, 1.7.10 {@code rotationState}).
 * <p>
 * Facing and {@code LIT} already fill the 4 metadata bits, so {@link #ROTATED} is kept in the block entity, as in
 * 1.7.10, and only shown in the actual state for the model and the outline.
 */
public class FluorescentLightBlock extends PanelLightBlock {
    public static final PropertyBool ROTATED = PropertyBool.create("rotated");

    private static final Map<EnumFacing, AxisAlignedBB> SQUARE = PanelShapes.of(0, 0, 0, 16, 3, 16);
    private static final Map<EnumFacing, AxisAlignedBB> SMALL = PanelShapes.of(0, 0, 5, 16, 3, 11);
    private static final Map<EnumFacing, AxisAlignedBB> SMALL_ROTATED = PanelShapes.of(5, 0, 0, 11, 3, 16);

    private final boolean small;

    /** @param small the 6 px strip (rotatable) instead of the full square */
    public FluorescentLightBlock(boolean small) {
        this.small = small;
        setDefaultState(getDefaultState().withProperty(ROTATED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT, ROTATED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldUtil.getTileEntity(world, pos);
        boolean rotated = small && tile instanceof FluorescentLightTileEntity
                && ((FluorescentLightTileEntity) tile).isRotated();
        return state.withProperty(ROTATED, rotated);
    }

    @Override
    protected AxisAlignedBB panelShape(IBlockState state) {
        Map<EnumFacing, AxisAlignedBB> shapes = !small ? SQUARE : state.getValue(ROTATED) ? SMALL_ROTATED : SMALL;
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected FloodlightTileEntity createLight() {
        return new FluorescentLightTileEntity();
    }

    /** Only the small light can be turned (the square one looks the same either way). */
    @Override
    protected boolean hasConfiguration() {
        return small;
    }

    /** Turns the strip by 90°; no block update, which would make the beam re-cast. */
    @Override
    protected void configure(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                             FloodlightTileEntity light) {
        if (light instanceof FluorescentLightTileEntity) {
            ((FluorescentLightTileEntity) light).toggleRotated();
        }
    }
}
