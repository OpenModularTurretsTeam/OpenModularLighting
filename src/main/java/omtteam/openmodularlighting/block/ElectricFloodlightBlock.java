package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.BeamFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.omlib.camo.CamoBlock;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.omlib.compat.BlockCodec;

/**
 * Electric floodlight: straight, narrow cone or wide cone, powered by Team Reborn Energy.
 */
public class ElectricFloodlightBlock extends FloodlightBlock implements CamoBlock {
    private static final BlockCodec<ElectricFloodlightBlock> CODEC = codecOf(ElectricFloodlightBlock::new);

    public ElectricFloodlightBlock(Properties properties) {
        super(CODEC, properties);
    }

    /** A disguised floodlight still shows its lamp ({@link #LAMP_TINT_INDEX}). */
    @Override
    public int overlayTintIndex() {
        return LAMP_TINT_INDEX;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFloodlightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.ELECTRIC_FLOODLIGHT;
    }

    @Override
    protected boolean hasConfiguration() {
        return true;
    }

    /** Cycles straight → narrow cone → wide cone. */
    @Override
    protected void configure(Level level, BlockPos pos, BlockState state, Player player, FloodlightBlockEntity light) {
        if (light instanceof BeamFloodlightBlockEntity beam) {
            beam.cycleMode();
            sendMode(player, beam.modeKey());
        }
    }
}
