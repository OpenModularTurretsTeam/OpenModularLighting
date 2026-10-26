package omtteam.openmodularlighting.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side helpers shared by the C2S packet handlers.
 */
public final class PacketValidation {
    private PacketValidation() {
    }

    /** Tells the player that the packet was refused for lack of permission. */
    public static void sendNoPermission(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.openmodularlighting.no_permission"));
    }
}
