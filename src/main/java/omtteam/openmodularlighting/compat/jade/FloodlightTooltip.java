package omtteam.openmodularlighting.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.compat.nbt.NbtCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Content of the block info overlay for lights (1.7.10 {@code WailaTileHandler}): invert and mode. The server sends
 * the values ({@link #write}), the client turns them into lines ({@link #lines}). Kept free of Jade classes, so it
 * also works (and can be tested) without Jade.
 */
public final class FloodlightTooltip {
    private static final String INVERTED = "inverted";
    private static final String MODE = "mode_key";

    private FloodlightTooltip() {
    }

    /** Server side: the values the overlay shows (block entity data is not synced to clients). */
    public static void write(CompoundTag tag, FloodlightBlockEntity light) {
        tag.putBoolean(INVERTED, light.isInverted());
        String mode = light.modeKey();
        if (mode != null) {
            tag.putString(MODE, mode);
        }
    }

    /** Client side: "Inverted: …" and, for lights with a mode, "Mode: …". Empty if the server sent nothing. */
    public static List<Component> lines(CompoundTag tag) {
        List<Component> lines = new ArrayList<>();
        if (!tag.contains(INVERTED)) return lines;
        lines.add(Component.translatable("gui.openmodularlighting.invert").append(": ").append(Component.translatable(
                NbtCompat.getBoolean(tag, INVERTED, false) ? "gui.openmodularlighting.true" : "gui.openmodularlighting.false")));
        if (tag.contains(MODE)) {
            lines.add(Component.translatable("gui.openmodularlighting.mode").append(": ")
                    .append(Component.translatable(NbtCompat.getString(tag, MODE, ""))));
        }
        return lines;
    }
}
