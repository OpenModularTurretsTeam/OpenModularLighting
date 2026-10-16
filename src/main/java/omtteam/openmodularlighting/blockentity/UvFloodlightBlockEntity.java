package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.light.LightType;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * UV floodlight (1.7.10 {@code TileEntityUVLight}): a straight beam of UV light blocks that damage living entities;
 * {@code energyUsageUVFloodlight} per tick. Unlike the other lights it has no timeout after turning off (1.7.10).
 */
public class UvFloodlightBlockEntity extends FloodlightBlockEntity {
    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageUVFloodlight, this::setChanged);

    public UvFloodlightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UV_FLOODLIGHT, pos, state);
    }

    @Override
    protected LightPower power() {
        return power;
    }

    @Override
    protected BeamShape currentShape() {
        return BeamShape.uv(OMLConfig.get().general.rangeUVFloodlight);
    }

    @Override
    protected int[] shapeKey() {
        return new int[]{OMLConfig.get().general.rangeUVFloodlight};
    }

    @Override
    protected LightType lightType() {
        return LightType.UV;
    }

    @Override
    protected LightBlock lightBlock() {
        return ModBlocks.UV_LIGHT;
    }

    @Override
    protected int timeoutTicks() {
        return 0;
    }
}
