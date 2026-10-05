# 1.1.1

- Added the supplied shield-and-campfire mod icon.
- Illagers investigate and destroy lit campfires only by default; extinguishing cancels breaking immediately.
- Default attraction and Slowness/Weakness ranges are 15 blocks. Glowing remains 30; hostile fear remains 20.
- Reduced placement/lighting prevention to 40 blocks. Raid-immune illagers no longer prevent setup.
- Players retain priority unless two or more nearby illagers within 10 blocks are already aggressive toward the same player.
- Default Slowness is 1.5: exact 22.5% movement-speed reduction, respecting stronger external effects.
- Awake villagers seeing zombies use reachable ward space around either lit fire and keep moving freely. Visible illagers override shelter; sleep and loss of detection release it.
- Added configurable group and villager behavior; decimal Slowness levels are supported.
- Shortened normal tooltips to colored summaries; Shift reveals details and dynamic mob lists.
- Migrates matching legacy default config values once while retaining other custom numbers.
