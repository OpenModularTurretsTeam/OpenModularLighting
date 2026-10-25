package omtteam.openmodularlighting.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.Gson;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The round trip OMLib's config sync makes: send {@code toJson}, apply it, restore the local config's JSON. */
class OMLConfigSyncTest {
    @TempDir
    Path dir;

    @Test
    void appliesTheServerConfigAndRestoresTheLocalOne() {
        OMLConfig.init(dir);
        ConfigData local = OMLConfig.get();
        String localJson = OMLConfig.toJson();

        ConfigData server = new ConfigData();
        server.general.energyUsage = local.general.energyUsage + 5;
        OMLConfig.applyServerConfig(new Gson().toJson(server));
        assertNotSame(local, OMLConfig.get());
        assertEquals(local.general.energyUsage + 5, OMLConfig.get().general.energyUsage);

        OMLConfig.applyServerConfig(localJson);
        assertSame(local, OMLConfig.get());
    }

    @Test
    void keepsTheConfigInEffectOnInvalidJson() {
        OMLConfig.init(dir);
        ConfigData before = OMLConfig.get();
        OMLConfig.applyServerConfig("{not json");
        assertSame(before, OMLConfig.get());
    }

    @Test
    void validatesTheServerConfig() {
        OMLConfig.init(dir);
        ConfigData server = new ConfigData();
        server.general.energyUsage = 5000;
        OMLConfig.applyServerConfig(new Gson().toJson(server));
        assertEquals(1000, OMLConfig.get().general.energyUsage);
    }
}
