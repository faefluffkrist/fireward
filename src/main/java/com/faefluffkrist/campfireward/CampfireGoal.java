package com.faefluffkrist.campfireward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import com.faefluffkrist.campfireward.mixin.ShulkerInvoker;
import com.faefluffkrist.campfireward.mixin.PhantomAccessor;
import com.faefluffkrist.campfireward.mixin.CubeControlInvoker;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class CampfireGoal extends Goal {
    private final Mob mob;
    private WardIndex.Fire fire;
    private Vec3 escape;
    private int breakTicks, repath;
    private boolean fleeing;
    public CampfireGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    public static boolean isFleeing(Mob mob) {
        return mob.getGoalSelector().getAvailableGoals().stream().anyMatch(wrapped ->
            wrapped.isRunning() && wrapped.getGoal() instanceof CampfireGoal goal && goal.fleeing);
    }
    private ServerLevel world() { return (ServerLevel)mob.level(); }
    private boolean playerPriority() { return IllagerTargeting.prioritizePlayer(mob,world()); }
    private boolean visible(WardIndex.Fire candidate) {
        var hit = world().clip(new ClipContext(mob.getEyePosition(), candidate.center().add(0, 0.3, 0),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(candidate.pos());
    }
    @Override public boolean canUse() {
        if (mob.isNoAi() || !mob.isAlive() || MobRules.raidImmune(mob)) return false;
        FirewardConfig config = FirewardConfig.active;
        if (!config.enabled || (!MobRules.flees(mob,false,config) && !MobRules.flees(mob,true,config)
                && !MobRules.attracts(mob,false,config) && !MobRules.attracts(mob,true,config))) return false;
        fire = WardIndex.nearby(world(),mob.position(),config.maxFearRange(),true).stream()
            .filter(f -> MobRules.flees(mob,f.soul()) && mob.position().distanceToSqr(f.center()) <= f.settings().fearRange * f.settings().fearRange)
            .min(Comparator.comparingDouble(f -> mob.position().distanceToSqr(f.center()))).orElse(null);
        fleeing = fire != null;
        if (fire == null && (MobRules.attracts(mob,false,config) || MobRules.attracts(mob,true,config))) {
            fire = WardIndex.nearby(world(),mob.position(),config.maxAttractionRange(),false).stream()
                .filter(f -> MobRules.attracts(mob,f.soul(),config)
                    && mob.position().distanceToSqr(f.center()) <= f.settings().attractionRange * f.settings().attractionRange
                    && (f.settings().destroyUnlit || world().getBlockState(f.pos()).getValue(net.minecraft.world.level.block.CampfireBlock.LIT))
                    && (!f.settings().requireLineOfSight || visible(f)))
                .min(Comparator.comparingDouble(f -> mob.position().distanceToSqr(f.center()))).orElse(null);
        }
        if(fire!=null && !fleeing && playerPriority())fire=null;
        return fire != null;
    }
    @Override public boolean canContinueToUse() {
        if (fire == null || mob.isNoAi() || !mob.isAlive() || MobRules.raidImmune(mob)) return false;
        if (fleeing) {
            // Finish crossing the boundary, then let ordinary AI resume.
            return mob.position().distanceToSqr(fire.center()) < Math.pow(fire.settings().fearRange + fire.settings().escapeBuffer,2)
                && WardIndex.nearby(world(), fire.center(), 0, true).stream()
                    .anyMatch(f -> MobRules.flees(mob, f.soul()));
        }
        return !playerPriority() && WardIndex.eligible(world(),fire.pos())
            && MobRules.attracts(mob,fire.soul(),FirewardConfig.active)
            && world().getBlockState(fire.pos()).is(fire.soul() ? Blocks.SOUL_CAMPFIRE : Blocks.CAMPFIRE)
            && (fire.settings().destroyUnlit || world().getBlockState(fire.pos()).getValue(net.minecraft.world.level.block.CampfireBlock.LIT))
            && mob.position().distanceToSqr(fire.center()) <= Math.pow(fire.settings().attractionRange,2)
            && (!fire.settings().requireLineOfSight || visible(fire));
    }
    @Override public void start() { breakTicks = 0; repath = 0; escape = null; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void stop() {
        if (fire != null && !fleeing) world().destroyBlockProgress(mob.getId(), fire.pos(), -1);
        mob.getNavigation().stop();
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        fire = null;
        escape = null;
        breakTicks = 0;
    }
    @Override public void tick() {
        if (fire == null) return;
        if (fleeing) fleeTick(); else breakTick();
    }
    private boolean flying() { return mob instanceof Vex || mob instanceof Ghast || mob instanceof Phantom; }
    private void fleeTick() {
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        if (mob instanceof Shulker shulker) {
            if (--repath <= 0) { repath = fire.settings().repathTicks; retreatShulker(shulker); }
            return;
        }
        if (--repath <= 0 || escape == null || mob.position().distanceToSqr(escape) < 2) {
            repath = fire.settings().repathTicks;
            Vec3 away = mob.position().subtract(fire.center());
            if (away.horizontalDistanceSqr() < 0.01) away = new Vec3(1, 0, 0);
            away = new Vec3(away.x, flying() ? away.y : 0, away.z).normalize();
            escape = fire.center().add(away.scale(fire.settings().fearRange + fire.settings().escapeExtra));
            if (!flying() && mob instanceof PathfinderMob pathfinder) {
                // Prefer a walkable intermediate point moving away from the source.
                Vec3 step = DefaultRandomPos.getPosAway(pathfinder, 16, 7, fire.center());
                if (step != null && step.distanceToSqr(fire.center()) > mob.position().distanceToSqr(fire.center()))
                    escape = step;
            }
            mob.getNavigation().moveTo(escape.x, escape.y, escape.z, fire.settings().fleeSpeed);
        }
        // Brain mobs read this on their next brain tick; move control also covers flying AI.
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(escape, (float)fire.settings().fleeSpeed, 1));
        if (mob instanceof Phantom phantom) ((PhantomAccessor)phantom).ward$escapePoint(escape);
        if (mob.getMoveControl() instanceof CubeControlInvoker control) {
            float yaw = (float)(Math.atan2(escape.z - mob.getZ(), escape.x - mob.getX()) * 180 / Math.PI) - 90;
            control.ward$direction(yaw, false);
            control.ward$movement(fire.settings().fleeSpeed);
        }
        if (flying()) mob.getMoveControl().setWantedPosition(escape.x, escape.y, escape.z, fire.settings().fleeSpeed);
    }
    private void retreatShulker(Shulker shulker) {
        ShulkerInvoker invoker = (ShulkerInvoker)shulker;
        BlockPos origin = shulker.blockPosition(), best = null;
        net.minecraft.core.Direction face = null;
        double distance = mob.position().distanceToSqr(fire.center());
        for (int attempt = 0; attempt < 80; attempt++) {
            BlockPos pos = origin.offset(mob.getRandom().nextInt(17) - 8,
                mob.getRandom().nextInt(9) - 4, mob.getRandom().nextInt(17) - 8);
            double candidateDistance = Vec3.atCenterOf(pos).distanceToSqr(fire.center());
            if (candidateDistance <= distance || !world().hasChunkAt(pos)
                    || !world().getWorldBorder().isWithinBounds(pos) || !world().isEmptyBlock(pos)
                    || !world().noCollision(shulker, new AABB(pos).deflate(0.000001))) continue;
            var surface = invoker.ward$findSurface(pos);
            if (surface != null) { best = pos; face = surface; distance = candidateDistance; }
        }
        if (best != null) {
            shulker.unRide();
            invoker.ward$attach(face);
            invoker.ward$close(0);
            shulker.playSound(SoundEvents.SHULKER_TELEPORT, 1, 1);
            shulker.setPos(best.getX() + 0.5, best.getY(), best.getZ() + 0.5);
            world().gameEvent(GameEvent.TELEPORT, origin, GameEvent.Context.of(shulker));
        }
    }
    private void breakTick() {
        // Recheck on the destruction tick as well as the scheduler tick.
        if(!canContinueToUse()) { stop();return; }
        IllagerTargeting.remember(mob,world());
        mob.setTarget(null);
        mob.getLookControl().setLookAt(fire.center().x, fire.center().y, fire.center().z);
        if (mob.position().distanceToSqr(fire.center()) > Math.pow(fire.settings().breakReach,2)) {
            if (breakTicks > 0) world().destroyBlockProgress(mob.getId(), fire.pos(), -1);
            breakTicks = 0;
            if (--repath <= 0) {
                repath = fire.settings().repathTicks;
                mob.getNavigation().moveTo(fire.center().x, fire.pos().getY(), fire.center().z, fire.settings().approachSpeed);
            }
            return;
        }
        mob.getNavigation().stop();
        if (!fire.settings().destroyEnabled || (FirewardConfig.active.respectMobGriefing && !world().getGameRules().get(GameRules.MOB_GRIEFING))) return;
        breakTicks++;
        int duration = Math.max(1,(int)Math.round(fire.settings().breakSeconds * 20));
        if (fire.settings().breakingOverlay) world().destroyBlockProgress(mob.getId(), fire.pos(), Math.min(9, breakTicks * 10 / duration));
        if (fire.settings().breakingSwing && breakTicks % 10 == 0) mob.swing(InteractionHand.MAIN_HAND);
        if (breakTicks >= duration) {
            // Never substitute an untracked block or a soul campfire during a destruction attempt.
            if (WardIndex.eligible(world(), fire.pos()) && world().getBlockState(fire.pos()).is(fire.soul() ? Blocks.SOUL_CAMPFIRE : Blocks.CAMPFIRE))
                world().destroyBlock(fire.pos(), fire.settings().breakDrops, mob);
            world().destroyBlockProgress(mob.getId(), fire.pos(), -1);
        }
    }
}
