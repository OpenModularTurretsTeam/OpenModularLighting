package omtteam.openmodularlighting.util;

import net.minecraft.entity.EntityLivingBase;

import java.util.Set;

/**
 * Created by Keridos on 06/02/17.
 * This Class
 */
public class OMTUtil {

    public static int getFakeDropsLevel(EntityLivingBase entity) {
        Set<String> tags = entity.getTags();
        return (tags.contains("openmodularlighting:fake_drops_3") ? 3 : tags.contains("openmodularlighting:fake_drops_2") ? 2 :
                tags.contains("openmodularlighting:fake_drops_1") ? 1 : tags.contains("openmodularlighting:fake_drops_0") ? 0 : -1);
    }
}
