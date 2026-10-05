package com.faefluffkrist.campfireward;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** A short combat-intent lease prevents a group's first investigator from erasing the group's aggression. */
public final class IllagerTargeting {
    private record Intent(Player player,long tick) {}
    private static final Map<Mob,Intent> INTENTS=new WeakHashMap<>();
    private IllagerTargeting() {}
    public static void clear(){INTENTS.clear();}
    private static boolean valid(Player player){return player.isAlive() && !player.isCreative() && !player.isSpectator();}
    public static void remember(Mob mob,ServerLevel level){
        if(mob.getTarget() instanceof Player player && valid(player))INTENTS.put(mob,new Intent(player,level.getGameTime()));
        else if(mob.getTarget()!=null)INTENTS.remove(mob);
    }
    private static Player combatPlayer(Mob mob,ServerLevel level){
        remember(mob,level);
        Intent intent=INTENTS.get(mob);
        if(intent==null)return null;
        long lease=Math.max(1,Math.round(FirewardConfig.active.groupMemorySeconds*20));
        if(!valid(intent.player) || intent.player.level()!=level || level.getGameTime()-intent.tick>lease){INTENTS.remove(mob);return null;}
        return intent.player;
    }
    public static boolean aggressiveGroup(Mob mob,Player player,ServerLevel level){
        var config=FirewardConfig.active;
        if(!config.groupException || !MobRules.illager(mob) || combatPlayer(mob,level)!=player)return false;
        int count=0;
        for(Mob ally:level.getEntitiesOfClass(Mob.class,mob.getBoundingBox().inflate(config.groupRange),
                entity->entity.isAlive() && MobRules.illager(entity) && !MobRules.raidImmune(entity)
                    && entity.position().distanceToSqr(mob.position())<=config.groupRange*config.groupRange)) {
            if(combatPlayer(ally,level)==player && ++count>=config.groupSize)return true;
        }
        return false;
    }
    public static boolean prioritizePlayer(Mob mob,ServerLevel level){
        if(!FirewardConfig.active.playerPriority)return false;
        Player combat=combatPlayer(mob,level);
        if(combat!=null){
            if(aggressiveGroup(mob,combat,level))return false;
            if(mob.getTarget()==combat || (mob.canAttack(combat) && mob.getSensing().hasLineOfSight(combat))){mob.setTarget(combat);return true;}
        }
        for(Player player:level.getEntitiesOfClass(Player.class,mob.getBoundingBox().inflate(FirewardConfig.active.playerPriorityRange),IllagerTargeting::valid)) {
            if(mob.position().distanceToSqr(player.position())>Math.pow(FirewardConfig.active.playerPriorityRange,2))continue;
            if(mob.canAttack(player) && mob.getSensing().hasLineOfSight(player)) {
                if(aggressiveGroup(mob,player,level))return false;
                mob.setTarget(player);remember(mob,level);return true;
            }
        }
        return false;
    }
}
