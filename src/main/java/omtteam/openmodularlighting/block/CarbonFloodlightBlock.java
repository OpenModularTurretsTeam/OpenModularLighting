package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.BeamFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.menu.OMLGuiHandler;

/**
 * Carbon (non-electric) floodlight: the same modes as the electric one, powered by furnace fuel.
 */
public class CarbonFloodlightBlock extends FloodlightBlock implements CamoBlock {
    public CarbonFloodlightBlock() {
        super(Material.ROCK);
    }

    @Override
    protected FloodlightBlockEntity createLight() {
        return new CarbonFloodlightBlockEntity();
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

    @Override
    protected void openMenu(EntityPlayer player, FloodlightBlockEntity light) {
        BlockPos pos = light.getPos();
        player.openGui(OML.instance, OMLGuiHandler.CARBON, light.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
