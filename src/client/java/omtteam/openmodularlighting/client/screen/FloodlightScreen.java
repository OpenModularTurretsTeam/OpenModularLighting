package omtteam.openmodularlighting.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.omlib.client.compat.ClientCompat;
import omtteam.omlib.client.compat.ClientNetworkCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.menu.FloodlightMenu;
import omtteam.omlib.network.CamoPackets;
import omtteam.omlib.network.OMLibPackets;

/**
 * Floodlight GUI as in 1.7.10: the 176 × 146 texture with one status line and no title labels. Lights that can be
 * disguised also show a button below the status line on the right that opens the camouflage menu.
 */
public abstract class FloodlightScreen<M extends FloodlightMenu> extends AbstractContainerScreen<M> {
    private static final int HINT_WIDTH = 160;
    /** The camouflage button: below the status line, right of the light's slot. */
    private static final int CAMO_BUTTON_X = 88;
    private static final int CAMO_BUTTON_Y = 38;
    private static final int CAMO_BUTTON_WIDTH = 80;
    private static final int CAMO_BUTTON_HEIGHT = 18;
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

    @Override
    protected void init() {
        super.init();
        if (menu.supportsCamo()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.openmodularlighting.camouflage"), b -> openCamo())
                    .bounds(leftPos + CAMO_BUTTON_X, topPos + CAMO_BUTTON_Y, CAMO_BUTTON_WIDTH, CAMO_BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("tooltip.openmodularlighting.camouflage")))
                    .build());
        }
    }

    /** Opens the camouflage menu (a server-side menu, like this one). */
    private void openCamo() {
        ClientNetworkCompat.sendToServer(OMLibPackets.OPEN_CAMO_MENU, new CamoPackets.OpenMenu(menu.getPos()));
    }

    /** The status line next to the slot. */
    protected abstract Component statusText();

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientCompat.renderContainerBackground(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isOverTimeoutHint(mouseX, mouseY)) {
            String seconds = FloodlightBlockEntity.seconds(OMLConfig.get().general.timeoutFloodlights);
            ClientCompat.renderTooltip(graphics, font, font.split(
                    Component.translatable("tooltip.openmodularlighting.timeout_hint", seconds), HINT_WIDTH),
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
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, statusText(), 50, 26, STATUS_COLOR, false);
        if (menu.hasTimeoutHint()) {
            // renderLabels gets screen coordinates for the mouse, like render
            int color = isOverTimeoutHint(mouseX, mouseY) ? HINT_HOVER_COLOR : LABEL_COLOR;
            graphics.drawString(font, HINT, HINT_X, HINT_Y, color, false);
        }
    }
}
