# Fireward configuration reference

All fields below have corresponding screen controls. Distances are spherical block radii. Slowness supports decimal levels; other effect levels use their integer part. Values are validated in the GUI and normalized when loading JSON.

## General settings

| JSON key | Default | Allowed values | Meaning |
|---|---|---|---|
| `enabled` | `true` | true / false | Master switch for all added gameplay behavior and restrictions. |
| `includeNatural` | `false` | true / false | Also affect generated, command-created, and pre-existing fires. Disabled by default to protect structures. |
| `friendsAndFoes` | `true` | true / false | Allow Fireward rules to affect Friends & Foes mobs. |
| `takesAPillage` | `true` | true / false | Allow Fireward rules to affect It Takes a Pillage Continuation mobs. |
| `raidImmunity` | `true` | true / false | Raid illagers ignore fear, auras, attraction, and destruction. Witches still receive configured auras. |
| `raidAreaImmunity` | `true` | true / false | Also grant immunity to illagers inside an active raid area, even without registered raid membership. |
| `playerPriority` | `true` | true / false | Visible attackable players interrupt campfire investigation and reset breaking progress. |
| `groupException` | `true` | true / false | Two or more nearby illagers fighting the same player can prioritize a lit campfire instead. |
| `groupSize` | `2` | 2–32 | Minimum illagers already fighting the same player, including this illager. |
| `groupRange` | `10` | 0–128 | Distance around the investigating illager used to count aggressive allies. |
| `groupMemorySeconds` | `5` | 0.05–120 | Briefly retain player aggression while a group member investigates a fire; avoids rapid switching between combat and breaking. |
| `villagerShelter` | `true` | true / false | Awake villagers seeing zombies prefer the ward area while remaining free to move. Visible illagers always override shelter. |
| `villagerSearchRange` | `32` | 0–128 | How far a threatened villager searches for an eligible lit fire. |
| `villagerThreatRange` | `16` | 0–128 | Maximum distance to a visible zombie or illager used by the shelter behavior. |
| `villagerShelterSpeed` | `0.5` | 0.1–5 | Movement speed multiplier while moving within the ward. |
| `villagerEdgeMargin` | `1` | 0–16 | Keep shelter destinations this far inside the configured fear radius. |
| `villagerFireClearance` | `2.5` | 1.5–8 | Keep shelter destinations away from the campfire block to avoid standing on flames. |
| `villagerRepathTicks` | `20` | 1–200 | Ticks between shelter path adjustments. Vanilla perception and illager danger are still checked every tick. |
| `villagerPathAttempts` | `8` | 1–48 | Maximum reachable shelter candidates tried per refresh, limiting pathfinding work in large villages. |
| `playerPriorityRange` | `32` | 0–128 | Distance searched for visible players before investigating fires. |
| `respectMobGriefing` | `true` | true / false | Prevent campfire destruction when the vanilla mobGriefing rule is false. |
| `restrictionEnabled` | `true` | true / false | Prevent lit placement and lighting near selected watching mobs. |
| `restrictionRange` | `40` | 0–128 | Spherical distance for refusing lit placement or lighting. |
| `restrictionThroughWalls` | `true` | true / false | Watching mobs need no line of sight. Disable to require sight of the campfire location. |
| `blockLitPlacement` | `true` | true / false | Deny campfires whose actual placement state is lit. Unlit-default placement remains allowed. |
| `blockIgnition` | `true` | true / false | Deny item use on watched, marked, unlit campfires before resources are consumed. |
| `warningEnabled` | `true` | true / false | Display the configured action-bar warning when an attempt is denied. |
| `warningText` | `"I shouldn't setup here, I have a feeling I'm being watched"` | text / registered identifier | Text displayed above the hotbar when placement or lighting is denied. |
| `warningColor` | `"white"` | text / registered identifier | Minecraft color name: white, red, gold, yellow, green, aqua, blue, light_purple, gray, or another standard color. |
| `tooltipsEnabled` | `true` | true / false | Show the current configured behavior on campfire item tooltips. |
| `showMobLists` | `true` | true / false | Include lists of affected mobs and exceptions based on installed mobs and configured rules. |
| `shiftForLists` | `true` | true / false | Keep normal tooltips short; reveal effects, behavior details, and mob lists while Shift is held. |

## Per-fire settings

Under `regular` and `soul`. Soul defaults disable attraction and destruction. Unlit investigation is OFF for both types.

| JSON key | Default | Allowed values | Meaning |
|---|---|---|---|
| `enabled` | `true` | true / false | Enable added gameplay behavior for this campfire type. |
| `fearEnabled` | `true` | true / false | Make selected mobs retreat from lit fires. |
| `fearRange` | `20` | 0–128 | Spherical radius in blocks for starting a retreat. |
| `fleeSpeed` | `1.3` | 0.1–5 | Movement speed multiplier while retreating. |
| `escapeBuffer` | `4` | 0–32 | Extra distance to retreat past the fear radius before normal AI resumes. |
| `escapeExtra` | `7` | 0–64 | Extra distance beyond the fear radius used for the escape destination. |
| `repathTicks` | `10` | 1–200 | Ticks between pathfinding attempts. Twenty ticks is one second. |
| `aurasEnabled` | `true` | true / false | Apply the three independently configurable effects to selected aura targets. |
| `attractionEnabled` | `true` | true / false | Selected investigation mobs approach this type of fire. |
| `attractionRange` | `15` | 0–128 | Spherical distance from which investigation mobs notice the fire. |
| `requireLineOfSight` | `true` | true / false | Require clear sight to notice or continue investigating the fire. |
| `destroyEnabled` | `true` | true / false | Let investigating mobs destroy the fire after the configured interval. |
| `destroyUnlit` | `false` | true / false | Allow investigation and destruction even when the campfire is unlit. |
| `breakSeconds` | `5` | 0.05–120 | Uninterrupted time in reach needed to destroy one campfire. |
| `breakReach` | `2.5` | 0.5–8 | Distance in blocks required to begin breaking. |
| `approachSpeed` | `1` | 0.1–5 | Movement speed multiplier while investigating. |
| `breakDrops` | `true` | true / false | Use normal block loot when a mob destroys a campfire. |
| `breakingOverlay` | `true` | true / false | Show block cracking progress during a destruction attempt. |
| `breakingSwing` | `true` | true / false | Animate the investigating mob swinging while breaking. |
| `patrolSuppression` | `true` | true / false | Only veto existing vanilla patrol attempts; never request additional spawns. |
| `patrolRange` | `60` | 0–128 | Distance from lit fires in which vanilla patrol attempts can be vetoed. |
| `patrolReductionPercent` | `50` | 0–100 | Chance to veto an existing vanilla patrol-member spawn attempt. |

## Independent effect slots

Each fire has `slowness`, `weakness`, and `glowing` slots. The JSON key stays fixed even when the effect ID changes. Slowness defaults to level 1.5/range 15; Weakness to level 1/range 15; Glowing to level 1/range 30. Default effect IDs are their respective vanilla minecraft IDs.

| JSON key | Default | Allowed values | Meaning |
|---|---|---|---|
| `enabled` | `true` | true / false | Apply this effect to selected aura targets. |
| `effectId` | `"minecraft:slowness"` | text / registered identifier | Vanilla or installed modded effect identifier, for example minecraft:slowness. |
| `level` | `1` | 1–255 | One means level I. Slowness supports fractional levels with exact speed interpolation; other effects use the whole-number part. |
| `range` | `20` | 0–128 | Spherical radius in blocks for this effect. |
| `showParticles` | `false` | true / false | Show status-effect particles on affected mobs. |
| `showIcon` | `false` | true / false | Show the effect icon where Minecraft displays status-effect icons. |

## Mob overrides

`regular.fearMobs`, `soul.fearMobs`, `regular.auraMobs`, `soul.auraMobs`, `regular.attractionMobs`, `soul.attractionMobs`, and top-level `watchingMobs` map registered entity IDs to true/false. Missing keys mean AUTO. Example:

```json
"fearMobs": { "minecraft:zombie": false, "minecraft:witch": true }
```

Overrides remain subject to the master, per-fire, per-behavior, mod-support, and raid-immunity switches. Attraction overrides do not enable a disabled attraction switch. Fear wins over investigation when an entity is included in both. Soul auras do not depend on soul attraction. Villager shelter uses the configured fear radius and only a fire that repels every detected zombie; visible illagers always disable shelter. Sleeping and unthreatened villagers use vanilla movement.

## Decimal Slowness

A level of 1.5 produces an exact 22.5% reduction. Minecraft displays Slowness I while a transient movement modifier supplies the remaining fractional strength. Existing stronger Slowness supersedes the fractional modifier. Other effects round down to a whole level.

## Groups and raids

The group exception counts aggressive illagers within groupRange of the investigator, including the investigator, that are attacking the same player. Combat intent is retained for groupMemorySeconds while investigating so clearing a target does not instantly dissolve the group. Group size, radius, memory, and exception toggle are configurable. Raid-immune illagers have no fear, auras, attraction, breaking, or watcher restrictions. Witches retain their configured aura behavior.

## Migration and applying changes

Pre-version-2 configs migrate matching old defaults once. Explicit old default values are indistinguishable from unchanged defaults and are migrated too. The legacy destroyUnlit value is disabled. Other custom numbers are retained. Version-2 files preserve intentional overrides, including reenabling unlit investigation.

Save and reopen a singleplayer world, or edit the dedicated-server JSON and restart it. Active settings sync to clients on join. Remote configs are read-only. Generated/pre-existing campfires remain excluded by default. Overlapping patrol reductions use the highest configured percentage and never add spawns. Terrain or independent modded AI may prevent successful escape or shelter.
