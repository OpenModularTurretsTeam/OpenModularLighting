package omtteam.openmodularlighting.client.gui;

import net.minecraft.client.resources.I18n;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.container.CarbonFloodlightContainer;

public class CarbonFloodlightGui extends FloodlightGui<CarbonFloodlightContainer> {
    public CarbonFloodlightGui(CarbonFloodlightContainer menu) {
        super(menu, OML.id("textures/gui/carbon_floodlight.png"));
    }

    /** 1.7.10 format: "Time remaining: 4m59s". */
    @Override
    protected String statusText() {
        long ticks = menu.getTicksRemaining();
        return I18n.format("gui.openmodularlighting.time_remaining") + ": " + ticks / 1200
                + I18n.format("gui.openmodularlighting.minutes_short") + ticks / 20 % 60
                + I18n.format("gui.openmodularlighting.seconds_short");
    }
}
