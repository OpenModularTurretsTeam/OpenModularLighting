package omtteam.openmodularlighting.gametest.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import omtteam.openmodularlighting.gametest.OMLGameTestMod;
import omtteam.openmodularlighting.gametest.TestItems;
import team.reborn.energy.api.EnergyStorage;

/**
 * Fabric entrypoint of the test mod.
 */
public final class OMLFabricGameTests implements ModInitializer {
    @Override
    public void onInitialize() {
        OMLGameTestMod.install(new FabricTestTransfer());
        OMLGameTestMod.registrationSteps().forEach(step -> step.register().run());
        EnergyStorage.ITEM.registerForItems((stack, context) -> new FabricTestBatteryStorage(context), TestItems.TEST_BATTERY);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> OMLGameTestMod.onServerStarting());
    }
}
