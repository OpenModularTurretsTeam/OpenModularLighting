package omtteam.openmodularlighting.client.platform.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of a light that can be disguised on NeoForge (taken over from OpenModularTurrets; the Fabric counterpart is
 * {@code FabricCamoModel}): without a disguise it is the light's own model, with one it returns the quads, render
 * types and ambient occlusion of the disguise's model, plus the lamp quads of the light's own model
 * ({@link CamoModels#LAMP_TINT_INDEX}) in the light's cutout layer. The block entity hands the disguise over as
 * {@link CompatBlockEntity#RENDER_DATA} in its model data.
 * <p>
 * Tinted quads keep their tint index; {@code FloodlightTint} gives the disguise's quads the disguise's colour and the
 * lamp the dye colour, so the vanilla mesher tints them with the client's biome blend like on the real block.
 */
public final class NeoForgeCamoModel extends BakedModelWrapper<BakedModel> {
    public NeoForgeCamoModel(BakedModel original) {
        super(original);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        BlockState camo = camo(data);
        if (camo == null) return super.getQuads(state, side, rand, data, renderType);
        List<BakedQuad> quads = new ArrayList<>();
        BakedModel camoModel = model(camo);
        // vanilla models ignore the render type: only ask the disguise for its own layers
        if (renderType == null || camoModel.getRenderTypes(camo, rand, ModelData.EMPTY).contains(renderType)) {
            quads.addAll(camoModel.getQuads(camo, side, rand, ModelData.EMPTY, renderType));
        }
        if (state != null && (renderType == null || super.getRenderTypes(state, rand, data).contains(renderType))) {
            for (BakedQuad quad : super.getQuads(state, side, rand, data, renderType)) {
                if (quad.getTintIndex() == CamoModels.LAMP_TINT_INDEX) quads.add(quad);
            }
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        BlockState camo = camo(data);
        ChunkRenderTypeSet own = super.getRenderTypes(state, rand, data);
        return camo == null ? own : ChunkRenderTypeSet.union(model(camo).getRenderTypes(camo, rand, ModelData.EMPTY), own);
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        BlockState camo = camo(data);
        return camo == null ? super.useAmbientOcclusion(state, data, renderType)
                : model(camo).useAmbientOcclusion(camo, ModelData.EMPTY, renderType);
    }

    @Nullable
    private static BlockState camo(ModelData data) {
        return data.get(CompatBlockEntity.RENDER_DATA) instanceof BlockState camo ? camo : null;
    }

    private static BakedModel model(BlockState camo) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
    }
}
