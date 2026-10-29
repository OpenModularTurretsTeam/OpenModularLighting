package omtteam.openmodularlighting.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import omtteam.omlib.api.render.camo.DisguiseRules;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.inventory.AutomationItemHandler;
import omtteam.omlib.power.EnergyAccessStorage;
import omtteam.omlib.tileentity.TileEntitySynced;
import omtteam.omlib.util.camo.CamoRules;
import omtteam.omlib.util.camo.CamoSettings;
import omtteam.omlib.util.world.BlockUtil;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.light.LightType;
import omtteam.openmodularlighting.core.state.LightController;
import omtteam.openmodularlighting.core.state.RecheckTimer;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.world.LightBlocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Base of every light (1.7.10 {@code TileEntityMetaFloodlight}): invert, colour, one item slot with its
 * {@link LightPower}, and the cached beam of light blocks (CLAUDE.md §6.3, §6.4). Subclasses define the beam shape and
 * may add a mode.
 * <p>
 * Lights whose block {@link FloodlightBlock#supportsCamo() supports camouflage} are OMLib camouflage hosts: the disguise
 * is set in OMLib's camouflage menu, by anyone who may open the light's GUI; light value and opacity stay the light's.
 */
public abstract class FloodlightTileEntity extends TileEntitySynced
        implements LightSourceTileEntity, ITickable, ICamoSupport {
    /** 1.7.10 colour value for "not dyed". Other values are {@code EnumDyeColor} metadata. */
    public static final int NO_COLOR = 16;

    private static final String INVERTED = "inverted";
    private static final String ON = "on";
    private static final String TIMEOUT = "timeout";
    private static final String COLOR = "color";
    private static final String INVENTORY = "inventory";
    /** Key of the disguise up to 1.0.0-beta.6 (a block state compound); read, no longer written. */
    private static final String LEGACY_CAMO = "camo";
    /** OMLib's camouflage rules without see-through blocks, with OML's texts (as on 1.20.1+). */
    private static final DisguiseRules CAMO_RULES = new DisguiseRules() {
        @Override
        public boolean isValidDisguise(IBlockState state) {
            return CamoRules.isValidDisguise(state, false);
        }

        @Override
        public ITextComponent invalidMessage() {
            return new TextComponentTranslation("message.openmodularlighting.camo_invalid");
        }

        @Override
        public String slotHint() {
            return "tooltip.openmodularlighting.camouflage_slot";
        }
    };

    private final LightController controller = new LightController();
    /** Periodic re-cast (every ~20 s) for world changes that sent no block update. */
    private final RecheckTimer recheck = new RecheckTimer();
    private int color = NO_COLOR;
    /** The disguise ({@code null}: the light's own look); only the block state is used. */
    private final CamoSettings camoSettings = new CamoSettings();
    protected final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isValidItem(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };
    /** Hoppers and pipes may insert valid items only and cannot extract (1.7.10). */
    private final IItemHandler itemAccess = new AutomationItemHandler(inventory, stack -> false);
    /** Forge Energy view of the last energy access handed out (the access objects do not change). */
    @Nullable
    private EnergyAccessStorage energyAdapter;

    /** Light block positions of the current beam, in casting order. */
    private final LightBeam beam = new LightBeam(this);

    /** What this light runs on. */
    protected abstract LightPower power();

    /** The beam shape for the current mode and config (beam space, see {@code BeamShape}). */
    protected abstract BeamShape currentShape();

    /** Everything {@link #currentShape()} depends on (mode, ranges): a different key means re-cast. */
    protected abstract int[] shapeKey();

    protected LightType lightType() {
        return LightType.PHANTOM;
    }

    protected LightBlock lightBlock() {
        return ModBlocks.PHANTOM_LIGHT;
    }

    /** Ticks the light stays off after turning off. */
    protected int timeoutTicks() {
        return OMLConfig.get().general.timeoutFloodlights;
    }

    /** Called every server tick after the light logic (grow light: growth ticks). */
    protected void afterTick(World world, BlockPos pos, IBlockState state, LightController.TickResult result) {
    }

    public boolean isValidItem(ItemStack stack) {
        return power().isValidItem(stack);
    }

    // ---- Ticking ----

    /** Called every server tick before the power is prepared (panel lights: cluster upkeep and energy sharing). */
    protected void beforePower(World world) {
    }

    @Override
    public void update() {
        if (world != null && !world.isRemote) {
            serverTick(world, pos, world.getBlockState(pos));
        }
    }

    private void serverTick(World world, BlockPos pos, IBlockState state) {
        if (!(state.getBlock() instanceof FloodlightBlock)) return;
        beforePower(world);
        power().prepare(inventory);
        if (beam.needsRecast()) {
            controller.requestRecast(); // placed, loaded without cache, mode or config changed
        }
        if (recheck.tick(world.rand)) {
            controller.requestRecast();
        }
        LightController.TickResult result = controller.tick(world.isBlockPowered(pos), power(), timeoutTicks());
        if (result.recast()) {
            beam.recast(world);
        }
        if (result.change() != LightController.Switch.NONE) {
            beam.switchLights(world, result.change() == LightController.Switch.ON);
        }
        boolean on = controller.isOn();
        if (state.getValue(FloodlightBlock.LIT) != on) {
            // no neighbour updates: they would make the light block in front request a re-cast
            world.setBlockState(pos, state.withProperty(FloodlightBlock.LIT, on), LightBlocks.UPDATE_CLIENTS);
        }
        afterTick(world, pos, state, result);
        if (result.running() || result.recast() || result.change() != LightController.Switch.NONE) {
            markDirty();
        }
    }

    /** The mode changed: re-cast at once (1.7.10 {@code changeMode}). */
    protected void shapeChanged() {
        if (world != null && !world.isRemote) {
            beam.recast(world);
        }
        markDirty();
    }

    /** The block was removed: leave all light blocks (removing those this light was the last source of). */
    public void removeLights(World world) {
        beam.removeLights(world);
    }

    public EnumFacing facing() {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof FloodlightBlock ? state.getValue(FloodlightBlock.FACING) : EnumFacing.SOUTH;
    }

    // ---- Settings ----

    /** Translation key of the current mode, or null if this light has no mode. */
    @Nullable
    public String modeKey() {
        return null;
    }

    public boolean isInverted() {
        return controller.isInverted();
    }

    public void toggleInverted() {
        controller.toggleInverted();
        markDirty();
    }

    /**
     * Ticks until the light turns on, if it would turn on now but still waits for the timeout after turning off;
     * 0 otherwise.
     */
    public int ticksUntilOn(World world) {
        boolean active = world.isBlockPowered(pos) ^ controller.isInverted();
        return active && !controller.isOn() && power().canRun() ? controller.timeout() : 0;
    }

    /** Ticks as seconds for messages: "2" for 40 ticks, "1.5" for 30. */
    public static String seconds(int ticks) {
        return ticks % 20 == 0 ? String.valueOf(ticks / 20) : String.format(Locale.ROOT, "%.1f", ticks / 20.0);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color == color) return;
        this.color = color;
        syncToClient();
    }

    /** Whether this light can be disguised (the electric, carbon and UV floodlights). */
    public boolean supportsCamo() {
        if (world == null) return false;
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof FloodlightBlock && ((FloodlightBlock) state.getBlock()).supportsCamo();
    }

    /** The disguise, or {@code null} for the light's own look. */
    @Nullable
    public IBlockState getCamo() {
        return camoSettings.getCamoBlockState();
    }

    // ---- ICamoSupport (OMLib's camouflage menu) ----

    @Override
    public IBlockState getCamoState() {
        IBlockState camo = getCamo();
        return camo != null ? camo : getDefaultCamoState();
    }

    /**
     * Sets the disguise; the light's own block (the menu's reset) removes it. On the server the client copies get it
     * through the update packet, on the client the block is redrawn.
     */
    @Override
    public void setCamoState(IBlockState state) {
        IBlockState camo = state.getBlock() == getDefaultCamoState().getBlock() ? null : state;
        if (!supportsCamo() || camo == getCamo()) return;
        camoSettings.setCamoBlockState(camo);
        syncToClient();
        remesh();
    }

    @Override
    public IBlockState getDefaultCamoState() {
        return world == null ? Blocks.AIR.getDefaultState() : world.getBlockState(pos).getBlock().getDefaultState();
    }

    @Override
    public CamoSettings getCamoSettings() {
        return camoSettings;
    }

    @Override
    public TileEntity getCamoTileEntity() {
        return this;
    }

    @Override
    public boolean isCamoEnabled() {
        return supportsCamo();
    }

    /** The light value is the light's own (on/off); no sliders. */
    @Override
    public boolean supportsLight() {
        return false;
    }

    /** Lights have no owner: whoever may open the light's GUI may disguise it (as with the former disguise slot). */
    @Override
    public boolean canSetCamo(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean canChangeLight(EntityPlayer player) {
        return false;
    }

    /** Only full, opaque blocks, so a disguised light shows no holes into the world; with OML's own texts. */
    @Override
    public DisguiseRules getDisguiseRules() {
        return CAMO_RULES;
    }

    /** The camouflage menu is opened from the light's GUI, which its Back button returns to. */
    @Override
    public boolean hasParentMenu() {
        return true;
    }

    @Override
    public void openParentMenu(EntityPlayerMP player) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof FloodlightBlock) {
            ((FloodlightBlock) block).openGui(player, this);
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /** The electric power of this light, or null if it burns fuel. */
    @Nullable
    public ElectricPower electricPower() {
        return power() instanceof ElectricPower ? (ElectricPower) power() : null;
    }

    /** Whether energy may be inserted from this side (null: unknown side); all sides by default (1.7.10). */
    protected boolean acceptsEnergyFrom(@Nullable EnumFacing side) {
        return true;
    }

    /** Energy exposed to other blocks on {@code side} (wrapped as Forge Energy), or null. */
    @Nullable
    public EnergyAccess getEnergyAccess(@Nullable EnumFacing side) {
        ElectricPower electric = electricPower();
        return electric != null && acceptsEnergyFrom(side) ? electric.store() : null;
    }

    /** The light block positions of the current beam. */
    public List<BlockPos> getShape() {
        return beam.positions();
    }

    // ---- Capabilities (items and Forge Energy) ----

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        if (capability == CapabilityEnergy.ENERGY) return getEnergyAccess(side) != null;
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemAccess);
        }
        if (capability == CapabilityEnergy.ENERGY) {
            EnergyAccess access = getEnergyAccess(side);
            if (access == null) return null;
            if (energyAdapter == null || energyAdapter.access() != access) {
                energyAdapter = new EnergyAccessStorage(access, false); // lights never give energy away
            }
            return CapabilityEnergy.ENERGY.cast(energyAdapter);
        }
        return super.getCapability(capability, side);
    }

    // ---- LightSourceTileEntity ----

    @Override
    public boolean isLightOn() {
        return controller.isOn();
    }

    @Override
    public boolean coversLightAt(BlockPos pos) {
        return beam.covers(pos);
    }

    @Override
    public void requestRecast() {
        controller.requestRecast();
    }

    // ---- Saving ----

    /** Whether the dropped item keeps this light's settings ("Configured" tooltip). */
    public boolean hasCustomSettings() {
        return controller.isInverted() || color != NO_COLOR || getCamo() != null || power().hasStored();
    }

    /**
     * Settings kept on the dropped item: invert, colour, disguise, mode and energy / burn time. Not the beam or
     * on/off state, and not the slot contents, which are dropped separately.
     */
    public NBTTagCompound saveItemData() {
        NBTTagCompound tag = new NBTTagCompound();
        writeSettings(tag);
        return tag;
    }

    /** Settings that are also kept on the dropped item; subclasses add their mode. */
    protected void writeSettings(NBTTagCompound tag) {
        tag.setBoolean(INVERTED, controller.isInverted());
        tag.setInteger(COLOR, color);
        writeCamo(tag);
        power().write(tag);
    }

    /** Reads what {@link #writeSettings} wrote. */
    protected void readSettings(NBTTagCompound tag) {
        color = tag.hasKey(COLOR) ? tag.getInteger(COLOR) : NO_COLOR;
        camoSettings.setCamoBlockState(readCamo(tag));
        power().read(tag);
    }

    /** The disguise in OMLib's format ({@code camoBlockRegName}, {@code camoBlockMeta}), if there is one. */
    private void writeCamo(NBTTagCompound tag) {
        IBlockState camo = getCamo();
        if (camo != null) {
            BlockUtil.writeBlockFromStateToNBT(tag, camo);
        }
    }

    /**
     * Reads what {@link #writeCamo} wrote, or the {@link #LEGACY_CAMO} compound of 1.0.0-beta.6 and older; no disguise
     * if there is none or it is no longer valid (the block was removed, or the rules changed).
     */
    @Nullable
    private static IBlockState readCamo(NBTTagCompound tag) {
        IBlockState state = BlockUtil.getBlockStateFromNBT(tag);
        if (state == null && tag.hasKey(LEGACY_CAMO, Constants.NBT.TAG_COMPOUND)) {
            state = NBTUtil.readBlockState(tag.getCompoundTag(LEGACY_CAMO));
        }
        return state != null && CAMO_RULES.isValidDisguise(state) ? state : null;
    }

    @Override
    protected void writeData(NBTTagCompound tag) {
        writeSettings(tag);
        tag.setBoolean(ON, controller.isOn());
        tag.setInteger(TIMEOUT, controller.timeout());
        tag.setTag(INVENTORY, inventory.serializeNBT());
        beam.write(tag);
    }

    @Override
    protected void readData(NBTTagCompound tag) {
        int oldColor = color;
        IBlockState oldCamo = getCamo();
        controller.load(tag.getBoolean(INVERTED), tag.getBoolean(ON), tag.getInteger(TIMEOUT));
        readSettings(tag);
        if (tag.hasKey(INVENTORY)) {
            inventory.deserializeNBT(tag.getCompoundTag(INVENTORY));
        }
        beam.read(tag);
        if (oldColor != color || oldCamo != getCamo()) {
            remesh(); // tint and disguise come from the block entity (client only)
        }
    }

    /** Clients only need the colour and the disguise (the on/off state is the {@code LIT} blockstate). */
    @Override
    protected void writeClientData(NBTTagCompound tag) {
        tag.setInteger(COLOR, color);
        writeCamo(tag);
    }
}
