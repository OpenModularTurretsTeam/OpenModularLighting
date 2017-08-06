package omtteam.openmodularlighting.init;


import net.minecraft.item.Item;
import omtteam.openmodularlighting.items.*;

import static omtteam.omlib.util.InitHelper.registerItem;


public class ModItems {
    public static Item addonMetaItem;
    public static Item upgradeMetaItem;
    public static Item intermediateProductTiered;
    public static Item intermediateProductRegular;
    public static Item usableMetaItem;

    public static void init() {
        intermediateProductTiered = registerItem(new IntermediateProductTiered());
        intermediateProductRegular = registerItem(new IntermediateProductRegular());
        addonMetaItem = registerItem(new AddonMetaItem());
        upgradeMetaItem = registerItem(new UpgradeMetaItem());
        usableMetaItem = registerItem(new UsableMetaItem());
    }
}
