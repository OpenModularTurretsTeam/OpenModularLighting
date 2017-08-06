package omtteam.openmodularlighting.handler.recipes;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import omtteam.openmodularlighting.OpenModularLighting;


class MekanismRecipeHandler {
    private static void postErrorMessage() {
        OpenModularLighting.getLogger().info("Error while initializing Mekanism recipes, please report to OMT devs!");
    }

    public static void init() {
        String Mek = "Mekanism";

        Item ironEnrichedItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "EnrichedIron"));
        Item alloyEnrichedItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "EnrichedAlloy"));
        Item alloyReinforcedItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "ReinforcedAlloy"));
        Item alloyAtomicItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "AtomicAlloy"));
        Item energyTabletItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "EnergyTablet"));
        Item controlCircuitItem = Item.REGISTRY.getObject(new ResourceLocation(Mek, "ControlCircuit"));
        ItemStack ironEnriched;
        ItemStack alloyEnriched;
        ItemStack alloyReinforced;
        ItemStack alloyAtomic;
        ItemStack energyTablet;
        ItemStack controlCircuit;


		/* ModItems */
        if (ironEnrichedItem != null) {
            ironEnriched = new ItemStack(ironEnrichedItem, 1);
        } else {
            postErrorMessage();
            return;
        }
        if (alloyEnrichedItem != null) {
            alloyEnriched = new ItemStack(alloyEnrichedItem, 1);
        } else {
            postErrorMessage();
            return;
        }
        if (alloyReinforcedItem != null) {
            alloyReinforced = new ItemStack(alloyReinforcedItem, 1);
        } else {
            postErrorMessage();
            return;
        }
        if (alloyAtomicItem != null) {
            alloyAtomic = new ItemStack(alloyAtomicItem, 1);
        } else {
            postErrorMessage();
            return;
        }
        if (energyTabletItem != null) {
            energyTablet = new ItemStack(energyTabletItem, 1);
            energyTablet.setItemDamage(OreDictionary.WILDCARD_VALUE);
        } else {
            postErrorMessage();
            return;
        }
        if (controlCircuitItem != null) {
            controlCircuit = new ItemStack(controlCircuitItem, 1);
        } else {
            postErrorMessage();
            return;
        }


    }
}
