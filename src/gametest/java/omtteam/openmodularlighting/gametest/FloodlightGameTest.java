package omtteam.openmodularlighting.gametest;

//? if neoforge {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import omtteam.openmodularlighting.OML;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.energy.EnergyStore;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.registry.ModBlocks;

import java.util.List;

/**
 * In-world tests of the electric and carbon floodlights (CLAUDE.md §6.3 – §6.6). The beam ranges are shortened to
 * {@link OMLGameTestMod#TEST_RANGE}, so every beam stays inside its test structure; lights shine up and remove
 * themselves at the end.
 */
//? if neoforge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class FloodlightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 1, 3);
    private static final BlockPos REDSTONE = new BlockPos(3, 1, 4);
    private static final int RANGE = OMLGameTestMod.TEST_RANGE;

    // ---- Beam and on/off ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void placedFloodlightCastsADarkBeam(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            assertBeam(helper, pos, false);
            helper.assertTrue(!helper.getLevel().getBlockState(pos.above(RANGE + 1)).is(ModBlocks.PHANTOM_LIGHT),
                    "beam ends at the range");
            helper.assertTrue(!lit(helper, LIGHT), "floodlight itself is off");
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void redstoneTurnsTheBeamOnAndOff(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> {
            assertBeam(helper, pos, true);
            helper.assertTrue(lit(helper, LIGHT), "floodlight itself is lit");
            helper.assertTrue(light.getEnergyStore().getAmount() < ElectricFloodlightBlockEntity.CAPACITY, "uses energy");
            helper.setBlock(REDSTONE, Blocks.AIR);
        });
        helper.runAfterDelay(5, () -> {
            assertBeam(helper, pos, false);
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void waitsForTheTimeoutBeforeTurningBackOn(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> helper.setBlock(REDSTONE, Blocks.AIR));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!light.isLightOn(), "off");
            helper.assertTrue(light.ticksUntilOn(helper.getLevel()) == 0, "no wait while it should stay off");
            light.toggleInverted(); // what sneak-clicking does: it wants to turn on again during the timeout
            int wait = light.ticksUntilOn(helper.getLevel());
            helper.assertTrue(wait > 0, "waits for the timeout: " + wait);
            helper.assertTrue(FloodlightBlockEntity.seconds(40).equals("2")
                    && FloodlightBlockEntity.seconds(30).equals("1.5"), "seconds text");
        });
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(!light.isLightOn(), "still off during the timeout");
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void staysOffWithoutEnergy(GameTestHelper helper) {
        placeElectric(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(5, () -> {
            assertBeam(helper, pos, false);
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void invertedFloodlightShinesWithoutRedstone(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        light.toggleInverted();
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            assertBeam(helper, pos, true);
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void chargesFromABatteryInItsSlot(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        ItemStack battery = new ItemStack(TestItems.TEST_BATTERY);
        TestItems.TEST_BATTERY.setStoredEnergy(battery, 10_000);
        helper.assertTrue(light.isValidItem(battery), "battery accepted");
        helper.assertTrue(!light.isValidItem(new ItemStack(Items.COAL)), "coal is no battery");
        light.getInventory().setItem(0, battery);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            helper.assertTrue(light.getEnergyStore().getAmount() == 10_000,
                    "energy moved into the floodlight, has " + light.getEnergyStore().getAmount());
            ItemStack inSlot = light.getInventory().getItem(0);
            helper.assertTrue(TestItems.TEST_BATTERY.getStoredEnergy(inSlot) == 0, "battery empty");
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void pushesEnergyIntoABlockThroughTheLoaderApi(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        EnergyStore source = new EnergyStore(1000, 0, 100, () -> { });
        source.generate(1000);
        long moved = Platform.energy().pushToBlock(helper.getLevel(), pos, Direction.NORTH, source, 500);
        helper.assertTrue(moved == 100, "maxExtract of the source limits the push, moved " + moved);
        helper.assertTrue(source.getAmount() == 900 && light.getEnergyStore().getAmount() == 100, "energy arrived");
        long none = Platform.energy().pushToBlock(helper.getLevel(), pos.east(), Direction.WEST, source, 500);
        helper.assertTrue(none == 0 && source.getAmount() == 900, "no energy block there, nothing moved");
        remove(helper, pos);
        helper.succeed();
    }

    // ---- Re-casting ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void blockingTheBeamShortensItAndUnblockingRestoresIt(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        ServerLevel level = helper.getLevel();
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        // the stone sits on a skipped distance (4), next to the phantom lights at 3 and 5, which notice the change
        helper.runAfterDelay(3, () -> level.setBlock(pos.above(4), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(isPhantom(level, pos.above(3), true), "lit up to the stone");
            helper.assertTrue(level.getBlockState(pos.above(2)).isAir(), "second block skipped");
            helper.assertTrue(level.getBlockState(pos.above(5)).isAir(), "beam ends at the stone");
            helper.assertTrue(level.getBlockState(pos.above(RANGE)).isAir(), "rest of the beam removed");
            level.setBlock(pos.above(4), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        });
        helper.runAfterDelay(7, () -> {
            assertBeam(helper, pos, true);
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lightproofGlassStopsTheBeamLikeAWall(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        ServerLevel level = helper.getLevel();
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> level.setBlock(pos.above(4), ModBlocks.LIGHTPROOF_GLASS.defaultBlockState(), Block.UPDATE_ALL));
        helper.runAfterDelay(5, () -> {
            BlockPos glass = pos.above(4);
            helper.assertTrue(level.getBlockState(glass).getLightBlock(level, glass) >= level.getMaxLightLevel(),
                    "lets no light through");
            helper.assertTrue(!level.getBlockState(glass).isSolidRender(level, glass), "but is see-through");
            helper.assertTrue(isPhantom(level, pos.above(3), true), "lit up to the glass");
            helper.assertTrue(level.getBlockState(pos.above(5)).isAir(), "beam ends at the glass");
            level.setBlock(glass, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        });
        helper.runAfterDelay(7, () -> {
            assertBeam(helper, pos, true);
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void modeChangeRecastsAtOnce(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(ElectricFloodlightBlockEntity.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        ServerLevel level = helper.getLevel();
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> {
            light.setMode(BeamMode.NARROW_CONE);
            // beam space (6, 3, 0) facing up is (-3, 6, 0): the end of the first outer cone ray (spread i / 2)
            BlockPos coneEnd = pos.offset(-3, RANGE, 0);
            helper.assertTrue(isPhantom(level, coneEnd, true), "narrow cone lit at once");
            light.setMode(BeamMode.WIDE_CONE);
            // the wide cone reaches range / 4 = 1: the centre line has a light there, the rest of the cone skips it
            helper.assertTrue(isPhantom(level, pos.above(), true), "centre line is lit at distance 1");
            helper.assertTrue(level.getBlockState(pos.offset(-1, 1, 0)).isAir(), "the rest of the cone skips the odd distances");
            helper.assertTrue(level.getBlockState(coneEnd).isAir(), "narrow cone removed at once");
            helper.assertTrue(level.getBlockState(pos.above(RANGE)).isAir(), "nothing beyond the wide cone");
            light.setMode(BeamMode.STRAIGHT);
            helper.assertTrue(level.getBlockState(pos.offset(-1, 1, 0)).isAir(), "wide cone removed again");
            assertBeam(helper, pos, true);
            remove(helper, pos);
            helper.succeed();
        });
    }

    // ---- Removing ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void breakingRemovesTheBeamAndTheDropKeepsTheSettings(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        light.getEnergyStore().setAmountUnchecked(1234);
        BlockPos pos = helper.absolutePos(LIGHT);
        ServerLevel level = helper.getLevel();
        helper.runAfterDelay(2, () -> {
            assertBeam(helper, pos, false);
            List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, light);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(ModBlocks.ELECTRIC_FLOODLIGHT.asItem()),
                    "drops itself, got " + drops);
            helper.assertTrue(VersionCompat.hasBlockEntityData(drops.get(0)), "drop keeps the stored energy");
            level.destroyBlock(pos, false);
            for (int i = 1; i <= RANGE; i++) {
                helper.assertTrue(level.getBlockState(pos.above(i)).isAir(), "phantom light " + i + " removed");
            }
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void unconfiguredDropHasNoData(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = placeElectric(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        ServerLevel level = helper.getLevel();
        List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, light);
        helper.assertTrue(drops.size() == 1 && !VersionCompat.hasBlockEntityData(drops.get(0)),
                "plain drop stacks with new floodlights");
        remove(helper, pos);
        helper.succeed();
    }

    // ---- Carbon floodlight ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void carbonFloodlightBurnsFuel(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.CARBON_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        CarbonFloodlightBlockEntity light = (CarbonFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        helper.assertTrue(light.isValidItem(new ItemStack(Items.COAL)) && !light.isValidItem(new ItemStack(Items.STONE)),
                "only fuel fits");
        light.getInventory().setItem(0, new ItemStack(Items.COAL, 2));
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(4, () -> {
            assertBeam(helper, pos, true);
            helper.assertTrue(light.getInventory().getItem(0).getCount() == 1, "one coal burnt");
            int full = 300 * 20; // carbonTime seconds of straight light per coal
            helper.assertTrue(light.getTicksRemaining() < full && light.getTicksRemaining() > full - 10,
                    "burn time runs down, " + light.getTicksRemaining() + " left");
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void carbonFloodlightUsesFuelTwiceAsFastInConeMode(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.CARBON_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        CarbonFloodlightBlockEntity light = (CarbonFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        light.cycleMode(); // straight -> narrow cone
        light.getInventory().setItem(0, new ItemStack(Items.COAL));
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(4, () -> {
            // the coal still stores 300 s of straight light, but lasts only half of that in a cone
            int full = 300 * 10;
            helper.assertTrue(light.getTicksRemaining() < full && light.getTicksRemaining() > full - 10,
                    "cone run time shown: " + light.getTicksRemaining());
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void pipesMayOnlyInsertValidItems(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.CARBON_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        CarbonFloodlightBlockEntity light = (CarbonFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        BlockPos pos = helper.absolutePos(LIGHT);
        TestTransfer transfer = OMLGameTestMod.transfer();
        ServerLevel level = helper.getLevel();
        helper.assertTrue(transfer.insertItem(level, pos, Direction.NORTH, new ItemStack(Items.STONE)) == 0, "no fuel, not accepted");
        helper.assertTrue(transfer.insertItem(level, pos, Direction.NORTH, new ItemStack(Items.COAL, 5)) == 5, "fuel accepted");
        helper.assertTrue(transfer.extractItem(level, pos, Direction.NORTH, new ItemStack(Items.COAL), 5) == 0, "nothing comes out");
        helper.assertTrue(light.getInventory().getItem(0).is(Items.COAL), "coal in the slot");
        remove(helper, pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void carbonFloodlightLeavesFuelRemainder(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.CARBON_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        CarbonFloodlightBlockEntity light = (CarbonFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        light.getInventory().setItem(0, new ItemStack(Items.LAVA_BUCKET));
        BlockPos pos = helper.absolutePos(LIGHT);
        // refuelling also happens while the light is off (1.7.10), so no redstone is needed
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(light.getInventory().getItem(0).is(Items.BUCKET), "the lava bucket leaves a bucket, like in a furnace");
            helper.assertTrue(light.getTicksRemaining() > 0, "the lava was burnt");
            remove(helper, pos);
            helper.succeed();
        });
    }

    // ---- Helpers ----

    private static ElectricFloodlightBlockEntity placeElectric(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.ELECTRIC_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        return (ElectricFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
    }

    /**
     * The straight beam above {@code pos} consists of phantom lights with this {@code LIT} at the odd distances; the
     * even ones are skipped ({@code Spacing.STRAIGHT}).
     */
    private static void assertBeam(GameTestHelper helper, BlockPos pos, boolean lit) {
        for (int i = 1; i <= RANGE; i++) {
            if (i % 2 == 1) {
                helper.assertTrue(isPhantom(helper.getLevel(), pos.above(i), lit),
                        "phantom light " + i + " should be " + (lit ? "lit" : "unlit") + ", is "
                                + helper.getLevel().getBlockState(pos.above(i)));
            } else {
                helper.assertTrue(helper.getLevel().getBlockState(pos.above(i)).isAir(), "block " + i + " is skipped");
            }
        }
    }

    private static boolean isPhantom(ServerLevel level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT) == lit;
    }

    private static boolean lit(GameTestHelper helper, BlockPos relative) {
        return helper.getBlockState(relative).getValue(FloodlightBlock.LIT);
    }

    /** Removes the floodlight, which removes its beam. */
    private static void remove(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getBlockEntity(pos) instanceof FloodlightBlockEntity) {
            helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
