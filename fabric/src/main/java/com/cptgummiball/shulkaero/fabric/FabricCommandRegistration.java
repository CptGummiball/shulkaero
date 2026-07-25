package com.cptgummiball.shulkaero.fabric;

import com.cptgummiball.shulkaero.ShulkaeroCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * Kept separate from the entrypoint so this class (and with it the Fabric API
 * command classes) is only loaded when Fabric API is actually installed.
 */
final class FabricCommandRegistration {

    private FabricCommandRegistration() {
    }

    static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ShulkaeroCommands.<FabricClientCommandSource>build(
                        FabricClientCommandSource::sendFeedback)));
    }
}
