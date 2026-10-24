package omtteam.openmodularlighting.compat.hwyla;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;

import java.util.List;

/**
 * Hwyla provider for every light (the 1.12.2 counterpart of the main branch's {@code FloodlightJadeProvider}): the
 * server adds invert and mode, the client shows them ({@link FloodlightTooltip}).
 */
public enum FloodlightWailaProvider implements IWailaDataProvider {
    INSTANCE;

    /** Key of the on/off switch in Hwyla's config; its name is the lang key {@code option.<key>}. */
    public static final String CONFIG_KEY = "openmodularlighting.light";

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                     BlockPos pos) {
        if (te instanceof FloodlightBlockEntity) {
            FloodlightTooltip.write(tag, (FloodlightBlockEntity) te);
        }
        return tag;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (config.getConfig(CONFIG_KEY)) {
            for (ITextComponent line : FloodlightTooltip.lines(accessor.getNBTData())) {
                tooltip.add(line.getFormattedText());
            }
        }
        return tooltip;
    }
}
