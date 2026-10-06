package com.faefluffkrist.campfireward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Only loaded chunks are indexed. No world scan or forced chunks. Unmarked fires remain ineligible by default. */
public final class WardIndex {
    private static final Map<ServerLevel, Map<Long, Set<BlockPos>>> LEVELS = new WeakHashMap<>();
    private WardIndex() {}
    public record Fire(BlockPos pos, boolean soul) {
        public Vec3 center() { return Vec3.atCenterOf(pos); }
        public FirewardConfig.FireSettings settings() { return FirewardConfig.active.fire(soul); }
    }
    public static void clear() { LEVELS.clear(); }
    public static void add(ServerLevel level, BlockPos pos) {
        LEVELS.computeIfAbsent(level, ignored -> new HashMap<>())
            .computeIfAbsent(ChunkPos.pack(pos), ignored -> new HashSet<>()).add(pos.immutable());
    }
    public static void unload(ServerLevel level, long chunk) {
        var map = LEVELS.get(level);
        if (map != null) map.remove(chunk);
    }
    public static boolean placed(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof CampfireBlockEntity be
            && ((WardMarker)be).ward$isPlayerPlaced();
    }
    public static boolean eligible(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof CampfireBlockEntity be
            && (FirewardConfig.active.includeNatural || ((WardMarker)be).ward$isPlayerPlaced());
    }
    public static List<Fire> nearby(ServerLevel level, Vec3 origin, double range, boolean litOnly) {
        return nearby(level,origin,range,litOnly,false);
    }
    public static boolean verticalReach(net.minecraft.world.entity.Entity entity, Fire fire) {
        return !FirewardConfig.active.verticalEffects || entity instanceof net.minecraft.world.entity.monster.Phantom
            || Math.abs(entity.getY()-fire.pos().getY())<=10;
    }
    public static List<Fire> nearbyMob(ServerLevel level, net.minecraft.world.entity.Mob mob, double range, boolean litOnly) {
        return nearby(level,mob.position(),range,litOnly,mob instanceof net.minecraft.world.entity.monster.Phantom);
    }
    private static List<Fire> nearby(ServerLevel level, Vec3 origin, double range, boolean litOnly, boolean phantom) {
        if (FireRestrictions.endBlocked(level)) return List.of();
        var map = LEVELS.get(level);
        if (map == null) return List.of();
        List<Fire> result = new ArrayList<>();
        int minX = (int)Math.floor((origin.x - range) / 16);
        int maxX = (int)Math.floor((origin.x + range) / 16);
        int minZ = (int)Math.floor((origin.z - range) / 16);
        int maxZ = (int)Math.floor((origin.z + range) / 16);
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            var positions = map.get(ChunkPos.pack(x, z));
            if (positions == null) continue;
            for (Iterator<BlockPos> it = positions.iterator(); it.hasNext();) {
                BlockPos pos = it.next();
                if (!level.hasChunkAt(pos)) continue;
                if (!(level.getBlockEntity(pos) instanceof CampfireBlockEntity)) { it.remove(); continue; }
                if (!eligible(level,pos)) continue;
                var state = level.getBlockState(pos);
                boolean regular = state.is(Blocks.CAMPFIRE), soul = state.is(Blocks.SOUL_CAMPFIRE);
                if (!(regular || soul)) { it.remove(); continue; }
                if (FirewardConfig.active.enabled && FirewardConfig.active.fire(soul).enabled
                    && (!litOnly || state.getValue(CampfireBlock.LIT))
                    && origin.distanceToSqr(Vec3.atCenterOf(pos)) <= range * range
                    && (!FirewardConfig.active.verticalEffects || phantom || Math.abs(origin.y-pos.getY())<=10))
                    result.add(new Fire(pos, soul));
            }
        }
        return result;
    }
}
