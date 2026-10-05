package com.faefluffkrist.campfireward;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Adjust only an unsafe panic destination; preserve normal movement already inside the ward. */
public final class VillagerShelter {
    private record Candidate(Vec3 point,double score) {}
    private record Owned(WalkTarget walk,Path path,long tick) {}
    private static final Map<Villager,Owned> OWNED=new WeakHashMap<>();
    private VillagerShelter() {}
    public static void clear(){OWNED.clear();}
    private static void release(Villager villager){
        Owned previous=OWNED.remove(villager);
        if(previous!=null && villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null)==previous.walk) {
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);villager.getNavigation().stop();
        }
    }
    public static List<Mob> threats(Villager villager,ServerLevel level){
        var config=FirewardConfig.active;
        var perceived=villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        List<Mob> result=new ArrayList<>();
        for(Mob mob:level.getEntitiesOfClass(Mob.class,villager.getBoundingBox().inflate(config.villagerThreatRange),
                entity->entity.isAlive() && (entity instanceof Zombie || MobRules.illager(entity))
                    && entity.position().distanceToSqr(villager.position())<=Math.pow(config.villagerThreatRange,2))) {
            // Respect vanilla's visibility predicate and supplement it between sensor refreshes.
            if(perceived.map(visible->visible.contains(mob)).orElse(false) || villager.getSensing().hasLineOfSight(mob))result.add(mob);
        }
        return result;
    }
    public static WardIndex.Fire refuge(Villager villager,ServerLevel level,List<Mob> threats){
        var config=FirewardConfig.active;
        if(threats.isEmpty() || threats.stream().anyMatch(MobRules::illager))return null;
        return WardIndex.nearby(level,villager.position(),config.villagerSearchRange,true).stream()
            .filter(fire->fire.settings().fearEnabled && fire.settings().fearRange-config.villagerEdgeMargin>config.villagerFireClearance
                && threats.stream().allMatch(zombie->MobRules.flees(zombie,fire.soul())))
            .min(Comparator.comparingDouble(fire->villager.position().distanceToSqr(fire.center()))).orElse(null);
    }
    public static boolean safeDestination(Vec3 point,WardIndex.Fire fire){
        var config=FirewardConfig.active;
        double distance=point.distanceToSqr(fire.center()),radius=fire.settings().fearRange-config.villagerEdgeMargin;
        return radius>config.villagerFireClearance && distance<=radius*radius
            && Math.pow(point.x-fire.center().x,2)+Math.pow(point.z-fire.center().z,2)>=Math.pow(config.villagerFireClearance,2);
    }
    public static boolean staysInside(Path path,WardIndex.Fire fire) {
        if(path==null)return true;
        double radius=fire.settings().fearRange-FirewardConfig.active.villagerEdgeMargin;
        for(int i=path.getNextNodeIndex();i<path.getNodeCount();i++)
            if(Vec3.atBottomCenterOf(path.getNodePos(i)).distanceToSqr(fire.center())>radius*radius)return false;
        return true;
    }
    public static void tick(Villager villager,ServerLevel level){
        var config=FirewardConfig.active;
        if(!config.enabled || !config.villagerShelter || villager.isNoAi() || !villager.isAlive() || villager.isSleeping()) {release(villager);return;}
        var nearby=threats(villager,level);var fire=refuge(villager,level,nearby);
        if(fire==null){release(villager);return;}
        var current=villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        Owned previous=OWNED.get(villager);
        boolean alreadyInside=villager.position().distanceToSqr(fire.center())<=Math.pow(fire.settings().fearRange-config.villagerEdgeMargin,2);
        if(current!=null && safeDestination(current.getTarget().currentPosition(),fire)
                && (!alreadyInside || staysInside(villager.getNavigation().getPath(),fire)))return;
        if(previous!=null && level.getGameTime()-previous.tick<config.villagerRepathTicks
                && safeDestination(previous.walk.getTarget().currentPosition(),fire)
                && (!alreadyInside || staysInside(previous.path,fire))) {
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET,previous.walk);
            villager.getNavigation().moveTo(previous.path,config.villagerShelterSpeed);return;
        }
        Vec3 best=null;Path bestPath=null;List<Candidate> candidates=new ArrayList<>();
        double radius=Math.min(8,fire.settings().fearRange-config.villagerEdgeMargin);
        for(int ring=0;ring<2;ring++)for(int i=0;i<12;i++) {
            double angle=(i+ring*0.5)*Math.PI/6,r=Math.max(config.villagerFireClearance+0.5,radius*(ring==0?1:0.65));
            BlockPos column=BlockPos.containing(fire.center().x+Math.cos(angle)*r,fire.pos().getY(),fire.center().z+Math.sin(angle)*r);
            for(int dy=-2;dy<=2;dy++) {
                BlockPos pos=column.above(dy);Vec3 point=Vec3.atBottomCenterOf(pos);
                if(!safeDestination(point,fire) || !level.hasChunkAt(pos) || !level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())
                        || !level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),net.minecraft.core.Direction.UP)
                        || !level.getFluidState(pos).isEmpty())continue;
                double danger=nearby.stream().mapToDouble(threat->point.distanceToSqr(threat.position())).min().orElse(0);
                double candidate=Math.sqrt(danger)-0.35*villager.position().distanceTo(point)+villager.getRandom().nextDouble();
                candidates.add(new Candidate(point,candidate));
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed());
        int attempts=0;
        for(Candidate candidate:candidates) {
            if(attempts++>=config.villagerPathAttempts)break;
            Path path=villager.getNavigation().createPath(BlockPos.containing(candidate.point),0);
            if(path==null || !path.canReach() || (alreadyInside && !staysInside(path,fire)))continue;
            best=candidate.point;bestPath=path;break;
        }
        if(best==null){release(villager);return;}
        WalkTarget walk=new WalkTarget(best,(float)config.villagerShelterSpeed,1);
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET,walk);
        villager.getNavigation().moveTo(bestPath,config.villagerShelterSpeed);
        OWNED.put(villager,new Owned(walk,bestPath,level.getGameTime()));
    }
}
