package com.leclowndu93150.thaumaturge.data.lang;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

public final class TCEnglishProvider extends LanguageProvider {
    public TCEnglishProvider(PackOutput output) {
        super(output, TCIds.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.thaumaturge", "Thaumaturge");
        addJade();

        aspect("aer", "Aer", "Air", "air");
        aspect("terra", "Terra", "Earth", "earth");
        aspect("ignis", "Ignis", "Fire", "fire");
        aspect("aqua", "Aqua", "Water", "water");
        aspect("ordo", "Ordo", "Order, Regularity, Purity", "order");
        aspect("perditio", "Perditio", "Entropy, Chaos, Destruction", "broken things");

        aspect("vacuos", "Vacuos", "Void", "empty things");
        aspect("lux", "Lux", "Light", "light");
        aspect("motus", "Motus", "Motion, Animation", "things that move");
        aspect("gelum", "Gelum", "Ice, Frost, Cold", "cold things");
        aspect("vitreus", "Vitreus", "Crystal, Glass, Clear", "crystals");
        aspect("metallum", "Metallum", "Metal", "metals");
        aspect("victus", "Victus", "Life", "the sources of life");
        aspect("mortuus", "Mortuus", "Death", "the nature of death");
        aspect("potentia", "Potentia", "Energy, Power", "energy");
        aspect("permutatio", "Permutatio", "Exchange, Barter", "trading and bartering");
        aspect("praecantatio", "Praecantatio", "Structured Magic, Spells, Enchantment", "magical things");
        aspect("auram", "Auram", "Aura, Vis", "the aura");
        aspect("alkimia", "Alkimia", "Alchemy, Chemistry", "alchemy");
        aspect("vitium", "Vitium", "Taint, Change, Mutation", "the corrupting influence of magic");
        aspect("tenebrae", "Tenebrae", "Darkness", "darkness");
        aspect("alienis", "Alienis", "Alien, Strange, The Eldritch", "strange things from other worlds");
        aspect("volatus", "Volatus", "Flight", "flight");
        aspect("herba", "Herba", "Plant", "plants");
        aspect("instrumentum", "Instrumentum", "Tool, Instrument", "tools");
        aspect("fabrico", "Fabrico", "Craft", "crafting");
        aspect("machina", "Machina", "Mechanism, Machine", "mechanical things");
        aspect("vinculum", "Vinculum", "Trap, Imprison", "things that entrap");
        aspect("spiritus", "Spiritus", "Soul", "spirits");
        aspect("cognitio", "Cognitio", "Mind, Memory, Cognition", "the mind");
        aspect("sensus", "Sensus", "Senses", "perception");
        aspect("aversio", "Aversio", "Aversion, Conflict", "conflict");
        aspect("praemunio", "Praemunio", "Fortify, Protect, Ward", "protective things");
        aspect("desiderium", "Desiderium", "Wish, Desire, Yearning, Want", "valuable things");
        aspect("exanimis", "Exanimis", "Undead", "the nature of undeath");
        aspect("bestia", "Bestia", "Beast", "beast");
        aspect("humanus", "Humanus", "Man", "man");

        researchCategory("basics", "Thaumaturgy");
        researchCategory("auromancy", "Auromancy");
        researchCategory("alchemy", "Alchemy");
        researchCategory("artifice", "Artifice");
        researchCategory("infusion", "Infusion");
        researchCategory("golemancy", "Golemancy");
        researchCategory("eldritch", "Eldritch");

        card(
                "study",
                "Study: %s",
                "You have spent some time studying %s and gained a small but real insight into the subject.");
        card(
                "analyze",
                "Analyze: %s",
                "By spending an observation you have made about %s you have gained additional insight into the subject as well as a small bonus to %s.");
        card(
                "balance",
                "Balance",
                "By rethinking your conclusions you redistribute your findings evenly amongst all known categories. Your progress in any one category will never exceed the average. A small bonus is added to Thaumaturgy.");
        card(
                "ponder",
                "Ponder",
                "After some careful pondering you make small but steady progress in all available categories. A small bonus is added to Thaumaturgy and you may draw one bonus card next round.");
        card("inspired", "Inspired", "Inspiration strikes! You gain %1$s additional progress in %2$s.");
        card(
                "notation",
                "Notation",
                "By spending the smaller of two related insights you can make significant progress in another. Moves the points from %1$s into %2$s.");
        card(
                "rethink",
                "Rethink",
                "You take a moment to rethink your approach. You remove some progress from your current categories but gain a bonus draw and a small Thaumaturgy bonus.");
        card(
                "reject",
                "Reject: %s",
                "You decide that %s is not worth pursuing this session. The category is blocked and a small Thaumaturgy bonus is granted.");
        card(
                "experimentation",
                "Experimentation",
                "Sometimes the best way forward is to simply try things. You gain progress in a random category and a small Thaumaturgy bonus.");

        add("resourcePack.thaumaturge.programmer_art.name", "Thaumaturge Programmer Art");
        add("knowledge_type.thaumaturge.theory", "Theory");
        add("knowledge_type.thaumaturge.observation", "Observation");
        add("tc.knowledge.tooltip", "%1$s: %2$s");
        add("tc.research_category.percent", "%1$s (%2$s%%)");
        add("tc.need.obtain", "Items that will be consumed");
        add("tc.need.craft", "Items that need to be crafted");
        add("tc.need.research", "Things that need to be discovered");
        add("tc.need.know", "Knowledge that must be used");
        add("tc.research.stage", "Current stage:");
        add(
                "tc.knowledge.none",
                "You have not accumulated any knowledge yet. Scan the world with a thaumometer or perform research at a table.");
        add("tc.research.stage.short", "%1$s / %2$s");
        add("tc.research.begin", "You have not yet begun this research");
        add("tc.researchmissing", "Missing required research:");
        add("tc.research.newresearch", "§6Newly discovered research§0");
        add("tc.research.newpage", "§aNew page added§0");
        add("tc.search", "Search");
        add("tc.search.more", "...too many results found. Refine your search");
        add("tile.researchtable.noink.0", "You have run out of ink!");
        add("tile.researchtable.noink.1", "Refill your scribing tools.");
        add("tile.researchtable.nopaper.0", "You have run out of paper!");
        add("tc.card.unknown", "Unknown item");
        add("tc.warp.warn", " Warping level: %n");
        add("tc.forbidden", "§l§5Forbidden knowledge (%n)§0");
        add("tc.forbidden.level.1", "Mostly Harmless");
        add("tc.forbidden.level.2", "Minor");
        add("tc.forbidden.level.3", "Moderate");
        add("tc.forbidden.level.4", "Dangerous");
        add("tc.forbidden.level.5", "Taboo");
        add("tc.inst", "Instability: ");
        add("tc.inst.0", "§1Negligible§0");
        add("tc.inst.1", "§9Minor§0");
        add("tc.inst.2", "§5Moderate§0");
        add("tc.inst.3", "§eHigh§0");
        add("tc.inst.4", "§6Very High§0");
        add("tc.inst.5", "§4Dangerous§0");
        add("tc.type.theory", "Theory");
        add("tc.type.observation", "Observation");
        add("recipe.return", "Back");
        add("recipe.clickthrough", "Click for recipe");
        add("recipe.unknown", "You cannot craft this yet");
        add("recipe.type.workbench", "Workbench");
        add("recipe.type.workbenchshapeless", "Workbench (Shapeless)");
        add("recipe.type.smelting", "Smelting");
        add("recipe.type.arcane", "Arcane Workbench");
        add("recipe.type.arcane.shapeless", "Arcane Workbench (Shapeless)");
        add("recipe.type.crucible", "Crucible");
        add("recipe.type.infusion", "Arcane Infusion");
        add("recipe.type.infusion_enchantment", "Infusion Enchantment");
        add("tooltip.thaumaturge.charge", "Vis: %s / %s");
        add("tooltip.thaumaturge.runic_charge", "Runic shield +%s");
        add("recipe.type.runic_augment", "Runic Augmentation");
        add("tooltip.thaumaturge.infusion_stabiliser", "Infusion Stabilizer");
        add("recipe.type.construct", "Mystical Construct");
        add("wandtable.text1", "Vis Cost");
        add("gui.thaumaturge.research_table.title", "Research Table");
        add("gui.thaumaturge.deconstruction_table.title", "Deconstruction Table");
        add("block.thaumaturge.deconstruction_table", "Deconstruction Table");
        add("item.thaumaturge.research_note", "Research Notes");
        add("item.thaumaturge.research_note.complete", "Discovery!");
        add("tc.researchtheory", "Theory: %s");
        add("tc.researchnote.click", "Click to obtain research notes (requires paper and scribing tools)");
        add("tc.researchnote.table", "Complete these notes at a research table");
        add("tc.researchnote.use", "Right-click to learn this theory");
        add("tc.researchnote.learned", "You have completed your research on %s!");
        add("tc.researchnote.missing", "You need scribing tools and paper to get this research note!");
        add("tc.addaspectdiscovery", "You have discovered the aspect %s!");
        add("tc.discoveryerror", "To understand this you need to study %1$s.");
        add("tc.aspectcost", "Required research points:");
        add("tc.research.copy", "Duplicate these research notes");
        add("tc.decon.collect", "Click to collect this research point");
        add("tc.table.combine", "Combine the two aspects into their compound");
        add("tc.table.helper", "Aspect combination reference");
        add("tc.device.unknown", "You do not yet understand how this device works.");
        add("tc.table.select", "Drag an aspect here to combine it");
        add("tc.table.page.prev", "Previous page");
        add("tc.table.page.next", "Next page");
        add("tc.table.slot.tools", "Place scribing tools here");
        add("tc.table.slot.note", "Place research notes here");
        add("research.thaumaturge.research_expertise.title", "Research Expertise");
        add(
                "research.thaumaturge.research_expertise.stage.1",
                "There must be a more efficient way to work through my research notes. If I complete another theory I am sure I can find ways to recover some of the research points I expend.");
        add(
                "research.thaumaturge.research_expertise.stage.2",
                "You have become more efficient at performing research.<BR>Whenever you remove an aspect that you placed in a hex, there is a 25%% chance that you will regain the research point.");
        add("research.thaumaturge.research_mastery.title", "Research Mastery");
        add(
                "research.thaumaturge.research_mastery.stage.1",
                "My expertise has grown, but true mastery of the research process still eludes me. More theory work should get me there, though I fear what prolonged exposure to these mysteries is doing to my mind.");
        add(
                "research.thaumaturge.research_mastery.stage.2",
                "You have become even more efficient at performing research.<BR>Whenever you remove an aspect that you placed in a hex, there is a 50%% chance that you will regain the research point.<BR>Additionally there is a 10%% chance that whenever you place an aspect that it will not cost any research points to do so.<BR>Lastly you are able to combine aspects in the research table by shift-clicking on the aspect you wish to create. If you have enough of the component aspects they will automatically combine to create the clicked aspect.");
        add("research.thaumaturge.research_duplication.title", "Research Duplication");
        add(
                "research.thaumaturge.research_duplication.stage.1",
                "A completed discovery holds its pattern permanently. Surely I could copy one onto fresh paper for a colleague, given enough research points and one more theory to work out the method.");
        add(
                "research.thaumaturge.research_duplication.stage.2",
                "You have discovered a way to copy completed research notes.<BR>When you complete research or place a completed research note in the research table you will see a star icon. Clicking this will create a copy of this research as long as you are carrying paper and ink and have enough aspects available.<BR>The more copies are created of that research, the more expensive copying it will become.");
        add("research.thaumaturge.deconstructor.title", "Deconstruction Table");
        add(
                "research.thaumaturge.deconstructor.stage.1",
                "Breaking things apart to see what makes them tick has always come naturally to me. A purpose-built table should let me reduce objects to their base essences and salvage research points from the wreckage.");
        add(
                "research.thaumaturge.deconstructor.stage.2",
                "There comes a point in any thaumaturge's career where he is unable to progress with research due to his lack of knowledge.<BR>One possible recourse is the Deconstruction Table. The table allows you to break down objects into their simplest parts which you can examine. There are limits however - the table breaks compound aspects into their component aspects until only primal aspects remain. During this process much knowledge is lost and at best the thaumaturge can hope for is a single piece of primal knowledge.<BR>For example iron (Metallum) <PAGE>will be simplified into §2Terra§0 and §7Ordo§0, only one of which will have a chance of being discovered.<BR>It is also fairly slow and the fewer aspects an object has, the lower the chance to discover something.");
        add("gui.thaumaturge.research_table.inspiration", "Inspiration: %s");
        add("gui.thaumaturge.research_table.draw", "Draw");
        add("gui.thaumaturge.research_table.play", "Play");
        add("gui.thaumaturge.research_table.complete", "Complete Research");
        add("gui.thaumaturge.arcane_workbench.vis_available", "%s available");
        add("gui.thaumaturge.arcane_workbench.required_vis", "%s vis");
        add("gui.thaumaturge.arcane_workbench.required_vis_discount", "%s vis (%s%% discount)");
        add("gui.thaumaturge.arcane_workbench.required_vis_crude", "%s vis (unfocused)");
        add(
                "gui.thaumaturge.arcane_workbench.wand_pay.tooltip",
                "Primal vis the wand will contribute in place of crystals (%s per crystal)");
        add("button.thaumaturge.create_theory", "Create Theory");
        add("button.thaumaturge.complete_theory", "Complete Theory");
        add("button.thaumaturge.scrap_theory", "Scrap Theory");
        add("block.thaumaturge.research_table", "Research Table");
        add("block.thaumaturge.arcane_workbench", "Arcane Workbench");
        add("block.thaumaturge.arcane_workbench_charger", "Workbench Charger");
        add("block.thaumaturge.crucible", "Crucible");
        add("block.thaumaturge.infernal_furnace", "Infernal Furnace");
        add("block.thaumaturge.alembic", "Arcane Alembic");
        add("block.thaumaturge.bellows", "Arcane Bellows");
        add("block.thaumaturge.smelter_basic", "Essentia Smeltery");
        add("block.thaumaturge.smelter_thaumium", "Thaumium Essentia Smeltery");
        add("block.thaumaturge.smelter_void", "Void Metal Essentia Smeltery");
        add("block.thaumaturge.smelter_aux", "Auxiliary Slurry Pump");
        add("block.thaumaturge.smelter_vent", "Auxiliary Venting Port");
        add("block.thaumaturge.jar_normal", "Warded Jar");
        add("block.thaumaturge.jar_void", "Void Jar");
        add("item.thaumaturge.thaumonomicon", "Thaumonomicon");
        add("item.thaumaturge.thaumonomicon_cheat", "Cheater's Thaumonomicon");
        add("item.thaumaturge.thaumonomicon_sharing", "Thaumonomicon of Sharing");
        add("item.thaumaturge.thaumonomicon_linking", "Thaumonomicon of Binding");
        add("item.thaumaturge.creative_node_placer", "Creative Node Placer");
        add("block.thaumaturge.node_transducer", "Node Transducer");
        add("block.thaumaturge.vis_relay", "Vis Relay");
        add("tooltip.thaumaturge.creative_only", "Creative only");
        add("tooltip.thaumaturge.sharing.bound", "Attuned to %s");
        add("tooltip.thaumaturge.sharing.hint", "Use once to attune, then have your research partner use it");
        add("tc.thaumonomicon.cheat.granted", "The book whispers %s secrets into your mind");
        add("tc.thaumonomicon.sharing.bound", "The book attunes to your mind - hand it to your research partner");
        add("tc.thaumonomicon.sharing.self", "The book is already attuned to you - it needs another's touch");
        add("tc.thaumonomicon.sharing.linked", "Your knowledge now flows freely between you and %s");
        add("tc.thaumonomicon.sharing.used", "%s has transmitted you all their knowledge");
        add("tc.elemental_sword.whirlwind_on", "The blade's winds stir once more");
        add("tc.elemental_sword.whirlwind_off", "The blade's winds fall still");
        add("tooltip.thaumaturge.elemental_sword.toggle", "Sneak + right-click to toggle the whirlwind");
        add("item.thaumaturge.jar_brace", "Brass Lid Brace");
        add("item.thaumaturge.label", "Label");
        add("item.thaumaturge.salis_mundus", "Salis Mundus");
        add(
                "tooltip.thaumaturge.salis_mundus.desc",
                "Magical dust that triggers transmutations when applied to certain blocks.");
        add("item.thaumaturge.vis_resonator", "Vis Resonator");
        add("item.thaumaturge.fabric", "Enchanted Fabric");
        add("item.thaumaturge.mirrored_glass", "Mirrored Glass");
        add("item.thaumaturge.filter", "Essentia Filter");
        add("item.thaumaturge.mechanism_simple", "Simple Arcane Mechanism");
        add("item.thaumaturge.mechanism_complex", "Complex Arcane Mechanism");
        add("item.thaumaturge.morphic_resonator", "Morphic Resonator");
        add("item.thaumaturge.bath_salts", "Bath Salts");
        add("item.thaumaturge.sanity_soap", "Sanity Soap");
        add("block.thaumaturge.spa", "Arcane Spa");
        add("block.thaumaturge.purifying_fluid", "Purifying Fluid");
        add("fluid_type.thaumaturge.purifying", "Purifying Fluid");
        add("block.thaumaturge.liquid_death", "Liquid Death");
        add("fluid_type.thaumaturge.liquid_death", "Liquid Death");
        add("item.thaumaturge.liquid_death_bucket", "Liquid Death Bucket");
        add("gui.thaumaturge.spa.mix.true", "Mix with ingredient");
        add("gui.thaumaturge.spa.mix.false", "Use just the fluid");
        add("item.thaumaturge.chunk_beef", "Beef Nugget");
        add("item.thaumaturge.chunk_chicken", "Chicken Nugget");
        add("item.thaumaturge.chunk_pork", "Pork Nugget");
        add("item.thaumaturge.chunk_fish", "Fish Nugget");
        add("item.thaumaturge.chunk_rabbit", "Rabbit Nugget");
        add("item.thaumaturge.chunk_mutton", "Mutton Nugget");
        add("item.thaumaturge.triple_meat_treat", "Triple Meat Treat");
        add("attributes.thaumaturge.vis_discount", "Vis Discount");
        add("subtitles.thaumaturge.jar", "Jar clinks");
        add("subtitles.thaumaturge.creak", "Tube creaks");
        add("subtitles.thaumaturge.key", "Filter clicks");
        add("subtitles.thaumaturge.pump", "Essentia pumps");
        add("subtitles.thaumaturge.squeek", "Valve squeaks");
        add("subtitles.thaumaturge.tool", "Tube clicks");
        add("block.thaumaturge.tube", "Essentia Tube");
        add("block.thaumaturge.tube_valve", "Essentia Valve");
        add("block.thaumaturge.tube_restrict", "Restricted Essentia Tube");
        add("block.thaumaturge.tube_filter", "Essentia Filter Tube");
        add("block.thaumaturge.tube_oneway", "One-Way Essentia Tube");
        add("block.thaumaturge.tube_buffer", "Essentia Buffer");

        add("item.thaumaturge.alumentum", "Alumentum");
        add("block.thaumaturge.nitor_white", "White Nitor");
        add("block.thaumaturge.nitor_orange", "Orange Nitor");
        add("block.thaumaturge.nitor_magenta", "Magenta Nitor");
        add("block.thaumaturge.nitor_light_blue", "Light Blue Nitor");
        add("block.thaumaturge.nitor_yellow", "Yellow Nitor");
        add("block.thaumaturge.nitor_lime", "Lime Nitor");
        add("block.thaumaturge.nitor_pink", "Pink Nitor");
        add("block.thaumaturge.nitor_gray", "Gray Nitor");
        add("block.thaumaturge.nitor_light_gray", "Light Gray Nitor");
        add("block.thaumaturge.nitor_cyan", "Cyan Nitor");
        add("block.thaumaturge.nitor_purple", "Purple Nitor");
        add("block.thaumaturge.nitor_blue", "Blue Nitor");
        add("block.thaumaturge.nitor_brown", "Brown Nitor");
        add("block.thaumaturge.nitor_green", "Green Nitor");
        add("block.thaumaturge.nitor_red", "Red Nitor");
        add("block.thaumaturge.nitor_black", "Black Nitor");

        ResearchTextEn.addAll(this::add);

        add("key.thaumaturge.thaumonomicon", "Open Thaumonomicon");
        add("gui.thaumaturge.entry.advance", "Mark As Read");
        add("tc.stage.complete", "Complete");
        add("tc.stage.hold", "Completing...");
        add("tc.research.complete", "Research Complete!");
        add("tc.aspect.name", "Aspects of Essentia");
        add("tc.knowledge.name", "Knowledge Totals");
        add("tc.aspect.primal", "Primal Aspect");
        add("tc.aspect.unknown", "Unknown Aspect");
        add("tc.aspect.unknown.short", "???");
        add("tc.aspect.unknown.desc", "Its nature is not yet understood.");
        add("tc.aspect.composition", "%1$s + %2$s");
        add("tc.aspect.progress", "Aspects known %1$s/%2$s");
        add("tc.discoveryerror.derive", "To understand this you must first derive an aspect of %1$s.");
        add("tc.addendumtext", "§oAddendum %1$s§r");

        add("jei.thaumaturge.category.arcane_workbench", "Arcane Workbench");
        add("jei.thaumaturge.arcane_workbench.vis_cost", "Vis Cost, drained from the local aura");
        add(
                "jei.thaumaturge.arcane_workbench.vis_cost_wand",
                "A slotted wand focuses the craft: its cap discount applies and it can pay %s vis per required crystal instead of consuming them");
        add("jei.thaumaturge.arcane_workbench.vis_cost_aura", "Paying with crystals instead runs unfocused: %s vis");
        add("jei.thaumaturge.category.crucible", "Crucible");
        add("jei.thaumaturge.category.dust_trigger", "Salis Mundus Trigger");
        add("jei.thaumaturge.category.multiblock_dust_trigger", "Multiblock Trigger");
        add("jei.thaumaturge.category.aspect_composition", "Aspect Composition");
        add("jei.thaumaturge.category.aspect_from_stacks", "Aspect from ItemStack");
        add(
                "jei.thaumaturge.dust_trigger.usage",
                "Right-click the target block with Salis Mundus to trigger this transmutation.");
        add("jei.thaumaturge.dust_trigger.target.tag", "Any block in tag %1$s");
        add(
                "jei.thaumaturge.dust_trigger.target.multiblock",
                "Right-click any block of the multiblock to trigger this transmutation.");
        add("jei.thaumaturge.research.missing_research", "Missing research: ");
        add("tooltip.thaumaturge.aspects.header", "Aspects:");

        // Resources
        add("block.thaumaturge.ore_amber", "Amber Bearing Stone");
        add("block.thaumaturge.ore_cinnabar", "Cinnabar Ore");
        add("block.thaumaturge.ore_quartz", "Quartz Ore");

        add("block.thaumaturge.alchemical_construct", "Alchemical Construct");
        add("block.thaumaturge.advanced_alchemical_construct", "Advanced Alchemical Construct");

        add("block.thaumaturge.metal_thaumium", "Thaumium Block");
        add("block.thaumaturge.metal_brass", "Alchemical Brass Block");
        add("block.thaumaturge.metal_void", "Void Metal Block");
        add("block.thaumaturge.amber_block", "Amber Block");

        add("item.thaumaturge.rare_earth", "Rare Earth");

        add("item.thaumaturge.quicksilver", "Quicksilver");
        add("item.thaumaturge.amber", "Amber");
        add("item.thaumaturge.ingot_thaumium", "Thaumium Ingot");
        add("item.thaumaturge.ingot_brass", "Alchemical Brass Ingot");
        add("item.thaumaturge.ingot_void", "Void Metal Ingot");

        add("item.thaumaturge.nugget_quartz", "Quartz Sliver");
        add("item.thaumaturge.nugget_quicksilver", "Quicksilver Drop");
        add("item.thaumaturge.nugget_thaumium", "Thaumium Nugget");
        add("item.thaumaturge.nugget_brass", "Alchemical Brass Nugget");
        add("item.thaumaturge.nugget_void", "Void Metal Nugget");

        add("item.thaumaturge.plate_iron", "Iron Plate");
        add("item.thaumaturge.plate_brass", "Alchemical Brass Plate");
        add("item.thaumaturge.plate_thaumium", "Thaumium Plate");
        add("item.thaumaturge.plate_void", "Void Metal Plate");

        add("item.thaumaturge.cluster_iron", "Native Iron Cluster");
        add("item.thaumaturge.cluster_gold", "Native Gold Cluster");
        add("item.thaumaturge.cluster_copper", "Native Copper Cluster");
        add("item.thaumaturge.cluster_silver", "Native Silver Cluster");
        add("item.thaumaturge.cluster_lead", "Native Lead Cluster");
        add("item.thaumaturge.cluster_tin", "Native Tin Cluster");
        add("item.thaumaturge.cluster_cinnabar", "Native Cinnabar Cluster");
        add("item.thaumaturge.cluster_quartz", "Native Quartz Cluster");

        langBCrystals();
        langBaubles();
        langCStone();
        langDTrees();
        langEPlants();
        langMAuraHud();
        langHContainers();
        langTaint();
        langGTools();
        langNScanning();
        langDecor();
        langCasters();
        langEnchantments();
        langGolemancy();
        langEldritch();
        langBosses();
        langConstructs();
        langDecorSweep();
        langOuterLands();
    }

    private void aspect(String tag, String name, String description, String help) {
        add("aspect.thaumaturge." + tag, name);
        add("aspect.thaumaturge." + tag + ".desc", description);
        add("aspect.thaumaturge." + tag + ".help", help);
    }

    private void researchCategory(String path, String name) {
        add("research_category.thaumaturge." + path, name);
    }

    private void card(String path, String name, String text) {
        add("card.thaumaturge." + path + ".name", name);
    }

    private void langBCrystals() {

        add("block.thaumaturge.crystal_aer", "Air Crystal");
        add("block.thaumaturge.crystal_ignis", "Fire Crystal");
        add("block.thaumaturge.crystal_aqua", "Water Crystal");
        add("block.thaumaturge.crystal_terra", "Earth Crystal");
        add("block.thaumaturge.crystal_ordo", "Order Crystal");
        add("block.thaumaturge.crystal_perditio", "Entropy Crystal");
        add("block.thaumaturge.crystal_vitium", "Flux Crystal");
    }

    private void langBaubles() {

        add("item.thaumaturge.amulet_mundane", "Mundane Amulet");
        add("item.thaumaturge.ring_mundane", "Mundane Ring");
        add("item.thaumaturge.girdle_mundane", "Mundane Belt");
        add("item.thaumaturge.ring_apprentice", "Apprentice's Ring");
        add("item.thaumaturge.amulet_fancy", "Fancy Amulet");
        add("item.thaumaturge.ring_fancy", "Fancy Ring");
        add("item.thaumaturge.girdle_fancy", "Fancy Belt");
        add("item.thaumaturge.amulet_vis", "Vis Stone");
        add("item.thaumaturge.amulet_vis_crafted", "Amulet of Vis");
        add("item.thaumaturge.amulet_vis.text", "Recharges armor, baubles and hotbar items.");
        add("item.thaumaturge.charm_undying", "Charm of Undying");
        add("item.thaumaturge.cloud_ring", "Cloudstepper Ring");
        add("item.thaumaturge.curiosity_band", "Headband of Curiosity");
        add("item.thaumaturge.verdant_charm", "Verdant Heart Charm");
        add("item.thaumaturge.verdant_charm.life.text", "Lifegiver");
        add("item.thaumaturge.verdant_charm.sustain.text", "Sustainer");
        add("item.thaumaturge.voidseer_charm", "Voidseer's Pearl");
        add(
                "item.thaumaturge.voidseer_charm.text",
                "You peer into the inky blackness... you think you see something staring back...");
        add("item.thaumaturge.focus_pouch", "Focus Pouch");
        add("item.thaumaturge.sanity_checker", "Sanity Checker");
        add("item.thaumaturge.curio_arcane", "Arcane Curiosity");
        add("item.thaumaturge.curio_preserved", "Preserved Curiosity");
        add("item.thaumaturge.curio_ancient", "Ancient Curiosity");
        add("item.thaumaturge.curio_eldritch", "Eldritch Curiosity");
        add("item.thaumaturge.curio_knowledge", "Illuminating Curiosity");
        add("item.thaumaturge.curio_twisted", "Twisted Curiosity");
        add("item.thaumaturge.curio_rites", "Crimson Rites");
        add("tc.knowledge.gained", "You have gained some knowledge");
        add("block.thaumaturge.mirror", "Magic Mirror");
        add("block.thaumaturge.mirror_essentia", "Essentia Mirror");
        add("item.thaumaturge.hand_mirror", "Magic Hand Mirror");
        add("tc.handmirrorlinked", "Link established.");
        add("tc.handmirrorerror", "Destination mirror is missing or misplaced. Link broken.");
        add("tc.handmirrorlinkedto.full", "Linked to %s,%s,%s in %s");
        add("tc.mirrorlinkedalready", "That mirror is already linked to a valid destination.");
        add("fail.crimsonrites", "This book contains nothing but crazed ravings.");
        add("item.thaumaturge.creative_flux_sponge", "Creative Flux Sponge");
        add("tooltip.thaumaturge.flux_sponge.drain.0", "Right-click to drain all");
        add("tooltip.thaumaturge.flux_sponge.drain.1", "flux from 9x9 chunk area");
        add("tooltip.thaumaturge.flux_sponge.rifts.0", "Also removes flux rifts");
        add("tooltip.thaumaturge.flux_sponge.rifts.1", "if used while sneaking.");
        add("tooltip.thaumaturge.flux_sponge.creative", "Creative only");
        add("tc.flux_sponge.drained", "%s flux drained from 81 chunks.");
        add("tc.flux_sponge.rifts", "%s flux rifts removed.");
        add("item.thaumaturge.resonator", "Essentia Resonator");
        add("item.thaumaturge.fortress_helm", "Thaumium Fortress Helm");
        add("item.thaumaturge.fortress_chest", "Thaumium Fortress Cuirass");
        add("item.thaumaturge.fortress_legs", "Thaumium Fortress Thigh Guards");
        add("item.thaumaturge.fortress_helm.mask.0", "Grinning Devil");
        add("item.thaumaturge.fortress_helm.mask.1", "Angry Ghost");
        add("item.thaumaturge.fortress_helm.mask.2", "Sipping Fiend");
        add("item.thaumaturge.void_robe_helm", "Void Thaumaturge Hood");
        add("item.thaumaturge.void_robe_chest", "Void Thaumaturge Robe");
        add("item.thaumaturge.void_robe_legs", "Void Thaumaturge Leggings");
        add("tc.resonator1", "Contains %1$s %2$s essentia");
        add("tc.resonator2", "Suction %1$s %2$s");
        add("tc.resonator3", "Untyped");
        add("tc.condenser1", "Cost: %1$s essentia");
        add("tc.condenser2", "Time: %1$s ticks (%2$s seconds)");
    }

    private void langCStone() {

        add("block.thaumaturge.stone_arcane", "Arcane Stone");
        add("block.thaumaturge.stone_arcane_brick", "Arcane Stone Brick");
        add("block.thaumaturge.stone_ancient", "Ancient Stone");
        add("block.thaumaturge.stone_ancient_tile", "Ancient Stone Tile");
        add("block.thaumaturge.stone_ancient_rock", "Ancient Rock");
        add("block.thaumaturge.stone_ancient_glyphed", "Glyphed Stone");
        add("block.thaumaturge.stone_ancient_doorway", "Ancient Barrier");
        add("block.thaumaturge.stone_eldritch_tile", "Eldritch Stone");
        add("block.thaumaturge.stone_porous", "Porous Stone");
        add("block.thaumaturge.stairs_arcane", "Arcane Stone Stairs");
        add("block.thaumaturge.stairs_arcane_brick", "Arcane Brick Stairs");
        add("block.thaumaturge.stairs_ancient", "Ancient Stone Stairs");
    }

    private void langDTrees() {

        add("block.thaumaturge.sapling_greatwood", "Greatwood Sapling");
        add("block.thaumaturge.sapling_silverwood", "Silverwood Sapling");
        add("block.thaumaturge.log_greatwood", "Greatwood Log");
        add("block.thaumaturge.log_silverwood", "Silverwood Log");
        add("block.thaumaturge.greatwood", "Greatwood");
        add("block.thaumaturge.silverwood", "Silverwood");
        add("block.thaumaturge.stripped_log_greatwood", "Stripped Greatwood Log");
        add("block.thaumaturge.stripped_log_silverwood", "Stripped Silverwood Log");
        add("block.thaumaturge.stripped_greatwood", "Stripped Greatwood");
        add("block.thaumaturge.stripped_silverwood", "Stripped Silverwood");
        add("block.thaumaturge.leaves_greatwood", "Greatwood Leaves");
        add("block.thaumaturge.leaves_silverwood", "Silverwood Leaves");
        add("block.thaumaturge.plank_greatwood", "Greatwood Planks");
        add("block.thaumaturge.plank_silverwood", "Silverwood Planks");
    }

    private void langEPlants() {

        add("block.thaumaturge.shimmerleaf", "Shimmerleaf");
        add("block.thaumaturge.cinderpearl", "Cinderpearl");
        add("block.thaumaturge.vishroom", "Vishroom");
        add("block.thaumaturge.grass_ambient", "Ambient Grass Block");
        add("biome.thaumaturge.magical_forest", "Magical Forest");
        add("biome.thaumaturge.eerie", "Eerie");
        add("biome.thaumaturge.eldritch", "Eldritch");
    }

    private void langMAuraHud() {

        add("item.thaumaturge.goggles_revealing", "Goggles of Revealing");
    }

    private void langHContainers() {

        add("item.thaumaturge.phial.empty", "Glass Phial");
        add("item.thaumaturge.phial.filled", "Phial of %1$s Essentia");
        add("item.thaumaturge.phial.unknown", "Phial of Unknown Essentia");
        add("item.thaumaturge.primordial_pearl.pearl", "Primordial Pearl");
        add("item.thaumaturge.primordial_pearl.nodule", "Primordial Nodule");
        add("item.thaumaturge.primordial_pearl.mote", "Primordial Mote");
    }

    private void langTaint() {

        add("block.thaumaturge.flux_goo", "Flux Goo");
        add("fluid_type.thaumaturge.flux_goo", "Flux Goo");

        add("block.thaumaturge.taint_rock", "Tainted Rock");
        add("block.thaumaturge.taint_soil", "Tainted Soil");
        add("block.thaumaturge.taint_crust", "Tainted Crust");
        add("block.thaumaturge.taint_geyser", "Taint Geyser");
        add("block.thaumaturge.taint_log", "Tainted Log");
        add("block.thaumaturge.taint_feature", "Taint Feature");
        add("block.thaumaturge.taint_fibre", "Taint Fibre");

        add("effect.thaumaturge.vis_exhaust", "Vis Exhaust");
        add("effect.thaumaturge.infectious_vis_exhaust", "Flux Phage");
        add("effect.thaumaturge.flux_taint", "Flux Taint");

        add("death.attack.thaumaturge.taint", "%1$s was tainted");
        add("death.attack.thaumaturge.taint.player", "%1$s was tainted by %2$s");
        add("death.attack.thaumaturge.taint.item", "%1$s was tainted by %2$s using %3$s");
        add("death.attack.thaumaturge.tentacle", "%1$s was strangled by tentacles");
        add("death.attack.thaumaturge.tentacle.player", "%1$s was strangled by %2$s's tentacles");
        add("death.attack.thaumaturge.tentacle.item", "%1$s was strangled by %2$s using %3$s");
        add("death.attack.thaumaturge.swarm", "%1$s was swarmed");
        add("death.attack.thaumaturge.swarm.player", "%1$s was swarmed by %2$s");
        add("death.attack.thaumaturge.swarm.item", "%1$s was swarmed by %2$s using %3$s");
        add("death.attack.thaumaturge.dissolve", "%1$s dissolved");
        add("death.attack.thaumaturge.dissolve.player", "%1$s was dissolved by %2$s");
        add("death.attack.thaumaturge.dissolve.item", "%1$s was dissolved by %2$s using %3$s");
        add("death.attack.thaumaturge.focus_fire.item", "%1$s was burned to death by %2$s using %3$s");

        add("item.thaumaturge.essentia_crystal", "%s Vis Crystal");
        add("item.thaumaturge.mana_bean", "Mana Bean");
        add("block.thaumaturge.mana_pod", "Mana Pod");
        add("item.thaumaturge.essentia_crystal.unknown", "Unknown Vis Crystal");

        add("entity.thaumaturge.thaumic_slime", "Thaumic Slime");
        add("entity.thaumaturge.taint_seed", "Taint Seed");
        add("entity.thaumaturge.taint_seed_prime", "Greater Taint Seed");
        add("entity.thaumaturge.taint_crawler", "Taint Crawler");
        add("entity.thaumaturge.taint_swarm", "Taint Swarm");
        add("entity.thaumaturge.taintacle", "Taintacle");
        add("entity.thaumaturge.taintacle_small", "Lesser Taintacle");
        add("entity.thaumaturge.falling_taint", "Falling Taint");
        add("entity.thaumaturge.bottle_taint", "Bottle of Tainted Goo");
        add("item.thaumaturge.bottle_taint", "Bottle of Taint");
        add("entity.thaumaturge.wisp", "Wisp");
        add("entity.thaumaturge.brainy_zombie", "Angry Zombie");
        add("entity.thaumaturge.giant_brainy_zombie", "Furious Zombie");
        add("entity.thaumaturge.firebat", "Firebat");
        add("entity.thaumaturge.mind_spider", "Mind Spider");
        add("item.thaumaturge.brain", "Zombie Brain");

        add("item.thaumaturge.brainy_zombie_spawn_egg", "Angry Zombie Spawn Egg");
        add("item.thaumaturge.giant_brainy_zombie_spawn_egg", "Furious Zombie Spawn Egg");
        add("item.thaumaturge.firebat_spawn_egg", "Firebat Spawn Egg");
        add("item.thaumaturge.mind_spider_spawn_egg", "Mind Spider Spawn Egg");
        add("item.thaumaturge.wisp_spawn_egg", "Wisp Spawn Egg");
        add("item.thaumaturge.thaumic_slime_spawn_egg", "Thaumic Slime Spawn Egg");
        add("item.thaumaturge.taint_crawler_spawn_egg", "Taint Crawler Spawn Egg");
        add("item.thaumaturge.taintacle_spawn_egg", "Taintacle Spawn Egg");
        add("item.thaumaturge.taint_swarm_spawn_egg", "Taint Swarm Spawn Egg");
        add("item.thaumaturge.taint_seed_spawn_egg", "Taint Seed Spawn Egg");
        add("item.thaumaturge.taint_seed_prime_spawn_egg", "Greater Taint Seed Spawn Egg");
    }

    private void langGTools() {

        add("item.thaumaturge.thaumometer", "Thaumometer");
        add("item.thaumaturge.scribing_tools", "Scribing Tools");
    }

    private void langNScanning() {

        add("tc.unknownobject", "Nothing new can be learned from this.");
        add("tc.knownobject", "You have learned something new.");
        add("tc.invtoolarge", "Inventory too large. Only scanning first 100 items.");
        add("tc.celestial.fail.1", "You have already studied that today.");
        add("tc.celestial.fail.2", "You are unable to take notes of your studies.");
        add("tc.celestial.studied", "You commit your celestial observations to memory.");
        add(
                "jei.thaumaturge.deconstruction.info",
                "Place any item that carries aspects on the Deconstruction Table and it will slowly break the item down. Each work cycle has a chance to shake loose a single research point of a random primal aspect; the richer the item's aspects, the better the odds. Click the floating aspect to collect it.");

        add("item.thaumaturge.celestial_notes", "Celestial Notes");
        add("item.thaumaturge.celestial_notes.sun.text", "Solar");
        add("item.thaumaturge.celestial_notes.stars_1.text", "Stellar, Northern Quadrant");
        add("item.thaumaturge.celestial_notes.stars_2.text", "Stellar, Southern Quadrant");
        add("item.thaumaturge.celestial_notes.stars_3.text", "Stellar, Western Quadrant");
        add("item.thaumaturge.celestial_notes.stars_4.text", "Stellar, Eastern Quadrant");
        add("item.thaumaturge.celestial_notes.moon_1.text", "Lunar, Full");
        add("item.thaumaturge.celestial_notes.moon_2.text", "Lunar, Waning Gibbous");
        add("item.thaumaturge.celestial_notes.moon_3.text", "Lunar, Third Quarter");
        add("item.thaumaturge.celestial_notes.moon_4.text", "Lunar, Waning Crescent");
        add("item.thaumaturge.celestial_notes.moon_5.text", "Lunar, New");
        add("item.thaumaturge.celestial_notes.moon_6.text", "Lunar, Waxing Crescent");
        add("item.thaumaturge.celestial_notes.moon_7.text", "Lunar, First Quarter");
        add("item.thaumaturge.celestial_notes.moon_8.text", "Lunar, Waxing Gibbous");

        add("card.thaumaturge.curio.name", "Study Curio");
        add(
                "card.thaumaturge.curio.text",
                "While examining a curio often reveals some interesting information it is much better to study it as part of controlled research.");
        add("card.thaumaturge.enchantment.name", "Study Enchantment");
        add(
                "card.thaumaturge.enchantment.text",
                "You study normal enchantment to see how it functions at a fundamental level. You are sure that is shares a lot in common with the enchantment methods used in Infusion and Artifice. You lose 5 experience levels, but will gain 15 to 20 progress in both Infusion and Auromancy. ");
        add("card.thaumaturge.beacon.name", "Aural Influence");
        add(
                "card.thaumaturge.beacon.text",
                "You believe that the beacon interacts in some way with the mystical aura. You carefully study this and while you cannot find anything concrete this time you have been inspired. You regain 2 inspiration, 1 bonus draw and an additional category will gain the full bonus upon completion. ");
        add("card.thaumaturge.dragonegg.name", "Draconic Studies");
        add(
                "card.thaumaturge.dragonegg.text",
                "The aura around the egg swirls with strange and chaotic energies. Occasionally you glimpse strange order in the chaos. ");
        add("card.thaumaturge.concentrate.name", "Concentrate");
        add(
                "card.thaumaturge.concentrate.text",
                "Often much can be learned by concentrating a substance into its purest form. Attempt to concentrate some %1$s essentia. Gain 15 Alchemy and 1 bonus draw. There is also a chance you will gain 1 inspiration.");
        add("card.thaumaturge.reactions.name", "Reactions");
        add(
                "card.thaumaturge.reactions.text",
                "Studying the reactions between two different types of vis should prove beneficial. You should study what happens when %1$s essentia reacts with %2$s essentia. Gain 25 Alchemy. There is also a chance you will gain 1 inspiration.");
        add("card.thaumaturge.synthesis.name", "Synthesis");
        add(
                "card.thaumaturge.synthesis.text",
                "When essentia combines to form more complex forms a number of interesting and intricate reactions take place. You will learn much by combining %1$s essentia with %2$s essentia and then studying the resulting combination. Gain 40 Alchemy. There is also a chance you will gain 1 inspiration.");
        add("card.thaumaturge.measure.name", "Measure");
        add(
                "card.thaumaturge.measure.text",
                "You take some time to make detailed measurements of various types of essentia and the potential vis they contain. Gain 15 Infusion progress and 1 bonus draw.");
        add("card.thaumaturge.channel.name", "Channel %1$s Essentia");
        add(
                "card.thaumaturge.channel.text",
                "You set up a simple experiment to examine what happens when you channel %1$s during infusion. Gain 25 Infusion.");
        add("card.thaumaturge.infuse.name", "Experimental Infusion");
        add(
                "card.thaumaturge.infuse.text",
                "By making assumptions on the results of infusing certain objects with essentia, and then testing those results valuable insight may be gained. You will learn much by combining %1$s essentia with %2$s and then studying the result. Gain %3$s Infusion.");
        add("card.thaumaturge.calibrate.name", "Calibrate");
        add(
                "card.thaumaturge.calibrate.text",
                "You take some time to properly calibrate your instruments and tools. Gain 15 Artifice and a bonus draw.");
        add("card.thaumaturge.mindmatter.name", "Mind over Matter");
        add(
                "card.thaumaturge.mindmatter.text",
                "You carefully examine and take apart some basic components in the hopes of finding new ways to assemble them into more complex creations. Gain %1$s Artifice. ");
        add("card.thaumaturge.tinker.name", "Tinker");
        add(
                "card.thaumaturge.tinker.text",
                "You start tinkering with some devices to find new ways of incorporating them into magical creations. Gain %1$s to %2$s Artifice.");
        add("card.thaumaturge.spellbinding.name", "Spellbinding");
        add(
                "card.thaumaturge.spellbinding.text",
                "You bind various test enchantments to small pieces of leftover crystal. They have no practical purpose, but grant you invaluable knowledge. Lose up to 5 experience levels, but gain 5 Auromancy per level lost.");
        add("card.thaumaturge.awareness.name", "Awareness");
        add(
                "card.thaumaturge.awareness.text",
                "You open yourself to the flows of vis around you. You gain a deeper understanding of how it works and the underlying nature of things, but this leaves you metaphysically vulnerable. Gain 20 Auromancy. There is a chance you gain Eldritch knowledge and Warp. ");
        add("card.thaumaturge.focus.name", "Spiritual Focus");
        add(
                "card.thaumaturge.focus.text",
                "You focus your mind on the magical and spiritual energy around you, hoping to grow more attuned to its ebb and flow. Gain 15 Auromancy and a bonus draw.");
        add("card.thaumaturge.sculpting.name", "Sculpting");
        add(
                "card.thaumaturge.sculpting.text",
                "You can hone your knowledge by creating simple and short-lived animated figurines. Gain 20 Golemancy and a bonus draw.");
        add("card.thaumaturge.scripting.name", "Scripting");
        add(
                "card.thaumaturge.scripting.text",
                "A large part of Golemancy is creating intricate arcane texts to control your creations. By creating some test scripts you can further your understanding. Gain 25 Golemancy. This consumes additional paper and ink from the research table. ");
        add("card.thaumaturge.synergy.name", "Synergy");
        add(
                "card.thaumaturge.synergy.text",
                "At its root, Golemancy is a blend of Alchemy, Artifice and Infusion. Only by fully understanding how these three disciplines interact with each other will you be able to master Golemancy. Lose 15 points divided evenly between Alchemy, Artifice and Infusion to gain 30 Golemancy. An additional category will gain the full bonus upon completion.");
        add("card.thaumaturge.darkwhisper.name", "Dark Whispers");
        add(
                "card.thaumaturge.darkwhisper.text",
                "The brain in a jar has been very talkative lately. It promises you ancient secrets for all your experience. Can you trust it?");
        add("card.thaumaturge.glyph.name", "Study Glyphs");
        add(
                "card.thaumaturge.glyph.text",
                "You study the ancient glyphs. What Eldritch secrets do they hold? You find the ancient language difficult to understand, but now and again some nugget of truth reveals itself to you.");
        add("card.thaumaturge.portal.name", "Voices from Beyond");
        add(
                "card.thaumaturge.portal.text",
                "Bathed in the light of this strange portal you find strange thoughts invading your mind. Most are incomprehensible, but some fill your mind with strange inspirations.");
        add("card.thaumaturge.revelation.name", "Revelation");
        add(
                "card.thaumaturge.revelation.text",
                "Studying the Eldritch is a dangerous pursuit, but the knowledge you can uncover is often worth it. You gain 30 Eldritch progress and 5 to 10 progress in a random category. You will also gain some temporary and normal Warp and an additional category will gain the full bonus upon completion. ");
        add("card.thaumaturge.realization.name", "Sudden Realization");
        add(
                "card.thaumaturge.realization.text",
                "While pondering the nature of the Eldritch you come to a sudden and shocking realization on the true nature of the universe. You gain 15 Eldritch progress and 5 to 10 progress in two random categories. You will also gain some temporary Warp. There is a chance you may gain some normal Warp as well.");
        add("card.thaumaturge.truth.name", "Find Truth");
        add(
                "card.thaumaturge.truth.text",
                "You desperately try and find the truth behind the Eldritch and what it could mean for the world. You gain 10 to 25 Eldritch progress and a bonus draw. You will also gain some temporary Warp.");
        add("card.thaumaturge.celestial.name", "Celestial Studies");
        add(
                "card.thaumaturge.celestial.text",
                "You take some of the celestial notes you have made and compare them for possible correlations with your primary research category. You gain 25 to 50 inspiration toward %1$s. You may gain other things as well...");

        add("research.thaumaturge.flux.warn", "Something seems to be building up in the aura. Something bad.");
        add("research.thaumaturge.warp.warn", "My mind reels from the knowledge I have gained");
        add("research.thaumaturge.oculus.title", "The Oculus");
        add(
                "research.thaumaturge.oculus.stage_0",
                "The whispers have grown into a chorus and at last I understand what they want of me. The obelisks scattered across the world are not monuments - they are doors, and every door has a key.<BR>The strange altars where I first encountered the crimson cult hold a keystone marked with four empty sockets. Four eyes must be seated there, crafted or bargained for, and the sinister energies above the keystone must remain intact.<BR>Before I attempt something this reckless I should set my theories in order.");
        add(
                "research.thaumaturge.oculus.stage_1",
                "It was all so simple - I am amazed the Crimson Cultists never discovered this.<BR>Four Eldritch Eyes seated upon the keystone, then a focused discharge of vis channeled through my wand into the altar. The local aura pays the price, and the so-called Eye is opened.<BR>Of course I have no idea what that means. No matter - only fools fear the unknown!");
        add("research.thaumaturge.enter_outer_lands.title", "The Outer Lands");
        add(
                "research.thaumaturge.enter_outer_lands.stage_0",
                "You are not quite sure what you were expecting when you stepped through the Oculus, but this strange structure of crumbling stone and twisted passageways was not it.<BR>Something is not quite right here - this structure was not designed for any practical purpose you can discern... unless that purpose was for it to be a deadly maze.<BR>Strange energies abound and your magic seems to act strangely in this alien environment. Even the other denizens you encounter seem out of place here.");
        add("research.thaumaturge.outer_revelations.title", "Outer Revelations");
        add(
                "research.thaumaturge.outer_revelations.stage_0",
                "Your suspicions have been confirmed. This is not the home of the race you have come to call the Eldritch. This place is something else entirely and you do not believe it exists in what you understand as being \"reality\" - it is as much a mental construct as a physical one, but what mind can contain this?<BR>You have been able to decipher only a small number of the symbols, but you are sure this place is a trap - a place to test visitors and weed out the weak. For what purpose you are not sure.");
        add(
                "gui.thaumaturge.altar.ritual_unknown",
                "The keystone hums with power, but its purpose escapes you... for now.");
    }

    private void langDecor() {

        add("block.thaumaturge.dioptra", "Thaumic Dioptra");
        add("block.thaumaturge.vis_battery", "Vis Battery");
        add("block.thaumaturge.matrix_speed", "Infusion Speed Stone");
        add("block.thaumaturge.matrix_cost", "Infusion Cost Stone");
        add("block.thaumaturge.jar_brain", "Brain in a Jar");
        add("block.thaumaturge.arcane_ear", "Arcane Ear");
        add("block.thaumaturge.arcane_ear_toggle", "Arcane Ear (Toggle)");
        add("block.thaumaturge.lamp_arcane", "Arcane Lamp");
        add("block.thaumaturge.lamp_growth", "Lamp of Growth");
        add("block.thaumaturge.lamp_fertility", "Lamp of Fertility");
        add("block.thaumaturge.centrifuge", "Essentia Centrifuge");
        add("block.thaumaturge.hungry_chest", "Hungry Chest");
        add("block.thaumaturge.everfull_urn", "Everfull Urn");
        add("block.thaumaturge.vis_generator", "Vis Generator");
        add("block.thaumaturge.essentia_input", "Filling Essentia Transfuser");
        add("block.thaumaturge.essentia_output", "Emptying Essentia Transfuser");
        add("gui.thaumaturge.golem_logistics", "Golem Logistics");
        add("block.thaumaturge.condenser", "Flux Condenser");
        add("block.thaumaturge.condenser_lattice", "Flux Condenser Lattice");
        add("block.thaumaturge.condenser_lattice_dirty", "Clogged Flux Condenser Lattice");
        add("block.thaumaturge.stabilizer", "Stabilizer");
        add("block.thaumaturge.redstone_relay", "Redstone Relay");
        add("block.thaumaturge.void_siphon", "Void Siphon");
        add("block.thaumaturge.thaumatorium", "Thaumatorium");
        add("block.thaumaturge.thaumatorium_top", "Thaumatorium");
        add("block.thaumaturge.brain_box", "Brain Box");
        add("item.thaumaturge.tallow", "Magic Tallow");
        add("block.thaumaturge.placeholder_obsidian", "Infernal Furnace");
        add("block.thaumaturge.placeholder_nether_bricks", "Infernal Furnace");
        add("item.thaumaturge.warping", "Warping");
        add("effect.thaumaturge.thaumarhia", "Thaumorrhea");
        add("effect.thaumaturge.unnatural_hunger", "Unnatural Hunger");
        add("effect.thaumaturge.sun_scorned", "Sun Scorned");
        add("effect.thaumaturge.death_gaze", "Deadly Gaze");
        add("effect.thaumaturge.blurred_vision", "Blurred Vision");
        add("effect.thaumaturge.warp_ward", "Warp Ward");
        add("warp.thaumaturge.gain.permanent", "You have gained permanent Warp!");
        add("warp.thaumaturge.gain.normal", "You have gained Warp!");
        add("warp.thaumaturge.gain.temporary", "You have gained temporary Warp!");
        add("warp.thaumaturge.lose.permanent", "You have lost permanent Warp!");
        add("warp.thaumaturge.lose.normal", "You have lost Warp!");
        add("warp.thaumaturge.lose.temporary", "You have lost temporary Warp!");
        add("warp.thaumaturge.text.1", "You feel oddly drained.");
        add("warp.thaumaturge.text.2", "A sudden and unnatural hunger consumes you.");
        add("warp.thaumaturge.text.3", "Strange whispers reveal secrets to you.");
        add("warp.thaumaturge.text.4", "Your vision becomes strange and grim.");
        add("warp.thaumaturge.text.5", "The light suddenly becomes overwhelmingly bright and burns your skin.");
        add("warp.thaumaturge.text.6", "A thick fog appears from nowhere. Something stirs in its depths.");
        add("warp.thaumaturge.text.7", "They're everywhere! Run!");
        add("warp.thaumaturge.text.8", "Surely there must be a way to stop these headaches?");
        add("warp.thaumaturge.text.9", "You suddenly feel reluctant to break things.");
        add("warp.thaumaturge.text.10", "Your perception suddenly expands.");
        add("warp.thaumaturge.text.11", "What was that noise? Something is behind you.");
        add("warp.thaumaturge.text.12", "Something is following you.");
        add("warp.thaumaturge.text.13", "Something is watching you. Maybe it is time to stop.");
        add("warp.thaumaturge.text.14", "You have a moment of clarity.");
        add("warp.thaumaturge.text.15", "Your stomach suddenly gurgles very strangely.");
        add("warp.thaumaturge.text.16", "The faint sound of chanting can be heard nearby.");
        add("warp.thaumaturge.text.hunger.1", "Your hunger cannot be satisfied with normal food.");
        add("warp.thaumaturge.text.hunger.2", "Your hunger begins to fade.");
        add("warp.thaumaturge.fluxevent.2", "You feel something invading your mind and sapping your will.");
        add("warp.thaumaturge.fluxevent.3", "You feel a sudden release of magical tension nearby.");
        add("entity.thaumaturge.flux_rift", "Flux Rift");
        add("item.thaumaturge.void_seed", "Void Seed");
        add("item.thaumaturge.causality_collapser", "Causality Collapser");
        add("block.thaumaturge.infusion_matrix", "Runic Matrix");
        add("block.thaumaturge.pedestal_arcane", "Arcane Pedestal");
        add("block.thaumaturge.recharge_pedestal", "Recharge Pedestal");
        add("block.thaumaturge.pedestal_ancient", "Ancient Pedestal");
        add("block.thaumaturge.pedestal_eldritch", "Eldritch Pedestal");
        add("block.thaumaturge.pillar_arcane", "Infusion Pillar");
        add("block.thaumaturge.pillar_ancient", "Ancient Infusion Pillar");
        add("block.thaumaturge.pillar_eldritch", "Eldritch Infusion Pillar");
        add("gui.thaumaturge.infusion.instability", "Instability: %s");
        add("gui.thaumaturge.infusion.instability.0", "Negligible");
        add("gui.thaumaturge.infusion.instability.1", "Minor");
        add("gui.thaumaturge.infusion.instability.2", "Moderate");
        add("gui.thaumaturge.infusion.instability.3", "High");
        add("gui.thaumaturge.infusion.instability.4", "Very High");
        add("gui.thaumaturge.infusion.instability.5", "Dangerous");
        add("gui.thaumaturge.infusion.stability.very_stable", "Very Stable");
        add("gui.thaumaturge.infusion.stability.stable", "Stable");
        add("gui.thaumaturge.infusion.stability.unstable", "Unstable");
        add("gui.thaumaturge.infusion.stability.very_unstable", "Dangerously Unstable");
        add("gui.thaumaturge.infusion.stability.gain", "gain / cycle");
        add("gui.thaumaturge.infusion.stability.range", "0 to ");
        add("gui.thaumaturge.infusion.stability.loss", "loss / cycle");
        add("entity.thaumaturge.causality_collapser", "Causality Collapser");
        add("item.thaumaturge.thaumium_sword", "Thaumium Sword");
        add("item.thaumaturge.thaumium_pickaxe", "Thaumium Pickaxe");
        add("item.thaumaturge.thaumium_axe", "Thaumium Axe");
        add("item.thaumaturge.thaumium_shovel", "Thaumium Shovel");
        add("item.thaumaturge.thaumium_hoe", "Thaumium Hoe");
        add("item.thaumaturge.thaumium_helm", "Thaumium Helm");
        add("item.thaumaturge.thaumium_chest", "Thaumium Chestplate");
        add("item.thaumaturge.thaumium_legs", "Thaumium Greaves");
        add("item.thaumaturge.thaumium_boots", "Thaumium Boots");
        add("item.thaumaturge.void_sword", "Void Sword");
        add("item.thaumaturge.void_pickaxe", "Void Pickaxe");
        add("item.thaumaturge.void_axe", "Void Axe");
        add("item.thaumaturge.void_shovel", "Void Shovel");
        add("item.thaumaturge.void_hoe", "Void Hoe");
        add("item.thaumaturge.void_helm", "Void Helm");
        add("item.thaumaturge.void_chest", "Void Chestplate");
        add("item.thaumaturge.void_legs", "Void Greaves");
        add("item.thaumaturge.void_boots", "Void Boots");
        add("item.thaumaturge.elemental_sword", "Sword of the Zephyr");
        add("item.thaumaturge.elemental_pickaxe", "Pickaxe of the Core");
        add("item.thaumaturge.elemental_axe", "Axe of the Stream");
        add("item.thaumaturge.elemental_shovel", "Shovel of the Earthmover");
        add("item.thaumaturge.elemental_hoe", "Hoe of Growth");
        add("item.thaumaturge.primal_crusher", "Primal Crusher");
        add("item.thaumaturge.traveller_boots", "Boots of the Traveller");
        add("item.thaumaturge.cloth_chest", "Apprentice's Robes");
        add("item.thaumaturge.cloth_legs", "Apprentice's Leggings");
        add("item.thaumaturge.cloth_boots", "Apprentice's Boots");

        for (DyeColor dye : DyeColor.values()) {
            add("block.thaumaturge.candle_" + dye.getName(), dyeName(dye) + " Tallow Candle");
            add("block.thaumaturge.banner_" + dye.getName(), dyeName(dye) + " Banner");
            add("block.thaumaturge.wall_banner_" + dye.getName(), dyeName(dye) + " Banner");
        }
        add("block.thaumaturge.banner_crimson_cult", "Crimson Cult Banner");
        add("block.thaumaturge.wall_banner_crimson_cult", "Crimson Cult Banner");
    }

    private void langCasters() {

        add("key.category.thaumaturge.main", "Thaumaturge");
        add("key.thaumaturge.change_focus", "Change Wand Focus");
        add("key.thaumaturge.misc_toggle", "Misc Wand Toggle");
        add("item.thaumaturge.wand", "Wand");
        add("item.thaumaturge.wand.named", "%1$s %2$s Wand");
        add("item.thaumaturge.wand.sceptre", "%1$s %2$s Scepter");
        add("item.thaumaturge.wand.staff", "%1$s %2$s Staff");
        add("wand.thaumaturge.cap.iron", "Iron Capped");
        add("wand.thaumaturge.cap.copper", "Copper Capped");
        add("wand.thaumaturge.cap.gold", "Gold Banded");
        add("wand.thaumaturge.cap.silver", "Silver Bossed");
        add("wand.thaumaturge.cap.thaumium", "Thaumium Bossed");
        add("wand.thaumaturge.cap.void", "Void Aspected");
        add("wand.thaumaturge.rod.wood", "Wooden");
        add("wand.thaumaturge.rod.greatwood", "Greatwood");
        add("wand.thaumaturge.rod.silverwood", "Silverwood");
        add("wand.thaumaturge.rod.obsidian", "Obsidian");
        add("wand.thaumaturge.rod.blaze", "Blazing");
        add("wand.thaumaturge.rod.ice", "Icy");
        add("wand.thaumaturge.rod.bone", "Bone");
        add("wand.thaumaturge.rod.reed", "Reed");
        add("wand.thaumaturge.rod.quartz", "Quartz");
        add("wand.thaumaturge.rod.primal", "Primal");
        add("tooltip.thaumaturge.focus_pouch.count", "Holds %1$s/%2$s foci");
        add("tc.wand.notenoughvis", "The wand does not hold enough vis");
        add("tc.node.name", "Aura Node");
        add("tc.node.jar.aspect", "%1$s %2$s");
        add("tc.node.typemod", "%1$s, %2$s");
        add("nodetype.thaumaturge.normal", "Normal");
        add("nodetype.thaumaturge.unstable", "Unstable");
        add("nodetype.thaumaturge.dark", "Sinister");
        add("nodetype.thaumaturge.tainted", "Tainted");
        add("nodetype.thaumaturge.hungry", "Hungry");
        add("nodetype.thaumaturge.pure", "Pure");
        add("nodemod.thaumaturge.bright", "Bright");
        add("nodemod.thaumaturge.pale", "Pale");
        add("nodemod.thaumaturge.fading", "Fading");
        add("tc.wand.noaura", "The aura here is too weak to draw upon");
        add("tc.jar.noresearch", "You sense potential in this arrangement, but lack the knowledge to exploit it");
        add(
                "tc.jar.structure",
                "The ritual fails - the node must be sealed in glass on all sides and capped with a roof of wooden slabs");
        add("tc.jar.vis", "The ritual fails - it demands %s vis of each primal aspect from wands in your hotbar");
        add("tc.dust.noresearch", "The dust sparkles with promise, but you lack the knowledge to direct it");
        add("tc.workbench.staff", "A staff is too unwieldy to use at the workbench");
        add("tooltip.thaumaturge.wand.capacity", "Capacity %s");
        add("tooltip.thaumaturge.wand.cost", "Vis cost %s%%");
        add("tooltip.thaumaturge.wand.cost.except", "Vis cost %1$s%% (%2$s)");
        add("item.thaumaturge.wand_cap_iron", "Iron Cap");
        add("item.thaumaturge.wand_cap_copper", "Copper Cap");
        add("item.thaumaturge.wand_cap_gold", "Gold Cap");
        add("item.thaumaturge.wand_cap_silver_inert", "Inert Silver Cap");
        add("item.thaumaturge.wand_cap_silver", "Charged Silver Cap");
        add("item.thaumaturge.wand_cap_thaumium_inert", "Inert Thaumium Cap");
        add("item.thaumaturge.wand_cap_thaumium", "Charged Thaumium Cap");
        add("item.thaumaturge.wand_cap_void_inert", "Inert Void metal Cap");
        add("item.thaumaturge.wand_cap_void", "Charged Void metal Cap");
        add("item.thaumaturge.wand_rod_greatwood", "Greatwood Rod");
        add("item.thaumaturge.wand_rod_obsidian", "Obsidian Rod");
        add("item.thaumaturge.wand_rod_blaze", "Blazing Rod");
        add("item.thaumaturge.wand_rod_ice", "Icy Rod");
        add("item.thaumaturge.wand_rod_quartz", "Quartz Rod");
        add("item.thaumaturge.wand_rod_bone", "Bone Rod");
        add("item.thaumaturge.wand_rod_reed", "Reed Rod");
        add("item.thaumaturge.wand_rod_silverwood", "Silverwood Rod");
        add("item.thaumaturge.staff_rod_greatwood", "Greatwood Staff Core");
        add("item.thaumaturge.staff_rod_obsidian", "Obsidian Staff Core");
        add("item.thaumaturge.staff_rod_blaze", "Blazing Staff Core");
        add("item.thaumaturge.staff_rod_ice", "Icy Staff Core");
        add("item.thaumaturge.staff_rod_quartz", "Quartz Staff Core");
        add("item.thaumaturge.staff_rod_bone", "Bone Staff Core");
        add("item.thaumaturge.staff_rod_reed", "Reed Staff Core");
        add("item.thaumaturge.staff_rod_silverwood", "Silverwood Staff Core");
        add("item.thaumaturge.staff_rod_primal", "Staff Core of the Primal");
        add("item.thaumaturge.primal_charm", "Primal Charm");
        add("tooltip.thaumaturge.primal_charm.0", "It seems to be leaking");
        add("tooltip.thaumaturge.primal_charm.1", "You think you hear whispering");
        add("tooltip.thaumaturge.primal_charm.2", "It is vibrating violently");
        add("tooltip.thaumaturge.primal_charm.3", "It's humming is quite soothing");
        add("tooltip.thaumaturge.primal_charm.4", "Wait, did it just flash a seventh color?");
        add("item.thaumaturge.focus_1", "Blank Lesser Focus");
        add("item.thaumaturge.focus_2", "Blank Advanced Focus");
        add("item.thaumaturge.focus_3", "Blank Greater Focus");
        add("tooltip.thaumaturge.caster.vis_cost", "Vis cost: %s Vis per cast");
        add("tooltip.thaumaturge.focus.vis_cost", "%s Vis per cast");
        add("entity.thaumaturge.aspect_orb", "Aspect Orb");
        add("entity.thaumaturge.focus_projectile", "Focus Projectile");
        add("entity.thaumaturge.focus_cloud", "Focus Cloud");
        add("entity.thaumaturge.focus_mine", "Focus Mine");
        add("entity.thaumaturge.spell_bat", "Spellbat");

        add("block.thaumaturge.node", "Aura Node");
        add("block.thaumaturge.jar_node", "Node in a Jar");
        add("block.thaumaturge.node_stabilizer", "Node Stabilizer");
        add("block.thaumaturge.node_stabilizer_advanced", "Advanced Node Stabilizer");
        add("block.thaumaturge.hole", "Dimensional Tear");
        add("block.thaumaturge.effect_sap", "Sapping Field");

        add("focus.thaumaturge.root.name", "Caster");
        add("focus.thaumaturge.root.text", "The caster and point of origin of the spell.");
        add("focus.thaumaturge.touch.name", "Touch");
        add(
                "focus.thaumaturge.touch.text",
                "Allows you to affect things you can touch, within roughly 4 blocks of the point of origin.");
        add("focus.thaumaturge.projectile.name", "Projectile");
        add(
                "focus.thaumaturge.projectile.text",
                "Gathers the energy of the focus into a magical orb that you can throw like a projectile. This projectile travels slowly and is affected by gravity.");
        add("focus.thaumaturge.bolt.name", "Bolt");
        add(
                "focus.thaumaturge.bolt.text",
                "Hurl magic directly at your target as a concentrated bolt of energy. The effect is instantaneous, but the range is limited to 16 blocks.");
        add("focus.thaumaturge.plan.name", "Plan");
        add("focus.thaumaturge.plan.text", "Allows you to plan exactly which blocks will be affected.");
        add("focus.thaumaturge.cloud.name", "Cloud");
        add(
                "focus.thaumaturge.cloud.text",
                "Creates a lingering cloud of magical energy that effects anything inside.");
        add("focus.thaumaturge.mine.name", "Arcane Mine");
        add(
                "focus.thaumaturge.mine.text",
                "Creates a mystical construct that detonates when a hostile entity passes nearby, releasing the effects upon it.");
        add("focus.thaumaturge.hellbat.name", "Nine Hells");
        add("focus.thaumaturge.hellbat.text", "Summons vicious hellbats that harry the target with fire and fury.");
        add("focus.hellbat.bats", "Bats");
        add("focus.thaumaturge.primal.name", "Primal");
        add(
                "focus.thaumaturge.primal.text",
                "Unleashes a burst of raw primal energy. Devastating, erratic and occasionally... generative.");
        add("focus.thaumaturge.spellbat.name", "Summon Spellbat");
        add(
                "focus.thaumaturge.spellbat.text",
                "Conjures a mystical bat that will hunt down enemies and inflict them with the focus's effects.");

        add("focus.thaumaturge.air.name", "Air");
        add(
                "focus.thaumaturge.air.text",
                "Creates a blast of air that knocks things back, but causes only minor damage.");
        add("focus.thaumaturge.earth.name", "Earth");
        add(
                "focus.thaumaturge.earth.text",
                "Hurls a blast of earthen shrapnel that causes significant damage and may break weaker blocks.");
        add("focus.thaumaturge.fire.name", "Fire");
        add("focus.thaumaturge.fire.text", "Hurls flame at your target and sets it alight.");
        add("focus.thaumaturge.frost.name", "Frost");
        add(
                "focus.thaumaturge.frost.text",
                "Throws chilling cold at your target, causing damage and freezing it. Freezes water and will slow down creatures.");
        add("focus.thaumaturge.break.name", "Break");
        add("focus.thaumaturge.break.text", "Summons disruptive energy that breaks down most materials.");
        add("focus.thaumaturge.curse.name", "Curse");
        add("focus.thaumaturge.curse.text", "Summons the powers of entropy to harm and disrupt the targeted creature.");
        add("focus.thaumaturge.flux.name", "Flux");
        add(
                "focus.thaumaturge.flux.text",
                "This effect conjures raw, unfocused vis that disrupts living (and dead) creatures. This energy cannot interact with inanimate objects, but it does bypass mundane armor.");
        add("focus.thaumaturge.rift.name", "Rift");
        add(
                "focus.thaumaturge.rift.text",
                "Shifts matter into an alternate reality, creating temporary 'holes' through which you can travel.");
        add("focus.thaumaturge.exchange.name", "Exchange");
        add("focus.thaumaturge.exchange.text", "Swap one type of block in the world for another.");
        add("focus.thaumaturge.heal.name", "Heal");
        add("focus.thaumaturge.heal.text", "This effect heals living creatures and harms undead.");

        add("focus.thaumaturge.scatter.name", "Scatter");
        add("focus.thaumaturge.scatter.text", "Split a single trajectory into multiple random trajectories.");
        add("focus.thaumaturge.split_target.name", "Split Target");
        add("focus.thaumaturge.split_target.text", "Split a single target into two weaker ones.");
        add("focus.thaumaturge.split_trajectory.name", "Split Trajectory");
        add("focus.thaumaturge.split_trajectory.text", "Split a single trajectory into two weaker ones.");

        add("focus.common.power", "Power");
        add("focus.common.duration", "Duration (seconds)");
        add("focus.common.radius", "Radius (blocks)");
        add("focus.common.silk", "Silk Touch");
        add("focus.common.no", "No");
        add("focus.common.yes", "Yes");
        add("focus.common.fortune", "Fortune");
        add("focus.common.target", "Target Type");
        add("focus.common.friend", "Friendly");
        add("focus.common.enemy", "Non-friendly");
        add("focus.common.none", "None");
        add("focus.common.options", "Options");
        add("focus.fire.burn", "Burn duration (seconds)");
        add("focus.scatter.cone", "Spread angle (degrees)");
        add("focus.scatter.forks", "Trajectory forks");
        add("focus.projectile.speed", "Projectile speed");
        add("focus.projectile.bouncy", "Bouncy");
        add("focus.projectile.seeking.friendly", "Seek friendly");
        add("focus.projectile.seeking.hostile", "Seek hostile");
        add("focus.break.power", "Breaking strength");
        add("focus.plan.method", "Planning method");
        add("focus.plan.full", "Full");
        add("focus.plan.surface", "Surface");
        add("focus.rift.depth", "Depth");
        add("focus.heal.power", "Healing");

        add("death.attack.thaumaturge.focus_fire", "%1$s was burned to a crisp by magic");
        add("death.attack.thaumaturge.focus_fire.player", "%1$s was burned to a crisp by %2$s's magic");
        add("block.thaumaturge.focal_manipulator", "Focal Manipulator");
        add("gui.thaumaturge.wandtable.craft", "Start Crafting");
        add("gui.thaumaturge.wandtable.complexity", "Total Complexity");
        add("gui.thaumaturge.wandtable.xp_cost", "Experience Cost");
        add("gui.thaumaturge.wandtable.vis_cost", "Vis Cost");
        add("gui.thaumaturge.wandtable.cast_cost", "Vis per cast: %s");
        add("gui.thaumaturge.wandtable.components", "Crystals Required");
        add("gui.thaumaturge.wandtable.part_complexity", "Complexity:");
        add("gui.thaumaturge.wandtable.part_efficiency", "Effect Multiplier:");
        add("gui.thaumaturge.wandtable.heal_power", "Healing");
        add("gui.thaumaturge.wandtable.problem.in_progress", "Crafting in progress...");
        add("gui.thaumaturge.wandtable.problem.complexity", "Too complex: %s/%s");
        add("gui.thaumaturge.wandtable.problem.empty_nodes", "The spell has unfilled nodes");
        add("gui.thaumaturge.wandtable.problem.crystal", "Missing %sx %s");
        add("gui.thaumaturge.wandtable.problem.no_effects", "The spell needs at least one effect");
        add("gui.thaumaturge.wandtable.problem.xp", "Requires %s experience levels");
        add("gui.thaumaturge.wandtable.problem.ready", "Ready to craft!");
    }

    private void langEnchantments() {

        add("enchantment.thaumaturge.collector", "Collector");
        add("enchantment.thaumaturge.destructive", "Destructive");
        add("enchantment.thaumaturge.burrowing", "Burrowing");
        add("enchantment.thaumaturge.sounding", "Sounding");
        add("enchantment.thaumaturge.refining", "Refining");
        add("enchantment.thaumaturge.arcing", "Arcing");
        add("enchantment.thaumaturge.essence", "Essence Harvester");
        add("enchantment.thaumaturge.lamplight", "Lamplighter");
        add("block.thaumaturge.effect_glimmer", "Glimmer");
    }

    private void langGolemancy() {

        add("item.thaumaturge.mind_clockwork", "Clockwork Mind");
        add("item.thaumaturge.mind_biothaumic", "Biothaumic Mind");
        add("item.thaumaturge.module_vision", "Vision Module");
        add("item.thaumaturge.module_aggression", "Aggression Module");
        add("item.thaumaturge.golem_bell", "Golemancer's Bell");
        add("item.thaumaturge.golem", "Golem");
        add("item.thaumaturge.golem_top_hat", "Golem Accessory: Top Hat");
        add("item.thaumaturge.golem_fez", "Golem Accessory: Fez");
        add("item.thaumaturge.golem_glasses", "Golem Accessory: Spectacles");
        add("item.thaumaturge.golem_bowtie", "Golem Accessory: Bowtie");
        add("item.thaumaturge.golem_visor", "Golem Accessory: Visor");
        add("item.thaumaturge.seal_blank", "Blank Seal");
        add("item.thaumaturge.seal_pickup", "Control Seal: Collect");
        add("item.thaumaturge.seal_pickup_advanced", "Advanced Control Seal: Collect");
        add("item.thaumaturge.seal_fill", "Control Seal: Store");
        add("item.thaumaturge.seal_fill_advanced", "Advanced Control Seal: Store");
        add("item.thaumaturge.seal_empty", "Control Seal: Empty");
        add("item.thaumaturge.seal_empty_advanced", "Advanced Control Seal: Empty");
        add("item.thaumaturge.seal_harvest", "Control Seal: Harvest");
        add("item.thaumaturge.seal_butcher", "Control Seal: Butcher");
        add("item.thaumaturge.seal_guard", "Control Seal: Guard");
        add("item.thaumaturge.seal_guard_advanced", "Advanced Control Seal: Guard");
        add("item.thaumaturge.seal_lumber", "Control Seal: Lumberjack");
        add("item.thaumaturge.seal_breaker", "Control Seal: Block Breaker");
        add("item.thaumaturge.seal_breaker_advanced", "Control Seal: Advanced Block Breaker");
        add("item.thaumaturge.seal_use", "Control Seal: Use");
        add("item.thaumaturge.seal_provider", "Control Seal: Provide");
        add("item.thaumaturge.seal_stock", "Control Seal: Stock");
        add("block.thaumaturge.levitator", "Arcane Levitator");
        add("block.thaumaturge.potion_sprayer", "Potion Sprayer");
        add("block.thaumaturge.pattern_crafter", "Arcane Pattern Crafter");
        add("block.thaumaturge.inlay", "Redstone Inlay");
        add("block.thaumaturge.golem_builder", "Golem Press");
        add("block.thaumaturge.placeholder_iron_bars", "Iron Bars");
        add("block.thaumaturge.placeholder_cauldron", "Cauldron");
        add("block.thaumaturge.placeholder_anvil", "Anvil");
        add("block.thaumaturge.placeholder_table", "Stone Table");
        add("entity.thaumaturge.golem", "Thaumaturge Golem");
        add("entity.thaumaturge.golem_dart", "Golem Dart");
        add("golem.follow", "I'm coming, Master!");
        add("golem.stay", "I will stay here, Master.");
        add("golem.rank", "Rank");
        add("tc.notowned", "This does not belong to you.");
        add("gui.thaumaturge.levitator", "Range set to %s blocks. (%s vis/minute)");
        trait(
                "smart",
                "Smart",
                "This golem is nearly sentient, with superior reasoning and decision-making capabilities.");
        trait(
                "deft",
                "Deft",
                "This golem has a great deal of manual dexterity and can perform tasks requiring precision and a delicate touch. Counters and countered by Clumsy.");
        trait(
                "clumsy",
                "Clumsy",
                "This golem has almost no manual dexterity and can only perform simple tasks requiring basic interactions. Counters and countered by Deft.");
        trait("fighter", "Fighter", "The golem is capable of taking hostile action against other entities.");
        trait(
                "wheeled",
                "Wheeled",
                "The golem propels itself using wheels. This gives it greater speed, but it is unable to jump or navigate steep slopes.");
        trait("flyer", "Flyer", "The golem is capable of flight, giving it greater mobility at the cost of speed.");
        trait(
                "climber",
                "Climber",
                "Sheer cliffs do not deter this golem. It can easily scale them to get to its destination.");
        trait(
                "heavy",
                "Heavy Frame",
                "The golem is heavier than average, which reduces its speed and agility. Counters and countered by Light.");
        trait(
                "light",
                "Light Frame",
                "The golem is lighter than average, which gives it increased speed and agility. Counters and countered by Heavy.");
        trait(
                "fragile",
                "Fragile",
                "The golem is built with delicate components or weak materials that reduce its life total and armour rating. Counters and countered by Armored.");
        trait(
                "repair",
                "Improved Self Repair",
                "The golem repairs any damage suffered at more than double the normal rate.");
        trait(
                "scout",
                "Scout",
                "The golem has a greater visual range and can operate in a much wider area: 48 blocks away from its home location, instead of 32.");
        trait(
                "armored",
                "Armored",
                "The golem is reinforced with additional material which increases its armor rating. Counters and countered by Fragile.");
        trait("brutal", "Brutal", "The golem inflicts greater melee damage in combat.");
        trait("fireproof", "Fireproof", "The golem is immune to fire damage.");
        trait("breaker", "Breaker", "The golem is capable of destroying most blocks with ease.");
        trait("hauler", "Hauler", "Allows the golem to carry two stack of items instead of one.");
        trait("ranged", "Ranged", "The golem can attack targets at range. ");
        trait("blastproof", "Blast-proof", "The golem is highly resistant to explosion damage.");
        material(
                "wood",
                "Greatwood",
                "The golem is crafted from greatwood. It is light and agile, but not particularly sturdy.");
        material("iron", "Iron", "The golem is crafted from iron. It is sturdy and fireproof, but heavy.");
        material(
                "clay",
                "Clay",
                "The golem is crafted from baked clay. It is not a particularly sturdy material, but the resulting golem is fireproof and relatively light.");
        material(
                "brass",
                "Brass",
                "The golem is crafted from brass. It is not as sturdy as iron nor as resistant to fire. Normally brass is heavier than iron, but it allows for superior construction methods which results in a much lighter frame.");
        material(
                "thaumium",
                "Thaumium",
                "The golem is crafted from thaumium. It shares many characteristics with iron, though it is sturdier and more resistant to damage.");
        material(
                "void",
                "Void Metal",
                "The golem is crafted from void metal. Slightly softer than iron, this metal makes up for it by being lighter and able to repair itself.");
        head("basic", "Clockwork Head", "The default golem head. No particular strengths or weaknesses.");
        head(
                "smart",
                "Smart Head",
                "This head contains an advanced biothaumic brain, giving the golem greater capabilities and the ability to learn. ");
        head("smart_armored", "Armored Smart Head", "The smart head enhanced with additional armor and padding.");
        head("scout", "Perceptive Head", "The basic clockwork head enhanced with improved biothaumic eyes. ");
        head(
                "smart_scout",
                "Biothaumic Head",
                "Using both biothaumic eyes and brain, this head is the pinnacle of the biothaumic art. ");
        arm("basic", "Basic Arms", "The default golem arms and hands. No particular strengths or weaknesses.");
        arm("fine", "Fine Manipulators", "These arms end in delicate and dexterous hands. ");
        arm(
                "claws",
                "Claw Arms",
                "These arms end in a terrifying pair of razor sharp metal pincers. Comes with a built-in aggression module.");
        arm("breakers", "Breaker Arms", "These arms end in a pair of pneumatic, diamond tipped grinders. ");
        arm(
                "darts",
                "Dart Launchers",
                "These arms end in a pair of pneumatic dart launchers. The darts are magically created as they are needed. Comes with a built-in aggression module.");
        leg("walker", "Basic Legs", "A pair of simple legs. No particular strengths or weaknesses.");
        leg("roller", "Uni-wheel ", "A single wheel. Quite fast, but incapable of jumping or going up steep hills.");
        leg(
                "climber",
                "Climbing Legs",
                "A pair of simple legs enhanced with crampons and other devices allowing for vertical ascent.");
        leg(
                "flyer",
                "Levitation Module",
                "A modified arcane levitator granting the golem the power of flight. Reduces speed by a third.");
        addon("none", "None", "No addon installed.");
        addon("armored", "Armor Plating", "Grants the golem increased durability.");
        addon("fighter", "Aggression Module", "Allows the golem to engage in combat.");
        addon("hauler", "Carry Frame", "Allows the golem to carry two stack of items instead of one.");
        add("golem.prop.replant", "Replant crops");
        add("golem.prop.cycle", "Cycle whitelist");
        add("golem.prop.meta", "Use metadata");
        add("golem.prop.nbt", "Use NBT data");
        add("golem.prop.ore", "Use Ore Dictionary");
        add("golem.prop.mod", "Use from same mod");
        add("golem.prop.mob", "Target Mobs ");
        add("golem.prop.animal", "Target Animals");
        add("golem.prop.player", "Target Players");
        add("golem.prop.left", "Left click");
        add("golem.prop.empty", "Click empty air");
        add("golem.prop.emptyhand", "Can use empty hand");
        add("golem.prop.sneak", "Simulate sneaky click");
        add("golem.prop.single", "Single item only");
        add("golem.prop.provision", "Request provisioning");
        add("golem.prop.provision.wl", "Request provisioning from whitelist");
        add("golem.prop.priority", "Task Priority");
        add("golem.prop.color", "Set for %s golems");
        add("golem.prop.colorall", "All golems");
        add("golem.prop.owner", "You own this seal");
        add("golem.prop.lock", "Only golems owned by seal owner can perform these tasks");
        add("golem.prop.unlock", "All golems can perform these tasks");
        add("golem.prop.redon", "Redstone sensitive");
        add("golem.prop.redoff", "Ignores Redstone signals");
        add("golem.prop.exist", "Container must already contain item");
        add("golem.prop.leave", "Always leave at least 1 item");
        add("golem.prop.silk", "Use Silk Touch");
        add("gui.thaumaturge.seal", "Seal");
        add("golem.prop.blacklist", "Blacklist");
        add("golem.prop.whitelist", "Whitelist");
        add("button.category.0", "Priority/Locking");
        add("button.category.1", "Filter");
        add("button.category.2", "Area");
        add("button.category.3", "Options");
        add("button.category.4", "Requirements");
        add("button.category.0.desc", "Task priority, golem colour and seal locking");
        add("button.category.1.desc", "Which items the seal applies to");
        add("button.category.2.desc", "Size of the area the seal covers");
        add("button.category.3.desc", "Behaviour options for this seal");
        add("button.category.4.desc", "Traits a golem must have or must lack");
        add("button.caption.x", "East / West");
        add("button.caption.y", "Up / Down");
        add("button.caption.z", "North / South");
        add("button.caption.required", "Required");
        add("button.caption.forbidden", "Forbidden");
    }

    private void langBosses() {
        add("entity.thaumaturge.cultist_leader", "Cultist Praetor");
        add("entity.thaumaturge.cultist_leader.name.custom", "Praetor %1$s the %2$s");
        add("entity.thaumaturge.cultist_portal_greater", "Greater Crimson Portal");
        add("entity.thaumaturge.eldritch_golem", "Eldritch Golem");
        add("entity.thaumaturge.eldritch_golem.name.custom", "%1$s Eldritch Construct");
        add("entity.thaumaturge.eldritch_warden", "Eldritch Warden");
        add("entity.thaumaturge.eldritch_warden.name.custom", "%1$s the %2$s");
        add("entity.thaumaturge.taintacle_giant", "Giant Taintacle");
        add("tc.boss.enrage", "is enraged by your powerful blows.");
        add("item.thaumaturge.crimson_praetor_helm", "Crimson Praetor Helm");
        add("item.thaumaturge.crimson_praetor_chest", "Crimson Praetor Cuirass");
        add("item.thaumaturge.crimson_praetor_legs", "Crimson Praetor Greaves");
    }

    private void langConstructs() {
        add("block.thaumaturge.activator_rail", "Arcane Activator Rail");
        add("item.thaumaturge.turret_basic", "Automated Crossbow");
        add("item.thaumaturge.turret_advanced", "Advanced Automated Crossbow");
        add("item.thaumaturge.turret_bore", "Arcane Bore");
        add("item.thaumaturge.grapple_gun", "Arcane Grappler");
        add("item.thaumaturge.grapple_gun_tip", "Grappler Head");
        add("item.thaumaturge.grapple_gun_spool", "Grappler Spool");
        add("entity.thaumaturge.turret_crossbow", "Automated Crossbow");
        add("entity.thaumaturge.turret_crossbow_advanced", "Advanced Automated Crossbow");
        add("entity.thaumaturge.arcane_bore", "Arcane Bore");
        add("entity.thaumaturge.grapple", "Grappler");
        add("button.turretfocus.1", "Target Animals");
        add("button.turretfocus.2", "Target Mobs");
        add("button.turretfocus.3", "Target Players");
        add("button.turretfocus.4", "Target Friendly");
        add("gui.thaumaturge.bore.width", "Width: %s");
        add("gui.thaumaturge.bore.depth", "Depth: %s");
        add("gui.thaumaturge.bore.speed", "Speed: +%s");
        add("gui.thaumaturge.bore.properties", "Other properties:");
        add("gui.thaumaturge.bore.refining", "Refining %s");
        add("gui.thaumaturge.bore.fortune", "Fortune %s");
        add("gui.thaumaturge.bore.silktouch", "Silk Touch");
    }

    private void langDecorSweep() {
        add("block.thaumaturge.slab_greatwood", "Greatwood Slab");
        add("block.thaumaturge.slab_silverwood", "Silverwood Slab");
        add("block.thaumaturge.slab_arcane_stone", "Arcane Stone Slab");
        add("block.thaumaturge.slab_arcane_brick", "Arcane Brick Slab");
        add("block.thaumaturge.slab_ancient", "Ancient Stone Slab");
        add("block.thaumaturge.slab_eldritch", "Eldritch Stone Slab");
        add("block.thaumaturge.stairs_greatwood", "Greatwood Stairs");
        add("block.thaumaturge.stairs_silverwood", "Silverwood Stairs");
        add("block.thaumaturge.table_wood", "Wood Table");
        add("block.thaumaturge.table_stone", "Stone Table");
        add("block.thaumaturge.paving_stone_travel", "Paving Stone of Travel");
        add("block.thaumaturge.paving_stone_barrier", "Barrier Stone");
        add("block.thaumaturge.barrier", "Barrier");
        add("block.thaumaturge.amber_brick", "Amber Bricks");
        add("block.thaumaturge.flesh_block", "Block of Flesh");
        add("block.thaumaturge.effect_shock", "Static Field");
    }

    private void langOuterLands() {
        add("block.thaumaturge.obsidian_tile", "Obsidian Tile");
        add("block.thaumaturge.obsidian_totem", "Obsidian Totem");
        add("block.thaumaturge.obsidian_totem_charged", "Charged Obsidian Totem");
        add("block.thaumaturge.eldritch_stone", "Eldritch Stone");
        add("block.thaumaturge.eldritch_stone_inert", "Inert Eldritch Stone");
        add("block.thaumaturge.eldritch_rock", "Eldritch Rock");
        add("block.thaumaturge.eldritch_crust", "Eldritch Crust");
        add("block.thaumaturge.eldritch_crust_glowing", "Glowing Eldritch Crust");
        add("block.thaumaturge.stairs_eldritch", "Eldritch Stone Stairs");
        add("block.thaumaturge.eldritch_door", "Glowing Eldritch Stone");
        add("block.thaumaturge.eldritch_pedestal", "Eldritch Pedestal");
        add("block.thaumaturge.eldritch_stone_crystal", "Crystallized Eldritch Stone");
        add("block.thaumaturge.eldritch_nothing", "Nothingness");
        add("block.thaumaturge.eldritch_lock", "Eldritch Lock");
        add("block.thaumaturge.eldritch_crab_spawner", "Crusted Opening");
        add("block.thaumaturge.eldritch_trap", "Eldritch Stone");
        add("block.thaumaturge.eldritch_altar", "Eldritch Altar");
        add("block.thaumaturge.eldritch_obelisk", "Eldritch Obelisk");
        add("block.thaumaturge.eldritch_pillar", "Eldritch Pillar");
        add("block.thaumaturge.eldritch_capstone", "Eldritch Capstone");
        add("block.thaumaturge.eldritch_portal", "Eldritch Portal");
        add("item.thaumaturge.eldritch_eye", "Eldritch Eye");
        add("item.thaumaturge.runed_tablet", "Runed Tablet");
        add("tc.boss.warden", "A voice thunders through the halls: WHO DARES DISTURB MY SLUMBER?");
        add("tc.boss.golem", "You hear the grinding of ancient gears as something massive stirs...");
        add("tc.boss.crimson", "Chanting in an unknown tongue echoes from beyond the door...");
        add("tc.boss.taint", "A sickening squelching sound comes from the chamber beyond...");
        add(
                "gui.thaumaturge.altar.not_enough_vis",
                "The local aura is too weak to tear open the veil. It needs at least 100 vis.");
    }

    private void langEldritch() {

        add("entity.thaumaturge.pech", "Pech Forager");
        add("entity.thaumaturge.pech.mage", "Pech Mage");
        add("entity.thaumaturge.pech.stalker", "Pech Stalker");
        add("entity.thaumaturge.eldritch_crab", "Eldritch Crab");
        add("entity.thaumaturge.inhabited_zombie", "Shambling Husk");
        add("entity.thaumaturge.eldritch_guardian", "Eldritch Guardian");
        add("entity.thaumaturge.cultist_knight", "Crimson Knight");
        add("entity.thaumaturge.cultist_cleric", "Crimson Cleric");
        add("entity.thaumaturge.cultist_portal_lesser", "Lesser Crimson Portal");
        add("entity.thaumaturge.eldritch_orb", "Eldritch Orb");
        add("entity.thaumaturge.golem_orb", "Arcane Orb");
        add("item.thaumaturge.pech_spawn_egg", "Pech Spawn Egg");
        add("item.thaumaturge.eldritch_crab_spawn_egg", "Eldritch Crab Spawn Egg");
        add("item.thaumaturge.inhabited_zombie_spawn_egg", "Shambling Husk Spawn Egg");
        add("item.thaumaturge.eldritch_guardian_spawn_egg", "Eldritch Guardian Spawn Egg");
        add("item.thaumaturge.cultist_knight_spawn_egg", "Crimson Knight Spawn Egg");
        add("item.thaumaturge.cultist_cleric_spawn_egg", "Crimson Cleric Spawn Egg");
        add("item.thaumaturge.cultist_portal_lesser_spawn_egg", "Lesser Crimson Portal Spawn Egg");
        add("item.thaumaturge.cultist_leader_spawn_egg", "Cultist Leader Spawn Egg");
        add("item.thaumaturge.cultist_portal_greater_spawn_egg", "Greater Crimson Portal Spawn Egg");
        add("item.thaumaturge.eldritch_warden_spawn_egg", "Eldritch Warden Spawn Egg");
        add("item.thaumaturge.eldritch_golem_spawn_egg", "Eldritch Golem Spawn Egg");
        add("item.thaumaturge.taintacle_giant_spawn_egg", "Giant Taintacle Spawn Egg");
        add("item.thaumaturge.pech_wand", "Pech Wand");
        add("item.thaumaturge.crimson_blade", "Crimson Blade");
        add("item.thaumaturge.crimson_boots", "Crimson Cult Boots");
        add("item.thaumaturge.crimson_robe_helm", "Crimson Cult Hood");
        add("item.thaumaturge.crimson_robe_chest", "Crimson Cult Robe");
        add("item.thaumaturge.crimson_robe_legs", "Crimson Cult Leggings");
        add("item.thaumaturge.crimson_plate_helm", "Crimson Cult Helm");
        add("item.thaumaturge.crimson_plate_chest", "Crimson Cult Chestplate");
        add("item.thaumaturge.crimson_plate_legs", "Crimson Cult Greaves");
        add("enchantment.special.sapgreat", "Greater Sapping");
        add("item.curio.text", "What knowledge does this hold?");
        add("not.pechwand", "You cannot fathom the workings of this yet.");
        add("got.pechwand", "Hmmm, curious. I should investigate this further.");
        add("item.thaumaturge.loot_bag_common", "Common Treasure");
        add("item.thaumaturge.loot_bag_uncommon", "Uncommon Treasure");
        add("item.thaumaturge.loot_bag_rare", "Rare Treasure");
        add("tc.lootbag", "Click to open, or keep to trade.");
        add("champion.thaumaturge.name", "%1$s %2$s");
        add("champion.mod.bold", "Bold");
        add("champion.mod.spine", "Spined");
        add("champion.mod.armor", "Armored");
        add("champion.mod.mighty", "Mighty");
        add("champion.mod.grim", "Grim");
        add("champion.mod.warded", "Warded");
        add("champion.mod.warp", "Warped");
        add("champion.mod.undying", "Undying");
        add("champion.mod.fiery", "Fiery");
        add("champion.mod.sickly", "Sickly");
        add("champion.mod.venomous", "Venomous");
        add("champion.mod.vampiric", "Vampiric");
        add("champion.mod.infested", "Infested");
        add("champion.mod.tainted", "Tainted");
        add("attributes.thaumaturge.champion_mod", "Champion Modifier");
        add("attributes.thaumaturge.tainted_mod", "Tainted Modifier");
        add("block.thaumaturge.loot_crate_common", "Common Crate");
        add("block.thaumaturge.loot_crate_uncommon", "Uncommon Crate");
        add("block.thaumaturge.loot_crate_rare", "Rare Crate");
        add("block.thaumaturge.loot_urn_common", "Common Urn");
        add("block.thaumaturge.loot_urn_uncommon", "Uncommon Urn");
        add("block.thaumaturge.loot_urn_rare", "Rare Urn");
    }

    private String dyeName(DyeColor dye) {
        String[] parts = dye.getName().split("_");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(StringUtils.capitalize(part));
        }
        return name.toString();
    }

    private void trait(String key, String name, String text) {
        ResourceLocation id = TCIds.rl(key);
        add(GolemTrait.nameKey(id), name);
        add(GolemTrait.descriptionKey(id), text);
    }

    private void material(String key, String name, String text) {
        ResourceLocation id = TCIds.rl(key);
        add(GolemMaterial.nameKey(id), name);
        add(GolemMaterial.descriptionKey(id), text);
    }

    private void partLang(String kind, String key, String name, String text) {
        ResourceLocation id = TCIds.rl(key);
        add(GolemPart.nameKey(kind, id), name);
        add(GolemPart.descriptionKey(kind, id), text);
    }

    private void head(String key, String name, String text) {
        partLang("head", key, name, text);
    }

    private void arm(String key, String name, String text) {
        partLang("arm", key, name, text);
    }

    private void leg(String key, String name, String text) {
        partLang("leg", key, name, text);
    }

    private void addon(String key, String name, String text) {
        partLang("addon", key, name, text);
    }

    private void addJade() {
        add("config.jade.thaumaturge", "Thaumcraft");
        add("config.jade.plugin_thaumaturge.display", "Thaumcraft Overlay Information");
        addJadeMode("nodes", "Aura Nodes");
        addJadeMode("golems", "Golems");
        addJadeMode("vis_relays", "Vis Relays");
        addJadeMode("node_transducers", "Node Transducers");
        addJadeMode("smelters", "Essentia Smelters");
        addJadeMode("jars", "Essentia Jars and Tanks");
        addJadeMode("alembics", "Alembics");
        addJadeMode("crucibles", "Crucibles");
        addJadeMode("tubes", "Essentia Tubes");
        addJadeMode("valves", "Tube Valves");
        addJadeMode("restricted_tubes", "Restricted Tubes");
        addJadeMode("filter_tubes", "Filtered Tubes");
        addJadeMode("one_way_tubes", "One-way Tubes");
        addJadeMode("buffers", "Essentia Buffers");
        addJadeMode("thaumatoriums", "Thaumatoriums");
        addJadeMode("centrifuges", "Essentia Centrifuges");
        addJadeMode("golem_builders", "Golem Builders");
        addJadeMode("void_siphons", "Void Siphons");
        addJadeMode("deconstruction_tables", "Deconstruction Tables");
        addJadeMode("spas", "Arcane Spas");
        addJadeMode("everfull_urns", "Everfull Urns");
        addJadeMode("infernal_furnaces", "Infernal Furnaces");
        addJadeMode("focal_manipulators", "Focal Manipulators");
        add("config.jade.plugin_thaumaturge.node", "Aura Node Info");
        add("config.jade.plugin_thaumaturge.essentia", "Essentia Contents");
        add("config.jade.plugin_thaumaturge.machine", "Machine Progress");
        add("config.jade.plugin_thaumaturge.golem", "Golem Info");
        add("jade.thaumaturge.aspect_amount", "%s x%s");
        add("jade.thaumaturge.aspect_separator", ", ");
        add("jade.thaumaturge.node.type.normal", "Aura Node");
        add("jade.thaumaturge.node.type.unstable", "Unstable Node");
        add("jade.thaumaturge.node.type.dark", "Sinister Node");
        add("jade.thaumaturge.node.type.tainted", "Tainted Node");
        add("jade.thaumaturge.node.type.pure", "Pure Node");
        add("jade.thaumaturge.node.type.hungry", "Hungry Node");
        add("jade.thaumaturge.node.modifier.bright", "Bright");
        add("jade.thaumaturge.node.modifier.pale", "Pale");
        add("jade.thaumaturge.node.modifier.fading", "Fading");
        add("jade.thaumaturge.node.modified", "%s %s");
        add("jade.thaumaturge.node.aspects", "Aspects: %s");
        add("jade.thaumaturge.node.energized", "Energized");
        add("jade.thaumaturge.node.feeds_aura", "Condensing raw vis from the local aura");
        add("jade.thaumaturge.node.feeds_flux", "Devouring flux from the local aura");
        add("jade.thaumaturge.node.reverts_to", "Reverts to: %s");
        add("jade.thaumaturge.essentia.empty", "Essentia: Empty");
        add("jade.thaumaturge.essentia.none", "Essentia: None");
        add("jade.thaumaturge.essentia.empty_capacity", "Essentia: Empty (0 / %s)");
        add("jade.thaumaturge.essentia.fill", "%s: %s / %s");
        add("jade.thaumaturge.essentia.contents", "Essentia: %s");
        add("jade.thaumaturge.essentia.amount", "Essentia stored: %s / %s");
        add("jade.thaumaturge.essentia.filter", "Filtering: %s");
        add("jade.thaumaturge.essentia.unfiltered", "No filter set");
        add("jade.thaumaturge.tube.valve.open", "Valve: Open");
        add("jade.thaumaturge.tube.valve.closed", "Valve: Closed");
        add("jade.thaumaturge.tube.direction", "Flow direction: %s");
        add("jade.thaumaturge.tube.restricted", "Restricted suction");
        add("jade.thaumaturge.tube.suction", "Suction: %s (%s)");
        add("jade.thaumaturge.tube.any_aspect", "Any essentia");
        add("jade.thaumaturge.tube.closed_sides", "Closed sides: %s");
        add("jade.thaumaturge.tube.choked", "Choke: %s (%s)");
        add("jade.thaumaturge.tube.choke.reduced", "Reduced");
        add("jade.thaumaturge.tube.choke.blocked", "Blocked");
        add("jade.thaumaturge.crucible.state.cold", "Crucible: Cold");
        add("jade.thaumaturge.crucible.state.heating", "Crucible: Heating");
        add("jade.thaumaturge.crucible.state.boiling", "Crucible: Boiling");
        add("jade.thaumaturge.direction.down", "Down");
        add("jade.thaumaturge.direction.up", "Up");
        add("jade.thaumaturge.direction.north", "North");
        add("jade.thaumaturge.direction.south", "South");
        add("jade.thaumaturge.direction.west", "West");
        add("jade.thaumaturge.direction.east", "East");
        add("jade.thaumaturge.thaumatorium.recipes", "Recipes: %s / %s");
        add("jade.thaumaturge.centrifuge.state", "Centrifuge: %s");
        add("jade.thaumaturge.state.idle", "Idle");
        add("jade.thaumaturge.state.processing", "Processing");
        add("jade.thaumaturge.machine.output", "Output items: %s");
        add("jade.thaumaturge.machine.fluid", "Fluid: %s / %s mB");
        add("jade.thaumaturge.machine.stored_items", "Stored items: %s");
        add("jade.thaumaturge.deconstruction.result", "Pending aspect: %s");
        add("jade.thaumaturge.spa.mix", "Mode: Mix bath salts");
        add("jade.thaumaturge.spa.water", "Mode: Dispense fluid");
        add("jade.thaumaturge.focal.vis", "Vis remaining: %s");
        add("jade.thaumaturge.focal.focus", "Focus: %s");
        add("jade.thaumaturge.focal.focus_inserted", "Focus inserted");
        add("jade.thaumaturge.focal.no_focus", "No focus inserted");
        add("jade.thaumaturge.machine.progress", "Progress: %s%%");
        add("jade.thaumaturge.machine.heat", "Heat: %s%%");
        add("jade.thaumaturge.transducer.status.0", "No node below");
        add("jade.thaumaturge.transducer.status.1", "Transducing");
        add("jade.thaumaturge.transducer.status.2", "Node energized");
        add("jade.thaumaturge.transducer.charge", "Charge: %s%%");
        add("jade.thaumaturge.relay.linked_node", "Linked to energized node");
        add("jade.thaumaturge.relay.linked", "Linked");
        add("jade.thaumaturge.relay.linked_relay", "Linked through %s relays");
        add("jade.thaumaturge.relay.unlinked", "No energized node in range");
        add("jade.thaumaturge.golem.rank", "Rank %s");
        add("jade.thaumaturge.golem.xp", "Experience: %s / %s");
    }

    private void addJadeMode(String path, String label) {
        String key = "config.jade.plugin_thaumaturge.display." + path;
        add(key, label);
        add(key + "_always", "Always");
        add(key + "_goggles", "Goggles");
    }
}
