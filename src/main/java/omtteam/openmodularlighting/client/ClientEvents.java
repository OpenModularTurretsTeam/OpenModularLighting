package omtteam.openmodularlighting.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.client.camo.CamoModel;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.client.render.FloodlightTint;
import omtteam.openmodularlighting.client.render.LightBlockDebugRenderer;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.registry.ModItems;

import java.util.Objects;

/**
 * The client events of the mod: models (item models, state mappers, the disguise wrapper), the dye tint, the
 * "Configured" tooltip and the light debug tool.
 */
public final class ClientEvents {
    private ClientEvents() {
    }

    /** Item models ({@code models/item/<name>.json}); light blocks without a facing ignore it in their blockstate. */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Item item : ModItems.ALL) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
        }
        // the cube light has no front: its blockstate file only has "lit"
        ModelLoader.setCustomStateMapper(ModBlocks.CUBE_LIGHT,
                new StateMap.Builder().ignore(FloodlightBlock.FACING).build());
    }

    /** Lights that can be disguised get the disguise model around their own (see {@link CamoModel}). */
    @SubscribeEvent
    public static void wrapCamoModels(ModelBakeEvent event) {
        for (ModelResourceLocation id : event.getModelRegistry().getKeys()) {
            if (CamoModels.isCamoModel(id)) {
                IBakedModel model = event.getModelRegistry().getObject(id);
                if (model != null) {
                    event.getModelRegistry().putObject(id, new CamoModel(model));
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler(FloodlightTint::color,
                ModBlocks.LIGHT_SOURCES.toArray(new Block[0]));
    }

    /** A floodlight item that kept its settings shows "Configured" (1.7.10 {@code ItemBlockFL}). */
    @SubscribeEvent
    public static void appendTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof FloodlightBlock
                && stack.getSubCompound(FloodlightBlock.BLOCK_ENTITY_TAG) != null) {
            event.getToolTip().add(I18n.format("tooltip.openmodularlighting.configured"));
        }
    }

    /** Light debug tool: wireframes around light blocks while it is held. */
    @SubscribeEvent
    public static void renderDebugWireframes(RenderWorldLastEvent event) {
        LightBlockDebugRenderer.render(event.getPartialTicks());
    }
}
