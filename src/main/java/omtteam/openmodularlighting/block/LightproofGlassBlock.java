package omtteam.openmodularlighting.block;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Glass that can be seen through but lets no light through, and stops floodlight beams. On the main branch this is
 * vanilla's tinted glass block; 1.12.2 has none, so it is glass with full light opacity that drops itself.
 */
public class LightproofGlassBlock extends BlockBreakable {
    public LightproofGlassBlock() {
        super(Material.GLASS, false);
        setHardness(0.3F);
        setSoundType(SoundType.GLASS);
        setLightOpacity(255);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }
}
