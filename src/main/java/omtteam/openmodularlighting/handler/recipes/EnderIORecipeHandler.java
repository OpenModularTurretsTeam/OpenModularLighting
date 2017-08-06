package omtteam.openmodularlighting.handler.recipes;


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings({"deprecation", "ConstantConditions"})
class EnderIORecipeHandler {
    public static void init() {
        ItemStack capacitorBank;
        ItemStack capacitorBankVibrant;
        ItemStack capacitorBankBasic;
        ItemStack basicCapacitor;
        ItemStack doubleCapacitor;
        ItemStack octadicCapacitor;
        ItemStack vibrantCrystal;
        ItemStack electricalSteel;
        ItemStack darkSteel;
        ItemStack conductiveIron;
        ItemStack soularium;
        
		/* ModItems */

        Block capBankBlock = Block.REGISTRY.getObject(new ResourceLocation("EnderIO", "blockCapBank"));
        capacitorBank = new ItemStack(capBankBlock, 1, 2);
        capacitorBankVibrant = new ItemStack(capBankBlock, 1, 3);
        capacitorBankBasic = new ItemStack(capBankBlock, 1, 1);

        Item capacitorItem = Item.REGISTRY.getObject(new ResourceLocation("EnderIO", "itemBasicCapacitor"));
        basicCapacitor = new ItemStack(capacitorItem, 1, 0);
        doubleCapacitor = new ItemStack(capacitorItem, 1, 1);
        octadicCapacitor = new ItemStack(capacitorItem, 1, 2);

        Item materialsItem = Item.REGISTRY.getObject(new ResourceLocation("EnderIO", "itemMaterial"));
        vibrantCrystal = new ItemStack(materialsItem, 1, 6);

        Item alloyItem = Item.REGISTRY.getObject(new ResourceLocation("EnderIO", "itemAlloy"));
        electricalSteel = new ItemStack(alloyItem, 1, 0);
        darkSteel = new ItemStack(alloyItem, 1, 6);
        conductiveIron = new ItemStack(alloyItem, 1, 4);
        soularium = new ItemStack(alloyItem, 1, 7);

    }
}
