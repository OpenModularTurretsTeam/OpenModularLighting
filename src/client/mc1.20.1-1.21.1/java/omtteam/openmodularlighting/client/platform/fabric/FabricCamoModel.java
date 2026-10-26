package omtteam.openmodularlighting.client.platform.fabric;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.client.camo.CamoModels;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Model of a light that can be disguised on Fabric (taken over from OpenModularTurrets; the NeoForge counterpart is
 * {@code NeoForgeCamoModel}): without a disguise it is the light's own model, with one it emits the quads of the
 * disguise's model into the normal chunk mesh, followed by the lamp quads of the light's own model
 * ({@link CamoModels#LAMP_TINT_INDEX}, cutout layer). The disguise gets proper ambient occlusion and face culling.
 * <p>
 * Tinted quads of the disguise (grass, ...) get its colour at the light's position, taken from the level view of the
 * chunk being meshed, so the client's biome blend setting applies like on the real block.
 * <p>
 * The variant for 1.20.1 and 1.21.1 (Fabric's baked model API); {@code OMLFabricClient} wraps the models.
 */
public final class FabricCamoModel extends ForwardingBakedModel {
    public FabricCamoModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
                               RenderContext context) {
        BlockState camo = camoAt(view, pos);
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (camo == null || renderer == null) {
            // no renderer implementation (e.g. Sodium on 1.20.1 without Indium): the light's own look
            super.emitBlockQuads(view, state, pos, randomSupplier, context);
            return;
        }
        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
        context.pushTransform(new DisguiseTransform(view, pos, camo, Minecraft.getInstance().getBlockColors(),
                renderer.materialFinder()));
        try {
            camoModel.emitBlockQuads(view, camo, pos, randomSupplier, context);
        } finally {
            context.popTransform();
        }
        // the lamp on top, in the light's own (cutout) layer and with its dye colour
        context.pushTransform(quad -> quad.colorIndex() == CamoModels.LAMP_TINT_INDEX);
        try {
            super.emitBlockQuads(view, state, pos, randomSupplier, context);
        } finally {
            context.popTransform();
        }
    }

    @Nullable
    private static BlockState camoAt(BlockAndTintGetter view, BlockPos pos) {
        return view.getBlockEntityRenderData(pos) instanceof BlockState camo ? camo : null;
    }

    /**
     * Puts the quads of the disguise into the render layer of the disguise (the light's layer is the cutout one) and
     * applies its tint.
     */
    private static final class DisguiseTransform implements RenderContext.QuadTransform {
        private final BlockAndTintGetter view;
        private final BlockPos pos;
        private final BlockState camo;
        private final BlockColors colors;
        private final MaterialFinder finder;
        private final BlendMode blendMode;

        private DisguiseTransform(BlockAndTintGetter view, BlockPos pos, BlockState camo, BlockColors colors,
                                  MaterialFinder finder) {
            this.view = view;
            this.pos = pos;
            this.camo = camo;
            this.colors = colors;
            this.finder = finder;
            this.blendMode = BlendMode.fromRenderLayer(ItemBlockRenderTypes.getChunkRenderType(camo));
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            quad.material(finder.copyFrom(quad.material()).blendMode(blendMode).find());
            int tintIndex = quad.colorIndex();
            if (tintIndex != -1) {
                int tint = colors.getColor(camo, view, pos, tintIndex);
                if (tint != -1) {
                    for (int vertex = 0; vertex < 4; vertex++) {
                        quad.color(vertex, multiply(quad.color(vertex), 0xFF000000 | tint));
                    }
                }
                // the tint is applied; the light's colour provider would apply the dye colour instead
                quad.colorIndex(-1);
            }
            return true;
        }

        private static int multiply(int first, int second) {
            int a = channel(first, 24) * channel(second, 24) / 255;
            int r = channel(first, 16) * channel(second, 16) / 255;
            int g = channel(first, 8) * channel(second, 8) / 255;
            int b = channel(first, 0) * channel(second, 0) / 255;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private static int channel(int argb, int shift) {
            return (argb >>> shift) & 0xFF;
        }
    }
}
