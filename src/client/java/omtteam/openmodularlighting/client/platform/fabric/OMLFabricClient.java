package omtteam.openmodularlighting.client.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.client.OMLClient;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.client.platform.ClientPlatform;
import omtteam.openmodularlighting.client.platform.ClientPlatformServices;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.registry.ModBlocks;
//? if >=1.21.11 {
/*import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import omtteam.openmodularlighting.camo.CamoBlock;
*///?} else {
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.RenderType;
//?}

/**
 * Fabric client entrypoint: installs the client platform services, starts the client and wires Fabric's client
 * callbacks to the shared code ({@link OMLClient}).
 */
public final class OMLFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.install(new ClientPlatformServices(new FabricClientNetwork()));
        OMLClient.init();
        OMLClient.registerScreens(MenuScreens::register);
        // back to the local config after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> OMLConfig.useLocalConfig());
        ColorProviderRegistry.BLOCK.register(FloodlightTint::color, FloodlightTint.tintedBlocks());
        // NeoForge reads these from "render_type" in the block models; Fabric ignores that field.
        // The lamp overlay of the floodlights (drawn alone over a disguise) is cut out.
        //? if >=1.21.11 {
        /*BlockRenderLayerMap.putBlock(ModBlocks.UV_LIGHT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.LIGHTPROOF_GLASS, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, CamoModels.camoBlocks().toArray(Block[]::new));
        *///?} else {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UV_LIGHT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LIGHTPROOF_GLASS, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), CamoModels.camoBlocks().toArray(Block[]::new));
        //?}
        // the disguise model around the models of the lights that can be disguised (FabricCamoModel)
        ModelLoadingPlugin.register(plugin -> {
            //? if >=1.21.11 {
            /*plugin.modifyBlockModelAfterBake().register((model, context) ->
                    context.state().getBlock() instanceof CamoBlock ? new FabricCamoModel(model) : model);
            *///?} else if >=1.21 {
            plugin.modifyModelAfterBake().register((model, context) -> {
                var topLevel = context.topLevelId();
                return CamoModels.isCamoModel(topLevel != null ? topLevel.id() : context.resourceId())
                        ? new FabricCamoModel(model) : model;
            });
            //?} else {
            /*plugin.modifyModelAfterBake().register((model, context) ->
                    CamoModels.isCamoModel(context.id()) ? new FabricCamoModel(model) : model);
            *///?}
        });
        // light debug tool: wireframes around light blocks while it is held
        //? if >=1.21.11 {
        /*WorldRenderEvents.AFTER_ENTITIES.register(context -> LightBlockDebugRenderer.render(Minecraft.getInstance().level,
                context.matrices(), context.consumers(), context.worldState().cameraRenderState.pos));
        *///?} else {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> LightBlockDebugRenderer.render(context.world(),
                context.matrixStack(), context.consumers(), context.camera().getPosition()));
        //?}
        //? if >=1.21 {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> OMLClient.appendTooltip(stack, flag, lines));
        //?} else {
        /*ItemTooltipCallback.EVENT.register(OMLClient::appendTooltip);
        *///?}
    }
}
