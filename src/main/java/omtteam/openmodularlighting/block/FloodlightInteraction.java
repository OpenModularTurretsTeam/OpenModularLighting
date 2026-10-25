package omtteam.openmodularlighting.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.DyeUtils;
import net.minecraftforge.oredict.OreDictionary;
import omtteam.openmodularlighting.compat.CommonTags;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModItems;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import java.util.Optional;

/**
 * Right-click on a light, as in 1.7.10 (CLAUDE.md §6.6). Handled in the block-use event instead of
 * {@code onBlockActivated}: the event also fires while sneaking with an item (a wrench of a mod that does not let
 * sneaking bypass), and {@link EnumActionResult#PASS} lets vanilla continue (e.g. placing a held block).
 */
public final class FloodlightInteraction {
    private static final String WOOL = "blockWool";

    private FloodlightInteraction() {
    }

    /** Runs on both sides, for both hands; only the main hand is handled. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND) return;
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        TileEntity tile = world.getTileEntity(pos);
        if (!(state.getBlock() instanceof FloodlightBlock) || !(tile instanceof FloodlightTileEntity)) return;
        EnumActionResult result = use((FloodlightBlock) state.getBlock(), state, world, pos, player,
                (FloodlightTileEntity) tile);
        if (result != EnumActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    /** Main hand only; the caller has checked that {@code light} is the block entity at {@code pos}. */
    static EnumActionResult use(FloodlightBlock block, IBlockState state, World world, BlockPos pos,
                                EntityPlayer player, FloodlightTileEntity light) {
        ItemStack held = player.getHeldItemMainhand();
        if (held.getItem() == ModItems.LIGHT_DEBUG_TOOL && !player.isSneaking()) {
            return fillEnergy(world, light);
        }
        if (player.isSneaking()) {
            return useSneaking(world, pos, player, light, held);
        }
        useItem(block, state, world, pos, player, light, held);
        return EnumActionResult.SUCCESS;
    }

    /** Light debug tool: fills the energy buffer of an electric light. */
    private static EnumActionResult fillEnergy(World world, FloodlightTileEntity light) {
        ElectricPower power = light.electricPower();
        if (power == null) return EnumActionResult.PASS;
        if (!world.isRemote) power.store().generate(power.store().getCapacity());
        return EnumActionResult.SUCCESS;
    }

    /** Sneaking: empty hand toggles invert, a wrench dismantles, anything else lets the held item act. */
    private static EnumActionResult useSneaking(World world, BlockPos pos, EntityPlayer player,
                                                FloodlightTileEntity light, ItemStack held) {
        if (held.isEmpty()) {
            if (!world.isRemote) toggleInverted(world, player, light);
        } else if (CommonTags.isWrench(held)) {
            if (!world.isRemote) world.destroyBlock(pos, true); // dismantle
        } else {
            return EnumActionResult.PASS; // e.g. place a block
        }
        return EnumActionResult.SUCCESS;
    }

    /** "Inverted: …", and when the light will turn on if it now waits for its timeout. */
    private static void toggleInverted(World world, EntityPlayer player, FloodlightTileEntity light) {
        light.toggleInverted();
        player.sendStatusMessage(new TextComponentTranslation("gui.openmodularlighting.invert")
                .appendSibling(new TextComponentString(": "))
                .appendSibling(new TextComponentTranslation(light.isInverted()
                        ? "gui.openmodularlighting.true" : "gui.openmodularlighting.false")), false);
        int wait = light.ticksUntilOn(world);
        if (wait > 0) {
            player.sendStatusMessage(new TextComponentTranslation("gui.openmodularlighting.turns_on_in",
                    FloodlightTileEntity.seconds(wait)), false);
        }
    }

    /** Not sneaking: stick or wrench configures, dye sets the colour, wool resets it, anything else opens the GUI. */
    private static void useItem(FloodlightBlock block, IBlockState state, World world, BlockPos pos,
                                EntityPlayer player, FloodlightTileEntity light, ItemStack held) {
        if (world.isRemote) return;
        Optional<EnumDyeColor> dye = DyeUtils.colorFromStack(held);
        if ((CommonTags.isWrench(held) || held.getItem() == Items.STICK) && block.hasConfiguration()) {
            block.configure(world, pos, state, player, light);
        } else if (dye.isPresent()) {
            light.setColor(dye.get().getMetadata());
        } else if (isWool(held)) {
            light.setColor(FloodlightTileEntity.NO_COLOR);
        } else {
            block.openGui(player, light);
        }
    }

    private static boolean isWool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (WOOL.equals(OreDictionary.getOreName(id))) return true;
        }
        return false;
    }
}
