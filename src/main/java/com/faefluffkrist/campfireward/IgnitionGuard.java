package com.faefluffkrist.campfireward;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public final class IgnitionGuard {
    private IgnitionGuard() {}
    public static boolean watched(ServerLevel level, BlockPos pos) {
        FirewardConfig config = FirewardConfig.active;
        if (!config.enabled || !config.restrictionEnabled) return false;
        Vec3 center = Vec3.atCenterOf(pos);
        return !level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(config.restrictionRange),
            mob -> mob.isAlive() && (!config.verticalEffects || Math.abs(mob.getY()-pos.getY())<=10) && MobRules.watches(mob,config) && mob.position().distanceToSqr(center) <= config.restrictionRange * config.restrictionRange
                && (config.restrictionThroughWalls || sees(mob,level,pos))).isEmpty();
    }
    public static boolean blocked(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
            && !state.getValue(CampfireBlock.LIT)
            && (FireRestrictions.reason(level,pos) != null || (FirewardConfig.active.fire(state.is(Blocks.SOUL_CAMPFIRE)).enabled
            && FirewardConfig.active.blockIgnition && !state.getValue(CampfireBlock.LIT)
            && WardIndex.eligible(level, pos) && watched(level, pos)));
    }
    private static boolean sees(Mob mob, ServerLevel level, BlockPos pos) {
        var hit = level.clip(new ClipContext(mob.getEyePosition(),Vec3.atCenterOf(pos).add(0,0.3,0),
            ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mob));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    public static void warn(ServerPlayer player) {
        if (!FirewardConfig.active.warningEnabled) return;
        player.sendOverlayMessage(Component.literal(FirewardConfig.active.warningText)
            .withStyle(FirewardConfig.color(FirewardConfig.active.warningColor)));
    }
}
