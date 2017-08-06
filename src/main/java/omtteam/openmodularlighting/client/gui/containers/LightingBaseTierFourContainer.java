package omtteam.openmodularlighting.client.gui.containers;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import omtteam.openmodularlighting.client.gui.customSlot.AddonSlot;
import omtteam.openmodularlighting.client.gui.customSlot.UpgradeSlot;
import omtteam.openmodularlighting.tileentity.LightingBase;

public class LightingBaseTierFourContainer extends LightingBaseContainer {
    public LightingBaseTierFourContainer(InventoryPlayer inventoryPlayer, LightingBase te) {
        this.tileEntity = te;

        for (int x = 0; x < 9; x++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, x, 8 + x * 18, 142));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + x + y * 9, 8 + x * 18, 84 + y * 18));
            }
        }

        addSlotToContainer(new AddonSlot(tileEntity, 9, 72, 18));
        addSlotToContainer(new AddonSlot(tileEntity, 10, 92, 18));
        addSlotToContainer(new UpgradeSlot(tileEntity, 11, 72, 52));
    }
}