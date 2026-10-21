package omtteam.openmodularlighting.client.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

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
}
