package omtteam.openmodularlighting.gametest;

//? if neoforge {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import omtteam.openmodularlighting.OML;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.LightBlockEntity;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.world.LightBlocks;
import omtteam.openmodularlighting.world.Positions;

/**
 * In-world tests of the phantom light block (CLAUDE.md §6.3): persistent light blocks that are switched with
 * {@code LIT}, behave like air and clean up after themselves.
 */
//? if neoforge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class PhantomLightGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(2, 2, 2);
    private static final BlockPos SOURCE_A = new BlockPos(2, 2, 5);
    private static final BlockPos SOURCE_B = new BlockPos(5, 2, 2);

    // ---- Placing and switching ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void joinPlacesAnUnlitPhantomLightInAir(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        helper.assertTrue(LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, helper.absolutePos(SOURCE_A), false),
                "join into air");
        BlockState state = level.getBlockState(light);
        helper.assertTrue(state.is(ModBlocks.PHANTOM_LIGHT), "phantom light placed");
        helper.assertTrue(!state.getValue(LightBlock.LIT), "placed unlit");
        helper.assertTrue(state.getLightEmission() == 0, "no light while unlit");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void joinDoesNotReplaceOtherBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(LIGHT, Blocks.STONE);
        helper.setBlock(LIGHT.above(), Blocks.GLASS);
        helper.assertTrue(!LightBlocks.join(level, helper.absolutePos(LIGHT), ModBlocks.PHANTOM_LIGHT,
                helper.absolutePos(SOURCE_A), true), "stone is not replaced");
        helper.assertTrue(!LightBlocks.join(level, helper.absolutePos(LIGHT.above()), ModBlocks.PHANTOM_LIGHT,
                helper.absolutePos(SOURCE_A), true), "glass is not replaced");
        helper.assertTrue(helper.getBlockState(LIGHT).is(Blocks.STONE) && helper.getBlockState(LIGHT.above()).is(Blocks.GLASS),
                "blocks unchanged");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void litWhileAnySourceIsOnAndRemovedWithTheLastSource(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        BlockPos a = helper.absolutePos(SOURCE_A);
        BlockPos b = helper.absolutePos(SOURCE_B);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, a, false);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, b, false);

        LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, a, true);
        assertLit(helper, true, "A on");
        LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, b, true);
        LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, a, false);
        assertLit(helper, true, "B still on");
        LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, b, false);
        assertLit(helper, false, "both off");

        LightBlocks.leave(level, light, ModBlocks.PHANTOM_LIGHT, a);
        helper.assertTrue(helper.getBlockState(LIGHT).is(ModBlocks.PHANTOM_LIGHT), "kept while B is a source");
        LightBlocks.leave(level, light, ModBlocks.PHANTOM_LIGHT, b);
        helper.assertTrue(helper.getBlockState(LIGHT).isAir(), "removed with the last source");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void litPhantomLightGivesLightLevel15(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, helper.absolutePos(SOURCE_A), true);
        helper.succeedWhen(() -> {
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, light) == 15, "block light 15");
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, light.above()) == 14, "light spreads");
        });
    }

    /**
     * Own batch, so that no light from tests running at the same time reaches this one. Finished tests leave
     * beacons (light 15) next to their structures, so the area is not completely dark: the light has to return to
     * the level measured before placing the phantom light.
     */
    @GameTest(template = EMPTY_STRUCTURE, batch = "phantom_light_darkness")
    public void switchingOffRemovesTheLight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        BlockPos source = helper.absolutePos(SOURCE_A);
        int ambient = level.getBrightness(LightLayer.BLOCK, light);
        helper.assertTrue(ambient < 15, "test area must not be fully lit already");
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, source, true);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, light) == 15, "lit first");
            LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, source, false);
        });
        helper.runAfterDelay(6, () -> helper.succeedWhen(() -> helper.assertTrue(
                level.getBrightness(LightLayer.BLOCK, light) == ambient, "back to the ambient light " + ambient
                        + ", but block light is " + level.getBrightness(LightLayer.BLOCK, light))));
    }

    // ---- Random-tick cleanup ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void randomTickRemovesOrphans(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        // the source position holds no light source at all
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, helper.absolutePos(SOURCE_A), true);
        randomTick(level, light);
        helper.assertTrue(level.getBlockState(light).isAir(), "orphan removed");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void randomTickFollowsTheSource(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        TestLightSourceBlockEntity source = placeSource(helper, SOURCE_A);
        source.covered.add(light);
        source.on = true;
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, source.getBlockPos(), false);

        randomTick(level, light);
        assertLit(helper, true, "stale LIT fixed: the source is on");
        source.on = false;
        randomTick(level, light);
        assertLit(helper, false, "the source turned off");
        source.covered.clear();
        randomTick(level, light);
        helper.assertTrue(level.getBlockState(light).isAir(), "removed: the source no longer covers it");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void randomTickKeepsSourcesInUnloadedChunks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        BlockPos farAway = light.offset(1_000_000, 0, 0);
        helper.assertTrue(!level.hasChunkAt(farAway), "test needs an unloaded source chunk");
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, farAway, true);
        randomTick(level, light);
        assertLit(helper, true, "kept as it was");
        helper.succeed();
    }

    // ---- Re-cast requests ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void replacingTheLightOrChangingANeighbourRequestsARecast(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        TestLightSourceBlockEntity source = placeSource(helper, SOURCE_A);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, source.getBlockPos(), true);
        // light blocks are switched and placed without neighbour updates, so they never trigger each other
        LightBlocks.join(level, light.east(), ModBlocks.PHANTOM_LIGHT, source.getBlockPos(), true);
        LightBlocks.setActive(level, light, ModBlocks.PHANTOM_LIGHT, source.getBlockPos(), false);
        helper.assertTrue(source.recastRequests == 0, "no re-cast from light block changes, got " + source.recastRequests);

        level.setBlock(light.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(source.recastRequests > 0, "neighbour change requests a re-cast");

        int before = source.recastRequests;
        level.setBlock(light, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(source.recastRequests > before, "replacing the light requests a re-cast");
        helper.succeed();
    }

    // ---- Behaves like air ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void behavesLikeAir(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, helper.absolutePos(SOURCE_A), false);
        BlockState state = level.getBlockState(light);
        helper.assertTrue(state.canBeReplaced(), "replaceable");
        helper.assertTrue(state.is(BlockTags.REPLACEABLE), "in #minecraft:replaceable");
        helper.assertTrue(state.is(BlockTags.REPLACEABLE_BY_TREES), "in #minecraft:replaceable_by_trees");
        helper.assertTrue(state.getCollisionShape(level, light).isEmpty(), "no collision");
        helper.assertTrue(state.getShape(level, light).isEmpty(), "no outline");
        helper.assertTrue(state.getPistonPushReaction() == PushReaction.DESTROY, "destroyed by pistons");
        helper.assertTrue(state.propagatesSkylightDown(level, light), "lets sky light through");
        helper.assertTrue(NaturalSpawner.isValidEmptySpawnBlock(level, light, state, state.getFluidState(), EntityType.ZOMBIE),
                "mobs can spawn in an unlit phantom light");
        helper.succeed();
    }

    // ---- Saving ----

    @GameTest(template = EMPTY_STRUCTURE)
    public void sourcesSurviveSaveAndLoadAndAreNotSynced(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos light = helper.absolutePos(LIGHT);
        BlockPos a = helper.absolutePos(SOURCE_A);
        BlockPos b = helper.absolutePos(SOURCE_B);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, a, true);
        LightBlocks.join(level, light, ModBlocks.PHANTOM_LIGHT, b, false);
        LightBlockEntity original = (LightBlockEntity) level.getBlockEntity(light);

        CompoundTag tag = BlockEntityData.save(original, level.registryAccess());
        LightBlockEntity loaded = new PhantomLightBlockEntity(light, level.getBlockState(light));
        BlockEntityData.load(loaded, tag, level.registryAccess());
        helper.assertTrue(loaded.sources().sources().equals(original.sources().sources()), "sources restored");
        helper.assertTrue(loaded.sources().activeSources().equals(java.util.Set.of(Positions.toPos(a))),
                "active sources restored");
        helper.assertTrue(BlockEntityData.clientData(original, level.registryAccess()).isEmpty(),
                "nothing sent to clients");
        helper.succeed();
    }

    // ---- Helpers ----

    private static TestLightSourceBlockEntity placeSource(GameTestHelper helper, BlockPos relative) {
        helper.setBlock(relative, TestBlocks.TEST_LIGHT_SOURCE);
        return (TestLightSourceBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static void randomTick(ServerLevel level, BlockPos pos) {
        level.getBlockState(pos).randomTick(level, pos, level.getRandom());
    }

    private static void assertLit(GameTestHelper helper, boolean lit, String message) {
        BlockState state = helper.getBlockState(LIGHT);
        helper.assertTrue(state.is(ModBlocks.PHANTOM_LIGHT) && state.getValue(LightBlock.LIT) == lit, message);
    }
}
