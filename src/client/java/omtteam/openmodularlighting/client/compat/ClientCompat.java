package omtteam.openmodularlighting.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
//? if >=1.21.11 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.Shapes;
*///?} else {
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
//?}

import java.util.List;

/**
 * Hides client-side API differences between the supported Minecraft versions.
 */
public final class ClientCompat {
    private ClientCompat() {
    }

    /**
     * Draws the dimmed background of a container screen. Only needed on 1.20.1; on 1.21 {@code Screen.render}
     * already does it.
     */
    public static void renderContainerBackground(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
                                                 float partialTick) {
        //? if <1.21 {
        /*screen.renderBackground(graphics);
        *///?}
    }

    /** Draws a part of a 256 × 256 GUI texture (1.21.11 also takes the render pipeline and the texture size). */
    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width,
                            int height) {
        //? if >=1.21.11 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
        *///?} else {
        graphics.blit(texture, x, y, u, v, width, height);
        //?}
    }

    /** Shows a tooltip at the mouse; 1.21.11 draws it at the end of the frame. */
    public static void renderTooltip(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int mouseX,
                                     int mouseY) {
        //? if >=1.21.11 {
        /*graphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
        *///?} else {
        graphics.renderTooltip(font, lines, mouseX, mouseY);
        //?}
    }

    /** The render type of lines in the world ({@code RenderTypes} on 1.21.11). */
    public static RenderType lines() {
        //? if >=1.21.11 {
        /*return RenderTypes.lines();
        *///?} else {
        return RenderType.lines();
        //?}
    }

    /** Draws the 12 edges of a box into a {@link #lines()} buffer; the colour components go from 0 to 1. */
    public static void renderLineBox(PoseStack poseStack, VertexConsumer lines, double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ, float red, float green, float blue,
                                     float alpha) {
        //? if >=1.21.11 {
        /*// 1.21.11 lines carry their width; this is the one of the block outline
        ShapeRenderer.renderShape(poseStack, lines, Shapes.box(minX, minY, minZ, maxX, maxY, maxZ), 0, 0, 0,
                ARGB.colorFromFloat(alpha, red, green, blue), Minecraft.getInstance().getWindow().getAppropriateLineWidth());
        *///?} else {
        LevelRenderer.renderLineBox(poseStack, lines, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        //?}
    }
}
