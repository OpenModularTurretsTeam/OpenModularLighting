package omtteam.openmodularlighting.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.compatibility.minecraft.CompatItem;
import omtteam.openmodularlighting.OpenModularLighting;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.init.ModItems;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

@SuppressWarnings("deprecation")
public class UpgradeMetaItem extends CompatItem {
    public UpgradeMetaItem() {
        super();

        this.setHasSubtypes(true);
        this.setCreativeTab(OpenModularLighting.creativeTab);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Items.upgradeMetaItem);
        this.setUnlocalizedName(OMLNames.Items.upgradeMetaItem);
    }

    public final static String[] subNames = {
            OMLNames.Items.efficiencyUpgrade,
    };

    @Override
    @ParametersAreNonnullByDefault
    public void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 1; i++) {
            subItems.add(new ItemStack(ModItems.upgradeMetaItem, 1, i));
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

    @SideOnly(Side.CLIENT)
    @Override
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean isAdvanced) {
        switch (stack.getMetadata()) {
            case 0:
                tooltip.add("");
                tooltip.add(TextFormatting.BLUE + safeLocalize("turret.upgrade.label"));
                tooltip.add("");
                tooltip.add("- " + ConfigHandler.getEfficiencyUpgradeBoostPercentage() * 100 + "% " + safeLocalize(
                        "turret.upgrade.eff"));
                tooltip.add(safeLocalize("turret.upgrade.stacks"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("turret.upgrade.eff.flavour"));
        }
    }
}
