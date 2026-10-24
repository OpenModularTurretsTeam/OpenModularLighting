package omtteam.openmodularlighting.client;

import net.minecraftforge.common.MinecraftForge;
import omtteam.openmodularlighting.CommonProxy;
import omtteam.openmodularlighting.client.screen.CarbonFloodlightScreen;
import omtteam.openmodularlighting.client.screen.ElectricFloodlightScreen;
import omtteam.openmodularlighting.menu.CarbonFloodlightMenu;
import omtteam.openmodularlighting.menu.ElectricFloodlightMenu;
import omtteam.openmodularlighting.menu.FloodlightMenu;

import javax.annotation.Nullable;

/**
 * Client start-up (the client counterpart of {@code OML}): wires the client events ({@link ClientEvents}) and creates
 * the screens of the menus.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    @Nullable
    @Override
    public Object createScreen(FloodlightMenu menu) {
        if (menu instanceof ElectricFloodlightMenu) return new ElectricFloodlightScreen((ElectricFloodlightMenu) menu);
        if (menu instanceof CarbonFloodlightMenu) return new CarbonFloodlightScreen((CarbonFloodlightMenu) menu);
        return null;
    }
}
