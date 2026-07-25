package com.cptgummiball.shulkaero.mixin;

import com.cptgummiball.shulkaero.PlacementHook;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {

    @Inject(method = "place", at = @At("HEAD"))
    private void shulkaero$beforePlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        PlacementHook.beforePlace(context);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void shulkaero$afterPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        PlacementHook.afterPlace(context, cir.getReturnValue());
    }
}
