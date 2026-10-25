package omtteam.openmodularlighting.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.container.FloodlightContainer;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

/**
 * Floodlight GUI as in 1.7.10: the 176 × 146 texture with one status line and no title labels. Lights that can be
 * disguised also show the disguise slot ({@link FloodlightContainer#hasCamoSlot()}) below the status line on the right.
 */
public abstract class FloodlightGui<M extends FloodlightContainer> extends GuiContainer {
    /** The light's slot on the texture (its background is copied for the disguise slot). */
    private static final int SLOT_U = 25;
    private static final int SLOT_V = 21;
    private static final int SLOT_SIZE = 18;
    private static final int HINT_WIDTH = 160;
    /** The "?" in the top right corner that explains the timeout. */
    private static final String HINT = "?";
    private static final int HINT_X = 164;
    private static final int HINT_Y = 6;
    private static final int STATUS_COLOR = 0x000000;
    private static final int LABEL_COLOR = 0x404040;
    private static final int HINT_HOVER_COLOR = 0xFFFFFF;

    protected final M menu;
    private final ResourceLocation texture;

    protected FloodlightGui(M menu, ResourceLocation texture) {
        super(menu);
        this.menu = menu;
        this.texture = texture;
        this.xSize = 176;
        this.ySize = 146;
    }

    /** The status line next to the slot. */
    protected abstract String statusText();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        Slot hovered = getSlotUnderMouse();
        if (menu.hasCamoSlot() && hovered != null && hovered.slotNumber == FloodlightContainer.CAMO_SLOT
                && !hovered.getHasStack()) {
            drawHoveringText(fontRenderer.listFormattedStringToWidth(
                    I18n.format("tooltip.openmodularlighting.camo_slot"), HINT_WIDTH), mouseX, mouseY);
        }
        if (isOverTimeoutHint(mouseX, mouseY)) {
            String seconds = FloodlightTileEntity.seconds(OMLConfig.get().general.timeoutFloodlights);
            drawHoveringText(fontRenderer.listFormattedStringToWidth(
                    I18n.format("tooltip.openmodularlighting.timeout_hint", seconds), HINT_WIDTH), mouseX, mouseY);
        }
    }

    private boolean isOverTimeoutHint(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        return menu.hasTimeoutHint() && x >= HINT_X - 1 && x < HINT_X + fontRenderer.getStringWidth(HINT) + 1
                && y >= HINT_Y - 1 && y < HINT_Y + fontRenderer.FONT_HEIGHT;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (menu.hasCamoSlot()) {
            drawTexturedModalRect(guiLeft + FloodlightContainer.CAMO_X - 1, guiTop + FloodlightContainer.CAMO_Y - 1,
                    SLOT_U, SLOT_V, SLOT_SIZE, SLOT_SIZE);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(statusText(), 50, 26, STATUS_COLOR);
        if (menu.hasCamoSlot()) {
            String label = I18n.format("gui.openmodularlighting.disguise");
            fontRenderer.drawString(label, FloodlightContainer.CAMO_X - 5 - fontRenderer.getStringWidth(label),
                    FloodlightContainer.CAMO_Y + 4, LABEL_COLOR);
        }
        if (menu.hasTimeoutHint()) {
            // the foreground layer gets screen coordinates for the mouse
            int color = isOverTimeoutHint(mouseX, mouseY) ? HINT_HOVER_COLOR : LABEL_COLOR;
            fontRenderer.drawString(HINT, HINT_X, HINT_Y, color);
        }
    }
}
