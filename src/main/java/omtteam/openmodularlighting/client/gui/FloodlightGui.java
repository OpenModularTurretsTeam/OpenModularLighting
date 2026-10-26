package omtteam.openmodularlighting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import omtteam.omlib.network.MessageOpenCamoMenu;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.container.FloodlightContainer;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

/**
 * Floodlight GUI as in 1.7.10: the 176 × 146 texture with one status line and no title labels. Lights that can be
 * disguised also show a button below the status line on the right ({@link FloodlightContainer#hasCamoButton()}) that
 * opens OMLib's camouflage menu.
 */
public abstract class FloodlightGui<M extends FloodlightContainer> extends GuiContainer {
    private static final int HINT_WIDTH = 160;
    /** The "?" in the top right corner that explains the timeout. */
    private static final String HINT = "?";
    private static final int HINT_X = 164;
    private static final int HINT_Y = 6;
    private static final int STATUS_COLOR = 0x000000;
    private static final int LABEL_COLOR = 0x000000;
    private static final int HINT_HOVER_COLOR = 0xFFFFFF;
    private static final int CAMO_BUTTON = 0;
    private static final int CAMO_WIDTH = 70;
    private static final int CAMO_X = 176 - 8 - CAMO_WIDTH;
    private static final int CAMO_Y = 36;

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
    public void initGui() {
        super.initGui();
        buttonList.clear();
        if (menu.hasCamoButton()) {
            buttonList.add(new GuiButton(CAMO_BUTTON, guiLeft + CAMO_X, guiTop + CAMO_Y, CAMO_WIDTH, 20,
                    I18n.format("gui.openmodularlighting.camouflage")));
        }
    }

    /** The camouflage menu has a container, so the server opens it (and checks reach). */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == CAMO_BUTTON) {
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageOpenCamoMenu(menu.getPos()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(statusText(), 50, 26, STATUS_COLOR);
        if (menu.hasTimeoutHint()) {
            // the foreground layer gets screen coordinates for the mouse
            int color = isOverTimeoutHint(mouseX, mouseY) ? HINT_HOVER_COLOR : LABEL_COLOR;
            fontRenderer.drawString(HINT, HINT_X, HINT_Y, color);
        }
    }
}
