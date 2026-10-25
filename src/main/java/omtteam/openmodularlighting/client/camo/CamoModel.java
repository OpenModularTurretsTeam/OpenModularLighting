package omtteam.openmodularlighting.client.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import omtteam.omlib.api.render.camo.RenderBlockStateContainer;
import omtteam.omlib.block.BlockAbstractCamoTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model of a light that can be disguised (the 1.12.2 counterpart of the main branch's camouflage models): without a
 * disguise it is the light's own model in the cutout layer; with one it returns the quads of the disguise's model for
 * each layer the disguise is drawn in, plus the lamp quads of the light's own model
 * ({@link CamoModels#LAMP_TINT_INDEX}) in the cutout layer. The disguise comes from OMLib's
 * {@link BlockAbstractCamoTileEntity#RENDERBLOCKSTATE} in the extended state; the block renders in every layer a valid
 * disguise may use.
 * <p>
 * Not OMLib's {@code CamoBakedModel}: that one draws the disguise only, without the lamp on top.
 */
public final class CamoModel implements IBakedModel {
    private final IBakedModel original;

    public CamoModel(IBakedModel original) {
        this.original = original;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        // null outside chunk rendering (break animation, item-like rendering): everything
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        boolean lampLayer = layer == null || layer == BlockRenderLayer.CUTOUT;
        IBlockState camo = camo(state);
        if (camo == null) {
            return lampLayer ? original.getQuads(state, side, rand) : Collections.emptyList();
        }
        List<BakedQuad> quads = new ArrayList<>();
        if (layer == null || camo.getBlock().canRenderInLayer(camo, layer)) {
            quads.addAll(model(camo).getQuads(camo, side, rand));
        }
        if (lampLayer) {
            for (BakedQuad quad : original.getQuads(state, side, rand)) {
                if (quad.getTintIndex() == CamoModels.LAMP_TINT_INDEX) quads.add(quad);
            }
        }
        return quads;
    }

    @Nullable
    private static IBlockState camo(@Nullable IBlockState state) {
        if (!(state instanceof IExtendedBlockState)) return null;
        RenderBlockStateContainer camo =
                ((IExtendedBlockState) state).getValue(BlockAbstractCamoTileEntity.RENDERBLOCKSTATE);
        return camo == null ? null : camo.getRenderState();
    }

    private static IBakedModel model(IBlockState camo) {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camo);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        IBlockState camo = camo(state);
        return camo == null ? original.isAmbientOcclusion(state) : model(camo).isAmbientOcclusion(camo);
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return original.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return original.getOverrides();
    }
}
