package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.BeamFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.MenuCompat;
import omtteam.openmodularlighting.menu.CarbonFloodlightMenu;
import omtteam.openmodularlighting.registry.ModBlockEntities;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;
//?}

/**
 * Carbon (non-electric) floodlight: the same modes as the electric one, powered by furnace fuel.
 */
public class CarbonFloodlightBlock extends FloodlightBlock {
    //? if >=1.21 {
    private static final MapCodec<CarbonFloodlightBlock> CODEC = simpleCodec(CarbonFloodlightBlock::new);
    //?}

    public CarbonFloodlightBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarbonFloodlightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.CARBON_FLOODLIGHT;
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

    @Override
    protected void openMenu(ServerPlayer player, FloodlightBlockEntity light) {
        if (light instanceof CarbonFloodlightBlockEntity carbon) {
            MenuCompat.open(player, Component.translatable(getDescriptionId()), light.getBlockPos(),
                    (containerId, inventory, menuPlayer) -> CarbonFloodlightMenu.server(containerId, inventory, carbon));
        }
    }
}
