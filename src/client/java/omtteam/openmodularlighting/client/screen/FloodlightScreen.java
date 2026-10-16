package omtteam.openmodularlighting.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import omtteam.openmodularlighting.client.compat.ClientCompat;
import omtteam.openmodularlighting.menu.FloodlightMenu;

/**
 * Floodlight GUI as in 1.7.10: the 176 × 146 texture with one status line and no title labels.
 */
public abstract class FloodlightScreen<M extends FloodlightMenu> extends AbstractContainerScreen<M> {
    private final ResourceLocation texture;

    protected FloodlightScreen(M menu, Inventory inventory, Component title, ResourceLocation texture) {
        super(menu, inventory, title);
        this.texture = texture;
        this.imageWidth = 176;
        this.imageHeight = 146;
    }

    /** The status line next to the slot. */
    protected abstract Component statusText();

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientCompat.renderContainerBackground(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, statusText(), 50, 26, 0x000000, false);
    }
}
