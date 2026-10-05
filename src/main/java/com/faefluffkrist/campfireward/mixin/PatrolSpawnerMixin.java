package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.WardIndex;
import com.faefluffkrist.campfireward.FirewardConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin {
    @Inject(method = "spawnPatrolMember", at = @At("HEAD"), cancellable = true)
    private void ward$reducePatrolChance(ServerLevel level, BlockPos pos, RandomSource random,
                                       boolean leader, CallbackInfoReturnable<Boolean> cir) {
        // Only veto an existing vanilla attempt. Never schedule or generate a new patrol.
        double reduction = 0;
        for (var fire : WardIndex.nearby(level,Vec3.atCenterOf(pos),FirewardConfig.active.maxPatrolRange(),true)) {
            var settings = fire.settings();
            if (settings.patrolSuppression && fire.center().distanceToSqr(Vec3.atCenterOf(pos)) <= settings.patrolRange * settings.patrolRange)
                reduction = Math.max(reduction,settings.patrolReductionPercent / 100);
        }
        if (reduction > 0 && random.nextDouble() < reduction) cir.setReturnValue(false);
    }
}
