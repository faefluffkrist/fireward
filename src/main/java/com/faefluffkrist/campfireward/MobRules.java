package com.faefluffkrist.campfireward;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.server.level.ServerLevel;
import java.util.Set;

/** Shared by behavior and registry-generated tooltips. Vexes are never illagers here. */
public final class MobRules {
    private static final Set<String> MOD_ILLAGERS = Set.of(
        "friendsandfoes:iceologer", "friendsandfoes:illusioner",
        "takesapillage:archer", "takesapillage:legioner", "takesapillage:skirmisher");
    private MobRules() {}
    public static boolean illager(Entity entity) {
        if (entity instanceof Vex) return false;
        return entity instanceof AbstractIllager || (entity instanceof Raider && !(entity instanceof Witch))
            || MOD_ILLAGERS.contains(id(entity));
    }
    public static String id(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
    public static boolean hostile(Entity entity) {
        return entity instanceof Mob && (entity instanceof Enemy
            || entity.getType().getCategory() == MobCategory.MONSTER || illager(entity));
    }
    public static boolean immune(Entity entity) {
        return entity instanceof Warden || entity instanceof WitherBoss
            || entity instanceof EnderDragon || entity instanceof Guardian;
    }
    public static boolean piglin(Entity entity) {
        return entity instanceof AbstractPiglin || entity instanceof ZombifiedPiglin
            || id(entity).contains("piglin");
    }
    public static boolean auraAffected(Entity entity) { return illager(entity) || entity instanceof Witch; }
    public static boolean raidImmune(Entity entity) { return raidImmune(entity, FirewardConfig.active); }
    public static boolean raidImmune(Entity entity, FirewardConfig config) {
        if (!config.enabled || !config.raidImmunity) return false;
        boolean raidMob = illager(entity);
        if (entity instanceof Vex vex && vex.getOwner() != null) {
            var owner = vex.getOwner();
            if (owner instanceof Raider raider && raider.hasActiveRaid()) return true;
            raidMob = illager(owner);
        }
        if (!raidMob) return false;
        if (entity instanceof Raider raider && raider.hasActiveRaid()) return true;
        if (config.raidAreaImmunity && entity.level() instanceof ServerLevel level) {
            var raid = level.getRaidAt(entity.blockPosition());
            return raid != null && raid.isActive() && !raid.isStopped();
        }
        return false;
    }
    public static boolean flees(Entity entity, boolean soul) { return flees(entity,soul,FirewardConfig.active); }
    public static boolean flees(Entity entity, boolean soul, FirewardConfig config) {
        var fire = config.fire(soul);
        if (!config.enabled || !fire.enabled || !fire.fearEnabled || !config.supports(id(entity)) || raidImmune(entity,config)) return false;
        Boolean override = fire.fearMobs.get(id(entity));
        return entity instanceof Mob && (override != null ? override : defaultFear(entity,soul));
    }
    public static boolean defaultFear(Entity entity, boolean soul) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (path.equals("sulfur_cube")) return false;
        if (!soul && Set.of("blaze", "breeze", "camel_husk", "husk_camel", "ghast", "hoglin").contains(path)) return false;
        if (soul && (path.equals("hoglin") || path.equals("ghast"))) return true;
        return hostile(entity) && !immune(entity) && !auraAffected(entity) && (soul || !piglin(entity));
    }
    public static boolean aura(Entity entity, boolean soul, FirewardConfig config) {
        var fire = config.fire(soul);
        return config.enabled && fire.enabled && fire.aurasEnabled && config.supports(id(entity))
            && !raidImmune(entity,config) && fire.auraMobs.getOrDefault(id(entity),auraAffected(entity));
    }
    public static boolean attracts(Entity entity, boolean soul, FirewardConfig config) {
        var fire = config.fire(soul);
        return config.enabled && fire.enabled && fire.attractionEnabled && config.supports(id(entity))
            && !raidImmune(entity,config) && fire.attractionMobs.getOrDefault(id(entity),illager(entity));
    }
    public static boolean watches(Entity entity, FirewardConfig config) {
        return !raidImmune(entity,config) && config.supports(id(entity)) && config.watchingMobs.getOrDefault(id(entity),illager(entity));
    }
}
