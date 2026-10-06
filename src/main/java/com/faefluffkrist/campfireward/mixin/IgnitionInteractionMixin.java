package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.IgnitionGuard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reject before item code executes, so stacks, durability, and custom use animations are untouched. */
@Mixin(value = ServerPlayerGameMode.class, priority = 1200)
public abstract class IgnitionInteractionMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void ward$blockTargetedUse(ServerPlayer player, Level world, ItemStack stack,
            InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof BlockItem)
                && world instanceof ServerLevel level && IgnitionGuard.blocked(level, hit.getBlockPos())) {
            com.faefluffkrist.campfireward.FireRestrictions.warn(player, level, hit.getBlockPos());
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void ward$blockRaycastUse(ServerPlayer player, Level world, ItemStack stack,
            InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof BlockItem) && world instanceof ServerLevel level
                && player.pick(player.blockInteractionRange(), 0, false) instanceof BlockHitResult hit
                && IgnitionGuard.blocked(level, hit.getBlockPos())) {
            com.faefluffkrist.campfireward.FireRestrictions.warn(player, level, hit.getBlockPos());
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
