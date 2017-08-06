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
import omtteam.openmodularlighting.init.ModItems;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

@SuppressWarnings("deprecation")
public class AddonMetaItem extends CompatItem {
    public AddonMetaItem() {
        super();

        this.setHasSubtypes(true);
        this.setCreativeTab(OpenModularLighting.creativeTab);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Items.addonMetaItem);
        this.setUnlocalizedName(OMLNames.Items.addonMetaItem);
    }

    public final static String[] subNames = {
            OMLNames.Items.concealerAddon, OMLNames.Items.fakeDropsAddon
    };

    @Override
    @ParametersAreNonnullByDefault
    public void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 2; i++) {
            subItems.add(new ItemStack(ModItems.addonMetaItem, 1, i));
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
                tooltip.add(TextFormatting.RED + safeLocalize("turret.addon.label"));
                tooltip.add("");
                tooltip.add(safeLocalize("turret.addon.concealer.1"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("turret.addon.concealer.flavour"));
                return;
            case 1:
                tooltip.add("");
                tooltip.add(TextFormatting.RED + safeLocalize("turret.addon.label"));
                tooltip.add("");
                tooltip.add(safeLocalize("turret.addon.fakedrops.a"));
                tooltip.add(safeLocalize("turret.addon.fakedrops.b"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("turret.addon.fakedrops.flavour"));
        }
    }
}
