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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoRules;
import omtteam.omlib.compat.VersionCompat;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.menu.CamoMenu;
import omtteam.openmodularlighting.menu.CarbonFloodlightMenu;
import omtteam.openmodularlighting.menu.ElectricFloodlightMenu;
import omtteam.openmodularlighting.registry.ModBlocks;

import java.util.List;

/**
 * Disguising the electric, carbon and UV floodlights (CLAUDE.md §6.11): which blocks are valid, the disguise slot of
 * the camouflage menu, and that the disguise is saved, sent to clients and kept on the dropped item. The rendering itself is
 * client-only (check it with {@code runClient}).
 */
//? if (neoforge && <1.21.11) || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class CamoGameTest implements OMLGameTests {
    private static final BlockPos LIGHT = new BlockPos(3, 1, 3);
    private static final BlockPos OTHER = new BlockPos(5, 1, 3);
    /** The first slot of the player's main inventory in the menu (inventory slot 9). */
    private static final int INVENTORY_SLOT = 0;

    @GameTest(template = EMPTY_STRUCTURE)
    public void onlyFullOpaqueBlocksAreValidDisguises(GameTestHelper helper) {
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.STONE)) == Blocks.STONE.defaultBlockState(), "stone");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.OAK_PLANKS)) != null, "planks");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.GLASS)) == null, "glass is see-through");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.OAK_LEAVES)) == null, "leaves are see-through");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.OAK_SLAB)) == null, "a slab is not a full block");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.CHEST)) == null, "a chest has a block entity");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(ModBlocks.ELECTRIC_FLOODLIGHT)) == null, "no floodlight");
        helper.assertTrue(CamoRules.disguiseFor(new ItemStack(Items.STICK)) == null, "not a block");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void disguiseSlotSetsAndRemovesTheDisguise(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.ELECTRIC_FLOODLIGHT);
        Player player = OMLGameTests.mockPlayer(helper);
        CamoMenu menu = CamoMenu.server(0, player.getInventory(), light);
        helper.assertTrue(!menu.showsLight(), "no light sliders on a light");

        menu.setCarried(new ItemStack(Items.STONE));
        menu.clicked(CamoMenu.PHANTOM_SLOT, 0, ClickType.PICKUP, player);
        helper.assertTrue(light.getCamo() == Blocks.STONE.defaultBlockState(), "stone set, got " + light.getCamo());
        helper.assertTrue(menu.getCarried().is(Items.STONE) && menu.getCarried().getCount() == 1, "stone stays in the cursor");
        helper.assertTrue(menu.getSlot(CamoMenu.PHANTOM_SLOT).getItem().is(Items.STONE), "slot shows the disguise");

        menu.setCarried(new ItemStack(Items.GLASS));
        menu.clicked(CamoMenu.PHANTOM_SLOT, 0, ClickType.PICKUP, player);
        helper.assertTrue(light.getCamo() == Blocks.STONE.defaultBlockState(), "glass is refused");

        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(CamoMenu.PHANTOM_SLOT, 0, ClickType.PICKUP, player);
        helper.assertTrue(light.getCamo() == null, "empty cursor removes the disguise");
        helper.assertTrue(menu.getSlot(CamoMenu.PHANTOM_SLOT).getItem().isEmpty(), "slot is empty again");
        remove(helper, LIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void shiftClickInTheCamouflageMenuSetsTheDisguise(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.UV_FLOODLIGHT);
        Player player = OMLGameTests.mockPlayer(helper);
        CamoMenu menu = CamoMenu.server(0, player.getInventory(), light);

        player.getInventory().setItem(9, new ItemStack(Items.STONE_BRICKS));
        menu.quickMoveStack(player, INVENTORY_SLOT);
        helper.assertTrue(light.getCamo() == Blocks.STONE_BRICKS.defaultBlockState(), "stone bricks set");
        helper.assertTrue(player.getInventory().getItem(9).is(Items.STONE_BRICKS), "the stack stays in the inventory");
        remove(helper, LIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void shiftClickInTheLightMenuNeverSetsTheDisguise(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.CARBON_FLOODLIGHT.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        CarbonFloodlightBlockEntity light = (CarbonFloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        Player player = OMLGameTests.mockPlayer(helper);
        CarbonFloodlightMenu menu = CarbonFloodlightMenu.server(0, player.getInventory(), light);
        helper.assertTrue(menu.supportsCamo(), "the screen offers the camouflage menu");

        // a coal block is fuel: it goes into the fuel slot
        player.getInventory().setItem(9, new ItemStack(Items.COAL_BLOCK));
        menu.quickMoveStack(player, INVENTORY_SLOT);
        helper.assertTrue(light.getInventory().getItem(0).is(Items.COAL_BLOCK), "coal block is fuel");
        // stone bricks are not: the stack stays and nothing is disguised
        player.getInventory().setItem(10, new ItemStack(Items.STONE_BRICKS));
        menu.quickMoveStack(player, INVENTORY_SLOT + 1);
        helper.assertTrue(light.getCamo() == null, "the light menu does not disguise");
        helper.assertTrue(player.getInventory().getItem(10).is(Items.STONE_BRICKS), "the stack stays in the inventory");
        light.getInventory().setItem(0, ItemStack.EMPTY);
        remove(helper, LIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void backReopensTheLightMenu(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.ELECTRIC_FLOODLIGHT);
        helper.assertTrue(light.isCamoEnabled() && light.hasParentMenu(), "the camouflage menu has a parent");
        helper.assertTrue(!light.supportsLight() && light.getLight().equals(CamoLight.DEFAULT), "no light settings");
        remove(helper, LIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void panelLightsCannotBeDisguised(GameTestHelper helper) {
        helper.setBlock(LIGHT, ModBlocks.SQUARE_FLUORESCENT_LIGHT.defaultBlockState());
        FloodlightBlockEntity light = (FloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(LIGHT));
        Player player = OMLGameTests.mockPlayer(helper);
        ElectricFloodlightMenu menu = ElectricFloodlightMenu.server(0, player.getInventory(), light, light.electricPower());
        helper.assertTrue(!menu.supportsCamo(), "no camouflage button");
        helper.assertTrue(!light.isCamoEnabled() && !light.canSetCamo(player), "not a camouflage host");
        light.setCamo(Blocks.STONE.defaultBlockState());
        helper.assertTrue(light.getCamo() == null, "setCamo is ignored");
        remove(helper, LIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void disguiseIsSavedSentToClientsAndKeptOnTheDrop(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.UV_FLOODLIGHT);
        light.setCamo(Blocks.BRICKS.defaultBlockState());
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(LIGHT);

        FloodlightBlockEntity loaded = place(helper, OTHER, ModBlocks.UV_FLOODLIGHT);
        BlockEntityData.load(loaded, BlockEntityData.save(light, level.registryAccess()), level.registryAccess());
        helper.assertTrue(loaded.getCamo() == Blocks.BRICKS.defaultBlockState(), "saved and loaded");

        CompoundTag client = BlockEntityData.clientData(light, level.registryAccess());
        helper.assertTrue(client.contains("camo"), "sent to clients: " + client);

        List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, light);
        helper.assertTrue(drops.size() == 1 && VersionCompat.hasBlockEntityData(drops.get(0)),
                "the drop keeps the disguise, got " + drops);
        loaded.setCamo(null);
        BlockEntityData.load(loaded, light.saveItemData(), level.registryAccess());
        helper.assertTrue(loaded.getCamo() == Blocks.BRICKS.defaultBlockState(), "placing the drop restores it");
        remove(helper, LIGHT);
        remove(helper, OTHER);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void invalidStoredDisguiseIsIgnored(GameTestHelper helper) {
        FloodlightBlockEntity light = place(helper, ModBlocks.ELECTRIC_FLOODLIGHT);
        CompoundTag tag = light.saveItemData();
        tag.put("camo", NbtUtils.writeBlockState(Blocks.GLASS.defaultBlockState()));
        BlockEntityData.load(light, tag, helper.getLevel().registryAccess());
        helper.assertTrue(light.getCamo() == null, "glass from NBT is not used");
        remove(helper, LIGHT);
        helper.succeed();
    }

    private static FloodlightBlockEntity place(GameTestHelper helper, FloodlightBlock block) {
        return place(helper, LIGHT, block);
    }

    private static FloodlightBlockEntity place(GameTestHelper helper, BlockPos relative, FloodlightBlock block) {
        helper.setBlock(relative, block.defaultBlockState().setValue(FloodlightBlock.FACING, Direction.UP));
        return (FloodlightBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    /** Removes the light, which removes its beam. */
    private static void remove(GameTestHelper helper, BlockPos relative) {
        helper.setBlock(relative, Blocks.AIR);
    }
}
