------------------------------------------------------
Version unspecified
------------------------------------------------------
Additions
- API: wand rods can declare an IWandVisStorage to keep a wand's vis somewhere other than the wand_vis component. WandVisHelper and WandAccess read and write through it
- API: wand rods can declare an IWandRodOnAssemble callback that runs when the arcane workbench assembles a wand from them, so rod data can be copied onto the finished wand
- API: InfusionCraftedEvent fires when an infusion finishes and lets listeners change the result before it is placed on the central pedestal

Changes
- taintSpreadRate is now the % chance of taint fibres spreading (default 100) and the Tainted Lands spread speed has moved to the new taintFrontierRate, as on 26.1.2. An existing taintSpreadRate setting is moved to taintFrontierRate the first time the game starts

Bug Fixes
- None
