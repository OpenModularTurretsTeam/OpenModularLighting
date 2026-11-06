package omtteam.openmodularlighting.gametest.platform.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.omlib.core.energy.EnergyStore;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.gametest.OMLGameTests;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * NEEPMeat (Fabric 1.20.1 only): its vascular network charges an electric floodlight at 1 eJ = 1 E (OMLib's
 * {@code FabricNeepMeatPower}; the sink's rules are tested in OpenModularTurrets). Only this node has NEEPMeat on the
 * game test server; the other Fabric nodes leave the class out of the test mod's entrypoints.
 */
@SuppressWarnings("unused")
public class NeepMeatGameTest implements OMLGameTests {
    private static final BlockPos EMITTER = new BlockPos(1, 1, 3);
    private static final BlockPos CONDUIT = EMITTER.east();
    private static final BlockPos LIGHT = CONDUIT.east();
    /** Power of the emitter, eJ per tick. */
    private static final int EMITTER_POWER = 1000;

    /**
     * Power emitter, vascular conduit, electric floodlight (off, so it uses nothing): the conduit finds the light as a
     * sink when placed, and the light is charged with the emitter's whole output, in whole ticks of it.
     */
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void vascularNetworkChargesAFloodlight(GameTestHelper helper) {
        helper.setBlock(EMITTER, block("power_emitter"));
        // its saved power (the block entity's class extends one of a library nested in NEEPMeat's jar, not on the
        // compile classpath)
        CompoundTag power = new CompoundTag();
        power.putInt("power", EMITTER_POWER);
        helper.getBlockEntity(EMITTER).load(power);
        helper.setBlock(LIGHT, ModBlocks.ELECTRIC_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        EnergyStore energy = ((ElectricFloodlightBlockEntity) helper.getBlockEntity(LIGHT)).getEnergyStore();
        placeAsPlayer(helper, CONDUIT, block("vascular_conduit"));
        helper.succeedWhen(() -> {
            long amount = energy.getAmount();
            helper.assertTrue(amount >= EMITTER_POWER * 20L, "The floodlight holds only " + amount + " E");
            helper.assertTrue(amount % EMITTER_POWER == 0,
                    "The floodlight holds " + amount + " E, not whole ticks of " + EMITTER_POWER + " eJ");
            helper.setBlock(CONDUIT, Blocks.AIR);
            helper.setBlock(EMITTER, Blocks.AIR);
            helper.setBlock(LIGHT, Blocks.AIR);
        });
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation("neepmeat", id));
    }

    /** Places a block like a player does: its state from its neighbours, then its placement callback. */
    private static void placeAsPlayer(GameTestHelper helper, BlockPos pos, Block block) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = Block.updateFromNeighbourShapes(block.defaultBlockState(), helper.getLevel(), absolute);
        helper.getLevel().setBlockAndUpdate(absolute, state);
        block.setPlacedBy(helper.getLevel(), absolute, state, null, ItemStack.EMPTY);
    }
}
