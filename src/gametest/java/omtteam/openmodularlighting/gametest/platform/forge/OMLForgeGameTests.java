package omtteam.openmodularlighting.gametest.platform.forge;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.gametest.OMLGameTestMod;
import omtteam.openmodularlighting.gametest.TestBatteryItem;
import omtteam.omlib.registry.RegistrationStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Forge entrypoint of the test mod. The test classes are found through their {@code @GameTestHolder} annotation.
 */
@Mod(OMLGameTestMod.MOD_ID)
public final class OMLForgeGameTests {
    private final List<RegistrationStep> steps = OMLGameTestMod.registrationSteps();

    public OMLForgeGameTests() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        OMLGameTestMod.install(new ForgeTestTransfer());
        modBus.addListener((RegisterEvent event) -> steps.stream()
                .filter(step -> step.registry().equals(event.getRegistryKey()))
                .forEach(step -> step.register().run()));
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, OMLForgeGameTests::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> OMLGameTestMod.onServerStarting());
    }

    /** FE of the test battery (attached while the stack is constructed, so the battery reads the stack only later). */
    private static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!(stack.getItem() instanceof TestBatteryItem)) return;
        LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new ForgeTestBattery(stack));
        event.addCapability(OML.id("test_battery"), new ICapabilityProvider() {
            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                return ForgeCapabilities.ENERGY.orEmpty(capability, energy);
            }
        });
        event.addListener(energy::invalidate);
    }
}
