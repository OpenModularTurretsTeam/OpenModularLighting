package omtteam.openmodularlighting.gametest;

//? if forge {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}
//? if forge {
/*import omtteam.openmodularlighting.OML;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;
import omtteam.openmodularlighting.core.beam.GrowMode;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * Grow light (CLAUDE.md §6.8): hangs facing down, lights the block below it and grows the crop two blocks below.
 */
//? if forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class GrowLightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 2, 3);
    private static final BlockPos CROP = new BlockPos(3, 0, 3);

    @GameTest(template = EMPTY_STRUCTURE)
    public void growsTheCropAndLightsTheBlockBetween(GameTestHelper helper) {
        setUp(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            helper.assertTrue(grownByLight(helper), "wheat grew");
            helper.assertTrue(helper.getLevel().getBlockState(pos.below()).is(ModBlocks.PHANTOM_LIGHT)
                    && helper.getLevel().getBlockState(pos.below()).getValue(LightBlock.LIT), "block between is lit");
            remove(helper, pos);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void blackLightGrowsWithoutLight(GameTestHelper helper) {
        GrowLightBlockEntity light = setUp(helper);
        light.setMode(GrowMode.BLACK_LIGHT);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            helper.assertTrue(grownByLight(helper), "wheat grew");
            helper.assertTrue(helper.getLevel().getBlockState(pos.below()).isAir(), "no light block in black light mode");
            remove(helper, pos);
        });
    }

    /** The first growth time is drawn when the light starts (10–30 ticks at the tests' chance of 1), not at once. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotGrowRightAfterPlacing(GameTestHelper helper) {
        setUp(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!grownByLight(helper), "wheat did not grow yet");
            remove(helper, pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotGrowThroughABlock(GameTestHelper helper) {
        // the glass first: placed next to the wheat it would update it, and wheat in a test area that is not lit yet
        // (Forge runs the first batch right at start-up) breaks
        helper.setBlock(LIGHT.below(), Blocks.GLASS);
        setUp(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(!grownByLight(helper), "wheat did not grow");
            remove(helper, pos);
            helper.succeed();
        });
    }

    /** Wheat on farmland, the grow light two blocks above it (placed facing down), energy and redstone. */
    private static GrowLightBlockEntity setUp(GameTestHelper helper) {
        helper.setBlock(CROP.below(), Blocks.FARMLAND);
        helper.setBlock(CROP, Blocks.WHEAT);
        helper.setBlock(LIGHT, ModBlocks.GROW_LIGHT.defaultBlockState());
        helper.setBlock(LIGHT.east(), Blocks.REDSTONE_BLOCK);
        GrowLightBlockEntity light = (GrowLightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        light.electricPower().store().setAmountUnchecked(ElectricPower.CAPACITY);
        return light;
    }

    /**
     * Whether the grow light has grown the wheat: its bonemeal adds 2 to 5 ages ({@code CropBlock.getBonemealAgeIncrease}).
     * The test server keeps random ticks on, and a random tick grows the wheat by 1 (on dry farmland about once in 13
     * random ticks), so age 1 does not count.
     */
    private static boolean grownByLight(GameTestHelper helper) {
        return helper.getBlockState(CROP).getValue(CropBlock.AGE) >= 2;
    }

    private static void remove(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
