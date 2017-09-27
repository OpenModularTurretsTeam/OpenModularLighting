package omtteam.openmodularlighting.tileentity.lights;


import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.util.EnumLightMode;
import omtteam.openmodularlighting.util.LightPlacingUtil;

public class Floodlight extends AbstractLightSource implements ITickable {
    protected LightingBase base;
    protected int range;
    protected boolean turnedOn;
    protected boolean active;
    protected EnumLightMode mode;
    protected int tier;

    public Floodlight() {
        super();
        this.orientation = EnumFacing.NORTH;
    }

    public Floodlight(int tier) {
        super();
        this.tier = tier;
        setSide();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean("active", active);
        nbtTagCompound.setBoolean("turnedOn", turnedOn);
        nbtTagCompound.setInteger("tier", tier);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.tier = nbtTagCompound.getInteger("tier");
        this.active = nbtTagCompound.getBoolean("active");
        this.turnedOn = nbtTagCompound.getBoolean("turnedOn");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTier() {
        return tier;
    }

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public EnumLightMode getMode() {
        return mode;
    }

    public void setMode(EnumLightMode mode) {
        this.mode = mode;
    }

    public int getCurrentRange() {
        return range;
    }

    public void setCurrentRange(int range) {
        if (range <= this.getMaxRange() && range > 0) {
            this.range = range;
        }
    }

    public int getMaxRange() {
        return tier < 3 ? 8 + tier * 8 : tier < 5 ? 16 + tier * 8 : 24 + tier * 8;
    }

    @Override
    protected void drainEnergy(int ticks) {
        this.getBase().removeEnergy(this.getEnergyUsage() * ticks);
    }

    @Override
    protected int getEnergyUsage() {
        int energyUsage = ConfigHandler.getFloodlightSettings().getEnergyUsage(this.tier);
        energyUsage = this.mode == EnumLightMode.CONE_NARROW ||
                this.mode == EnumLightMode.CONE_WIDE ? energyUsage * 4 : energyUsage;
        return energyUsage;
    }

    @Override
    protected boolean energySufficient(int ticks) {
        return this.getBase().getEnergyLevel(EnumFacing.DOWN) >= getEnergyUsage() * ticks;
    }

    private void removeLights() {
        if (!this.getWorld().isRemote) {
            switch (mode) {
                case STRAIGHT:
                    LightPlacingUtil.straightSource(this.getWorld(), this, true);
            }
        }
    }

    private void placeLights() {
        if (!this.getWorld().isRemote) {
            switch (mode) {
                case STRAIGHT:
                    LightPlacingUtil.straightSource(this.getWorld(), this, false);
            }
        }
    }

    private void updateLights() {
        if (active && !turnedOn) { // turning on
            switch (mode) {
                case STRAIGHT:
                    LightPlacingUtil.straightSource(this.getWorld(), this, false);
            }
            turnedOn = true;
        } else if (!active && turnedOn) { // turning off
            switch (mode) {
                case STRAIGHT:
                    LightPlacingUtil.straightSource(this.getWorld(), this, true);
            }
            turnedOn = false;
        }
    }

    @Override
    public void update() {
        if (this.getWorld().getTotalWorldTime() % 15 == 0 && base == null || dropBlock) {
            this.getWorld().destroyBlock(this.pos, true);
        }
        if (!this.getWorld().isRemote && this.getWorld().getTotalWorldTime() % 5 == 1) {
            if (active && energySufficient(5)) {
                drainEnergy(5);
            } else if (active) {
                active = false;
            } else {
                return;
            }
            updateLights();
        }
    }

    public void toggleMode() {
        if (this.tier > 3) {
            if (active) {
                removeLights();
            }
            if (mode.ordinal() < EnumLightMode.values().length - 1) {
                mode = EnumLightMode.values()[mode.ordinal() + 1];
            } else {
                mode = EnumLightMode.values()[0];
            }
            if (active) {
                placeLights();
            }
        }

    }
}
