package com.leclowndu93150.thaumaturge.data.recipe;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.label.LabelFilterRecipe;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionEnchantmentRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumaturge.mixin.data.recipes.RecipeProviderAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockFamilies;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.CustomCraftingRecipeBuilder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TTRecipeProvider extends RecipeProvider {
    private static final int ARCANE_GRINDSTONE_VIS = 50;
    private static final List<GearShape> TOOL_SHAPES = List.of(new GearShape(RecipeCategory.COMBAT, true, List.of("I", "I", "S")),
            new GearShape(RecipeCategory.TOOLS, true, List.of("III", " S ", " S ")), new GearShape(RecipeCategory.TOOLS, true, List.of("II", "IS", " S")),
            new GearShape(RecipeCategory.TOOLS, true, List.of("I", "S", "S")), new GearShape(RecipeCategory.TOOLS, true, List.of("II", " S", " S")),
            new GearShape(RecipeCategory.COMBAT, true, List.of("  I", " S ", "S  ")));
    private static final List<GearShape> ARMOR_SHAPES = List.of(new GearShape(RecipeCategory.COMBAT, false, List.of("III", "I I")),
            new GearShape(RecipeCategory.COMBAT, false, List.of("I I", "III", "III")), new GearShape(RecipeCategory.COMBAT, false, List.of("III", "I I", "I I")),
            new GearShape(RecipeCategory.COMBAT, false, List.of("I I", "I I")));
    private static final int ELEMENTAL_TOOL_INSTABILITY = 1;
    private static final int CLOTH_VIS = 100;

    private TTRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    private static ResearchGate gate(String path) {
        return new ResearchGate(TTIds.rl(path), Optional.empty(), false);
    }

    private static ResearchGate gate(String path, int stage) {
        return new ResearchGate(TTIds.rl(path), Optional.of(stage), false);
    }

    private static ResourceKey<Recipe<?>> recipeId(String path) {
        return ResourceKey.create(Registries.RECIPE, TTIds.rl(path));
    }

    private static AspectCost cost(ResourceKey<IAspect> aspect, int amount) {
        return new AspectCost(aspect, amount);
    }

    private static <R extends ArcaneWorkbenchRecipeBuilder<R>> R priced(R builder, AspectCost... costs) {
        for (AspectCost cost : costs) {
            builder.aspect(cost.aspect(), cost.amount());
        }
        return builder;
    }

    private static CrucibleRecipeBuilder priced(CrucibleRecipeBuilder builder, AspectCost... costs) {
        for (AspectCost cost : costs) {
            builder.aspect(cost.aspect(), cost.amount());
        }
        return builder;
    }

    private static InfusionRecipeBuilder priced(InfusionRecipeBuilder builder, AspectCost... costs) {
        for (AspectCost cost : costs) {
            builder.aspect(cost.aspect(), cost.amount());
        }
        return builder;
    }

    private static ShapedRecipeBuilder rows(ShapedRecipeBuilder builder, String... rows) {
        for (String row : rows) {
            builder.pattern(row);
        }
        return builder;
    }

    private static ArcaneWorkbenchShapedRecipeBuilder rows(ArcaneWorkbenchShapedRecipeBuilder builder, String... rows) {
        for (String row : rows) {
            builder.pattern(row);
        }
        return builder;
    }

    private void commit(RecipeBuilder builder) {
        builder.save(output);
    }

    private void commit(RecipeBuilder builder, String id) {
        builder.save(output, id);
    }

    private void commit(RecipeBuilder builder, ResourceKey<Recipe<?>> key) {
        builder.save(output, key);
    }

    private void commit(RecipeBuilder builder, RecipeOutput target) {
        builder.save(target);
    }

    private void commit(RecipeBuilder builder, RecipeOutput target, String id) {
        builder.save(target, id);
    }

    private RecipeOutput whenTagPresent(TagKey<Item> tag) {
        return output.withConditions(new NotCondition(new TagEmptyCondition<>(tag)));
    }

    private static InfusionRecipeBuilder parts(InfusionRecipeBuilder builder, Ingredient... ingredients) {
        for (Ingredient ingredient : ingredients) {
            builder.component(ingredient);
        }
        return builder;
    }

    @Override
    protected void buildRecipes() {
        buildDustTriggerRecipes();
        buildSalisMundusRecipe();
        buildArcaneWorkbenchRecipes();
        buildBannerRecipes();
        buildGearRecipes();
        buildInfusionAltarRecipes();
        buildInfusionEnchantmentRecipes();
        buildRunicAugmentRecipe();
        buildElementalToolRecipes();
        buildEssentiaReservoirRecipe();
        buildTravellerBootsRecipe();
        buildArmorTrimRecipes();
        buildThaumostaticHarnessRecipe();
        buildRechargePedestalRecipe();
        buildFocalManipulatorRecipe();
        buildCrucibleRecipes();
        buildCrystalClusterRecipes();
        buildFocusRecipes();
        buildIngredientRecipes();
        buildGolemancyRecipes();
        buildAuraDeviceRecipes();
        buildConstructRecipes();
        buildDecorRecipes();
        buildWardingRecipes();
        buildNoiseDeviceRecipes();
        buildEssentiaMachineRecipes();
        buildFluxMachineRecipes();
        buildBaubleRecipes();
        buildWearableInfusionRecipes();
        buildWandRecipes();
        buildNodeHusbandryRecipes();

        commit(shapeless(RecipeCategory.MISC, TTItems.SCRIBING_TOOLS).requires(TTItems.PHIAL).requires(Tags.Items.DYES_BLACK).requires(Tags.Items.FEATHERS).unlockedBy("has", has(TTItems.PHIAL)));

        commit(shapeless(RecipeCategory.MISC, TTItems.SCRIBING_TOOLS).requires(Items.GLASS_BOTTLE).requires(Tags.Items.DYES_BLACK).requires(Tags.Items.FEATHERS).unlockedBy("has",
                has(Tags.Items.GLASS_PANES)), TTIds.MODID + ":scribing_tools_alt");

        commit(shapeless(RecipeCategory.MISC, TTItems.LABEL, 4).requires(Tags.Items.DYES_BLACK).requires(Tags.Items.SLIME_BALLS).requires(Items.PAPER, 4).unlockedBy("has",
                has(Tags.Items.SLIME_BALLS)));

        commit(shapeless(RecipeCategory.MISC, TTItems.LABEL).requires(TTItems.LABEL).unlockedBy("has", has(TTItems.LABEL)), TTIds.MODID + ":label_clear");

        SpecialRecipeBuilder.special(LabelFilterRecipe::new).save(output, TTIds.rl("label_filter").toString());

        commit(rows(shaped(RecipeCategory.MISC, TTItems.JAR_BRACE, 2), "SBS", "B B", "SBS").define('S', Tags.Items.RODS_WOODEN).define('B', TTItemTags.NUGGETS_BRASS).unlockedBy("has",
                has(TTItemTags.NUGGETS_BRASS)));

        for (DyeColor color : DyeColor.values()) {
            shapeless(RecipeCategory.MISC, TTItems.NITORS.get(color).get()).requires(TTItemTags.NITORS).requires(color.getTag()).group(NITOR_DYE_GROUP).unlockedBy("has", has(TTItemTags.NITORS))
                    .save(output, TTIds.MODID + ":nitors/" + color.getName());
        }

        ShapedRecipeBuilder arcaneStone = shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.STONE_ARCANE, 8);
        rows(arcaneStone, "SSS", "SVS", "SSS");
        arcaneStone.define('S', Tags.Items.STONES);
        arcaneStone.define('V', TTItems.ESSENTIA_CRYSTAL);
        arcaneStone.unlockedBy("has", has(TTItems.ESSENTIA_CRYSTAL));
        commit(arcaneStone);

        TTBlockFamilies.getAllFamilies().forEach(family -> {
            family.getVariants().forEach((variant, result) -> {
                if (family.shouldGenerateCraftingRecipe()) {
                    ItemLike base = this.getBaseBlockForCrafting(family, variant);
                    ((RecipeProviderAccessor) (RecipeProvider) this).thaumaturge$generateCraftingRecipe(family, variant, result, base);
                    if (variant == BlockFamily.Variant.CRACKED) {
                        this.smeltingResultFromBase(result, base);
                    }
                }

                if (family.shouldGenerateStonecutterRecipe()) {
                    Block base = family.getBaseBlock();
                    ((RecipeProviderAccessor) (RecipeProvider) this).thaumaturge$generateStonecutterRecipe(family, variant, base);
                }
            });
        });

        commit(shapeless(RecipeCategory.BUILDING_BLOCKS, TTItems.PLANK_GREATWOOD, 4).requires(TTItemTags.GREATWOOD_LOGS).unlockedBy("has", has(TTItemTags.GREATWOOD_LOGS)));

        commit(shapeless(RecipeCategory.BUILDING_BLOCKS, TTItems.PLANK_SILVERWOOD, 4).requires(TTItemTags.SILVERWOOD_LOGS).unlockedBy("has", has(TTItemTags.SILVERWOOD_LOGS)));

        commit(rows(shaped(RecipeCategory.MISC, TTItems.PHIAL, 8), " C ", "P P", " P ").define('C', Items.CLAY_BALL).define('P', Tags.Items.GLASS_BLOCKS).unlockedBy("has",
                has(Tags.Items.GLASS_BLOCKS)));

        oreSmelting(TTItems.QUICKSILVER, TTItemTags.ORES_CINNABAR, 1F, "quicksilver");
        rawSmelting(TTItems.QUICKSILVER, TTItemTags.RAW_MATERIALS_CINNABAR, 0.7F, "quicksilver", "raw_cinnabar");
        oreSmelting(TTItems.AMBER, TTItemTags.ORES_AMBER, 1F, "amber");
        oreSmelting(Items.QUARTZ, Tags.Items.ORES_QUARTZ, 0.2F, "quartz");

        SimpleCookingRecipeBuilder.smoking(single(Items.RESIN_CLUMP), RecipeCategory.MISC, TTItems.AMBER.get(), AMBER_FROM_RESIN_XP, AMBER_FROM_RESIN_TIME).group("amber")
                .unlockedBy("has", has(Items.RESIN_CLUMP)).save(output, ResourceKey.create(Registries.RECIPE, TTIds.rl("amber_from_resin")));

        block3x3(TTItems.METAL_BRASS_BLOCK, TTItemTags.INGOTS_BRASS, TTItems.INGOT_BRASS, TTItemTags.STORAGE_BLOCKS_BRASS);
        block3x3(TTItems.METAL_THAUMIUM_BLOCK, TTItemTags.INGOTS_THAUMIUM, TTItems.INGOT_THAUMIUM, TTItemTags.STORAGE_BLOCKS_THAUMIUM);
        block3x3(TTItems.METAL_VOID_BLOCK, TTItemTags.INGOTS_VOID_METAL, TTItems.INGOT_VOID, TTItemTags.STORAGE_BLOCKS_VOID_METAL);
        block2x2(TTItems.AMBER_BLOCK, TTItemTags.GEMS_AMBER, TTItems.AMBER, TTItemTags.STORAGE_BLOCKS_AMBER);

        nuggets3x3(Items.QUARTZ, TTItemTags.NUGGETS_QUARTZ, TTItems.NUGGET_QUARTZ, Tags.Items.GEMS_QUARTZ);
        nuggets3x3(TTItems.QUICKSILVER, TTItemTags.NUGGETS_QUICKSILVER, TTItems.NUGGET_QUICKSILVER, TTItemTags.GEMS_QUICKSILVER);
        nuggets3x3(TTItems.INGOT_BRASS, TTItemTags.NUGGETS_BRASS, TTItems.NUGGET_BRASS, TTItemTags.INGOTS_BRASS);
        nuggets3x3(TTItems.INGOT_THAUMIUM, TTItemTags.NUGGETS_THAUMIUM, TTItems.NUGGET_THAUMIUM, TTItemTags.INGOTS_THAUMIUM);
        nuggets3x3(TTItems.INGOT_VOID, TTItemTags.NUGGETS_VOID_METAL, TTItems.NUGGET_VOID, TTItemTags.INGOTS_VOID_METAL);

        plateRecipe(TTItems.PLATE_IRON, Tags.Items.INGOTS_IRON);
        plateRecipe(TTItems.PLATE_BRASS, TTItemTags.INGOTS_BRASS);
        plateRecipe(TTItems.PLATE_THAUMIUM, TTItemTags.INGOTS_THAUMIUM);
        plateRecipe(TTItems.PLATE_VOID, TTItemTags.INGOTS_VOID_METAL);

        clusterSmelting(Items.IRON_INGOT, TTItems.CLUSTER_IRON, "iron_ingot");
        clusterSmelting(Items.GOLD_INGOT, TTItems.CLUSTER_GOLD, "gold_ingot");
        clusterSmelting(Items.COPPER_INGOT, TTItems.CLUSTER_COPPER, "copper_ingot");
        clusterSmelting(TTItems.QUICKSILVER, TTItems.CLUSTER_CINNABAR, "quicksilver");
        clusterSmelting(Items.QUARTZ, TTItems.CLUSTER_QUARTZ, "quartz");

        commit(shapeless(RecipeCategory.MISC, TTItems.QUICKSILVER).requires(TTItems.PLANT_SHIMMERLEAF).unlockedBy("has", has(TTItems.PLANT_SHIMMERLEAF)),
                TTIds.MODID + ":quicksilver_from_shimmerleaf");

        commit(shapeless(RecipeCategory.MISC, Items.BLAZE_POWDER).requires(TTItems.PLANT_CINDERPEARL).unlockedBy("has", has(TTItems.PLANT_CINDERPEARL)),
                TTIds.MODID + ":blaze_powder_from_cinderpearl");

        commit(rows(shaped(RecipeCategory.DECORATIONS, TTBlocks.CANDLES.get(DyeColor.WHITE).get(), 3), " S ", " T ", " T ").define('S', Tags.Items.STRINGS).define('T', TTItems.TALLOW.get())
                .unlockedBy("has_tallow", has(TTItems.TALLOW.get())));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CANDLE_HOLDERS.get(CandleHolderMaterial.BRASS)), 10), cost(TTAspects.IGNIS, 1)), " N ", "NPN")
                .define('N', TTItemTags.NUGGETS_BRASS).define('P', TTItemTags.PLATES_BRASS).gate(gate("candle_holders", 1)).unlockedBy("has", has(TTItemTags.PLATES_BRASS)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CANDLE_HOLDERS.get(CandleHolderMaterial.THAUMIUM)), 25), cost(TTAspects.IGNIS, 1), cost(TTAspects.ORDO, 1)), " N ", "NPN")
                .define('N', TTItemTags.NUGGETS_THAUMIUM).define('P', TTItemTags.PLATES_THAUMIUM).gate(gate("candle_holders", 2)).unlockedBy("has", has(TTItemTags.PLATES_THAUMIUM)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CANDLE_HOLDERS.get(CandleHolderMaterial.VOID)), 50), cost(TTAspects.IGNIS, 1), cost(TTAspects.PERDITIO, 1)), " N ", "NPN")
                .define('N', TTItemTags.NUGGETS_VOID_METAL).define('P', TTItemTags.PLATES_VOID_METAL).gate(gate("candle_holders", 3)).unlockedBy("has", has(TTItemTags.PLATES_VOID_METAL)));
        for (DyeColor dye : DyeColor.values()) {
            shapeless(RecipeCategory.DECORATIONS, TTBlocks.CANDLES.get(dye).get()).requires(dyeTag(dye)).requires(TTItemTags.CANDLES).unlockedBy("has_candle", has(TTItemTags.CANDLES)).save(output,
                    TTIds.MODID + ":candle_" + dye.getName() + "_from_dye");
        }

        commit(parts(priced(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.THAUMONOMICON_LINKING), single(TTItems.THAUMONOMICON_SHARING)), cost(TTAspects.COGNITIO, 40),
                cost(TTAspects.SENSUS, 20), cost(TTAspects.ALIENIS, 10)), single(TTItems.VOID_SEED), single(TTItems.BRAIN), single(TTItems.VOID_SEED), single(Items.ENDER_EYE)).instability(2)
                .gate(gate("link_book", 1)).unlockedBy("has", has(TTItems.THAUMONOMICON_SHARING)));

        woodFromLogs(TTItems.WOOD_GREATWOOD, TTItems.LOG_GREATWOOD);
        woodFromLogs(TTItems.STRIPPED_WOOD_GREATWOOD, TTItems.STRIPPED_LOG_GREATWOOD);
        woodFromLogs(TTItems.WOOD_SILVERWOOD, TTItems.LOG_SILVERWOOD);
        woodFromLogs(TTItems.STRIPPED_WOOD_SILVERWOOD, TTItems.STRIPPED_LOG_SILVERWOOD);
    }

    private void block3x3(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag) {
        commit(rows(shaped(RecipeCategory.MISC, block), "###", "###", "###").define('#', baseTag).unlockedBy("has", has(baseTag)));

        commit(shapeless(RecipeCategory.MISC, baseItem, 9).requires(blockTag).unlockedBy("has", has(blockTag)),
                TTIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(baseItem.asItem()).getPath() + "_from_block");
    }

    private void block2x2(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag) {
        commit(rows(shaped(RecipeCategory.MISC, block), "##", "##").define('#', baseTag).unlockedBy("has", has(baseTag)));

        commit(shapeless(RecipeCategory.MISC, baseItem, 4).requires(blockTag).unlockedBy("has", has(blockTag)),
                TTIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(baseItem.asItem()).getPath() + "_from_block");
    }

    private void nuggets3x3(ItemLike item, TagKey<Item> nuggetsTag, ItemLike nuggets, TagKey<Item> itemTag) {
        commit(rows(shaped(RecipeCategory.MISC, item), "###", "###", "###").define('#', nuggetsTag).unlockedBy("has", has(nuggetsTag)),
                TTIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(nuggets.asItem()).getPath() + "_from_nuggets");

        commit(shapeless(RecipeCategory.MISC, nuggets, 9).requires(itemTag).unlockedBy("has", has(itemTag)));
    }

    private void oreSmelting(ItemLike item, TagKey<Item> oreTag, float xp, String group) {
        SimpleCookingRecipeBuilder.smelting(tagged(oreTag), RecipeCategory.MISC, CookingBookCategory.MISC, item, xp, 200).group(group).unlockedBy("has", this.has(oreTag)).save(this.output,
                recipeId(getItemName(item) + "_from_ore"));

        SimpleCookingRecipeBuilder.blasting(tagged(oreTag), RecipeCategory.MISC, CookingBookCategory.MISC, item, xp, 100).group(group).unlockedBy("has", this.has(oreTag)).save(this.output,
                recipeId(getItemName(item) + "_blasting_from_ore"));
    }

    private void rawSmelting(ItemLike item, TagKey<Item> rawTag, float xp, String group, String rawName) {
        SimpleCookingRecipeBuilder.smelting(tagged(rawTag), RecipeCategory.MISC, CookingBookCategory.MISC, item, xp, 200).group(group).unlockedBy("has", this.has(rawTag)).save(this.output,
                recipeId(getItemName(item) + "_from_" + rawName));

        SimpleCookingRecipeBuilder.blasting(tagged(rawTag), RecipeCategory.MISC, CookingBookCategory.MISC, item, xp, 100).group(group).unlockedBy("has", this.has(rawTag)).save(this.output,
                recipeId(getItemName(item) + "_blasting_from_" + rawName));
    }

    private void clusterSmelting(ItemLike item, ItemLike cluster, String group) {

        SimpleCookingRecipeBuilder.smelting(single(cluster), RecipeCategory.MISC, CookingBookCategory.MISC, new ItemStackTemplate(item.asItem(), 2), 1F, 200).group(group)
                .unlockedBy("has", this.has(cluster)).save(this.output, recipeId(getItemName(item) + "_from_cluster"));

        SimpleCookingRecipeBuilder.blasting(single(cluster), RecipeCategory.MISC, CookingBookCategory.MISC, new ItemStackTemplate(item.asItem(), 2), 1F, 100).group(group)
                .unlockedBy("has", this.has(cluster)).save(this.output, recipeId(getItemName(item) + "_blasting_from_cluster"));
    }

    @Override
    protected void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) {
        SingleItemRecipeBuilder.stonecutting(single(base), category, result, count).unlockedBy(getHasName(base), this.has(base)).save(this.output,
                recipeId(getConversionRecipeName(result, base) + "_stonecutting"));
    }

    private void plateRecipe(ItemLike plate, TagKey<Item> ingotTag) {
        commit(rows(shaped(RecipeCategory.MISC, plate, 3), "NNN").define('N', ingotTag).unlockedBy("has", has(ingotTag)));
    }

    private void buildWardingRecipes() {
        ResearchGate wardedArcana = gate("warded_arcana");
        commit(rows(
                priced(arcaneShaped(new ItemStackTemplate(TTItems.WARDED_GLASS.get(), 8), 25), cost(TTAspects.AQUA, 5), cost(TTAspects.ORDO, 10), cost(TTAspects.TERRA, 5), cost(TTAspects.IGNIS, 5)),
                "GGG", "WBW", "GGG").define('G', Tags.Items.GLASS_BLOCKS).define('W', TTItems.PLANK_GREATWOOD).define('B', TTItems.BRAIN).gate(wardedArcana).unlockedBy("has", has(TTItems.BRAIN)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_DOOR.get()), 45), cost(TTAspects.AQUA, 20), cost(TTAspects.ORDO, 10), cost(TTAspects.TERRA, 10), cost(TTAspects.IGNIS, 5)),
                "TDT", "DBD", "TDT").define('T', TTItemTags.INGOTS_THAUMIUM).define('D', TTItems.PLANK_GREATWOOD).define('B', TTItems.BRAIN).gate(wardedArcana).unlockedBy("has", has(TTItems.BRAIN)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_PRESSURE_PLATE.get()), 45), cost(TTAspects.AQUA, 20), cost(TTAspects.ORDO, 10), cost(TTAspects.TERRA, 10),
                cost(TTAspects.IGNIS, 5)), " B ", "TDT").define('T', TTItemTags.INGOTS_THAUMIUM).define('D', TTItems.PLANK_GREATWOOD).define('B', TTItems.BRAIN).gate(wardedArcana)
                .unlockedBy("has", has(TTItems.BRAIN)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_KEY_IRON.get(), 2), 10), cost(TTAspects.AQUA, 5), cost(TTAspects.ORDO, 5)), "NNI", "N  ")
                .define('N', Tags.Items.NUGGETS_IRON).define('I', Tags.Items.INGOTS_IRON).gate(wardedArcana).unlockedBy("has", has(Tags.Items.INGOTS_IRON)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_KEY_GOLD.get(), 2), 10), cost(TTAspects.AQUA, 5), cost(TTAspects.ORDO, 5)), "NNI", "N  ")
                .define('N', Tags.Items.NUGGETS_GOLD).define('I', Tags.Items.INGOTS_GOLD).gate(wardedArcana).unlockedBy("has", has(Tags.Items.INGOTS_GOLD)));
    }

    private void buildDecorRecipes() {
        ResearchGate artificeGate = gate("paving_stones");

        stairsRecipe(TTBlocks.STAIRS_GREATWOOD.get(), TTItemTags.PLANKS_GREATWOOD);
        stairsRecipe(TTBlocks.STAIRS_SILVERWOOD.get(), TTItemTags.PLANKS_SILVERWOOD);
        slabRecipe(TTBlocks.SLAB_GREATWOOD.get(), TTItemTags.PLANKS_GREATWOOD);
        slabRecipe(TTBlocks.SLAB_SILVERWOOD.get(), TTItemTags.PLANKS_SILVERWOOD);
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_GRINDSTONE), ARCANE_GRINDSTONE_VIS), cost(TTAspects.ORDO, 2), cost(TTAspects.PERDITIO, 2)), " T ", "TGT", " T ")
                .define('T', TTItemTags.INGOTS_THAUMIUM).define('G', Items.GRINDSTONE).gate(gate("infusion_enchantment")).unlockedBy("has", has(TTItemTags.INGOTS_THAUMIUM)));
        commit(doorBuilder(TTBlocks.DOOR_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_door").unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));
        commit(trapdoorBuilder(TTBlocks.TRAPDOOR_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_trapdoor").unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));
        commit(fenceBuilder(TTBlocks.FENCE_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_fence").unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));
        commit(fenceGateBuilder(TTBlocks.FENCE_GATE_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_fence_gate").unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));
        commit(buttonBuilder(TTBlocks.BUTTON_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_button").unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));
        commit(pressurePlateBuilder(RecipeCategory.REDSTONE, TTBlocks.PRESSURE_PLATE_GREATWOOD.get(), tagged(TTItemTags.PLANKS_GREATWOOD)).group("wooden_pressure_plate").unlockedBy("has",
                has(TTItemTags.PLANKS_GREATWOOD)));
        commit(doorBuilder(TTBlocks.DOOR_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_door").unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));
        commit(trapdoorBuilder(TTBlocks.TRAPDOOR_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_trapdoor").unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));
        commit(fenceBuilder(TTBlocks.FENCE_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_fence").unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));
        commit(fenceGateBuilder(TTBlocks.FENCE_GATE_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_fence_gate").unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));
        commit(buttonBuilder(TTBlocks.BUTTON_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_button").unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));
        commit(pressurePlateBuilder(RecipeCategory.REDSTONE, TTBlocks.PRESSURE_PLATE_SILVERWOOD.get(), tagged(TTItemTags.PLANKS_SILVERWOOD)).group("wooden_pressure_plate").unlockedBy("has",
                has(TTItemTags.PLANKS_SILVERWOOD)));
        pillarRecipe(TTBlocks.STONE_ARCANE_PILLAR.get(), TTBlocks.STONE_ARCANE.get());
        pillarRecipe(TTBlocks.STONE_ANCIENT_PILLAR.get(), TTBlocks.STONE_ANCIENT.get());
        pillarRecipe(TTBlocks.STONE_ELDRITCH_PILLAR.get(), TTBlocks.ELDRITCH_STONE.get());

        commit(rows(shaped(RecipeCategory.DECORATIONS, TTItems.TABLE_WOOD), "SSS", "W W").define('S', ItemTags.WOODEN_SLABS).define('W', ItemTags.PLANKS).unlockedBy("has",
                has(ItemTags.WOODEN_SLABS)));

        commit(rows(shaped(RecipeCategory.DECORATIONS, TTItems.TABLE_STONE), "SSS", "W W").define('S', Items.STONE_SLAB).define('W', Tags.Items.STONES).unlockedBy("has", has(Items.STONE_SLAB)));

        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.FLESH_BLOCK), "###", "###", "###").define('#', Items.ROTTEN_FLESH).unlockedBy("has", has(Items.ROTTEN_FLESH)));

        commit(shapeless(RecipeCategory.MISC, Items.ROTTEN_FLESH, 9).requires(TTItems.FLESH_BLOCK).unlockedBy("has", has(TTItems.FLESH_BLOCK)), TTIds.MODID + ":rotten_flesh_from_flesh_block");

        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, TTItems.OBSIDIAN_TILE, Items.OBSIDIAN, 1);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, TTItems.OBSIDIAN_TOTEM, Items.OBSIDIAN, 1);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, TTItems.OBSIDIAN_TOTEM, TTItems.OBSIDIAN_TILE, 1);

        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.AMBER_BRICK, 4), "##", "##").define('#', TTItemTags.STORAGE_BLOCKS_AMBER).unlockedBy("has", has(TTItemTags.STORAGE_BLOCKS_AMBER)));

        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.AMBER_BLOCK, 4), "##", "##").define('#', TTItems.AMBER_BRICK).unlockedBy("has", has(TTItems.AMBER_BRICK)),
                TTIds.MODID + ":amber_block_from_brick");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PAVING_STONE_BARRIER.get(), 4), 50), "SAS", "SBS").define('S', TTItems.STONE_ARCANE_BRICK).define('A', crystal(TTAspects.IGNIS))
                .define('B', crystal(TTAspects.ORDO)).gate(artificeGate).unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PAVING_STONE_TRAVEL.get(), 4), 50), "SAS", "SBS").define('S', TTItems.STONE_ARCANE_BRICK).define('A', crystal(TTAspects.AER))
                .define('B', crystal(TTAspects.TERRA)).gate(artificeGate).unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK)));
    }

    private void stairsRecipe(Block result, Block base) {
        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, result, 4), "K  ", "KK ", "KKK").define('K', base).unlockedBy("has", has(base)));
    }

    private void pillarRecipe(Block result, Block base) {
        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, result, 2), "K", "K").define('K', base).unlockedBy("has", has(base)));
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1);
    }

    private void stairsRecipe(Block result, TagKey<Item> base) {
        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, result, 4), "K  ", "KK ", "KKK").define('K', base).unlockedBy("has", has(base)));
    }

    private void slabRecipe(Block result, TagKey<Item> base) {
        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, result, 6), "KKK").define('K', base).unlockedBy("has", has(base)));
    }

    private void buildConstructRecipes() {
        commit(arcaneShapeless(new ItemStackTemplate(TTItems.ACTIVATOR_RAIL), 10).requires(Items.ACTIVATOR_RAIL).gate(gate("first_steps")).unlockedBy("has", has(Items.ACTIVATOR_RAIL)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.TURRET_BASIC), 100), cost(TTAspects.AER, 1)), "BGI", "WMW", "S S").define('G', TTItems.MECHANISM_SIMPLE)
                .define('I', TTItemTags.PLATES_IRON).define('S', Tags.Items.RODS_WOODEN).define('M', TTItems.MIND_CLOCKWORK).define('B', Tags.Items.TOOLS_BOW).define('W', TTItemTags.PLANKS_GREATWOOD)
                .gate(gate("basic_turret")).unlockedBy("has", has(TTItems.MIND_CLOCKWORK)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.TURRET_ADVANCED), 150), cost(TTAspects.AER, 2)), "PMP", "PTP").define('T', TTItems.TURRET_BASIC)
                .define('P', TTItemTags.PLATES_IRON).define('M', TTItems.MIND_BIOTHAUMIC).gate(gate("advanced_turret")).unlockedBy("has", has(TTItems.MIND_BIOTHAUMIC)));

        commit(priced(
                parts(infusion(RecipeCategory.TOOLS, new ItemStackTemplate(TTItems.ARCANE_BORE), single(TTItems.TURRET_BASIC.get())), tagged(TTItemTags.PLANKS_GREATWOOD),
                        tagged(TTItemTags.PLANKS_GREATWOOD), single(TTItems.MECHANISM_COMPLEX.get()), tagged(TTItemTags.PLATES_BRASS), single(Items.DIAMOND_PICKAXE), single(Items.DIAMOND_SHOVEL),
                        single(TTItems.MORPHIC_RESONATOR.get()), single(TTItems.RARE_EARTH.get())),
                cost(TTAspects.POTENTIA, 25), cost(TTAspects.TERRA, 25), cost(TTAspects.MACHINA, 100), cost(TTAspects.VACUOS, 25), cost(TTAspects.MOTUS, 25)).instability(4).gate(gate("arcane_bore"))
                .unlockedBy("has", has(TTItems.TURRET_BASIC)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GRAPPLE_GUN_TIP), 25), cost(TTAspects.TERRA, 1)), "BRB", "RHR", "BRB").define('B', TTItemTags.PLATES_BRASS)
                .define('R', TTItems.RARE_EARTH).define('H', Items.TRIPWIRE_HOOK).gate(gate("grapple_gun")).unlockedBy("has", has(TTItems.RARE_EARTH)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GRAPPLE_GUN_SPOOL), 25), cost(TTAspects.AQUA, 1)), "SHS", "SGS", "SSS").define('G', TTItems.MECHANISM_SIMPLE)
                .define('S', Tags.Items.STRINGS).define('H', Items.TRIPWIRE_HOOK).gate(gate("grapple_gun")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GRAPPLE_GUN), 75), cost(TTAspects.AER, 1), cost(TTAspects.IGNIS, 1)), "  S", "TII", " BW").define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON).define('T', TTItems.GRAPPLE_GUN_TIP).define('W', ItemTags.PLANKS).define('S', TTItems.GRAPPLE_GUN_SPOOL).gate(gate("grapple_gun"))
                .unlockedBy("has", has(TTItems.GRAPPLE_GUN_TIP)));
    }

    private void buildFocalManipulatorRecipe() {
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.FOCAL_MANIPULATOR), 100), cost(TTAspects.TERRA, 1), cost(TTAspects.AQUA, 1)), "ISI", "BRB", "GTG")
                .define('I', TTItemTags.PLATES_IRON).define('S', TTItems.SLAB_ARCANE_STONE).define('B', TTItems.STONE_ARCANE).define('R', TTItems.VIS_RESONATOR).define('G', Tags.Items.INGOTS_GOLD)
                .define('T', TTItems.TABLE_STONE).gate(gate("base_auromancy", 1)).unlockedBy("has", has(TTItems.VIS_RESONATOR)));
    }

    private void buildInfusionAltarRecipes() {
        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.INFUSION_MATRIX), 150).allAspects(), "S S", " N ", "S S").define('S', TTItems.STONE_ARCANE_BRICK).define('N', TTItemTags.NITORS)
                .gate(gate("infusion", 1)).unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PEDESTAL_ARCANE), 10), "SSS", " B ", "SSS").define('S', TTItems.SLAB_ARCANE_STONE).define('B', TTItems.STONE_ARCANE)
                .gate(gate("infusion")).unlockedBy("has", has(TTItems.STONE_ARCANE)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PEDESTAL_ANCIENT), 150), "SSS", " B ", "SSS").define('S', TTItems.SLAB_ANCIENT).define('B', TTItems.STONE_ANCIENT)
                .gate(gate("infusion_ancient")).unlockedBy("has", has(TTItems.STONE_ANCIENT)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PEDESTAL_ELDRITCH), 150), "SSS", " B ", "SSS").define('S', TTItems.SLAB_ELDRITCH).define('B', TTItems.STONE_ELDRITCH_TILE)
                .gate(gate("infusion_eldritch")).unlockedBy("has", has(TTItems.STONE_ELDRITCH_TILE)));
    }

    private void buildEssentiaReservoirRecipe() {
        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.ESSENTIA_RESERVOIR), single(TTItems.TUBE_BUFFER.get())), single(TTItems.INGOT_VOID.get()),
                        single(TTItems.JAR_NORMAL.get()), single(TTItems.JAR_NORMAL.get()), single(TTItems.INGOT_VOID.get()), single(TTItems.JAR_NORMAL.get()), single(TTItems.JAR_NORMAL.get())),
                cost(TTAspects.AQUA, 8), cost(TTAspects.VACUOS, 8), cost(TTAspects.PRAECANTATIO, 8), cost(TTAspects.PERMUTATIO, 8)).instability(6).gate(gate("essentia_reservoir"))
                .unlockedBy("has", has(TTItems.TUBE_BUFFER)));
    }

    private void buildElementalToolRecipes() {
        for (ElementalToolSpec spec : elementalToolSpecs()) {
            elementalTool(spec);
        }

        commit(priced(
                parts(infusion(RecipeCategory.TOOLS, enchantedTool(TTItems.PRIMAL_CRUSHER.get(), Map.of(InfusionEnchantment.DESTRUCTIVE, 1, InfusionEnchantment.REFINING, 1)),
                        single(TTItems.PRIMORDIAL_PEARL.get())), single(TTItems.VOID_PICKAXE.get()), single(TTItems.VOID_SHOVEL.get()), single(TTItems.ELEMENTAL_PICKAXE.get()),
                        single(TTItems.ELEMENTAL_SHOVEL.get())),
                cost(TTAspects.TERRA, 75), cost(TTAspects.INSTRUMENTUM, 75), cost(TTAspects.PERDITIO, 50), cost(TTAspects.VACUOS, 50), cost(TTAspects.AVERSIO, 50), cost(TTAspects.ALIENIS, 50),
                cost(TTAspects.DESIDERIUM, 50)).instability(6).gate(gate("primal_crusher")).unlockedBy("has", has(TTItems.PRIMORDIAL_PEARL)));
    }

    private record ElementalToolSpec(RecipeCategory category, ItemStackTemplate result, Item base, ResourceKey<IAspect> firstCrystal, ResourceKey<IAspect> secondCrystal, List<AspectCost> costs) {
    }

    private static ElementalToolSpec elemental(RecipeCategory category, ItemStackTemplate result, Item base, ResourceKey<IAspect> firstCrystal, ResourceKey<IAspect> secondCrystal, AspectCost... costs) {
        return new ElementalToolSpec(category, result, base, firstCrystal, secondCrystal, List.of(costs));
    }

    private static List<ElementalToolSpec> elementalToolSpecs() {
        return List.of(
                elemental(RecipeCategory.TOOLS, enchantedTool(TTItems.ELEMENTAL_AXE.get(), Map.of(InfusionEnchantment.COLLECTOR, 1, InfusionEnchantment.BURROWING, 1)), TTItems.THAUMIUM_AXE.get(),
                        TTAspects.AQUA, TTAspects.AQUA, cost(TTAspects.AQUA, 60), cost(TTAspects.HERBA, 30)),
                elemental(RecipeCategory.TOOLS, enchantedTool(TTItems.ELEMENTAL_PICKAXE.get(), Map.of(InfusionEnchantment.REFINING, 1, InfusionEnchantment.SOUNDING, 2)),
                        TTItems.THAUMIUM_PICKAXE.get(), TTAspects.IGNIS, TTAspects.IGNIS, cost(TTAspects.IGNIS, 30), cost(TTAspects.METALLUM, 30), cost(TTAspects.SENSUS, 30)),
                elemental(RecipeCategory.COMBAT, enchantedTool(TTItems.ELEMENTAL_SWORD.get(), Map.of(InfusionEnchantment.ARCING, 2)), TTItems.THAUMIUM_SWORD.get(), TTAspects.AER, TTAspects.AER,
                        cost(TTAspects.AER, 30), cost(TTAspects.MOTUS, 30), cost(TTAspects.AVERSIO, 30)),
                elemental(RecipeCategory.COMBAT, enchantedTool(TTItems.ELEMENTAL_SPEAR.get(), Map.of(InfusionEnchantment.ESSENCE, 2)), TTItems.THAUMIUM_SPEAR.get(), TTAspects.AER, TTAspects.AER,
                        cost(TTAspects.MOTUS, 60), cost(TTAspects.AER, 30), cost(TTAspects.POTENTIA, 30)),
                elemental(RecipeCategory.TOOLS, enchantedTool(TTItems.ELEMENTAL_SHOVEL.get(), Map.of(InfusionEnchantment.DESTRUCTIVE, 1)), TTItems.THAUMIUM_SHOVEL.get(), TTAspects.TERRA,
                        TTAspects.TERRA, cost(TTAspects.TERRA, 60), cost(TTAspects.FABRICO, 30)),
                elemental(RecipeCategory.TOOLS, new ItemStackTemplate(TTItems.ELEMENTAL_HOE.get()), TTItems.THAUMIUM_HOE.get(), TTAspects.ORDO, TTAspects.PERDITIO, cost(TTAspects.ORDO, 30),
                        cost(TTAspects.HERBA, 30), cost(TTAspects.PERDITIO, 30)));
    }

    private void elementalTool(ElementalToolSpec spec) {
        InfusionRecipeBuilder builder = infusion(spec.category(), spec.result(), single(spec.base()));
        parts(builder, crystal(spec.firstCrystal()), crystal(spec.secondCrystal()), tagged(TTItemTags.NUGGETS_QUARTZ), tagged(TTItemTags.PLANKS_GREATWOOD));
        priced(builder, spec.costs().toArray(AspectCost[]::new));
        commit(builder.instability(ELEMENTAL_TOOL_INSTABILITY).gate(gate("elemental_tools")).unlockedBy("has", has(spec.base())));
    }

    private record AspectCost(ResourceKey<IAspect> aspect, int amount) {
    }

    private void buildRechargePedestalRecipe() {
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.RECHARGE_PEDESTAL), 100), cost(TTAspects.AER, 1), cost(TTAspects.ORDO, 1)), " R ", "DID", "SSS")
                .define('R', TTItems.VIS_RESONATOR).define('D', Tags.Items.GEMS_DIAMOND).define('I', Tags.Items.INGOTS_GOLD).define('S', Tags.Items.STONES).gate(gate("recharge_pedestal"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR)));
    }

    private void buildArmorTrimRecipes() {
        armorTrim(TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get(), TTTrimPatterns.ARCANE, TTBlocks.STONE_ARCANE.get());
        armorTrim(TTItems.OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE.get(), TTTrimPatterns.OCCULT, TTBlocks.STONE_ANCIENT.get());
    }

    private void armorTrim(Item template, ResourceKey<TrimPattern> pattern, ItemLike duplicationBase) {
        Holder.Reference<TrimPattern> trimPattern = registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(pattern);
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(template), tag(TTItemTags.TRIMMABLE_ARMOR), tag(ItemTags.TRIM_MATERIALS), trimPattern, RecipeCategory.MISC)
                .unlocks("has_smithing_trim_template", has(template)).save(output, recipeId("smithing/" + pattern.identifier().getPath() + "_armor_trim"));
        copySmithingTemplate(template, duplicationBase);
    }

    private void buildTravellerBootsRecipe() {
        commit(priced(parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.TRAVELLER_BOOTS.get()), single(Items.LEATHER_BOOTS)), crystal(TTAspects.AER), crystal(TTAspects.AER),
                single(TTItems.FABRIC.get()), single(TTItems.FABRIC.get()), tagged(Tags.Items.FEATHERS), tagged(ItemTags.FISHES)), cost(TTAspects.VOLATUS, 100), cost(TTAspects.MOTUS, 100))
                .instability(1).gate(gate("boots_traveller")).unlockedBy("has", has(Items.LEATHER_BOOTS)));
    }

    private void buildThaumostaticHarnessRecipe() {
        Ingredient airCrystal = crystal(TTAspects.AER);
        Ingredient greatwoodPlanks = tagged(TTItemTags.PLANKS_GREATWOOD);
        Ingredient gold = tagged(Tags.Items.INGOTS_GOLD);
        Ingredient iron = tagged(Tags.Items.INGOTS_IRON);
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.THAUMOSTATIC_HARNESS.get()), single(Items.LEATHER_CHESTPLATE)), airCrystal, airCrystal, greatwoodPlanks,
                        greatwoodPlanks, single(Items.COMPARATOR), gold, gold, iron, iron),
                cost(TTAspects.VOLATUS, 32), cost(TTAspects.POTENTIA, 32), cost(TTAspects.MACHINA, 32), cost(TTAspects.MOTUS, 16)).instability(6).gate(gate("thaumostatic_harness"))
                .unlockedBy("has", has(TTItems.TRAVELLER_BOOTS)));
    }

    private static ItemStackTemplate enchantedTool(Item item, Map<InfusionEnchantment, Integer> enchantments) {
        DataComponentPatch patch = DataComponentPatch.builder().set(TTDataComponents.INFUSION_ENCHANTMENTS.get(), new InfusionEnchantments(enchantments)).build();
        return new ItemStackTemplate(item, patch);
    }

    private record EnchantStep(InfusionEnchantment enchantment, Item displayCatalyst, Ingredient signature, List<AspectCost> costs) {
    }

    private static EnchantStep enchantStep(InfusionEnchantment enchantment, Item displayCatalyst, Ingredient signature, AspectCost... costs) {
        return new EnchantStep(enchantment, displayCatalyst, signature, List.of(costs));
    }

    private List<EnchantStep> enchantSteps() {
        return List.of(enchantStep(InfusionEnchantment.BURROWING, Items.WOODEN_PICKAXE, single(Items.RABBIT_FOOT), cost(TTAspects.SENSUS, 80), cost(TTAspects.TERRA, 150)),
                enchantStep(InfusionEnchantment.COLLECTOR, Items.STONE_AXE, single(Items.LEAD), cost(TTAspects.DESIDERIUM, 80), cost(TTAspects.AQUA, 100)),
                enchantStep(InfusionEnchantment.DESTRUCTIVE, Items.STONE_PICKAXE, single(Items.TNT), cost(TTAspects.AVERSIO, 200), cost(TTAspects.PERDITIO, 250)),
                enchantStep(InfusionEnchantment.REFINING, Items.IRON_PICKAXE, single(TTItems.SALIS_MUNDUS.get()), cost(TTAspects.ORDO, 80), cost(TTAspects.PERMUTATIO, 60)),
                enchantStep(InfusionEnchantment.SOUNDING, Items.GOLDEN_PICKAXE, single(Items.MAP), cost(TTAspects.SENSUS, 40), cost(TTAspects.IGNIS, 60)),
                enchantStep(InfusionEnchantment.ARCING, Items.WOODEN_SWORD, tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE), cost(TTAspects.POTENTIA, 40), cost(TTAspects.AER, 60)),
                enchantStep(InfusionEnchantment.ESSENCE, Items.STONE_SWORD, single(TTItems.ESSENTIA_CRYSTAL.get()), cost(TTAspects.BESTIA, 40), cost(TTAspects.VITIUM, 60)),
                enchantStep(InfusionEnchantment.LAMPLIGHT, Items.GOLDEN_PICKAXE, tagged(TTItemTags.NITORS), cost(TTAspects.LUX, 80), cost(TTAspects.AER, 20)));
    }

    private void buildInfusionEnchantmentRecipes() {
        for (EnchantStep step : enchantSteps()) {
            InfusionEnchantmentRecipeBuilder builder = infusionEnchantment(step.enchantment(), step.displayCatalyst(), step.signature());
            for (AspectCost cost : step.costs()) {
                builder.aspect(cost.aspect(), cost.amount());
            }
            builder.save(output);
        }
    }

    private void buildRunicAugmentRecipe() {
        HolderGetter<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
        AspectList baseAspects = AspectList.of(new AspectInstance(aspects.getOrThrow(TTAspects.PRAEMUNIO), 40), new AspectInstance(aspects.getOrThrow(TTAspects.VITREUS), 20),
                new AspectInstance(aspects.getOrThrow(TTAspects.POTENTIA), 20));
        Ingredient amber = tagged(TTItemTags.GEMS_AMBER);
        InfusionRunicAugmentRecipe recipe = new InfusionRunicAugmentRecipe(List.of(single(TTItems.SALIS_MUNDUS.get()), amber), amber, baseAspects, single(Items.IRON_CHESTPLATE),
                Optional.of(gate("runic_shielding")));
        output.accept(ResourceKey.create(Registries.RECIPE, TTIds.rl("runic_augment/runic_shielding")), recipe, null);
    }

    private InfusionEnchantmentRecipeBuilder infusionEnchantment(InfusionEnchantment enchantment, Item displayCatalyst, Ingredient signature) {
        return new InfusionEnchantmentRecipeBuilder(registries.lookupOrThrow(IAspect.REGISTRY_KEY), enchantment, single(displayCatalyst)).component(single(Items.ENCHANTED_BOOK)).component(signature)
                .gate(gate("infusion_enchantment"));
    }

    private void buildGearRecipes() {
        toolRecipes(TTItemTags.INGOTS_THAUMIUM, TTItems.THAUMIUM_SWORD.get(), TTItems.THAUMIUM_PICKAXE.get(), TTItems.THAUMIUM_AXE.get(), TTItems.THAUMIUM_SHOVEL.get(), TTItems.THAUMIUM_HOE.get(),
                TTItems.THAUMIUM_SPEAR.get());
        toolRecipes(TTItemTags.INGOTS_VOID_METAL, TTItems.VOID_SWORD.get(), TTItems.VOID_PICKAXE.get(), TTItems.VOID_AXE.get(), TTItems.VOID_SHOVEL.get(), TTItems.VOID_HOE.get(),
                TTItems.VOID_SPEAR.get());
        armorRecipes(TTItemTags.INGOTS_THAUMIUM, TTItems.THAUMIUM_HELM.get(), TTItems.THAUMIUM_CHEST.get(), TTItems.THAUMIUM_LEGS.get(), TTItems.THAUMIUM_BOOTS.get());
        armorRecipes(TTItemTags.INGOTS_VOID_METAL, TTItems.VOID_HELM.get(), TTItems.VOID_CHEST.get(), TTItems.VOID_LEGS.get(), TTItems.VOID_BOOTS.get());
        dyeRecipe(TTItems.CLOTH_CHEST.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.CLOTH_LEGS.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.CLOTH_BOOTS.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.VOID_ROBE_HELM.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.VOID_ROBE_CHEST.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.VOID_ROBE_LEGS.get(), ROBE_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PLATE_HELM.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PLATE_CHEST.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PLATE_LEGS.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_RANGER_HELM.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_RANGER_CHEST.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_RANGER_LEGS.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_CAPTAIN_HELM.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_CAPTAIN_CHEST.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_ROBE_HELM.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_ROBE_CHEST.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_ROBE_LEGS.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PRAETOR_HELM.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PRAETOR_CHEST.get(), CRIMSON_DYE_GROUP);
        dyeRecipe(TTItems.CRIMSON_PRAETOR_LEGS.get(), CRIMSON_DYE_GROUP);
    }

    private void dyeRecipe(Item target, String group) {
        CustomCraftingRecipeBuilder
                .customCrafting(RecipeCategory.MISC, (commonInfo, bookInfo) -> new DyeRecipe(commonInfo, bookInfo, single(target), tag(ItemTags.DYES), new ItemStackTemplate(target)))
                .unlockedBy(getHasName(target), has(target)).group(group).save(output, TTIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(target).getPath() + "_dyed");
    }

    private void toolRecipes(TagKey<Item> ingot, Item... tools) {
        gearSet(TOOL_SHAPES, ingot, tools);
    }

    private void armorRecipes(TagKey<Item> ingot, Item... pieces) {
        gearSet(ARMOR_SHAPES, ingot, pieces);
    }

    private void gearSet(List<GearShape> shapes, TagKey<Item> ingot, Item[] pieces) {
        int index = 0;
        for (Item piece : pieces) {
            gearPiece(shapes.get(index++), piece, ingot);
        }
    }

    private void gearPiece(GearShape shape, Item result, TagKey<Item> ingot) {
        ShapedRecipeBuilder builder = shaped(shape.category(), result);
        for (String row : shape.rows()) {
            builder.pattern(row);
        }
        builder.define('I', ingot);
        if (shape.usesRod()) {
            builder.define('S', Tags.Items.RODS_WOODEN);
        }
        builder.unlockedBy("has_ingot", has(ingot)).save(output);
    }

    private record GearShape(RecipeCategory category, boolean usesRod, List<String> rows) {
    }

    private void buildBannerRecipes() {
        for (DyeColor dye : DyeColor.values()) {
            arcaneShaped(new ItemStackTemplate(TTItems.BANNERS.get(dye).get()), 10).pattern("WS").pattern("WS").pattern("WB").define('W', wool(dye)).define('S', Tags.Items.RODS_WOODEN)
                    .define('B', ItemTags.WOODEN_SLABS).unlockedBy("has_wool", has(ItemTags.WOOL)).save(output, TTIds.MODID + ":arcane/banner_" + dye.getName());
        }
    }

    private static Item wool(DyeColor dye) {
        return switch (dye) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
    }

    private static TagKey<Item> dyeTag(DyeColor dye) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + dye.getName()));
    }

    private void buildIngredientRecipes() {
        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.INLAY.get(), 2), 25), cost(TTAspects.AQUA, 1)).requires(Tags.Items.DUSTS_REDSTONE).requires(Tags.Items.INGOTS_GOLD)
                .gate(gate("infusion_stable")).unlockedBy("has", has(Tags.Items.DUSTS_REDSTONE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.PATTERN_CRAFTER), 50), cost(TTAspects.TERRA, 1), cost(TTAspects.AQUA, 1), cost(TTAspects.ORDO, 1)), "VH ", "GCG", " W ")
                .define('H', Items.HOPPER).define('W', TTItemTags.PLANKS_GREATWOOD).define('G', TTItems.MECHANISM_SIMPLE).define('V', TTItems.VIS_RESONATOR)
                .define('C', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES).gate(gate("arcane_pattern_crafter")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(shapeless(RecipeCategory.TOOLS, TTItems.SCRIBING_TOOLS).requires(TTItems.SCRIBING_TOOLS).requires(Tags.Items.DYES_BLACK).unlockedBy("has", has(TTItems.SCRIBING_TOOLS)),
                "thaumaturge:scribing_tools_refill");

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTBlocks.DECONSTRUCTION_TABLE.asItem()), 20), cost(TTAspects.PERDITIO, 1)), " S ", "ATP").define('S', TTItems.THAUMOMETER)
                .define('T', TTBlocks.TABLE_WOOD.asItem()).define('A', Items.GOLDEN_AXE).define('P', Items.GOLDEN_PICKAXE).gate(gate("deconstructor")).unlockedBy("has", has(TTItems.THAUMOMETER)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.POTION_SPRAYER), 75), cost(TTAspects.AQUA, 1), cost(TTAspects.IGNIS, 1)), "BDB", "IAI", "ICI")
                .define('B', TTItemTags.PLATES_BRASS).define('I', TTItemTags.PLATES_IRON).define('A', Items.BREWING_STAND).define('D', Items.DISPENSER).define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("potion_sprayer")).unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SPA), 50), cost(TTAspects.AQUA, 1)), "QIQ", "SJS", "SPS").define('Q', Items.QUARTZ_BLOCK).define('I', Items.IRON_BARS)
                .define('S', TTItems.STONE_ARCANE).define('J', TTItems.JAR_NORMAL).define('P', TTItems.MECHANISM_SIMPLE).gate(gate("arcane_spa")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.FABRIC), 5), " S ", "SCS", " S ").define('S', Tags.Items.STRINGS).define('C', ItemTags.WOOL).gate(gate("unlock_infusion"))
                .unlockedBy("has", has(Tags.Items.STRINGS)));

        clothRecipe(TTItems.CLOTH_CHEST.get(), "I I", "III", "III");
        clothRecipe(TTItems.CLOTH_LEGS.get(), "III", "I I", "I I");
        clothRecipe(TTItems.CLOTH_BOOTS.get(), "I I", "I I");

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MECHANISM_SIMPLE), 10), cost(TTAspects.IGNIS, 1), cost(TTAspects.AQUA, 1)), " B ", "ISI", " B ")
                .define('B', TTItemTags.PLATES_BRASS).define('I', TTItemTags.PLATES_IRON).define('S', Tags.Items.RODS_WOODEN).gate(gate("base_artifice"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MECHANISM_COMPLEX), 50), cost(TTAspects.IGNIS, 1), cost(TTAspects.AQUA, 1)), " M ", "TQT", " M ")
                .define('T', TTItemTags.PLATES_THAUMIUM).define('Q', Items.PISTON).define('M', TTItems.MECHANISM_SIMPLE).gate(gate("base_artifice")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.MIRRORED_GLASS), 50), cost(TTAspects.AQUA, 1), cost(TTAspects.ORDO, 1)).requires(TTItemTags.GEMS_QUICKSILVER)
                .requires(Tags.Items.GLASS_PANES).gate(gate("base_artifice")).unlockedBy("has", has(TTItemTags.GEMS_QUICKSILVER)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.FILTER.get(), 2), 15), cost(TTAspects.AQUA, 1)), "GWG").define('G', Tags.Items.INGOTS_GOLD)
                .define('W', TTItemTags.PLANKS_SILVERWOOD).gate(gate("base_alchemy")).unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MORPHIC_RESONATOR), 50), cost(TTAspects.AER, 1), cost(TTAspects.IGNIS, 1)), " G ", "BSB", " G ")
                .define('G', Tags.Items.GLASS_PANES).define('B', TTItemTags.PLATES_BRASS).define('S', TTItemTags.NUGGETS_QUICKSILVER).gate(gate("base_alchemy"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS)));

        commit(priced(
                crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.BOTTLE_TAINT),
                        DataComponentIngredient.of(TTDataComponents.ASPECTS.get(), AspectList.of(new AspectInstance(getAspect(TTAspects.VITIUM), PhialItem.BASE_AMOUNT)), TTItems.PHIAL.get())),
                cost(TTAspects.VITIUM, 30), cost(TTAspects.AQUA, 30)).gate(gate("bottle_taint")).unlockedBy("has", has(TTItems.PHIAL.get())));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.ETHEREAL_BLOOM), single(TTItems.PLANT_SHIMMERLEAF.get())), cost(TTAspects.LUX, 8), cost(TTAspects.HERBA, 16),
                cost(TTAspects.VICTUS, 16), cost(TTAspects.VITIUM, 16)).gate(gate("ethereal_bloom")).unlockedBy("has", has(TTItems.PLANT_SHIMMERLEAF.get())));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.BATH_SALTS), single(TTItems.SALIS_MUNDUS)), cost(TTAspects.COGNITIO, 40), cost(TTAspects.AER, 40),
                cost(TTAspects.ORDO, 40), cost(TTAspects.VICTUS, 40)).gate(gate("bath_salts")).unlockedBy("has", has(TTItems.SALIS_MUNDUS)));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.SANITY_SOAP), single(Items.ROTTEN_FLESH)), cost(TTAspects.COGNITIO, 75), cost(TTAspects.ALIENIS, 50),
                cost(TTAspects.ORDO, 75), cost(TTAspects.VICTUS, 50)).gate(gate("sane_soap")).unlockedBy("has", has(Items.ROTTEN_FLESH)));

        commit(shapeless(RecipeCategory.FOOD, TTItems.TRIPLE_MEAT_TREAT).requires(TTItemTags.MEAT_CHUNKS).requires(TTItemTags.MEAT_CHUNKS).requires(TTItemTags.MEAT_CHUNKS).requires(Items.SUGAR)
                .unlockedBy("has", has(TTItemTags.MEAT_CHUNKS)));
    }

    private void clothRecipe(Item result, String... rows) {
        ArcaneWorkbenchShapedRecipeBuilder builder = arcaneShaped(new ItemStackTemplate(result), CLOTH_VIS);
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.define('I', TTItems.FABRIC).gate(gate("unlock_infusion")).unlockedBy("has", has(TTItems.FABRIC)).save(output);
    }

    private void buildCrystalClusterRecipes() {
        crystalCluster(TTItems.CRYSTAL_AER, TTAspects.AER, 0);
        crystalCluster(TTItems.CRYSTAL_IGNIS, TTAspects.IGNIS, 0);
        crystalCluster(TTItems.CRYSTAL_AQUA, TTAspects.AQUA, 0);
        crystalCluster(TTItems.CRYSTAL_TERRA, TTAspects.TERRA, 0);
        crystalCluster(TTItems.CRYSTAL_ORDO, TTAspects.ORDO, 0);
        crystalCluster(TTItems.CRYSTAL_PERDITIO, TTAspects.PERDITIO, 0);
        crystalCluster(TTItems.CRYSTAL_VITIUM, TTAspects.VITIUM, 4);

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.ELDRITCH_EYE.get()), single(Items.ENDER_EYE)), single(TTItems.VOID_SEED.get()), tagged(Tags.Items.INGOTS_GOLD)),
                cost(TTAspects.ALIENIS, 64), cost(TTAspects.VACUOS, 16), cost(TTAspects.TENEBRAE, 16), cost(TTAspects.MOTUS, 16)).instability(5).gate(gate("oculus"))
                .unlockedBy("has", has(Items.ENDER_EYE)));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.CAUSALITY_COLLAPSER.get()), single(Items.TNT)), single(TTItems.MORPHIC_RESONATOR.get()),
                tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE), single(TTItems.ALUMENTUM.get()), tagged(TTItemTags.NITORS), single(TTItems.VIS_RESONATOR.get()), tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE),
                single(TTItems.ALUMENTUM.get()), tagged(TTItemTags.NITORS)), cost(TTAspects.ALIENIS, 50), cost(TTAspects.VITIUM, 50)).instability(8).gate(gate("rift_closer"))
                .unlockedBy("has", has(TTItems.MORPHIC_RESONATOR)));
    }

    private void crystalCluster(ItemLike cluster, ResourceKey<IAspect> aspect, int instability) {
        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(cluster.asItem()), crystal(aspect)), tagged(Tags.Items.SEEDS_WHEAT), single(TTItems.SALIS_MUNDUS.get())).aspect(aspect, 10),
                cost(TTAspects.VITREUS, 10), cost(TTAspects.VINCULUM, 5)).instability(instability).gate(gate("crystal_farmer")).unlockedBy("has", has(TTItems.SALIS_MUNDUS)));
    }

    private void buildFocusRecipes() {
        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.FOCUS_1.get()),
                DataComponentIngredient.of(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(getAspect(TTAspects.ORDO), 1), TTItems.ESSENTIA_CRYSTAL.get())).gate(gate("unlock_auromancy")),
                cost(TTAspects.VITREUS, 20), cost(TTAspects.PRAECANTATIO, 10), cost(TTAspects.AURAM, 5)).unlockedBy("has", has(TTItems.ESSENTIA_CRYSTAL.get())));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.FOCUS_2.get()), single(TTItems.FOCUS_1.get())), tagged(TTItemTags.GEMS_QUICKSILVER),
                tagged(Tags.Items.GEMS_DIAMOND), tagged(TTItemTags.GEMS_QUICKSILVER), tagged(Tags.Items.ENDER_PEARLS)), cost(TTAspects.PRAECANTATIO, 25), cost(TTAspects.ORDO, 50)).instability(3)
                .gate(gate("focus_advanced", 0)).unlockedBy("has", has(TTItems.FOCUS_1.get())));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.FOCUS_3.get()), single(TTItems.FOCUS_2.get())), tagged(TTItemTags.GEMS_QUICKSILVER),
                        single(TTItems.PRIMORDIAL_PEARL.get()), tagged(TTItemTags.GEMS_QUICKSILVER), tagged(Tags.Items.NETHER_STARS)),
                cost(TTAspects.PRAECANTATIO, 25), cost(TTAspects.ORDO, 50), cost(TTAspects.VACUOS, 100)).instability(5).gate(gate("focus_greater", 0)).unlockedBy("has", has(TTItems.FOCUS_2.get())));
    }

    private void buildCrucibleRecipes() {
        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.TALLOW.get()), single(Items.ROTTEN_FLESH)), cost(TTAspects.IGNIS, 1)).gate(gate("hedge_alchemy", 0)).unlockedBy("has",
                has(Items.ROTTEN_FLESH)));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.LEATHER), single(Items.ROTTEN_FLESH)), cost(TTAspects.AER, 3), cost(TTAspects.BESTIA, 3)).gate(gate("hedge_alchemy", 0))
                .unlockedBy("has", has(Items.ROTTEN_FLESH)), TTIds.MODID + ":crucible/leather");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.GUNPOWDER, 2), tagged(Tags.Items.GUNPOWDERS)), cost(TTAspects.IGNIS, 10), cost(TTAspects.PERDITIO, 10),
                cost(TTAspects.ALKIMIA, 5)).gate(gate("hedge_alchemy", 1)).unlockedBy("has", has(Tags.Items.GUNPOWDERS)), TTIds.MODID + ":crucible/gunpowder");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.SLIME_BALL, 2), tagged(Tags.Items.SLIME_BALLS)), cost(TTAspects.AQUA, 5), cost(TTAspects.VICTUS, 5),
                cost(TTAspects.ALKIMIA, 1)).gate(gate("hedge_alchemy", 1)).unlockedBy("has", has(Tags.Items.SLIME_BALLS)), TTIds.MODID + ":crucible/slime_ball");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.GLOWSTONE_DUST, 2), tagged(Tags.Items.DUSTS_GLOWSTONE)), cost(TTAspects.SENSUS, 5), cost(TTAspects.LUX, 10))
                .gate(gate("hedge_alchemy", 1)).unlockedBy("has", has(Tags.Items.DUSTS_GLOWSTONE)), TTIds.MODID + ":crucible/glowstone_dust");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.INK_SAC, 2), single(Items.INK_SAC)), cost(TTAspects.AQUA, 2), cost(TTAspects.BESTIA, 2)).gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.INK_SAC)), TTIds.MODID + ":crucible/dye");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.CLAY_BALL), single(Items.DIRT)), cost(TTAspects.AQUA, 5)).gate(gate("hedge_alchemy", 2)).unlockedBy("has",
                has(Items.DIRT)), TTIds.MODID + ":crucible/clay_ball");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.STRING), tagged(Tags.Items.CROPS_WHEAT)), cost(TTAspects.BESTIA, 5), cost(TTAspects.FABRICO, 1))
                .gate(gate("hedge_alchemy", 2)).unlockedBy("has", has(Tags.Items.CROPS_WHEAT)), TTIds.MODID + ":crucible/string");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.COBWEB), tagged(Tags.Items.STRINGS)), cost(TTAspects.VINCULUM, 5)).gate(gate("hedge_alchemy", 2)).unlockedBy("has",
                has(Tags.Items.STRINGS)), TTIds.MODID + ":crucible/cobweb");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.LAVA_BUCKET), tagged(Tags.Items.BUCKETS_EMPTY)), cost(TTAspects.IGNIS, 15), cost(TTAspects.TERRA, 5))
                .gate(gate("hedge_alchemy", 2)).unlockedBy("has", has(Tags.Items.BUCKETS_EMPTY)), TTIds.MODID + ":crucible/lava_bucket");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.MOSSY_COBBLESTONE), single(Items.COBBLESTONE)), cost(TTAspects.HERBA, 2)).gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.COBBLESTONE)), TTIds.MODID + ":crucible/mossy_cobblestone");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.MOSSY_STONE_BRICKS), single(Items.STONE_BRICKS)), cost(TTAspects.HERBA, 2)).gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.STONE_BRICKS)), TTIds.MODID + ":crucible/mossy_stone_bricks");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.ICE), single(Items.SNOW_BLOCK)), cost(TTAspects.ORDO, 1), cost(TTAspects.GELUM, 1)).gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.SNOW_BLOCK)), TTIds.MODID + ":crucible/ice");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.BONE_MEAL, 4), single(Items.BONE)), cost(TTAspects.PERDITIO, 1)).gate(gate("hedge_alchemy", 2)).unlockedBy("has",
                has(Items.BONE)), TTIds.MODID + ":crucible/bone_meal");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.BUCKET_LIQUID_DEATH.get()), tagged(Tags.Items.BUCKETS_EMPTY)), cost(TTAspects.MORTUUS, 100),
                cost(TTAspects.PERDITIO, 50), cost(TTAspects.ALKIMIA, 20)).gate(gate("liquid_death", 0)).unlockedBy("has", has(Tags.Items.BUCKETS_EMPTY)), TTIds.MODID + ":crucible/liquid_death");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.INGOT_BRASS.get()), tagged(Tags.Items.INGOTS_COPPER)), cost(TTAspects.INSTRUMENTUM, 5)).gate(gate("metallurgy", 0))
                .unlockedBy("has", has(Tags.Items.INGOTS_COPPER)));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.INGOT_THAUMIUM.get()), tagged(Tags.Items.INGOTS_IRON)), cost(TTAspects.PRAECANTATIO, 5), cost(TTAspects.TERRA, 5))
                .gate(gate("metallurgy", 1)).unlockedBy("has", has(Tags.Items.INGOTS_IRON)));

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.NITORS.get(DyeColor.YELLOW).get()), tagged(Tags.Items.DUSTS_GLOWSTONE)).gate(gate("unlock_alchemy", 2)),
                cost(TTAspects.POTENTIA, 10), cost(TTAspects.IGNIS, 10), cost(TTAspects.LUX, 10)).unlockedBy("has", has(Tags.Items.DUSTS_GLOWSTONE)));

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(this::visCrystalRecipe);

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.INGOT_VOID.get()), single(TTItems.VOID_SEED.get())).gate(gate("base_eldritch")), cost(TTAspects.METALLUM, 10),
                cost(TTAspects.VITIUM, 5)).unlockedBy("has", has(TTItems.VOID_SEED.get())), TTIds.MODID + ":crucible/void_ingot");

        clusterRecipe(TTItems.CLUSTER_IRON, Tags.Items.RAW_MATERIALS_IRON);
        clusterRecipe(TTItems.CLUSTER_GOLD, Tags.Items.RAW_MATERIALS_GOLD);
        clusterRecipe(TTItems.CLUSTER_COPPER, Tags.Items.RAW_MATERIALS_COPPER);
        clusterRecipe(TTItems.CLUSTER_TIN, TTItemTags.ORES_TIN);
        clusterRecipe(TTItems.CLUSTER_SILVER, TTItemTags.ORES_SILVER);
        clusterRecipe(TTItems.CLUSTER_LEAD, TTItemTags.ORES_LEAD);
        clusterRecipe(TTItems.CLUSTER_CINNABAR, TTItemTags.RAW_MATERIALS_CINNABAR);
        clusterRecipe(TTItems.CLUSTER_QUARTZ, Tags.Items.ORES_QUARTZ);

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.IRON_NUGGET, 3), tagged(Tags.Items.NUGGETS_IRON)), cost(TTAspects.METALLUM, 2)).gate(gate("metal_purification"))
                .unlockedBy("has", has(Tags.Items.NUGGETS_IRON)), TTIds.MODID + ":crucible/iron_nugget_transmutation");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(Items.GOLD_NUGGET, 3), tagged(Tags.Items.NUGGETS_GOLD)), cost(TTAspects.METALLUM, 2), cost(TTAspects.DESIDERIUM, 1))
                .gate(gate("metal_purification")).unlockedBy("has", has(Tags.Items.NUGGETS_GOLD)), TTIds.MODID + ":crucible/gold_nugget_transmutation");

        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.ALUMENTUM), tagged(ItemTags.COALS)).gate(gate("alumentum")), cost(TTAspects.IGNIS, 10), cost(TTAspects.POTENTIA, 10),
                cost(TTAspects.PERDITIO, 5)).unlockedBy("has", has(ItemTags.COALS)));
    }

    private void visCrystalRecipe(Holder.Reference<IAspect> aspect) {
        DataComponentPatch crystalPatch = DataComponentPatch.builder().set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)).build();
        CrucibleRecipeBuilder builder = crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.ESSENTIA_CRYSTAL, 1, crystalPatch), tagged(TTItemTags.NUGGETS_QUARTZ));
        builder.gate(gate("base_alchemy")).aspect(aspect, 2);
        commit(builder.unlockedBy("has", has(TTItemTags.NUGGETS_QUARTZ)), TTIds.MODID + ":crucible/vis_crystal/" + aspect.getKey().identifier().getPath());
    }

    private void clusterRecipe(ItemLike cluster, TagKey<Item> oreTag) {
        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(cluster.asItem()), tagged(oreTag)), cost(TTAspects.METALLUM, 5), cost(TTAspects.ORDO, 5)).gate(gate("metal_purification"))
                .unlockedBy("has", has(oreTag)), whenTagPresent(oreTag));
    }

    private void buildArcaneWorkbenchRecipes() {
        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.THAUMOMETER), 20).allAspects(), " G ", "GPG", " G ").define('G', Tags.Items.INGOTS_GOLD).define('P', Tags.Items.GLASS_PANES)
                .gate(gate("first_steps", 1)).unlockedBy("has", has(Tags.Items.INGOTS_GOLD)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.VIS_RESONATOR), 50), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1)).requires(TTItemTags.PLATES_IRON)
                .requires(Tags.Items.GEMS_QUARTZ).gate(gate("unlock_auromancy", 1)).unlockedBy("has", has(Tags.Items.GEMS_QUARTZ)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_WORKBENCH_CHARGER), 200), cost(TTAspects.AER, 2), cost(TTAspects.ORDO, 2)), " R ", "P P", "I I")
                .define('R', TTItems.VIS_RESONATOR).define('P', TTItemTags.PLANKS_GREATWOOD).define('I', Tags.Items.INGOTS_IRON).gate(gate("workbench_charger"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.GOGGLES_REVEALING), 50), "LBL", "L L", "MBM").define('L', Tags.Items.LEATHERS).define('B', TTItemTags.INGOTS_BRASS)
                .define('M', TTItems.THAUMOMETER).gate(gate("unlock_artifice")).unlockedBy("has", has(TTItems.THAUMOMETER)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ALEMBIC), 50), cost(TTAspects.AQUA, 1)), "GFG", "PBP", "GFG").define('G', TTItemTags.PLANKS_GREATWOOD).define('F', TTItems.FILTER)
                .define('P', TTItemTags.PLATES_BRASS).define('B', Tags.Items.BUCKETS_EMPTY).gate(gate("essentia_smelter")).unlockedBy("has", has(TTItemTags.PLATES_BRASS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SMELTER_BASIC), 50), cost(TTAspects.IGNIS, 1)), "PRP", "CFC", "CCC").define('C', ItemTags.STONE_TOOL_MATERIALS)
                .define('F', Tags.Items.PLAYER_WORKSTATIONS_FURNACES).define('P', TTItemTags.PLATES_BRASS).define('R', TTItems.CRUCIBLE).gate(gate("essentia_smelter", 1))
                .unlockedBy("has", has(TTItems.CRUCIBLE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ESSENTIA_CRYSTALIZER), 125), cost(TTAspects.AQUA, 1), cost(TTAspects.TERRA, 3), cost(TTAspects.ORDO, 1)), "IDI", "QCQ", "WTW")
                .define('I', Tags.Items.INGOTS_IRON).define('D', Items.DIAMOND_BLOCK).define('Q', TTItems.SALIS_MUNDUS).define('C', TTItems.ALCHEMICAL_CONSTRUCT).define('W', ItemTags.PLANKS)
                .define('T', TTItems.TUBE).gate(gate("essentia_crystalizer")).unlockedBy("has", has(TTItems.SALIS_MUNDUS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SMELTER_THAUMIUM), 250), cost(TTAspects.IGNIS, 2)), "PRP", "CFC", "CCC").define('C', TTItemTags.PLATES_THAUMIUM)
                .define('F', TTItems.ALCHEMICAL_CONSTRUCT).define('P', TTItemTags.PLATES_BRASS).define('R', TTItems.SMELTER_BASIC).gate(gate("essentia_smelter_thaumium"))
                .unlockedBy("has", has(TTItems.SMELTER_BASIC)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SMELTER_VOID), 750), cost(TTAspects.IGNIS, 3)), "PRP", "CFC", "CCC").define('C', TTItemTags.PLATES_VOID_METAL)
                .define('F', TTItems.ADVANCED_ALCHEMICAL_CONSTRUCT).define('P', TTItemTags.PLATES_BRASS).define('R', TTItems.SMELTER_THAUMIUM).gate(gate("essentia_smelter_void"))
                .unlockedBy("has", has(TTItems.SMELTER_THAUMIUM)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.JAR_NORMAL), 5), "PRP", "P P", "PPP").define('P', Tags.Items.GLASS_PANES).define('R', ItemTags.WOODEN_SLABS).gate(gate("warded_jars"))
                .unlockedBy("has", has(Tags.Items.GLASS_PANES)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.JAR_VOID), 50), cost(TTAspects.PERDITIO, 1)).requires(TTItems.JAR_NORMAL).gate(gate("warded_jars")).unlockedBy("has",
                has(TTItems.JAR_NORMAL)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.TUBE, 8), 10), " Q ", "PGP", " B ").define('Q', TTItemTags.NUGGETS_QUICKSILVER).define('P', TTItemTags.PLATES_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS).define('B', TTItemTags.NUGGETS_BRASS).gate(gate("tubes")).unlockedBy("has", has(Tags.Items.GEMS_QUARTZ)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.TUBE_RESTRICT), 10), cost(TTAspects.TERRA, 1)).requires(TTItems.TUBE).requires(Tags.Items.STONES).gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.TUBE_ONEWAY), 10), cost(TTAspects.AQUA, 1)).requires(TTItems.TUBE).requires(Tags.Items.DYES_BLUE).gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE)));

        commit(arcaneShapeless(new ItemStackTemplate(TTItems.TUBE_FILTER), 10).requires(TTItems.TUBE).requires(TTItems.FILTER).gate(gate("tubes")).unlockedBy("has", has(TTItems.TUBE)));

        commit(arcaneShapeless(new ItemStackTemplate(TTItems.TUBE_VALVE), 10).requires(TTItems.TUBE).requires(Items.LEVER).gate(gate("tubes")).unlockedBy("has", has(TTItems.TUBE)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.TUBE_BUFFER), 25), "PVP", "TIT", "PRP").define('P', TTItems.PHIAL).define('V', TTItems.TUBE_VALVE).define('T', TTItems.TUBE)
                .define('I', TTItemTags.PLATES_IRON).define('R', TTItems.TUBE_RESTRICT).gate(gate("tubes")).unlockedBy("has", has(TTItems.TUBE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SMELTER_AUX), 100), cost(TTAspects.AER, 1), cost(TTAspects.TERRA, 1)), "PVP", "BCB", "ILI")
                .define('P', TTItemTags.PLANKS_GREATWOOD).define('V', TTItems.TUBE_FILTER).define('B', TTItemTags.PLATES_BRASS).define('I', TTItemTags.PLATES_IRON)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT).define('L', TTItems.BELLOWS).gate(gate("improved_smelting")).unlockedBy("has", has(TTItems.BELLOWS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SMELTER_VENT), 150), cost(TTAspects.AER, 1)), "IBI", "FCF", "IBI").define('F', TTItems.FILTER)
                .define('B', TTItemTags.PLATES_BRASS).define('I', TTItemTags.PLATES_IRON).define('C', TTItems.ALCHEMICAL_CONSTRUCT).gate(gate("improved_smelting_2"))
                .unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ALCHEMICAL_CONSTRUCT, 2), 75), cost(TTAspects.AQUA, 1), cost(TTAspects.PERDITIO, 1), cost(TTAspects.ORDO, 1)), "IAI", "VPV",
                "IAI").define('A', TTItems.TUBE_VALVE).define('V', TTItems.TUBE).define('I', TTItemTags.PLATES_IRON).define('P', TTItemTags.PLANKS_GREATWOOD).gate(gate("tubes"))
                .unlockedBy("has", has(TTItemTags.PLATES_IRON)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ADVANCED_ALCHEMICAL_CONSTRUCT, 4), 50), cost(TTAspects.AQUA, 10), cost(TTAspects.ORDO, 30), cost(TTAspects.TERRA, 10)), "VAV",
                "APA", "VAV").define('A', TTItems.ALCHEMICAL_CONSTRUCT).define('V', TTItems.INGOT_VOID).define('P', TTItems.PRIMORDIAL_PEARL).gate(gate("essentia_smelter_void", 0))
                .unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.BELLOWS), 25), cost(TTAspects.AER, 1)), "PP ", "LLI", "PP ").define('P', ItemTags.PLANKS).define('L', Tags.Items.LEATHERS)
                .define('I', Tags.Items.INGOTS_IRON).gate(gate("bellows")).unlockedBy("has", has(Tags.Items.LEATHERS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.FLUX_SCRUBBER), 200), cost(TTAspects.AQUA, 2), cost(TTAspects.ORDO, 2), cost(TTAspects.AER, 1)), " B ", "GOG", "STS")
                .define('B', TTItems.BELLOWS).define('G', Items.IRON_BARS).define('O', TTItems.FILTER).define('S', TTItems.STONE_ARCANE_BRICK).define('T', TTItems.TUBE).gate(gate("flux_scrubber"))
                .unlockedBy("has", has(TTItems.BELLOWS)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.THAUMONOMICON_SHARING), 500).allAspects(), " B ", "MQM", " B ").define('B', TTItems.BRAIN).define('M', TTItems.MIRROR)
                .define('Q', Items.WRITABLE_BOOK).gate(gate("share_book", 1)).unlockedBy("has", has(TTItems.BRAIN)));
    }

    private InfusionRecipeBuilder infusion(RecipeCategory category, ItemStackTemplate result, Ingredient catalyst) {
        return new InfusionRecipeBuilder(registries.lookupOrThrow(IAspect.REGISTRY_KEY), category, result, catalyst);
    }

    private CrucibleRecipeBuilder crucible(RecipeCategory category, ItemStackTemplate result, Ingredient catalyst) {
        return new CrucibleRecipeBuilder(registries.lookupOrThrow(IAspect.REGISTRY_KEY), category, result, catalyst);
    }

    private Holder<IAspect> getAspect(ResourceKey<IAspect> key) {
        return registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private void buildGolemancyRecipes() {
        commit(rows(shaped(RecipeCategory.TOOLS, TTItems.GOLEM_BELL.get()), " QQ", " QQ", "S  ").define('S', Tags.Items.RODS_WOODEN).define('Q', Tags.Items.GEMS_QUARTZ).unlockedBy("has",
                has(Tags.Items.GEMS_QUARTZ)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_TOP_HAT), 16), cost(TTAspects.ORDO, 1), cost(TTAspects.IGNIS, 1)), " C ", " G ", "CCC").define('C', Items.BLACK_WOOL)
                .define('G', Tags.Items.INGOTS_GOLD).gate(gate("golem_accessories")).unlockedBy("has", has(ItemTags.WOOL)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_FEZ), 8), cost(TTAspects.AQUA, 1), cost(TTAspects.TERRA, 1)), "CCS", "CCS", "  S").define('C', Items.RED_WOOL)
                .define('S', Tags.Items.STRINGS).gate(gate("golem_accessories")).unlockedBy("has", has(ItemTags.WOOL)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_BOWTIE), 8), cost(TTAspects.AER, 1), cost(TTAspects.ORDO, 1)), "CSC", "C C").define('C', Items.BLACK_WOOL)
                .define('S', Tags.Items.STRINGS).gate(gate("golem_accessories")).unlockedBy("has", has(ItemTags.WOOL)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_GLASSES), 8), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1)), "GIG").define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Tags.Items.INGOTS_IRON).gate(gate("golem_accessories")).unlockedBy("has", has(Tags.Items.INGOTS_IRON)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_VISOR), 8), cost(TTAspects.TERRA, 1), cost(TTAspects.AQUA, 1)), "IHI").define('I', Tags.Items.INGOTS_IRON)
                .define('H', Items.IRON_HELMET).gate(gate("golem_accessories")).unlockedBy("has", has(Tags.Items.INGOTS_IRON)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MIND_CLOCKWORK), 25), cost(TTAspects.IGNIS, 1), cost(TTAspects.ORDO, 1)), " P ", "PGP", "BCB")
                .define('G', TTItems.MECHANISM_SIMPLE).define('B', TTItemTags.PLATES_BRASS).define('P', Tags.Items.GLASS_PANES).define('C', Items.COMPARATOR).gate(gate("mind_clockwork", 1))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.GOLEM_FETTER), 10), cost(TTAspects.TERRA, 5), cost(TTAspects.ORDO, 5)), "SSS", "IRI", "BBB").define('S', TTItems.STONE_ARCANE)
                .define('I', Tags.Items.INGOTS_IRON).define('R', Items.BEACON).define('B', TTItems.STONE_ARCANE_BRICK).gate(gate("golem_fetter")).unlockedBy("has", has(Items.BEACON)));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.MIND_BIOTHAUMIC), single(TTItems.MIND_CLOCKWORK.get())), single(TTItems.BRAIN.get()),
                single(TTItems.MECHANISM_COMPLEX.get())), cost(TTAspects.COGNITIO, 50), cost(TTAspects.MACHINA, 25)).instability(4).gate(gate("mind_biothaumic"))
                .unlockedBy("has", has(TTItems.MIND_CLOCKWORK)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MODULE_VISION), 50), cost(TTAspects.AQUA, 1)), "B B", "E E", "PGP").define('B', Items.GLASS_BOTTLE)
                .define('E', Items.FERMENTED_SPIDER_EYE).define('P', TTItemTags.PLATES_BRASS).define('G', TTItems.MECHANISM_SIMPLE).gate(gate("golem_vision"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MODULE_AGGRESSION), 50), cost(TTAspects.IGNIS, 1)), " R ", "RTR", "PGP").define('R', Tags.Items.GLASS_PANES)
                .define('T', Items.BLAZE_POWDER).define('P', TTItemTags.PLATES_BRASS).define('G', TTItems.MECHANISM_SIMPLE).gate(gate("seal_guard")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.LEVITATOR), 35), cost(TTAspects.AER, 1)), "WIW", "BNB", "WGW").define('I', TTItemTags.PLATES_THAUMIUM)
                .define('N', TTItemTags.NITORS).define('W', ItemTags.PLANKS).define('B', TTItemTags.PLATES_IRON).define('G', TTItems.MECHANISM_SIMPLE).gate(gate("levitator"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(priced(arcaneShapeless(new ItemStackTemplate(TTItems.SEAL_BLANK.get(), 3), 20), cost(TTAspects.AER, 1)).requires(Items.CLAY_BALL).requires(TTItems.TALLOW.get())
                .requires(Tags.Items.DYES_RED).requires(TTItemTags.NITORS).gate(gate("control_seals")).unlockedBy("has", has(TTItems.TALLOW)));

        for (SealStep step : sealSteps()) {
            sealCrucible(step);
        }

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.SEAL_HARVEST), single(TTItems.SEAL_BLANK.get())), tagged(Tags.Items.SEEDS_WHEAT), tagged(Tags.Items.SEEDS_PUMPKIN),
                        tagged(Tags.Items.SEEDS_MELON), tagged(Tags.Items.SEEDS_BEETROOT), tagged(Tags.Items.CROPS_SUGAR_CANE), tagged(Tags.Items.CROPS_CACTUS)),
                cost(TTAspects.HERBA, 10), cost(TTAspects.SENSUS, 10), cost(TTAspects.HUMANUS, 10)).instability(0).gate(gate("seal_harvest")).unlockedBy("has", has(TTItems.SEAL_BLANK)));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.SEAL_BUTCHER), single(TTItems.SEAL_GUARD.get())), tagged(Tags.Items.LEATHERS), tag(ItemTags.WOOL),
                        single(Items.RABBIT_HIDE), single(Items.PORKCHOP), single(Items.MUTTON), single(Items.BEEF)),
                cost(TTAspects.BESTIA, 10), cost(TTAspects.SENSUS, 10), cost(TTAspects.HUMANUS, 10)).instability(0).gate(gate("seal_butcher")).unlockedBy("has", has(TTItems.SEAL_GUARD)));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.SEAL_BREAKER), single(TTItems.SEAL_BLANK.get())), single(Items.GOLDEN_AXE), single(Items.GOLDEN_PICKAXE),
                single(Items.GOLDEN_SHOVEL)), cost(TTAspects.INSTRUMENTUM, 10), cost(TTAspects.PERDITIO, 10), cost(TTAspects.HUMANUS, 10)).instability(1).gate(gate("seal_break"))
                .unlockedBy("has", has(TTItems.SEAL_BLANK)));
    }

    private record SealStep(String research, DeferredItem<ItemSealPlacer> result, DeferredItem<ItemSealPlacer> catalyst, List<AspectCost> costs) {
    }

    private static SealStep seal(String research, DeferredItem<ItemSealPlacer> result, DeferredItem<ItemSealPlacer> catalyst, AspectCost... costs) {
        return new SealStep(research, result, catalyst, List.of(costs));
    }

    private static List<SealStep> sealSteps() {
        return List.of(seal("seal_collect", TTItems.SEAL_PICKUP, TTItems.SEAL_BLANK, cost(TTAspects.DESIDERIUM, 10)),
                seal("seal_collect", TTItems.SEAL_PICKUP_ADVANCED, TTItems.SEAL_PICKUP, cost(TTAspects.SENSUS, 10), cost(TTAspects.COGNITIO, 10)),
                seal("seal_store", TTItems.SEAL_FILL, TTItems.SEAL_BLANK, cost(TTAspects.AVERSIO, 10)),
                seal("seal_store", TTItems.SEAL_FILL_ADVANCED, TTItems.SEAL_FILL, cost(TTAspects.SENSUS, 10), cost(TTAspects.COGNITIO, 10)),
                seal("seal_empty", TTItems.SEAL_EMPTY, TTItems.SEAL_BLANK, cost(TTAspects.VACUOS, 10)),
                seal("seal_empty", TTItems.SEAL_EMPTY_ADVANCED, TTItems.SEAL_EMPTY, cost(TTAspects.SENSUS, 10), cost(TTAspects.COGNITIO, 10)),
                seal("seal_provide", TTItems.SEAL_PROVIDER, TTItems.SEAL_EMPTY_ADVANCED, cost(TTAspects.PERMUTATIO, 10), cost(TTAspects.DESIDERIUM, 10)),
                seal("seal_stock", TTItems.SEAL_STOCK, TTItems.SEAL_FILL, cost(TTAspects.COGNITIO, 10), cost(TTAspects.DESIDERIUM, 10)),
                seal("seal_guard", TTItems.SEAL_GUARD, TTItems.SEAL_BLANK, cost(TTAspects.AVERSIO, 20), cost(TTAspects.PRAEMUNIO, 20)),
                seal("seal_guard", TTItems.SEAL_GUARD_ADVANCED, TTItems.SEAL_GUARD, cost(TTAspects.SENSUS, 20), cost(TTAspects.COGNITIO, 20)),
                seal("seal_lumber", TTItems.SEAL_LUMBER, TTItems.SEAL_BREAKER, cost(TTAspects.HERBA, 40), cost(TTAspects.SENSUS, 20)),
                seal("seal_use", TTItems.SEAL_USE, TTItems.SEAL_BLANK, cost(TTAspects.FABRICO, 20), cost(TTAspects.SENSUS, 10), cost(TTAspects.COGNITIO, 20)),
                seal("seal_break", TTItems.SEAL_BREAKER_ADVANCED, TTItems.SEAL_BREAKER, cost(TTAspects.SENSUS, 10), cost(TTAspects.COGNITIO, 10), cost(TTAspects.INSTRUMENTUM, 20)));
    }

    private void sealCrucible(SealStep step) {
        CrucibleRecipeBuilder base = crucible(RecipeCategory.MISC, new ItemStackTemplate(step.result()), single(step.catalyst().get())).gate(gate(step.research()));
        commit(priced(base, step.costs().toArray(AspectCost[]::new)).unlockedBy("has", has(step.catalyst())));
    }

    private void buildAuraDeviceRecipes() {

        commit(priced(parts(infusion(RecipeCategory.DECORATIONS, new ItemStackTemplate(TTItems.MIRROR), single(TTItems.MIRRORED_GLASS.get())), tagged(Tags.Items.INGOTS_GOLD),
                tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.ENDER_PEARLS)), cost(TTAspects.MOTUS, 25), cost(TTAspects.TENEBRAE, 25),
                cost(TTAspects.PERMUTATIO, 25)).instability(1).gate(gate("mirror")).unlockedBy("has", has(TTItems.MIRRORED_GLASS)));

        commit(priced(parts(infusion(RecipeCategory.TOOLS, new ItemStackTemplate(TTItems.HAND_MIRROR), single(TTItems.MIRROR.get())), tagged(Tags.Items.RODS_WOODEN), single(Items.COMPASS),
                single(Items.MAP)), cost(TTAspects.INSTRUMENTUM, 50), cost(TTAspects.MOTUS, 50)).instability(5).gate(gate("mirror_hand")).unlockedBy("has", has(TTItems.MIRROR)));

        commit(priced(
                parts(infusion(RecipeCategory.DECORATIONS, new ItemStackTemplate(TTItems.MIRROR_ESSENTIA), single(TTItems.MIRRORED_GLASS.get())), tagged(Tags.Items.INGOTS_IRON),
                        tagged(Tags.Items.INGOTS_IRON), tagged(Tags.Items.INGOTS_IRON), tagged(Tags.Items.ENDER_PEARLS)),
                cost(TTAspects.MOTUS, 25), cost(TTAspects.AQUA, 25), cost(TTAspects.PERMUTATIO, 25)).instability(2).gate(gate("mirror_essentia")).unlockedBy("has", has(TTItems.MIRRORED_GLASS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.THAUMIUM_SHIELD), 60), cost(TTAspects.TERRA, 2), cost(TTAspects.ORDO, 1)), "GGG", "TTT", " G ")
                .define('G', TTItemTags.PLANKS_GREATWOOD).define('T', TTItemTags.PLATES_THAUMIUM).gate(gate("thaumium_shield")).unlockedBy("has", has(TTItemTags.PLATES_THAUMIUM)));

        commit(priced(parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.MIRROR_SHIELD), single(Items.SHIELD)), single(TTItems.MIRRORED_GLASS.get()), tagged(Tags.Items.INGOTS_GOLD),
                tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.INGOTS_GOLD)), cost(TTAspects.PRAEMUNIO, 30), cost(TTAspects.PERMUTATIO, 25), cost(TTAspects.MOTUS, 25)).instability(5)
                .gate(gate("mirror_shield")).unlockedBy("has", has(TTItems.MIRRORED_GLASS)));

        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.VOID_SHIELD), single(TTItems.THAUMIUM_SHIELD.get())), tagged(TTItemTags.PLATES_VOID_METAL),
                        tagged(TTItemTags.PLANKS_SILVERWOOD), tagged(TTItemTags.PLATES_VOID_METAL), tagged(TTItemTags.PLATES_VOID_METAL)),
                cost(TTAspects.ALIENIS, 25), cost(TTAspects.METALLUM, 25), cost(TTAspects.PRAEMUNIO, 35), cost(TTAspects.VACUOS, 25)).instability(8).gate(gate("void_shield"))
                .unlockedBy("has", has(TTItems.THAUMIUM_SHIELD)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MATRIX_SPEED), 500), cost(TTAspects.AER, 1), cost(TTAspects.ORDO, 1)), "SNS", "NGN", "SNS").define('S', TTItems.STONE_ARCANE)
                .define('N', TTItemTags.NITORS).define('G', Tags.Items.STORAGE_BLOCKS_DIAMOND).gate(gate("infusion_boost")).unlockedBy("has", has(TTItems.STONE_ARCANE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.MATRIX_COST), 500), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1), cost(TTAspects.PERDITIO, 1)), "SAS", "AGA", "SAS")
                .define('S', TTItems.STONE_ARCANE).define('A', TTItems.ALUMENTUM).define('G', Tags.Items.STORAGE_BLOCKS_DIAMOND).gate(gate("infusion_boost"))
                .unlockedBy("has", has(TTItems.STONE_ARCANE)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.DIOPTRA), 50), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1)), "APA", "IGI", "AAA").define('A', TTItems.STONE_ARCANE)
                .define('P', TTItems.VIS_RESONATOR).define('G', TTItems.THAUMOMETER).define('I', TTItemTags.PLATES_IRON).gate(gate("dioptra")).unlockedBy("has", has(TTItems.THAUMOMETER)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.VIS_BATTERY), 50), cost(TTAspects.AER, 2), cost(TTAspects.TERRA, 2), cost(TTAspects.AQUA, 2), cost(TTAspects.IGNIS, 2),
                cost(TTAspects.ORDO, 2), cost(TTAspects.PERDITIO, 2)), "SSS", "SRS", "SSS").define('S', TTItems.SLAB_ARCANE_STONE).define('R', TTItems.VIS_RESONATOR).gate(gate("vis_battery"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR)));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.JAR_BRAIN), single(TTItems.JAR_NORMAL.get())), single(TTItems.BRAIN.get()), single(Items.SPIDER_EYE),
                tagged(Tags.Items.BUCKETS_WATER), single(Items.SPIDER_EYE)), cost(TTAspects.COGNITIO, 25), cost(TTAspects.SENSUS, 25), cost(TTAspects.EXANIMIS, 25)).instability(4)
                .gate(gate("jar_brain")).unlockedBy("has", has(TTItems.JAR_NORMAL.get())));
    }

    private void buildNoiseDeviceRecipes() {

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.LAMP_ARCANE), 50), cost(TTAspects.AER, 1), cost(TTAspects.IGNIS, 1)), " I ", "IAI", " I ")
                .define('A', TTItemTags.STORAGE_BLOCKS_AMBER).define('I', TTItemTags.PLATES_IRON).gate(gate("arcane_lamp")).unlockedBy("has", has(TTItemTags.PLATES_IRON)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ARCANE_EAR), 15), cost(TTAspects.AER, 1)), "P P", " G ", "WRW").define('W', ItemTags.WOODEN_SLABS)
                .define('R', Tags.Items.DUSTS_REDSTONE).define('G', TTItems.MECHANISM_SIMPLE).define('P', TTItemTags.PLATES_BRASS).gate(gate("arcane_ear"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS)));

        commit(arcaneShapeless(new ItemStackTemplate(TTItems.ARCANE_EAR_TOGGLE), 5).requires(TTItems.ARCANE_EAR.get()).requires(Items.LEVER).gate(gate("arcane_ear")).unlockedBy("has",
                has(TTItems.ARCANE_EAR.get())), TTIds.MODID + ":arcane_ear_toggle");

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.HUNGRY_CHEST), 15), cost(TTAspects.TERRA, 1), cost(TTAspects.AQUA, 1)), "WTW", "W W", "WWW")
                .define('W', TTItemTags.PLANKS_GREATWOOD).define('T', ItemTags.WOODEN_TRAPDOORS).gate(gate("hungry_chest")).unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD)));

        commit(rows(shaped(RecipeCategory.REDSTONE, TTItems.ITEM_GRATE), "#", "H").define('#', Items.IRON_BARS).define('H', Items.HOPPER).unlockedBy("has", has(Items.HOPPER)));
        commit(rows(shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.TALLOW_BLOCK), "TTT", "TTT", "TTT").define('T', TTItems.TALLOW).unlockedBy("has", has(TTItems.TALLOW)));
        commit(shapeless(RecipeCategory.MISC, TTItems.TALLOW, 9).requires(TTItems.TALLOW_BLOCK).unlockedBy("has", has(TTItems.TALLOW_BLOCK)), TTIds.MODID + ":tallow_from_tallow_block");

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CENTRIFUGE), 100), cost(TTAspects.ORDO, 1), cost(TTAspects.PERDITIO, 1)), " T ", "RCP", " T ").define('T', TTItems.TUBE)
                .define('P', TTItems.MECHANISM_SIMPLE).define('R', TTItems.MORPHIC_RESONATOR).define('C', TTItems.ALCHEMICAL_CONSTRUCT).gate(gate("centrifuge"))
                .unlockedBy("has", has(TTItems.MORPHIC_RESONATOR)));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.LAMP_GROWTH), single(TTItems.LAMP_ARCANE.get())), tagged(Tags.Items.INGOTS_GOLD), single(Items.BONE_MEAL),
                        crystal(TTAspects.TERRA), tagged(Tags.Items.INGOTS_GOLD), single(Items.BONE_MEAL), crystal(TTAspects.TERRA)),
                cost(TTAspects.HERBA, 20), cost(TTAspects.LUX, 15), cost(TTAspects.VICTUS, 15), cost(TTAspects.INSTRUMENTUM, 15)).instability(4).gate(gate("lamp_growth"))
                .unlockedBy("has", has(TTItems.LAMP_ARCANE.get())));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.LAMP_FERTILITY), single(TTItems.LAMP_ARCANE.get())), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.CROPS_WHEAT),
                        crystal(TTAspects.IGNIS), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.CROPS_CARROT), crystal(TTAspects.IGNIS)),
                cost(TTAspects.BESTIA, 20), cost(TTAspects.LUX, 15), cost(TTAspects.VICTUS, 15), cost(TTAspects.DESIDERIUM, 15)).instability(4).gate(gate("lamp_fertility"))
                .unlockedBy("has", has(TTItems.LAMP_ARCANE.get())));
    }

    private void buildEssentiaMachineRecipes() {
        commit(priced(crucible(RecipeCategory.MISC, new ItemStackTemplate(TTItems.EVERFULL_URN), single(Items.DECORATED_POT)), cost(TTAspects.AQUA, 30), cost(TTAspects.FABRICO, 10),
                cost(TTAspects.TERRA, 10)).gate(gate("everfull_urn")).unlockedBy("has", has(Items.DECORATED_POT)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.VIS_GENERATOR), 25), cost(TTAspects.IGNIS, 1), cost(TTAspects.ORDO, 1)), "WSW", "EPE", "WRW").define('R', TTItems.VIS_RESONATOR)
                .define('E', TTItemTags.NUGGETS_BRASS).define('S', Tags.Items.DUSTS_REDSTONE).define('P', Items.PISTON).define('W', ItemTags.PLANKS).gate(gate("vis_generator"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ESSENTIA_INPUT), 100), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1)), "BQB", "IGI").define('I', TTItemTags.PLATES_IRON)
                .define('B', TTItemTags.PLATES_BRASS).define('Q', Items.DISPENSER).define('G', TTItems.ALCHEMICAL_CONSTRUCT).gate(gate("essentia_transport"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.ESSENTIA_OUTPUT), 100), cost(TTAspects.AER, 1), cost(TTAspects.AQUA, 1)), "BQB", "IGI").define('I', TTItemTags.PLATES_IRON)
                .define('B', TTItemTags.PLATES_BRASS).define('Q', Items.HOPPER).define('G', TTItems.ALCHEMICAL_CONSTRUCT).gate(gate("essentia_transport"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS)));
    }

    private void buildFluxMachineRecipes() {

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.BRAIN_BOX), 50), cost(TTAspects.TERRA, 1), cost(TTAspects.ORDO, 1)), "IAI", "ABA", "IAI").define('B', TTItems.MIND_CLOCKWORK)
                .define('A', TTItemTags.GEMS_AMBER).define('I', TTItemTags.PLATES_IRON).gate(gate("thaumatorium")).unlockedBy("has", has(TTItems.MIND_CLOCKWORK)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CONDENSER), 500), cost(TTAspects.AER, 5), cost(TTAspects.AQUA, 5), cost(TTAspects.PERDITIO, 5)), "BCB", "WMW", "BTB")
                .define('T', TTItems.TUBE).define('C', TTItems.MORPHIC_RESONATOR).define('W', ItemTags.PLANKS).define('M', TTItems.MECHANISM_COMPLEX).define('B', TTItemTags.PLATES_BRASS)
                .gate(gate("flux_cleanup")).unlockedBy("has", has(TTItems.MORPHIC_RESONATOR)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.CONDENSER_LATTICE), 100), cost(TTAspects.TERRA, 3), cost(TTAspects.AER, 3)), "QTQ", "QFQ", "QTQ")
                .define('T', TTItemTags.PLATES_THAUMIUM).define('F', TTItems.FILTER).define('Q', Tags.Items.GEMS_QUARTZ).gate(gate("flux_cleanup")).unlockedBy("has", has(TTItems.FILTER)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.STABILIZER), 250), cost(TTAspects.TERRA, 1), cost(TTAspects.AQUA, 1), cost(TTAspects.PERDITIO, 1)), "SRS", "BVB", "IMI")
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE).define('S', TTItems.SLAB_ARCANE_STONE).define('B', TTItems.STONE_ARCANE).define('M', TTItems.MECHANISM_COMPLEX)
                .define('V', TTItems.VIS_RESONATOR).define('I', TTItemTags.PLATES_IRON).gate(gate("infusion_stable")).unlockedBy("has", has(TTItems.VIS_RESONATOR)));

        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.REDSTONE_RELAY), 10), cost(TTAspects.ORDO, 1)), "TGT", "SSS").define('T', Items.REDSTONE_TORCH)
                .define('G', TTItems.MECHANISM_SIMPLE).define('S', Items.STONE_SLAB).gate(gate("redstone_relay")).unlockedBy("has", has(TTItems.MECHANISM_SIMPLE)));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.VOID_SIPHON), tagged(TTItemTags.STORAGE_BLOCKS_VOID_METAL)), single(TTItems.STONE_ARCANE.get()),
                        single(TTItems.STONE_ARCANE.get()), single(TTItems.MECHANISM_COMPLEX.get()), tagged(TTItemTags.PLATES_BRASS), tagged(TTItemTags.PLATES_BRASS), tagged(Tags.Items.NETHER_STARS)),
                cost(TTAspects.ALIENIS, 50), cost(TTAspects.PERDITIO, 50), cost(TTAspects.VACUOS, 100), cost(TTAspects.FABRICO, 50)).instability(7).gate(gate("void_siphon"))
                .unlockedBy("has", has(TTItemTags.STORAGE_BLOCKS_VOID_METAL)));
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory.MISC, result, items, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis);
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.MISC, result, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis, items);
    }

    private Ingredient tagged(TagKey<Item> tag) {
        return Ingredient.of(items.getOrThrow(tag));
    }

    private static Ingredient single(ItemLike item) {
        return Ingredient.of(item);
    }

    private Ingredient crystal(ResourceKey<IAspect> aspect) {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        return DataComponentIngredient.of(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspects.getOrThrow(aspect), 1), TTItems.ESSENTIA_CRYSTAL.get());
    }

    private static final TagKey<Item> NUGGETS_COPPER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/copper"));
    private static final TagKey<Item> NUGGETS_SILVER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/silver"));
    private static final float AMBER_FROM_RESIN_XP = 0.2F;
    private static final String NITOR_DYE_GROUP = TTIds.rl("nitor_dye").toString();
    private static final String ROBE_DYE_GROUP = "cloth_robes";
    private static final String CRIMSON_DYE_GROUP = "crimson_armor";
    private static final int AMBER_FROM_RESIN_TIME = 100;
    private static final int WAND_CAP_GOLD_VIS = 9;
    private static final int WAND_CAP_COPPER_VIS = 6;
    private static final int WAND_CAP_SILVER_VIS = 12;
    private static final int WAND_CAP_THAUMIUM_VIS = 18;
    private static final int WAND_CAP_VOID_VIS = 90;
    private static final int WAND_ROD_GREATWOOD_VIS = 3;
    private static final int STAFF_GREATWOOD_VIS = 8;
    private static final int STAFF_ELEMENTAL_VIS = 14;
    private static final int STAFF_SILVERWOOD_VIS = 24;
    private static final int PRIMAL_CHARM_VIS = 150;

    private ItemStackTemplate wandResult(WandCap cap, WandRod rod, boolean sceptre) {
        DataComponentPatch patch = DataComponentPatch.builder().set(TTDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre)).build();
        return new ItemStackTemplate(TTItems.WAND, 1, patch);
    }

    private void buildWandRecipes() {
        ShapedRecipePattern starterPattern = ShapedRecipePattern.of(Map.of('I', single(TTItems.WAND_CAP_IRON.get()), 'S', tagged(Tags.Items.RODS_WOODEN)), List.of("  I", " S ", "I  "));
        ShapedRecipe starter = new ShapedRecipe(RecipeBuilder.createCraftingCommonInfo(true), RecipeBuilder.createCraftingBookInfo(RecipeCategory.TOOLS, null), starterPattern,
                wandResult(TTWandParts.CAP_IRON.get(), TTWandParts.ROD_WOOD.get(), false));
        output.accept(ResourceKey.create(Registries.RECIPE, TTIds.rl("wand/assembly/iron_wood")), starter, null);

        commit(rows(shaped(RecipeCategory.MISC, TTItems.WAND_CAP_IRON), "NNN", "N N").define('N', Tags.Items.NUGGETS_IRON).unlockedBy("has", has(Tags.Items.NUGGETS_IRON)),
                TTIds.MODID + ":wand/part/wand_cap_iron");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_CAP_GOLD.get()), WAND_CAP_GOLD_VIS), "NNN", "N N").define('N', Tags.Items.NUGGETS_GOLD).gate(gate("cap_gold")).unlockedBy("has",
                has(Tags.Items.NUGGETS_GOLD)), TTIds.MODID + ":wand/part/wand_cap_gold");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_CAP_COPPER.get()), WAND_CAP_COPPER_VIS), "NNN", "N N").define('N', NUGGETS_COPPER).gate(gate("cap_copper")).unlockedBy("has",
                has(NUGGETS_COPPER)), whenTagPresent(NUGGETS_COPPER), TTIds.MODID + ":wand/part/wand_cap_copper");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_CAP_SILVER_INERT.get()), WAND_CAP_SILVER_VIS), "NNN", "N N").define('N', NUGGETS_SILVER).gate(gate("cap_silver")).unlockedBy("has",
                has(NUGGETS_SILVER)), whenTagPresent(NUGGETS_SILVER), TTIds.MODID + ":wand/part/wand_cap_silver_inert");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_CAP_THAUMIUM_INERT.get()), WAND_CAP_THAUMIUM_VIS), "NNN", "N N").define('N', TTItemTags.NUGGETS_THAUMIUM).gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TTItemTags.NUGGETS_THAUMIUM)), TTIds.MODID + ":wand/part/wand_cap_thaumium_inert");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_CAP_VOID_INERT.get()), WAND_CAP_VOID_VIS), "NNN", "N N").define('N', TTItemTags.NUGGETS_VOID_METAL).gate(gate("cap_void"))
                .unlockedBy("has", has(TTItemTags.NUGGETS_VOID_METAL)), TTIds.MODID + ":wand/part/wand_cap_void_inert");

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.WAND_CAP_SILVER.get()), single(TTItems.WAND_CAP_SILVER_INERT.get())), single(TTItems.SALIS_MUNDUS.get()),
                single(TTItems.SALIS_MUNDUS.get())), cost(TTAspects.POTENTIA, 8), cost(TTAspects.AURAM, 4)).instability(4).gate(gate("cap_silver"))
                .unlockedBy("has", has(TTItems.WAND_CAP_SILVER_INERT.get())));

        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.WAND_CAP_THAUMIUM.get()), single(TTItems.WAND_CAP_THAUMIUM_INERT.get())), single(TTItems.SALIS_MUNDUS.get()),
                single(TTItems.SALIS_MUNDUS.get()), single(TTItems.SALIS_MUNDUS.get())), cost(TTAspects.POTENTIA, 12), cost(TTAspects.AURAM, 6)).instability(5).gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TTItems.WAND_CAP_THAUMIUM_INERT.get())));

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.WAND_CAP_VOID.get()), single(TTItems.WAND_CAP_VOID_INERT.get())), single(TTItems.SALIS_MUNDUS.get()),
                        single(TTItems.SALIS_MUNDUS.get()), single(TTItems.SALIS_MUNDUS.get()), single(TTItems.SALIS_MUNDUS.get())),
                cost(TTAspects.POTENTIA, 18), cost(TTAspects.VACUOS, 18), cost(TTAspects.ALIENIS, 18), cost(TTAspects.AURAM, 18)).instability(8).gate(gate("cap_void"))
                .unlockedBy("has", has(TTItems.WAND_CAP_VOID_INERT.get())));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.WAND_ROD_GREATWOOD.get()), WAND_ROD_GREATWOOD_VIS), " G", "G ").define('G', TTItemTags.GREATWOOD_LOGS).gate(gate("rod_greatwood"))
                .unlockedBy("has", has(TTItemTags.GREATWOOD_LOGS)), TTIds.MODID + ":wand/part/wand_rod_greatwood");

        elementalRodInfusion(TTItems.WAND_ROD_OBSIDIAN, tagged(Tags.Items.OBSIDIANS_NORMAL), TTAspects.TERRA, TTAspects.TENEBRAE, "rod_obsidian");
        elementalRodInfusion(TTItems.WAND_ROD_ICE, single(Blocks.ICE), TTAspects.AQUA, TTAspects.GELUM, "rod_ice");
        elementalRodInfusion(TTItems.WAND_ROD_QUARTZ, single(Blocks.QUARTZ_BLOCK), TTAspects.ORDO, TTAspects.VITREUS, "rod_quartz");
        elementalRodInfusion(TTItems.WAND_ROD_REED, tagged(Tags.Items.CROPS_SUGAR_CANE), TTAspects.AER, TTAspects.MOTUS, "rod_reed");
        elementalRodInfusion(TTItems.WAND_ROD_BLAZE, tagged(Tags.Items.RODS_BLAZE), TTAspects.IGNIS, TTAspects.BESTIA, "rod_blaze");
        elementalRodInfusion(TTItems.WAND_ROD_BONE, tagged(Tags.Items.BONES), TTAspects.PERDITIO, TTAspects.EXANIMIS, "rod_bone");

        InfusionRecipeBuilder silverwoodRod = infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.WAND_ROD_SILVERWOOD.get()), tagged(TTItemTags.SILVERWOOD_LOGS))
                .component(single(TTItems.SALIS_MUNDUS.get()));
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            silverwoodRod.component(crystal(primal)).aspect(primal, 9);
        }
        silverwoodRod.aspect(TTAspects.PRAECANTATIO, 9).instability(5).gate(gate("rod_silverwood")).unlockedBy("has", has(TTItemTags.SILVERWOOD_LOGS)).save(output);

        staffCoreRecipe(TTItems.STAFF_ROD_GREATWOOD, TTItems.WAND_ROD_GREATWOOD, STAFF_GREATWOOD_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_OBSIDIAN, TTItems.WAND_ROD_OBSIDIAN, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_ICE, TTItems.WAND_ROD_ICE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_QUARTZ, TTItems.WAND_ROD_QUARTZ, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_REED, TTItems.WAND_ROD_REED, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_BLAZE, TTItems.WAND_ROD_BLAZE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_BONE, TTItems.WAND_ROD_BONE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_SILVERWOOD, TTItems.WAND_ROD_SILVERWOOD, STAFF_SILVERWOOD_VIS);

        InfusionRecipeBuilder primalStaff = infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.STAFF_ROD_PRIMAL.get()), single(TTItems.WAND_ROD_SILVERWOOD.get()))
                .component(single(TTItems.PRIMAL_CHARM.get())).component(single(TTItems.WAND_ROD_OBSIDIAN.get())).component(single(TTItems.WAND_ROD_ICE.get()))
                .component(single(TTItems.WAND_ROD_QUARTZ.get())).component(single(TTItems.PRIMAL_CHARM.get())).component(single(TTItems.WAND_ROD_REED.get()))
                .component(single(TTItems.WAND_ROD_BLAZE.get())).component(single(TTItems.WAND_ROD_BONE.get()));
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            primalStaff.aspect(primal, 32);
        }
        primalStaff.aspect(TTAspects.PRAECANTATIO, 64).instability(8).gate(gate("staff_primal")).unlockedBy("has", has(TTItems.WAND_ROD_SILVERWOOD.get())).save(output);

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.PRIMAL_CHARM.get()), PRIMAL_CHARM_VIS), "123", "ISI", "456").define('1', crystal(TTAspects.AER)).define('2', crystal(TTAspects.IGNIS))
                .define('3', crystal(TTAspects.AQUA)).define('4', crystal(TTAspects.TERRA)).define('5', crystal(TTAspects.ORDO)).define('6', crystal(TTAspects.PERDITIO))
                .define('I', Tags.Items.INGOTS_GOLD).define('S', TTItems.SALIS_MUNDUS).gate(gate("unlock_artifice")).unlockedBy("has", has(TTItems.SALIS_MUNDUS)),
                TTIds.MODID + ":wand/part/primal_charm");
    }

    private static final int NODE_STABILIZER_VIS = 96;

    private void buildNodeHusbandryRecipes() {
        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.NODE_STABILIZER.get()), NODE_STABILIZER_VIS), " G ", "QPQ", "SNS").define('G', Tags.Items.INGOTS_GOLD).define('Q', Blocks.QUARTZ_BLOCK)
                .define('P', Blocks.PISTON).define('S', TTItems.STONE_ARCANE_BRICK).define('N', TTItemTags.NITORS).gate(gate("node_stabilizer")).unlockedBy("has", has(TTItemTags.NITORS)),
                TTIds.MODID + ":node_stabilizer");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.NODE_TRANSDUCER.get()), NODE_STABILIZER_VIS), "RCR", "ISI", "RAR").define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('C', Items.COMPARATOR).define('I', Tags.Items.INGOTS_IRON).define('S', TTItems.NODE_STABILIZER).define('A', TTItemTags.NITORS).gate(gate("node_transducer"))
                .unlockedBy("has", has(TTItems.NODE_STABILIZER)), TTIds.MODID + ":node_transducer");

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.VIS_RELAY.get()), NODE_STABILIZER_VIS), " A ", "GNG", " S ").define('A', Tags.Items.GEMS_AMETHYST).define('G', Tags.Items.INGOTS_GOLD)
                .define('N', TTItemTags.NITORS).define('S', TTItems.STONE_ARCANE).gate(gate("vis_relay")).unlockedBy("has", has(TTItemTags.NITORS)), TTIds.MODID + ":vis_relay");

        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.NODE_STABILIZER_ADVANCED.get()), single(TTItems.NODE_STABILIZER.get())), tagged(TTItemTags.NITORS),
                        tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE), single(TTItems.ALUMENTUM.get()), tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE), tagged(TTItemTags.NITORS),
                        tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE), single(TTItems.ALUMENTUM.get()), tagged(Tags.Items.STORAGE_BLOCKS_REDSTONE)),
                cost(TTAspects.AURAM, 32), cost(TTAspects.PRAECANTATIO, 16), cost(TTAspects.ORDO, 16), cost(TTAspects.POTENTIA, 16)).instability(10).gate(gate("node_stabilizer_advanced"))
                .unlockedBy("has", has(TTItems.NODE_STABILIZER.get())));
    }

    private void elementalRodInfusion(DeferredItem<? extends Item> rod, Ingredient catalyst, ResourceKey<IAspect> primal, ResourceKey<IAspect> flavor, String gateEntry) {
        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(rod.get()), catalyst), single(TTItems.SALIS_MUNDUS.get()), crystal(primal)).aspect(primal, 12),
                cost(TTAspects.PRAECANTATIO, 6)).aspect(flavor, 6).instability(3).gate(gate(gateEntry)).unlockedBy("has", has(TTItems.SALIS_MUNDUS.get())));
    }

    private void staffCoreRecipe(DeferredItem<? extends Item> core, DeferredItem<? extends Item> rod, int vis) {
        commit(rows(arcaneShaped(new ItemStackTemplate(core.get()), vis), "  S", " G ", "G  ").define('S', TTItems.PRIMAL_CHARM).define('G', rod).gate(gate("staves")).unlockedBy("has",
                has(TTItems.PRIMAL_CHARM)), TTIds.MODID + ":wand/part/" + core.getId().getPath());
    }

    private void buildBaubleRecipes() {

        commit(rows(shaped(RecipeCategory.MISC, TTItems.AMULET_MUNDANE), " S ", "S S", " I ").define('S', Tags.Items.STRINGS).define('I', TTItemTags.INGOTS_BRASS).unlockedBy("has",
                has(TTItemTags.INGOTS_BRASS)));
        commit(rows(shaped(RecipeCategory.MISC, TTItems.RING_MUNDANE), "NNN", "N N", "NNN").define('N', TTItemTags.NUGGETS_BRASS).unlockedBy("has", has(TTItemTags.NUGGETS_BRASS)));
        commit(rows(shaped(RecipeCategory.MISC, TTItems.GIRDLE_MUNDANE), " L ", "L L", " I ").define('L', Tags.Items.LEATHERS).define('I', TTItemTags.INGOTS_BRASS).unlockedBy("has",
                has(TTItemTags.INGOTS_BRASS)));
        commit(rows(shaped(RecipeCategory.MISC, TTItems.AMULET_FANCY), " S ", "SGS", " I ").define('S', Tags.Items.STRINGS).define('G', Tags.Items.GEMS_DIAMOND).define('I', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has", has(Tags.Items.GEMS_DIAMOND)));
        commit(rows(shaped(RecipeCategory.MISC, TTItems.RING_FANCY), "NGN", "N N", "NNN").define('G', Tags.Items.GEMS_DIAMOND).define('N', Tags.Items.NUGGETS_GOLD).unlockedBy("has",
                has(Tags.Items.GEMS_DIAMOND)));
        commit(rows(shaped(RecipeCategory.MISC, TTItems.GIRDLE_FANCY), " L ", "LGL", " I ").define('L', Tags.Items.LEATHERS).define('G', Tags.Items.GEMS_DIAMOND).define('I', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has", has(Tags.Items.GEMS_DIAMOND)));

        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.FOCUS_POUCH), 25), "LGL", "LBL", "LLL").define('B', TTItems.GIRDLE_MUNDANE).define('L', Tags.Items.LEATHERS)
                .define('G', Tags.Items.INGOTS_GOLD).gate(gate("focus_pouch")).unlockedBy("has", has(Tags.Items.LEATHERS)));
        commit(rows(priced(arcaneShaped(new ItemStackTemplate(TTItems.SANITY_CHECKER), 20), cost(TTAspects.ORDO, 1), cost(TTAspects.PERDITIO, 1)), "BN ", "M N", "BN ")
                .define('N', TTItemTags.NUGGETS_BRASS).define('B', TTItems.BRAIN).define('M', TTItems.MIRRORED_GLASS).gate(gate("warp")).unlockedBy("has", has(TTItems.MIRRORED_GLASS)));
        commit(rows(arcaneShaped(new ItemStackTemplate(TTItems.RESONATOR), 50), "I I", "INI", " S ").define('I', TTItemTags.PLATES_IRON).define('N', Tags.Items.GEMS_QUARTZ)
                .define('S', Tags.Items.RODS_WOODEN).gate(gate("tubes")).unlockedBy("has", has(TTItemTags.PLATES_IRON)));
    }

    private void buildWearableInfusionRecipes() {
        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.AMULET_VIS_CRAFTED.get()), single(TTItems.AMULET_MUNDANE.get())), single(TTItems.VIS_RESONATOR.get()),
                        crystal(TTAspects.AER), crystal(TTAspects.IGNIS), crystal(TTAspects.AQUA), crystal(TTAspects.TERRA), crystal(TTAspects.ORDO)),
                cost(TTAspects.AURAM, 50), cost(TTAspects.POTENTIA, 100), cost(TTAspects.VACUOS, 50)).instability(6).gate(gate("vis_amulet")).unlockedBy("has", has(TTItems.AMULET_MUNDANE.get())));
        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.VERDANT_CHARM.get()), single(TTItems.AMULET_FANCY.get())), tagged(TTItemTags.NUGGETS_QUICKSILVER),
                crystal(TTAspects.VICTUS), tagged(Tags.Items.BUCKETS_MILK), crystal(TTAspects.HERBA)), cost(TTAspects.VICTUS, 60), cost(TTAspects.ORDO, 30), cost(TTAspects.HERBA, 60)).instability(5)
                .gate(gate("verdant_charms")).unlockedBy("has", has(TTItems.AMULET_FANCY.get())));
        commit(priced(parts(
                infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.VERDANT_CHARM.get()), single(TTItems.VERDANT_CHARM.get()))
                        .catalystPatch(DataComponentPatch.builder().set(TTDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_LIFE).build()),
                single(Items.GOLDEN_APPLE), crystal(TTAspects.VICTUS), potion(Potions.STRONG_HEALING), crystal(TTAspects.HUMANUS)), cost(TTAspects.VICTUS, 80), cost(TTAspects.HUMANUS, 80))
                .instability(5).gate(gate("verdant_charms")).unlockedBy("has", has(TTItems.VERDANT_CHARM.get())), TTIds.MODID + ":infusion/verdant_charm_life");
        commit(priced(parts(
                infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.VERDANT_CHARM.get()), single(TTItems.VERDANT_CHARM.get()))
                        .catalystPatch(DataComponentPatch.builder().set(TTDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_SUSTAIN).build()),
                single(TTItems.TRIPLE_MEAT_TREAT.get()), crystal(TTAspects.DESIDERIUM), potion(Potions.STRONG_REGENERATION), crystal(TTAspects.AER)), cost(TTAspects.DESIDERIUM, 80),
                cost(TTAspects.AER, 80)).instability(5).gate(gate("verdant_charms")).unlockedBy("has", has(TTItems.VERDANT_CHARM.get())), TTIds.MODID + ":infusion/verdant_charm_sustain");
        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.CLOUD_RING.get()), single(TTItems.RING_MUNDANE.get())), crystal(TTAspects.AER), tagged(Tags.Items.FEATHERS)),
                cost(TTAspects.AER, 50)).instability(1).gate(gate("cloud_ring")).unlockedBy("has", has(TTItems.RING_MUNDANE.get())));
        commit(priced(parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.CURIOSITY_BAND.get()), single(TTItems.GIRDLE_FANCY.get())), tagged(Tags.Items.GEMS_EMERALD),
                single(Items.WRITABLE_BOOK), tagged(Tags.Items.GEMS_EMERALD), single(Items.WRITABLE_BOOK), tagged(Tags.Items.GEMS_EMERALD), single(Items.WRITABLE_BOOK),
                tagged(Tags.Items.GEMS_EMERALD), single(Items.WRITABLE_BOOK)), cost(TTAspects.COGNITIO, 150), cost(TTAspects.VACUOS, 50), cost(TTAspects.VINCULUM, 100)).instability(5)
                .gate(gate("curiosity_band")).unlockedBy("has", has(TTItems.GIRDLE_FANCY.get())));
        commit(priced(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.CHARM_UNDYING.get()), single(Items.TOTEM_OF_UNDYING)).component(tagged(TTItemTags.PLATES_BRASS)),
                cost(TTAspects.VICTUS, 25)).instability(2).gate(gate("charm_undying")).unlockedBy("has", has(Items.TOTEM_OF_UNDYING)));
        commit(priced(
                parts(infusion(RecipeCategory.MISC, new ItemStackTemplate(TTItems.VOIDSEER_CHARM.get()), single(TTItems.AMULET_FANCY.get())), single(TTItems.BRAIN.get()),
                        single(TTItems.VOID_SEED.get()), single(TTItems.BRAIN.get()), single(TTItems.PRIMORDIAL_PEARL.get())),
                cost(TTAspects.COGNITIO, 150), cost(TTAspects.VACUOS, 150), cost(TTAspects.PRAECANTATIO, 100)).instability(8).gate(gate("voidseer_pearl"))
                .unlockedBy("has", has(TTItems.PRIMORDIAL_PEARL.get())));

        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.FORTRESS_HELM.get()), single(TTItems.THAUMIUM_HELM.get())), tagged(TTItemTags.PLATES_THAUMIUM),
                        tagged(TTItemTags.PLATES_THAUMIUM), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.GEMS_EMERALD)),
                cost(TTAspects.METALLUM, 50), cost(TTAspects.PRAEMUNIO, 20), cost(TTAspects.POTENTIA, 25)).instability(3).gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_HELM.get())));
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.FORTRESS_CHEST.get()), single(TTItems.THAUMIUM_CHEST.get())), tagged(TTItemTags.PLATES_THAUMIUM),
                        tagged(TTItemTags.PLATES_THAUMIUM), tagged(TTItemTags.PLATES_THAUMIUM), tagged(TTItemTags.PLATES_THAUMIUM), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.LEATHERS)),
                cost(TTAspects.METALLUM, 50), cost(TTAspects.PRAEMUNIO, 30), cost(TTAspects.POTENTIA, 25)).instability(3).gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_CHEST.get())));
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.FORTRESS_LEGS.get()), single(TTItems.THAUMIUM_LEGS.get())), tagged(TTItemTags.PLATES_THAUMIUM),
                        tagged(TTItemTags.PLATES_THAUMIUM), tagged(TTItemTags.PLATES_THAUMIUM), tagged(Tags.Items.INGOTS_GOLD), tagged(Tags.Items.LEATHERS)),
                cost(TTAspects.METALLUM, 50), cost(TTAspects.PRAEMUNIO, 25), cost(TTAspects.POTENTIA, 25)).instability(3).gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_LEGS.get())));
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.FORTRESS_HELM.get()), single(TTItems.FORTRESS_HELM.get())).catalystPatch(
                        DataComponentPatch.builder().set(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE).build()), tagged(Tags.Items.SLIME_BALLS), single(TTItems.GOGGLES_REVEALING.get())),
                cost(TTAspects.SENSUS, 40), cost(TTAspects.AURAM, 20), cost(TTAspects.PRAEMUNIO, 20)).instability(5).gate(gate("fortress_mask")).unlockedBy("has", has(TTItems.FORTRESS_HELM.get())),
                TTIds.MODID + ":infusion/fortress_helm_goggles");
        for (MaskVariant variant : maskVariants()) {
            buildMaskRecipe(gate("fortress_mask"), variant);
        }

        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.VOID_ROBE_HELM.get()), single(TTItems.VOID_HELM.get())), single(TTItems.GOGGLES_REVEALING.get()),
                        single(TTItems.FABRIC.get()), single(TTItems.FABRIC.get()), single(TTItems.SALIS_MUNDUS.get()), single(TTItems.FABRIC.get()), single(TTItems.FABRIC.get())),
                cost(TTAspects.METALLUM, 25), cost(TTAspects.SENSUS, 25), cost(TTAspects.PRAEMUNIO, 25), cost(TTAspects.POTENTIA, 25), cost(TTAspects.ALIENIS, 25), cost(TTAspects.VACUOS, 25))
                .instability(6).gate(gate("void_robe_armor")).unlockedBy("has", has(TTItems.VOID_HELM.get())));
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.VOID_ROBE_CHEST.get()), single(TTItems.VOID_CHEST.get())), single(TTItems.CLOTH_CHEST.get()),
                        tagged(TTItemTags.PLATES_VOID_METAL), tagged(TTItemTags.PLATES_VOID_METAL), single(TTItems.SALIS_MUNDUS.get()), single(TTItems.FABRIC.get()), tagged(Tags.Items.LEATHERS)),
                cost(TTAspects.METALLUM, 35), cost(TTAspects.PRAEMUNIO, 35), cost(TTAspects.POTENTIA, 25), cost(TTAspects.ALIENIS, 25), cost(TTAspects.VACUOS, 35)).instability(6)
                .gate(gate("void_robe_armor")).unlockedBy("has", has(TTItems.VOID_CHEST.get())));
        commit(priced(
                parts(infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.VOID_ROBE_LEGS.get()), single(TTItems.VOID_LEGS.get())), single(TTItems.CLOTH_LEGS.get()),
                        tagged(TTItemTags.PLATES_VOID_METAL), tagged(TTItemTags.PLATES_VOID_METAL), single(TTItems.SALIS_MUNDUS.get()), single(TTItems.FABRIC.get()), tagged(Tags.Items.LEATHERS)),
                cost(TTAspects.METALLUM, 30), cost(TTAspects.PRAEMUNIO, 30), cost(TTAspects.POTENTIA, 25), cost(TTAspects.ALIENIS, 25), cost(TTAspects.VACUOS, 30)).instability(6)
                .gate(gate("void_robe_armor")).unlockedBy("has", has(TTItems.VOID_LEGS.get())));
    }

    private record MaskVariant(int mask, ResourceKey<IAspect> first, ResourceKey<IAspect> second, Ingredient dye, Ingredient special1, Ingredient special2) {
    }

    private List<MaskVariant> maskVariants() {
        return List.of(new MaskVariant(0, TTAspects.COGNITIO, TTAspects.VICTUS, single(Items.INK_SAC), single(TTItems.PLANT_SHIMMERLEAF.get()), single(TTItems.BRAIN.get())),
                new MaskVariant(1, TTAspects.PERDITIO, TTAspects.MORTUUS, single(Items.BONE_MEAL), single(Items.POISONOUS_POTATO), single(Items.WITHER_SKELETON_SKULL)),
                new MaskVariant(2, TTAspects.EXANIMIS, TTAspects.VICTUS, tagged(Tags.Items.DYES_RED), single(Items.GHAST_TEAR), tagged(Tags.Items.BUCKETS_MILK)));
    }

    private void buildMaskRecipe(ResearchGate gate, MaskVariant variant) {
        InfusionRecipeBuilder mask = infusion(RecipeCategory.COMBAT, new ItemStackTemplate(TTItems.FORTRESS_HELM.get()), single(TTItems.FORTRESS_HELM.get()))
                .catalystPatch(DataComponentPatch.builder().set(TTDataComponents.FORTRESS_MASK.get(), variant.mask()).build());
        Ingredient plates = tagged(TTItemTags.PLATES_IRON);
        parts(mask, variant.dye(), plates, tagged(Tags.Items.LEATHERS), variant.special1(), variant.special2(), plates);
        mask.aspect(variant.first(), 80).aspect(variant.second(), 80);
        commit(priced(mask, cost(TTAspects.PRAEMUNIO, 20)).instability(8).gate(gate).unlockedBy("has", has(TTItems.FORTRESS_HELM.get())),
                TTIds.MODID + ":infusion/fortress_helm_mask_" + variant.mask());
    }

    private Ingredient potion(Holder<Potion> potion) {
        return DataComponentIngredient.of(DataComponents.POTION_CONTENTS, new PotionContents(potion), Items.POTION);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new TTRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Thaumaturge Recipes";
        }
    }

    private void buildDustTriggerRecipes() {
        dustTrigger("bookshelf_to_thaumonomicon", new DustTriggerTagRecipe(Tags.Blocks.BOOKSHELVES, new ItemStackTemplate(TTItems.THAUMONOMICON.get()), Optional.of(gate("gotdream"))));
        dustTrigger("crafting_tables_to_arcane_workbench",
                new DustTriggerTagRecipe(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES, new ItemStackTemplate(TTBlocks.ARCANE_WORKBENCH.get().asItem()), Optional.of(gate("first_steps", 0))));
        dustTrigger("cauldron_to_crucible", new DustTriggerSimpleRecipe(Blocks.CAULDRON, new ItemStackTemplate(TTBlocks.CRUCIBLE.get().asItem()), Optional.of(gate("unlock_alchemy", 0))));
        multiblockTrigger("golem_press", TTBlocks.GOLEM_BUILDER.get(), "mind_clockwork");
        multiblockTrigger("advanced_alchemical_furnace", TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get(), "advanced_alchemical_furnace");
        multiblockTrigger("infernal_furnace", TTBlocks.INFERNAL_FURNACE.get(), "infernal_furnace");
        multiblockTrigger("infusion_altar", TTBlocks.INFUSION_MATRIX.get(), "infusion");
        multiblockTrigger("infusion_altar_ancient", TTBlocks.INFUSION_MATRIX.get(), "infusion_ancient");
        multiblockTrigger("infusion_altar_eldritch", TTBlocks.INFUSION_MATRIX.get(), "infusion_eldritch");
        multiblockTrigger("thaumatorium", TTBlocks.THAUMATORIUM.get(), "thaumatorium");
    }

    private void multiblockTrigger(String name, Block target, String research) {
        dustTrigger(name, new DustTriggerMultiblockRecipe(TTIds.rl(name), new ItemStackTemplate(target.asItem()), Optional.of(gate(research))));
    }

    private void dustTrigger(String name, Recipe<?> recipe) {
        output.accept(ResourceKey.create(Registries.RECIPE, TTIds.rl("dust_trigger/" + name)), recipe, null);
    }

    private void buildSalisMundusRecipe() {
        SpecialRecipeBuilder.special(() -> SalisMundusRecipe.INSTANCE).unlockedBy("has_crystal", has(TTItems.ESSENTIA_CRYSTAL)).save(output, "thaumaturge:salis_mundus");
    }
}
