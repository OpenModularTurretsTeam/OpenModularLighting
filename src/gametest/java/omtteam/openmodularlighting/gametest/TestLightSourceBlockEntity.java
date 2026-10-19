package omtteam.openmodularlighting.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.LightSourceBlockEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * A light source whose state the test sets directly, recording re-cast requests.
 */
public class TestLightSourceBlockEntity extends BlockEntity implements LightSourceBlockEntity {
    boolean on;
    final Set<BlockPos> covered = new HashSet<>();
    int recastRequests;

    public TestLightSourceBlockEntity(BlockPos pos, BlockState state) {
        super(TestBlockEntities.TEST_LIGHT_SOURCE, pos, state);
    }

    @Override
    public boolean isLightOn() {
        return on;
    }

    @Override
    public boolean coversLightAt(BlockPos pos) {
        return covered.contains(pos);
    }

    @Override
    public void requestRecast() {
        recastRequests++;
    }
}
