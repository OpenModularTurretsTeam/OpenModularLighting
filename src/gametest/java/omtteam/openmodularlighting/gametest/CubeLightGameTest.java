package omtteam.openmodularlighting.gametest;

//? if neoforge && <1.21.11 {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}
//? if forge {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}
//? if (neoforge && <1.21.11) || forge {
/*import omtteam.openmodularlighting.OML;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if <1.21.11 {
import net.minecraft.gametest.framework.GameTest;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import omtteam.omlib.block.InteractiveBlock;
import omtteam.openmodularlighting.registry.ModItems;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.CubeLightBlockEntity;
import omtteam.openmodularlighting.blockentity.PanelLightBlockEntity;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModBlocks;

/** Cube light: 6 lit neighbours, energy from every side, shared with touching cube lights. */
//? if (neoforge && <1.21.11) || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class CubeLightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 3, 3);

    @GameTest(template = EMPTY_STRUCTURE)
    public void lightsAllSixNeighbours(GameTestHelper helper) {
        CubeLightBlockEntity light = place(helper, LIGHT);
        light.electricPower().store().setAmountUnchecked(ElectricPower.CAPACITY);
        helper.setBlock(LIGHT.above(), Blocks.REDSTONE_BLOCK);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            for (Direction side : Direction.values()) {
                BlockState state = helper.getLevel().getBlockState(pos.relative(side));
                if (side == Direction.UP) continue; // the redstone block
                helper.assertTrue(state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT), "lit at " + side);
            }
            helper.assertTrue(light.getShape().size() == 5, "5 light blocks (the sixth side is the redstone block): " + light.getShape().size());
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void turnsBackOnWithoutTimeout(GameTestHelper helper) {
        CubeLightBlockEntity light = place(helper, LIGHT);
        light.electricPower().store().setAmountUnchecked(ElectricPower.CAPACITY);
        BlockPos pos = helper.absolutePos(LIGHT);
        BlockPos below = pos.below();
        helper.setBlock(LIGHT.above(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(lit(helper, below), "on");
            helper.setBlock(LIGHT.above(), Blocks.AIR);
        });
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!lit(helper, below), "off");
            helper.setBlock(LIGHT.above(), Blocks.REDSTONE_BLOCK);
        });
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(lit(helper, below), "on again without waiting for a timeout");
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void acceptsEnergyFromEverySide(GameTestHelper helper) {
        place(helper, LIGHT);
        BlockPos pos = helper.absolutePos(LIGHT);
        for (Direction side : Direction.values()) {
            helper.assertTrue(OMLGameTestMod.transfer().hasEnergy(helper.getLevel(), pos, side), "accepts energy from " + side);
        }
        remove(helper, pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void cubeLightsOnAnyFaceShareAClusterButPanelsDoNot(GameTestHelper helper) {
        PanelLightBlockEntity a = place(helper, LIGHT);
        PanelLightBlockEntity b = place(helper, LIGHT.above());
        PanelLightBlockEntity c = place(helper, LIGHT.above().east());
        helper.setBlock(LIGHT.west(), ModBlocks.GROW_LIGHT.defaultBlockState());
        PanelLightBlockEntity panel = (PanelLightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT.west()));
        helper.succeedWhen(() -> {
            helper.assertTrue(a.cluster() != null && a.cluster() == b.cluster() && b.cluster() == c.cluster(),
                    "cube lights in a chain share a cluster");
            helper.assertTrue(a.cluster().size() == 3, "cluster size " + a.cluster().size());
            helper.assertTrue(panel.cluster() != null && panel.cluster().size() == 1, "the panel light is not part of it");
            for (BlockPos pos : new BlockPos[]{LIGHT, LIGHT.above(), LIGHT.above().east(), LIGHT.west()}) {
                remove(helper, helper.absolutePos(pos));
            }
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void debugToolChargesTheLight(GameTestHelper helper) {
        CubeLightBlockEntity light = place(helper, LIGHT);
        BlockPos pos = helper.absolutePos(LIGHT);
        Player player = OMLGameTests.mockPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.LIGHT_DEBUG_TOOL));
        BlockState state = helper.getLevel().getBlockState(pos);
        ((InteractiveBlock) state.getBlock()).onUse(state, helper.getLevel(), pos, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(light.electricPower().store().getAmount() == ElectricPower.CAPACITY, "charged");
        remove(helper, pos);
        helper.succeed();
    }

    private static CubeLightBlockEntity place(GameTestHelper helper, BlockPos relative) {
        helper.setBlock(relative, ModBlocks.CUBE_LIGHT.defaultBlockState());
        return (CubeLightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static boolean lit(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getLevel().getBlockState(pos);
        return state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT);
    }

    private static void remove(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
