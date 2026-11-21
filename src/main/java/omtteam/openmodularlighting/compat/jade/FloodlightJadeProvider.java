package omtteam.openmodularlighting.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server half of the Jade overlay for every light: adds invert and mode ({@link FloodlightTooltip#write}). The client
 * half, which shows them, is {@code client.compat.jade.FloodlightJadeComponent}.
 */
public enum FloodlightJadeProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    /** Also the key of the on/off switch in Jade's plugin config ({@code config.jade.plugin_openmodularlighting.light}). */
    public static final Identifier UID = OML.id("light");

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FloodlightBlockEntity light) {
            FloodlightTooltip.write(tag, light);
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
