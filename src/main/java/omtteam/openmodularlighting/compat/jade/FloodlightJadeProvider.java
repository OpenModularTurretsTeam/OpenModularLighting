package omtteam.openmodularlighting.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade provider for every light: the server adds invert and mode, the client shows them ({@link FloodlightTooltip}).
 */
public enum FloodlightJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    /** Also the key of the on/off switch in Jade's plugin config ({@code config.jade.plugin_openmodularlighting.light}). */
    public static final ResourceLocation UID = OML.id("light");

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FloodlightBlockEntity light) {
            FloodlightTooltip.write(tag, light);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        for (Component line : FloodlightTooltip.lines(accessor.getServerData())) {
            tooltip.add(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
