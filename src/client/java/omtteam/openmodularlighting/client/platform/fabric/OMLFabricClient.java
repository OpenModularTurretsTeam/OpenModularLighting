package omtteam.openmodularlighting.client.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.client.OMLClient;
import omtteam.omlib.camo.CamoBlock;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.registry.ModBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

/**
 * Fabric client entrypoint: starts the client and wires Fabric's client callbacks to the shared code
 * ({@link OMLClient}). OMLib creates the client platform services and wraps the disguise models itself.
 */
public final class OMLFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OMLClient.init();
        OMLClient.registerScreens(MenuScreens::register);
        ColorProviderRegistry.BLOCK.register(FloodlightTint::color, FloodlightTint.tintedBlocks());
        // NeoForge reads these from "render_type" in the block models; Fabric ignores that field.
        // The lamp overlay of the floodlights (drawn alone over a disguise) is cut out. Only this mod's lights: other
        // mods' camouflage blocks (Open Modular Turrets) choose their own layer.
        Block[] camoLights = ModBlocks.LIGHT_SOURCES.stream().filter(CamoBlock.class::isInstance).toArray(Block[]::new);
        BlockRenderLayerMap.putBlock(ModBlocks.UV_LIGHT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.LIGHTPROOF_GLASS, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, camoLights);
        // light debug tool: wireframes around light blocks while it is held
        WorldRenderEvents.AFTER_ENTITIES.register(context -> LightBlockDebugRenderer.render(Minecraft.getInstance().level,
                context.matrices(), context.consumers(), context.worldState().cameraRenderState.pos));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> OMLClient.appendTooltip(stack, flag, lines));
    }
}
