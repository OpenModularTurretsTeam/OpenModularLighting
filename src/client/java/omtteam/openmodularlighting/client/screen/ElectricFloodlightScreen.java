package omtteam.openmodularlighting.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.menu.ElectricFloodlightMenu;

public class ElectricFloodlightScreen extends FloodlightScreen<ElectricFloodlightMenu> {
    public ElectricFloodlightScreen(ElectricFloodlightMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, OML.id("textures/gui/electric_floodlight.png"));
    }

    /** 1.7.10 format: "Energy: 12.3k/50k". */
    @Override
    protected Component statusText() {
        long energy = menu.getEnergy();
        return Component.translatable("gui.openmodularlighting.energy_storage")
                .append(": " + energy / 1000 + "." + energy % 1000 / 100 + "k/" + menu.getCapacity() / 1000 + "k");
    }
}
