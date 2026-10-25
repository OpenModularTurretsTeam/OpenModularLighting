package omtteam.openmodularlighting.client.gui;

import net.minecraft.client.resources.I18n;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.container.ElectricFloodlightContainer;

public class ElectricFloodlightGui extends FloodlightGui<ElectricFloodlightContainer> {
    public ElectricFloodlightGui(ElectricFloodlightContainer menu) {
        super(menu, OML.id("textures/gui/electric_floodlight.png"));
    }

    /** 1.7.10 format: "Energy: 12.3k/50k". */
    @Override
    protected String statusText() {
        long energy = menu.getEnergy();
        return I18n.format("gui.openmodularlighting.energy_storage")
                + ": " + energy / 1000 + "." + energy % 1000 / 100 + "k/" + menu.getCapacity() / 1000 + "k";
    }
}
