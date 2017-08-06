package omtteam.openmodularlighting.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import omtteam.openmodularlighting.client.gui.containers.LightingBaseTierTwoContainer;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.tileentity.LightingBase;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class LightingBaseTierTwoGUI extends LightingBaseAbstractGUI {
    public LightingBaseTierTwoGUI(InventoryPlayer inventoryPlayer, LightingBase tileEntity) {
        super(inventoryPlayer, tileEntity, new LightingBaseTierTwoContainer(inventoryPlayer, tileEntity));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        ResourceLocation texture = (new ResourceLocation(OMLNames.Textures.lightingBaseTierTwoGUI));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(texture);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        int expression = (base.getEnergyLevel(EnumFacing.DOWN) * 51) / base.getMaxEnergyLevel(
                EnumFacing.DOWN);

        drawTexturedModalRect(x + 153, y + 17, 178, 17, 14, 51);

        int next = new Random().nextInt(3);

        if (next == 0) {
            drawTexturedModalRect(x + 153, y + 17 + 51 - expression, 196, 68 - expression, 14, expression);
        }

        if (next == 1) {
            drawTexturedModalRect(x + 153, y + 17 + 51 - expression, 215, 68 - expression, 14, expression);
        }

        if (next == 2) {
            drawTexturedModalRect(x + 153, y + 17 + 51 - expression, 234, 68 - expression, 14, expression);
        }
    }
}