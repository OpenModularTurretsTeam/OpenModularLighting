package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.handler.OMLGuiHandler;
import omtteam.openmodularlighting.tileentity.BeamFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.CarbonFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

/**
 * Carbon (non-electric) floodlight: the same modes as the electric one, powered by furnace fuel.
 */
public class CarbonFloodlightBlock extends FloodlightBlock {
    public CarbonFloodlightBlock() {
        super(Material.ROCK);
    }

    /** Can be disguised (OMLib's camouflage menu). */
    @Override
    public boolean supportsCamo() {
        return true;
    }

    @Override
    protected FloodlightTileEntity createLight() {
        return new CarbonFloodlightTileEntity();
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

    @Override
    public void openGui(EntityPlayer player, FloodlightTileEntity light) {
        BlockPos pos = light.getPos();
        player.openGui(OML.instance, OMLGuiHandler.CARBON, light.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
