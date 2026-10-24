package omtteam.openmodularlighting;

import omtteam.openmodularlighting.menu.FloodlightMenu;

import javax.annotation.Nullable;

/**
 * Side-specific start-up; the dedicated server uses this class as it is, the client {@code client.ClientProxy}. Main
 * code never references client classes directly.
 */
public class CommonProxy {
    /** Called at the end of pre-initialization. */
    public void preInit() {
    }

    /** Called at the end of initialization. */
    public void init() {
    }

    /** The client screen of a menu (a {@code GuiScreen}); null on a dedicated server. */
    @Nullable
    public Object createScreen(FloodlightMenu menu) {
        return null;
    }
}
