package com.faefluffkrist.campfireward.mixin;

import com.faefluffkrist.campfireward.WardMarker;
import com.faefluffkrist.campfireward.WardIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireMarkerMixin implements WardMarker {
    @Unique private boolean ward$playerPlaced;
    @Override public boolean ward$isPlayerPlaced() { return ward$playerPlaced; }
    @Override public void ward$markPlayerPlaced() {
        ward$playerPlaced = true;
        ((BlockEntity)(Object)this).setChanged();
    }
    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void ward$load(ValueInput input, CallbackInfo ci) {
        ward$playerPlaced = input.getBooleanOr("campfire_ward:player_placed", false);
        BlockEntity be = (BlockEntity)(Object)this;
        if (ward$playerPlaced && be.getLevel() instanceof ServerLevel level)
            WardIndex.add(level, be.getBlockPos());
    }
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void ward$save(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("campfire_ward:player_placed", ward$playerPlaced);
    }
}
