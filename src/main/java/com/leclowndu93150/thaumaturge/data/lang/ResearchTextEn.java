package com.leclowndu93150.thaumaturge.data.lang;

import java.util.function.BiConsumer;

public final class ResearchTextEn {
    private ResearchTextEn() {}

    public static void addAll(BiConsumer<String, String> add) {
        part0(add);
        part1(add);
        part2(add);
        part3(add);
        part4(add);
    }

    private static void part0(BiConsumer<String, String> add) {
        add.accept("research.thaumaturge.first_steps.title", "First Steps");
        add.accept(
                "research.thaumaturge.first_steps.stage_0",
                "Who would have thought that a few sprinkles of dust could have created this marvellous book? It is mostly blank, but it hints at a powerful new form of magic: Thaumaturgy.<BR>Firstly I will need to study the magical world that has been shown to me. For that I will need a tool - a Thaumometer the book calls it.<BR>A normal crafting table won't do however since the magical energies I need to channel while crafting it are quite delicate. A sprinkling of Salis Mundus on a mundane crafting table should solve that. The book has taught me the proper incantation to use so it should be a simple matter.<BR>To keep myself on track I will make a list of all the requirements that need to be met on the first page of each new entry. ");
        add.accept(
                "research.thaumaturge.first_steps.stage_1",
                "Progress! I now have an Arcane Workbench at my disposal. It works much like a mundane crafting table and can even be used as such, however there are a few key differences.<BR>Firstly, many arcane recipes will require the use of those vis crystals I've been finding of late. They act as a focus for the magical energy I need to channel into the items I craft. The book calls this magical energy Vis.<BR>Supposedly this energy is all around me, but as yet I cannot perceive or measure it.<BR>It is time I craft the Thaumometer and use it to study the world around me. It should clearly mark those things I still need to examine.<BR>I have placed a bookmark to the Thaumometer recipe on the right.");
        add.accept(
                "research.thaumaturge.first_steps.stage_2",
                "Such strange wonders are being revealed to me. Peering through the lens of the Thaumometer shows me a world coursing with magic, Vis as the books calls it.<BR>Normally this energy is a pure ocean of magic that ebbs and flows, but it also permeates all things, both animate and inanimate. The book calls Vis in this state Essentia. Where Vis is formless and pure, Essentia is more rigid in its nature and falls into a multitude of clearly defined types, each mixing together with others to form greater and more complex types of essentia.<BR>I am not quite sure if the matter it is bound to gives Essentia its nature, or that the very nature of matter depends on the Essentia it contains. This bears further study.<BR>I am keeping a log of all the types of essentia I discover. A bookmark on the left will show my current knowledge.<BR>Every object and creature I examine yields research points of the aspects it contains - these points are the currency of my studies.<BR>I have recorded everything I have learned about aspects and the process of research itself in two separate entries in this book: §5Aspects of Magic§0 and §5Research§0. I should study both before proceeding.");
        add.accept("research.thaumaturge.aspects.title", "Aspects of Magic");
        add.accept(
                "research.thaumaturge.aspects.stage_0",
                "Every item, object or creature is bound by varying mystical elements known as §5aspects§0. Aspects describe the object's makeup and bent towards the magical elements. A simple stone may possess the aspect of Terra while more complex items like trees or even creatures can be made up of various different aspects in varying amounts. There are dozens of known aspects with more being discovered all the time.<LINE>There are two main types of aspects: §5Primal§0 and §5Compound§0.<PAGE>§lPrimal Aspects§r are the most basic types of aspects and there are only 6 of them: §6Aer§0 (air), §2Terra§0 (earth), §cIgnis§0 (fire), §3Aqua§0 (water), §7Ordo§0 (order), §8Perditio§0 (entropy).<BR>These form the basic building blocks of all other aspects.<BR>§lCompound Aspects§r are built from two other aspects. The simplest compound aspects are made from two primal aspects, but it is possible to have incredibly complex compound aspects made from successive layers of simpler aspects.<BR>The simplest example of such a compound aspect is the aspect of Victus (life), which is a combination of §2Terra§0 and §3Aqua§0.<PAGE>To view what aspects an item possesses, simply hover your cursor over the item and press sneak (shift), and you will see what aspects that item has and what amounts of each. You will first need to study the item however, until then the aspects will remain unknown.<LINE>Aspects that have somehow been separated from their object and purified are known as §5Essentia§0 - a magical liquid that is both a valuable crafting ingredient and a subject for magical research.");
        add.accept("research.thaumaturge.research.title", "Research");
        add.accept(
                "research.thaumaturge.research.stage_0",
                "Before you can build most thaumaturgical items and blocks, you first need to discover how to do so. There are several steps in this process:<BR>(1) Exploring the world and examining blocks, items and creatures with a §5Thaumometer§0<BR><BR>(2) Taking the research points you gathered from the first step and expanding your aspect knowledge with the §5Research Table§0<BR><BR>(3) Using the aspects you know and discovering practical knowledge and recipes with the §5Research Table§0.<PAGE>§l      §n(1) Exploration§r<BR><BR>You cannot perform research without research points, and the best way to gain research points is by exploration and examining objects in the world.<BR>The first thing you will need to do is construct a §5Thaumometer§0 (the recipe is listed at the end of this entry). By pointing the §5Thaumometer§0 at objects or creatures and holding the right mouse button you will start examining them. The process does not take long, but your movement speed is reduced while it is occurring.<BR>Once complete the §5Thaumometer§0 will display what aspects the object contains, along with a summary of the aspects and research points <PAGE>you discovered.<IMG>thaumaturge:textures/research/research1.png:0:0:255:208:.5</IMG>You will initially not be able to examine all objects since you have not discovered their component aspects. For example, an object containing the Lux aspect can only be examined if you have previously discovered the Lux aspect, or know<PAGE>the two aspects that combine to create it.<BR>The very first time you discover an aspect by examining something you gain two more research points than you would have normally.<BR>Objects and creatures can usually only ever be examined once.<BR>Occasionally examining something will reveal additional clues and insights into thaumaturgical research, unlocking subjects that you can research further.<PAGE>§l   §n(2) Combining Aspects§r<BR><BR>Once you have gathered some research points you can attempt to combine them into compound aspects within a research table.<BR>To craft a research table simply place a §5wooden table§0 and click on it with a set of Scribing Tools. The recipes for both are listed at the end of this entry.<BR>If you open the research table's inventory, you will see an interface as pictured on the right.<BR>The scribing tools you used to craft the table will be located in slot §l1§r.<PAGE><IMG>thaumaturge:textures/research/research2.png:0:0:128:83:1</IMG>You should see all the primal aspects (and possibly some compound aspects) listed in the area marked §l3§r with varying amounts attached to them. When you enter a world for the first time, every player is assigned a small pool of primal aspects to get them started.<BR>If you had done some exploring with<PAGE>the thaumometer beforehand then you might have a significant number of primal and compound aspects listed here.<BR>You can now start combining aspects into more complex compound aspects by dragging those you wish to combine to the runic circles in area §l4§r. When you have two selected, pressing the purple button will attempt to combine them. If successful you should see what was gained in the bottom right and it should appear in area §l3§r.<BR>The component aspects are consumed even if the combination did not result in a valid compound aspect.<PAGE>§l   §n(3) Primary Research§r<IMG>thaumaturge:textures/research/research2.png:0:88:255:158:.5</IMG>You need to be carrying a set of scribing tools and paper for this step.<BR>If you open your Thaumonomicon you will see all the knowledge you have and also all the knowledge that you are now able to pursue.<BR>Research that must be worked out on paper lists its theories on the entry's requirement page.<PAGE>Clicking a listed theory will consume paper and ink from your inventory and you will gain a research note. You can now take this research note to the table to start researching it by placing it in slot §l2§r.<BR>Section §l5§r will now display a sheet of paper covered in hexagonal tiles with aspect icons around the outer edge. If you see question mark icons it means you do not know that aspect yet and you will need to learn it first.<PAGE>To complete your research you simply need to connect and activate all the aspects. You do this by dragging an aspect to a hex adjacent to it that is either composed of the target aspect, or can be combined with another aspect to form the target aspect.<IMG>thaumaturge:textures/research/research3.png:0:0:150:95:.75</IMG>Placing an aspect consumes one research point. If you make a mistake you can click on the aspect to remove it, but you will not regain the research point.<PAGE>You continue doing this until you have formed a pattern connecting all the starting aspects. The outer aspects need not be connected to all the others, they simply need to form a connected whole.<BR>Once this is done the research will complete and the research note will turn into a discovery. Using the discovery will commit its knowledge to memory.<LINE>NOTE: Placing and removing aspects from the hex grid consumes ink from your scribing tools. If it runs out you will not be able to proceed until you refill it.<PAGE>§l    §nSecondary Research§r<BR>Not all knowledge requires notes. Simpler subjects instead list a direct cost in aspects on their requirement page. Advancing such research consumes the required research points from your pool directly, and no scribing tools or paper are needed.");
        add.accept("research.thaumaturge.flux.title", "Flux");
        add.accept(
                "research.thaumaturge.flux.stage_0",
                "I have been monitoring a strange buildup of magical energy in the aura. It seems to be a form of 'dirty' vis that I have named Flux. It shows as an ominous dark bar on my thaumometer.<BR>I now know what those purple puffs are I occasionally see when I am being careless - it is essentia spilling into the atmosphere and becoming Flux. Pure essentia, it seems, should not be directly exposed to the aura.<BR>I'm not exactly sure what negative effects this will cause, but I have noticed that Flux seems to clog the local aura preventing Vis from replenishing naturally.<BR>Picture it as a thick layer of scum on a natural pool of clean water.<BR>The local aura has a point at which it is 'filled' and Flux seems to count towards this capacity.<BR>More alarmingly, it seems to take precedence over vis and I suspect it can probably exceed this natural limit.<BR>I am not sure what will happen when Flux overflows, but it it bound to be interesting.");
        add.accept(
                "research.thaumaturge.flux.stage_1",
                "I have been monitoring a strange buildup of magical energy in the aura. It seems to be a form of 'dirty' vis that I have named Flux. It shows as an ominous dark bar on my thaumometer.<BR>I now know what those purple puffs are I occasionally see when I am being careless - it is essentia spilling into the atmosphere and becoming Flux. Pure essentia, it seems, should not be directly exposed to the aura.<BR>I'm not exactly sure what negative effects this will cause, but I have noticed that Flux seems to clog the local aura preventing Vis from replenishing naturally.<BR>Picture it as a thick layer of scum on a natural pool of clean water.<BR>The local aura has a point at which it is 'filled' and Flux seems to count towards this capacity.<BR>More alarmingly, it seems to take precedence over vis and I suspect it can probably exceed this natural limit.<BR>I am not sure what will happen when Flux overflows, but it it bound to be interesting.");
        add.accept(
                "research.thaumaturge.flux.addendum_0",
                "Note to self: Do NOT let the flux build. It is bad.<BR>Very, very bad.<BR>If Flux builds high enough it seem to cause a tear in the very fabric of magic itself. The flux escapes through this tear, but usually raw, chaotic magic spills through into our world causing all manner of calamity.<BR>I should be a lot more careful of causing flux in the future. In fact, I should start looking into ways to neutralise Flux.");
        add.accept("research.thaumaturge.warp.title", "Warp");
        add.accept(
                "research.thaumaturge.warp.stage_0",
                "Many believe man was not meant to meddle with magic, and nothing gives more credence to this than the existence of Warp.<BR>Researching forbidden magics or crafting objects of a questionable nature tends to distort a thaumaturge's view of reality, twisting both their mind and their body. This effect is called Warp.<BR>While some warp gained is temporary and will fade over time, there is no known way to get rid of permanent Warp once it is gained and a thaumaturge who chooses to ignore the dangers in the pursuit of power often finds himself on a slippery slope.<BR>Warp usually manifests as minor lapses in concentration, physical pain or hallucinations.<BR>These are more than merely mental though, as the visions sometimes impart useful knowledge or seeming hallucinations that prove to be frighteningly real.<BR>I should monitor my mental well being carefully and to that end I have designed a device that should show me exactly how even my keel is at the moment.");
        add.accept("research.thaumaturge.ore.title", "Ores");
        add.accept(
                "research.thaumaturge.ore.stage_0",
                "There are many unique ores and mineable blocks that are useful to a Thaumaturge. I should make a note of all those I come across.");
        add.accept(
                "research.thaumaturge.ore.addendum_0",
                "§oAmber Ore§r<IMG>thaumaturge:textures/research/research3.png:0:128:128:128:.6</IMG>This ore is found near the surface and when mined drops chunks of amber.");
        add.accept(
                "research.thaumaturge.ore.addendum_1",
                "§oCinnabar§r<IMG>thaumaturge:textures/research/research3.png:128:128:128:128:.6</IMG>This reddish ore is found deep underground and when smelted produces quicksilver.");
        add.accept(
                "research.thaumaturge.ore.addendum_2",
                "§oVis Crystals§r<IMG>thaumaturge:textures/research/research4.png:0:0:128:128:.6</IMG>On occasion there is so much primal energy in the aura that it will settle into rock from which vis infused crystals will grow. As long as they are fed vis they will continue to grow and even spread.<BR>Once the aura dips low enough the energy once again dissipates out of the crystals, replenishing the aura and causing the crystals to crumble. In effect, they are natural, or in this case, supernatural vis batteries. They will never crumble away completely and at least a small remnant of the crystal will remain.<BR>These crystals can be harvested for elemental shards that have many uses in Thaumaturgy.<IMG>thaumaturge:textures/research/research3.png:0:104:96:16:1</IMG>You have yet to discover a method to extract individual crystals without destroying the entire cluster. It is a matter of all or none.<BR>Vis crystals growing in tainted areas may become tainted as well.");
        add.accept("research.thaumaturge.plants.title", "Plants");
        add.accept(
                "research.thaumaturge.plants.stage_0",
                "There are many unique plants that are useful to a Thaumaturge. I should study those I find.");
        add.accept(
                "research.thaumaturge.plants.addendum_0",
                "§oGreatwood§r<IMG>thaumaturge:textures/research/research4.png:128:128:128:128:.6</IMG>These large and majestic trees can be found dotted all over the countryside.<BR>Their wood is able to absorb impressive amounts of magic making it a popular component in the crafting of magical devices.");
        add.accept(
                "research.thaumaturge.plants.addendum_1",
                "§oSilverwood§r<IMG>thaumaturge:textures/research/research4.png:0:128:128:128:.6</IMG>These rare and magical trees are a wondrous sight. Their leaves shimmer with magical energy and their pale, soft wood is valued for its purifying properties.<BR>The tree's magical nature is such that if allowed to grow naturally they will slow replenish the aura around them - this feature alone makes a silverwood sapling a rare prize.");
        add.accept(
                "research.thaumaturge.plants.addendum_2",
                "§oCinderpearls§r<IMG>thaumaturge:textures/research/research5.png:0:0:128:128:.6</IMG>Found only in the hottest of climes, these plants burn with elemental fire. They can be harvested and ground to produce a fine powder akin to Blaze powder.");
        add.accept(
                "research.thaumaturge.plants.addendum_3",
                "§oShimmerleaf§r<IMG>thaumaturge:textures/research/research4.png:128:0:128:128:.6</IMG>These mysterious, glowing flowers only grow in the shadow of Silverwood Trees. They can be harvested and pulped to produce quicksilver.");
        add.accept(
                "research.thaumaturge.plants.addendum_4",
                "§oVishroom§r<IMG>thaumaturge:textures/research/research5.png:0:128:128:128:.6</IMG>This strange, glowing mushroom can only be found growing in magical forests.<BR>They have hallucinogenic properties so one must be careful not to approach too closely.");
        add.accept(
                "research.thaumaturge.plants.addendum_5",
                "§oMana Beans§r<BR>These beans, known as Mana Beans, contain concentrated amounts of essentia. They can also be consumed, though the results are often a bit unpredictable.<BR>They can be replanted on the underside of logs within magical biomes, but cultivation is difficult and time-consuming.<BR>Wild mana pods usually only produce primal aspects, though it is said that pods growing adjacent to pods of differing aspects sometimes result in beans with compound aspects.");
        add.accept("research.thaumaturge.crystal_farmer.title", "Crystal Farming");
        add.accept(
                "research.thaumaturge.crystal_farmer.stage_0",
                "I have observed that vis crystals naturally grow over time, but once I harvest them they seem to lose this ability. I am hoping to discover what exactly is lost and how to reinvigorate the crystals.");
        add.accept(
                "research.thaumaturge.crystal_farmer.stage_1",
                "I have found a way to reinvigorate vis crystals in such a way that I can replant them.<BR>I am hoping this means I can grow my own steady supply of primal vis crystals.");
        add.accept("research.thaumaturge.primordial_pearl.title", "Primordial Pearls");
        add.accept(
                "research.thaumaturge.primordial_pearl.stage_0",
                "For the first time I find myself with a puzzle that I do not think can be solved.<BR>This object contains pure primal aspects, but they possess properties unlike what I consider normal, everyday primal aspects. I have a few theories why this could be:<BR>Firstly this \"pearl\" could be the remnants of primal matter that was created at the dawn of reality - elementary magic that existed before it mingled and merged into compound aspects.<BR>Alternatively it could be primal matter from a place that does not conform to the known rules of magic as you have come to understand it. I'm not sure which explanation is more worrying.<BR>Either way, the pearl is a potent artifact containing vast magical power and nearly unlimited potential.<BR>It is fairly rare to find a whole one though. Often they have degenerated into nodules or even motes. Even a mote is valuable though.");
        add.accept("research.thaumaturge.unlock_alchemy.title", "Discovering Alchemy");
        add.accept(
                "research.thaumaturge.unlock_alchemy.stage_0",
                "Brewing simple potions has its uses, but true alchemical interactions require more than mixing a few monster parts together.<BR>I think Essentia holds the key to a much more sophisticated way of manipulating matter.<BR>I should try and learn more about alchemy and how it fits into the magical world.<BR>Armed with that knowledge and a little bit of inspiration I am sure Thaumaturgic Alchemy will be within my grasp.");
        add.accept(
                "research.thaumaturge.unlock_alchemy.stage_1",
                "My recent discoveries have given me a new insight into how the world is put together and I think I might know a way to transform matter by altering its Essentia.<BR>This will require experimentation and something to hold the alchemical substrate. A simple brewing stand will not do - what I need is a mystical crucible.<BR>A cauldron is the right shape and size, but what is effectively a big metal bowl will never be able to hold the magical energies I wish to manipulate.<BR>Once again, applying some Salis Mundus to a cauldron should resolve my problems.");
        add.accept(
                "research.thaumaturge.unlock_alchemy.stage_2",
                "I have my crucible! Now I need something to experiment with, and I happen to have the perfect thing. I have spent many a late-night studying thaumaturgy and flickering torchlight is not the best thing to read by. A steady magical light is exactly what I need now.<BR>Glowstone is a steady source of light, but I require large handfuls of it for a light source and it lacks... style. I am sure I can alter it into something better.<BR>The crucible needs to be placed over a source of heat and filled with water. Once the water boils I can start adding the things I need. I have jotted down a recipe that I think will create a substance I have named Nitor. A small core of energized glowstone empowered with energy, fire and light should burn forever. It should be naturally buoyant, allowing it to be placed wherever I choose - even floating in mid-air.<BR>I simply need to find objects with the required essentia (pressing shift whilst hovering over an item might help here) and throw them into the crucible.<BR>The crucible will render them down into their constituent essentia. I might not be able to gather the exact combination of essentia I require, but if there are enough of the types I need everything should be fine.<BR>Once I have enough all I need to do is throw in the catalyst. In this case a handful of glowstone dust.<BR>If all goes well the glowstone will absorb the essentia and transform into nitor.<BR>I am not sure what will happen to the excess essentia, but I can only assume it will eventually dissolve harmlessly.<BR>[Shift-right clicking with an empty hand will empty a crucible. Each crafting operation will consume some of the water so you will have to replenish it now and again.]");
        add.accept("research.thaumaturge.unlock_auromancy.title", "Discovering Auromancy");
        add.accept(
                "research.thaumaturge.unlock_auromancy.stage_0",
                "Crafting objects of power with Vis has opened a whole new world for me, yet I am not satisfied. There must be a way of manipulating the aura directly. A way to draw vis from the very air itself and mould it to my will.<BR>I should study the world around me and travel to its edges. There I will learn what I need.<BR>Sometimes I feel like I am being driven by forces outside my understanding - unheard voices urging me towards great power and knowledge.<BR>Shaping vis will be one more step on this journey.");
        add.accept(
                "research.thaumaturge.unlock_auromancy.stage_1",
                "Slowly but surely I am beginning to understand the nature of Vis. So much is still hidden to me, but I think I now know enough to begin moulding it to my will. For this I will need a tool. A very special tool.<BR>I have stumbled upon some dusty scrolls penned by a long dead thaumaturge. They describe how to craft wands that store and channel vis - and the more I read, the more the design impresses me. A wand is always the combination of a rod and two end-caps. The rod holds the vis, six pools of it, one for each primal aspect, while the caps focus it as it is spent. Different materials should make for better rods and caps, but for a first attempt mundane wood and iron will have to do, poor conductors though they are.<BR>The scrolls also mention crystalline foci that can be socketed into a wand to shape its vis into all manner of useful effects. Brilliant actually. It irks me that I did not think of it first.<BR>Only this basic wand can be put together on a normal crafting table - I suspect anything finer will require the arcane workbench and a cost in vis besides. I will also need a vis resonator to study how the energy settles into the rod.");
        add.accept(
                "research.thaumaturge.unlock_auromancy.stage_2",
                "I have put together my first casting wand and I am quite pleased with the result. It is rough - iron caps waste a tenth of every drop of vis they channel and the wooden rod holds a mere 25 vis of each primal - but it works.<BR>To fill it I can hold it high and slowly condense the raw aura around me into primal vis, though the process is crude and wasteful. Aura nodes promise a far richer harvest - pointing the wand at one and holding lets me tap the node directly. Creatures slain in battle also shed motes of primal energy that the wand drinks in eagerly, and even the humble vis-bearing plants of the world will give up a little charge when harvested.<BR>While holding the wand, an indicator shows me how much vis rests within, and its tooltip lays out each pool exactly.<BR>The wand has also earned a place at my arcane workbench. Slotted beside the crafting grid, its caps focus the vis the bench draws from the aura, discounting the cost of every arcane recipe. Better still, a charged wand can stand in for vis crystals entirely, paying two vis of the matching primal for each crystal a recipe demands - no crystals consumed at all. Without a wand the workbench still functions, but the work runs unfocused: the crystals are burned as crude catalysts and the aura is taxed a quarter more for my trouble.<BR>My main stumbling block now is that in its current state it is little more than a magical wrench. Until I crack the secret of crafting foci its utility will be limited. The path before me is clear...");
        add.accept("research.thaumaturge.unlock_artifice.title", "Discovering Artifice");
        add.accept(
                "research.thaumaturge.unlock_artifice.stage_0",
                "I have learned much and one of those things is how much more I still have to learn. The worlds revealed to me are arcane and mysterious with many invisible wonders... and dangers.<BR>I need a tool that will allow me to see the unseen and ease my journey of discovery. The old texts I have read make mention of a set of goggles commonly used by thaumaturges for just this purpose.<BR>Alas the text does not mention exactly how they work, so I will need to design my own from first principles.");
        add.accept(
                "research.thaumaturge.unlock_artifice.stage_1",
                "I have penned my own designs based on the little information I could find and my own experimental findings. Time to put theory into practice.<BR>Not only will the goggles reveal hidden things, but they will also reduce the vis cost of certain thaumaturgical tasks.<BR>Lastly these goggles will allow me to see the vis or essentia content of objects in the world which will prove a great boon I am sure.<PAGE>The most important thing I have learned however is the possibility of crafting delicate mechanical devices using magic and imbuing them with arcane power. ");
        add.accept("research.thaumaturge.unlock_infusion.title", "Discovering Infusion");
        add.accept(
                "research.thaumaturge.unlock_infusion.stage_0",
                "Thus far I have been applying vis to objects as a matter of course. It is simple enchanting much like is possible using an everyday enchanting table. I believe more is possible however - by §oinfusing§r objects with essentia I should be able to artificially force them to take on properties and aspects of other objects or even abstract concepts.<BR>Anyone can enchant an object with magic that lifts it into the air, but infusion should be able to imbue the object with lightness itself, making it part of its essential nature.");
        add.accept(
                "research.thaumaturge.unlock_infusion.stage_1",
                "My experiments have been successful. I have been able to infuse simple objects with essentia, but the process is complex, difficult and dangerous. I need to find a way to simplify it before I can truly delve into all the possibilities it holds.<PAGE>The experiments did have some practical results as well.<BR>I have also discovered a fabric that is soft, supple and easy to enchant. I have designed patterns for a set of clothing made from this fabric.<BR>It will not only be comfortable and stylish, but while worn it will reduce the vis cost of certain types of thaumaturgical tasks. The clothing should also be easily dyeable much like leather armor.<BR>All in all I am quite pleased with what I have done here. ");
        add.accept("research.thaumaturge.unlock_golemancy.title", "Discovering Golemancy");
        add.accept(
                "research.thaumaturge.unlock_golemancy.stage_0",
                "This book has taught me many things: How to manipulate matter and magic on a fundamental level, how to imbue objects with magical properties and even how to draw magic from the very air around me. Still some things elude me.<BR>There are some very basic, but amazingly magical things almost anyone can do. Just stacking a few blocks together in the right pattern will cause them to transform into semi-sentient animated golems. How is this possible? Why would it even happen? Is the aura somehow responsible for this?<BR>I should study this phenomenon more closely.");
        add.accept(
                "research.thaumaturge.unlock_golemancy.stage_1",
                "My findings are confusing to say the least.<BR>The 'natural' golems that can be created are indeed infused with vis, but I do not believe their creation is thaumaturgical in nature. Whatever force animates them seems to come from elsewhere.<BR>Still, my research has at least borne some fruit. I have studied this animating force and I believe I can recreate it using thaumaturgical principles. I should further my studies as soon as possible.");
        add.accept("research.thaumaturge.crimson_rites.title", "The Crimson Rites");
        add.accept(
                "research.thaumaturge.crimson_rites.stage_0",
                "I should find the book the Cultists call the Crimson Rites and study it.");
        add.accept(
                "research.thaumaturge.crimson_rites.stage_1",
                "Much of this book is written in a spidery and unintelligible script, but what I can understand offers me some strange insights into the goals of the Crimson Cult.<BR>Their origins are shrouded in mystery, but it seems their goal is the perfection of a ritual they refer to as \"Apertis Oculis\" - Opening the Eye. What it does is unclear, but I doubt it is anything good.<BR>Interestingly enough, I think thaumaturgy might offer the missing pieces they have so long sought. Obviously only a madman would pursue this line of study...");
        add.accept("research.thaumaturge.unlock_eldritch.title", "Delving into the Eldritch");
        add.accept(
                "research.thaumaturge.unlock_eldritch.stage_0",
                "The Crimson Cultists have some strange ideas, but there seems to be a small nugget of hidden truth there. Maybe I should find proof of what they are suggesting? Who knows what knowledge I could discover...");
        add.accept(
                "research.thaumaturge.unlock_eldritch.stage_1",
                "I have found something strange... something that doesn't quite fit the rules that reality and reason has set for us.<BR>I do not believe this seed comes from our reality at all. It comes from somewhere... outside.<BR>I must study this further... I must.");
        add.accept("research.thaumaturge.flux_cleanup.title", "Flux Cleanup");
        add.accept(
                "research.thaumaturge.flux_cleanup.stage_0",
                "I may have made a mess of things. I have allowed flux to build up in the aura and now I need to get rid of it - fast.<BR>Fortunately the answer might be found in one of the unique properties of flux. Technically flux is just 'dirty' vis, but what sets it apart is the fact that it can easily transform from a form of energy into physical matter. This makes it dangerous as it can spawn Taint and other forms of nastiness, but maybe it can also be coaxed into safer forms.");
        add.accept(
                "research.thaumaturge.flux_cleanup.stage_1",
                "I have managed to find a way of turn pure Flux in the aura into flux essentia. Flux essentia is hardly benign, but it is much easier to handle than raw Flux. Unfortunately the process is not simple, cheap, or fast.<BR>I have drawn up plans for a Flux Condenser. When supplied with lots of essentia it can draw flux from the aura and force it to become essentia itself. Every point of flux drawn from the aura will require 5 or more essentia and some time. The type of essentia does not matter as long as I do not use Flux essentia. That would be bad.<BR>Essentia can be piped into the sides of the Condenser and the resulting flux essentia can be drained from the bottom.<LINE>The Condenser is not all that is needed. I also need a special lattice that will actually draw the flux from the aura. The lattice pieces can be connected to the top of the Condenser and each other to form a multiblock structure. The more pieces I connect, the quicker the Condenser can draw flux from the aura. It does however increase the essentia cost.<BR>The lattice cannot be attached any old way however:<BR>1) The lattice blocks must be attached to the top of the condenser and all the connected lattice pieces must be above the condenser itself.<BR>2) I can attach many lattice blocks together, but any lattice pieces further than 8 blocks from the condenser will automatically detach.<BR>3) It is theoretically possibly to pack a large amount of lattice pieces into this 8 block range, but the benefit a lattice piece provides is reduced for each other lattice piece it is connected to.<BR>4) It may appear that a single unbroken line of lattices would be the best configuration, but occasionally the filter in a lattice piece becomes clogged and unusable. All lattice pieces after the clogged one will no longer aid the conversion speed, though they will still add to the cost. The filter can be replaced by clicking on the clogged lattice piece with a fresh one.<BR>To determine the cost and conversion rate of the Condenser multiblock I can simply use a Resonator on the Condenser itself.");
        add.accept("research.thaumaturge.flux_rift.title", "Flux Rifts");
        add.accept(
                "research.thaumaturge.flux_rift.stage_0",
                "As I suspected these tears are holes in reality itself. The chaotic energies that flow from them is extremely dangerous and I should find a way to close these rifts as soon as possible.<BR>My observations of the rift itself has revealed some interesting things however. The rifts actually seem to feed on flux - the more flux there is in the environment, the larger they grow. They also grow more unstable over time and eventually this instability causes a burst of pure chaotic energy. This energy can take many forms and is seldom a good thing. Fortunately they regain some stability when this happens. The severity of the chaos burst depends on the size of the rift.<BR>Even though I can't close them yet, maybe I can find some way of keeping them stable.");
        add.accept("research.thaumaturge.rift_closer.title", "Closing Flux Rifts");
        add.accept(
                "research.thaumaturge.rift_closer.stage_0",
                "I need to find a way of closing these flux rifts before they grow too large and unstable.");
        add.accept(
                "research.thaumaturge.rift_closer.stage_1",
                "I have studied the chaotic energies that form these flux rifts and have made an interesting discovery: The rifts themselves are inherently unstable. In theory they will close naturally should they become unstable enough, but the laws of our reality makes this highly unlikely - instead their unstable energy is usually released in smaller bursts. That is probably a good thing - the amount of energy that will be released should a fully unstable rift collapse would be catastrophic.<BR>I think that I have worked out a way to trigger that collapse without the rift needing to be fully unstable - in short, I plan on blowing them up.<BR>No normal explosive will do however - I need an explosive that sends its destructive energies into more than the three normal dimensions. To this end I have designed an explosive I have named the Causality Collapser. This pulsing red sphere needs to be thrown as close to the rift as possible. In theory it will break down the chaotic energies holding the rift together.<BR>Unfortunately my calculations suggest that while collapsing the rift will release large amounts of unstable primal energy - the more unstable the rift, the more chaotic this release will be. I should try and make the rift as stable as possible before detonating the explosive.");
        add.accept("research.thaumaturge.base_auromancy.title", "Basic Auromancy");
        add.accept(
                "research.thaumaturge.base_auromancy.stage_0",
                "I have the wand, but now I need to craft the foci. The old scrolls aren't very clear on how to go about this, so I will need to find my own way.<BR>I have played with the idea of assembling a focus from crystals and then using vis to bind them together. My initial experiments have been failures... explosive failures.<BR>I think the answer I seek lies with an alchemical process.<BR>Using an ordo crystal as a core I might be able to leverage its ordered structure to grow a focus around it. While this is happening it should absorb praecantatio and auram essentia.<BR>The result should hopefully be a crystal focus able to absorb and manipulate vis. ");
        add.accept(
                "research.thaumaturge.base_auromancy.stage_1",
                "Using Alchemy to create the focus was indeed the path to success. The focus is stable and clear - now I simply need to imprint the proper patterns into it. For this I will need a Focal Manipulator<BR>I have already drawn up plans for a Focal Manipulator. The way it functions is similar to an arcane workbench in many ways, but instead of using vis to craft items it instead imprints patterns into the focus crystal.<BR>I must also design the patterns that I will need to imprint. I have an idea for a pattern to create flame, but I need to do some more research.");
        add.accept(
                "research.thaumaturge.base_auromancy.stage_2",
                "I have everything I need. All I need to do is craft my first focus.<BR>By arranging specially prepared vis crystals around the blank focus I can set up patterns that will determine what function the focus will perform. For now I only know the patterns for §nRoot§0, §nTouch§0 and §nFire§0.<BR>The §nRoot§0 node represents me, the caster. In the focal manipulator interface it is shown as an eye icon. The empty node below it is marked with both a target and a trajectory icon. Mediums (like §nTouch§0) require trajectories to determine what they effect - the §nRoot§0 node provides a trajectory that matches where the caster is looking so by linking §nTouch§0 to it I will be able to 'touch' anything I am looking at. Mediums almost always provide a target (in this case whatever has been 'touched') and some can also provide a trajectory. In this case though I require a target so I will be able to link the §nFire§0 effect.<BR>By linking §nRoot§0, §nTouch§0 and §nFire§0 in this way I will be able to create a focus that applies the §nFire§0 effect to whatever I touch.<BR>I could in theory link §nFire§0 directly to §nRoot§0, but since root provides the caster as a target that means I will simply set myself on fire.<BR>I can fine tune exactly what the §nFire§0 effect does by tweaking its damage and burn duration in the bottom right of the interface. Many patterns will allow me to fine tune them in this way.<BR>Creating a focus require three types of resources - the first is the crystals used in the crafting process, the second is vis from the aura, and lastly it also requires some player experience. Every time a focus is used it will also consume a small amount of vis from the aura.<PAGE>§nComplexity§0<BR>The effects I can create now are fairly simple and limited. There is a limit to how many patterns I can attach to a focus. This is measured as the focus Complexity. Each pattern type has a complexity cost and increasing their paramaters (for example the damage of a fire pattern) will increase the complexity further. Using a pattern more than once also incurs a stacking complexity cost increase of 50%.<BR>The complexity of a focus directly determines how much vis it will cost to cast and how long my wand needs to cool down between casts.");
        add.accept("research.thaumaturge.focus_advanced.title", "Advanced Foci");
        add.accept(
                "research.thaumaturge.focus_advanced.stage_0",
                "Lesser foci simply cannot carry patterns of any appreciable complexity. Discovering Infusion has given me a new way of crafting however and I hope it will be able to help me solve the problem of creating foci intricate enough to carry complex patterns.");
        add.accept(
                "research.thaumaturge.focus_advanced.stage_1",
                "By taking a lesser focus and infusing it with the maleability of quicksilver and the complex crystalline structure of diamonds and ender pearls I have created a advanced focus with a lot more internal complexity.");
        add.accept("research.thaumaturge.focus_greater.title", "Greater Foci");
        add.accept(
                "research.thaumaturge.focus_greater.stage_0",
                "I have reached a dead end with advanced foci. The crystalline matrix inside them is quite complex, but no amount of tinkering, alchemy or infusion will be able to improve it. There is another option however.<BR>Instead of trying to increase the complexity, I might be able to simply increase the size of the focus itself - instead of trying to squeeze more into the box I should increase the size of the box itself.<BR>Of course, foci are the size they are for a reason and increasing their physical size would be counterproductive. I should instead increase their metaphysical size using dimensional magic. Primal Pearls carry vast amounts of energy in a relatively compact package. I should study them to see how I can replicate their properties.");
        add.accept(
                "research.thaumaturge.focus_greater.stage_1",
                "I have managed to increase the complexity of foci nearly twofold. By infusing an advanced focus with the energy storage capability of a Primal Pearl and the dense internal structure of a Nether Star I have created a focus that is bigger on the inside - magically speaking. Physically it has the same dimensions as a normal focus, though it is noticeably heavier.");
        add.accept("research.thaumaturge.focus_elemental.title", "Elemental Effects");
        add.accept(
                "research.thaumaturge.focus_elemental.stage_0",
                "It should be a simple matter for me to use the fundamentals I have learned to harness other elements.");
        add.accept(
                "research.thaumaturge.focus_elemental.stage_1",
                "§nAir§0<BR>This effect creates a blast of air. It does not cause much damage, but it will knock back anything it hits.<PAGE>§nFrost§0<BR>Fire may be destructive, but intense cold can be nearly as harmful with the added benefit of chilling my enemies and slowing them to a crawl.<PAGE>§nEarth§0<BR>The blast of earth and rock created by this effect packs a significant punch. It is even powerful enough to break blocks made from weaker materials.");
        add.accept("research.thaumaturge.focus_flux.title", "Focus Effect: Flux");
        add.accept(
                "research.thaumaturge.focus_flux.stage_0",
                "Using the elements as a weapon has its benefits, but raw magic can be just as destructive - most creatures are harmed if exposed to raw, unfiltered vis. Even armor cannot protect you from this energy. ");
        add.accept("research.thaumaturge.focus_heal.title", "Focus Effect: Heal");
        add.accept(
                "research.thaumaturge.focus_heal.stage_0",
                "Infusing targets with victus is a proven way to heal them. Applying the principle to a focus effect should be simple enough.<BR>The effect should be quite devestating against undead.");
        add.accept("research.thaumaturge.focus_curse.title", "Focus Effect: Curse");
        add.accept(
                "research.thaumaturge.focus_curse.stage_0",
                "The pech wand I discovered is strange and unusable, but its workings bear some striking similarities to thaumaturgical foci.<BR>I should be able to reverse engineer it.");
        add.accept(
                "research.thaumaturge.focus_curse.stage_1",
                "As I suspected the pech wand is thaumaturgical in nature. Unfortunately this discovery leaves me with more questions than answers.<BR>At least I was able to decipher the wands magic into a usable focus pattern. The Curse pattern magically poisons and cripples anything it hits with random maladies. It can even curse the target with a streak of bad luck. Quite nasty and probably devastating if combined with other effects.<BR>Even if it misses, the curse will leave a lasting mark on the world in the form of a corrupting field of energy. The field will eventually fade.");
        add.accept("research.thaumaturge.focus_break.title", "Focus Effect: Break");
        add.accept(
                "research.thaumaturge.focus_break.stage_0",
                "Calling up the destructive power of perditio I should be able to design a pattern that can be used to safely break most blocks.<BR>I should also study examples of the Fortune and Silk Touch enchantments. Digging magic will be much more useful if I can duplicate their effects. ");
        add.accept(
                "research.thaumaturge.focus_break.stage_1",
                "Not only have I created an effect that can harvest nearly every block, but if I wish I can also apply Fortune and Silk Touch effects as well.<BR>Harder blocks will take longer to break, unless I increase the strength (and thus complexity) of the focus.<BR>There is a small additional vis cost per block broken beyond what is used to create the initial effect.");
        add.accept("research.thaumaturge.focus_exchange.title", "Focus Effect: Exchange");
        add.accept(
                "research.thaumaturge.focus_exchange.stage_0",
                "While teleportation magic is beyond me at the moment, my studies of the permutatio aspect leads me to believe that I should be able to make physical objects swap positions.<BR>I should see if my theory holds any water.");
        add.accept(
                "research.thaumaturge.focus_exchange.stage_1",
                "My theory has proven correct! By applying this pattern to a focus I can use it to select a type of block in the world. I simply need to sneak while right-clicking on it.<BR>When I then cast the effect on most other blocks it will exchange the target block for the one I selected earlier. Obviously I must be carrying enough of the selected blocks for the exchange to happen and the targeted block (or what it normally drops when broken) will appear in my inventory.<BR>It seems I cannot exchange particularly complex blocks, or those that cannot be broken by normal means.<BR>Apart from the cost to cast it, it seems the vis cost is also subtracted for each block exchanged in this way. I should be cautious.  ");
        add.accept("research.thaumaturge.focus_plan.title", "Focus Medium: Plan");
        add.accept(
                "research.thaumaturge.focus_plan.stage_0",
                "I have learned to manipulate blocks in the world with foci effects such as Exchange, but their practical use is a bit limited.<BR>I should discover a way to fine tune exactly which blocks I can apply such foci effects to.");
        add.accept(
                "research.thaumaturge.focus_plan.stage_1",
                "I have designed a medium pattern that will allow me to plan which blocks I manipulate. This medium works much like Touch, except I will be shown a ghostly outline if which blocks will be manipulated. The range is also much greater (16 blocks). By pressing the focus modify key ('G' by default) I can alter the size of the area. By pressing CTRL + G I can alter the axis in which the size will be changed.<BR>This medium has two modes of operation which I can select between when I add it to a focus: Full and Surface. Full mode affects all possible blocks that match the size, while surface mode will only affect blocks that have an exposed side.<BR>This focus medium cannot be used in combination with any other mediums.");
        add.accept("research.thaumaturge.focus_rift.title", "Focus Effect: Rift");
        add.accept(
                "research.thaumaturge.focus_rift.stage_0",
                "I have performed a few tentative experiments with dimensional magic. Thus far I have had mixed results, but I am able to reliably shift inanimate matter into an adjacent reality while part of that reality shifts into ours.<BR>I think. Either way, the matter returns unharmed almost every time. Living matter is a different matter - I dare not pen what I saw and I doubt I will attempt that experiment again.<BR>Luckily I think I will be able to replicate this discovery in the form a focus effect.");
        add.accept(
                "research.thaumaturge.focus_rift.stage_1",
                "I have managed to create a focus that manipulates dimensional magic. Most inanimate objects will be shifted into this other realm temporarily like some sort of portable hole.<BR>The diameter of this hole will remain fixed at around 3 yards, but I should be able to manipulate the depth quite a bit. ");
        add.accept("research.thaumaturge.focus_projectile.title", "Focus Medium: Projectile");
        add.accept(
                "research.thaumaturge.focus_projectile.stage_0",
                "Touching my foe to set them alight is fine and all, but I would much rather hurl magic at them from afar. I need to find a way to extend focal energy beyond my limited reach.");
        add.accept(
                "research.thaumaturge.focus_projectile.stage_1",
                "I can now place the energy of the focus into a magical projectile that I can hurl at enemies or blocks. The projectile is slow, but I can increase it's speed by increasing the complexity.<BR>Further research might allow me to refine these projectiles even further. Studying other kinds of projectiles closely should inspire me. This might involve some personal risk...");
        add.accept(
                "research.thaumaturge.focus_projectile.stage_2",
                "I have discoverd methods to improve the utility of projectile even further!<LINE>§nBouncy§0<BR>When this projectile hits a solid surface it will bounce off. This projectile cannot effect blocks anymore as it will bounce off any block it contacts.<BR>§nSeek Hostiles§0<BR>Projectiles with this special property will automatically redirect their flight path towards hostile targets ahead of them.<BR>§nSeek Friendlies§0<BR>Similar to Seek Hostiles, this projectile will seek out friendly targets.");
        add.accept("research.thaumaturge.focus_bolt.title", "Focus Medium: Bolt");
        add.accept(
                "research.thaumaturge.focus_bolt.stage_0",
                "Hurling magical projectiles is a chore sometimes. They are hard to aim and take time to travel to the intended target.<BR>Maybe there is a way I can extend the functionality of the the Touch medium.");
        add.accept(
                "research.thaumaturge.focus_bolt.stage_1",
                "Who needs projectiles when they can hurl bolts of magical energy? They affect their target instantly and aiming them is a lot easier.<BR>Unfortunately their range is limited. Trying to keep the vis stream coherent beyond 16 blocks has proven difficult. For longer range engagements, projectiles are still superior.");
        add.accept("research.thaumaturge.focus_scatter.title", "Focus Modifier: Scatter");
        add.accept(
                "research.thaumaturge.focus_scatter.stage_0",
                "I now know how to create both projectiles and bolts that propagate focus effects. I believe it should be fairly simple to design a plan that allows me to split a single trajectory into multiple.");
        add.accept(
                "research.thaumaturge.focus_scatter.stage_1",
                "I have managed to split a single medium trajectory into multiple, though not quite to the degree I had hoped. There are two problems:<BR>Firstly is that while I can control the number of forks created from the original trajectory, I have no control over the resulting trajectories once it is split. It simply scatters them in random directions. Fortunately I have figured out how to limit the cone of scatter.<BR>The second problem is that the more forks I create, the weaker the individual effects are. This is not surprising - there is only so much vis I can channel. I mitigate this slightly by drawing in additional vis to bolster the effects, but each resulting fork is still weaker than the original.<BR>This modifier can only be used once in a single focus.");
        add.accept("research.thaumaturge.focus_mine.title", "Focus Medium: Arcane Mine");
        add.accept(
                "research.thaumaturge.focus_mine.stage_0",
                "I have done some studies into creating contructs of pure arcane energy recently. Maybe I can apply what I have learned");
        add.accept(
                "research.thaumaturge.focus_mine.stage_1",
                "I can now create a quasi-physical construct of vis that can hold focus effects in stasis until triggered. When created, this 'mine' take two seconds to arm. Once it is armed any non-allied entity that gets close enough will cause the mine to dissipate, releasing the stored effects towards the triggering entity.<BR>The mine can be set to trigger on friendlies instead of enemies and neutral creatures.");
        add.accept("research.thaumaturge.focus_spellbat.title", "Focus Medium: Spellbat");
        add.accept(
                "research.thaumaturge.focus_spellbat.stage_0",
                "Having observed the strange, elemental infused bats in the nether I believe I should be able to do something similar with foci.");
        add.accept(
                "research.thaumaturge.focus_spellbat.stage_1",
                "I have created a medium pattern that conjures an arcane bat and infuses it with focus effects. It will hunt down enemies and repeatedly inflict the focus effects on them.<BR>The bats have limited lifespans, are fragile and each time they attack they also lose some of the vis holding them together.<BR>The bat can be set to target friendlies instead of enemies and neutral creatures.");
        add.accept("research.thaumaturge.focus_cloud.title", "Focus Medium: Cloud");
        add.accept(
                "research.thaumaturge.focus_cloud.stage_0",
                "Seeing the lingering effects the end dragon's breath causes has given me some ideas. I might be able to duplicate the effects as a focus medium.");
        add.accept(
                "research.thaumaturge.focus_cloud.stage_1",
                "Much like lingering potions can be used to create a static effect in the world, the Cloud medium can be used to create a field of energy that periodically applies focus effects to entities and blocks caught within.<BR>The strength of effects linked to this medium will be reduced significantly.");
        add.accept("research.thaumaturge.focus_split.title", "Focus Modifier: Split");
        add.accept(
                "research.thaumaturge.focus_split.stage_0",
                "While the Scatter modifier has proven quite useful, it is technically a failure. I realise now that my original plan was too ambititious.<BR>I should start smaller.");
        add.accept(
                "research.thaumaturge.focus_split.stage_1",
                "I have managed to draw up designs for two new focus modifiers. The first splits a single target result into two, allowing me to have two different effects apply to the target.<BR>The second modifier does the same, except it splits trajectories.<BR>The only downside is that the split reduces to power of any effects placed below them, though I mitigate the full impact somewhat.<BR>Much to my suprise I have discovered that these modifiers can be split recursivly allowing for some interesting focal designs. ");
        add.accept("research.thaumaturge.recharge_pedestal.title", "Recharge Pedestal");
    }

    private static void part1(BiConsumer<String, String> add) {
        add.accept(
                "research.thaumaturge.recharge_pedestal.stage_0",
                "I expect that I will soon begin crafting objects that require vis to function, but that cannot easily draw the vis from the aura themselves. I will need a way to recharge them.");
        add.accept(
                "research.thaumaturge.recharge_pedestal.stage_1",
                "This specially crafted pedestal can recharge vis storing items. Any rechargeable item placed in it will automatically draw vis from the aura to fill itself. Simply click on the pedestal with the item you wish to recharge. Use the pedestal again to retrieve the item.<BR>Placing a wand upon the pedestal reveals another property: the pedestal slowly siphons vis from any aura nodes within eight blocks directly into the wand's reserves. Wands of mundane wood and iron lack the finesse to leave anything behind and will drain a node dry.");
        add.accept("research.thaumaturge.vis_amulet.title", "Vis Amulet");
        add.accept(
                "research.thaumaturge.vis_amulet.stage_0",
                "If I wish to recharge my vis storage items I currently need to return to the nearest recharge pedestal or carry one of them around. This is not always convenient however - I must find a portable way to recharge my items. ");
        add.accept(
                "research.thaumaturge.vis_amulet.stage_1",
                "I have crafted an amulet capable of channeling vis from the aura, allowing me to recharge items in the field while it is worn.<BR>The amulet will only recharge items located in your hotbar, baubles or armor slots (in that order). It will only recharge items that can normally be recharged in a recharge pedestal.");
        add.accept("research.thaumaturge.workbench_charger.title", "Workbench Charger");
        add.accept(
                "research.thaumaturge.workbench_charger.stage_0",
                "I am beginning to run into an annoying limitation with the arcane workbench. It is only able to draw vis from its immediate surroundings. While this is fine for most of the things I have been crafting so far, I soon foresee a time where I will have to craft something that requires more vis than there is nearby.<BR>I need to find a way to extend the reach of the arcane workbench. The further it can reach to draw vis, the more vis will be available for crafting.");
        add.accept(
                "research.thaumaturge.workbench_charger.stage_1",
                "I have designed a special rig that can be placed on top of the Arcane Workbench and the Focal Manipulator. This framework allows those workbenches to draw vis not only from the chunk they are located in, but the surrounding 8 chunks as well. In theory, this increases the vis available for crafting ninefold.<BR>When vis is drawn from the aura, it will be roughly split between all 9 of these chunks, which should prevent depletion of a single chunk.");
        add.accept("research.thaumaturge.focus_pouch.title", "Focus Pouch");
        add.accept(
                "research.thaumaturge.focus_pouch.stage_0",
                "Once I have gathered a sizable collection of foci it will be quite annoying to carry them all with me. I should find a convenient way to carry them all.");
        add.accept(
                "research.thaumaturge.focus_pouch.stage_1",
                "I have invented a special pouch I can carry with me to store my foci.<BR>Whenever I change the focus in my wand, it will check this pouch first and any foci that get removed will be placed inside it first. I can also right-click the pouch to place or remove foci by hand.<BR>It can also be carried in my belt bauble slot.");
        add.accept("research.thaumaturge.vis_battery.title", "Vis Battery");
        add.accept(
                "research.thaumaturge.vis_battery.stage_0",
                "Natural vis crystals have an interesting ability to act as vis 'batteries'. They absorb vis when the aura is high and discharge it when it is low, but this process is quite slow and unreliable.<BR>By using what I have learned about growing vis crystals I should be able to create a vis battery of my own that is a lot more reliable.");
        add.accept(
                "research.thaumaturge.vis_battery.stage_1",
                "I have succeeded in creating a vis battery.<BR>If the local aura is above 95% of its normal capacity the battery will absorb vis and if the aura drops below 75% it will discharge.<BR>There are a few drawbacks however. While it is quicker and a lot more reliable than natural vis crystals, the battery still takes some time to react to changes in the local aura. The second drawback is it's capacity. It can only store 10 points of vis. My attemps to increase this has proven disastrous. I guess this just means that I will need to use them in larger numbers.<BR>Applying a redstone signal to the battery will not only prevent it from functioning, but it will rapidly discharge any vis it is holding back into the aura.");
        add.accept("research.thaumaturge.base_alchemy.title", "Basic Alchemy");
        add.accept(
                "research.thaumaturge.base_alchemy.stage_0",
                "Crucible crafting guide<BR>1) Place crucible over heat source (fire, lava, nitor or something similar).<BR>2) Add water and wait till it boils.<BR>3) Add ingredients with the essentia you require, either by dropping them in or clicking with the item on the crucible's top face<BR>4) Throw in catalyst.<PAGE>Things to note<BR>- Shift-right clicking with an empty hand will empty a crucible.<BR>- Each crafting operation will consume some of the water so I will have to replenish it now and again. Piping mechanics can be used for this.<BR>- I can craft multiple items at once by adding sufficient essentia and using multiple catalysts.<BR>- Essentia does dissolve over time so I shouldn't wait too long before starting to craft. I wonder what the purple puffs of smoke means when this happens? Probably nothing to worry about.<LINE>§oCrafting Crystals§r<BR>Essentia crystals should be fairly easy to craft. All I need to do is break down a quartz crystal into smaller shards to act as a growth medium.<BR>Simply dipping a shard into the crucible should soak up some of the essentia it contains and cause the crystal to grow.<BR>This won't be a particularly efficient way of doing things - it will probably cost at least twice the essentia that the crystal will end up containing and there is the net loss of quartz which is not always the easiest thing to find.");
        add.accept("research.thaumaturge.alumentum.title", "Alumentum");
        add.accept(
                "research.thaumaturge.alumentum.stage_0",
                "Nitor is a useful substance. It provides heat, light and is aesthetically pleasing. Unfortunately it provides only slightly more heat than a torch - enough to slowly heat some water or keep you warm at night, but not enough for practical purposes like smelting ore.<BR>I need something more... reactive.<BR>I should take some time to collect my thoughts and study the elements. Maybe there is a way to improve coal like I did glowstone.");
        add.accept(
                "research.thaumaturge.alumentum.stage_1",
                "I was right! I should be able to take a lump of coal and add more of the essentia it already contains... and possibly a little bit of perditio for extra kick.<BR>If my calculations are correct the resulting substance, which I have called Alumentum, should burn three times longer than normal coal.<BR>My calculations also show that the substance will be quite energetic and I should take care not to chuck it about... not unless I have good reason to, that is.");
        add.accept("research.thaumaturge.metallurgy.title", "Alchemical Metallurgy");
        add.accept(
                "research.thaumaturge.metallurgy.stage_0",
                "Brass is a sturdy, low friction metal perfectly suited to craft intricate mechanical devices.<BR>Unfortunately, it is not always possible to get hold of it due to the morphic properties of the universe I may find myself in.<BR>There should be a way of crafting it via alchemical transmutation. I simply need to find it.");
        add.accept(
                "research.thaumaturge.metallurgy.stage_1",
                "Metals have proven to be quite easy to manipulate via thaumaturgical principles. My first experiments has led to the discovery of Alchemical Brass, but it was a simple matter to create something that already exists. It is time to create something new.<BR>I have theorized that by infusing a base metal like iron with magical energy should result in something with very interesting properties.<BR> It should be harder than iron with the ability to accept enchantments beyond that which iron is normally capable of.<BR>A metal such as this should have many applications in the crafting of more advanced thaumaturgical devices.");
        add.accept(
                "research.thaumaturge.metallurgy.stage_2",
                "For now my studies are complete, but who knows what other wondrous discoveries I will make in the future?");
        add.accept("research.thaumaturge.metal_purification.title", "Metal Purification");
        add.accept(
                "research.thaumaturge.metal_purification.stage_0",
                "Native clusters are rare concentrations of metals in its purest form. These clusters, when smelted, produce twice the normal amount of ingots.<BR>If I can find a way of producing native clusters alchemically it would greatly reduce my need to constantly go mining.");
        add.accept("research.thaumaturge.hedge_alchemy.title", "Hedge Alchemy");
        add.accept(
                "research.thaumaturge.hedge_alchemy.stage_0",
                "It should be a fairly simple process to apply the alchemical principles I've learned to creating common materials.<BR>It will hardly be a challenge for me, but the practice should do me good.<BR>I realised that I have gathered an alarmingly large stock of rotten flesh for who knows what reason. Maybe it is time I put it to use and render it into tallow. It should be handy for crafting candles if nothing else.<BR>I should also be able to turn it into leather with a little bit of magical elbow grease.");
        add.accept(
                "research.thaumaturge.hedge_alchemy.stage_1",
                "Some substances are simple enough that just adding more of the same essentia it contains is enough to create more of that substance. This isn't true for everything, but the phenomenon will probably prove useful.");
        add.accept(
                "research.thaumaturge.hedge_alchemy.stage_2",
                "Some things you simply have to craft from scratch - all I need is a base material to add essentia to and with a bit of luck I can manufacture a few relatively common, but annoying to get materials.");
        add.accept(
                "research.thaumaturge.hedge_alchemy.stage_3",
                "For now my studies are complete, but who knows what other wondrous discoveries I will make in the future?");
        add.accept("research.thaumaturge.liquid_death.title", "Liquid Death");
        add.accept(
                "research.thaumaturge.liquid_death.stage_0",
                "Curiosity has taken me down a dark path. I wonder what a liquid made mostly from entropic and death energy would do. Nothing good I imagine.<BR>At the very least it should make a handy weed killer or control pests.");
        add.accept(
                "research.thaumaturge.liquid_death.stage_1",
                "Oh dear, what have I done? This liquid is so dangerously toxic that virtually nothing can prevent it from dissolving flesh, bone and even harder substances. Nothing alive or that even displays the semblance of life can survive it.<BR>Apart from obvious practical applications, this vile substance has another interesting property: Any essentia a dissolved creature possessed form into crystals that can be harvested.");
        add.accept("research.thaumaturge.bottle_taint.title", "Bottled Taint");
        add.accept(
                "research.thaumaturge.bottle_taint.stage_0",
                "I should be able to replicate the Taint causing properties of flux. I am not exactly sure why I would want to, but sometimes progress requires risk.<BR>The process should be fairly simple - I merely need to create a concentrated mixture of flux and it should easily react with almost anything to create Taint. ");
        add.accept("research.thaumaturge.bath_salts.title", "Purifying Bath Salts");
        add.accept(
                "research.thaumaturge.bath_salts.stage_0",
                "Warp is a danger for even the most careful of thaumaturges. For those that have done a bit too much dabbling, Warp effects are probably more than just an annoyance.<BR>I should make solving this problem a priority. Even if I am not able to remove Warp, at the very least I should be able to alleviate it's effects.");
        add.accept(
                "research.thaumaturge.bath_salts.stage_1",
                "I have drawn up a recipe to create relaxing and refreshing bath salts. Simply tossing these salts into water will turn the water into a silvery, purifying liquid that soothes away the stresses of the day and brings temporary relief from the effects of Warp. I should note however that the more warp a thaumaturge suffers from, the shorter the duration of this protection.<BR>The salts are quite fragile and will soon dissolve if tossed around haphazardly.");
        add.accept("research.thaumaturge.sane_soap.title", "Sanity Soap");
        add.accept(
                "research.thaumaturge.sane_soap.stage_0",
                "I seem to have stumbled upon something here. Bath Salts relax the body and brings temporary relief from Warp, but I need a more permanent solution. Something that will cleanse my body of Warp completely.<BR>Sympathetic magic could come in handy here. Soap cleanses the body, but with the proper amount of persuasion maybe it can cleanse the mind as well?");
        add.accept(
                "research.thaumaturge.sane_soap.stage_1",
                "Eureka! I have formulated a recipe for what I have named Sanity Soap (patent pending).<BR>This soap cleans the flesh, but the effects it has on the psyche is what truly makes it special. When used it has a small chance of reducing non-permanent warp and will remove all temporary warp.<BR>Using it while under the effects of a Warp Ward or while standing in Purifying Fluid will increase your odds.");
        add.accept("research.thaumaturge.arcane_spa.title", "Arcane Spa");
        add.accept(
                "research.thaumaturge.arcane_spa.stage_0",
                "Pouring bath salts into water is a bit of a chore, but luckily one easily solved with a bit of magic and ingenuity.");
        add.accept(
                "research.thaumaturge.arcane_spa.stage_1",
                "I have created a handy machine that when filled with water and bath salts, will automatically mix them and pump the liquid into an area above it. The liquid will fill a 5x5 area above the location where the machine is placed.<BR>Additionally, I have designed it in such a way that it can be used to pump nearly any liquid I can fill it with. It can be filled with automation, or by clicking on the block with the appropriate container. Applying a redstone signal will disable it.");
        add.accept("research.thaumaturge.essentia_smelter.title", "Essentia Smelting");
        add.accept(
                "research.thaumaturge.essentia_smelter.stage_0",
                "I see a time fast approaching where I will need to have ready access to multiple different kinds of essentia. For that I will need a source and the most obvious is the essentia found in, well, everything.<BR>Using alchemical principles, I should be able to find a way to break down objects and convert them into essentia. Before I can start however I will need to study essentia in the purest form available to me - vis crystals.<BR>By subjecting these crystals to, um, \"destructive testing\" I should be able to determine how they react in various extremes and the best way to preserve their purity.<BR>I should start with primal Essentia.");
        add.accept(
                "research.thaumaturge.essentia_smelter.stage_1",
                "My experiments are promising, if a bit unpredictable. Fortunately the fires were easily quenched and my hair should grow back in a few days.<BR>It seems melting objects down in a special alchemical process is my best option. Unfortunately my tests so far have been on primal crystals. I should also test my finding on crystals containing secondary essentia - that is essentia made up of a combination of two primal aspects.<BR>A third tier known as tertiary essentia also exists. That is essentia made up of a mixture of primal and secondary aspects, or two secondary aspects.");
        add.accept(
                "research.thaumaturge.essentia_smelter.stage_2",
                "I have managed to break down all my samples of primary and secondary essentia into a liquid state. The process has been slow and tedious however. Until I simplify the process it will never be practical for large scale usage.<BR>Luckily extracting essentia from objects has a lot in common with the smelting of metals. Using a furnace as a base I might be able to create an essentia smeltery that automatically melts down items and separates out individual types of essentia. ");
        add.accept(
                "research.thaumaturge.essentia_smelter.stage_3",
                "I have drawn up the plans for an Essentia Smelter. The smelter works much like a normal furnace by burning coal to produce heat, but instead of smelting ore into metal, it breaks down objects into a slurry of raw, unprocessed essentia. This process isn't very efficient and a small amount of essentia is converted into flux roughly 20% of the time.<BR>The next step in the process is to take the essentia slurry and distill it using an Arcane Alembic.<BR>To work, arcane alembics need to be placed on top of a smelter. Multiple alembics can be placed on top of each other to increase storage, but it does not influence processing speed. The essentia slurry will slowly be drawn into the alembic and the individual aspected essentia separated out.<BR>If the smelter is being powered by alumentum, this distillation process is much faster.<BR>The essentia can then be extracted out of an alembic using phials, jars or more complex means.");
        add.accept(
                "research.thaumaturge.essentia_smelter.addendum_0",
                "Bellows placed on the side of a smelter will greatly increase the heat it generates, allowing items to be broken down into a slurry much quicker.");
        add.accept("research.thaumaturge.warded_jars.title", "Warded Jars & labels");
        add.accept(
                "research.thaumaturge.warded_jars.stage_0",
                "Essentia can be stored in §lWarded Jars§r.<BR>Warded jars can be placed in the world and are able to store essentia virtually indefinitely. The enchantment placed on the jars also makes adding or extracting essentia a breeze.<BR>Jars can be filled from an alembic by clicking with the jar directly on the alembic. They can also be filled and emptied using phials.<BR>What is more they make an excellent storage medium for jams and preserves.<BR>Shift clicking on jars and alembics with an empty hand will empty them.<PAGE>As long as a jar contains some essentia, you can attach a §lJar Label§r to it.<BR>This label will 'lock' the jar to that type of essentia and it will not be able to accept any other kind of essentia.<BR>You can remove a label by shift clicking with an empty hand on the side of the jar containing the label. Take care not to shift click on any other side as it will empty the jar as normal.<BR>You can place a label in a crafting bench with a phial of essentia to assign an aspect to it and when placed on an empty jar it will automatically lock the jar to that aspect. The essentia used in the recipe is not consumed.<BR>Placing it on a filled jar will change the label to the jar's essentia type.<BR>A label can be made blank again by simply placing it by itself in a crafting grid.<PAGE>You can also craft §lBrass Lid Braces§r. When used on a jar, they will prevent essentia being drawn from or placed into the jar by anything other than tubes.<BR>Picking up the jar will remove the brace and return it as an item.");
        add.accept("research.thaumaturge.tubes.title", "Essentia Tubes");
        add.accept(
                "research.thaumaturge.tubes.stage_0",
                "I can transport essentia around in jars and flasks, but that is not useful for large scale alchemical operations. The obvious answer is to transport essentia with pipes or tubes, but due to its volatile nature no mundane pipe will be able to transport it.<BR>I should gather some exotic essentia samples and use them to test various materials.");
        add.accept(
                "research.thaumaturge.tubes.stage_1",
                "I have unlocked the key to creating stable and relatively leak-free tubing. While mundane chemicals can be transported in glass, certain types of essentia either dissolve or simply pass right through it. By wrapping the glass in iron and treating it with quicksilver I have managed to protect it from even the most volatile types of essentia. Brass fittings finish off the piping system and makes it visually appealing.<PAGE>The tubes can be connected to all manner of alchemical devices, though warded jars and alembics are their primary targets.<BR>Essentia flows through the tubes whenever something is connected to them that can accept it, like warded jars.<BR>Jars containing different kinds of essentia will only draw their kind of essentia towards them. Tubes however can only draw and transport one kind of essentia at a time - the highest drawing strength will take priority. Without the proper use of valves it is quite easy for a network of tubes to get gummed up. The use of valves is essential.<BR>When you see gasses being vented from a tube, it means different kinds of suction are interfering with each other. The color gives a clue as to the types of suction that is causing the problem.<LINE>Tubes can be connected and disconnected from their neighbors by clicking on a section of tubing with a wand.<BR>The wand can also be used to click on the central section to rotate certain tubes like the valve.<PAGE><IMG>thaumaturge:textures/item/resonator.png:0:0:255:255:.125</IMG>The Essentia Resonator is handy for troubleshooting your tube network. You can use it on a tube or other essentia using device to see what essentia it currently contains and the suction it is applying.<PAGE><IMG>thaumaturge:textures/item/tube_valve.png:0:0:255:255:.125</IMG>The essentia valve acts much like a normal length of tubing, except it can be turned off by hand or a redstone signal. When this is done the flow of essentia through it will be cut off.<PAGE><IMG>thaumaturge:textures/item/tube_filter.png:0:0:255:255:.125</IMG>Filtered essentia tubes work like normal tubes, except it is possible to mark them with labels, phials or other items that clearly identify a single type of essentia. When marked they will restrict essentia flow through them to what they have been marked with.<BR>Shift-clicking on them with an empty hand will remove the filter.<PAGE><IMG>thaumaturge:textures/item/tube_restrict.png:0:0:255:255:.125</IMG>Restricted essentia tubes work like normal tubes, except they halve the any suction passing through them.<PAGE><IMG>thaumaturge:textures/item/tube_oneway.png:0:0:255:255:.125</IMG>Directional essentia tubes only accept suction from one direction. The blue markings indicate the direction from which suction will be accepted.<PAGE><IMG>thaumaturge:textures/item/tube_buffer.png:0:0:255:255:.125</IMG>The essentia buffer allows you to store a limited amount of essentia - 8 points total, though it can be made up of any combination of essentia types. Like a jar it has suction, though it applies the minimum amount of suction possible (1).<BR>This means that is usually only able to draw essentia from an essentia source it is directly attached to. However tubes can be connected to all its sides, meaning that it is invaluable for getting essentia out of devices that could contain variable types of essentia.<BR>You can attach bellows to the buffer - each attached bellows increases its suction by 32.<BR>Additionally you can toggle individual connections on and off as with a normal tube, but if you shift click a connection, it will choke the suction back to 1 (blue band) or 0 (red band) on that side.<BR>This allows for some interesting possibilities when routing essentia.");
        add.accept("research.thaumaturge.essentia_smelter_thaumium.title", "Thaumium Essentia Smelter");
        add.accept(
                "research.thaumaturge.essentia_smelter_thaumium.stage_0",
                "The basic essentia smelter is quite crude, if I have to be honest with myself. It is extremely inefficient and rather slow. I chalk this up to the low quality of the materials it is crafted from.<BR>I believe Thaumium could prove useful in improving the smelting process. I should run some experiments to see how effective it would be at containing essentia slurry.<BR>I should probably also improve the plumbing inside the smelter itself. ");
        add.accept(
                "research.thaumaturge.essentia_smelter_thaumium.stage_1",
                "Thaumium has proven to be a good choice, though not quite as effective as I had hoped.<BR>The Thaumium Smeltery works much like the basic smeltery, but it has roughly 90% efficiency instead of 80%. Not nearly the improvement I was hoping for, but better than nothing I suppose.<BR>Its main benefit is that it breaks down essentia slurry at double the normal speed.");
        add.accept("research.thaumaturge.essentia_smelter_void.title", "Void Metal Essentia Smelter");
        add.accept(
                "research.thaumaturge.essentia_smelter_void.stage_0",
                "I need to go back to the drawing board. Using Thaumium did improve the smelting process, but not to the degree I had hoped.<BR>My recent discovery of Void Metal has opened up a new avenue of research for me. It is much more resilient and malleable than Thaumium. I should put it through the same battery of tests I did for Thaumium and see how the two compare.<BR>It might also be worth changing the internal plumbing to incorporate Void Metal in some manner.");
        add.accept(
                "research.thaumaturge.essentia_smelter_void.stage_1",
                "I am pleased with the results of my testing. I expect the Void Metal Smeltery will work much like the basic smeltery, but at roughly 95% efficiency instead of 80%.<BR>It should break down essentia slurry slightly quicker than the basic smeltery, but not nearly as fast as the thaumium smeltery.");
        add.accept("research.thaumaturge.improved_smelting.title", "Improved Essentia Distillation (part 1)");
        add.accept(
                "research.thaumaturge.improved_smelting.stage_0",
                "While essentia smelters can be very fast, they could always be faster - especially the speed at which they break down the essentia slurry into usable essentia.<BR>It should be a simple matter for me to speed up this process.");
        add.accept(
                "research.thaumaturge.improved_smelting.stage_1",
                "I have discovered a way to make essentia smelting faster and more efficient: Auxiliary Slurry Pumps.<BR>Each Auxiliary Slurry Pump placed on the side of a smelter acts as a secondary processor, outputting refined essentia to any alembics placed above it.");
        add.accept("research.thaumaturge.improved_smelting_2.title", "Improved Essentia Distillation (part 2)");
        add.accept(
                "research.thaumaturge.improved_smelting_2.stage_0",
                "Designing the the slurry pumps have given me an idea to reduce the amount of flux generated during the smelting process.<BR>I should follow up on those ideas as soon as possible.");
        add.accept(
                "research.thaumaturge.improved_smelting_2.stage_1",
                "I have discovered a way to manage the flux generated by the smelting process. By running the slurry through a secondary set of venting ports fitted with filters I am able to separate out the flux and safely vent it as harmless steam.<BR>Like the slurry pumps, the vents need to be placed on the side of the smelter.<BR>This process will reduce flux produced by about a third and additional vents will increase this, though there are diminishing returns.");
        add.accept("research.thaumaturge.centrifuge.title", "Essentia Centrifuge");
        add.accept(
                "research.thaumaturge.centrifuge.stage_0",
                "I often find myself with complex essentia I have no real need for. I need to find a way to put all that unused essentia to a practical use.");
        add.accept(
                "research.thaumaturge.centrifuge.stage_1",
                "The alchemical centrifuge is a wondrous invention that allows me to break essentia into its component parts.<BR>When compound essentia flows into the centrifuge from below it will be slowly broken down into the primal or compound essentia that it is made from. Only one of the components is created for each point of essentia broken down.<BR>The resulting essentia needs to be drawn from the top of the centrifuge. Because of the idiosyncrasies of the tube pressure system, it would be best to connect an Essentia Buffer to the top of the centrifuge.");
        add.accept("research.thaumaturge.thaumatorium.title", "Alchemical Automation");
        add.accept(
                "research.thaumaturge.thaumatorium.stage_0",
                "Sometimes alchemy can be a hit and miss affair - miscounted aspects, degrading essentia and ingredients that require manual labor to mix. I have had enough.<BR>Now that I have raw, liquid essentia available to me I should improve the entire alchemical process.");
        add.accept(
                "research.thaumaturge.thaumatorium.stage_1",
                "I have invented an automated device I have named the Thaumatorium. Like many multi part devices this one must be created using Salis Mundus.<BR>The Thaumatorium is a marvelous invention that allows a thaumaturge to select which formula to use and what catalysts to add. It then automatically draws essentia from available sources using tubes. As long as there is a supply of catalyst, it produces the required object perfectly, every time.<BR>The crucible that forms its base does not require water, but a heat source is still needed.<LINE>When I access the device there are several slots. The top-left slot is where I place the catalyst. Once I have done this, I can choose which recipe I wish to craft with that catalyst from a list of possibilities that appear below it.<BR>On the right will be shown the essentia required in the order they will be required. It will also display progress bars once the machine is in operation.<BR>The created object is ejected out the front of the device or into an inventory placed in front of the ejection port.<LINE>Normally a Thaumatorium can only have one active formula, but by attaching a Mnemonic Matrix to its side I can increase the number of formulas it can keep track of. Each matrix increases the number of formulas by 2.");
        add.accept("research.thaumaturge.essentia_transport.title", "Advanced Essentia Transport");
        add.accept(
                "research.thaumaturge.essentia_transport.stage_0",
                "During the infusion crafting process essentia is drawn directly from jars and floats through the air to the infusion matrix. The magic of the matrix keeps it stable enough for the infusion process, but normally the essentia is changed in such a way that it becomes unusable for anything else.<BR>If only I can find a way to draw essentia through the air outside of the infusion process and still keep it stable. ");
        add.accept(
                "research.thaumaturge.essentia_transport.stage_1",
                "I have cracked the method of keeping aerated essentia stable outside of infusion.<BR>I have drawn up plans for two devices. The Filling Essentia Transfuser can take essentia directly from a pipe network or other essentia source and transport it to jars within 16 blocks of the direction it is facing.<BR>The Emptying Essentia Transfuser does the opposite - drawing essentia from jars within range and placing it within tubes or devices requiring essentia of a specific type.");
        add.accept("research.thaumaturge.everfull_urn.title", "Everfull Urn");
        add.accept(
                "research.thaumaturge.everfull_urn.stage_0",
                "Alchemical creation has proven to be much easier than I expected. If I can somehow automate this process to create something simple and practical it will go a long way toward further alchemical breakthroughs.<BR>Maybe something like an infinite water source would be a good start?");
        add.accept(
                "research.thaumaturge.everfull_urn.stage_1",
                "A small fountain of pure water always flows from the top of this urn, making it a perfect water source to fill my buckets, bottles or other liquid containers.<BR>The urn will also automatically replenish any liquid containers within two blocks of it if they can accept water from the top. This is perfect for automatically refilling crucibles, but has other uses as well.<BR>Fluid pipes can be attached to the top of the urn for more mundane uses.<BR>Every bucket full of water created costs 1 vis which is drawn directly from the aura.<BR>The water does taste a bit... odd. Nothing to worry about I'm sure.");
        add.accept("research.thaumaturge.potion_sprayer.title", "Potion Sprayer");
        add.accept(
                "research.thaumaturge.potion_sprayer.stage_0",
                "I have made some initial tests to replicate the vanilla potion crafting system using essentia. My results have been mixed.<BR>I should focus my efforts to make it work.");
        add.accept(
                "research.thaumaturge.potion_sprayer.stage_1",
                "This has been an effort in frustration, but I have managed to salvage some success from the jaws of failure. I can indeed replicate the magical effects of a potion by combining the essentia it contains, but the result is extremely volatile. There is no way to simply decant the results into a potion bottle - at least none that I have found so far.<BR>The volatile substance immediately dissipates when exposed to air, but it can still transfer the magical effects to anything nearby, much like splash potion.<BR>I have crafted something that I am calling a Potion Sprayer. You supply it with a potion that acts as a template and then provide it with the correct types of essentia. The sprayer will then manufacture a dose of the potion and store it internally. I can store up to 8 such doses.<BR>When it receives a redstone signal it will emit a single dose of the potion in a 3x3x3 area in front of it.");
        add.accept("research.thaumaturge.base_artifice.title", "Basic Artifice");
        add.accept(
                "research.thaumaturge.base_artifice.stage_0",
                "I am now ready to begin crafting magical mechanisms and intricate arcane devices.");
        add.accept("research.thaumaturge.hungry_chest.title", "Hungry Chest");
        add.accept(
                "research.thaumaturge.hungry_chest.stage_0",
                "I should start with some simple projects to test my theories on animating inanimate objects.<BR>The humble hopper has proven an invaluable tool, but it requires a lot of iron to craft. Sometimes you simply do not have the iron to spare for something that (at its most basic) places objects into chests.<BR>But what if chests could gather their own items?");
        add.accept(
                "research.thaumaturge.hungry_chest.stage_1",
                "This chest, which I have decided to call a Hungry Chest, will gobble up any loose items that touches it. In effect it functions like a hopper, though it does not output any items it gathers to other inventories. ");
        add.accept("research.thaumaturge.levitator.title", "Arcane Levitator");
        add.accept(
                "research.thaumaturge.levitator.stage_0",
                "I have always been fascinated by the power of flight. I am sure flying is almost within my grasp, but first I need to learn the fundamentals.<BR>I need to study simple levitation first. ");
        add.accept(
                "research.thaumaturge.levitator.stage_1",
                "While not true flight, the Arcane Levitator is the next best thing.<BR>When placed the levitator lifts any item or creature above it up to 8 blocks into the air. A sneaking creature or player will be slowly lowered.<BR>While levitating something it consumes 1 vis per second from the aura.<BR>The levitator can be placed in any orientation, and when placed horizontally will push creatures along while keeping them aloft.<BR>A toggle on the back of the device allows you to alter the range at which it operates, but also changes the amount of vis it uses.");
        add.accept("research.thaumaturge.infernal_furnace.title", "Infernal Furnace");
        add.accept(
                "research.thaumaturge.infernal_furnace.stage_0",
                "I have found that a large part of my day tends to be taken up with smelting things in a furnace. Surely there is a way I can make this less of a chore?<BR>Time to go somewhere hot for inspiration. Somewhere really hot. ");
        add.accept(
                "research.thaumaturge.infernal_furnace.stage_1",
                "I took a trip to the nether and was inspired by what I found there. By harnessing raw §cIgnis§0 within a furnace crafted from obsidian, netherbrick and a pinch of Salis Mundus I will be able to make a highly efficient and fuel free furnace. So efficient in fact, that occasionally it will produce bonus materials in the form of nuggets, rare earths or other items.<BR>While this is essentially free, it is also very slow unless the furnace gets a steady supply of vis from the aura. It should be noted that the furnace will occasionally create a small amount of flux as well. I'm sure it is nothing to worry about.<LINE>Anything dropped into the top of the furnace will be smelted down and spit out the front - if the object is not normally smeltable it will be destroyed.<BR>I can place a chest or other container directly in front of the furnace to have it eject items into it.<PAGE>Cooking meat in the infernal furnace will produce meaty chunks as a byproduct. I can combine three different types of these chunks with sugar for a delicious and invigorating treat.");
        add.accept(
                "research.thaumaturge.infernal_furnace.addendum_0",
                "Attaching an arcane bellows to an Infernal Furnace has shown some interesting results. The elemental infused air not only stokes the flames increasing its smelting speed, but causes them to burn much purer, increasing the chance of creating bonus nuggets and other materials when smelting.");
        add.accept("research.thaumaturge.bellows.title", "Arcane Bellows");
        add.accept(
                "research.thaumaturge.bellows.stage_0",
                "I should start with some simple projects to test my theories. Animating the inanimate is no simple feat.<BR>Stoking furnaces with a bellows increases their heat and smelting speed, but pumping air by hand seems like a crazy waste of my time and talent. Now, if the bellows were able to pump itself that would be a different matter entirely.");
        add.accept(
                "research.thaumaturge.bellows.stage_1",
                "The Arcane Bellows performs much the same function as its mundane counterpart, the only difference is that it happily pumps away by itself sending forth a stream of never-ending air.<BR>As I had hoped they can be attached to furnances to increase their smelting speed.<BR>It is also possible to attach them to crucibles to reduce the time it takes for their water to boil.<BR>Bellows can be deactivated by applying a redstone signal.<BR>I am confident more uses will reveal themselves eventually. ");
        add.accept("research.thaumaturge.redstone_relay.title", "Redstone Relay");
        add.accept(
                "research.thaumaturge.redstone_relay.stage_0",
                "I have always been curious about the seemingly negative reaction between mystical devices and redstone signals. I should study this phenomenon further and find a practical use for its effects.");
        add.accept(
                "research.thaumaturge.redstone_relay.stage_1",
                "I have studied the matter and while it is a well-known phenomenon that redstone inhibits magic, I have discovered that in specific circumstances the opposite can be true as well.<BR>One practical application of this is the Redstone Relay. By sending a redstone signal through a magical substance like vis crystals you can inhibit it as much or as little as you want.<BR>Relays are placed much like redstone repeaters with their output side marked by a redstone torch.<BR>Unlike repeaters, relays have two toggles - the rearmost one determines the minimum input signal required to overwhelm this inhibiting effect and allow the relay to activate.<BR>The front toggle (next to the redstone torch) determines how strong the redstone signal is that the relay will output once it is active.<BR>This way, for example, you could have the relay emit a signal of strength 7 once it receives a signal of at least strength 2.");
        add.accept("research.thaumaturge.arcane_ear.title", "Arcane Ear");
        add.accept(
                "research.thaumaturge.arcane_ear.stage_0",
                "I should investigate the creation of arcane sensors that can react to things in the world. Maybe sound is a good place to start.");
        add.accept(
                "research.thaumaturge.arcane_ear.stage_1",
                "I have succeeded! The Arcane Ear is a device that is capable of detecting the sound emitted from nearby note blocks. If it hears a note it recognises, it will emit a short redstone pulse.<BR>The Arcane Ear needs to be tuned in a manner identical to note blocks, the tone and note it listens for depends on the material it is placed against.<BR>Arcane ears can be placed against any surface.<LINE>I can also craft a version of the arcane ear that toggles on or off when it hears a note instead of sending a pulse.");
        add.accept("research.thaumaturge.dioptra.title", "Thaumic Dioptra");
        add.accept(
                "research.thaumaturge.dioptra.stage_0",
                "While a thaumometer will show me the local aura it would be useful to be able to get a general overview of the aura in a larger area. A device like this should be simple enough to design and make. ");
        add.accept(
                "research.thaumaturge.dioptra.stage_1",
                "As I expected it was not hard to expand on the workings of the thaumometer to design the Thaumic Dioptra.<BR>This device allows a thaumaturge to see the surrounding aura in a simple and clear manner. It will render the aura in a 13 chunk diameter area around it as a simple height map. It can also be switched to show flux instead by simply clicking on it.<BR>A comparator can be connected if you want to monitor the aura or flux level of the chunk in which it is placed.");
        add.accept("research.thaumaturge.node_transducer.title", "Harnessing Vis");
        add.accept("research.thaumaturge.vis_relay.title", "Vis Relays");
        add.accept(
                "research.thaumaturge.vis_relay.stage_0",
                "An energized node holds its gateway open in a steady stream, but that power is trapped at the node itself. My instruments suggest the stream can be caught and redirected: a properly attuned crystal will resonate with an energized node and carry a measure of its output onward as a beam of faded light.<BR>The rate of flow is not fixed. The wider the node's reserves, the harder its stream presses against the crystal - a great node drives far more centivis through a relay each second than a meagre one. Each relay also gathers a small buffer of centivis over time, so a hungry device can gulp a burst of power and then wait while the stream refills it.");
        add.accept(
                "research.thaumaturge.vis_relay.stage_1",
                "The Vis Relay must be placed within eight blocks of an energized node, or within eight blocks of another relay already carrying a stream - chains of relays can carry power a great distance, though each stream can only wind through so many crystals before it frays.<BR>Devices simply drink from the nearest relay. A Recharge Pedestal within reach of a relay will feed its stream directly into any wand resting upon it, and the Arcane Workbench will happily pay crafting costs from a relayed stream before falling back to cruder methods.<BR>The relay itself moves power without loss, but remember the source: an energized node replenishes what is drawn by gulping raw vis from its local aura, and a network that drinks faster than the aura recovers will run the land dry.");
        add.accept(
                "research.thaumaturge.node_transducer.stage_0",
                "Without vis nodes thaumaturges would not even be able to perform the simplest of tasks, but the nodes themselves have several limitations that make their use impractical for extensive operations. Foremost amongst these limitations is the fact that they are easily depleted and it takes quite some time for them to once again gather energy from the mystical source they are connected to.<BR>You think you may have found a way around that limitation. In effect nodes are tiny gateways to a mysterious font of mystical energy around which the energy slowly accumulates in the form of Vis. The gateway is so small that only a tiny amount of energy leaks into our reality. By levering open this doorway you can cause vis to pour forth in a torrent.");
        add.accept(
                "research.thaumaturge.node_transducer.stage_1",
                "This is even more dangerous than it sounds however and careful steps must be taken during the entire process.<BR>Firstly the node needs to be stabilized during the entire process - a Node Stabilizer placed directly beneath it will serve. Take great care that no redstone signal ever reaches the stabilizer itself: a powered stabilizer shuts down, and the moment it does the transduction grinds to a halt. Once that is done you need to craft and place a Node Transducer above the node. When you apply a redstone signal to the transducer - and only the transducer - the node will begin converting to an energized node and quickly lose its stored vis in the process.<BR>The process takes some time, but when it is done the node will change into an energized node. Caution should be taken to keep both the stabilizer and transducer active from now on - if the transducer loses its redstone signal for too long, the node will revert and its energies will be lost.<LINE>An energized node is a changed thing. The widened gateway admits only the purest currents: any compound aspects the node once held are torn apart into their primal components for as long as it remains energized, and will only settle back into their old forms should the node ever revert.<BR>The node no longer condenses vis at its own gentle pace. Instead it gulps raw vis straight from the local aura to keep itself brimming - a swift but terribly wasteful process that will strain the aura of the surrounding land. Curiously, a tainted node put to the transducer gorges on flux instead, scouring its surroundings clean as it feeds.<BR>The widened gate also drowns out a node's wilder urges. A hungry node ceases its endless devouring, a tainted node stops spreading corruption, and a dark node no longer calls horrors to its side - for as long as the energy flows.<BR>Best of all, the torrent can be tapped like any other node: draw from it directly with a wand, or let a Recharge Pedestal placed nearby feed it into any wand resting upon it.<BR>I suspect this steady stream could even be caught and carried elsewhere - a matter worth further research.");
        add.accept("research.thaumaturge.mirror.title", "Mirror Magic");
        add.accept(
                "research.thaumaturge.mirror.stage_0",
                "My thaumonomicon provides some tantalizing hints that mirrors have many mystical uses. One such use is the ability to link physical locations via mirrors and the use of sympathetic magic.<BR>The book does nothing more than provide hints however - as usual I will have to figure out things myself.");
        add.accept(
                "research.thaumaturge.mirror.stage_1",
                "I am now able to enchant a pair of magical mirrors that act as doorways to the void between the worlds. Anything shoved (or piped) through one mirror will pass through this strange realm and emerge from the other mirror - even across dimensional boundaries.<BR>Unfortunately this realm is extremely hostile to mortal creatures like myself so using these mirrors to transport living creatures is not yet possible.<BR>To link a pair of these magical mirrors you simply need to place one of them in a desired location. You then right-click with a second mirror on the one you placed, and the pair will become linked. You may then place the second wherever you wish and items thrown into one will be ejected out the other.<BR>Mirrors can only be linked in pairs, so while both are placed in the world and able to transmit items you cannot link them to another mirror. When you pick up a mirror it will remember the last partner it was linked with, allowing you to easily move it without having to relink it. While one of the paired mirrors is picked up, you may easily relink either of them.<BR>Transporting items through mirrors places stress on the magical aura. Every now and again this causes Flux to be generated. The more items you transport, the more flux is generated.");
        add.accept("research.thaumaturge.share_book.title", "Thaumonomicon of Sharing");
        add.accept(
                "research.thaumaturge.share_book.stage_0",
                "Mirror magic has taught me that two objects, properly attuned, may share what passes between them across any distance. Matter is one thing - but what of thought?<BR>My thaumonomicon is not truly a book at all. It is a window into my own accumulated understanding. If I could craft a copy attuned to my mind, perhaps another thaumaturge could peer through that window and take from it what I have learned.");
        add.accept(
                "research.thaumaturge.share_book.stage_1",
                "I have crafted a Thaumonomicon of Sharing - a strange, half-empty tome that hungers for an imprint.<BR>Using it once attunes it to my mind, filling its pages with a reflection of everything I have researched. If I then hand the book to a fellow researcher and they open it, the whole of my recorded knowledge pours into their mind in a single sitting.<BR>The transfer is a gift, freely given and complete - though I suspect the recipient will still have to earn their own understanding of anything I myself have not yet mastered. The book merely copies what is written; it forges no lasting bond between us.");
        add.accept("research.thaumaturge.link_book.title", "Thaumonomicon of Binding");
        add.accept(
                "research.thaumaturge.link_book.stage_0",
                "The Thaumonomicon of Sharing merely copies - a single pouring of knowledge from one vessel into another, after which each mind goes its own way.<BR>But in my studies of the void I have glimpsed something more: threads that join minds the way tubes join jars, and do not sever when the vessel is set down. The whispers suggest such a bond need never close.<BR>I am not entirely certain I should build such a thing. I am entirely certain I am going to.");
        add.accept(
                "research.thaumaturge.link_book.stage_1",
                "I have infused a Thaumonomicon of Binding. Like its lesser cousin it must first be opened by me to attune itself, then placed in the hands of another.<BR>When they open it, the book is spent - but the bond it forges is not. From that moment our minds are joined: every discovery either of us makes flows quietly to the other, no matter the distance between us, as though we were two pages of the same book.<BR>The bond does not fade. I do occasionally wonder, late at night, whose thoughts I am thinking - but that seems a small price for never having to explain my research twice.");
        add.accept("research.thaumaturge.mirror_hand.title", "Magical Hand Mirror");
        add.accept(
                "research.thaumaturge.mirror_hand.stage_0",
                "I should find a way of interacting with hand mirrors while out and about. I could carry a linked mirror with me, but that seems inconvenient");
        add.accept(
                "research.thaumaturge.mirror_hand.stage_1",
                "I have created a magical hand mirror that functions in much the same way as other magical mirrors. I simply push items through the glass and they will pop out at the linked mirror.<BR>Right-click while holding the linked hand mirror and I will be able to place any items in my inventory into the looking glass to have them transported.<BR>The hand mirror can be linked to a mirror that is already paired with another.<BR>I should note that the hand mirror can only send items, not receive them.");
        add.accept("research.thaumaturge.mirror_essentia.title", "Essentia Mirrors");
        add.accept(
                "research.thaumaturge.mirror_essentia.stage_0",
                "Magical mirrors are very useful for transporting items over long distances, but what if that same principle can be applied to essentia?");
        add.accept(
                "research.thaumaturge.mirror_essentia.stage_1",
                "Essentia mirrors, like their name implies, can transport essentia in the same way that magic mirrors transport items. There are a few important limitations however.<BR>I cannot pipe essentia directly into the mirrors - they are only able to transport essentia that has been drawn from a source towards something like an Infusion Matrix.<BR>In effect the mirrors allow these things to draw essentia from sources via the mirror.<BR>Like item mirrors, these mirrors cause great stress to the magical aura and will generate flux.");
        add.accept("research.thaumaturge.arcane_lamp.title", "Arcane Lamp");
        add.accept(
                "research.thaumaturge.arcane_lamp.stage_0",
                "My laboratory has seemed quite gloomy of late. I should find a way to light it more effectively.");
        add.accept(
                "research.thaumaturge.arcane_lamp.stage_1",
                "This mystical lamp can be attached to walls, floors or ceilings and will provide strong and steady light.<BR>Additional sources of light may also appear up to 16 blocks away wherever light levels fall below accepted 'safe' levels. Remember: Only YOU can prevent zombie outbreaks.<BR>These secondary sources of light will disappear soon after the lamp has been removed.<BR>The lamp can be turned off by applying a redstone signal.");
        add.accept("research.thaumaturge.lamp_growth.title", "Lamp of Growth");
        add.accept(
                "research.thaumaturge.lamp_growth.stage_0",
                "My studies of Lux has shown me that light can be used as a medium for other helpful magics.<BR>Light already has a positive effect on plants so if I add some magic to the equation it should only improve things, right?");
    }

    private static void part2(BiConsumer<String, String> add) {
        add.accept(
                "research.thaumaturge.lamp_growth.stage_1",
                "Unlike the Arcane lamp from which it is made, the Lamp of Growth does not cast much light unless it is provided with §2Herba§0 essentia. If this is done, any plants which grow where its light shines the brightest will grow much faster than normal.<BR>Essentia can be fed to the lamp by attaching it directly to an essentia piping system.<BR>The lamp can be turned off by applying a redstone signal.");
        add.accept("research.thaumaturge.lamp_fertility.title", "Lamp of Fertility");
        add.accept(
                "research.thaumaturge.lamp_fertility.stage_0",
                "My studies of Lux has shown me that light can be used as a medium for other helpful magics.<BR>Mundane light has been proven to have both positive and negative effects on moods. I think I might be able to greatly improve that effect by adding some arcane flair.");
        add.accept(
                "research.thaumaturge.lamp_fertility.stage_1",
                "Any living creatures bathed in the gentle pink glow of this lamp is immediately placed in a much more romantic mood. If they are able, they will seek others of their kind to mate.<BR>This lamp will have no effect if there is 8 or more of a particular species already present.<BR>It requires §dDesiderium§0 essentia to function which can be fed to the lamp by attaching it directly to an essentia piping system.<BR>The lamp can be turned off by applying a redstone signal.");
        add.accept("research.thaumaturge.arcane_pattern_crafter.title", "Arcane Pattern Crafter");
        add.accept(
                "research.thaumaturge.arcane_pattern_crafter.stage_0",
                "Certain objects are very simple to craft - simply place the same type of item in a pattern and you get what you want. For example combining 2 wood planks to make a stick, or a single ingot making 9 nuggets. This process is so simple that I am sure there is a way to automate it. I have some theories I need to try.");
        add.accept(
                "research.thaumaturge.arcane_pattern_crafter.stage_1",
                "I have designed a device I am calling the Arcane Pattern Crafter. The Arcane Pattern Crafter needs an inventory above and below it. It will search the inventory above it for components and try to combine whatever it finds in the pattern you have selected. If this results in a valid crafting operation it will place the result in the container below it.<BR>You can change the pattern by clicking on the brass dial on the front of the device.<BR>A redstone signal will disable the device.<LINE>Each crafting operation will drain 1 vis from the aura.");
        add.accept("research.thaumaturge.basic_turret.title", "Automated Crossbow");
        add.accept(
                "research.thaumaturge.basic_turret.stage_0",
                "A few attacks from wandering monsters has recently turned my mind towards home defense. I should design some sort of automated device that will defend my home from unwanted intruders. ");
        add.accept(
                "research.thaumaturge.basic_turret.stage_1",
                "I have found a way to give a crossbow a mind of its own, turning it into a simple automated turret.<BR>When placed in the world it will automatically fire at any hostile monsters it detects within range. Unfortunately its limited mind does not allow me to fine tune its targets.<BR>It has a very limited internal inventory, but if I place it over an upward facing dispenser it will automatically reload itself from that inventory.<BR>Right clicking on it while sneaking will pick it up.<PAGE>The turret can ride in a minecart and will reload from a dispenser placed underneath the track.<BR>Passing over an arcane activator rail will change the behaviour of the turret. If the rail is active it will disable the turret. Passing over an inactive rail will enable it again.");
        add.accept("research.thaumaturge.advanced_turret.title", "Advanced Automated Crossbow");
        add.accept(
                "research.thaumaturge.advanced_turret.stage_0",
                "My recent discovery of advanced artificial minds might allow me to make some much needed improvements to my automated turrets. ");
        add.accept(
                "research.thaumaturge.advanced_turret.stage_1",
                "The Advanced Automated Crossbow works much like its basic counterpart, with a few significant improvements.<BR>The addition of iron plating makes it much sturdier, and a more complicated loading mechanism gives it a greater rate of fire.<BR>Lastly, it is possible to specify the type of targets it can attack.");
        add.accept("research.thaumaturge.arcane_bore.title", "Arcane Bore");
        add.accept(
                "research.thaumaturge.arcane_bore.stage_0",
                "I need to work out an automated way of digging out large areas. It should be a fairly simple matter to redesign one of my new turrets to target the ground instead of enemies.<BR>My discovery of the breaker focus effect should allow me to create a specialized and limited version of a wand that can be used by this mining turret. ");
        add.accept(
                "research.thaumaturge.arcane_bore.stage_1",
                "I have designed a mining device I am calling the Arcane Bore. Once placed and given a redstone signal it will start digging out an area in front of it. The device requires a pickaxe to be placed inside it. Normally inanimate objects cannot use foci, but with a liberal application of sympathetic magic the focus could be 'tricked' into working.<BR>This had a unforeseen benefit as well - the pickaxe is actually being used to dig on metaphysical level and any attributes and enchantments it has are actually applied to the digging process. The pickaxe will wear out however, but much slower than normal - I estimate an average pickaxe will last about 50 times longer.<BR>The size of the area dug is also determined by the material of the pickaxe. The more enchantable the material is, the more efficient the sympathetic magic is, which in turn increases the area that can be dug.<BR>Any blocks dug are ejected at the position of the bore. If there is an inventory adjacent to the bore the items will be placed in there instead.<PAGE>The bore also requires vis to function consuming 1 vis for every 4 blocks dug.<LINE>The bore can be turned to face any direction except down. To change the direction it faces simply left-click on it to make it face towards you.<BR>Like other arcane turrets the bore will also respond to arcane activator rails - passing over an inactive rail will switch it on while an active one will disable it.");
        add.accept("research.thaumaturge.grapple_gun.title", "Arcane Grappler");
        add.accept(
                "research.thaumaturge.grapple_gun.stage_0",
                "In my endeavours thus far I have learned an important fact: Mobility is key.<BR>I should create a tool that will allow me to traverse difficult terrain with ease.<BR>I believe magnets are the answer. Rare earths can be used to create magnets, but they are limited in what they can stick to. If I can enhance their innate magnetic properties with some form of sympathetic magic, it should allow them to stick to anything.");
        add.accept(
                "research.thaumaturge.grapple_gun.stage_1",
                "By manipulating magnets with vis, and a few key enchantments, they are able to stick to virtually anything. I am calling this phenomenon Arcanomagnetism. I am sure I will find many uses for this, but for now I have used this knowledge to create an Arcane Grappler.<BR>This launcher fires a brass globe enhanced with arcanomagnets that will attach to any block it comes into contact with. The globe, in turn, is attached to the launcher with a spool of rope that will reel the user towards the globe allowing him access to previously inaccessible locations.<BR>The user can detach the globe at any point by crouching.<BR>I should take care using the grappler as it does not protect me from falling and it is possible to gain impressive speeds if pulled far enough.<LINE>The grappler requires vis to function and will need to be recharged.");
        add.accept("research.thaumaturge.vis_generator.title", "Vis Powered Generator");
        add.accept(
                "research.thaumaturge.vis_generator.stage_0",
                "It is a well known fact that vis does not play well with mundane power sources like redstone, redstone flux or forge energy.<BR>The question is: Can you use vis to generate power? The obvious answer is no, but if I settled for the obvious I do not deserve to call myself a thaumaturge.");
        add.accept(
                "research.thaumaturge.vis_generator.stage_1",
                "One more impossible thing to mark off my list and it is not even lunchtime yet.<BR>I have managed to generate mundane power using vis, though I had to cheat a bit to do it. It is a simple matter to turn kinetic energy into power and creating kinetic energy with vis is thaumaturgy 101. Using vis to turn an axle connected to a dynamo did not work as some magical energy would transmit through the axle and cause all kinds of problems.<BR>In the end I designed a special linkage using magnets and some clever engineering. This linkage would transmit the kinetic energy while not allowing the vis and mundage energy to interfere with each other.<BR>Unfortunately the nature of the linkage places some limits on the power that can be generated as some kinetic energy is lost. It draws vis directly from the aura to do this and a single point produces 1000 FE/RF though it can only output 20 FE/RF per tick. <BR>A redstone signal will disable the generator.");
        add.accept("research.thaumaturge.base_infusion.title", "Basic Infusion");
        add.accept(
                "research.thaumaturge.base_infusion.stage_0",
                "I am now ready to begin crafting enchanted objects and tools.<LINE>I have drawn up designs for some banners to decorate my lab. I can apply a phial of essentia to them to have them display that essentia symbol.");
        add.accept("research.thaumaturge.infusion.title", "Infusion");
        add.accept(
                "research.thaumaturge.infusion.stage_0",
                "Arcane crafting and alchemy can go only so far. Something better is needed to create truly powerful mystical objects.<BR>I believe that it should be possible to infuse objects with essentia directly. You could, for example, infuse a stone with §6Aer§0 vis and a feather and it would result in a very light stone.<BR>At least, that is my theory.");
        add.accept(
                "research.thaumaturge.infusion.stage_1",
                "Unsurprisingly, infusion crafting is proving to be a lot more complex than I expected. Simply trying to infuse aspects into something does not work. I will need a relatively complex system to stabilize the entire process. I have drawn up plans for a Runic Matrix that will allow me to do this. ");
        add.accept(
                "research.thaumaturge.infusion.stage_2",
                "To even get started with infusion crafting there are several things I will need: The Runic Matrix (which I now have), arcane pedestals and a ready supply of magic in the form of essentia.<BR>Once you have your Runic Matrix properly placed into a structure commonly known as an Infusion Altar, you can begin to craft. The altar is crafted with the use of Salis Mundus, like most other mystical structures. Once the pillars have formed, the runic matrix can be activated using a casting tool.<BR>The central pedestal located under the matrix is where the item you wish to infuse will go. You will also need several more pedestals placed around the altar where you can place the blocks and items you wish to infuse into the target object. Lastly you will need warded jars or similar essentia containers holding the requisite amount of vis.<BR>Once all this is gathered you can click on the Runic Matrix with a casting tool to start the crafting process.<BR>During the first stage of crafting, essentia will be drained from nearby sources. Crafting will stall if there is insufficient essentia available - something that you do not want and will be explained on following pages.<BR>Once all the required essentia has been infused into the target object, the other objects will have their essential essence drained. Only when this is done will the crafting process complete.<LINE>Infusion crafting is not without risks. The entire process involves forcing vast energies into a single object. Unpredictable things tend to happen.<BR>Usually one of the fusion objects get knocked off a pedestal, or flux gets generated, but more catastrophic events are not unheard of.<BR>The longer crafting continues due to unresolved problems like lack of vis or dropped items, the greater the chance of something bad happening.<BR>These effects can be reduced by various means. Firstly you wish to keep the entire fusion structure as symmetrical as possible - take careful note of where you place pedestals and make sure they are balanced with other pedestals on the opposite side of the altar. When placing fusion items into them you may wish to keep them balanced as well. Lastly you can place occult paraphernalia around the altar in symmetrical formations (things like candles, skulls, etc.). These will have the effect of reducing instability.<IMG>thaumaturge:textures/research/research5.png:128:0:128:136:1</IMG>");
        add.accept("research.thaumaturge.infusion_stable.title", "Infusion Stabilization");
        add.accept(
                "research.thaumaturge.infusion_stable.stage_0",
                "Infusion has proven to be much more unstable than I expected. Placing occult paraphernalia does mitigate the effect somewhat, but much less than the texts suggested. The more items of the same type you place, the less effective they are.<BR>I need to find more effective ways of mitigating instability. Strangely enough, I think mundane technology might have the answer.<BR>It is well known that mundane power sources like redstone interferes with thaumaturgy. Of course, throwing redstone into the mix of an already unstable infusion is not the answer, but maybe there is some way to harness the interference.");
        add.accept(
                "research.thaumaturge.infusion_stable.stage_1",
                "I have put together something I am calling, appropriately enough, a Stabilizer.<BR>The core of the Stabilizer is made from a block of redstone that has been bombarded by vis. As a result it has become an instability magnet of sorts. Unstable magical energy is drawn to it and discharged as flux. While not ideal, flux is easier to manage than losing expensive crafting ingredients.<BR>While Stabilizers provide a fair amount of stability to the infusion process when placed near the altar, they have a much more important function: When discharges occur that would normally knock items off their pedestals or even destroy them, the discharge can instead be redirected to the Stabilizer and absorbed.<BR>Unfortunately the Stabilizer needs a little help to perform this function. Pedestals you wish to protect needs to be connected to the Stabilizer via Redstone Inlay. This is placed much like normal redstone and even has similar limits as redstone. For example the protective charge created by the Stabilizer cannot reach more than 15 blocks. Pedestals themselves act as redstone inlay and will propagate the protective charge. A properly connected pedestal will have runes appear at its base.<BR>When an instability discharge hits a protected pedestal, the discharge will travel along the inlay towards the nearest Stabilizer and discharge there instead. This temporarily weakens the stabilizer and the protective charge will be reduced till the Stabilizer has had a chance to discharge the unstable energy as flux. I may wish to use multiple stabilizers to prevent overloading a single one and leaving some pedestals temporarily unprotected.<BR>It should be noted that Redstone Inlay provide a small amount of stability themselves, so I should take care to keep their placement properly balanced.");
        add.accept("research.thaumaturge.infusion_boost.title", "Infusion Boosting");
        add.accept(
                "research.thaumaturge.infusion_boost.stage_0",
                "There should be a way to improve the function of the Infusion Altar. I should investigate this.");
        add.accept(
                "research.thaumaturge.infusion_boost.stage_1",
                "I have found a way to improve infusion altars through the use of special blocks placed under one or more of the four altar pillars.<BR>The §5Infusion Speed Stone§0 will increase the speed at which the altar operates, but at a slight increase to costs.<BR>The §5Infusion Cost Stone§0 will decrease infusion costs, but at a slight reduction to operating speed.<BR>These stones must be placed directly under an altar pillar, but they need not be balanced for instability purposes.");
        add.accept("research.thaumaturge.infusion_ancient.title", "Infusion: Ancient Stone");
        add.accept(
                "research.thaumaturge.infusion_ancient.stage_0",
                "The strange, ancient stone I have found has some interesting mystical properties. I think it might be useful in the infusion process.");
        add.accept(
                "research.thaumaturge.infusion_ancient.stage_1",
                "I believe that when this ancient stone is used to craft the pillars for an infusion altar that it will help reduce the amount of essentia required during infusion crafting and speed up the crafting process by a small amount. It will however increase instability slightly.<BR>If I find pedestals crafted from the same material, each one will also help reduce essentia costs.");
        add.accept("research.thaumaturge.infusion_eldritch.title", "Infusion: Eldritch Stone");
        add.accept(
                "research.thaumaturge.infusion_eldritch.stage_0",
                "The dark, eldritch stone I have discovered has some strange properties. I think it might be useful in the infusion process.");
        add.accept(
                "research.thaumaturge.infusion_eldritch.stage_1",
                "I believe that when this eldritch stone is used to craft the infusion altar pillars it will help reduce instability and speed up the crafting process by a moderate amount. It will unfortunately increase crafting cost slightly.<BR>Pedestals made from the same material should have the same effect. Caution should be taken to carefully balance them however as unbalanced eldritch pedestals will massively increase your instability.");
        add.accept("research.thaumaturge.paving_stones.title", "Arcane Paving Stones");
        add.accept(
                "research.thaumaturge.paving_stones.stage_0",
                "Arcane stone has no innate magical properties of its own, but acts as a spell sponge of sorts. I should be able to add all manner of enchantments to them.");
        add.accept(
                "research.thaumaturge.paving_stones.stage_1",
                "<PAGE>§oPaving Stone of Travel§r<DIV>By altering the magical structure of arcane stone I am able to create a paving stone that adds new bounce to the step of anyone walking across it.<BR>Anything walking across it will have greater speed and agility for a couple of seconds.<PAGE>§oBarrier Stone§r<DIV>I have discovered a way of turning arcane stone blocks into mystical wards that will prevent most creatures from crossing them.<BR>It should be noted that these wards aren't always 100% foolproof and they cannot prevent other players from entering an area, but they keep most common threats at bay. They will also prevent golems from crossing them.<BR>For best results you need an unbroken line of them between the areas you wish to protect and the outside world.<BR>An active redstone signal will disable this block.<BR>The color of the runes above the stone indicates its current status.<BR>Purple runes show that it is currently preventing something from passing through.<BR>Blue runes indicates it has been deactivated with a redstone signal.<BR>Red runes shows that something is preventing it from fully warding an area and a gap exists where something might be able to pass through.");
        add.accept("research.thaumaturge.boots_traveller.title", "Boots of the Traveler");
        add.accept(
                "research.thaumaturge.boots_traveller.stage_0",
                "I need to create something that will ease my travels. I should examine the mechanics of movement and how one can go about improving it.");
        add.accept(
                "research.thaumaturge.boots_traveller.stage_1",
                "I have designed a pair of enchanted boots that will ease the journey of any traveler.<BR>They will allow one to move faster, jump higher and fall further.<BR>Lastly they allow you to cut through water like a fish. A slow and clumsy fish, but a fish nonetheless.<BR>This power does have a cost however - the boots need to be charged with vis for their abilities to function.");
        add.accept("research.thaumaturge.elemental_tools.title", "Elemental Tools");
        add.accept(
                "research.thaumaturge.elemental_tools.stage_0",
                "With the discovery of infusion, I should experiment with binding primal elements into tools and weapons. If I am correct this will grant them unique and powerful properties.<BR>For my initial experiments I should stick to working with thaumium tools - they will be easy to infuse and will be a good base for future experiments.");
        add.accept(
                "research.thaumaturge.elemental_tools.stage_1",
                "I have drawn up the required rites to infuse the thaumium tools I have created. If my calculations are correct they will have some unique and interesting abilities.<PAGE>§oAxe of the Stream§r<DIV>By combining water shards and a thaumium axe, I have designed a tool that embodies the aspects of motion and flow.<BR>It functions like a normal axe on all materials except natural logs and trees. When used on them the axe seeks to furthest block of the same type that is still connected to the block you are attempting to harvest and harvests that one instead.<BR>In effect you will chop trees from the top down even if you hit the bottom-most block.<BR>Any wood chopped in this fashion will automatically float towards you.<BR>If you press and hold right click the axe will axe will draw all nearby items towards you - not just wood it chopped.<BR>If shift is held while using this tool all its special properties are suspended and it functions like a normal axe.<PAGE>§oPickaxe of the Core§r<DIV>By infusing a thaumium pickaxe with fire shards, I have designed a pickaxe that glows with the heat of a forge.<BR>When you mine ore blocks there is a chance that you will mine a cluster instead. Native clusters are much easier to process and produces double the normal amount of metal when smelted.<BR>It can also be used to sound out any surface you right click against to find out what valuable ores can be found nearby.<BR>If shift is held while using this tool all its special properties are suspended and it functions like a normal pickaxe.<PAGE>§oSword of the Zephyr§r<DIV>I have merged the elemental fury of air shards with a thaumium sword.<BR>When used to strike a foe, the blade will sweep in a wide arc, hitting additional foes near your main target.<BR>While blocking the sword will conjure a sphere of whirling winds around you, pushing enemies and loose objects away and deflecting incoming projectiles.<BR>While blocking it will also slowly lift you into the air.<PAGE>§oShovel of the Earthmover§r<DIV>Combining earth shards with a thaumium shovel I have designed a peerless digging implement.<BR>When used to dig earth, sand and similar materials it will dig a 3x3 area.<BR>If you right click on the side of a block, it will attempt to place a 3x3 section of the same material in the world as long as you have sufficient blocks in my inventory.<BR>The orientation of both the digging and construction depends on the face you dug or clicked.<BR>If shift is pressed you can see where blocks will be placed in the world and you can change the orientation of how blocks are placed by pressing 'G' on the keyboard. Blocks can be placed either parallel to the block face selected, or extending out of the face vertically or horizontally.<BR>If shift is held while using this tool it will no longer dig in a 3x3 pattern.<PAGE>§oHoe of Growth§r<DIV>By merging the magical energy from order and entropy shards with a thaumium hoe, I have created a farming implement with few equals.<BR>When used to till fields, it will till a 3x3 area instead of just a single block.<BR>When used to crops or other plants it will act as bonemeal to promote its growth, though at a higher cost to its durability. It might even be able to grow plants that normal bonemeal can't.<BR>If shift is held while using this tool all its special properties are suspended and it functions like a normal hoe.");
        add.accept("research.thaumaturge.infusion_enchantment.title", "Infusion Enchantment");
        add.accept(
                "research.thaumaturge.infusion_enchantment.stage_0",
                "The elemental tools have proven quite useful, but the method I use to enchant them is inflexible. I must discover a way to apply many of the same abilities to nearly any tool or weapon, not just thaumium tools.<BR>A good place to start would be to study a normal enchantment and see how I can apply some of those primitive principles to the infusion process.");
        add.accept(
                "research.thaumaturge.infusion_enchantment.stage_1",
                "I have cracked the problem and should now be able to add infusion enchantments to most tools or weapons. Some enchantments can be applied multiple times to the same item, but each rank has an increased essentia cost when crafted.<BR>Each different infusion enchantment on the item also increases the cost by 33% and has an increased chance of giving it the Warping property.<BR>Certain infusion enchantments can only be applied to specific tools or weapons. This will be listed under each individual entry that follows.<BR>Tools with infusion enchantments can still be enchanted with the normal enchanting table and visa versa.<BR>Like with the elemental tools, sneaking while using an infusion enchanted tool will suppress its abilities.<PAGE>§oCollector§r<DIV>Whenever this tool harvests an item it will automatically float towards you.<DIV>§oRanks§r: none<BR>§oTarget§r: axe, pickaxe, shovel, weapon<PAGE>§oBurrowing§r<DIV>When you try to harvest a tree or ore the furthest block will be harvested, instead of the one you are trying to break.<DIV>§oRanks§r: none<BR>§oTarget§r: axe, pickaxe<PAGE>§oRefining§r<DIV>Whenever you harvest ore there is a chance of gaining a native cluster instead. Increasing the rank of the enchant will improve the chance.<DIV>§oRanks§r: 1-4<BR>§oTarget§r: pickaxe<PAGE>§oSounding§r<DIV>You can sneak + right click on a block with this tool to send out a sounding pulse. This pulse will reveal any ores hidden nearby. Using this ability will cause some damage to the tool.<DIV>§oRanks§r: 1-4<BR>§oTarget§r: pickaxe<PAGE>§oDestructive§r<DIV>When you harvest a block the 8 blocks surrounding (3x3 area) it will also be harvested if they are normally harvestable by the tool. Each additional block harvested causes durability loss as if you had harvested them by hand.<DIV>§oRanks: none<BR>§oTarget§r: axe, pickaxe, shovel<PAGE>§oArcing§r<DIV>When you strike an enemy with a weapon enchanted with this, an arc of biting wind will hit an additional target nearby for half your weapons damage. Additional ranks will increase the number of additional targets and the range.<DIV>§oRanks§r: 1-4<BR>§oTarget§r: weapon<PAGE>§oEssence Harvester§r<DIV>When you you kill a creature with a weapon enchanted with this it has a chance of dropping some of its essence in crystal form. Increasing the rank of this enchantment increases the chance of crystals dropping and may also increase the number dropped.<DIV>§oRanks§r: 1-5<BR>§oTarget§r: weapon<PAGE>§oLamplighter§r<DIV>When you break a block this enchantment will place an invisible and intangible light source at the location if the light level is below 10.<DIV>§oRanks§r: none<BR>§oTarget§r: axe, pickaxe, shovel");
        add.accept("research.thaumaturge.runic_shielding.title", "Runic Shielding");
        add.accept(
                "research.thaumaturge.runic_shielding.stage_0",
                "It has proven simple enough to enchant objects with offensive and utilitarian abilities, but some defensive abilities would be useful as well. I should investigate this.");
        add.accept(
                "research.thaumaturge.runic_shielding.stage_1",
                "My research into protective magics have led me to discover Runic Shielding. This is not armor in the traditional sense, but a way of enchanting most baubles and armor to create a barrier of magical energy that wards out most types of damage. The shielding has a pool of hit points from which damage is subtracted first, protecting the wearer from all harm. Once this pool is depleted the wearer starts taking damage as normal. The pool replenishes over time and requires vis to replenish its charge, which it draws from the aura.<BR>An object can be enchanted multiple times to increase the amount of protection it offers, though it becomes more expensive each time.<BR>The armor offers no protection from damage suffered due to certain environmental hazards.");
        add.accept("research.thaumaturge.armor_fortress.title", "Thaumium Fortress Armor");
        add.accept(
                "research.thaumaturge.armor_fortress.stage_0",
                "Thaumium armor has many things to recommend it, but its basic construction leaves it inferior to diamond armor. I should find a way to improve the construction process and complexity.");
        add.accept(
                "research.thaumaturge.armor_fortress.stage_1",
                "I set out to improve thaumium armor and I think I have outdone myself. I have drawn up designs for a suit of armor of exceptional craftsmanship, durability and strength. I am calling it Thaumium Fortress Armor.<BR>Each individual piece of Thaumium Fortress armor has the same protective properties as diamond armor, but has greater durability and offers additional protection against magic and fire.<BR>The biggest benefit however is that the individual armor pieces are designed and constructed to be worn together: Each additional piece of this armor worn increases the overall protection offered by all other pieces and improves its visual appeal.<BR>As with most objects constructed from thaumium, the armor is easy to enchant.");
        add.accept("research.thaumaturge.fortress_mask.title", "Fortress Armor Faceplates");
        add.accept(
                "research.thaumaturge.fortress_mask.stage_0",
                "I designed the faceplate used by fortress armor to be removable. I might be able to create specially enchanted faceplates that gives the armor additional abilities.");
        add.accept(
                "research.thaumaturge.fortress_mask.stage_1",
                "I have drawn up designs for several magical masks that will replace the standard faceplate used by fortress armor.<PAGE>§nHelm of Revealing§r<BR>Usually a thaumaturge needs to choose between protection and utility when out and about. Not anymore!<BR>This modifies a Thaumium Fortress Helm to give you all the functionality of Goggles of Revealing. It does not, however, give the vis discount the goggles usually do.<BR>A helm can have this upgrade along with the other masks.<PAGE>§nGrinning Devil Faceplate§r<BR>When worn, this mask has an interesting damping effect on those suffering from Warp. While it does not reduce the number of events, it does reduce their severity somewhat.<BR>It seems laughing in the face of danger does have its rewards.<PAGE>§nAngry Ghost Faceplate§r<BR>When you are struck in combat while wearing this grim mask the creature striking you has a chance to suffer a Withering effect.<BR>Serves them right.<PAGE>§nSipping Fiend Faceplate§r<BR>When you strike a creature in combat while wearing this mask, you sometimes steal a small portion of their life force to strengthen your own.<BR>The fact that you do this by supping on their blood doesn't seem to bother you nearly as much as it should. You wonder if you should be worried about that.");
        add.accept("research.thaumaturge.cloud_ring.title", "Cloudstepper Ring");
        add.accept(
                "research.thaumaturge.cloud_ring.stage_0",
                "I should get my feet wet by creating a simple bauble. After some thought I decided to create a ring that will reduce falling damage and possibly help my mobility.<BR>It should be fairly simple to draw up plans for something like that.");
        add.accept(
                "research.thaumaturge.cloud_ring.stage_1",
                "I have made plans for a bauble I am calling the Cloudstepper Ring. It makes the wearer lighter, therefore greatly reducing falling damage. Additionally it can create a momentary puff of air below the wearer when they jump, allowing them to perform a second jump in mid-air.");
        add.accept("research.thaumaturge.curiosity_band.title", "Headband of Curiosity");
        add.accept(
                "research.thaumaturge.curiosity_band.stage_0",
                "The experience orbs I gather from harvested resources and slain monsters contain both magical energy and packets of knowledge. I am certain that more can be done with them than simple enchanting.");
        add.accept(
                "research.thaumaturge.curiosity_band.stage_1",
                "My theory has proven correct - I have found a way to extract fragments of knowledge from experience orbs. These parcels of knowledge are very small but may provide useful snippets of insight into certain kinds of research.<BR>I have drawn up designs for a headband that when worn will absorb half the experience from any orbs I collect and turn it into research knowledge. The rate of return is very low, but every little bit helps.");
        add.accept("research.thaumaturge.verdant_charms.title", "Verdant Charms");
        add.accept(
                "research.thaumaturge.verdant_charms.stage_0",
                "I occasionally find myself being affected with poisons and other nasty afflictions. I should find a way to protect myself. ");
        add.accept(
                "research.thaumaturge.verdant_charms.stage_1",
                "I have created designs for a mystical charm that pulses with healing energy that will wash all poisons or similar afflictions from my body. Unfortunately it cannot heal actual wounds.<BR>The ring has an internal store of vis that needs to be charged with a recharge pedestal or similar item - it cannot draw vis from the aura directly.<BR>There are also two specialized enchantments I can apply to the charm to improve its function.<LINE>§nLifegiver§r<BR>This infusion enchantment allows the Verdant Heart Charm to regenerate the health of whomever wears it.<LINE>§nSustainer§r<BR>This infusion enchantment allows the Verdant Heart Charm to slowly feed and sustain whomever wears it. This includes providing breathable air.");
        add.accept("research.thaumaturge.charm_undying.title", "Charm of Undying");
        add.accept(
                "research.thaumaturge.charm_undying.stage_0",
                "The Totem of Undying is potent magic that even thaumaturgy cannot easily replicate.<BR>I should however be able to make the totem more convenient to use by allowing it to be worn as a charm instead of having to be held.");
        add.accept(
                "research.thaumaturge.charm_undying.stage_1",
                "The Totem of Undying is potent magic that even thaumaturgy cannot easily replicate.<BR>I should however be able to make the totem more convenient to use by allowing it to be worn as a charm instead of having to be held.");
        add.accept("research.thaumaturge.base_golemancy.title", "Basic Golemancy");
        add.accept(
                "research.thaumaturge.base_golemancy.stage_0",
                "I now understand the basic concepts that shape Golemancy, but I am sure I can improve on the primitive constructs that anyone can craft. At the very least I can use arcane science to design a simple mind that will be both dependable and predictable instead of relying on the chaotic nature of magic to direct my constructs.");
        add.accept("research.thaumaturge.jar_brain.title", "Brain in a Jar");
        add.accept(
                "research.thaumaturge.jar_brain.stage_0",
                "Isn't it just the worst when you have a bunch of spare brain matter lying around, but nothing useful to do with it? Time to change that.");
        add.accept(
                "research.thaumaturge.jar_brain.stage_1",
                "I have discovered a way to reanimate a zombie brain and put it to my own dark uses.<BR>It retains some of the hunger it possessed as a zombie and while it cannot consume brains, it still enjoys eating the thoughts and experiences of those slain nearby. Giving the jar a good shake will make it surrender some of its ill-gotten gains for my own use.<BR>It will only hold a certain amount of experience. Particle effects will be displayed above the jar when it is full.<BR>This jar can also be used to a much greater effect than bookcases to improve mundane enchanting.");
        add.accept("research.thaumaturge.mat_stud_wood.title", "Material Studies: Wood");
        add.accept(
                "research.thaumaturge.mat_stud_wood.stage_0",
                "Golemancy places great demands on my craftsmanship as it often requires intricate components with very narrow margins for error. I will need to increase my skill and knowledge of both mundane and arcane materials. My first target is wood.<BR>Wood is a versatile substance and despite its weakness, it is light and easy to work. I have found Greatwood to be a superior wood particularly well suited to golemancy.<BR>I should learn as much about it as I can.");
        add.accept("research.thaumaturge.mat_stud_iron.title", "Material Studies: Iron");
        add.accept(
                "research.thaumaturge.mat_stud_iron.stage_0",
                "Iron has many properties to recommend it: it is much sturdier than wood, and malleable in a way that wood can never be. It is harder to work with, however, and quite heavy.");
        add.accept("research.thaumaturge.mat_stud_clay.title", "Material Studies: Clay");
        add.accept(
                "research.thaumaturge.mat_stud_clay.stage_0",
                "Clay is not particularly strong, light or durable, but it is easy to work, highly resistant to fire and lighter than iron.<BR>Basically, the perfect choice for a thaumaturge on a budget.");
        add.accept("research.thaumaturge.mat_stud_brass.title", "Material Studies: Brass");
        add.accept(
                "research.thaumaturge.mat_stud_brass.stage_0",
                "While brass is softer than iron, it is still quite sturdy and well-suited to fine mechanical constructs.<BR>In my opinion it is the best mundane material for Golemancy purposes, and I would not dream of crafting intricate devices from any other material.<BR>It is quite pleasing to the eye as well.");
        add.accept("research.thaumaturge.mat_stud_thaumium.title", "Material Studies: Thaumium");
        add.accept(
                "research.thaumaturge.mat_stud_thaumium.stage_0",
                "Thaumium is an arcane metal that sucks up enchantments like a proverbial sponge. It is significantly stronger than iron too. Unfortunately it is slightly heavier than iron and its strength also makes it hard to work.<BR>While it cannot compete with brass when it comes to intricate clockwork, thaumiums strength makes it a useful material to craft mechanisms that require strength over precision.");
        add.accept("research.thaumaturge.mat_stud_void.title", "Material Studies: Void Metal");
        add.accept(
                "research.thaumaturge.mat_stud_void.stage_0",
                "Void metal is relatively light, but quite soft which would normally make it an unsuitable material for mechanisms. Luckily its morphic properties and ability to self-repair opens up interesting possibilities for a thaumaturge willing to think outside the box.");
        add.accept("research.thaumaturge.mind_clockwork.title", "Clockwork Minds");
        add.accept(
                "research.thaumaturge.mind_clockwork.stage_0",
                "Iron golems, snow golems and similar creations that anyone can throw together all have the same thing in common - they are unpredictable and uncontrollable.<BR>The spirits that animate them are wild and follow their own will. This is a problem.<BR>If I am going to create intelligent magical servants then I will have to give them a mind that is willing to take orders. The simplest solution is a clockwork construct quickened by Thaumaturgy.<BR>Designing one will be no simple feat, however.");
        add.accept(
                "research.thaumaturge.mind_clockwork.stage_1",
                "I have managed to crack artificial intelligence in the form of a magically powered clockwork mind. It has its limitations and very little autonomy, but it is a start.<BR>Of course, now that I have a mind it is time to put it to use. I should draw up some designs for golems along with a way to craft them.");
        add.accept(
                "research.thaumaturge.mind_clockwork.stage_2",
                "I have designed a special press that will allow me to craft simple golems. It includes an anvil and forge to make assembling the parts easier.<LINE>To assemble a golem I simply need to access the Golem Press and select the components I wish to craft the golem from. For now the selection is fairly limited, but I am sure I will develop more.<PAGE>Actually designing a golem is fairly straightforward: Each golem is made up from up to 5 components: The base material, a type of head, arms, legs and an optional addon module. Simply select which of these you want to use and a list of crafting components will be displayed on the right. You need to have the listed components in your inventory before you can start the crafting process.<BR>Listed in the middle of the interface you will see the traits, hearts, armor and damage the golem will possess.<BR>Lastly you will need to supply Machina essentia to the press. Several ports are provided below the main press mechanism for this purpose.<PAGE>You can now place the golem wherever you wish, but it will not do much unless you also place down a §5Golem Control Seal§0. To learn more about them see the reseach category that you have unlocked.<BR>When they have no tasks to perform, golems will return to the spot they were originally placed. You can pick up a golem by right clicking on it while sneaking.<BR>It is also possible to assign a color to a golem by clicking on it with dye. This is useful for limiting it to specific seals. Picking up the golem will clear the color assigned to it.");
        add.accept("research.thaumaturge.control_seals.title", "Control Seals");
        add.accept(
                "research.thaumaturge.control_seals.stage_0",
                "Golems cannot do anything they are not told to do. For this purpose I have created Control Seals. You can place these circular clay seals against almost any surface you wish. Once you do they will become become ethereal and intangible, but you will still be able to see them as long as you hold a golem, another seal or §5Golemancer's Bell§0.<BR>The bell will be an important tool as you will use it to fine tune the settings and parameters of the command seals.<BR>Since seals are ethereal you can place down another block in the same spot as you placed a seal.<BR>Several seals can also occupy the same block space as long as the are located on different faces of a block. This should allow you to keep things quite compact.<BR>You can retrieve a seal by holding shift and right-clicking on it with the bell.<PAGE>Clicking on a seal with the bell will allow you to tweak its parameters. The exact parameters will vary from seal to seal, but most seals will allow you to give them a priority between -5 and +5 (default 0). Tasks created by higher priority seals will be performed first. You will also be able to see a list of required traits that a golem will need to have in order to perform tasks created by that seal.<BR>For example, a seal might require a golem to have the §5Deft§0 trait, or perhaps another seal will require that a golem NOT have the §5Clumsy§0 trait.<BR>For a practical example, look at the §5Collect Seal§0.<LINE>Seals can be set to become inactive when they recieve a redstone signal. All queued or in progress tasks from that seal will also be removed.<BR>It is also possible to assign a color to a seal. If you do so tasks from that seal will only be performed by uncolored golems or golems with the same color. Uncolored seals will assign tasks to all golems (colored and uncolored alike).<BR>You set seal colors in the same location where you set priorities.<BR>You assign a golem a color by using a dye on it. The color will be cleared if the golem is picked up.");
        add.accept("research.thaumaturge.golem_direct.title", "Golem Mastery");
        add.accept(
                "research.thaumaturge.golem_direct.stage_0",
                "Having golems mindlessly following seals is not my only goal. I want them to mindlessly follow me as well.<BR>There has to be a way to make that happen.");
        add.accept(
                "research.thaumaturge.golem_direct.stage_1",
                "I can now right-click on a golem with the §5Golemancer's Bell§0 to have it follow me. While following me, it will no longer obey seals and combat-capable golems will defend me in combat.<BR>Clicking on the golem again will return it to normal functioning, and its home location will be set to its current location.");
        add.accept("research.thaumaturge.golem_logistics.title", "Golem Logistics");
        add.accept(
                "research.thaumaturge.golem_logistics.stage_0",
                "Having created the Provider Seal, I now have an interesting opportunity. It should be possible for me to directly request items from nearby golems.<BR>Some tweaking of their pathfinding will be required, but it should not be too hard to achieve.");
        add.accept(
                "research.thaumaturge.golem_logistics.stage_1",
                "I can now sneak + right click with the §5Golemancer's Bell§0 to bring up an inventory of all items accessible by provider seals within 32 blocks. I can then request items from this list and a golem will deliver it.<BR>If I clicked on a block the item will be delivered to the block, or placed inside if it is an inventory. The side you clicked on will be taken into account.<BR>If I clicked on empty air the item will be delivered directly to me.<PAGE>Golems are still limited by their range so I must take care where I try and deliver items to.");
        add.accept("research.thaumaturge.mind_biothaumic.title", "Biothaumic Minds");
        add.accept(
                "research.thaumaturge.mind_biothaumic.stage_0",
                "As predicted, the Clockwork Minds I have created offers very little in the way of abstract reasoning and autonomy. Attempting to create more complex clockworks has met with very little success, so I am turning to something I already know will work: living brains.<BR>By studying them, I might be able to unlock the secret behind true intelligence and autonomy!");
        add.accept(
                "research.thaumaturge.mind_biothaumic.stage_1",
                "Though hesitant at first, I soon overcame any trepidation I felt and delved into Biothaumaturgy - merging flesh, machines and magic into a single whole.<BR>The results are encouraging and I am now able to infuse clockwork minds with salvaged gray matter to form a mind with much greater capabilities.<BR>This has also allowed me to create more complex versions of seals I already know.<BR>Another advantage if this mind is that it will allow a golem to learn. As it completes tasks it will gain experience and rank up. Higher ranks grants a golem additional health, speed and damage.<BR>Golems start at rank 0 and can improve up to rank 10.<BR>Golems will retain their rank if picked up.");
        add.accept("research.thaumaturge.seal_collect.title", "Control Seal: Collect ");
        add.accept(
                "research.thaumaturge.seal_collect.stage_0",
                "This seal will constantly check an area I have defined for dropped items. If items are found, it will instruct any nearby golems to pick up those items.<BR>I can define the exact dimensions of the area as well as filter out certain items using a whitelist or blacklist.");
        add.accept(
                "research.thaumaturge.seal_collect.addendum_0",
                "I can now craft an advanced version of this seal that allows filtering more items at once. It also gives greater control over how the items are filtered, though golems need to be much smarter to perform tasks created by this seal.<BR>I can choose to have the seal ignore NBT or damage values when filtering items, or use the ore dictionary, or to have the seal filter items from the same mod as those included in the filter.");
        add.accept("research.thaumaturge.seal_store.title", "Control Seal: Store");
        add.accept(
                "research.thaumaturge.seal_store.stage_0",
                "This seal can be placed on the side of a container. It will designate the container as a possible destination for any items that golems nearby happen to be holding. As long as the container can accept it, the golems will deliver the item.<BR>You can filter out certain items using a white- or blacklist.<BR>For the white list you can also specify a specific number of items that must be placed in the inventory. By default this number is infinite (marked with a gold star).<BR>If placed against a non-container block the golem will simply drop items at that location.<BR>Like with inventories you can also specify a specific number of items with the white list. Any items located in the world within a 1 block radius of the seal will be counted.");
        add.accept(
                "research.thaumaturge.seal_store.addendum_0",
                "I can now craft an advanced version of the seal that will allow you to filter more items at once and give you greater control over how the items are filtered, though golems need to be much smarter to perform tasks created by this seal.<BR>You can choose to have the seal ignore NBT or damage values when filtering items, use the ore dictionary, or to have it filter items from the same mod as those in the filter.<BR>If you specified item counts in the white list, then all items that match the filter with these settings will be counted.");
        add.accept("research.thaumaturge.seal_empty.title", "Control Seal: Empty");
        add.accept(
                "research.thaumaturge.seal_empty.stage_0",
                "I can instruct golems to pick up and store items, but I might also need to retrieve items from inventories. <BR>Designing a seal to do this should be simple enough.");
        add.accept(
                "research.thaumaturge.seal_empty.stage_1",
                "This seal is effectively the opposite of the Store seal. It will empty items from the inventory it is attached to. Any nearby golems will simply take the items and hold them. I will need to use other seals to give them instructions as to what they need to do with the items they have taken.<BR>A specific item can be filtered so it is removed, or excluded from removal.");
        add.accept(
                "research.thaumaturge.seal_empty.addendum_0",
                "I can now craft an advanced version of the seal that will allow you to filter more items at once and give you greater control over how the items are filtered, though golems need to be much smarter to perform tasks created by this seal.<BR>You can choose to have the seal ignore NBT or damage values when filtering items, use the ore dictionary, or to have it filter items from the same mod as those in the filter.<BR>You can also have the seal only check for a single item in the filter at a time in the order they are listed.");
        add.accept("research.thaumaturge.seal_provide.title", "Control Seal: Provide");
        add.accept(
                "research.thaumaturge.seal_provide.stage_0",
                "I should design a refinement of the Empty seal that will only remove items that are specifically requested by other seals.");
        add.accept(
                "research.thaumaturge.seal_provide.stage_1",
                "This seal is similar to the Empty seal, except it will only remove an item from the inventory if there is a specific need for it.<BR>Certain seals may request provisioning. If they require specific items for their tasks a secondary task will be created telling a nearby golem to retrieve those items from any inventories marked with a Provide seal.<BR>This task will use the requesting seals priority and the golem needs to meet the requirements for both the Provider seal and the requesting seal.<BR>The filter on the Provide seal is merely used to compare what items may be taken from the container. The filters (if any) on the requesting seal will determine the exact item taken.");
        add.accept("research.thaumaturge.seal_stock.title", "Control Seal: Stock");
        add.accept(
                "research.thaumaturge.seal_stock.stage_0",
                "I need a seal that will allow me to keep a minimum number of items stocked in a container at all times.");
    }

    private static void part3(BiConsumer<String, String> add) {
        add.accept(
                "research.thaumaturge.seal_stock.stage_1",
                "This seal is similar to the store seal, except it keeps a certain number of items stocked inside its container at all times. The seal only insures that a minimum number of items are kept in stock - it will not prevent overstocking should it occur by other means.<BR>This seal requires provider seals to know where to get the items.");
        add.accept("research.thaumaturge.seal_guard.title", "Control Seal: Guard");
        add.accept(
                "research.thaumaturge.seal_guard.stage_0",
                "Though small, thaumaturgical golems should be perfectly capable of combat.<BR>Combat is quite a complex task, however, and I will need to carefully test the seal to avoid accidents.");
        add.accept(
                "research.thaumaturge.seal_guard.stage_1",
                "When placed, this seal will protect an area from intrusion. Any hostile creatures it detects will be targeted by nearby combat-capable golems and attacked.<BR>If the golem has the §5Deft§0 property, any creatures slain will drop experience orbs as if killed by a player.<BR>§9This research also unlocks additional golem parts§0.");
        add.accept(
                "research.thaumaturge.seal_guard.addendum_0",
                "I can now create an advanced version of the seal that will give greater control over which creatures my golems will target.");
        add.accept("research.thaumaturge.seal_butcher.title", "Control Seal: Butcher");
        add.accept(
                "research.thaumaturge.seal_butcher.stage_0",
                "While §5Guard§0 seals can be used to kill animals, they are not very selective.<BR>I should refine the arcane algorithms in a guard seal to make it more suited for animal farming.");
        add.accept(
                "research.thaumaturge.seal_butcher.stage_1",
                "The butcher seal works much like a §5Guard§0 seal, except it only targets adult animals. Furthermore it will always make sure that at least one breeding pair of animals remain alive.");
        add.accept("research.thaumaturge.seal_use.title", "Control Seal: Use");
        add.accept(
                "research.thaumaturge.seal_use.stage_0",
                "I should be able to design a seal that allows golems to interact with the world in more complex ways than simply carrying items around or attacking things. There are many objects that require manual operation and maybe I can have golems do that for me.");
        add.accept(
                "research.thaumaturge.seal_use.stage_1",
                "This seal allows a golem to act like a player and left- or right-click on a marked block.<BR>The golem can also click with any items it happens to be holding, or specific filtered items.<BR>The seal will remain even if the block it was placed on is removed, allowing the seal to target empty air if desired.<BR>If a whitelist item is specified, the golem can also request to be §5Provisioned§0.");
        add.accept("research.thaumaturge.seal_harvest.title", "Control Seal: Harvest");
        add.accept(
                "research.thaumaturge.seal_harvest.stage_0",
                "Farming is a menial task best performed by a golem and not a busy thaumaturge like myself.<BR>Designing a seal that will instruct golems to harvest crops will save me a lot of manual labor in the long run.");
        add.accept(
                "research.thaumaturge.seal_harvest.stage_1",
                "This seal will search the designated area for any fully-grown crops or other harvestable plants. Once found, a golem will be dispatched to harvest the plant.<BR>If required, the seal will also attempt to replant the harvested crop as long as seeds (or similar items) are available. If the seed was previously planted on farmland and the soil has turned into dirt, the golem will also try to till the block.");
        add.accept("research.thaumaturge.seal_break.title", "Control Seal: Block Breaker");
        add.accept(
                "research.thaumaturge.seal_break.stage_0",
                "Specially equipped and instructed golems should be capable of digging and breaking blocks. I just need to design the proper seal for that task.");
        add.accept(
                "research.thaumaturge.seal_break.stage_1",
                "This seal commands golems to break any blocks they find in the area.<BR>The hardness of a block determines how quickly it can be broken.<BR>It is possible to filter the type of blocks to be broken or ignored.<BR>It should be noted that due to factors like golem mobility, limited area, and speed, this seal probably will not make a very effective mining tool, though it could be used as such in a pinch.");
        add.accept(
                "research.thaumaturge.seal_break.addendum_0",
                "I can now craft an advanced version of the seal that will allow you to filter more items at once and allow the golem to break blocks as if they are using Silk Touch.<BR>Golems will take longer to break block if Silk Touch is used. ");
        add.accept("research.thaumaturge.seal_lumber.title", "Control Seal: Lumberjack");
        add.accept(
                "research.thaumaturge.seal_lumber.stage_0",
                "While the §5Block Breaker§0 seal can be used to chop down trees it is not particularly effective at the task. I should do something about that.");
        add.accept(
                "research.thaumaturge.seal_lumber.stage_1",
                "A refinement of the §5Block Breaker§0 seal, the Lumberjack seal will only target wooden logs, but instead of breaking the log the golem is hitting, it will instead break the furthest connected log.<BR>This means that it is highly effective when used to harvest trees, as it will effectively chop them from the top down.");
        add.accept("research.thaumaturge.golem_vision.title", "Golem Biothaumic Eyes");
        add.accept(
                "research.thaumaturge.golem_vision.stage_0",
                "I should find a way to improve the visual acuity of my golems.");
        add.accept(
                "research.thaumaturge.golem_vision.stage_1",
                "By merging flesh and arcane constructs, I have crafted a set of highly acute eyes.<BR>Any golem equipped with such a set of peepers will be able to operate at 150% the normal range (48 blocks).<BR>§9This research unlocks additional golem parts§0.");
        add.accept("research.thaumaturge.golem_accessories.title", "Golem Accessories");
        add.accept(
                "research.thaumaturge.golem_accessories.stage_0",
                "My golems serve me faithfully, but they lack a certain... style. Surely a bit of tailoring in miniature would do them no harm - and I suspect the right accoutrements might even improve their function.");
        add.accept(
                "research.thaumaturge.golem_accessories.stage_1",
                "I have designed a small wardrobe of golem-sized accessories. Simply using one on a golem will fit it in place, and it will be returned when the golem is picked up or destroyed.<BR>The tiny top hat is the perfect accessory for any golem. Not only is it stylish and dashing, but any golem wearing it will have increased health and vitality. The fez fills its wearer with unusual vigor, allowing it to repair damage at a much faster pace - though no golem will suffer wearing both at once.<BR>Golems are notoriously short sighted, and a pair of tiny spectacles alleviates that weakness to some degree. The metal visor instead grants a small amount of armor and ensures that foes slain by the golem yield experience, but it leaves no room for spectacles.<BR>Lastly, a bowtie is a must for any golem on the go, filling it with additional energy and increasing its pace.");
        add.accept("research.thaumaturge.golem_climber.title", "Golem Mobility: Climbing");
        add.accept(
                "research.thaumaturge.golem_climber.stage_0",
                "I should find a way to improve the mobility acuity of my golems. Maybe I could give them the ability to climb walls?");
        add.accept(
                "research.thaumaturge.golem_climber.stage_1",
                "By equipping a golem with crampons, hooks and various other tools, it will be able to scale sheer walls and other obstacles.<BR>§9This research unlocks additional golem parts§0.");
        add.accept("research.thaumaturge.golem_flyer.title", "Golem Mobility: Flight");
        add.accept(
                "research.thaumaturge.golem_flyer.stage_0",
                "I should find a way to further improve the mobility acuity of my golems. Maybe I could give them the ability to fly?");
        add.accept(
                "research.thaumaturge.golem_flyer.stage_1",
                "By basically chopping off a golem's legs and gluing an Arcane Levitator in their place, I have managed to get a golem to fly! This grants it good, if slightly erratic mobility, though it does move more slowly.<BR>§9This research unlocks additional golem parts§0.");
        add.accept("research.thaumaturge.golem_breaker.title", "Golem Manipulation: Block Breaking");
        add.accept(
                "research.thaumaturge.golem_breaker.stage_0",
                "I should increase the number of tools and abilities my golems have available to them. Maybe I could give them the ability to break blocks?");
        add.accept(
                "research.thaumaturge.golem_breaker.stage_1",
                "By replacing a golem's arms with a set of powerful, diamond-tipped pneumatic grinders it will be able to break through all but the hardest materials.<BR>§9This research unlocks additional golem parts§0.");
        add.accept("research.thaumaturge.golem_combat_adv.title", "Improved Golem Combat");
        add.accept(
                "research.thaumaturge.golem_combat_adv.stage_0",
                "I should design improved weapons and armor for my golems.");
        add.accept(
                "research.thaumaturge.golem_combat_adv.stage_1",
                "You can now craft specialized golem weaponry and armor to make them much more effective in combat.<BR>§9This research unlocks additional golem parts§0.");
        add.accept("research.thaumaturge.base_eldritch.title", "Void Seeds");
        add.accept(
                "research.thaumaturge.base_eldritch.stage_0",
                "The void seed I have discovered begs further study. I should study it further and perhaps discover a practical use for it.<BR>Currently it seems morphicly unstable - it's physical properties are constantly shifting and changing shape. Maybe binding it with something else will make it easier to manipulate. Metal perhaps?");
        add.accept(
                "research.thaumaturge.base_eldritch.stage_1",
                "My hand shakes as I write this. The strange substance I have investigated has opened strange new vistas in my mind.<BR>Before me I have formulas for creating a mysterious new metal, yet I do not remember writing them, nor does the handwriting look quite like mine.<BR>The metal they describe is a mass of contradictions. It is hard and unyielding, yet at the same time soft and easy to work. It will feel warm to the touch, almost alive and like living things it will slowly heal itself if damaged.<BR>Crafting mere physical tools from this seems almost... wrong.");
        add.accept("research.thaumaturge.void_siphon.title", "Void Siphon");
        add.accept(
                "research.thaumaturge.void_siphon.stage_0",
                "Destroying a rift to get Void Seeds is highly impractical. I must find a better way.<BR>While most Crimson Cultist rantings are nonsense, they do make reference to drawing power from rifts. I believe it should be possible to siphon off some of the eldritch energy in rifts to form Void Seeds without destroying the rift outright.");
        add.accept(
                "research.thaumaturge.void_siphon.stage_1",
                "I hate to admit it, but the Crimson Cultists might be on to something. I have managed to design a Void Siphon that slowly leeches energy from nearby rifts. This leeched energy will slowly form Void Seeds. The larger the rift is, the quicker this process occurs.<BR>Unfortunately this is not without consequence for the rift itself. The rift will become increasingly unstable and will slowly shrink as it's energy is leeched away. I must take care to not overuse them.<BR>A redstone signal will disable the Void Siphon.");
        add.accept("research.thaumaturge.voidseer_pearl.title", "Voidseer's Pearl");
        add.accept(
                "research.thaumaturge.voidseer_pearl.stage_0",
                "In madness there is insight. Or so I have been told... I'm not sure by who, but it has the ring of truth about it.<BR>I have been having strange thoughts and ideas of late, but their significance and meaning is just outside my grasp. If I can focus my mind somehow I might be able to see... more.");
        add.accept(
                "research.thaumaturge.voidseer_pearl.stage_1",
                "When I hold this charm I get glimpses of what lies beyond. The meaning still eludes me, but I feel more connected to the aura and magic than I have ever felt before.<BR>While I have this charm equipped I will find vis much easier to manipulate.<BR>Oddly enough, the size of the bonus seems to be proportional to the amount of permanent warp I have suffered. What a strange coincidence.");
        add.accept("research.thaumaturge.void_robe_armor.title", "Void Thaumaturge Armor");
        add.accept(
                "research.thaumaturge.void_robe_armor.stage_0",
                "The simple void metal armor I can now craft is practical, but not particularly useful to full blooded thaumaturge like myself. I should craft something better. Much better.");
        add.accept(
                "research.thaumaturge.void_robe_armor.stage_1",
                "Using the knowledge I have gained thus far, I have designed a set of armored robes that combines the utility of Thaumaturge's Robes with the practacality of Thaumium Fortress Armor.<BR>Primarily made from void metal this armor has all the benefits (and side-effects) that metal offers.<BR>Lastly, the hood of the robes contain integrated Goggles of Revealing.<BR>This armor can be dyed.");
        add.accept("research.thaumaturge.primal_crusher.title", "Primal Crusher");
        add.accept(
                "research.thaumaturge.primal_crusher.stage_0",
                "The variable and malleable nature of Void Metal has openend up some interesting possibilities.<BR>I believe it should be possible to create a tool with inherent morphic instability. In other words it will constantly fluctuate between various states so rapdily that it might not be visible to the naked eye.");
        add.accept(
                "research.thaumaturge.primal_crusher.stage_1",
                "Success! I have created a tool I am calling the Primal Crusher. It has the essential nature of both a pickaxe and a shovel. It does not look like either of those tools, but it defintely functions like them. Better in fact. It is also surprisingly effective against Taint.<BR>It is slightly unpleasant to hold however - it seems to vibrate in one's hand and the feeling becomes quite uncomfortable after a while. A little discomfort will not stop me from using it however.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/wisp.title", "Wisp");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/wisp.stage_0",
                "These strange creatures appear to be floating points of light and are often found near high concentrations of vis. Honestly I am not sure if they are creatures at all - they move randomly and do not appear to exhibit the normal characteristics of a living organism. They do however react to being attacked and occasionally become aggressive for no apparent reason.<BR>My initial thoughts are they might be a form of proto-life made up of vis instead of normal matter, but that cannot be the whole picture as I am detecting strange dimensional energies at their core.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/pech.title", "The Pech");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/pech.stage_0",
                "These strange humanoids can be found wherever the veil between worlds is at its thinnest. They seem drawn to the magical energies that abound in such places.<IMG>thaumaturge:textures/research/research5.png:128:136:108:117:.75</IMG>Don't let their small stature fool you. They have incredible strength and can carry many times their own bodyweight.<BR>Pech are normally not aggressive, but when riled up they will band together and hunt down their attacker. Under normal conditions they avoid people at all costs.<BR>Pechs are notoriously avaricious, and will loot anything not nailed down, though they prefer precious things. It is said that if you feed this desire for material wealth it could be possible to befriend one.<BR>Once befriended they often carry wondrous objects that they might be willing to part with.<BR>It should be noted that such 'friendships' seldom last long.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/thaumic_slime.title", "Thaumic Slime");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/thaumic_slime.stage_0",
                "These purple slimes are a strange offshoot of their green cousins. Normal slimes form from the organic gunk that occasionally collects in caves, but Thaumic Slimes are formed from the purple proto-matter residue left over from magical mishaps.<BR>Apart from their strange origins they are rather similar to their green cousins for the most part. They do appear to have the ability to split at will, often spitting a lesser version of themselves at targets that venture too close. If there are no targets nearby, split slimes will endeavour to reform to once again form a larger organism. ");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/firebat.title", "Firebat");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/firebat.stage_0",
                "Fire bats, or Hellbats, are usually found in the Nether. Engulfed in flame and unreasonably aggressive they are perfectly suited to the environment they live in. They also have the alarming tendency to explode when attacking.<BR>One wonders what possible evolutionary advantage exploding gives them as a species, but the same can be asked of creepers. ");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/taint_seed.title", "Taint Seed");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/taint_seed.stage_0",
                "Occasionally tight knots of Tainted tendrils will form. From out of this 'seed' foul tainted air will spew which acts as the basis for new Taint growth.<BR>These seeds can lash out to defend themselves, but the risk must be taken if you wish to rid an area of taint.<BR>Without the foul air these seeds create, tainted growth soon withers and dies. ");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/taint_crawler.title", "Tainted Crawler");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/taint_crawler.stage_0",
                "These disgusting grubs secrete corrupting Taint wherever they go. Most of the time the Taint is short-lived if they wander too far from the core of the Taint growth, but other times this can cause the Taint to spread much quicker than it normally would.<BR>It is best to destroy them on sight.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/taintacle.title", "Taintacle");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/taintacle.stage_0",
                "These dangerous growths look like giant tentacles. They will lash out at anything that comes nearby.<BR>Though they are immobile, keeping your distance might not keep you safe as miniature versions of them will spawn all around you should you draw their attention.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/taint_swarm.title", "Taint Swarm");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/taint_swarm.stage_0",
                "Sometimes purple hive-like growths will form on the remains of trees within Taint. Swarms of tiny biting creatures will spawn from these growths and rove around in aggressive swarms.<BR>They move fast, are hard to avoid and even harder to hit. Fortunately the individual creatures are not particularly robust so a properly prepared traveller should be able to dispatch them easily.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/cultist.title", "The Crimson Cult");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/cultist.stage_0",
                "These red-robed cultists have been popping up of late. Their goals are unknown to me and they seem unwilling to talk. Beyond that there is something... wrong about them. Their grip on sanity seems tenuous at best, but the wrongness goes much deeper than that. It is hard to describe.<BR>My presence seems to enrage them and I believe it might have something to do with my capacity for thaumaturgy.<BR>Their clerics are also capable of wielding magic and I can sense that they are drawing it from the aura, but the magic itself does not seem to be thaumaturgy. Whatever it is, it makes my skin crawl and I can feel the aura buckling and tearing every time they draw upon it.<BR>Some of them seem to carry a book around. I should try and get my hand on one of those, maybe it will tell me more about this strange cult. I should be careful though, some knowledge can be dangerous.");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/brainy_zombie.title", "Angry Zombie");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/brainy_zombie.stage_0",
                "I have observed that some zombies seem much more aware than their lumbering brethren. Their eyes burn with a malevolent intelligence leading me to believe that they retained much more intelligence after death than is normal. They are definitely more aggressive and dangerous than a normal zombie.<BR>They should make good sources of brain matter for my experiments. ");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/eldritch_guardian.title", "Eldritch Guardian");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/eldritch_guardian.stage_0",
                "These ghost-like figures are a dangerous menace. They can be found wherever the walls of reality are at their thinnest. I believe they act as guardians of such places, protecting those weak spots from trespassers.<BR>Their form is held together by dimensional energies from some other place making them much weaker in our world. I would hate having to confront them in whatever place they call home.<BR>Their touch is deadly and they are able to hurl entropic dimensional energy at those too far to reach. ");
        add.accept("research.thaumaturge.scanned/entity/thaumaturge/eldritch_crab.title", "Eldritch Crab");
        add.accept(
                "research.thaumaturge.scanned/entity/thaumaturge/eldritch_crab.stage_0",
                "Eldritch Crabs, or Helmed Crabs, are a most unusual and unnatural creature. I am convinced these creatures are the result of magical tinkering.<BR>A pair of powerful claws make them dangerous to face in combat, but it is how they use those claws that make them horrific. They leap at their foes heads in an attempt to decapitate them. If successful they are somehow able to burrow into the victims body and take control of what remains. Their abdomen looks much like a helmed head allowing them to disguise the true nature of the husk they now control.<BR>These husks are not much more powerful than a normal zombie so the reason why they do this eludes me - they are much more of a threat without their 'mount'. Possibly they were created as weapons of terror by some demented inventor?   ");
        add.accept(
                "got.crystals",
                "Your fingers tingle strangely as you handle the crystal. What does that mean? Maybe some rest will inspire you.");
        add.accept("got.dream", "You awaken from a strange dream. You quickly write it down before the memory fades. ");
        add.accept("research.thaumaturge.m_deepdown.title", "Visit the lowest depths of the world.");
        add.accept("research.thaumaturge.m_uphigh.title", "Visit the highest peaks you can find.");
        add.accept("research.thaumaturge.m_finddesert.title", "Visit dry and barren wastes.");
        add.accept("research.thaumaturge.m_findocean.title", "Visit the endless, blue seas.");
        add.accept("got.deepdown", "You have visited deep and dark depths and have learned much.");
        add.accept("got.uphigh", "The world is spread out before you. Your perspective has been broadened.");
        add.accept("got.finddesert", "You have visited the scorching desert.");
        add.accept("got.findocean", "You have peered into the endless blue abyss.");
        add.accept("research.thaumaturge.f_golem.title", "Study an animated construct.");
        add.accept("research.thaumaturge.f_toomuchflux.title", "Let the the Flux build");
        add.accept("research.thaumaturge.f_voidseed.title", "Examine something native to the Void ");
        add.accept("got.instability", "I should study infusion instability further.");
        add.accept("research.thaumaturge.m_walker.title", "Walk a lot.");
        add.accept("research.thaumaturge.m_runner.title", "Run a lot.");
        add.accept("research.thaumaturge.m_swimmer.title", "Swim a lot.");
        add.accept("research.thaumaturge.m_jumper.title", "Jump a lot.");
        add.accept("research.thaumaturge.f_teleport.title", "Study teleportation");
        add.accept("research.thaumaturge.f_onfire.title", "Examine fire very closely.");
        add.accept("research.thaumaturge.f_arrow.title", "A normal projectile");
        add.accept("research.thaumaturge.f_fireball.title", "A firey projectile");
        add.accept("research.thaumaturge.f_spit.title", "A sticky projectile");
        add.accept("got.projectile", "You learn some more about projectiles.");
        add.accept("research.thaumaturge.f_dispenser.title", "Examine something that can launch objects.");
        add.accept("research.thaumaturge.f_brain.title", "Examine something brainy.");
        add.accept("research.thaumaturge.f_matiron.title", "Examine iron");
        add.accept("research.thaumaturge.f_matclay.title", "Examine clay");
        add.accept("research.thaumaturge.f_matbrass.title", "Examine brass ");
        add.accept("research.thaumaturge.f_matthaumium.title", "Examine thaumium ");
        add.accept("research.thaumaturge.f_matvoid.title", "Examine void metal ");
        add.accept("research.thaumaturge.f_spider.title", "Closely examine a climbing creature");
        add.accept("research.thaumaturge.f_fly.title", "Closely examine a flying creature.");
        add.accept("got.onfire", "You have felt the touch of flame.");
        add.accept("got.hellandback", "You have visited the Nether.");
        add.accept("got.endoftheworld", "You have visited the End.");
        add.accept("research.thaumaturge.m_hellandback.title", "Visit a hellish place.");
        add.accept("research.thaumaturge.m_endoftheworld.title", "Find the end of everything.");
        add.accept("book.thaumaturge.start.title", "Strange Dreams");
        add.accept(
                "book.thaumaturge.start.1",
                "\"I had the strangest dream last night.\n\nIn the dream, I took three of those strange crystals I've been finding and ground them with a handful of redstone in a bowl, using a piece of flint.\"");
        add.accept(
                "book.thaumaturge.start.2",
                "\"The crystals had to be of different types, but if I did it properly then the result was a strange, glowing dust.\n\nIn the dream, I took the dust and sprinkled it on a bookcase, but the dream ended before I saw what happened.\"");
        add.accept(
                "book.thaumaturge.start.3",
                "\"I wonder, should I do what the dream showed?\n\nI have the impression the dust was going to reveal something wondrous, but dangerous.\"");
        add.accept("tc.addaddendum", "§aAddendum added to %1$s§r");
        add.accept("research.thaumaturge.gotdream.title", "Strange Dreams");
        add.accept("research.thaumaturge.gotthaumonomicon.title", "The Thaumonomicon");
    }

    private static void part4(BiConsumer<String, String> add) {
        add.accept("research.thaumaturge.focus_hellbat.title", "Focus Effect: Nine Hells");
        add.accept(
                "research.thaumaturge.focus_hellbat.stage_0",
                "Your trips into the Nether have taught you to fear the tiny terrors known as firebats. Now you have discovered a way to summon the little terrors and send them after your enemies. Excellent.<BR>The Nine Hells effect can be worked into a focus at the focal manipulator. Whatever the focus strikes, summoned firebats will harry - and the more power behind the cast, the harder they bite. The summons are short-lived, crumbling to ash after half a minute.");
        add.accept("research.thaumaturge.focus_primal.title", "Focus Effect: Primal");
        add.accept(
                "research.thaumaturge.focus_primal.stage_0",
                "What madness possessed you to create this thing? This effect seems wildly dangerous and only a lunatic would attempt to use it anywhere but the most controlled environments.<BR>Granted, the burst of raw primal energy it creates is quite devastating, but the energies involved are erratic at best and downright malicious at worst. Once in a great while the detonation tears at the aura itself - sometimes leaving a smear of flux, and sometimes, remarkably, condensing into a small aura node. It exhibits energy patterns you have only seen in wisps and aura nodes, and it begs further study.");
        add.accept("research.thaumaturge.cap_gold.title", "Gold Wand Caps");
        add.accept(
                "research.thaumaturge.cap_gold.stage_0",
                "Gold is a far superior focusing agent than iron, and wands capped with this material take no penalty when used to channel vis.");
        add.accept("research.thaumaturge.cap_copper.title", "Copper Wand Caps");
        add.accept(
                "research.thaumaturge.cap_copper.stage_0",
                "Copper has similar properties to iron when used as a wand cap, but is as efficient as gold when used to channel Ordo or Perditio vis.");
        add.accept("research.thaumaturge.cap_silver.title", "Silver Wand Caps");
        add.accept(
                "research.thaumaturge.cap_silver.stage_0",
                "Silver has similar properties to gold when used as a wand cap, but is slightly more efficient at channeling the four elemental primals (fire, earth, air and water).<BR>Crafting these wand caps in an arcane workbench is not sufficient - they need to be infused with additional magical energy to make them usable.");
        add.accept("research.thaumaturge.cap_thaumium.title", "Thaumium Wand Caps");
        add.accept(
                "research.thaumaturge.cap_thaumium.stage_0",
                "Thaumium is known for its ability to store and absorb magic, so using it to craft wand caps is a logical choice. It is very efficient at focusing vis of all types.<BR>Crafting these wand caps in an arcane workbench is not sufficient - they need to be infused with additional magical energy to make them usable.");
        add.accept("research.thaumaturge.cap_void.title", "Void Metal Wand Caps");
        add.accept(
                "research.thaumaturge.cap_void.stage_0",
                "Crafting wand caps from Void metal seems counterintuitive since it seems to be highly resistant to magic, but counterintuitive is where you live now.<BR>Your gamble paid off. The metal itself may be resistant to magic, but it channels with surprising ease. The magic does seem strange somehow, but it is probably just your imagination.");
        add.accept("research.thaumaturge.rod_greatwood.title", "Greatwood Wand Core");
        add.accept(
                "research.thaumaturge.rod_greatwood.stage_0",
                "Carved from greatwood logs, this rod has double the vis storage potential of mundane wood, being able to store up to 50 vis of each type.");
        add.accept("research.thaumaturge.rod_obsidian.title", "Obsidian Wand Core");
        add.accept(
                "research.thaumaturge.rod_obsidian.stage_0",
                "A wand core chiseled from obsidian has slightly greater storage potential than greatwood cores. What makes it superior to greatwood cores however is that if the level of Terra stored inside it drops below 10%%, it will slowly replenish without the use of a node.");
        add.accept("research.thaumaturge.rod_ice.title", "Icy Wand Core");
        add.accept(
                "research.thaumaturge.rod_ice.stage_0",
                "Wand cores crafted from ice can store slightly more vis than greatwood cores. What makes it superior to greatwood cores however is that if the level of Aqua stored inside it drops below 10%%, its icy nature will slowly replenish it.");
        add.accept("research.thaumaturge.rod_quartz.title", "Quartz Wand Core");
        add.accept(
                "research.thaumaturge.rod_quartz.stage_0",
                "A wand core chiseled from nether quartz has slightly greater storage potential than greatwood cores. What makes it superior to greatwood cores however is that if the level of Ordo stored inside it drops below 10%%, it will slowly replenish without the use of a node.");
        add.accept("research.thaumaturge.rod_blaze.title", "Blaze Rod Wand Core");
        add.accept(
                "research.thaumaturge.rod_blaze.stage_0",
                "Blaze rods used as wand cores have slightly greater storage potential than greatwood cores. What makes it superior to greatwood cores however is that if the level of Ignis stored inside it drops below 10%%, its fiery nature will slowly replenish it.");
        add.accept("research.thaumaturge.rod_bone.title", "Bone Wand Core");
        add.accept(
                "research.thaumaturge.rod_bone.stage_0",
                "A bone used as a wand core has slightly greater storage potential than greatwood cores. What makes it superior to greatwood cores however is that if the level of Perditio stored inside it drops below 10%%, it will slowly replenish without the use of a node.");
        add.accept("research.thaumaturge.rod_reed.title", "Reed Wand Core");
        add.accept(
                "research.thaumaturge.rod_reed.stage_0",
                "A wand core made from reeds has slightly greater storage potential than greatwood cores. What makes it superior to greatwood cores however is that if the level of Aer stored inside it drops below 10%%, it will slowly replenish without the use of a node.");
        add.accept("research.thaumaturge.rod_silverwood.title", "Silverwood Wand Core");
        add.accept(
                "research.thaumaturge.rod_silverwood.stage_0",
                "Silverwood has proved to be an excellent repository of magical energy, and wand cores crafted from it can store up to 100 vis of each type.");
        add.accept("research.thaumaturge.sceptre.title", "Crafting Scepters");
        add.accept(
                "research.thaumaturge.sceptre.stage_0",
                "Crafting scepters are specialized wands made to be used to craft mystical objects and nothing else. Foci cannot be attached to them, but they have several features that still make them worthwhile using.<BR>Firstly, they have an additional 10%% vis discount above and beyond what their caps would normally grant.<BR>Secondly they are able to hold 50%% more vis than their cores would normally allow.<BR>Like other wands they can be crafted using most combinations of rods and caps - just a few examples are given here.");
        add.accept("research.thaumaturge.staves.title", "Magic Staves");
        add.accept(
                "research.thaumaturge.staves.stage_0",
                "Wands are the stock and trade of the thaumaturge, but sometimes you want more bang for your buck. Magical staves are basically just bigger wands. They are able to store more vis.<BR>Since they are physically larger and sturdier than a wand, they can actually be used as weapons in an emergency.<BR>Unfortunately this also makes them unsuitable for the intricate workmanship and fine control required by an arcane workbench or similar crafting.<BR>A staff made from greatwood has more than double the vis storage potential of a greatwood wand, being able to store up to 125 vis of each type, elemental staff cores store 175, and silverwood cores a full 250.<BR>Note: You still need to add caps to a stave rod, just like you would for a wand.");
        add.accept("research.thaumaturge.staff_primal.title", "Staff Core of the Primal");
        add.accept(
                "research.thaumaturge.staff_primal.stage_0",
                "This core pulses with latent energy.<BR>It combines all the features of the lesser stave cores - it has the vis storage capacity of the silverwood core and is able to replenish its vis stores like the various primal wand cores.<BR>Lastly it adds one level of potency to any foci used with it.<BR>This is truly a potent tool in any thaumaturge's arsenal.<BR>Note: You still need to add caps to this stave rod, just like you would for a wand.");
        add.accept("research.thaumaturge.node_tapper_1.title", "Advanced Node Tapping");
        add.accept(
                "research.thaumaturge.node_tapper_1.stage_0",
                "Usually the process of drawing vis from a node with a wand is quite slow... but not anymore.<BR>You have discovered a more efficient way of tapping into aura nodes, resulting in a doubling of the vis draw rate.");
        add.accept("research.thaumaturge.node_tapper_2.title", "Master Node Tapping");
        add.accept(
                "research.thaumaturge.node_tapper_2.stage_0",
                "You have added further refinements to the methods you use to tap aura nodes.<BR>You are now able to draw vis three times faster than normal.");
        add.accept("research.thaumaturge.node_preserve.title", "Node Preserver");
        add.accept(
                "research.thaumaturge.node_preserve.stage_0",
                "You now have better control over the amount of vis you draw from nodes. You will always leave at least one vis of each aspect in the node, unless you are sneaking.<BR>Be warned, this level of control is not possible when using wands crafted from normal wood and iron.");
        add.accept("research.thaumaturge.node_stabilizer.title", "Node Stabilizer");
        add.accept(
                "research.thaumaturge.node_stabilizer.stage_0",
                "This handy device has several beneficial effects if placed below a node.<BR>Firstly it prevents the node from draining vis from other nodes, or being drained in turn. Secondly it prevents unstable nodes from losing their vis and it has a tiny chance of eventually removing the unstable condition completely. Lastly it has a very, very small chance of turning fading nodes into pale nodes.<BR>This requires power however and the Node Stabilizer draws this from the node itself. It does not draw enough to reduce the vis of the node, but it does halve the rate at which the node refills itself from the local aura.<BR>Applying a redstone signal will switch the stabilizer off.");
        add.accept("research.thaumaturge.node_stabilizer_advanced.title", "Advanced Node Stabilizer");
        add.accept(
                "research.thaumaturge.node_stabilizer_advanced.stage_0",
                "Advanced Node Stabilizers perform all the functions of the lesser versions with a few major differences.<BR>Firstly they still prevent lesser nodes from having their vis drained, but they do allow stabilized nodes to drain the vis from lesser ones.<BR>Secondly they have a higher chance to improve unstable and fading nodes.<BR>Unfortunately this costs a lot more power from the stabilized node - still not enough to drain vis from it, but even the brightest node will have its refill rate almost completely negated.");
        add.accept("research.thaumaturge.node_jar.title", "Node in a Jar");
        add.accept(
                "research.thaumaturge.node_jar.stage_0",
                "Warded jars have proven themselves capable of storing all manner of mundane and mystical things, and you believe you have discovered another use for them - trapping aura nodes.<BR>By enclosing a node in a shell of glass capped with a roof of wooden slabs and striking the glass with a wand, the node will become trapped inside a jar which can then be safely picked up and moved around. The trapping ritual demands a heavy price of vis from your wand and has a very good chance of damaging the node.<BR>While trapped the node is dormant: it runs none of its stranger behaviors and only sips at the local aura, refilling at half its normal rate. Vis can still be drawn from it with a wand.<BR>It is possible to free the node from the jar by clicking on it with a wand. The jar will be destroyed in the process.");
        add.accept("research.thaumaturge.nodes.title", "Auras and Nodes");
        add.accept(
                "research.thaumaturge.nodes.stage_0",
                "The Aura is a field of magical energy prevalent throughout the world. Its origin is unclear. Some believe it is the accumulated life force of living beings, while others believe it to merely be another type of energy seeping in from some other place. The last theory seems most likely, as the aura is often found surrounding a knot of concentrated mystical energy known as an Aura Node.<BR>Each aura node has a base amount of magical energy it can contain and this energy is aligned to one or more aspects. It is usually aligned to Primal Aspects, but rarely a node may contain Compound Aspects.<BR>The aspects within an aura node can be tapped by a thaumaturge for his own purposes. The simplest way to do so is with the use of a wand - right-click and hold on the node to draw from it. Energy harvested in this manner is known as vis.<BR>Aura nodes are hard to see with the naked eye, but faint glimmers of light often betray their presence. The Thaumometer makes nodes slightly easier to find, as looking through its lens makes them clearly visible.<BR>Important: Caution must be taken when draining a node. When a specific aspect within a node is completely drained, it may wither or harm the integrity of the node, and the node may disappear completely if all its aspects are used up.<BR>Fortunately nodes replenish their aspects by slowly condensing the raw vis of their own chunk's aura, and if properly maintained can provide a clever thaumaturge with all the vis he could want. But a node is a mirror of its home: starve the aura and the node grows pale and desperate, let flux fester and the node itself may turn tainted.<BR>Nodes also react to other nearby nodes - bigger nodes tend to drain the vis out of their smaller neighbors to increase their own girth.");
        add.accept("research.thaumaturge.gotcrystals.title", "Vis Crystals");
    }
}
