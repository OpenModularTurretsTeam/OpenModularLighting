package omtteam.openmodularlighting.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.handler.OMLGuiHandler;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;
import omtteam.openmodularlighting.world.Worlds;

import javax.annotation.Nullable;

/**
 * Base of every light block (floodlights, UV floodlight, fluorescent, grow and cube lights). {@link #FACING} is the
 * direction the light shines (the front), {@link #LIT} is set while it is on. Interaction as in 1.7.10 (CLAUDE.md
 * §6.6, {@link FloodlightInteraction}); subclasses decide what a stick or wrench does ({@link #configure}).
 * <p>
 * Metadata: the facing index in bits 0-2, {@code LIT} in bit 3.
 */
public abstract class FloodlightBlock extends Block {
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    public static final PropertyBool LIT = PropertyBool.create("lit");
    /** NBT key of the block entity data on a dropped item; vanilla loads it into the block entity on placement. */
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    private static final int LIT_BIT = 8;
    private static final int FACING_BITS = 7;

    /** 1.7.10: rock (or metal for the panels), metal sound, hardness 2.5, stone pickaxe. */
    protected FloodlightBlock(Material material) {
        super(material);
        setHardness(2.5F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH).withProperty(LIT, false));
    }

    /** Creates this light's block entity. */
    protected abstract FloodlightTileEntity createLight();

    /**
     * Opens the GUI of this light: the shared electric GUI (energy, battery slot) for electric lights. Server side.
     */
    protected void openGui(EntityPlayer player, FloodlightTileEntity light) {
        if (light.electricPower() != null) {
            BlockPos pos = light.getPos();
            player.openGui(OML.instance, OMLGuiHandler.ELECTRIC, light.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * Right-click with a stick or wrench (not sneaking), on the server: cycle the mode or rotate the model. Only called
     * if {@link #hasConfiguration()}.
     */
    protected void configure(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                             FloodlightTileEntity light) {
    }

    /** Whether {@link #configure} does something (otherwise a stick or wrench opens the GUI). */
    protected boolean hasConfiguration() {
        return false;
    }

    /** Sends "Mode: …" to the player (1.7.10 chat message). */
    protected static void sendMode(EntityPlayer player, String modeKey) {
        player.sendStatusMessage(new TextComponentTranslation("gui.openmodularlighting.mode")
                .appendSibling(new TextComponentString(": "))
                .appendSibling(new TextComponentTranslation(modeKey)), false);
    }

    /** Lights that can be disguised also carry the disguise in their extended state, for the model. */
    @Override
    protected BlockStateContainer createBlockState() {
        if (this instanceof CamoBlock) {
            return new ExtendedBlockState(this, new IProperty<?>[]{FACING, LIT},
                    new IUnlistedProperty<?>[]{CamoBlock.CAMO});
        }
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) return state;
        TileEntity tile = Worlds.getTileEntity(world, pos);
        IBlockState camo = tile instanceof FloodlightTileEntity ? ((FloodlightTileEntity) tile).getCamo() : null;
        return ((IExtendedBlockState) state).withProperty(CamoBlock.CAMO, camo);
    }

    /**
     * The light itself is drawn in the cutout layer; a disguise needs the layers of full opaque blocks as well (the
     * model only returns quads for the layer being drawn).
     */
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        if (this instanceof CamoBlock) return layer != BlockRenderLayer.TRANSLUCENT;
        return layer == getRenderLayer();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & FACING_BITS))
                .withProperty(LIT, (meta & LIT_BIT) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(LIT) ? LIT_BIT : 0);
    }

    /** Faces the player, like a dispenser (1.7.10 {@code BlockFL.getFacing}). */
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    /** The lamp overlay of the floodlights has transparent pixels (CLAUDE.md §6.11). */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createLight();
    }

    /** Removed: leave the phantom lights and drop the slot contents. */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof FloodlightTileEntity) {
            FloodlightTileEntity light = (FloodlightTileEntity) tile;
            light.removeLights(world);
            ItemStack slot = light.getInventory().getStackInSlot(0);
            if (!slot.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), slot.copy());
                light.getInventory().setStackInSlot(0, ItemStack.EMPTY);
            }
        }
        super.breakBlock(world, pos, state);
    }

    /** Keeps the block (and its block entity) until {@link #harvestBlock} has read the drops. */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        if (willHarvest) return true;
        return super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        world.setBlockToAir(pos);
    }

    /** The dropped block keeps mode, invert, colour and energy / burn time (1.7.10 {@code BlockFL.getDrops}). */
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof FloodlightTileEntity && ((FloodlightTileEntity) tile).hasCustomSettings()) {
            for (ItemStack drop : drops) {
                if (drop.getItem() == Item.getItemFromBlock(this)) {
                    drop.setTagInfo(BLOCK_ENTITY_TAG, ((FloodlightTileEntity) tile).saveItemData());
                }
            }
        }
    }
}
