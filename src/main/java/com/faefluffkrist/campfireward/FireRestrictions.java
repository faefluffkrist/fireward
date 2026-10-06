package com.faefluffkrist.campfireward;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Server-authoritative checks against generated structure bounds, not locate origins. */
public final class FireRestrictions {
    public static final int RANGE = 30;
    private static final Set<String> VANILLA = Set.of("stronghold", "fortress", "end_city", "desert_pyramid",
        "jungle_pyramid", "ancient_city", "mansion", "swamp_hut", "pillager_outpost", "monument",
        "trial_chambers", "ocean_ruin_cold", "ocean_ruin_warm", "bastion_remnant");
    private static final Set<String> KNOWN = Set.of("citadel", "bastille", "pillager_camp", "iceologer_cabin",
        "illusioner_shack", "illusioner_training_grounds", "catacomb", "creeping_crypt", "undead_crypt",
        "trial_dungeon", "trident_trial_monument", "lone_citadel", "nether_keep", "sealing_halls",
        "toxic_lair", "end_castle", "end_ship", "illager_barracks", "illager_camp", "illager_hideout",
        "illager_manor", "mangrove_witch_hut", "witch_villa", "stray_fort", "stray_outlook",
        "bogged_camp", "parched_camp", "stray_camp", "piglin_donjon", "piglin_outstation");
    private FireRestrictions() {}
    private static boolean hostileName(String path) {
        if (path.equals("end_city") || path.equals("ancient_city")) return true;
        if (path.matches(".*(?:^|_)(?:village|town|city|well|bridge|tavern|hamlet)(?:_|$).*")) return false;
        return VANILLA.contains(path) || KNOWN.contains(path)
            || path.matches(".*(?:^|_)(?:dungeon|stronghold|temple|fortress)(?:_|$).*")
            || path.startsWith("nether_skeleton_tower_");
    }
    private static boolean hostile(ServerLevel level, Structure structure) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var id = registry.getKey(structure);
        if (id != null && hostileName(id.getPath())) return true;
        return registry.wrapAsHolder(structure).tags().anyMatch(tag -> {
            String path = tag.location().getPath();
            return tag.location().toString().equals("fireward:blocked_campfires")
                || path.matches(".*(?:^|[/_])(?:dungeons?|strongholds?)(?:[/_]|$).*");
        });
    }
    public static String reason(ServerLevel level, BlockPos pos) {
        if (!FirewardConfig.active.enabled) return null;
        if (endBlocked(level)) return "It's too frigid and desolate here to light a fire..";
        if (!FirewardConfig.active.dangerousAreas) return null;
        for (int x = (pos.getX()-RANGE-5)>>4; x <= (pos.getX()+RANGE+5)>>4; x++) {
            for (int z = (pos.getZ()-RANGE-5)>>4; z <= (pos.getZ()+RANGE+5)>>4; z++) {
                var chunk = level.getChunkSource().getChunkNow(x,z);
                if (chunk == null) continue;
                for (var start : level.structureManager().startsForStructure(new ChunkPos(x,z), s -> hostile(level,s))) {
                    if (!start.isValid()) continue;
                    var b = start.getBoundingBox();
                    double dx = Math.max(Math.max(b.minX()-pos.getX(), pos.getX()-b.maxX()),0);
                    double dy = Math.max(Math.max(b.minY()-pos.getY(), pos.getY()-b.maxY()),0);
                    double dz = Math.max(Math.max(b.minZ()-pos.getZ(), pos.getZ()-b.maxZ()),0);
                    if (dy < 10 && dx*dx+dz*dz <= RANGE*RANGE) return "It's too dangerous to light a fire here..";
                }
                // Monster rooms are features, so their spawners supply the fallback in existing worlds.
                for (var be : chunk.getBlockEntities().values()) {
                    if (be instanceof SpawnerBlockEntity) {
                        var p = be.getBlockPos();
                        // A vanilla room extends at most five blocks horizontally from its spawner.
                        double dx = Math.max(Math.abs(p.getX()-pos.getX())-5,0);
                        double dy = Math.max(Math.abs(p.getY()-pos.getY())-4,0);
                        double dz = Math.max(Math.abs(p.getZ()-pos.getZ())-5,0);
                        if (dy < 10 && dx*dx+dz*dz <= RANGE*RANGE) return "It's too dangerous to light a fire here..";
                    }
                }
            }
        }
        return null;
    }
    public static boolean endBlocked(ServerLevel level) {
        return FirewardConfig.active.enabled && FirewardConfig.active.endRestriction && level.dimension().equals(Level.END);
    }
    private static final java.util.Map<ServerLevel,java.util.Map<Long,Long>> COLD = new java.util.WeakHashMap<>();
    public static void coldFeedback(ServerLevel level, BlockPos pos) {
        if (!endBlocked(level)) return;
        var times = COLD.computeIfAbsent(level, ignored -> new java.util.HashMap<>());
        long now = level.getGameTime();
        times.entrySet().removeIf(entry -> now-entry.getValue()>20);
        Long last = times.get(pos.asLong());
        if (last != null && now-last<10) return;
        times.put(pos.asLong(),now);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
            pos.getX()+0.5,pos.getY()+0.65,pos.getZ()+0.5,4,0.18,0.12,0.18,0.01);
        level.playSound(null,pos,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
            net.minecraft.sounds.SoundSource.BLOCKS,0.35F,1.1F);
    }
    public static void warn(ServerPlayer player, ServerLevel level, BlockPos pos) {
        String reason = reason(level,pos);
        if (reason != null) {
            player.sendOverlayMessage(Component.literal(reason));
            coldFeedback(level,pos);
        }
        else IgnitionGuard.warn(player);
    }
}
