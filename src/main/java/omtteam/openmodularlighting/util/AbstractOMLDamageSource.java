package omtteam.openmodularlighting.util;

import net.minecraft.util.DamageSource;
import omtteam.openmodularlighting.tileentity.LightingBase;

/**
 * Created by Keridos on 28/07/17.
 * This Class
 */
public class AbstractOMLDamageSource extends DamageSource {
    private LightingBase base;

    public AbstractOMLDamageSource(String damageTypeIn, LightingBase base) {
        super(damageTypeIn);
        this.base = base;
    }

    public LightingBase getBase() {
        return this.base;
    }
}
