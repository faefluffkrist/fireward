package com.faefluffkrist.campfireward.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerInvoker {
    @Invoker("findAttachableSurface") Direction ward$findSurface(BlockPos pos);
    @Invoker("setAttachFace") void ward$attach(Direction direction);
    @Invoker("setRawPeekAmount") void ward$close(int amount);
}
