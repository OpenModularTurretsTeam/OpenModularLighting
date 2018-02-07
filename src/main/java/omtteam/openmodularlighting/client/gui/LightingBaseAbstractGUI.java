package omtteam.openmodularlighting.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import omtteam.omlib.client.gui.BlockingAbstractGuiContainer;
import omtteam.omlib.client.gui.IHasTooltips;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.PlayerUtil;
import omtteam.openmodularlighting.handler.NetworkingHandler;
import omtteam.openmodularlighting.network.messages.MessageAdjustRange;
import omtteam.openmodularlighting.network.messages.MessageDropBase;
import omtteam.openmodularlighting.network.messages.MessageDropLights;
import omtteam.openmodularlighting.network.messages.MessageToggleMode;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.tileentity.lights.AbstractLightSource;
import omtteam.openmodularlighting.tileentity.lights.Floodlight;

import java.awt.*;
import java.util.ArrayList;

import static omtteam.omlib.util.GeneralUtil.*;

/**
 * Created by nico on 6/4/15.
 * Abstract class for all turret base GUIs.
 */

class LightingBaseAbstractGUI extends BlockingAbstractGuiContainer implements IHasTooltips {
    private int mouseX;
    private int mouseY;
    private final EntityPlayer player;
    final LightingBase base;
    private AbstractLightSource source;

    LightingBaseAbstractGUI(InventoryPlayer inventoryPlayer, LightingBase tileEntity, Container container) {
        super(container);
        this.base = tileEntity;
        player = inventoryPlayer.player;
        DebugHandler.getInstance().setPlayer(player);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        if (PlayerUtil.isPlayerOwner(player, base)) {
            this.buttonList.add(new GuiButton(1, x + 5, y + 15, 60, 20, safeLocalize(OMLibNames.Localizations.GUI.NEXT)));
            this.buttonList.add(new GuiButton(2, x + 5, y + 50, 60, 20, safeLocalize(OMLibNames.Localizations.GUI.PREVIOUS)));
            this.buttonList.add(new GuiButton(1, x + 120, y + 15, 20, 20, "+"));
            this.buttonList.add(new GuiButton(2, x + 120, y + 50, 20, 20, "-"));
            this.buttonList.add(new GuiButton(3, x + 180, y, 80, 20, safeLocalize(OMLNames.Localizations.GUI.DROP_LIGHTS)));
            this.buttonList.add(new GuiButton(4, x + 180, y + 25, 80, 20, safeLocalize(OMLNames.Localizations.GUI.DROP_BASE)));
            this.buttonList.add(new GuiButton(5, x + 155, y + 3, 11, 10, "M"));
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.mouseX = par1;
        this.mouseY = par2;
        super.drawScreen(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 1) {
            if (source instanceof Floodlight)
                ((Floodlight) this.source).setCurrentRange((((Floodlight) this.source).getCurrentRange() + 1));
            sendChangeToServer();
        }

        if (guibutton.id == 2) {
            if (source instanceof Floodlight)
                ((Floodlight) this.source).setCurrentRange((((Floodlight) this.source).getCurrentRange() - 1));
            sendChangeToServer();
        }

        if (guibutton.id == 3) {
            sendDropLightsToServer();
        }

        if (guibutton.id == 4) {
            sendDropBaseToServer();
        }

        if (guibutton.id == 5) {
            sendToggleModeToServer();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerForegroundLayer(int param1, int param2) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        fontRenderer.drawString(base.getTier() > 1 ? safeLocalize(OMLNames.Localizations.GUI.ADDONS) : "", 71, 6, 0);
        fontRenderer.drawString(base.getTier() > 1 ? safeLocalize(OMLNames.Localizations.GUI.UPGRADES) : "", 71, 39, 0);

        fontRenderer.drawString(safeLocalize(OMLNames.Localizations.GUI.RANGE), 116, 6, 0);

        ArrayList targetInfo = new ArrayList();

        targetInfo.add("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.OWNER) + ": \u00A7f" + base.getOwnerName());
        targetInfo.add("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.MODE) + ": \u00A7f" + getMachineModeLocalization(base.getMode()));
        boolean isCurrentlyOn = base.isActive();
        targetInfo.add("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.ACTIVE) + ": " + getColoredBooleanLocalizationYesNo(isCurrentlyOn));
        targetInfo.add("");

        this.drawHoveringText(targetInfo, -128, 17, fontRenderer);

        drawTooltips();
    }

    @Override
    public void drawTooltips() {
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        int tooltipToDraw = buttonList.stream().filter(GuiButton::isMouseOver).mapToInt(s -> s.id).sum();
        ArrayList<String> tooltip = new ArrayList<>();
        switch (tooltipToDraw) {
            case 5:
                tooltip.add(safeLocalize(OMLNames.Localizations.Tooltip.TOGGLE_MODE));
                break;
        }

        if (mouseX > k + 153 && mouseX < k + 153 + 14 && mouseY > l + 17 && mouseY < l + 17 + 51) {
            tooltip.add(base.getEnergyLevel(EnumFacing.DOWN) + "/" + base.getMaxEnergyLevel(EnumFacing.DOWN) + " RF");
            tooltip.add("EU Buffer: " + Math.round(base.getStorageEU()) + "/" + Math.round(base.getMaxStorageEU()));
        }
        if (base.getTier() > 1 && mouseX > k + 71 && mouseX < k + 71 + 40 && mouseY > l + 6 && mouseY < l + 6 + 14) {
            tooltip.add(safeLocalize(OMLNames.Localizations.Tooltip.ADDON_SLOT));
        }
        if (base.getTier() > 1 && mouseX > k + 71 && mouseX < k + 71 + 40 && mouseY > l + 39 && mouseY < l + 39 + 14) {
            tooltip.add(safeLocalize(OMLNames.Localizations.Tooltip.UPGRADE_SLOT));
        }
        if (mouseX > k + 123 && mouseX < k + 134 && mouseY > l + 35 && mouseY < l + 48) {
            tooltip.add(safeLocalize(OMLNames.Localizations.Tooltip.BASE_MAX_RANGE));
        }
        if (!tooltip.isEmpty())
            this.drawHoveringText(tooltip, mouseX - k, mouseY - l, Minecraft.getMinecraft().fontRendererObj);
    }

    private void sendChangeToServer() {
        if (source instanceof Floodlight) {
            MessageAdjustRange message = new MessageAdjustRange(base.getPos().getX(), base.getPos().getY(), base.getPos().getZ(), ((Floodlight) source).getCurrentRange());


            NetworkingHandler.INSTANCE.sendToServer(message);
        }
    }


    private void sendDropLightsToServer() {
        MessageDropLights message = new MessageDropLights(base.getPos().getX(), base.getPos().getY(), base.getPos().getZ());
        NetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendDropBaseToServer() {
        MessageDropBase message = new MessageDropBase(base.getPos().getX(), base.getPos().getY(), base.getPos().getZ());
        NetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendToggleModeToServer() {
        MessageToggleMode message = new MessageToggleMode(base.getPos().getX(), base.getPos().getY(), base.getPos().getZ());
        NetworkingHandler.INSTANCE.sendToServer(message);
    }

    @Override
    public ArrayList<Rectangle> getBlockingAreas() {
        ArrayList<Rectangle> list = new ArrayList<>();
        Rectangle rectangleGUI = new Rectangle(0, 0, 0, 0);
        if (player.getUniqueID().toString().equals(base.getOwner())) {
            rectangleGUI = new Rectangle((width - xSize) / 2 + 180, (height - ySize) / 2, 80, 95);
        }
        list.add(rectangleGUI);
        return list;
    }
}
