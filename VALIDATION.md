# Fireward 1.1.1 validation

Compiled against Minecraft 26.2, Java 25, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2, and optional Mod Menu 20.0.3. Dedicated-server startup and all 72 assertions passed in an isolated test copy. Live tick tests ran for 220 ticks, checking villager mobility, ward boundaries under sustained zombie threat, and resumed fleeing from illagers. Test helpers are excluded from the release.

## Passed checks

- Lit placement denied near illager
- Denied placement consumes no campfire
- Exact action-bar warning
- Unlit default placement allowed near illager
- Placed unlit fire is marked
- Watched flint-and-steel ignition rejected
- No ignition or tool wear
- Watched fire-charge ignition rejected
- No fire charge consumed
- Witch does not restrict placement or ignition
- Witch immune to fear
- Witch receives all three auras
- Non-raid illager receives aura
- Active raid membership grants immunity
- Raid illager has no campfire auras
- Raid illager ignores destruction goal
- Lit placement allowed near witch alone
- Default zombie fear enabled
- Configured fear radius respected
- Per-mob fear exclusion respected
- Per-mob fear inclusion respected
- Custom watcher restriction respected
- Configured watcher radius respected
- Ignition restriction toggle respected
- Custom aura effect and level respected
- Independent aura radius respected
- Raid immunity configurable
- Soul attraction configurable
- Unmarked fires excluded by default
- Optional natural fires indexed and enabled
- Configured destruction selects regular campfire
- Custom break time waits full interval
- Custom break time destroys on configured tick
- Master switch disables behavior
- Partial JSON keeps defaults and clamps limits
- Configuration round-trip
- Server settings packet round-trip
- Updated detection and restriction defaults
- Aura defaults 15 / Glowing 30 / fear 20
- Fractional slowness and lit-only defaults
- Unlit fire never selected by default
- Lit fire selected
- Extinguishing immediately ends investigation
- Extinguished fire preserved mid-break
- Slowness 1.5 reduces movement by exactly 22.5 percent
- Stronger external Slowness II preserved
- At 16 blocks only longer-range glow remains
- Raid entry clears fractional modifier immediately
- Raid illagers ignore fire investigation and placement
- Lone aggressive illager prioritizes player
- Two nearby illagers attacking same player allow campfire priority
- Aggressive group may investigate lit fire despite player target
- Group intent survives investigator clearing attack target
- Aggressive allies beyond ten blocks do not count
- Group exception can be disabled
- Visible zombie enables villager campfire refuge
- Villager unsafe escape redirected into ward
- Shelter path stays inside the ward
- Paths leaving the ward are rejected
- Safe wandering destination remains unchanged
- Visible illager overrides zombie comfort
- Sleeping villager is not redirected
- No detected zombies leaves villagers unrestricted
- Soul campfire also shelters villagers
- Villager shelter toggle respected
- Legacy defaults migrate to new behavior
- Custom legacy numbers are retained
- New schema retains intentional fractional and unlit overrides
- Live villager movement remains inside ward under sustained zombie threat
- Live villager remains mobile instead of pinned to fire
- Live villager releases campfire comfort around illager
- Live villager resumes fleeing visible illager

## Manual validation remaining

The config screen and tooltips compile but have not been visually rendered in this headless environment. Check GUI scales and tooltip placement in-game. Actual optional modded-mob AI, custom ignition mechanics, and remote multiplayer tooltip syncing require tests with the installed modpack. Sheltering and retreat remain pathfinding behaviors; impassable terrain and independent third-party AI can prevent movement.
