package omtteam.openmodularlighting.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import omtteam.omlib.compatibility.minecraft.CompatItem;
import omtteam.openmodularlighting.OpenModularLighting;
import omtteam.openmodularlighting.init.ModItems;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class IntermediateProductTiered extends CompatItem {
    public IntermediateProductTiered() {
        super();

        this.setHasSubtypes(true);
        this.setCreativeTab(OpenModularLighting.creativeTab);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Items.intermediateTieredItem);
        this.setUnlocalizedName(OMLNames.Items.intermediateTieredItem);
    }

    public final static String[] subNames = {
            OMLNames.Items.lightBulbTierOne, OMLNames.Items.lightBulbTierTwo, OMLNames.Items.lightBulbTierThree,
            OMLNames.Items.lightBulbTierFour, OMLNames.Items.lightBulbTierFive,
    };

    @Override
    @ParametersAreNonnullByDefault
    public void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 5; i++) {
            subItems.add(new ItemStack(ModItems.intermediateProductTiered, 1, i));
        }
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUnlocalizedName(ItemStack itemStack) {
        return "item." + subNames[itemStack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}