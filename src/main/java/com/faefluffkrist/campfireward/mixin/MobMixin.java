package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void ward$auras(CallbackInfo ci) {
        Mob mob = (Mob)(Object)this;
        if(mob.level() instanceof ServerLevel level)AuraController.tick(mob,level);
    }
    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Mob;customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V"))
    private void ward$pauseBrainWhileFleeing(Mob mob, ServerLevel level) {
        boolean fear = FirewardConfig.active.enabled && (MobRules.flees(mob,false) || MobRules.flees(mob,true)) && (CampfireGoal.isFleeing(mob) ||
            WardIndex.nearbyMob(level,mob,FirewardConfig.active.maxFearRange(),true).stream()
                .anyMatch(f -> MobRules.flees(mob,f.soul())
                    && mob.position().distanceToSqr(f.center()) <= Math.pow(f.settings().fearRange,2)));
        if (fear) {
            mob.setTarget(null);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return;
        }
        ((MobInvoker)mob).ward$customAi(level);
        if(mob instanceof net.minecraft.world.entity.npc.villager.Villager villager)VillagerShelter.tick(villager,level);
    }
}
