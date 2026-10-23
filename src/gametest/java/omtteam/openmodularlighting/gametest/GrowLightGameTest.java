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
//? if neoforge || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class GrowLightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 2, 3);
    private static final BlockPos CROP = new BlockPos(3, 0, 3);

    @GameTest(template = EMPTY_STRUCTURE)
    public void growsTheCropAndLightsTheBlockBetween(GameTestHelper helper) {
        GrowLightBlockEntity light = setUp(helper);
        BlockPos pos = helper.absolutePos(LIGHT);
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(CROP).getValue(CropBlock.AGE) > 0, "wheat grew");
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
            helper.assertTrue(helper.getBlockState(CROP).getValue(CropBlock.AGE) > 0, "wheat grew");
            helper.assertTrue(helper.getLevel().getBlockState(pos.below()).isAir(), "no light block in black light mode");
            remove(helper, pos);
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
            helper.assertTrue(helper.getBlockState(CROP).getValue(CropBlock.AGE) == 0, "wheat did not grow");
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

    private static void remove(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
