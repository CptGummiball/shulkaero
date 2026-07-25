package com.cptgummiball.shulkaero.fabric;

import com.cptgummiball.shulkaero.Shulkaero;
import net.fabricmc.api.ClientModInitializer;

public final class ShulkaeroFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Shulkaero.init("Fabric");
        // The /shulkaero client command needs Fabric API's command module, which
        // is an optional dependency - everything else works without it.
        if (classExists("net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback")) {
            FabricCommandRegistration.register();
        } else {
            Shulkaero.LOGGER.info("Fabric API not found - /shulkaero command disabled (everything else works)");
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, ShulkaeroFabric.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
