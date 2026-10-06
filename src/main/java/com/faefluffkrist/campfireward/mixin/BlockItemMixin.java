package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Unique private boolean ward$isCampfireItem() {
        var block = ((BlockItem)(Object)this).getBlock();
        return block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE;
    }
    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void ward$rejectWatchedLitPlacement(BlockPlaceContext context, BlockState state,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!ward$isCampfireItem() || !FirewardConfig.active.enabled || !state.getValue(CampfireBlock.LIT)
                || context.getPlayer() == null || !(context.getLevel() instanceof ServerLevel level)) return;
        String reason = FireRestrictions.reason(level,context.getClickedPos());
        if (reason != null) {
            if (context.getPlayer() instanceof ServerPlayer player) FireRestrictions.warn(player,level,context.getClickedPos());
            if (level.dimension().equals(net.minecraft.world.level.Level.END)) {
                cir.setReturnValue(level.setBlock(context.getClickedPos(),state.setValue(CampfireBlock.LIT,false),11));
            } else cir.setReturnValue(false);
            return;
        }
        if (!FirewardConfig.active.blockLitPlacement || !FirewardConfig.active.fire(state.is(Blocks.SOUL_CAMPFIRE)).enabled) return;
        if (IgnitionGuard.watched(level, context.getClickedPos())) {
            if (context.getPlayer() instanceof ServerPlayer player) IgnitionGuard.warn(player);
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "place", at = @At("RETURN"))
    private void ward$enrollPlayerFire(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ward$isCampfireItem() || context.getPlayer() == null || !cir.getReturnValue().consumesAction()
                || !(context.getLevel() instanceof ServerLevel level)) return;
        if (level.getBlockEntity(context.getClickedPos()) instanceof CampfireBlockEntity be) {
            ((WardMarker)be).ward$markPlayerPlaced();
            WardIndex.add(level, context.getClickedPos());
        }
    }
}
