# Fireward 1.1.3 — revised vertical restrictions

- Dangerous areas restrict lit placement/ignition within 30 horizontal blocks and less than 10 vertical blocks from structure bounds. At exactly 10 blocks above/below, placement and ignition are allowed.
- Campfire behavior is limited to 10 blocks above/below the campfire block (inclusive). Phantoms retain their configured spherical range. This applies to fear, auras, attraction, villager shelter, patrol suppression, and watcher checks.
- Added independent General toggles: Dangerous area restrictions, No fires in the End, Limit vertical effects. All default ON; saving requires reopening the world.
- End restriction also disables ward behavior for any pre-existing End campfires. Lit placement extinguishes; rejected ignition and extinguishing emit four snowflakes and a quiet sizzle, rate limited per campfire.
- Item tooltips describe End restrictions, vertical reach, and dangerous-area categories.
- Compatibility shortcuts and General support toggles are disabled/grayed out for absent mods.
- Structure checks use loaded chunks without forcing generation.
- Retain previous mob defaults, warnings, and tooltip deduplication.

## Known limitation
Monster-room bounds use a spawner-based estimate (five blocks horizontally and four vertically). Player-placed spawners also restrict fires, and destroying the spawner removes this fallback. The 10-block vertical clearance is measured from the estimated room bounds, not the spawner itself.

## Validation
Minecraft class signatures and resource constants inspected. ZIP integrity checked. Build and in-game tests not run: this environment lacks Java 25 and cannot download Gradle.

## Focused test checklist
- Build using Java 25: .\gradlew.bat build.
- Test 9, 10 and 11 blocks above/below a stronghold's bounding box: restricted at 9, allowed at 10/11. Repeat with a room/spawner using the estimated bounds.
- Test mobs 10 and 11 blocks vertically from a campfire; at 11, no fear/aura/attraction. Phantoms retain normal range.
- Test surface fires over deeply buried strongholds/monster rooms: no structure warning.
- Toggle each new option independently, save and reopen the world; verify tooltips and behavior reflect it.
- In The End, test lit placement and flint-and-steel, fire-charge, Spark and Sift, and flaming-arrow ignition. Expect warning, four snowflakes and a quiet sizzle; no persistent smoke from unlit fires.
- Confirm General compatibility toggles and shortcut buttons are gray and disabled when each mod is absent.
- Repeat in singleplayer and on a dedicated server.
