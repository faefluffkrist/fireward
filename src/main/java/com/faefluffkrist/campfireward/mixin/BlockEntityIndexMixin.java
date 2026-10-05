package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.WardIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityIndexMixin {
    @Inject(method="setLevel",at=@At("TAIL"))
    private void fireward$indexLoadedFire(Level level, CallbackInfo ci) {
        if ((Object)this instanceof CampfireBlockEntity fire && level instanceof ServerLevel server)
            WardIndex.add(server,fire.getBlockPos());
    }
}
