package omtteam.openmodularlighting.items.blocks;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.compatibility.minecraft.CompatItemBlock;
import omtteam.omlib.items.IDrawOutlineBase;
import omtteam.openmodularlighting.api.ILightingBaseAddon;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public abstract class ItemBlockBaseAddon extends CompatItemBlock implements IDrawOutlineBase {
    public ItemBlockBaseAddon(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderOutline(EnumFacing facing, World world, BlockPos pos) {
        return ((ILightingBaseAddon) this.getBlock()).getBoundingBoxFromFacing(facing, world, pos);
    }
}
