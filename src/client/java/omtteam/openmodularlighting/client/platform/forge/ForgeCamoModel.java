package omtteam.openmodularlighting.client.platform.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of a light that can be disguised on Forge, the same as {@code NeoForgeCamoModel} (whose Forge 1.20.1 API it
 * shares): without a disguise it is the light's own model, with one it returns the quads and render types of the
 * disguise's model, plus the lamp quads of the light's own model ({@link CamoModels#LAMP_TINT_INDEX}) in the light's
 * cutout layer. The block entity hands the disguise over as {@link CompatBlockEntity#RENDER_DATA} in its model data.
 * <p>
 * Unlike on NeoForge, ambient occlusion stays the light's own: Forge 1.20.1 does not pass the model data there. The
 * valid disguises are full opaque blocks, which use ambient occlusion like the light.
 */
public final class ForgeCamoModel extends BakedModelWrapper<BakedModel> {
    public ForgeCamoModel(BakedModel original) {
        super(original);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                             @NotNull ModelData data, @Nullable RenderType renderType) {
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
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                                      @NotNull ModelData data) {
        BlockState camo = camo(data);
        ChunkRenderTypeSet own = super.getRenderTypes(state, rand, data);
        return camo == null ? own : ChunkRenderTypeSet.union(model(camo).getRenderTypes(camo, rand, ModelData.EMPTY), own);
    }

    @Nullable
    private static BlockState camo(ModelData data) {
        return data.get(CompatBlockEntity.RENDER_DATA) instanceof BlockState camo ? camo : null;
    }

    private static BakedModel model(BlockState camo) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
    }
}
