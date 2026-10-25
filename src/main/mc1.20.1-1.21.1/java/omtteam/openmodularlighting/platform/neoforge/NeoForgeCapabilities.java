package omtteam.openmodularlighting.platform.neoforge;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Item and energy capabilities of the lights ({@code IItemHandler} and {@code IEnergyStorage}, NeoForge before its
 * transfer API).
 */
public final class NeoForgeCapabilities {
    private NeoForgeCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<? extends FloodlightBlockEntity> type : ModBlockEntities.lights()) {
            registerType(event, type);
        }
    }

    private static <T extends FloodlightBlockEntity> void registerType(RegisterCapabilitiesEvent event,
                                                                        BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                (light, side) -> new NeoForgeItemHandler(light.getItemAccess(side)));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (light, side) -> {
            EnergyAccess access = light.getEnergyAccess(side);
            return access == null ? null : new NeoForgeEnergyHandler(access);
        });
    }
}
