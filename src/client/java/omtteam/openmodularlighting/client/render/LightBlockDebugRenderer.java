package omtteam.openmodularlighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.LightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvLightBlockEntity;
import omtteam.openmodularlighting.registry.ModItems;

/**
 * The light debug tool (1.7.10 {@code TileEntityPhantomLightRenderer}): while it is held, every light block within
 * {@link #RANGE} blocks gets a wireframe box, yellow for phantom lights and magenta for UV lights. Unlit light blocks
 * (which 1.7.10 did not have) are drawn at {@link #UNLIT_BRIGHTNESS}.
 * <p>
 * Not a block entity renderer: that would make the game visit every light block entity each frame. This world render
 * callback returns at once unless the tool is held, and only then looks for light blocks in the nearby chunks.
 */
public final class LightBlockDebugRenderer {
    /** Blocks around the camera; the default block entity render distance. */
    private static final int RANGE = 64;
    private static final int CHUNK_RANGE = SectionPos.blockToSectionCoord(RANGE) + 1;
    private static final float UNLIT_BRIGHTNESS = 0.45F;
    /** 1.7.10 box: just inside the block, so it does not z-fight with neighbouring blocks. */
    private static final double INSET = 0.002;

    private LightBlockDebugRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(LightBlockDebugRenderer::render);
    }

    /** Whether the local player holds the light debug tool in either hand. */
    public static boolean isHoldingDebugTool() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && (player.getMainHandItem().is(ModItems.LIGHT_DEBUG_TOOL)
                || player.getOffhandItem().is(ModItems.LIGHT_DEBUG_TOOL));
    }

    private static void render(WorldRenderContext context) {
        if (!isHoldingDebugTool()) return;
        ClientLevel level = context.world();
        MultiBufferSource buffers = context.consumers();
        if (level == null || buffers == null) return;
        Vec3 camera = context.camera().getPosition();
        PoseStack poseStack = context.matrixStack();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        int centerX = SectionPos.blockToSectionCoord(camera.x);
        int centerZ = SectionPos.blockToSectionCoord(camera.z);
        for (int chunkX = centerX - CHUNK_RANGE; chunkX <= centerX + CHUNK_RANGE; chunkX++) {
            for (int chunkZ = centerZ - CHUNK_RANGE; chunkZ <= centerZ + CHUNK_RANGE; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (blockEntity instanceof LightBlockEntity light
                            && light.getBlockPos().closerToCenterThan(camera, RANGE)) {
                        drawBox(poseStack, lines, camera, light);
                    }
                }
            }
        }
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer lines, Vec3 camera, LightBlockEntity light) {
        BlockPos pos = light.getBlockPos();
        boolean uv = light instanceof UvLightBlockEntity;
        float brightness = light.getBlockState().getValue(LightBlock.LIT) ? 1F : UNLIT_BRIGHTNESS;
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        LevelRenderer.renderLineBox(poseStack, lines, INSET, INSET, INSET, 1 - INSET, 1 - INSET, 1 - INSET,
                brightness, uv ? 0F : brightness, uv ? brightness : 0F, 1F);
        poseStack.popPose();
    }
}
