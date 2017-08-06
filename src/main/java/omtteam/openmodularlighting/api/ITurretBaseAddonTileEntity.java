package omtteam.openmodularlighting.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.api.IOwnedBlockAddon;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.util.LightAddonUtil;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public interface ITurretBaseAddonTileEntity extends IOwnedBlockAddon {
    /**
     * This should give back the base that this addon block belongs to.
     *
     * @param world the World of the block
     * @param pos   the BlockPos of the block in the world
     * @return the corresponding base.
     */
    default LightingBase getBase(World world, BlockPos pos) {
        return LightAddonUtil.getBase(world, pos);
    }
}
