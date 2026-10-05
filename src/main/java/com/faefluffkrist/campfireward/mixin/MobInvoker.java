package com.faefluffkrist.campfireward.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobInvoker {
    @Invoker("customServerAiStep") void ward$customAi(ServerLevel level);
}
