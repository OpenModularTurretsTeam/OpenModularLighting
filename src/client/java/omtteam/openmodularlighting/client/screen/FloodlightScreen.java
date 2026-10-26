package omtteam.openmodularlighting.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.client.compat.ClientCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.menu.FloodlightMenu;

/**
 * Floodlight GUI as in 1.7.10: the 176 × 146 texture with one status line and no title labels. Lights that can be
 * disguised also show the disguise slot ({@link FloodlightMenu#hasCamoSlot()}) below the status line on the right.
 */
public abstract class FloodlightScreen<M extends FloodlightMenu> extends AbstractContainerScreen<M> {
    /** The light's slot on the texture (its background is copied for the disguise slot). */
    private static final int SLOT_U = 25;
    private static final int SLOT_V = 21;
    private static final int SLOT_SIZE = 18;
    private static final int CAMO_HINT_WIDTH = 160;
    /** The "?" in the top right corner that explains the timeout. */
    private static final String HINT = "?";
    private static final int HINT_X = 164;
    private static final int HINT_Y = 6;
    /** Text colours with alpha: from 1.21.6 on, text without alpha is not drawn. */
    private static final int STATUS_COLOR = 0xFF000000;
    private static final int LABEL_COLOR = 0xFF000000;
    private static final int HINT_HOVER_COLOR = 0xFFFFFFFF;

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
        Slot hovered = hoveredSlot;
        if (menu.hasCamoSlot() && hovered != null && hovered.index == FloodlightMenu.CAMO_SLOT && !hovered.hasItem()) {
            ClientCompat.renderTooltip(graphics, font, font.split(
                    Component.translatable("tooltip.openmodularlighting.camo_slot"), CAMO_HINT_WIDTH), mouseX, mouseY);
        }
        if (isOverTimeoutHint(mouseX, mouseY)) {
            String seconds = FloodlightBlockEntity.seconds(OMLConfig.get().general.timeoutFloodlights);
            ClientCompat.renderTooltip(graphics, font, font.split(
                    Component.translatable("tooltip.openmodularlighting.timeout_hint", seconds), CAMO_HINT_WIDTH),
                    mouseX, mouseY);
        }
    }

    private boolean isOverTimeoutHint(int mouseX, int mouseY) {
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        return menu.hasTimeoutHint() && x >= HINT_X - 1 && x < HINT_X + font.width(HINT) + 1
                && y >= HINT_Y - 1 && y < HINT_Y + font.lineHeight;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ClientCompat.blit(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.hasCamoSlot()) {
            ClientCompat.blit(graphics, texture, leftPos + FloodlightMenu.CAMO_X - 1, topPos + FloodlightMenu.CAMO_Y - 1,
                    SLOT_U, SLOT_V, SLOT_SIZE, SLOT_SIZE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, statusText(), 50, 26, STATUS_COLOR, false);
        if (menu.hasCamoSlot()) {
            Component label = Component.translatable("gui.openmodularlighting.disguise");
            graphics.drawString(font, label, FloodlightMenu.CAMO_X - 5 - font.width(label), FloodlightMenu.CAMO_Y + 4,
                    LABEL_COLOR, false);
        }
        if (menu.hasTimeoutHint()) {
            // renderLabels gets screen coordinates for the mouse, like render
            int color = isOverTimeoutHint(mouseX, mouseY) ? HINT_HOVER_COLOR : LABEL_COLOR;
            graphics.drawString(font, HINT, HINT_X, HINT_Y, color, false);
        }
    }
}
