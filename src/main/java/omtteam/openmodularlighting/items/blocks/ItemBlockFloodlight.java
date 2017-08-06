package omtteam.openmodularlighting.items.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularlighting.util.LightAddonUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

/**
 * Created by Keridos on 22/07/16.
 * This Class
 */
@SuppressWarnings("deprecation")
public class ItemBlockFloodlight extends ItemBlockBaseAddon {
    public ItemBlockFloodlight(Block block) {
        super(block);
        setHasSubtypes(true);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Blocks.floodlight);
    }

    private final static String[] subNames = {
            OMLNames.Blocks.floodlightTierOne, OMLNames.Blocks.floodlightTierTwo, OMLNames.Blocks.floodlightTierThree,
            OMLNames.Blocks.floodlightTierFour, OMLNames.Blocks.floodlightTierFive,
    };

    @Override
    @ParametersAreNonnullByDefault
    public void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 5; i++) {
            subItems.add(new ItemStack(ModBlocks.floodlight, 1, i));
        }
    }

    @Override
    @Nonnull
    public String getUnlocalizedName(ItemStack itemStack) {
        return "tile." + subNames[itemStack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }


    @Override
    public EnumFacing getBaseFacing(World world, BlockPos pos) {
        return LightAddonUtil.getBaseFacing(world, pos);
    }

    @Override
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
        switch (stack.getMetadata()) {
            case 0:
                tooltip.add("");
                tooltip.add(TextFormatting.GOLD + safeLocalize("tooltip.floodlight.label"));
                tooltip.add("");
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.desc"));
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.range") + " 16.");
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.floodlight.1"));
                return;
            case 1:
                tooltip.add("");
                tooltip.add(TextFormatting.GOLD + safeLocalize("tooltip.floodlight.label"));
                tooltip.add("");
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.desc"));
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.range") + " 24.");
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.floodlight.inv.2"));
                return;
            case 2:
                tooltip.add("");
                tooltip.add(TextFormatting.GOLD + safeLocalize("tooltip.floodlight.label"));
                tooltip.add("");
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.desc"));
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.range") + " 40.");
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.floodlight.inv.3"));
                return;
            case 3:
                tooltip.add("");
                tooltip.add(TextFormatting.GOLD + safeLocalize("tooltip.floodlight.label"));
                tooltip.add("");
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.desc2"));
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.range") + " 48.");
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.floodlight.inv.4"));
                return;
            case 4:
                tooltip.add("");
                tooltip.add(TextFormatting.GOLD + safeLocalize("tooltip.floodlight.label"));
                tooltip.add("");
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.desc2"));
                tooltip.add(TextFormatting.WHITE + safeLocalize("tooltip.floodlight.range") + " 64.");
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.floodlight.inv.5"));
        }
    }
}
