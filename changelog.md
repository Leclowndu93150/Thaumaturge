------------------------------------------------------
Version unspecified
------------------------------------------------------
Additions
- API: wand rods can declare an IWandVisStorage to keep a wand's vis somewhere other than the wand_vis component. WandVisHelper and WandAccess read and write through it
- API: wand rods can declare an IWandRodOnAssemble callback that runs when the arcane workbench assembles a wand from them, so rod data can be copied onto the finished wand
- API: InfusionCraftedEvent fires when an infusion finishes and lets listeners change the result before it is placed on the central pedestal
- Added expanded focus crafting with spell parts, modifiers and affinities
- Added redesigned Eldritch labyrinth encounters and room generation
- Added complete decorative stone sets, connected textures and Eldritch corridor stairs
- Added Thaumometer scans of container inventories with Sneak+Use
- Added datapack customization for mob drops, equipment, infusion outcomes, altar bonuses and smelter stats

Changes
- taintSpreadRate is now the % chance of taint fibres spreading (default 100) and the Tainted Lands spread speed has moved to the new taintFrontierRate, as on 26.1.2. An existing taintSpreadRate setting is moved to taintFrontierRate the first time the game starts
- Improved performance of machines, aura nodes, spells and aspect indexing
- Expanded translations for commands, tooltips and screens
- Updated equipment models, textures and magical effects
- Updated Jade machine details and research descriptions

Bug Fixes
- Fixed machine and research actions accepting requests without the matching menu open
- Fixed item duplication in focus pouches, harnesses, sprayers and Arcane Bores
- Fixed block-changing spells and tools ignoring protection
- Fixed workbench ingredient, vis and crafting-remainder handling
- Fixed missing aura in existing chunks and stale aspects after datapack reloads
- Fixed discovered aspects missing from JEI after login
- Fixed vis relay sightlines and placement on walls, ceilings and partial supports (#469, #470)
- Fixed obsolete labyrinth mobs and missing rooms after chunk reloads
- Fixed taint spreading through protected blocks and repeated flux-gas movement
- Fixed stale client state after changing worlds and memory leaks in magical effects
- Fixed Arcane Bore animations, falling taint rendering and recharge-pedestal item placement (#473)
- Fixed research-table slots, Primal Charm tooltips and wrapped tooltip colors (#449, #481, #482)
- Fixed the FTB Library sidebar overlapping the Focal Manipulator (#483)
- Fixed target assignment for mobs spawned by warp events (#474)
