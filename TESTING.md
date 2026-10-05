# In-game checks for Fireward 1.1.1

Use a disposable flat world and default settings. Place fires before summoning illagers. Creative observers do not become combat targets. See VALIDATION.md for completed server checks.

1. Confirm the supplied shield/campfire icon appears in Mod Menu. Open Fireward settings; check scrolling, search, decimal Slowness fields, Save/Back, and the world-reopen notice.
2. Spawn zombies, skeletons, creepers, spiders, and vexes within 20 blocks of a player-placed lit fire. Check regular piglin exemptions and soul-fire piglin/hoglin retreat. Terrain can block escape.
3. Spawn an illager within 15 blocks: Slowness 1.5 and Weakness I should apply. Glowing extends to 30 blocks. Fractional Slowness displays vanilla Slowness I but reduces speed by 22.5%. External Slowness II or stronger should win.
4. Only lit regular fires should attract illagers within 15 blocks with line of sight. Unlit fires should be ignored. Extinguish one mid-break; it must remain intact. Lit breaking takes five uninterrupted seconds at 20 TPS.
5. In Survival, a lone attacking illager should prioritize you. Add another illager attacking you within 10 blocks of the investigator; the group may instead break the lit fire. Non-aggressive allies and allies attacking another player must not count. Increasing distance beyond 10 blocks should restore player priority.
6. Within 40 blocks of a non-raid illager, lit placement and lighting should be denied without consumption. Unlit-default placement should work. At 41 blocks the restriction should not apply. Check the exact white action-bar warning and custom igniters from your pack.
7. Start a real raid near existing fires. Illagers should ignore the fire's auras, attention, breaking, and placement/ignition restrictions. Witches still receive configured auras and do not trigger setup prevention.
8. Put awake villagers inside the ward and summon a zombie nearby. They should move inside the ward, not become pinned to the fire. Summon a visible illager; they should flee normally. Repeat with soul fire, sleeping villagers, a blocked line of sight, extinguished fire, and no detected zombie.
9. Generated/command fires without the player marker should have no added behavior. Save/reopen and unload/reload marked fires; their eligibility should persist.
10. Normal tooltips should contain a few colored summary lines. Hold Shift to see translated effects, behavior details, and installed mob lists. Test multiple GUI scales.
11. Repeat with Friends & Foes and It Takes a Pillage Continuation. Check their supported illagers, raid membership, group counting, and campfire behavior. Their own AI may require compatibility.
12. Patrol suppression remains a veto of existing vanilla attempts (50% within 60 blocks by default). No extra patrols are created. A few spawns cannot establish the exact probability.

Sample commands:

```mcfunction
/summon minecraft:zombie ~6 ~ ~
/summon minecraft:villager ~4 ~ ~
/summon minecraft:pillager ~12 ~ ~
/summon minecraft:vindicator ~10 ~ ~
/summon minecraft:witch ~12 ~ ~
/summon minecraft:piglin ~6 ~ ~ {IsImmuneToZombification:1b}
/summon friendsandfoes:iceologer ~12 ~ ~
/summon takesapillage:skirmisher ~12 ~ ~
```
