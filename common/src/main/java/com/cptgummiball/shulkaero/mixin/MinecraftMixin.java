package com.cptgummiball.shulkaero.mixin;

import com.cptgummiball.shulkaero.ShulkerWaypointManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void shulkaero$clientTick(CallbackInfo ci) {
        ShulkerWaypointManager.clientTick((Minecraft) (Object) this);
    }
}
