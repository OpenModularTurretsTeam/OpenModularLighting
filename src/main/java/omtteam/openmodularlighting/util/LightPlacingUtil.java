package omtteam.openmodularlighting.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blocks.BlockPhantomLight;
import omtteam.openmodularlighting.tileentity.lights.Floodlight;
import omtteam.openmodularlighting.tileentity.lights.PhantomLight;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 03/08/17.
 * This Class
 */
public class LightPlacingUtil {

    @ParametersAreNonnullByDefault
    public static void straightSource(World world, Floodlight source, boolean remove) {
        BlockPos currentpos = source.getPos();
        for (int i = 1; i <= source.getMaxRange(); i++) {
            currentpos = currentpos.offset(source.getOrientation());
            if (!remove && world.getBlockState(currentpos).getBlock().isVisuallyOpaque()) {
                return;
            } else if (!remove && source.getCurrentRange() < i) {
                return;  //cancel when exceeding custom range on placing
            }
            if (i % 2 == 0 && !remove) {  //only place light block every 2 blocks.
                world.setBlockState(currentpos, new BlockPhantomLight().getDefaultState());
                TileEntity phantomLight = world.getTileEntity(currentpos);
                if (phantomLight instanceof PhantomLight) {
                    ((PhantomLight) phantomLight).addSource(source.getPos());
                }
            } else if (i % 2 == 0 && remove) {
                TileEntity phantomLight = world.getTileEntity(currentpos);
                if (phantomLight instanceof PhantomLight) {
                    ((PhantomLight) phantomLight).removeSource(source.getPos());
                }
            }
        }
    }
}
