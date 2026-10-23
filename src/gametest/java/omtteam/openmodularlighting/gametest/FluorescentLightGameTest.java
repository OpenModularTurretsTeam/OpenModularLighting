package omtteam.openmodularlighting.gametest;

//? if neoforge {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}
//? if forge {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}
//? if neoforge || forge {
/*import omtteam.openmodularlighting.OML;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.PanelLightBlockEntity;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * Small and square fluorescent lights (CLAUDE.md §6.2, §6.3): 5 lit neighbours, energy only from the back, the panel
 * on the back face and the small light's rotation.
 */
//? if neoforge || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class FluorescentLightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 1, 3);

    @GameTest(template = EMPTY_STRUCTURE)
    public void lightsTheBlockInFrontAndTheFourBesideIt(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP);
        light.electricPower().store().setAmountUnchecked(ElectricPower.CAPACITY);
        helper.setBlock(LIGHT.below(), Blocks.REDSTONE_BLOCK); // behind the light
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            for (BlockPos lit : new BlockPos[]{pos.above(), pos.north(), pos.south(), pos.east(), pos.west()}) {
                BlockState state = helper.getLevel().getBlockState(lit);
                helper.assertTrue(state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT), "lit at " + lit);
            }
            helper.assertTrue(light.getShape().size() == 5, "exactly 5 light blocks");
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void turnsBackOnWithoutTimeout(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.SQUARE_FLUORESCENT_LIGHT, Direction.UP);
        light.electricPower().store().setAmountUnchecked(ElectricPower.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.setBlock(LIGHT.below(), Blocks.REDSTONE_BLOCK); // behind the light
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(light.isLightOn(), "on");
            helper.setBlock(LIGHT.below(), Blocks.AIR);
        });
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!light.isLightOn(), "off");
            helper.setBlock(LIGHT.below(), Blocks.REDSTONE_BLOCK);
        });
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(light.isLightOn(), "on again without waiting for a timeout");
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void acceptsEnergyOnlyFromTheBack(GameTestHelper helper) {
        place(helper, ModBlocks.SQUARE_FLUORESCENT_LIGHT, Direction.UP);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(LIGHT);
        TestTransfer transfer = OMLGameTestMod.transfer();
        helper.assertTrue(transfer.hasEnergy(level, pos, Direction.DOWN), "back face (below) accepts energy");
        for (Direction side : new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST}) {
            helper.assertTrue(!transfer.hasEnergy(level, pos, side), "no energy from " + side);
        }
        remove(helper, pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void panelSitsOnTheBackFaceAndTheSmallOneTurns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(LIGHT);
        place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.NORTH);
        // facing north: mounted on the south face, a horizontal strip
        AABB strip = level.getBlockState(pos).getShape(level, pos).bounds();
        helper.assertTrue(strip.minZ >= 13 / 16.0 && strip.maxZ == 1, "on the back (south) face: " + strip);
        helper.assertTrue(strip.maxX - strip.minX == 1 && strip.maxY - strip.minY == 6 / 16.0, "horizontal: " + strip);

        BlockState state = level.getBlockState(pos);
        level.setBlock(pos, state.cycle(FluorescentLightBlock.ROTATED), Block.UPDATE_CLIENTS);
        AABB turned = level.getBlockState(pos).getShape(level, pos).bounds();
        helper.assertTrue(turned.maxY - turned.minY == 1 && turned.maxX - turned.minX == 6 / 16.0, "vertical: " + turned);
        helper.assertTrue(level.getBlockState(pos).getCollisionShape(level, pos).isEmpty(), "no collision");

        remove(helper, pos);
        place(helper, ModBlocks.SQUARE_FLUORESCENT_LIGHT, Direction.UP);
        AABB square = level.getBlockState(pos).getShape(level, pos).bounds();
        helper.assertTrue(square.minY == 0 && square.maxY == 3 / 16.0 && square.maxX - square.minX == 1,
                "square panel on the floor: " + square);
        remove(helper, pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lightsNextToEachOtherShareACluster(GameTestHelper helper) {
        PanelLightBlockEntity a = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.west());
        PanelLightBlockEntity b = place(helper, ModBlocks.SQUARE_FLUORESCENT_LIGHT, Direction.UP, LIGHT);
        PanelLightBlockEntity c = place(helper, ModBlocks.GROW_LIGHT, Direction.UP, LIGHT.east());
        PanelLightBlockEntity apart = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.east(2).south());
        PanelLightBlockEntity other = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.NORTH, LIGHT.north());
        helper.succeedWhen(() -> {
            helper.assertTrue(a.cluster() != null && a.cluster() == b.cluster() && b.cluster() == c.cluster(),
                    "three lights in a row share a cluster");
            helper.assertTrue(a.cluster().size() == 3, "cluster size " + a.cluster().size());
            helper.assertTrue(apart.cluster() != null && apart.cluster().size() == 1, "diagonal light is alone");
            helper.assertTrue(other.cluster() != null && other.cluster().size() == 1, "light facing another way is alone");
            for (BlockPos pos : new BlockPos[]{LIGHT.west(), LIGHT, LIGHT.east(), LIGHT.east(2).south(), LIGHT.north()}) {
                remove(helper, helper.absolutePos(pos));
            }
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void energyInsertedIntoOneLightIsSeenByTheCluster(GameTestHelper helper) {
        PanelLightBlockEntity a = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT);
        PanelLightBlockEntity b = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.east());
        ServerLevel level = helper.getLevel();
        helper.runAfterDelay(3, () -> {
            long inserted = OMLGameTestMod.transfer().insertEnergy(level, helper.absolutePos(LIGHT), Direction.DOWN, 1000);
            helper.assertTrue(inserted == 1000, "inserted through the back face: " + inserted);
            long total = a.buffer().store().getAmount() + b.buffer().store().getAmount();
            helper.assertTrue(total == 1000, "all energy is in the cluster: " + total);
            EnergyAccess cluster = a.getEnergyAccess(Direction.DOWN);
            helper.assertTrue(cluster.getAmount() == 1000, "cluster amount " + cluster.getAmount());
            helper.assertTrue(cluster.getCapacity() == 2 * ElectricPower.CAPACITY, "cluster capacity " + cluster.getCapacity());
            helper.assertTrue(cluster.extract(100, false) == 0, "nothing can be extracted");
            remove(helper, helper.absolutePos(LIGHT));
            remove(helper, helper.absolutePos(LIGHT.east()));
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void aLightRunsOnTheEnergyOfItsNeighbour(GameTestHelper helper) {
        PanelLightBlockEntity charged = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.east());
        PanelLightBlockEntity empty = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT);
        charged.buffer().store().setAmountUnchecked(ElectricPower.CAPACITY);
        helper.setBlock(LIGHT.below(), Blocks.REDSTONE_BLOCK); // powers only the empty light
        helper.succeedWhen(() -> {
            helper.assertTrue(empty.isLightOn(), "the empty light is on");
            helper.assertTrue(charged.buffer().store().getAmount() < ElectricPower.CAPACITY, "neighbour paid");
            helper.assertFalse(charged.isLightOn(), "the charged light has no redstone and stays off");
            remove(helper, helper.absolutePos(LIGHT));
            remove(helper, helper.absolutePos(LIGHT.east()));
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void breakingTheMiddleLightSplitsTheCluster(GameTestHelper helper) {
        PanelLightBlockEntity a = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.west());
        place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT);
        PanelLightBlockEntity c = place(helper, ModBlocks.SMALL_FLUORESCENT_LIGHT, Direction.UP, LIGHT.east());
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(a.cluster() != null && a.cluster() == c.cluster(), "joined first");
            remove(helper, helper.absolutePos(LIGHT));
            helper.succeedWhen(() -> {
                helper.assertTrue(a.cluster() != c.cluster(), "split");
                helper.assertTrue(a.cluster().size() == 1 && c.cluster().size() == 1, "both alone");
                remove(helper, helper.absolutePos(LIGHT.west()));
                remove(helper, helper.absolutePos(LIGHT.east()));
            });
        });
    }

    private static FloodlightBlockEntity place(GameTestHelper helper, Block block, Direction facing) {
        return place(helper, block, facing, LIGHT);
    }

    private static PanelLightBlockEntity place(GameTestHelper helper, Block block, Direction facing, BlockPos relative) {
        helper.setBlock(relative, block.defaultBlockState().setValue(FloodlightBlock.FACING, facing));
        return (PanelLightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static void remove(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
