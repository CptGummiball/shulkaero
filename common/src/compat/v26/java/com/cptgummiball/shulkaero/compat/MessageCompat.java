package com.cptgummiball.shulkaero.compat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Message delivery for MC 26.x, where displayClientMessage was replaced by
 * sendSystemMessage / sendOverlayMessage.
 */
public final class MessageCompat {

    private MessageCompat() {
    }

    public static void send(LocalPlayer player, Component message, boolean actionBar) {
        if (actionBar) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
    }
}
