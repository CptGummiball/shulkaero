package com.cptgummiball.shulkaero.fabric;

import com.cptgummiball.shulkaero.Shulkaero;
import net.fabricmc.api.ClientModInitializer;

public final class ShulkaeroFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Shulkaero.init("Fabric");
    }
}
