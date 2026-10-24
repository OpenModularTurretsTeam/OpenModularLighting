package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blockentity.BeamFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoBlock;

/**
 * Electric floodlight: straight, narrow cone or wide cone, powered by Forge Energy.
 */
public class ElectricFloodlightBlock extends FloodlightBlock implements CamoBlock {
    public ElectricFloodlightBlock() {
        super(Material.ROCK);
    }

    @Override
    protected FloodlightBlockEntity createLight() {
        return new ElectricFloodlightBlockEntity();
    }

    @Override
    protected boolean hasConfiguration() {
        return true;
    }

    /** Cycles straight → narrow cone → wide cone. */
    @Override
    protected void configure(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                             FloodlightBlockEntity light) {
        if (light instanceof BeamFloodlightBlockEntity) {
            BeamFloodlightBlockEntity beam = (BeamFloodlightBlockEntity) light;
            beam.cycleMode();
            sendMode(player, beam.modeKey());
        }
    }
}
