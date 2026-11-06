# THIS UPDATE WILL BREAK ALL OLD ADDONS
https://gist.github.com/Leclowndu93150/955cb128ed296b02d4cfada706b5e515 (changelog if the CF one cuts)
### Added

- Added armor trims: arcane and occult smithing templates, with vis crystals, brass, thaumium and void metal as trim materials. Crystal trims take their aspect's color, thaumium trims give a vis discount and void metal trims give a vis discount and warp.
- Trim templates drop from dungeons, loot bags and brainy husks, and wandering traders sell the arcane one. Modelled armors have their own trim textures.
- Added a Thaumonomicon entry for armor trims, and the Thaumonomicon shows smithing recipes.
- Crimson cultist armor can be dyed, and trimmed cultist armor keeps its red cloth.
- Added crimson ranger patrols led by a captain, replacing wild crimson portals. The captain opens a portal when it spots you and drops a map to an eldritch obelisk. Ranger armor by ordeaux.
- Added thaumium, mirror and void shields. Thaumium and void shields block magic, the mirror shield reflects projectiles and the void shield saps melee attackers. (#328)
- Tainted mobs have their own abilities: purple wool and flux from sheep, flux goo from cows, tainted eggs, pig swarms, warping zombies, taint arrows, taint brewing witches, lifting phantoms and enderman taintacle lures. (#500)
- Added the creative jar, holding infinite essentia of one aspect set with a phial.
- The brain in a jar powers enchanting tables, and Jade shows its XP level and stored XP.
- Outer lands guardians have a regenerating shield and a wisp trail.
- Conquered labyrinths rebuild with a new layout once every player has left for a while.
- Hoppers and pipes can feed the magic mirror.
- The Thaumometer shows what's inside crucibles, jars and other containers.
- Research table tooltips show aspect composition.
- Thaumonomicon and research table pages scroll with the mouse wheel.
- Added tooltips to the Arcane Workbench.
- Jade shows potion sprayer doses, potion and missing essentia, and the arcane spa fluid.
- Clicking node locate coordinates suggests a teleport command.
- Added `/thaumaturge scan <players> everything` to scan every item and mob at once.
- Added `/thaumaturge patrol` to spawn a crimson patrol and `/thaumaturge reservoir <aspects>` to give a filled essentia reservoir.
- Added flesh and tallow storage block tags and a curiosities item tag. (#543)

### Changed

- Taint grows from taint seeds that feed on flux instead of spreading forever. Tainted nodes feed on flux like seeds, and tainted lands start full of flux.
- Flux goo, outbreaks and bottles of taint plant a taint seed instead of tainting land directly.
- Dead taint leaves clean ground instead of flux goo.
- Tainted mobs wither away from taint, and flux lightning hits softer.
- Taint seeds inside an ethereal bloom starve.
- Tainted animals from older worlds turn into vanilla animals with the taint trait.
- Missing infusion components cost essentia and stability, with sparks on the problem blocks. Infusions started before the update keep the old penalty.
- Dark nodes paint eerie in a 16 block disc and pure nodes paint magical forest in a disc instead of squares. (#480)
- Crafting and infusion recipes take vis crystals of the matching aspect.
- Cleaned up the auromancy research tree: crystal farmer moves to infusion and the primal staff needs every rod. By Atomizate.
- Candle holders moved under metallurgy and only need brass. (#544)
- The Amulet of Vis also recharges the item in your offhand.
- Beams flash where they hit and merge when cast from the same source.
- Traveller's boots steer better in the air.
- Golems are named after their material.
- Crab nests are crusted and riddled with vents, warrens get more vents and mazes roll more nests.
- The labyrinth arrival portal capstone stands one block up on obsidian.
- Item grates use iron trapdoor sounds.
- Updated the Simplified and Traditional Chinese translations, by Black_Moss. (#497)
- Improved performance of bolt effects, vis crystals, the Thaumometer readout and the infernal furnace.

### Models and Textures

- New void effect on eldritch portals, obelisks, pillars and the nothingness, with parallax star layers and a drifting nebula.
- New vis crystal cluster model and texture, by Bocha.
- New essentia reservoir model and texture, by Bocha.
- New essentia tube valve texture, by Bocha. (#522)
- New crimson cult armor: new leader, plate, robe and inhabited zombie textures, a new cleric armor model, reworked knight, praetor and robe models with swaying capes, and smaller crimson boots. By Atomizate and ordeaux.
- New thaumium fortress armor model and texture with a new plate helmet, by Atomizate. The goggles upgrade uses the cultist captain's lenses.
- New crab, cultist and taint swarm research icons, by Atomizate.
- Greatwood logs connect into 2x2 trunks with new wide log textures, by Wenbell.
- New mana pod model with new bean and stem textures, by DeadSix.
- New textures from Thaum pack by zozozrob for the essentia tubes, resonator, mechanisms, golem bell, foci, magic mirror, mirrored glass, crimson armor items and void robe armor.
- New bath salts, causality collapser, primal charm and primordial pearl textures.
- New textures for the advanced alchemical furnace, arcane door, arcane pressure plate, essentia crystallizer, flux gas, golem fetter, item grate, taint spore stalk, tallow block, arcane keys, purifying bucket and aura node bubble.
- New dioptra textures and new greatwood and silverwood door textures.
- Redrawn activator rails, crucible top, inlays, loot urn top, research table, arcane bore, creative flux sponge, jar labels, turrets and some particles, by Saereth.
- Re-exported focus effect icons, several aspect icons and research icons, by Saereth.
- Smoother texture animations on the lamps, eldritch altar, mana bean, primal charm, runed tablet and golem seals.
- New rune script and rift shard textures.
- Smoother Thaumonomicon category backgrounds.
- Warded glass uses a new connected texture. Silverwood planks are a plain block again.
- The research table inkwell has its own model, and the research table item shows its paper stack.
- The void jar only has a void lid now, the body keeps the plain jar top.
- The advanced alchemical furnace keeps its top opening and its top is lit from above.
- Banner emblems follow both cloth bends and sit on the cloth. Banners no longer vanish when their base is off screen, and the standing banner hitbox follows the pole.
- Locked research items are darkened instead of drawn on a black square.
- Fixed the taint growth texture.

### Fixed

#### Essentia and alchemy

- Fixed filled essentia jars looking empty when held or in the inventory.
- Fixed essentia in jars, lava in the golem press, the advanced alchemical furnace goo and the eldritch reliquary panel drawing a piece of the texture atlas instead of their own texture.
- Fixed jar labels tilting more on one side of the world than the other.
- Fixed phials acting as if they worked on jars holding a different essentia or with a non-matching label.
- Fixed essentia ports pulling essentia out of jars when the tube couldn't take it.
- Fixed essentia buffers getting blocked by neighboring blocks they aren't connected to.
- Fixed essentia buffers not showing their suction bands. Clicking the core face now picks that side. (#530)
- Fixed the resonator on an essentia buffer reporting the wrong arm.
- Fixed the thaumatorium drawing essentia after it stopped working.
- Fixed the crucible crafting a whole thrown-in stack after its water ran out.
- Fixed essentia leaking from a crucible sometimes vanishing without making flux, or making flux twice.
- Fixed an overflowing crucible giving a comparator signal above 15.
- Fixed crucible water and the arcane spa gauge ignoring the biome water color.
- Fixed splitting quicksilver, nuggets and storage blocks giving more essentia than the whole item. (#516)
- Fixed polished, smooth, cut and chiseled stone, deepslate, sandstone and quartz carrying too little ordo. (#511)
- Fixed the colors of ten aspects, by Bocha.
- Fixed the brain in a jar counting merged experience orbs as a single orb.

#### Devices

- Fixed the Arcane Bore rendering pure white. (#549)
- Fixed right-clicking your own arcane bore or crossbow turret also using the item in your hand.
- Fixed arcane bores and turrets barely moving when pushed by pistons.
- Fixed the Lamp of Fertility breeding animals without enough essentia.
- Fixed inlays not updating when their stabilizer charged or drained.
- Fixed magic mirrors force-loading the linked mirror's chunk when items were thrown in.
- Fixed the pattern crafter being placed facing away from you.
- Fixed the potion sprayer firing on its own when its chunk loaded while powered.
- Fixed the condenser lattice losing its shape when it turned dirty.
- Fixed the iron bars left by a broken infernal furnace not connecting to their neighbors.
- Fixed the focal manipulator's floating focus disappearing when the table was off screen.
- Fixed the focal manipulator reverting an edit while the server catches up.
- Fixed the levitator changing its range from any face. Only the back face does now, so blocks can be placed against it.
- Fixed barrier stone runes always showing red. They only show while blocking a mob, purple when working and red when broken. (#540)
- Fixed the infusion matrix doing nothing on a locked recipe. It now tells you which research it needs. (#539)
- Fixed the wand floating above the Arcane Workbench. (#531)
- Fixed the Arcane Workbench crystal glows sitting at uneven angles and jumping.
- Fixed the earthmover not breaking its 3x3 in creative.
- Fixed hungry nodes speeding up items endlessly in chunks they don't tick. (#506)
- Fixed the brass scan only accepting Thaumaturge brass. Any ingot tagged as brass works now.
- Fixed labels swinging your arm on blocks that can't take a label.

#### Wands, tools and equipment

- Fixed wands being unable to cast or pick blocks while aimed at glass, and glass not being wardable.
- Fixed thaumium, void and elemental axes not stripping logs, and thaumium and void shovels not making paths. (#541)
- Thaumaturge hoes and the Primal Crusher mine taint faster, and silk touch drops taint fibres and spore stalks. (#547)
- Fixed enchantments from other mods being ignored by the Arcane Bore, golem looting and the infusion silk touch check.
- Fixed the extra blocks broken by infusion-enchanted tools and the felling seal ignoring other mods' harvest rules.
- Fixed warping gear held in the offhand not counting toward warp.
- Fixed the Thaumometer losing its lens readout when the other hand holds an item. (#507)
- Fixed very long names on the Thaumometer lens shrinking to nothing or flipping.
- Fixed the wand HUD vis arrows flashing when switching wands.
- Fixed the wand HUD showing a picked block for foci that don't use one.
- Fixed grappler hooks staying in the world when their owner logged out or died mid-pull.
- Fixed salis mundus sparkles coming from the wrong hand for left-handed players.
- Fixed purifying fluid not acting like water for swimming, putting out fire and breaking falls.
- Fixed flux goo, liquid death and the other Thaumaturge fluids stopping all movement. (#492)

#### Golems

- Fixed the golem press taking too many components when they were split between nearby chests and your inventory.
- Fixed the golem press not drawing machina from a tube when an earlier side had an empty essentia block.
- Fixed the golem press getting stuck, and possibly losing the golem, when its output slot was blocked.
- Fixed the breaker seal whitelist only working with one block in the filter.
- Fixed breaker seal golems trying to break water and lava.
- Fixed harvest seals forgetting what to replant after a reload.
- Fixed golems with no saved home walking toward 0, 0, 0.
- Fixed the golem logistics screen scrolling one empty row past the end.

#### Aura, nodes and taint

- Fixed nodes drained empty by a stronger neighbor staying around instead of collapsing.
- Fixed nodes generating over water.
- Fixed removing a pure or dark node not giving back the biome it painted over. (#546)
- Fixed walking through taint fibres never exposing you to taint.
- Fixed taint swarm bites never dealing their bonus damage.
- Fixed taint crust creeping sideways into solid blocks.
- Fixed taint near the edge of loaded areas dying back.
- Fixed taint seeds giving flux taint to spectators.
- Fixed bottles of taint and taint crawlers spreading fibres with wuss mode on.
- Fixed a second collapse event shrinking the rewards of a flux rift that was already collapsing.
- Fixed flux rifts spawning wisps and taint seeds into unloaded chunks.
- Fixed flux rifts picking a spot below the world in skyless dimensions.
- Fixed invisible sap clouds catching clicks and showing a block outline.

#### Mobs and bosses

- Fixed eldritch crabs latched onto you being invisible to you, out of place and impossible to hit.
- Fixed anything killed by an inhabited zombie dropping nothing. Players killed by one lost their whole inventory.
- Fixed the crab bursting out of an inhabited zombie facing the wrong way.
- Fixed wisps treating moving players as standing still.
- Fixed lesser cultist portals spawning cultists while dying.
- Fixed energy orbs from cultists and the Eldritch Golem not being deflectable. They also no longer hit each other or survive a reload.
- Fixed bosses getting enraged by hits they were immune to, or after dying.
- Fixed the Eldritch Golem losing its head from hits taken while dying.
- Fixed the Eldritch Golem's awakening and the Eldritch Warden's emergence not showing.
- Fixed bosses switching targets to players in creative or spectator mode.
- Fixed the Eldritch Warden's sap ring lasting much longer than intended.
- Fixed mobs spawned by Thaumaturge skipping other mods' spawn hooks.

#### Eldritch and labyrinths

- Fixed labyrinths failing to generate rooms with corner stairs in the doorway, and wrong stair corners in mirrored rooms.
- Fixed labyrinth rooms sitting empty. Crab vents, loot, traps and guard posts are back.
- Fixed holes in labyrinth floors, wall niches opening without loot and crab vents sticking out of walls.
- Fixed one-block grooves left in labyrinth ceilings, cornices not wrapping corners and pillars not reaching the ceiling.
- Fixed the boss room door and lock sitting off center and facing away from the corridor.
- Fixed labyrinth minions being lost when the game saved right after they spawned.
- Fixed the outer lands obelisk sitting one block too high, and eldritch portals not catching players pressed against them.
- Fixed eldritch obelisks, portals, the nothingness, flux rifts and portable holes losing their void effect under shader packs. (#606)
- Fixed the eldritch lock's portal drawing in the wrong plane when facing up or down.
- Fixed crimson portals rendering upside down. (#495)

#### World generation and compatibility

- Fixed greatwood, silverwood, mounds and obelisks not generating in modded biomes. Mystic Grove is now full of silverwood. (#537)
- Fixed Dynamic Trees silverwood and greatwood growing undersized with thin trunks, missing their drops and names, and generating where they have no room. Silverwood grows shimmerleaf around it again. (#538)
- Fixed mana pods floating under thin Dynamic Trees branches, and their placement and collision on Dynamic Trees. (#538)
- Fixed mana pod stems having collision.
- Fixed the Thaumonomicon and Crimson Rites not counting as books for bookshelves, bookcases and chiseled bookshelves. (#552)
- Fixed a crash with JEI 29.47 and newer. (#518)
- Fixed JEI aspect pages not listing every item with that aspect in singleplayer. (#569)
- Fixed JEI not listing the thaumium and void smelters as smelting stations.
- Fixed Jade showing time left instead of progress for the infernal furnace.
- Fixed a crash on Thaumonomicon multiblock previews with fluids when a light engine mod is installed. (#545)

#### Research and Thaumonomicon

- Fixed the research reset command not resetting the aspect pool. (#488)
- Fixed the Thaumonomicon only unlocking when picked up, not when used.
- Fixed research text running outside the book pages.
- Fixed aspect lists on Thaumonomicon crucible and infusion pages shifting sideways when the last row was full.
- Fixed the knowledge types grid showing on every page of its entry.
- Fixed the research table recipe asking for one wooden table instead of two in some translations. (#513)
- Fixed the out of ink text drawing a shadow.
- Fixed vis crystal hitboxes not matching the crystals.

#### Effects

- Fixed the node drain beam disappearing when the node was off screen.
- Fixed infusion crumbs rendering solid instead of faint.
- Fixed wispy lines from node drains, recharge pedestals and cultist clerics stuttering in old worlds.
- Fixed essentia, bore and void streams whipping around next to their target.
- Fixed the causality collapser trail not matching its purple texture.
