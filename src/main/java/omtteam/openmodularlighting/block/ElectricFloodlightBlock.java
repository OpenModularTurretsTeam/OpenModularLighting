package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.tileentity.BeamFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.ElectricFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

/**
 * Electric floodlight: straight, narrow cone or wide cone, powered by Forge Energy.
 */
public class ElectricFloodlightBlock extends FloodlightBlock {
    public ElectricFloodlightBlock() {
        super(Material.ROCK);
    }

    /** Can be disguised (OMLib's camouflage menu). */
    @Override
    public boolean supportsCamo() {
        return true;
    }

    @Override
    protected FloodlightTileEntity createLight() {
        return new ElectricFloodlightTileEntity();
    }

    @Override
    protected boolean hasConfiguration() {
        return true;
    }

    /** Cycles straight → narrow cone → wide cone. */
    @Override
    protected void configure(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                             FloodlightTileEntity light) {
        if (light instanceof BeamFloodlightTileEntity) {
            BeamFloodlightTileEntity beam = (BeamFloodlightTileEntity) light;
            beam.cycleMode();
            sendMode(player, beam.modeKey());
        }
    }
}
