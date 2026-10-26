package omtteam.openmodularlighting.platform.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.PlatformServices;
import omtteam.openmodularlighting.registry.RegistrationStep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Forge entrypoint: installs the platform services, starts the mod, runs each registration step in the
 * {@link RegisterEvent} of its registry and wires Forge's events to the shared code. Forge 1.20.1 has no client-only
 * {@code @Mod} classes, so it also starts the client entrypoint ({@code OMLForgeClient}) on a client.
 */
@Mod(OML.MOD_ID)
public final class OMLForge {
    /** Started by name: main cannot see the client source set. */
    private static final String CLIENT_ENTRYPOINT = "omtteam.openmodularlighting.client.platform.forge.OMLForgeClient";

    private final List<RegistrationStep> steps = OML.registrationSteps();
    private final Set<RegistrationStep> done = new HashSet<>();

    public OMLForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Platform.install(new PlatformServices(new ForgeLoaderInfo(), new ForgeFuel(), new ForgeNetwork(),
                new ForgeMenus(), new ForgeEnergy()));
        OML.init();
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ForgeBlockCapabilities::attach);
        MinecraftForge.EVENT_BUS.addListener(OMLForge::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(OMLForge::onPlayerLoggedIn);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            startClient(modBus);
        }
    }

    private static void startClient(IEventBus modBus) {
        try {
            Class.forName(CLIENT_ENTRYPOINT).getConstructor(IEventBus.class).newInstance(modBus);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not start the client entrypoint " + CLIENT_ENTRYPOINT, e);
        }
    }

    private void onRegister(RegisterEvent event) {
        for (RegistrationStep step : steps) {
            if (step.registry().equals(event.getRegistryKey())) {
                step.register().run();
                done.add(step);
            }
        }
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            CraftingHelper.register(ForgeBlockEnabledCondition.SERIALIZER);
        }
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
