package omtteam.openmodularlighting.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.menu.CarbonFloodlightMenu;

public class CarbonFloodlightScreen extends FloodlightScreen<CarbonFloodlightMenu> {
    public CarbonFloodlightScreen(CarbonFloodlightMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, OML.id("textures/gui/carbon_floodlight.png"));
    }

    /** 1.7.10 format: "Time remaining: 4m59s". */
    @Override
    protected Component statusText() {
        long ticks = menu.getTicksRemaining();
        return Component.translatable("gui.openmodularlighting.time_remaining").append(": " + ticks / 1200)
                .append(Component.translatable("gui.openmodularlighting.minutes_short"))
                .append(String.valueOf(ticks / 20 % 60))
                .append(Component.translatable("gui.openmodularlighting.seconds_short"));
    }
}
