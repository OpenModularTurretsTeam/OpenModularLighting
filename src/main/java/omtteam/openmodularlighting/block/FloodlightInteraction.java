package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.CommonTags;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModItems;

/** Right-click on a light ({@link FloodlightBlock#onUse}), as in 1.7.10 (CLAUDE.md §6.6). */
final class FloodlightInteraction {
    private FloodlightInteraction() {
    }

    /** Main hand only; the caller has checked that {@code light} is the block entity at {@code pos}. */
    static InteractionResult use(FloodlightBlock block, BlockState state, Level level, BlockPos pos, Player player,
                                 FloodlightBlockEntity light) {
        ItemStack held = player.getMainHandItem();
        if (held.is(ModItems.LIGHT_DEBUG_TOOL) && !player.isShiftKeyDown()) {
            return fillEnergy(level, light);
        }
        if (player.isShiftKeyDown()) {
            return useSneaking(level, pos, player, light, held);
        }
        return useItem(block, state, level, pos, player, light, held);
    }

    /** Light debug tool: fills the energy buffer of an electric light. */
    private static InteractionResult fillEnergy(Level level, FloodlightBlockEntity light) {
        ElectricPower power = light.electricPower();
        if (power == null) return InteractionResult.PASS;
        if (!level.isClientSide()) power.store().generate(power.store().getCapacity());
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Sneaking: empty hand toggles invert, a wrench dismantles, anything else lets the held item act. */
    private static InteractionResult useSneaking(Level level, BlockPos pos, Player player, FloodlightBlockEntity light,
                                                 ItemStack held) {
        if (held.isEmpty()) {
            if (!level.isClientSide()) toggleInverted(level, player, light);
        } else if (held.is(CommonTags.WRENCHES)) {
            if (!level.isClientSide()) level.destroyBlock(pos, true, player); // dismantle
        } else {
            return InteractionResult.PASS; // e.g. place a block
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** "Inverted: …", and when the light will turn on if it now waits for its timeout. */
    private static void toggleInverted(Level level, Player player, FloodlightBlockEntity light) {
        light.toggleInverted();
        player.displayClientMessage(Component.translatable("gui.openmodularlighting.invert").append(": ")
                .append(Component.translatable(light.isInverted()
                        ? "gui.openmodularlighting.true" : "gui.openmodularlighting.false")), false);
        int wait = light.ticksUntilOn(level);
        if (wait > 0) {
            player.displayClientMessage(Component.translatable("gui.openmodularlighting.turns_on_in",
                    FloodlightBlockEntity.seconds(wait)), false);
        }
    }

    /** Not sneaking: stick or wrench configures, dye sets the colour, wool resets it, anything else opens the GUI. */
    private static InteractionResult useItem(FloodlightBlock block, BlockState state, Level level, BlockPos pos,
                                             Player player, FloodlightBlockEntity light, ItemStack held) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if ((held.is(CommonTags.WRENCHES) || held.is(Items.STICK)) && block.hasConfiguration()) {
            block.configure(level, pos, state, player, light);
        } else if (held.getItem() instanceof DyeItem dye) {
            light.setColor(dye.getDyeColor().getId());
        } else if (held.is(ItemTags.WOOL)) {
            light.setColor(FloodlightBlockEntity.NO_COLOR);
        } else if (player instanceof ServerPlayer serverPlayer) {
            block.openMenu(serverPlayer, light);
        }
        return InteractionResult.CONSUME;
    }
}
