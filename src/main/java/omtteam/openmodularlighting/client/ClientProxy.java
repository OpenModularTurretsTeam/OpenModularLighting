package omtteam.openmodularlighting.client;

import net.minecraftforge.common.MinecraftForge;
import omtteam.openmodularlighting.CommonProxy;
import omtteam.openmodularlighting.client.gui.CarbonFloodlightGui;
import omtteam.openmodularlighting.client.gui.ElectricFloodlightGui;
import omtteam.openmodularlighting.container.CarbonFloodlightContainer;
import omtteam.openmodularlighting.container.ElectricFloodlightContainer;
import omtteam.openmodularlighting.container.FloodlightContainer;

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
    public Object createGui(FloodlightContainer menu) {
        if (menu instanceof ElectricFloodlightContainer) return new ElectricFloodlightGui((ElectricFloodlightContainer) menu);
        if (menu instanceof CarbonFloodlightContainer) return new CarbonFloodlightGui((CarbonFloodlightContainer) menu);
        return null;
    }
}
