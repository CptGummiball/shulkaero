package com.cptgummiball.shulkaero.compat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Message delivery for MC 1.21.x (pre-26 API):
 * displayClientMessage(message, actionBar).
 */
public final class MessageCompat {

    private MessageCompat() {
    }

    public static void send(LocalPlayer player, Component message, boolean actionBar) {
        player.displayClientMessage(message, actionBar);
    }
}
