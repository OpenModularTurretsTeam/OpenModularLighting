package omtteam.openmodularlighting.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Forge's registry events, fired after pre-initialization: blocks (with their block entity classes), then items.
 */
public final class Registration {
    private Registration() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : ModBlocks.ALL) {
            event.getRegistry().register(block);
        }
        ModTileEntities.register();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Item item : ModItems.ALL) {
            event.getRegistry().register(item);
        }
    }
}
