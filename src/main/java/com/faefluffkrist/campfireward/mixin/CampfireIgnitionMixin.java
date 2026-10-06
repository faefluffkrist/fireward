package com.faefluffkrist.campfireward.mixin;
import com.faefluffkrist.campfireward.FireRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Catch projectile and modded ignition paths that bypass player item interactions. */
@Mixin(Level.class)
public abstract class CampfireIgnitionMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at=@At("HEAD"), cancellable=true)
    private void fireward$rejectIgnition(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof ServerLevel level) || !(state.getBlock() instanceof CampfireBlock)
                || !state.getValue(CampfireBlock.LIT)) return;
        var previous = level.getBlockState(pos);
        if (previous.getBlock() instanceof CampfireBlock && !previous.getValue(CampfireBlock.LIT)
                && FireRestrictions.reason(level,pos) != null) {
            FireRestrictions.coldFeedback(level,pos);
            cir.setReturnValue(false);
        }
    }
}
