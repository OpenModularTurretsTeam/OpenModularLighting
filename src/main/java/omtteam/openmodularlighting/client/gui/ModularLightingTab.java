package omtteam.openmodularlighting.client.gui;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import omtteam.omlib.compatibility.minecraft.CompatCreativeTabs;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.items.blocks.ItemBlockFloodlight;
import omtteam.openmodularlighting.reference.Reference;


@MethodsReturnNonnullByDefault
public class ModularLightingTab extends CompatCreativeTabs {
    private static ModularLightingTab instance;

    @SuppressWarnings("SameParameterValue")
    private ModularLightingTab(String label) {
        super(label);
    }

    public static ModularLightingTab getInstance() {
        if (instance == null) {
            instance = new ModularLightingTab(Reference.MOD_ID);
        }
        return instance;
    }

    @Override
    public ItemStack getIconItemStack() {
        return new ItemStack(ModBlocks.floodlight);
    }

    @Override
    public Item getItem() {
        return new ItemBlockFloodlight(ModBlocks.floodlight);
    }
}
