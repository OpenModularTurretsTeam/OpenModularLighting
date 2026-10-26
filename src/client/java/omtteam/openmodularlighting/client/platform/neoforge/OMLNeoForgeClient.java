package omtteam.openmodularlighting.client.platform.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.client.OMLClient;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.client.compat.ClientCompat;
import omtteam.openmodularlighting.client.platform.ClientPlatform;
import omtteam.openmodularlighting.client.platform.ClientPlatformServices;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.neoforge.NeoForgeNetwork;
//? if >=1.21.11 {
/*import omtteam.openmodularlighting.camo.CamoBlock;
*///?}

/**
 * NeoForge client entrypoint: installs the client platform services, starts the client and wires NeoForge's client
 * events to the shared code ({@link OMLClient}). FML constructs it after the common {@code OMLNeoForge} (entrypoints for
 * more sides first), so {@link Platform} is already installed. Render types come from "render_type" in the block models.
 */
@Mod(value = OML.MOD_ID, dist = Dist.CLIENT)
public final class OMLNeoForgeClient {
    public OMLNeoForgeClient(IEventBus modBus) {
        ClientPlatform.install(new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network())));
        OMLClient.init();
        modBus.addListener((RegisterMenuScreensEvent event) -> OMLClient.registerScreens(event::register));
        modBus.addListener((RegisterColorHandlersEvent.Block event) ->
                event.register(FloodlightTint::color, FloodlightTint.tintedBlocks()));
        // the disguise model around the models of the lights that can be disguised (NeoForgeCamoModel)
        //? if >=1.21.11 {
        /*modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getBakingResult().blockStateModels()
                .replaceAll((state, model) -> state.getBlock() instanceof CamoBlock ? new NeoForgeCamoModel(model) : model));
        *///?} else {
        modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getModels().replaceAll((id, model) ->
                CamoModels.isCamoModel(id.id()) ? new NeoForgeCamoModel(model) : model));
        //?}
        // back to the local config after leaving a server
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> OMLConfig.useLocalConfig());
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
                OMLClient.appendTooltip(event.getItemStack(), event.getFlags(), event.getToolTip()));
        NeoForge.EVENT_BUS.addListener(OMLNeoForgeClient::onRenderLevelStage);
    }

    /** Light debug tool: wireframes around light blocks while it is held. */
    //? if >=1.21.11 {
    /*private static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        LightBlockDebugRenderer.render(minecraft.level, event.getPoseStack(), buffers,
                event.getLevelRenderState().cameraRenderState.pos);
        buffers.endBatch(ClientCompat.lines());
    }
    *///?} else {
    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        LightBlockDebugRenderer.render(minecraft.level, event.getPoseStack(), buffers, event.getCamera().getPosition());
        buffers.endBatch(ClientCompat.lines());
    }
    //?}
}
