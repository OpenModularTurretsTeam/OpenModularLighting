package omtteam.openmodularlighting.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import omtteam.omlib.item.ItemMultiTool;

/**
 * What the common ({@code c:}) tags of the main branch stand for on 1.12.2, where there are no item tags: the ore
 * dictionary (the recipes use {@code ingotIron}, {@code ingotGold}, {@code dustRedstone} and {@code paneGlass}) and,
 * for wrenches, the tool classes items declare (and OMLib's wrench, which declares none).
 */
public final class CommonTags {
    /** Ore dictionary names other mods use for their wrenches. */
    private static final String[] WRENCH_ORES = {"toolWrench", "itemWrench", "wrench"};

    private CommonTags() {
    }

    /** Whether the stack is a wrench: OMLib's, tool class {@code wrench}, or a wrench in the ore dictionary. */
    public static boolean isWrench(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ItemMultiTool) return true;
        if (stack.getItem().getToolClasses(stack).contains("wrench")) return true;
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            for (String wrench : WRENCH_ORES) {
                if (wrench.equals(name)) return true;
            }
        }
        return false;
    }
}
