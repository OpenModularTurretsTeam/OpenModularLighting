package omtteam.openmodularlighting.client.platform.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import omtteam.openmodularlighting.client.OMLClient;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.client.platform.ClientPlatform;
import omtteam.openmodularlighting.client.platform.ClientPlatformServices;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.forge.ForgeNetwork;

/**
 * Forge client entrypoint: installs the client platform services, starts the client and wires Forge's client events to
 * the shared code ({@link OMLClient}). Forge 1.20.1 has no client-only {@code @Mod} classes: the common
 * {@code OMLForge} constructs this one by name on a client, after installing {@link Platform}. Render types come from
 * "render_type" in the block models.
 */
public final class OMLForgeClient {
    public OMLForgeClient(IEventBus modBus) {
        ClientPlatform.install(new ClientPlatformServices(new ForgeClientNetwork((ForgeNetwork) Platform.network())));
        OMLClient.init();
        // no screen registration event on Forge 1.20.1: register on the main thread during client setup
        modBus.addListener((FMLClientSetupEvent event) ->
                event.enqueueWork(() -> OMLClient.registerScreens(MenuScreens::register)));
        modBus.addListener((RegisterColorHandlersEvent.Block event) ->
                event.register(FloodlightTint::color, FloodlightTint.tintedBlocks()));
        modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getModels().replaceAll((id, model) ->
                CamoModels.isCamoModel(id) ? new ForgeCamoModel(model) : model));
        // back to the local config after leaving a server
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> OMLConfig.useLocalConfig());
        MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
                OMLClient.appendTooltip(event.getItemStack(), event.getFlags(), event.getToolTip()));
        MinecraftForge.EVENT_BUS.addListener(OMLForgeClient::onRenderLevelStage);
    }

    /** Light debug tool: wireframes around light blocks while it is held. */
    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        LightBlockDebugRenderer.render(minecraft.level, event.getPoseStack(), buffers, event.getCamera().getPosition());
        buffers.endBatch(RenderType.lines());
    }
}
