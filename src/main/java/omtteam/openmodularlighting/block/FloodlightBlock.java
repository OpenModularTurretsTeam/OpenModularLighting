package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.CommonTags;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.compat.MenuCompat;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.menu.ElectricFloodlightMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base of every light block (floodlights, UV floodlight, fluorescent and grow lights). {@link #FACING} is the
 * direction the light shines (the front), {@link #LIT} is set while it is on. Interaction as in 1.7.10 (CLAUDE.md
 * §6.6); subclasses decide what a stick or wrench does ({@link #configure}).
 */
public abstract class FloodlightBlock extends BaseEntityBlock implements InteractiveBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected FloodlightBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(LIT, false));
    }

    /** 1.7.10: rock, metal sound, hardness 2.5, stone pickaxe (tags in datagen). */
    public static Properties createProperties() {
        return Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(2.5F).requiresCorrectToolForDrops();
    }

    protected abstract BlockEntityType<? extends FloodlightBlockEntity> blockEntityType();

    /** Opens the GUI of this light: the shared electric GUI (energy, battery slot) for electric lights. */
    protected void openMenu(ServerPlayer player, FloodlightBlockEntity light) {
        ElectricPower power = light.electricPower();
        if (power != null) {
            MenuCompat.open(player, Component.translatable(getDescriptionId()), light.getBlockPos(),
                    (containerId, inventory, menuPlayer) -> ElectricFloodlightMenu.server(containerId, inventory, light, power));
        }
    }

    /**
     * Right-click with a stick or wrench (not sneaking), on the server: cycle the mode or rotate the model. Only called
     * if {@link #hasConfiguration()}.
     */
    protected void configure(Level level, BlockPos pos, BlockState state, Player player, FloodlightBlockEntity light) {
    }

    /** Sends "Mode: …" to the player (1.7.10 chat message). */
    protected static void sendMode(Player player, String modeKey) {
        player.displayClientMessage(Component.translatable("gui.openmodularlighting.mode").append(": ")
                .append(Component.translatable(modeKey)), false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    /** Faces the player, like a dispenser (1.7.10 {@code BlockFL.getFacing}). */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != blockEntityType()) return null;
        return (tickLevel, pos, tickState, blockEntity) ->
                FloodlightBlockEntity.serverTick(tickLevel, pos, tickState, (FloodlightBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof FloodlightBlockEntity light)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getMainHandItem();
        boolean wrench = held.is(CommonTags.WRENCHES);
        boolean server = !level.isClientSide();
        InteractionResult handled = InteractionResult.sidedSuccess(level.isClientSide());
        if (player.isShiftKeyDown()) {
            if (held.isEmpty()) {
                if (server) {
                    light.toggleInverted();
                    player.displayClientMessage(Component.translatable("gui.openmodularlighting.invert").append(": ")
                            .append(Component.translatable(light.isInverted()
                                    ? "gui.openmodularlighting.true" : "gui.openmodularlighting.false")), false);
                }
                return handled;
            }
            if (wrench) {
                if (server) level.destroyBlock(pos, true, player); // dismantle
                return handled;
            }
            return InteractionResult.PASS; // let the held item act (e.g. place a block)
        }
        if ((wrench || held.is(Items.STICK)) && hasConfiguration()) {
            if (server) configure(level, pos, state, player, light);
            return handled;
        }
        if (held.getItem() instanceof DyeItem dye) {
            if (server) light.setColor(dye.getDyeColor().getId());
            return handled;
        }
        if (held.is(ItemTags.WOOL)) {
            if (server) light.setColor(FloodlightBlockEntity.NO_COLOR);
            return handled;
        }
        if (server && player instanceof ServerPlayer serverPlayer) {
            openMenu(serverPlayer, light);
        }
        return handled;
    }

    /** Whether {@link #configure} does something (otherwise a stick or wrench opens the GUI). */
    protected boolean hasConfiguration() {
        return false;
    }

    /** Removed: leave the phantom lights and drop the slot contents. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof FloodlightBlockEntity light) {
            light.removeLights(level);
            Containers.dropContents(level, pos, light.getInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** The dropped block keeps mode, invert, colour and energy / burn time (1.7.10 {@code BlockFL.getDrops}). */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof FloodlightBlockEntity light && light.hasCustomSettings()) {
            for (ItemStack drop : drops) {
                if (drop.is(asItem())) {
                    VersionCompat.setBlockEntityData(drop, blockEntity.getType(), light.saveItemData());
                }
            }
        }
        return drops;
    }
}
