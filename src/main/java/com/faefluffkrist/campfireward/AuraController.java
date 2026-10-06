package com.faefluffkrist.campfireward;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;

public final class AuraController {
    public static final Identifier FRACTIONAL_SLOWNESS=Identifier.fromNamespaceAndPath("fireward","fractional_slowness");
    private AuraController() {}
    public static void tick(Mob mob,ServerLevel level) {
        var config=FirewardConfig.active;
        double requestedSlowness=0;
        if(config.enabled && (MobRules.aura(mob,false,config) || MobRules.aura(mob,true,config))) {
            for(var fire:WardIndex.nearbyMob(level,mob,config.maxAuraRange(),true)) {
                if(!MobRules.aura(mob,fire.soul(),config))continue;
                double distance=mob.position().distanceToSqr(fire.center());
                for(var rule:fire.settings().effects()) {
                    if(!rule.enabled || distance>rule.range*rule.range)continue;
                    var id=Identifier.tryParse(rule.effectId);
                    if(id==null)continue;
                    var effect=BuiltInRegistries.MOB_EFFECT.get(id);
                    if(effect.isEmpty())continue;
                    mob.addEffect(new MobEffectInstance(effect.get(),2,(int)Math.floor(rule.level)-1,true,rule.showParticles,rule.showIcon));
                    if(effect.get().equals(MobEffects.SLOWNESS))requestedSlowness=Math.max(requestedSlowness,rule.level);
                }
            }
        }
        var speed=mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed==null)return;
        var slowness=mob.getEffect(MobEffects.SLOWNESS);
        double actualLevel=slowness==null?0:slowness.getAmplifier()+1;
        if(requestedSlowness>actualLevel && actualLevel>0 && actualLevel<7) {
            double desiredFactor=Math.max(0,1-0.15*requestedSlowness),vanillaFactor=1-0.15*actualLevel;
            speed.addOrUpdateTransientModifier(new AttributeModifier(FRACTIONAL_SLOWNESS,
                desiredFactor/vanillaFactor-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else speed.removeModifier(FRACTIONAL_SLOWNESS);
    }
}
