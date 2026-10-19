package omtteam.openmodularlighting.client.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.client.compat.ClientCompat;
import omtteam.openmodularlighting.client.network.ClientPacketHandlers;
import omtteam.openmodularlighting.client.platform.ClientPlatform;
import omtteam.openmodularlighting.client.platform.ClientPlatformServices;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.client.screen.CarbonFloodlightScreen;
import omtteam.openmodularlighting.client.screen.ElectricFloodlightScreen;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.registry.ModMenus;

/**
 * Fabric client entrypoint: screens, renderers and render layers.
 */
public final class OMLFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.install(new ClientPlatformServices(new FabricClientNetwork()));
        ClientPacketHandlers.init();
        MenuScreens.register(ModMenus.ELECTRIC_FLOODLIGHT, ElectricFloodlightScreen::new);
        MenuScreens.register(ModMenus.CARBON_FLOODLIGHT, CarbonFloodlightScreen::new);
        FloodlightTint.register();
        // the UV light texture has transparent pixels
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UV_LIGHT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LIGHTPROOF_GLASS, RenderType.translucent());
        // light debug tool: wireframes around light blocks while it is held
        LightBlockDebugRenderer.register();
        // 1.7.10 ItemBlockFL: a dropped floodlight that kept its settings
        ClientCompat.registerTooltip((stack, flag, lines) -> {
            if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof FloodlightBlock
                    && VersionCompat.hasBlockEntityData(stack)) {
                lines.add(Component.translatable("tooltip.openmodularlighting.configured"));
            }
        });
    }
}
