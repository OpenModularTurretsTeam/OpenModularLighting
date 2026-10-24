package omtteam.openmodularlighting.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;

import java.util.Map;

/**
 * Grow light (1.7.10 {@code BlockGrowLight}): a square panel that always hangs from the ceiling, shining down; a
 * stick or wrench switches between normal lighting and black light.
 */
public class GrowLightBlock extends PanelLightBlock {
    private static final Map<EnumFacing, AxisAlignedBB> SHAPES = PanelShapes.of(0, 0, 0, 16, 3, 16);

    public GrowLightBlock() {
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.DOWN));
    }

    /** 1.7.10 {@code onBlockPlacedBy}: always facing down. */
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, EnumFacing.DOWN);
    }

    @Override
    protected AxisAlignedBB panelShape(IBlockState state) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected FloodlightBlockEntity createLight() {
        return new GrowLightBlockEntity();
    }

    @Override
    protected boolean hasConfiguration() {
        return true;
    }

    @Override
    protected void configure(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                             FloodlightBlockEntity light) {
        if (light instanceof GrowLightBlockEntity) {
            GrowLightBlockEntity grow = (GrowLightBlockEntity) light;
            grow.cycleMode();
            sendMode(player, grow.modeKey());
        }
    }
}
