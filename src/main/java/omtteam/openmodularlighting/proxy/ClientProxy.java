package omtteam.openmodularlighting.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.render.CamoBlockColor;
import omtteam.openmodularlighting.client.render.models.LightingBaseBakedModel;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.init.ModItems;
import omtteam.openmodularlighting.items.*;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private void registerItemModel(final Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName().toString().toLowerCase()));
    }

    private void registerItemModel(final Item item, int meta, final String variantName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(item.getRegistryName().toString().toLowerCase()), variantName));
    }

    @SuppressWarnings("SameParameterValue")
    private void registerItemModel(final Item item, int meta, final String customName, boolean useCustomName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + customName.toLowerCase()));
    }

    @SuppressWarnings("ConstantConditions")
    private void registerBlockModelAsItem(final Block block, int meta, final String blockName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + blockName, "inventory"));
    }

    @SuppressWarnings("ConstantConditions")
    private void registerBlockModelAsItem(final Block block, int meta, final String blockName, String variantName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + blockName, variantName));
    }

    @Override
    public void preInit() {
        super.preInit();

        ModelLoaderRegistry.registerLoader(new LightingBaseBakedModel.ModelLoader());
        ModelLoader.setCustomStateMapper(ModBlocks.lightingBase, new LightingBaseBakedModel.Statemapper());

        for (int i = 0; i < 5; i++) {
            registerBlockModelAsItem(ModBlocks.lightingBase, i, OMLNames.Blocks.lightingBase + "_normal", "tier=" + (i + 1));
        }
        for (int i = 0; i < 5; i++) {
            registerBlockModelAsItem(ModBlocks.floodlight, i, OMLNames.Blocks.floodlight, "facing=north,tier=" + i);
        }
        for (int i = 0; i < 5; i++) {
            registerItemModel(ModItems.intermediateProductTiered, i, IntermediateProductTiered.subNames[i], true);
        }
        for (int i = 0; i < 2; i++) {
            registerItemModel(ModItems.addonMetaItem, i, AddonMetaItem.subNames[i], true);
        }
        for (int i = 0; i < 1; i++) {
            registerItemModel(ModItems.upgradeMetaItem, i, UpgradeMetaItem.subNames[i], true);
        }
        for (int i = 0; i < 1; i++) {
            registerItemModel(ModItems.intermediateProductRegular, i, IntermediateProductRegular.subNames[i], true);
        }
        for (int i = 0; i < 1; i++) {
            registerItemModel(ModItems.usableMetaItem, i, UsableMetaItem.subNames[i], true);
        }
    }

    @Override
    public void init() {
        super.init();
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new CamoBlockColor(), ModBlocks.lightingBase);
    }

    @Override
    protected void initTileRenderers() {
        super.initTileRenderers();

    }

    @Override
    protected void initEntityRenderers() {
        super.initEntityRenderers();
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
    }
}