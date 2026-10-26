package omtteam.openmodularlighting.registry;

import net.minecraft.world.inventory.MenuType;
import omtteam.openmodularlighting.OML;
import omtteam.omlib.compat.MenuCompat;
import omtteam.openmodularlighting.menu.CarbonFloodlightMenu;
import omtteam.openmodularlighting.menu.ElectricFloodlightMenu;

/**
 * All menu types of the mod.
 */
public final class ModMenus {
    public static final MenuType<ElectricFloodlightMenu> ELECTRIC_FLOODLIGHT =
            MenuCompat.register(OML.id("electric_floodlight"), ElectricFloodlightMenu::client);
    public static final MenuType<CarbonFloodlightMenu> CARBON_FLOODLIGHT =
            MenuCompat.register(OML.id("carbon_floodlight"), CarbonFloodlightMenu::client);


    private ModMenus() {
    }

    /** Loads the class, registering all menu types. */
    public static void init() {
    }
}
