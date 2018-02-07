package omtteam.openmodularlighting.blocks;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.IHasItemBlock;
import omtteam.omlib.blocks.BlockAbstractCamoTileEntity;
import omtteam.omlib.compatibility.theoneprobe.TOPInfoProvider;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.tileentity.EnumMachineMode;
import omtteam.omlib.util.PlayerUtil;
import omtteam.omlib.util.compat.ItemStackTools;
import omtteam.openmodularlighting.OpenModularLighting;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.items.UsableMetaItem;
import omtteam.openmodularlighting.items.blocks.ItemBlockLightingBase;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularlighting.tileentity.LightingBase;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.*;
import static omtteam.omlib.util.compat.ChatTools.addChatMessage;

@SuppressWarnings("deprecation")
public class BlockLightingBase extends BlockAbstractCamoTileEntity implements IHasItemBlock, TOPInfoProvider {
    public static final PropertyInteger TIER = PropertyInteger.create("tier", 1, 5);

    public BlockLightingBase() {
        super(Material.ROCK);
        this.setCreativeTab(OpenModularLighting.creativeTab);
        setDefaultState(this.blockState.getBaseState().withProperty(TIER, 1));
        this.setSoundType(SoundType.STONE);
        this.setUnlocalizedName(OMLNames.Blocks.lightingBase);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Blocks.lightingBase);
    }

    @Override
    public ItemBlock getItemBlock(Block block) {
        return new ItemBlockLightingBase(block);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state) {
        int MaxCharge;
        int MaxIO;
        switch (state.getValue(TIER) - 1) {
            case 0:
                MaxCharge = ConfigHandler.getBaseTierOneMaxCharge();
                MaxIO = ConfigHandler.getBaseTierOneMaxIo();
                return new LightingBase(MaxCharge, MaxIO, 1, state);
            case 1:
                MaxCharge = ConfigHandler.getBaseTierTwoMaxCharge();
                MaxIO = ConfigHandler.getBaseTierTwoMaxIo();
                return new LightingBase(MaxCharge, MaxIO, 2, state);
            case 2:
                MaxCharge = ConfigHandler.getBaseTierThreeMaxCharge();
                MaxIO = ConfigHandler.getBaseTierThreeMaxIo();
                return new LightingBase(MaxCharge, MaxIO, 3, state);
            case 3:
                MaxCharge = ConfigHandler.getBaseTierFourMaxCharge();
                MaxIO = ConfigHandler.getBaseTierFourMaxIo();
                return new LightingBase(MaxCharge, MaxIO, 4, state);
            case 4:
                MaxCharge = ConfigHandler.getBaseTierFiveMaxCharge();
                MaxIO = ConfigHandler.getBaseTierFiveMaxIo();
                return new LightingBase(MaxCharge, MaxIO, 5, state);
            default:
                return new LightingBase(1, 1, 1, state);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return true;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TIER, meta + 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TIER) - 1;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{TIER}, new IUnlistedProperty[]{RENDERBLOCKSTATE});
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected boolean clOnBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
            ItemStack heldItem = player.getHeldItemMainhand();
            LightingBase base = (LightingBase) world.getTileEntity(pos);
            if (player.isSneaking() && ConfigHandler.isAllowBaseCamo() && heldItem == ItemStackTools.getEmptyStack()) {
                if (base != null) {
                    if (player.getUniqueID().toString().equals(base.getOwner())) {
                        base.setCamoState(state);
                        world.notifyBlockUpdate(pos, state, state, 3);
                    } else {
                        addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                    }
                }
            }

            Block heldItemBlock = null;

            if (heldItem != ItemStackTools.getEmptyStack()) {
                heldItemBlock = Block.getBlockFromItem(heldItem.getItem());
            }

            if (!player.isSneaking() && ConfigHandler.isAllowBaseCamo() && heldItem != ItemStackTools.getEmptyStack() && heldItem.getItem() instanceof ItemBlock &&
                    heldItemBlock.isNormalCube(heldItemBlock.getStateFromMeta(heldItem.getMetadata())) && Block.getBlockFromItem(
                    heldItem.getItem()).isOpaqueCube(heldItemBlock.getStateFromMeta(heldItem.getMetadata())) && !(Block.getBlockFromItem(
                    heldItem.getItem()) instanceof BlockLightingBase)) {
                if (base != null) {
                    if (player.getUniqueID().toString().equals(base.getOwner())) {
                        base.setCamoState(heldItemBlock.getStateFromMeta(heldItem.getItemDamage()));
                        world.notifyBlockUpdate(pos, state, state, 3);
                    } else {
                        addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                    }
                }

            } else if (player.isSneaking() && base != null && player.getHeldItemMainhand() != ItemStackTools.getEmptyStack() &&
                    player.getHeldItemMainhand().getItem() instanceof UsableMetaItem && player.getHeldItemMainhand().getItemDamage() == 2) {
                ((UsableMetaItem) player.getHeldItemMainhand().getItem()).setDataStored(player.getHeldItemMainhand(), base.writeMemoryCardNBT());
            } else if (!player.isSneaking() && base != null && player.getHeldItemMainhand() != ItemStackTools.getEmptyStack() &&
                    player.getHeldItemMainhand().getItem() instanceof UsableMetaItem && player.getHeldItemMainhand().getItemDamage() == 2 &&
                    ((UsableMetaItem) player.getHeldItemMainhand().getItem()).hasDataStored(player.getHeldItemMainhand())) {
                base.readMemoryCardNBT(((UsableMetaItem) player.getHeldItemMainhand().getItem()).getDataStored(player.getHeldItemMainhand()));
            } else if (!player.isSneaking() && base != null) {
                if (PlayerUtil.isPlayerOwner(player, base)) {
                    world.notifyBlockUpdate(pos, state, state, 6);
                    player.openGui(OpenModularLighting.instance, base.getTier(), world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                }
            }
        }
        return true;
    }

    @Override
    protected void clOnNeighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        if (!worldIn.isRemote) {
            LightingBase base = (LightingBase) worldIn.getTileEntity(pos);
            if (base != null && worldIn.isBlockIndirectlyGettingPowered(pos) > 0) {
                base.setRedstone(true);
            } else if (base != null && worldIn.isBlockIndirectlyGettingPowered(pos) == 0) {
                base.setRedstone(false);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack
            stack) {

        if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof LightingBase) {
            EntityPlayerMP player = (EntityPlayerMP) placer;
            LightingBase base = (LightingBase) worldIn.getTileEntity(pos);
            if (base == null) {
                return;
            }
            base.setCamoState(state);
            base.setOwner(player.getUniqueID().toString());
            if (worldIn.isBlockIndirectlyGettingPowered(pos) > 0) {
                base.setRedstone(true);
            } else if (worldIn.isBlockIndirectlyGettingPowered(pos) == 0) {
                base.setRedstone(false);
            }
            base.markDirty();
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            dropItems(worldIn, pos);
            worldIn.removeTileEntity(pos);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return getResistanceFromBlockState(blockState);
    }

    protected float getResistanceFromBlockState(IBlockState blockState) {
        switch (blockState.getValue(TIER)) {
            case 1:
                return 10F;
            case 2:
                return 20F;
            case 3:
                return 30F;
            case 4:
                return 40F;
            case 5:
                return 50F;
            default:
                return 10F;
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return getResistanceFromBlockState(world.getBlockState(pos));
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(0, new ItemStack(ModBlocks.lightingBase, 1, this.getMetaFromState(state)));
        return drops;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TIER) - 1;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer
            player) {
        return new ItemStack(ModBlocks.lightingBase, 1, state.getValue(TIER) - 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public void clGetSubBlocks(Item item, CreativeTabs tab, List subItems) {
        for (int i = 0; i < 5; i++) {
            subItems.add(new ItemStack(ModBlocks.lightingBase, 1, i));
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if (te != null && te instanceof LightingBase) {
            LightingBase base = (LightingBase) te;
            EnumMachineMode machineMode = base.getMode();
            boolean active = base.isActive();
            probeInfo.text("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.MODE) + ": \u00A7A" + getMachineModeLocalization(machineMode));
            probeInfo.text("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.ACTIVE) + ": " + getColoredBooleanLocalizationYesNo(active));
            String ownerName = base.getOwnerName();
            probeInfo.text("\u00A76" + safeLocalize(OMLibNames.Localizations.GUI.OWNER) + ": \u00A7F" + ownerName);
        }
    }
}
