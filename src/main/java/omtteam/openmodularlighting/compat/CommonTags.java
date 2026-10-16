package omtteam.openmodularlighting.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
//? if >=1.21 {
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
//?} else {
/*import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
*///?}

/**
 * Common ({@code c:}) tags. The ids differ between versions ({@code c:iron_ingots} on 1.20.1, {@code c:ingots/iron}
 * on 1.21); Fabric API provides both.
 */
public final class CommonTags {
    /** Wrenches of other mods: {@code c:wrenches} on 1.20.1, {@code c:tools/wrench} on 1.21. */
    //? if >=1.21 {
    public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, VersionCompat.id("c", "tools/wrench"));
    //?} else {
    /*public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, VersionCompat.id("c", "wrenches"));
    *///?}

    public static final TagKey<Item> IRON_INGOTS = ConventionalItemTags.IRON_INGOTS;
    public static final TagKey<Item> GOLD_INGOTS = ConventionalItemTags.GOLD_INGOTS;
    public static final TagKey<Item> REDSTONE_DUSTS = ConventionalItemTags.REDSTONE_DUSTS;
    public static final TagKey<Item> GLASS_PANES = ConventionalItemTags.GLASS_PANES;

    private CommonTags() {
    }
}
