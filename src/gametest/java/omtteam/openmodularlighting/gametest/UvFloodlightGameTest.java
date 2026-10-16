package omtteam.openmodularlighting.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * UV floodlight and UV light block (CLAUDE.md §6.3, §6.9): a beam of UV light blocks that damage living entities only
 * while lit, take over phantom lights and give them back when removed.
 */
public class UvFloodlightGameTest implements FabricGameTest {
    /** On the ground, shining east along x = 1 .. range. */
    private static final BlockPos UV = new BlockPos(0, 0, 3);
    private static final int RANGE = OMLGameTestMod.TEST_RANGE;

    @GameTest(template = EMPTY_STRUCTURE)
    public void beamOfUvLightDamagesWhileLit(GameTestHelper helper) {
        placeUv(helper, UV, true);
        helper.setBlock(UV.above(), Blocks.REDSTONE_BLOCK);
        Pig pig = pig(helper, new BlockPos(3, 0, 3));
        float health = pig.getHealth();
        BlockPos pos = helper.absolutePos(UV);
        helper.runAfterDelay(25, () -> {
            for (int i = 1; i <= RANGE; i++) {
                BlockState state = helper.getLevel().getBlockState(pos.east(i));
                helper.assertTrue(state.is(ModBlocks.UV_LIGHT) && state.getValue(LightBlock.LIT), "UV light " + i);
            }
            helper.assertTrue(pig.getHealth() < health, "pig in the beam is damaged");
            pig.discard();
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void unlitUvLightIsHarmless(GameTestHelper helper) {
        placeUv(helper, UV, true); // energy, but no redstone
        Pig pig = pig(helper, new BlockPos(3, 0, 3));
        float health = pig.getHealth();
        BlockPos pos = helper.absolutePos(UV);
        helper.runAfterDelay(25, () -> {
            BlockState state = helper.getLevel().getBlockState(pos.east(3));
            helper.assertTrue(state.is(ModBlocks.UV_LIGHT) && !state.getValue(LightBlock.LIT), "unlit UV light placed");
            helper.assertTrue(pig.getHealth() == health, "pig unharmed");
            pig.discard();
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void uvBeamTakesOverAPhantomLightAndGivesItBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // electric floodlight shining up through the UV beam at height 3
        BlockPos floodlightRel = new BlockPos(3, 0, 3);
        helper.setBlock(floodlightRel, ModBlocks.ELECTRIC_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        FloodlightBlockEntity floodlight = (FloodlightBlockEntity) level.getBlockEntity(helper.absolutePos(floodlightRel));
        floodlight.electricPower().storage().amount = ElectricPower.CAPACITY;
        helper.setBlock(floodlightRel.south(), Blocks.REDSTONE_BLOCK);
        BlockPos uvRel = new BlockPos(0, 3, 3);
        placeUv(helper, uvRel, true);
        helper.setBlock(uvRel.above(), Blocks.REDSTONE_BLOCK);
        BlockPos crossing = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(level.getBlockState(crossing).is(ModBlocks.UV_LIGHT), "UV light at the crossing");
            helper.assertTrue(isLitPhantom(level, crossing.above()), "floodlight beam continues behind the UV light");
            remove(helper, helper.absolutePos(uvRel));
        });
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(isLitPhantom(level, crossing), "phantom light back at the crossing");
            remove(helper, helper.absolutePos(floodlightRel));
            helper.succeed();
        });
    }

    private static void placeUv(GameTestHelper helper, BlockPos relative, boolean energy) {
        helper.setBlock(relative, ModBlocks.UV_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.EAST));
        FloodlightBlockEntity light = (FloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        if (energy) light.electricPower().storage().amount = ElectricPower.CAPACITY;
    }

    private static Pig pig(GameTestHelper helper, BlockPos relative) {
        Pig pig = helper.spawn(EntityType.PIG, relative);
        pig.setNoAi(true);
        return pig;
    }

    private static boolean isLitPhantom(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT);
    }

    private static void remove(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
