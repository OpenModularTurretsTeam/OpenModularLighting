package omtteam.openmodularlighting.platform.neoforge;

import omtteam.omlib.platform.neoforge.NeoForgeNetwork;
import omtteam.omlib.platform.neoforge.NeoForgeCapabilities;
import omtteam.omlib.platform.neoforge.NeoForgeCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.registry.RegistrationStep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NeoForge entrypoint: installs the platform services, starts the mod, runs each registration step in the
 * {@link RegisterEvent} of its registry and wires NeoForge's events to the shared code.
 */
@Mod(OML.MOD_ID)
public final class OMLNeoForge {
    private final List<RegistrationStep> steps = OML.registrationSteps();
    private final Set<RegistrationStep> done = new HashSet<>();

    public OMLNeoForge(IEventBus modBus) {
        OML.init();
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener((RegisterCapabilitiesEvent event) ->
                NeoForgeCapabilities.register(event, ModBlockEntities.transferTypes()));
        modBus.addListener((RegisterPayloadHandlersEvent event) ->
                ((NeoForgeNetwork) Platform.network()).applyRegistrations(event, OML.MOD_ID));
        NeoForge.EVENT_BUS.addListener(OMLNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(OMLNeoForge::onPlayerLoggedIn);
    }

    private void onRegister(RegisterEvent event) {
        for (RegistrationStep step : steps) {
            if (step.registry().equals(event.getRegistryKey())) {
                step.register().run();
                done.add(step);
            }
        }
        event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, BlockEnabledCondition.ID,
                () -> NeoForgeBlockEnabledCondition.CODEC);
    }

    /** A step whose registry fired no event would leave its content unregistered: fail loudly instead. */
    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (done.size() != steps.size()) {
            List<RegistrationStep> missing = steps.stream().filter(step -> !done.contains(step)).toList();
            throw new IllegalStateException("Registration steps never ran: " + missing);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = InteractiveBlock.handleUse(event.getEntity(), event.getLevel(), event.getHand(),
                event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OMLConfig.onPlayerJoin(player);
        }
    }
}
