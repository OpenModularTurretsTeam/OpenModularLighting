package omtteam.openmodularlighting.compat.hwyla;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Content of the block info overlay for lights (1.7.10 {@code WailaTileHandler}): invert and mode. The server sends
 * the values ({@link #write}), the client turns them into lines ({@link #lines}). Kept free of Hwyla classes, so it
 * also loads (and can be tested) without Hwyla.
 */
public final class FloodlightTooltip {
    private static final String INVERTED = "inverted";
    private static final String MODE = "mode_key";

    private FloodlightTooltip() {
    }

    /** Server side: the values the overlay shows (block entity data is not synced to clients). */
    public static void write(NBTTagCompound tag, FloodlightTileEntity light) {
        tag.setBoolean(INVERTED, light.isInverted());
        String mode = light.modeKey();
        if (mode != null) {
            tag.setString(MODE, mode);
        }
    }

    /** Client side: "Inverted: …" and, for lights with a mode, "Mode: …". Empty if the server sent nothing. */
    public static List<ITextComponent> lines(NBTTagCompound tag) {
        List<ITextComponent> lines = new ArrayList<>();
        if (!tag.hasKey(INVERTED)) return lines;
        lines.add(new TextComponentTranslation("gui.openmodularlighting.invert")
                .appendSibling(new TextComponentString(": "))
                .appendSibling(new TextComponentTranslation(tag.getBoolean(INVERTED)
                        ? "gui.openmodularlighting.true" : "gui.openmodularlighting.false")));
        if (tag.hasKey(MODE)) {
            lines.add(new TextComponentTranslation("gui.openmodularlighting.mode")
                    .appendSibling(new TextComponentString(": "))
                    .appendSibling(new TextComponentTranslation(tag.getString(MODE))));
        }
        return lines;
    }
}
