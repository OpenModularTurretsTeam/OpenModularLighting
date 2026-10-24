package omtteam.openmodularlighting.energy;

import net.minecraftforge.energy.IEnergyStorage;
import omtteam.openmodularlighting.core.energy.EnergyAccess;

/**
 * An {@link EnergyAccess} as Forge Energy (1 E = 1 FE) for cables and other mods. Forge Energy counts in {@code int},
 * so amounts are clamped to {@link Integer#MAX_VALUE}. Lights never give energy away, so nothing can be extracted.
 */
public final class ForgeEnergyStorage implements IEnergyStorage {
    private final EnergyAccess access;

    public ForgeEnergyStorage(EnergyAccess access) {
        this.access = access;
    }

    /** The wrapped access, so a caller can tell whether a cached adapter still fits. */
    public EnergyAccess access() {
        return access;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) access.insert(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return clamp(access.getAmount());
    }

    @Override
    public int getMaxEnergyStored() {
        return clamp(access.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private static int clamp(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }
}
