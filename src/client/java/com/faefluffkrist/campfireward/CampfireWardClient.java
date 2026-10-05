package com.faefluffkrist.campfireward;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Items;
import java.util.*;
import org.slf4j.LoggerFactory;

public final class CampfireWardClient implements ClientModInitializer {
    private ClientLevel cachedLevel;
    private FirewardConfig cachedConfig;
    private final List<Component> regularFlee = new ArrayList<>(), soulFlee = new ArrayList<>();
    private final List<Component> regularExcept = new ArrayList<>(), soulExcept = new ArrayList<>();
    @Override public void onInitializeClient() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(ConfigNetworking.ConfigPayload.TYPE,
            (payload, context) -> context.client().execute(() -> {try {
                FirewardConfig.serverOverride=FirewardConfig.fromJson(payload.json());cachedLevel=null;
            }catch(RuntimeException e){org.slf4j.LoggerFactory.getLogger("fireward").warn("Invalid server settings",e);}}));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{
            FirewardConfig.serverOverride=null;cachedLevel=null;
        });
        ItemTooltipCallback.EVENT.register((stack, context, flags, tooltip) -> {
            boolean soul = stack.is(Items.SOUL_CAMPFIRE);
            if (!soul && !stack.is(Items.CAMPFIRE)) return;
            FirewardConfig config=FirewardConfig.current();var fire=config.fire(soul);
            if(!config.tooltipsEnabled)return;
            if(!config.enabled || !fire.enabled){tooltip.add(Component.literal("Fireward effects disabled").withStyle(ChatFormatting.GRAY));return;}
            if(fire.fearEnabled)line(tooltip,"Wards off hostiles",number(fire.fearRange)+" blocks",ChatFormatting.GOLD);
            if(fire.attractionEnabled)line(tooltip,fire.attractionMobs.isEmpty()?"Draws illager attention":"Draws selected mobs",number(fire.attractionRange)+" blocks",ChatFormatting.RED);
            else if(soul)tooltip.add(Component.literal("Soul fire is unseen by illagers").withStyle(ChatFormatting.AQUA));
            boolean expanded=Minecraft.getInstance().hasShiftDown() || !config.shiftForLists;
            if(!expanded){tooltip.add(Component.literal("Hold Shift for details & mob lists").withStyle(ChatFormatting.DARK_AQUA));return;}
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Fireward details").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
            if(fire.aurasEnabled)for(var effect:fire.effects())if(effect.enabled) {
                var id=net.minecraft.resources.Identifier.tryParse(effect.effectId);
                String name=id==null?effect.effectId:BuiltInRegistries.MOB_EFFECT.get(id)
                    .map(value->value.value().getDisplayName().getString()).orElse(effect.effectId);
                line(tooltip,name+" "+number(effect.level),number(effect.range)+" blocks",ChatFormatting.LIGHT_PURPLE);
            }
            if(fire.attractionEnabled){
                tooltip.add(Component.literal((fire.destroyUnlit?"Lit or unlit":"Lit only")+(fire.requireLineOfSight?" • Line of sight":"")).withStyle(ChatFormatting.YELLOW));
                if(fire.destroyEnabled)line(tooltip,"Campfire destruction",number(fire.breakSeconds)+" seconds",ChatFormatting.RED);
                if(config.playerPriority)tooltip.add(Component.literal("Players take priority"+(config.groupException?" • "+config.groupSize+"+ aggressive allies may break fire":"")).withStyle(ChatFormatting.YELLOW));
            }
            if(config.restrictionEnabled && (config.blockLitPlacement || config.blockIgnition))line(tooltip,"Watching range",number(config.restrictionRange)+" blocks",ChatFormatting.RED);
            if(config.raidImmunity)tooltip.add(Component.literal("Raid illagers ignore campfires").withStyle(ChatFormatting.GREEN));
            if(config.villagerShelter)tooltip.add(Component.literal("Shelters villagers from visible zombies").withStyle(ChatFormatting.GREEN));
            if(!config.includeNatural)tooltip.add(Component.literal("Player-placed fires only").withStyle(ChatFormatting.DARK_GRAY));
            if(!config.showMobLists)return;
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                tooltip.add(Component.literal("Enter a world to view the installed mob lists.").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }
            if (cachedLevel != level || cachedConfig != config) rebuild(level,config);
            if (config.shiftForLists && !Minecraft.getInstance().hasShiftDown()) {
                tooltip.add(Component.literal("Hold Shift: dynamic mob and exception lists").withStyle(ChatFormatting.AQUA));
                return;
            }
            tooltip.add(Component.empty());
            appendList(tooltip, "Wards off:", soul ? soulFlee : regularFlee, ChatFormatting.GREEN);
            appendList(tooltip, "Does not scare off:", soul ? soulExcept : regularExcept, ChatFormatting.RED);
        });
    }
    private void rebuild(ClientLevel level,FirewardConfig config) {
        cachedConfig=config;
        cachedLevel = level;
        regularFlee.clear(); soulFlee.clear(); regularExcept.clear(); soulExcept.clear();
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            try {
                var entity = type.create(level, EntitySpawnReason.LOAD);
                if (!(entity instanceof net.minecraft.world.entity.Mob) || (!MobRules.hostile(entity) && !config.regular.fearMobs.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()) && !config.soul.fearMobs.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()))) continue;
                Component name = type.getDescription();
                (MobRules.flees(entity, false,config) ? regularFlee : regularExcept).add(name);
                (MobRules.flees(entity, true,config) ? soulFlee : soulExcept).add(name);
            } catch (RuntimeException e) {
                LoggerFactory.getLogger("fireward").warn("Cannot classify tooltip entity {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
            }
        }
        for (var list : List.of(regularFlee, soulFlee, regularExcept, soulExcept))
            list.sort(Comparator.comparing(Component::getString));
    }
    private static String number(double value){return value==Math.rint(value)?Long.toString((long)value):Double.toString(value);}
    private static void line(List<Component> tooltip,String label,String value,ChatFormatting color){
        tooltip.add(Component.literal(label+": ").withStyle(color).append(Component.literal(value).withStyle(ChatFormatting.WHITE)));
    }
    private static void appendList(List<Component> tooltip, String label, List<Component> names, ChatFormatting color) {
        tooltip.add(Component.literal(label).withStyle(color));
        // Keep lines narrow enough for ordinary GUI scales. Names are translated by Minecraft.
        StringBuilder line = new StringBuilder("  ");
        for (Component name : names) {
            String next = name.getString();
            if (line.length() > 2 && line.length() + next.length() + 2 > 52) {
                tooltip.add(Component.literal(line.toString()).withStyle(ChatFormatting.GRAY));
                line = new StringBuilder("  ");
            }
            if (line.length() > 2) line.append(", ");
            line.append(next);
        }
        if (line.length() > 2) tooltip.add(Component.literal(line.toString()).withStyle(ChatFormatting.GRAY));
    }
}
