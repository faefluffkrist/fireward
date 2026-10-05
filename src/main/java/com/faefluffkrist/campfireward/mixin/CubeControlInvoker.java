package com.faefluffkrist.campfireward.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.entity.monster.cubemob.AbstractCubeMob$CubeMobMoveControl")
public interface CubeControlInvoker {
    @Invoker("setDirection") void ward$direction(float yaw, boolean aggressive);
    @Invoker("setWantedMovement") void ward$movement(double speed);
}
