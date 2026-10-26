package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import omtteam.openmodularlighting.gametest.OMLGameTestMod;
import omtteam.omlib.registry.RegistrationStep;

import java.util.List;

/**
 * NeoForge entrypoint of the test mod. Before 1.21.11 NeoForge finds the test classes through their
 * {@code @GameTestHolder} annotation; from 1.21.11 on {@code NeoForgeGameTestRegistration} registers them.
 */
@Mod(OMLGameTestMod.MOD_ID)
public final class OMLNeoForgeGameTests {
    private final List<RegistrationStep> steps = OMLGameTestMod.registrationSteps();

    public OMLNeoForgeGameTests(IEventBus modBus) {
        OMLGameTestMod.install(new NeoForgeTestTransfer());
        modBus.addListener((RegisterEvent event) -> steps.stream()
                .filter(step -> step.registry().equals(event.getRegistryKey()))
                .forEach(step -> step.register().run()));
        modBus.addListener(NeoForgeTestBattery::register);
        //? if >=1.21.11 {
        /*NeoForgeGameTestRegistration.register(modBus);
        *///?}
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> OMLGameTestMod.onServerStarting());
    }
}
