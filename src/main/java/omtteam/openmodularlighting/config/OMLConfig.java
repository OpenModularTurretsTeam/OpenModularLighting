package omtteam.openmodularlighting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.network.ConfigSyncPacket;
import omtteam.openmodularlighting.network.OMLNetwork;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads, validates and saves {@code config/openmodularlighting.json}, and syncs it from the server to clients.
 * <p>
 * Missing keys are filled in with defaults, unknown keys are dropped and out-of-range values are clamped; the file
 * is rewritten whenever that happens. The file is JSON with {@code //} comments (read leniently): every key is
 * documented with its description, range and default, see {@link ConfigWriter}. The file is also rewritten when it
 * differs from what the mod would write, which refreshes the comments after an update; comments added by users are
 * not kept.
 */
public final class OMLConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FILE_NAME = OML.MOD_ID + ".json";

    /** Config loaded from this instance's own file. */
    private static ConfigData local = new ConfigData();
    /** Config in effect: the server's while connected to a remote server, otherwise {@link #local}. */
    private static volatile ConfigData active = local;
    private static Path configDir;

    private OMLConfig() {
    }

    /**
     * The config currently in effect. Do not modify the returned object.
     */
    public static ConfigData get() {
        return active;
    }

    /**
     * Loads the config file from Forge's config folder (writing defaults if it is missing).
     */
    public static void init(Path dir) {
        configDir = dir;
        load();
    }

    /**
     * Sends the server's config to a joining player.
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        MinecraftServer server = player.mcServer;
        // The singleplayer host already uses this config
        if (server != null && (server.isDedicatedServer() || !player.getName().equals(server.getServerOwner()))) {
            OMLNetwork.CHANNEL.sendTo(new ConfigSyncPacket(GSON.toJson(local)), player);
        }
    }

    public static void load() {
        Path path = configDir.resolve(FILE_NAME);
        ConfigData data;
        boolean changed;
        if (Files.exists(path)) {
            JsonElement json;
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                json = new JsonParser().parse(reader);
            } catch (Exception e) {
                OML.LOGGER.error("Could not read {}, using defaults (the file is left untouched)", path, e);
                local = active = new ConfigData();
                return;
            }
            ParseResult result = parse(json);
            data = result.data;
            changed = result.changed || !commented(data).equals(readText(path));
        } else {
            data = new ConfigData();
            changed = true;
        }
        if (changed) {
            try {
                Files.createDirectories(path.getParent());
                Files.write(path, commented(data).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                OML.LOGGER.error("Could not write {}", path, e);
            }
        }
        local = active = data;
    }

    /** The file contents for {@code data}: JSON with the comments from {@link Comment} and {@link Range}. */
    private static String commented(ConfigData data) {
        return ConfigWriter.write(data, new ConfigData());
    }

    private static String readText(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Client side: use the config received from the server.
     */
    public static void applyServerConfig(String json) {
        try {
            active = parse(new JsonParser().parse(json)).data;
        } catch (Exception e) {
            OML.LOGGER.error("Received an invalid config from the server, keeping the local one", e);
        }
    }

    /**
     * Client side: go back to the local config, e.g. after disconnecting.
     */
    public static void useLocalConfig() {
        active = local;
    }

    private static final class ParseResult {
        final ConfigData data;
        final boolean changed;

        ParseResult(ConfigData data, boolean changed) {
            this.data = data;
            this.changed = changed;
        }
    }

    private static ParseResult parse(JsonElement json) {
        JsonObject defaults = GSON.toJsonTree(new ConfigData()).getAsJsonObject();
        boolean[] changed = {false};
        // before merging: a file without a version gets the current one from the defaults
        if (json.isJsonObject()) changed[0] = ConfigMigration.migrate(json.getAsJsonObject());
        JsonElement merged = json.isJsonObject() ? merge(defaults, json.getAsJsonObject(), "", changed) : defaults;
        if (!json.isJsonObject()) {
            OML.LOGGER.warn("Config: root is not a JSON object, using defaults");
            changed[0] = true;
        }
        ConfigData data = GSON.fromJson(merged, ConfigData.class);
        ConfigValidator validator = new ConfigValidator();
        data.validate(validator);
        return new ParseResult(data, changed[0] || validator.changed());
    }

    /**
     * Overlays {@code user} onto {@code defaults}: keeps user values whose JSON type matches the default,
     * falls back to the default otherwise and drops unknown keys.
     */
    private static JsonObject merge(JsonObject defaults, JsonObject user, String path, boolean[] changed) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            String key = entry.getKey();
            JsonElement def = entry.getValue();
            JsonElement value = user.get(key);
            if (value == null) {
                changed[0] = true;
                result.add(key, def);
            } else if (def.isJsonObject() && value.isJsonObject()) {
                result.add(key, merge(def.getAsJsonObject(), value.getAsJsonObject(), path + key + ".", changed));
            } else if (sameType(def, value)) {
                result.add(key, value);
            } else {
                OML.LOGGER.warn("Config: {}{} has the wrong type, using the default {}", path, key, def);
                changed[0] = true;
                result.add(key, def);
            }
        }
        for (Map.Entry<String, JsonElement> entry : user.entrySet()) {
            if (!defaults.has(entry.getKey())) {
                OML.LOGGER.warn("Config: removed unknown key {}{}", path, entry.getKey());
                changed[0] = true;
            }
        }
        return result;
    }

    private static boolean sameType(JsonElement a, JsonElement b) {
        if (a.isJsonArray()) return b.isJsonArray();
        if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
            JsonPrimitive pa = a.getAsJsonPrimitive();
            JsonPrimitive pb = b.getAsJsonPrimitive();
            return pa.isBoolean() == pb.isBoolean() && pa.isNumber() == pb.isNumber() && pa.isString() == pb.isString();
        }
        return false;
    }
}
