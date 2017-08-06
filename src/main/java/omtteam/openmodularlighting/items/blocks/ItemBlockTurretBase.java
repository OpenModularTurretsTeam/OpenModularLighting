package omtteam.openmodularlighting.items.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import omtteam.omlib.compatibility.minecraft.CompatItemBlock;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

@SuppressWarnings("deprecation")
public class ItemBlockTurretBase extends CompatItemBlock {
    public ItemBlockTurretBase(Block block) {
        super(block);
        setHasSubtypes(true);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Blocks.lightingBase);
    }

    private final static String[] subNames = {
            "_tier_one", "_tier_two", "_tier_three", "_tier_four", "_tier_five"
    };

    @Override
    @ParametersAreNonnullByDefault
    public void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 5; i++) {
            subItems.add(new ItemStack(ModBlocks.lightingBase, 1, i));
        }
    }

    @Override
    @Nonnull
    public String getUnlocalizedName(ItemStack itemStack) {
        return "tile." + OMLNames.Blocks.lightingBase + subNames[itemStack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
        tooltip.add(safeLocalize(OMLNames.Localizations.GUI.LIGHT_BASE_DESCRIPTION));

        switch (stack.getMetadata()) {
            case 0:
                tooltip.add("");
                tooltip.add(TextFormatting.AQUA + "--" + safeLocalize("tooptip.energy.label") + "--");
                tooltip.add(safeLocalize("tooltip.rf.max") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierOneMaxCharge());
                tooltip.add(safeLocalize("tooltip.rf.io") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierOneMaxIo());
                tooltip.add("");
                tooltip.add(TextFormatting.GREEN + "--" + safeLocalize("tooltip.extras.label") + "--");
                tooltip.add(safeLocalize("tooltip.extras.addons.0"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.1"));
                return;
            case 1:
                tooltip.add("");
                tooltip.add(TextFormatting.AQUA + "--" + safeLocalize("tooptip.energy.label") + "--");
                tooltip.add(safeLocalize("tooltip.rf.max") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierTwoMaxCharge());
                tooltip.add(safeLocalize("tooltip.rf.io") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierTwoMaxIo());
                tooltip.add("");
                tooltip.add(TextFormatting.GREEN + "--" + safeLocalize("tooltip.extras.label") + "--");
                tooltip.add(safeLocalize("tooltip.extras.addons.2"));
                tooltip.add(safeLocalize("tooltip.extras.upgrade.1"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.2"));
                return;
            case 2:
                tooltip.add("");
                tooltip.add(TextFormatting.AQUA + "--" + safeLocalize("tooptip.energy.label") + "--");
                tooltip.add(safeLocalize("tooltip.rf.max") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierThreeMaxCharge());
                tooltip.add(safeLocalize("tooltip.rf.io") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierThreeMaxIo());
                tooltip.add("");
                tooltip.add(TextFormatting.GREEN + "--" + safeLocalize("tooltip.extras.label") + "--");
                tooltip.add(safeLocalize("tooltip.extras.addons.2"));
                tooltip.add(safeLocalize("tooltip.extras.upgrade.1"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.3"));
                return;
            case 3:
                tooltip.add("");
                tooltip.add(TextFormatting.AQUA + "--" + safeLocalize("tooptip.energy.label") + "--");
                tooltip.add(safeLocalize("tooltip.rf.max") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierFourMaxCharge());
                tooltip.add(safeLocalize("tooltip.rf.io") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierFourMaxIo());
                tooltip.add("");
                tooltip.add(TextFormatting.GREEN + "--" + safeLocalize("tooltip.extras.label") + "--");
                tooltip.add(safeLocalize("tooltip.extras.addons.2"));
                tooltip.add(safeLocalize("tooltip.extras.upgrade.1"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.4"));
                return;
            case 4:
                tooltip.add("");
                tooltip.add(TextFormatting.AQUA + "--" + safeLocalize("tooptip.energy.label") + "--");
                tooltip.add(safeLocalize("tooltip.rf.max") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierFiveMaxCharge());
                tooltip.add(safeLocalize("tooltip.rf.io") + ": " + TextFormatting.WHITE +
                        ConfigHandler.getBaseTierFiveMaxIo());
                tooltip.add("");
                tooltip.add(TextFormatting.GREEN + "--" + safeLocalize("tooltip.extras.label") + "--");
                tooltip.add(safeLocalize("tooltip.extras.addons.2"));
                tooltip.add(safeLocalize("tooltip.extras.upgrade.2"));
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.5a"));
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.base.5b"));
        }
    }
}
