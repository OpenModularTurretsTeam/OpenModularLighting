package omtteam.openmodularlighting.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import omtteam.openmodularlighting.OML;

/**
 * The mod's creative tab (1.7.10 {@code CreativeTabFloodlight}), titled {@code itemGroup.openmodularlighting}.
 */
public final class ModCreativeTabs {
    public static final CreativeTabs MAIN = new CreativeTabs(OML.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.ELECTRIC_FLOODLIGHT);
        }
    };

    private ModCreativeTabs() {
    }
}
