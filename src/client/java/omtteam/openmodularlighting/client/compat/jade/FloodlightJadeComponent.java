package omtteam.openmodularlighting.client.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.jade.FloodlightJadeProvider;
import omtteam.openmodularlighting.compat.jade.FloodlightTooltip;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Client half of the Jade overlay for every light: shows what {@link FloodlightJadeProvider} sent
 * ({@link FloodlightTooltip#lines}).
 */
public enum FloodlightJadeComponent implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        for (Component line : FloodlightTooltip.lines(accessor.getServerData())) {
            tooltip.add(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FloodlightJadeProvider.UID;
    }
}
