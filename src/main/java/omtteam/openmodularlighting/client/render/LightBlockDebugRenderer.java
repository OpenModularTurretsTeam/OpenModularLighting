package omtteam.openmodularlighting.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.registry.ModItems;
import omtteam.openmodularlighting.tileentity.LightTileEntity;
import omtteam.openmodularlighting.tileentity.UvLightTileEntity;

/**
 * The light debug tool (1.7.10 {@code TileEntityPhantomLightRenderer}): while it is held, every light block within
 * {@link #RANGE} blocks gets a wireframe box, yellow for phantom lights and magenta for UV lights. Unlit light blocks
 * (which 1.7.10 did not have) are drawn at {@link #UNLIT_BRIGHTNESS}.
 * <p>
 * Not a tile entity renderer: that would make the game visit every light block entity each frame. This world render
 * callback returns at once unless the tool is held, and only then looks for light blocks in the nearby chunks.
 */
public final class LightBlockDebugRenderer {
    /** Blocks around the camera; the default block entity render distance. */
    private static final int RANGE = 64;
    private static final int CHUNK_RANGE = (RANGE >> 4) + 1;
    private static final float UNLIT_BRIGHTNESS = 0.45F;
    /** 1.7.10 box: just inside the block, so it does not z-fight with neighbouring blocks. */
    private static final double INSET = 0.002;
    private static final float LINE_WIDTH = 2.0F;

    private LightBlockDebugRenderer() {
    }

    /** Whether the local player holds the light debug tool in either hand. */
    public static boolean isHoldingDebugTool() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && (player.getHeldItemMainhand().getItem() == ModItems.LIGHT_DEBUG_TOOL
                || player.getHeldItemOffhand().getItem() == ModItems.LIGHT_DEBUG_TOOL);
    }

    /** Draws the wireframes while the tool is held; called after the world is rendered. */
    public static void render(float partialTicks) {
        if (!isHoldingDebugTool()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        WorldClient world = minecraft.world;
        Entity camera = minecraft.getRenderViewEntity();
        if (world == null || camera == null) return;
        double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(LINE_WIDTH);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        int centerX = (int) Math.floor(x) >> 4;
        int centerZ = (int) Math.floor(z) >> 4;
        for (int chunkX = centerX - CHUNK_RANGE; chunkX <= centerX + CHUNK_RANGE; chunkX++) {
            for (int chunkZ = centerZ - CHUNK_RANGE; chunkZ <= centerZ + CHUNK_RANGE; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk != null) drawChunk(chunk, x, y, z);
            }
        }
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /** The light blocks of one loaded chunk within {@link #RANGE}. */
    private static void drawChunk(Chunk chunk, double x, double y, double z) {
        for (TileEntity tile : chunk.getTileEntityMap().values()) {
            if (tile instanceof LightTileEntity
                    && tile.getPos().distanceSqToCenter(x, y, z) <= (double) RANGE * RANGE) {
                drawBox((LightTileEntity) tile, x, y, z);
            }
        }
    }

    private static void drawBox(LightTileEntity light, double x, double y, double z) {
        BlockPos pos = light.getPos();
        IBlockState state = light.getWorld().getBlockState(pos);
        boolean lit = state.getBlock() instanceof LightBlock && state.getValue(LightBlock.LIT);
        boolean uv = light instanceof UvLightTileEntity;
        float brightness = lit ? 1F : UNLIT_BRIGHTNESS;
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(-INSET).offset(-x, -y, -z);
        RenderGlobal.drawSelectionBoundingBox(box, brightness, uv ? 0F : brightness, uv ? brightness : 0F, 1F);
    }
}
