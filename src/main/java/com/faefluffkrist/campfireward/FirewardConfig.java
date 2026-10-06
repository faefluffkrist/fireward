package com.faefluffkrist.campfireward;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import org.slf4j.LoggerFactory;

public final class FirewardConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fireward.json");
    public static volatile FirewardConfig active = new FirewardConfig();
    public static FirewardConfig editable = new FirewardConfig();
    public static volatile FirewardConfig serverOverride;
    @ConfigOption(label="Enable Fireward", description="Master switch for all added gameplay behavior and restrictions.")
    public boolean enabled = true;
    @ConfigOption(label="Include generated campfires", description="Also affect generated, command-created, and pre-existing fires. Disabled by default to protect structures.")
    public boolean includeNatural = false;
    @ConfigOption(label="Dangerous area restrictions", description="Block lit placement and ignition near hostile structures: 30 blocks horizontally, less than 10 vertically from their bounds. Unlit placement is allowed.")
    public boolean dangerousAreas = true;
    @ConfigOption(label="No fires in the End", description="Extinguish placed fires and reject ignition in the End. Campfires provide no Fireward effects there. Show a brief snowflake puff and sizzle.")
    public boolean endRestriction = true;
    @ConfigOption(label="Limit vertical effects", description="Limit campfire effects to 10 blocks above or below the fire. Phantoms retain their normal configured range.")
    public boolean verticalEffects = true;
    @ConfigOption(label="Friends & Foes support", description="Allow Fireward rules to affect Friends & Foes mobs.")
    public boolean friendsAndFoes = true;
    @ConfigOption(label="It Takes a Pillage support", description="Allow Fireward rules to affect It Takes a Pillage Continuation mobs.")
    public boolean takesAPillage = true;
    @ConfigOption(label="Raid immunity", description="Raid illagers ignore fear, auras, attraction, and destruction. Witches still receive configured auras.")
    public boolean raidImmunity = true;
    @ConfigOption(label="Protect nearby raid illagers", description="Also grant immunity to illagers inside an active raid area, even without registered raid membership.")
    public boolean raidAreaImmunity = true;
    @ConfigOption(label="Prioritize players", description="Visible attackable players interrupt campfire investigation and reset breaking progress.")
    public boolean playerPriority = true;
    @ConfigOption(label="Aggressive group exception", description="Two or more nearby illagers fighting the same player can prioritize a lit campfire instead.")
    public boolean groupException = true;
    @ConfigOption(label="Aggressive group size", description="Minimum illagers already fighting the same player, including this illager.", min=2, max=32)
    public int groupSize = 2;
    @ConfigOption(label="Aggressive group radius", description="Distance around the investigating illager used to count aggressive allies.", min=0, max=128)
    public double groupRange = 10;
    @ConfigOption(label="Combat memory (seconds)", description="Briefly retain player aggression while a group member investigates a fire; avoids rapid switching between combat and breaking.", min=0.05, max=120)
    public double groupMemorySeconds = 5;
    @ConfigOption(label="Villager campfire shelter", description="Awake villagers seeing zombies prefer the ward area while remaining free to move. Visible illagers always override shelter.")
    public boolean villagerShelter = true;
    @ConfigOption(label="Villager campfire search range", description="How far a threatened villager searches for an eligible lit fire.", min=0, max=128)
    public double villagerSearchRange = 32;
    @ConfigOption(label="Villager threat detection range", description="Maximum distance to a visible zombie or illager used by the shelter behavior.", min=0, max=128)
    public double villagerThreatRange = 16;
    @ConfigOption(label="Villager shelter speed", description="Movement speed multiplier while moving within the ward.", min=0.1, max=5)
    public double villagerShelterSpeed = 0.5;
    @ConfigOption(label="Villager ward edge margin", description="Keep shelter destinations this far inside the configured fear radius.", min=0, max=16)
    public double villagerEdgeMargin = 1;
    @ConfigOption(label="Villager fire clearance", description="Keep shelter destinations away from the campfire block to avoid standing on flames.", min=1.5, max=8)
    public double villagerFireClearance = 2.5;
    @ConfigOption(label="Villager shelter path interval", description="Ticks between shelter path adjustments. Vanilla perception and illager danger are still checked every tick.", min=1, max=200)
    public int villagerRepathTicks = 20;
    @ConfigOption(label="Villager path attempt limit", description="Maximum reachable shelter candidates tried per refresh, limiting pathfinding work in large villages.", min=1, max=48)
    public int villagerPathAttempts = 8;
    @ConfigOption(label="Player priority range", description="Distance searched for visible players before investigating fires.", min=0, max=128)
    public double playerPriorityRange = 32;
    @ConfigOption(label="Respect mobGriefing", description="Prevent campfire destruction when the vanilla mobGriefing rule is false.")
    public boolean respectMobGriefing = true;
    @ConfigOption(label="Placement / ignition restriction", description="Prevent lit placement and lighting near selected watching mobs.")
    public boolean restrictionEnabled = true;
    @ConfigOption(label="Watching range", description="Spherical distance for refusing lit placement or lighting.", min=0, max=128)
    public double restrictionRange = 40;
    @ConfigOption(label="Watch through walls", description="Watching mobs need no line of sight. Disable to require sight of the campfire location.")
    public boolean restrictionThroughWalls = true;
    @ConfigOption(label="Block lit placement", description="Deny campfires whose actual placement state is lit. Unlit-default placement remains allowed.")
    public boolean blockLitPlacement = true;
    @ConfigOption(label="Block lighting", description="Deny item use on watched, marked, unlit campfires before resources are consumed.")
    public boolean blockIgnition = true;
    @ConfigOption(label="Show warning", description="Display the configured action-bar warning when an attempt is denied.")
    public boolean warningEnabled = true;
    @ConfigOption(label="Warning text", description="Text displayed above the hotbar when placement or lighting is denied.")
    public String warningText = "I shouldn't setup here, I have a feeling I'm being watched";
    @ConfigOption(label="Warning color", description="Minecraft color name: white, red, gold, yellow, green, aqua, blue, light_purple, gray, or another standard color.")
    public String warningColor = "white";
    @ConfigOption(label="Campfire tooltips", description="Show the current configured behavior on campfire item tooltips.")
    public boolean tooltipsEnabled = true;
    @ConfigOption(label="Dynamic mob lists", description="Include lists of affected mobs and exceptions based on installed mobs and configured rules.")
    public boolean showMobLists = true;
    @ConfigOption(label="Hold Shift for details", description="Keep normal tooltips short; reveal effects, behavior details, and mob lists while Shift is held.")
    public boolean shiftForLists = true;
    public int configVersion = 2;
    public Map<String, Boolean> watchingMobs = new LinkedHashMap<>();
    public FireSettings regular = new FireSettings(false);
    public FireSettings soul = new FireSettings(true);

    public static final class FireSettings {
    @ConfigOption(label="Enable this campfire", description="Enable added gameplay behavior for this campfire type.")
    public boolean enabled = true;
    @ConfigOption(label="Fear / fleeing", description="Make selected mobs retreat from lit fires.")
    public boolean fearEnabled = true;
    @ConfigOption(label="Fear range", description="Spherical radius in blocks for starting a retreat.", min=0, max=128)
    public double fearRange = 20;
    @ConfigOption(label="Flee speed multiplier", description="Movement speed multiplier while retreating.", min=0.1, max=5)
    public double fleeSpeed = 1.3;
    @ConfigOption(label="Retreat boundary buffer", description="Extra distance to retreat past the fear radius before normal AI resumes.", min=0, max=32)
    public double escapeBuffer = 4;
    @ConfigOption(label="Retreat destination padding", description="Extra distance beyond the fear radius used for the escape destination.", min=0, max=64)
    public double escapeExtra = 7;
    @ConfigOption(label="Path refresh interval", description="Ticks between pathfinding attempts. Twenty ticks is one second.", min=1, max=200)
    public int repathTicks = 10;
    @ConfigOption(label="Debuff auras", description="Apply the three independently configurable effects to selected aura targets.")
    public boolean aurasEnabled = true;
    @ConfigOption(label="Campfire attraction", description="Selected investigation mobs approach this type of fire.")
    public boolean attractionEnabled = true;
    @ConfigOption(label="Attraction range", description="Spherical distance from which investigation mobs notice the fire.", min=0, max=128)
    public double attractionRange = 15;
    @ConfigOption(label="Require line of sight", description="Require clear sight to notice or continue investigating the fire.")
    public boolean requireLineOfSight = true;
    @ConfigOption(label="Destroy campfires", description="Let investigating mobs destroy the fire after the configured interval.")
    public boolean destroyEnabled = true;
    @ConfigOption(label="Investigate unlit fires", description="Allow investigation and destruction even when the campfire is unlit.")
    public boolean destroyUnlit = false;
    @ConfigOption(label="Destruction time (seconds)", description="Uninterrupted time in reach needed to destroy one campfire.", min=0.05, max=120)
    public double breakSeconds = 5;
    @ConfigOption(label="Destruction reach", description="Distance in blocks required to begin breaking.", min=0.5, max=8)
    public double breakReach = 2.5;
    @ConfigOption(label="Approach speed multiplier", description="Movement speed multiplier while investigating.", min=0.1, max=5)
    public double approachSpeed = 1;
    @ConfigOption(label="Drop destroyed campfire loot", description="Use normal block loot when a mob destroys a campfire.")
    public boolean breakDrops = true;
    @ConfigOption(label="Show breaking overlay", description="Show block cracking progress during a destruction attempt.")
    public boolean breakingOverlay = true;
    @ConfigOption(label="Show breaking swings", description="Animate the investigating mob swinging while breaking.")
    public boolean breakingSwing = true;
    @ConfigOption(label="Reduce patrol attempts", description="Only veto existing vanilla patrol attempts; never request additional spawns.")
    public boolean patrolSuppression = true;
    @ConfigOption(label="Patrol suppression range", description="Distance from lit fires in which vanilla patrol attempts can be vetoed.", min=0, max=128)
    public double patrolRange = 60;
    @ConfigOption(label="Patrol reduction (%)", description="Chance to veto an existing vanilla patrol-member spawn attempt.", min=0, max=100)
    public double patrolReductionPercent = 50;
        public EffectSettings slowness = new EffectSettings("minecraft:slowness", 15, 1.5);
        public EffectSettings weakness = new EffectSettings("minecraft:weakness", 15);
        public EffectSettings glowing = new EffectSettings("minecraft:glowing", 30);
        public Map<String, Boolean> fearMobs = new LinkedHashMap<>();
        public Map<String, Boolean> auraMobs = new LinkedHashMap<>();
        public Map<String, Boolean> attractionMobs = new LinkedHashMap<>();
        public FireSettings() {}
        public FireSettings(boolean soul) { attractionEnabled = destroyEnabled = !soul; }
        public List<EffectSettings> effects() { return List.of(slowness, weakness, glowing); }
        public double maxEffectRange() { return effects().stream().filter(e -> e.enabled).mapToDouble(e -> e.range).max().orElse(0); }
    }
    public static final class EffectSettings {
    @ConfigOption(label="Enabled", description="Apply this effect to selected aura targets.")
    public boolean enabled = true;
    @ConfigOption(label="Effect ID", description="Vanilla or installed modded effect identifier, for example minecraft:slowness.")
    public String effectId = "minecraft:slowness";
    @ConfigOption(label="Effect level", description="One means level I. Slowness supports fractional levels with exact speed interpolation; other effects use the whole-number part.", min=1, max=255)
    public double level = 1;
    @ConfigOption(label="Effect range", description="Spherical radius in blocks for this effect.", min=0, max=128)
    public double range = 20;
    @ConfigOption(label="Effect particles", description="Show status-effect particles on affected mobs.")
    public boolean showParticles = false;
    @ConfigOption(label="Effect icon", description="Show the effect icon where Minecraft displays status-effect icons.")
    public boolean showIcon = false;
        public EffectSettings() {}
        public EffectSettings(String id, double radius) { effectId = id; range = radius; }
        public EffectSettings(String id, double radius, double strength) { effectId=id;range=radius;level=strength; }
    }
    public FireSettings fire(boolean soulFire) { return soulFire ? soul : regular; }
    public double maxFearRange() { return Math.max(regular.fearRange, soul.fearRange); }
    public double maxAttractionRange() { return Math.max(regular.attractionRange, soul.attractionRange); }
    public double maxAuraRange() { return Math.max(regular.maxEffectRange(), soul.maxEffectRange()); }
    public double maxPatrolRange() { return Math.max(regular.patrolRange, soul.patrolRange); }
    public static FirewardConfig current() { return serverOverride == null ? active : serverOverride; }
    public static FirewardConfig copy(FirewardConfig config) { return fromJson(GSON.toJson(config)); }
    public static boolean same(FirewardConfig a, FirewardConfig b) { return GSON.toJsonTree(a).equals(GSON.toJsonTree(b)); }
    public static String toJson(FirewardConfig config) { return GSON.toJson(config); }
    public static FirewardConfig fromJson(String json) {
        JsonElement supplied=JsonParser.parseString(json);
        if(!supplied.isJsonObject())throw new IllegalArgumentException("Expected a config object");
        JsonObject merged=GSON.toJsonTree(new FirewardConfig()).getAsJsonObject();
        JsonObject input=supplied.getAsJsonObject();
        boolean legacy=!input.has("configVersion") || input.get("configVersion").getAsInt()<2;
        merge(merged,input);
        if(legacy) {
            migrateNumber(merged,"restrictionRange",60,40);
            for(String kind:List.of("regular","soul")) {
                if(!merged.get(kind).isJsonObject())continue;
                JsonObject fire=merged.getAsJsonObject(kind);
                migrateNumber(fire,"attractionRange",30,15);
                if(fire.has("destroyUnlit") && !fire.get("destroyUnlit").isJsonNull() && fire.get("destroyUnlit").getAsBoolean())fire.addProperty("destroyUnlit",false);
                if(fire.has("slowness") && fire.get("slowness").isJsonObject()) {
                    migrateNumber(fire.getAsJsonObject("slowness"),"range",20,15);
                    migrateNumber(fire.getAsJsonObject("slowness"),"level",1,1.5);
                }
                if(fire.has("weakness") && fire.get("weakness").isJsonObject())migrateNumber(fire.getAsJsonObject("weakness"),"range",20,15);
            }
            merged.addProperty("configVersion",2);
        }
        FirewardConfig config = GSON.fromJson(merged, FirewardConfig.class);
        if (config == null) throw new IllegalArgumentException("Empty Fireward configuration");
        config.normalize(); return config;
    }
    private static void migrateNumber(JsonObject object,String key,double oldDefault,double newDefault) {
        JsonElement value=object.get(key);
        if(value!=null && !value.isJsonNull() && value.getAsDouble()==oldDefault)object.addProperty(key,newDefault);
    }
    private static void merge(JsonObject target,JsonObject source) {
        for(var entry:source.entrySet()) {var prior=target.get(entry.getKey());var value=entry.getValue();
            if(prior!=null && prior.isJsonObject() && value.isJsonObject())merge(prior.getAsJsonObject(),value.getAsJsonObject());
            else target.add(entry.getKey(),value);
        }
    }
    public boolean supports(String id) {
        return (!id.startsWith("friendsandfoes:") || friendsAndFoes)
            && (!id.startsWith("takesapillage:") || takesAPillage);
    }
    public void normalize() {
        FirewardConfig defaults = new FirewardConfig();
        if (regular == null) regular = defaults.regular;
        if (soul == null) soul = defaults.soul;
        if (watchingMobs == null) watchingMobs = new LinkedHashMap<>();
        if (warningText == null) warningText = defaults.warningText;
        if (warningColor == null || FirewardConfig.color(warningColor) == null
                || FirewardConfig.color(warningColor).ordinal()>ChatFormatting.WHITE.ordinal()) warningColor = "white";
        watchingMobs.values().removeIf(Objects::isNull);
        clamp(this);
        for (FireSettings fire : List.of(regular, soul)) {
            if (fire.fearMobs == null) fire.fearMobs = new LinkedHashMap<>();
            if (fire.auraMobs == null) fire.auraMobs = new LinkedHashMap<>();
            if (fire.attractionMobs == null) fire.attractionMobs = new LinkedHashMap<>();
            if (fire.slowness == null) fire.slowness = new EffectSettings("minecraft:slowness",15,1.5);
            if (fire.weakness == null) fire.weakness = new EffectSettings("minecraft:weakness",15);
            if (fire.glowing == null) fire.glowing = new EffectSettings("minecraft:glowing",30);
            for(var map:List.of(fire.fearMobs,fire.auraMobs,fire.attractionMobs))map.values().removeIf(Objects::isNull);
            clamp(fire);
            for (EffectSettings effect : fire.effects()) {
                clamp(effect);
                if (effect.effectId == null) effect.effectId = "minecraft:slowness";
            }
        }
    }
    private static void clamp(Object target) {
        for (Field field : target.getClass().getFields()) {
            ConfigOption option = field.getAnnotation(ConfigOption.class);
            if (option == null || !(field.getType() == double.class || field.getType() == int.class)) continue;
            try {
                double value = ((Number)field.get(target)).doubleValue();
                if (!Double.isFinite(value)) value = option.min();
                value = Math.max(option.min(), Math.min(option.max(), value));
                if (field.getType() == int.class) field.setInt(target,(int)value); else field.setDouble(target,value);
            } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
    }
    public static void load() {
        try {
            editable = Files.exists(FILE) ? fromJson(Files.readString(FILE)) : new FirewardConfig();
            save(); active = copy(editable);
        } catch (Exception failure) {
            LoggerFactory.getLogger("fireward").error("Cannot read Fireward config; preserving original and using defaults",failure);
            editable = new FirewardConfig(); active = copy(editable);
        }
    }
    public static void save() {
        editable.normalize();
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = FILE.resolveSibling(FILE.getFileName()+".tmp");
            Files.writeString(temporary,toJson(editable));
            Files.move(temporary,FILE,StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception failure) { throw new IllegalStateException("Cannot save Fireward config",failure); }
    }
    public static ChatFormatting color(String name) {
        try {ChatFormatting color=ChatFormatting.valueOf(name.toUpperCase(Locale.ROOT));return color.ordinal()<=ChatFormatting.WHITE.ordinal()?color:null;}
        catch(RuntimeException invalid){return null;}
    }
    public static boolean validEffect(String id) {
        Identifier identifier = Identifier.tryParse(id);
        return identifier != null && BuiltInRegistries.MOB_EFFECT.getOptional(identifier).isPresent();
    }
}
