package omtteam.openmodularlighting.compatibility.hwyla;

import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Created by Keridos on 25/11/16.
 * This Class
 */
@SuppressWarnings("unused")
public class WailaDataProvider {
    public static void register(IWailaRegistrar register) {
        WailaLightingBaseHandler.callbackRegister(register);
        WailaFloodlightHandler.callbackRegister(register);
    }
}
