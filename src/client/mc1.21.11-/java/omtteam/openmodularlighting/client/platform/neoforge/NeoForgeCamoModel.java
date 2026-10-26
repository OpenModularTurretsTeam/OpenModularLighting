package omtteam.openmodularlighting.client.platform.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of a light that can be disguised on NeoForge (taken over from OpenModularTurrets; the Fabric counterpart is
 * {@code FabricCamoModel}): without a disguise it is the light's own model, with one it consists of the parts of the
 * disguise's model (with their render types and ambient occlusion), plus the lamp quads of the light's own model
 * ({@link CamoModels#LAMP_TINT_INDEX}) in the light's cutout layer. The block entity hands the disguise over as
 * {@link CompatBlockEntity#RENDER_DATA} in its model data.
 * <p>
 * Tinted quads keep their tint index; {@code FloodlightTint} gives the disguise's quads the disguise's colour and the
 * lamp the dye colour, so the vanilla mesher tints them with the client's biome blend like on the real block.
 * <p>
 * The variant for 1.21.11 (block state models made of parts); {@code OMLNeoForgeClient} wraps the models.
 */
public final class NeoForgeCamoModel extends DelegateBlockStateModel {
    public NeoForgeCamoModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockModelPart> parts) {
        BlockState camo = level.getModelData(pos).get(CompatBlockEntity.RENDER_DATA) instanceof BlockState found
                ? found : null;
        if (camo == null) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        BlockStateModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
        for (BlockModelPart part : camoModel.collectParts(level, pos, camo, random)) {
            parts.add(new DisguisePart(part, camo));
        }
        for (BlockModelPart part : delegate.collectParts(level, pos, state, random)) {
            parts.add(new LampPart(part));
        }
    }

    /** A part of the disguise's model, drawn in the disguise's render type (the light's is the cutout one). */
    private record DisguisePart(BlockModelPart part, BlockState camo) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction side) {
            return part.getQuads(side);
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean useAmbientOcclusion() {
            return part.useAmbientOcclusion();
        }

        @Override
        public TriState ambientOcclusion() {
            return part.ambientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return part.particleIcon();
        }

        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            return part.getRenderType(camo);
        }
    }

    /** Only the lamp quads of a part of the light's own model. */
    private record LampPart(BlockModelPart part) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction side) {
            List<BakedQuad> lamp = new ArrayList<>();
            for (BakedQuad quad : part.getQuads(side)) {
                if (quad.tintIndex() == CamoModels.LAMP_TINT_INDEX) lamp.add(quad);
            }
            return lamp;
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean useAmbientOcclusion() {
            return part.useAmbientOcclusion();
        }

        @Override
        public TriState ambientOcclusion() {
            return part.ambientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return part.particleIcon();
        }

        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            return part.getRenderType(state);
        }
    }
}
