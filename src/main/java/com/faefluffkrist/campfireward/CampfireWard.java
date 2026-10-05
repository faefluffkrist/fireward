package com.faefluffkrist.campfireward;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class CampfireWard implements ModInitializer {
    private static final Set<Mob> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    @Override public void onInitialize() {
        FirewardConfig.load();
        ConfigNetworking.initialize();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> { FirewardConfig.load(); WardIndex.clear(); INSTALLED.clear(); IllagerTargeting.clear(); VillagerShelter.clear(); });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { WardIndex.clear(); INSTALLED.clear(); IllagerTargeting.clear(); VillagerShelter.clear(); });
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyGenerated) -> {
            chunk.getBlockEntitiesPos().forEach(pos -> {
                var be = chunk.getBlockEntity(pos);
                if (be instanceof CampfireBlockEntity)
                    WardIndex.add(level, pos);
            });
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> WardIndex.unload(level, chunk.getPos().pack()));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Mob mob) || !INSTALLED.add(mob)) return;
            // A single controller reserves movement before ordinary goal-based combat.
            mob.getGoalSelector().addGoal(0, new CampfireGoal(mob));
        });
    }
}
