package omtteam.openmodularlighting.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.client.network.ClientPacketHandlers;
import omtteam.openmodularlighting.client.screen.CarbonFloodlightScreen;
import omtteam.openmodularlighting.client.screen.ElectricFloodlightScreen;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.registry.ModMenus;

import java.util.List;

/**
 * Shared client start-up, the client counterpart of {@code OML}. The loader client entrypoints
 * ({@code client/platform/<loader>/}) call it and wire their loader's events to it.
 */
public final class OMLClient {
    private OMLClient() {
    }

    /**
     * Packet handlers; called first thing after the client platform services are installed.
     */
    public static void init() {
        ClientPacketHandlers.init();
    }

    /**
     * All menu screens of the mod, through the loader's registration.
     */
    public static void registerScreens(ScreenRegistrar registrar) {
        registrar.register(ModMenus.ELECTRIC_FLOODLIGHT, ElectricFloodlightScreen::new);
        registrar.register(ModMenus.CARBON_FLOODLIGHT, CarbonFloodlightScreen::new);
    }

    /**
     * Lines added to item tooltips (called for every item): a floodlight item that kept its settings shows
     * "Configured" (1.7.10 {@code ItemBlockFL}).
     */
    public static void appendTooltip(ItemStack stack, TooltipFlag flag, List<Component> lines) {
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof FloodlightBlock
                && VersionCompat.hasBlockEntityData(stack)) {
            lines.add(Component.translatable("tooltip.openmodularlighting.configured"));
        }
    }
}
