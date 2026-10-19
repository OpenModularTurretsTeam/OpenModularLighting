package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import omtteam.openmodularlighting.gametest.OMLGameTestMod;
import omtteam.openmodularlighting.gametest.TestItems;
import omtteam.openmodularlighting.registry.RegistrationStep;

import java.util.List;

/**
 * NeoForge entrypoint of the test mod. The test classes are found through their {@code @GameTestHolder} annotation.
 */
@Mod(OMLGameTestMod.MOD_ID)
public final class OMLNeoForgeGameTests {
    private final List<RegistrationStep> steps = OMLGameTestMod.registrationSteps();

    public OMLNeoForgeGameTests(IEventBus modBus) {
        OMLGameTestMod.install(new NeoForgeTestTransfer());
        modBus.addListener((RegisterEvent event) -> steps.stream()
                .filter(step -> step.registry().equals(event.getRegistryKey()))
                .forEach(step -> step.register().run()));
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new NeoForgeTestBattery(stack), TestItems.TEST_BATTERY));
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> OMLGameTestMod.onServerStarting());
    }
}
