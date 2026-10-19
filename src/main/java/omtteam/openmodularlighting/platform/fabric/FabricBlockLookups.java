package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import team.reborn.energy.api.EnergyStorage;

/**
 * Fabric API lookups of the lights: items on all sides (1.7.10); energy on all sides or only from the back
 * ({@code FloodlightBlockEntity#acceptsEnergyFrom}).
 */
public final class FabricBlockLookups {
    private FabricBlockLookups() {
    }

    public static void register() {
        for (BlockEntityType<? extends FloodlightBlockEntity> type : ModBlockEntities.lights()) {
            ItemStorage.SIDED.registerForBlockEntity((light, side) -> FabricItemStorages.of(light.getItemAccess(side)), type);
            EnergyStorage.SIDED.registerForBlockEntity((light, side) -> {
                EnergyAccess access = light.getEnergyAccess(side);
                return access == null ? null : FabricEnergyStorages.of(access);
            }, type);
        }
    }
}
