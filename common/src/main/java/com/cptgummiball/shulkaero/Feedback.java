package com.cptgummiball.shulkaero;

import com.cptgummiball.shulkaero.compat.MessageCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * User-facing messages (actionbar/chat/none per config), localized via the
 * mod's lang files. Message delivery goes through {@link MessageCompat},
 * which is selected per build target (the API changed in MC 26.x).
 */
public final class Feedback {

    private Feedback() {
    }

    public static void waypointAdded(String name) {
        send(Component.translatable("shulkaero.waypoint.added", name));
    }

    public static void boxDropped(String name) {
        send(Component.translatable("shulkaero.waypoint.dropped", name));
    }

    public static void waypointRemoved(String name) {
        send(Component.translatable("shulkaero.waypoint.removed", name));
    }

    private static void send(Component message) {
        ShulkaeroConfig config = ShulkaeroConfig.get();
        if (ShulkaeroConfig.FEEDBACK_NONE.equals(config.feedback)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        MessageCompat.send(player, message, ShulkaeroConfig.FEEDBACK_ACTIONBAR.equals(config.feedback));
    }
}
