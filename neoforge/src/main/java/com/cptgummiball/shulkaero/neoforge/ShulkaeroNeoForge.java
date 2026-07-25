package com.cptgummiball.shulkaero.neoforge;

import com.cptgummiball.shulkaero.Shulkaero;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Shulkaero.MOD_ID, dist = Dist.CLIENT)
public final class ShulkaeroNeoForge {

    public ShulkaeroNeoForge() {
        Shulkaero.init("NeoForge");
    }
}
