package omtteam.openmodularlighting.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Common ({@code c:}) tags, shared by Fabric and NeoForge. The ids differ between versions ({@code c:iron_ingots} on
 * 1.20.1, {@code c:ingots/iron} on 1.21).
 */
public final class CommonTags {
    //? if >=1.21 {
    /** Wrenches of other mods: {@code c:wrenches} on 1.20.1, {@code c:tools/wrench} on 1.21. */
    public static final TagKey<Item> WRENCHES = tag("tools/wrench");
    public static final TagKey<Item> IRON_INGOTS = tag("ingots/iron");
    public static final TagKey<Item> GOLD_INGOTS = tag("ingots/gold");
    public static final TagKey<Item> REDSTONE_DUSTS = tag("dusts/redstone");
    //?} else {
    /*/^* Wrenches of other mods: {@code c:wrenches} on 1.20.1, {@code c:tools/wrench} on 1.21. ^/
    public static final TagKey<Item> WRENCHES = tag("wrenches");
    public static final TagKey<Item> IRON_INGOTS = tag("iron_ingots");
    public static final TagKey<Item> GOLD_INGOTS = tag("gold_ingots");
    public static final TagKey<Item> REDSTONE_DUSTS = tag("redstone_dusts");
    *///?}
    public static final TagKey<Item> GLASS_PANES = tag("glass_panes");

    private CommonTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, VersionCompat.id("c", path));
    }
}
