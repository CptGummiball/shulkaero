package com.cptgummiball.shulkaero;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-side placement detection, driven by the {@code BlockItem#place} mixin.
 * <p>
 * The item stack must be captured BEFORE placement: on success vanilla shrinks
 * the stack, and an empty stack no longer exposes its data components (i.e. the
 * custom name would be lost for the last box in the stack).
 */
public final class PlacementHook {

    private static ItemStack pendingStack = ItemStack.EMPTY;

    private PlacementHook() {
    }

    public static void beforePlace(BlockPlaceContext context) {
        pendingStack = ItemStack.EMPTY;
        Level level = context.getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }
        Player player = context.getPlayer();
        if (player == null || player != Minecraft.getInstance().player) {
            return;
        }
        ItemStack stack = context.getItemInHand();
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            pendingStack = stack.copy();
        }
    }

    public static void afterPlace(BlockPlaceContext context, InteractionResult result) {
        ItemStack captured = pendingStack;
        pendingStack = ItemStack.EMPTY;
        if (captured.isEmpty() || result == null || !result.consumesAction()) {
            return;
        }
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            ShulkerWaypointManager.onShulkerPlaceDetected(pos, captured);
        }
    }
}
