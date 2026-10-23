package omtteam.openmodularlighting.platform.forge;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Items and energy of a light for other mods (Forge capabilities, attached in {@code AttachCapabilitiesEvent}; the
 * counterpart of the Fabric lookups and NeoForge's block capabilities). Forge attaches it while the block entity is
 * constructed, so the light is only asked on a query. It is asked on every query, because the sides it exposes can
 * change (a rotated panel light): a side keeps its {@link LazyOptional} while the light returns the same access object,
 * and the old one is invalidated when that changes or the block entity is removed ({@link #invalidate}).
 */
final class ForgeLightCapabilities implements ICapabilityProvider {
    private final FloodlightBlockEntity light;
    private final SideCache items = new SideCache();
    private final SideCache energy = new SideCache();

    ForgeLightCapabilities(FloodlightBlockEntity light) {
        this.light = light;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return items.get(side, light.getItemAccess(side), ForgeItemHandler::new).cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energy.get(side, light.getEnergyAccess(side), ForgeEnergyHandler::new).cast();
        }
        return LazyOptional.empty();
    }

    void invalidate() {
        items.invalidate();
        energy.invalidate();
    }

    /** The {@link LazyOptional} per side (index 6: no side) and the access object it wraps. */
    private static final class SideCache {
        private final Object[] accesses = new Object[7];
        private final LazyOptional<?>[] optionals = new LazyOptional<?>[7];

        <A> LazyOptional<?> get(@Nullable Direction side, @Nullable A access, Function<A, ?> adapter) {
            int index = side == null ? 6 : side.ordinal();
            if (optionals[index] == null || accesses[index] != access) {
                if (optionals[index] != null) optionals[index].invalidate();
                accesses[index] = access;
                optionals[index] = access == null ? LazyOptional.empty() : LazyOptional.of(() -> adapter.apply(access));
            }
            return optionals[index];
        }

        void invalidate() {
            for (int index = 0; index < optionals.length; index++) {
                if (optionals[index] != null) optionals[index].invalidate();
                optionals[index] = null;
                accesses[index] = null;
            }
        }
    }
}
