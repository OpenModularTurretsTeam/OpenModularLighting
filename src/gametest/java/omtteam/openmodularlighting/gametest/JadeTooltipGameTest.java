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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.jade.FloodlightTooltip;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.registry.ModBlocks;

import java.util.List;

/**
 * The block info overlay content (1.7.10 WAILA: invert and mode). Both loaders load the mod's en_us translations on
 * the server too, so this also checks the lang file.
 */
//? if neoforge || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class JadeTooltipGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 1, 3);

    @GameTest(template = EMPTY_STRUCTURE)
    public void floodlightShowsInvertAndMode(GameTestHelper helper) {
        ElectricFloodlightBlockEntity light = (ElectricFloodlightBlockEntity) place(helper, ModBlocks.ELECTRIC_FLOODLIGHT);
        light.toggleInverted();
        light.setMode(BeamMode.NARROW_CONE);
        List<String> lines = lines(light);
        helper.assertTrue(lines.size() == 2, "two lines, got " + lines);
        helper.assertTrue(lines.get(0).equals("Inverted: true"), lines.get(0));
        helper.assertTrue(lines.get(1).equals("Mode: narrow cone"), lines.get(1));
        remove(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void growLightShowsItsModeAndLightsWithoutModeOnlyInvert(GameTestHelper helper) {
        List<String> grow = lines(place(helper, ModBlocks.GROW_LIGHT));
        helper.assertTrue(grow.size() == 2 && grow.get(1).equals("Mode: Normal lighting"),
                "grow light mode, got " + grow);
        remove(helper);
        for (Block block : List.of(ModBlocks.UV_FLOODLIGHT, ModBlocks.SQUARE_FLUORESCENT_LIGHT)) {
            List<String> lines = lines(place(helper, block));
            helper.assertTrue(lines.size() == 1 && lines.get(0).equals("Inverted: false"),
                    block + ": only invert, got " + lines);
            remove(helper);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void noDataNoLines(GameTestHelper helper) {
        helper.assertTrue(FloodlightTooltip.lines(new CompoundTag()).isEmpty(), "nothing without server data");
        helper.succeed();
    }

    private static List<String> lines(FloodlightBlockEntity light) {
        CompoundTag tag = new CompoundTag();
        FloodlightTooltip.write(tag, light);
        return FloodlightTooltip.lines(tag).stream().map(Component::getString).toList();
    }

    private static FloodlightBlockEntity place(GameTestHelper helper, Block block) {
        helper.setBlock(LIGHT, block);
        return (FloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
    }

    private static void remove(GameTestHelper helper) {
        helper.getLevel().setBlock(helper.absolutePos(LIGHT), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
