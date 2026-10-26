package omtteam.openmodularlighting.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.OML;
import omtteam.omlib.compat.VersionCompat;

/**
 * The mod's creative tab (1.7.10 {@code CreativeTabFloodlight}).
 */
public final class ModCreativeTabs {
    public static final CreativeModeTab MAIN = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, OML.id("main"),
            VersionCompat.creativeTabBuilder()
                    .title(Component.translatable("itemGroup.openmodularlighting"))
                    .icon(() -> new ItemStack(ModItems.ELECTRIC_FLOODLIGHT))
                    .displayItems((parameters, output) -> {
                        ModBlocks.LIGHT_SOURCES.forEach(output::accept);
                        output.accept(ModItems.LIGHTPROOF_GLASS);
                        ModItems.CRAFTING_ITEMS.forEach(output::accept);
                    })
                    .build());

    private ModCreativeTabs() {
    }

    /** Loads the class, registering the tab. */
    public static void init() {
    }
}
