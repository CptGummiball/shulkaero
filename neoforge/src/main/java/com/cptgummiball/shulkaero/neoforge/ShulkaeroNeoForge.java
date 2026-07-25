package com.cptgummiball.shulkaero.neoforge;

import com.cptgummiball.shulkaero.Shulkaero;
import com.cptgummiball.shulkaero.ShulkaeroCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Shulkaero.MOD_ID, dist = Dist.CLIENT)
public final class ShulkaeroNeoForge {

    public ShulkaeroNeoForge() {
        Shulkaero.init("NeoForge");
        NeoForge.EVENT_BUS.addListener(ShulkaeroNeoForge::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(ShulkaeroCommands.build(
                (source, message) -> source.sendSystemMessage(message)));
    }
}
