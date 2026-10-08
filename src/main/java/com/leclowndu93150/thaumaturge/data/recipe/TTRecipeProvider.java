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
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumaturge.registry.TTBlockFamilies;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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

    private RecipeOutput output;
    private HolderLookup.Provider registries;
    private HolderLookup<Item> items;

    public TTRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    private static ResearchGate gate(String path) {
        return new ResearchGate(TTIds.rl(path), Optional.empty(), false);
    }

    private static ResearchGate gate(String path, int stage) {
        return new ResearchGate(TTIds.rl(path), Optional.of(stage), false);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        throw new IllegalStateException("buildRecipes(RecipeOutput, HolderLookup.Provider) is the entrypoint");
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider provider) {
        this.output = recipeOutput;
        this.registries = provider;
        this.items = provider.lookupOrThrow(Registries.ITEM);
        buildDustTriggerRecipes();
        buildSalisMundusRecipe();
        buildArcaneWorkbenchRecipes();
        buildBannerRecipes();
        buildGearRecipes();
        buildInfusionAltarRecipes();
        buildThaumostaticHarnessRecipe();
        buildInfusionEnchantmentRecipes();
        buildRunicAugmentRecipe();
        buildElementalToolRecipes();
        buildTravellerBootsRecipe();
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
        buildLegacyWardRecipes();
        buildNoiseDeviceRecipes();
        buildEssentiaMachineRecipes();
        buildFluxMachineRecipes();
        buildBaubleRecipes();
        buildWearableInfusionRecipes();
        buildWandRecipes();
        buildNodeHusbandryRecipes();

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TTItems.SCRIBING_TOOLS)
                .requires(TTItems.PHIAL)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(TTItems.PHIAL))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TTItems.SCRIBING_TOOLS)
                .requires(Items.GLASS_BOTTLE)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(Tags.Items.GLASS_PANES))
                .save(output, TTIds.MODID + ":scribing_tools_alt");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TTItems.LABEL, 4)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PAPER, 4)
                .unlockedBy("has", has(Tags.Items.SLIME_BALLS))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TTItems.LABEL)
                .requires(TTItems.LABEL)
                .unlockedBy("has", has(TTItems.LABEL))
                .save(output, TTIds.MODID + ":label_clear");

        SpecialRecipeBuilder.special(category -> LabelFilterRecipe.INSTANCE)
                .save(output, TTIds.rl("label_filter").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.JAR_BRACE, 2)
                .pattern("SBS")
                .pattern("B B")
                .pattern("SBS")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('B', TTItemTags.NUGGETS_BRASS)
                .unlockedBy("has", has(TTItemTags.NUGGETS_BRASS))
                .save(output);

        for (DyeColor color : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(
                            RecipeCategory.MISC, TTItems.NITORS.get(color).get())
                    .requires(TTItemTags.NITORS)
                    .requires(color.getTag())
                    .unlockedBy("has", has(TTItemTags.NITORS))
                    .save(output, TTIds.MODID + ":nitors/" + color.getName());
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.STONE_ARCANE, 8)
                .pattern("SSS")
                .pattern("SVS")
                .pattern("SSS")
                .define('S', Tags.Items.STONES)
                .define('V', TTItems.ESSENTIA_CRYSTAL)
                .unlockedBy("has", has(TTItems.ESSENTIA_CRYSTAL))
                .save(output);

        TTBlockFamilies.getAllFamilies()
                .forEach(family -> generateRecipes(output, family, FeatureFlagSet.of(FeatureFlags.VANILLA)));

        TTBlockFamilies.getAllFamilies()
                .forEach(family -> family.getVariants().forEach((variant, block) -> {
                    if (variant == BlockFamily.Variant.STAIRS
                            || variant == BlockFamily.Variant.SLAB
                            || variant == BlockFamily.Variant.WALL
                            || variant == BlockFamily.Variant.POLISHED) {
                        stonecutterResultFromBase(
                                output,
                                RecipeCategory.BUILDING_BLOCKS,
                                block,
                                family.getBaseBlock(),
                                variant == BlockFamily.Variant.SLAB ? 2 : 1);
                    }
                }));
        pillarRecipe(TTBlocks.STONE_ARCANE_PILLAR.get(), TTBlocks.STONE_ARCANE.get());
        pillarRecipe(TTBlocks.STONE_ANCIENT_PILLAR.get(), TTBlocks.STONE_ANCIENT.get());
        pillarRecipe(TTBlocks.STONE_ELDRITCH_PILLAR.get(), TTBlocks.ELDRITCH_STONE.get());

        planksFromLogs(output, TTItems.PLANK_GREATWOOD.get(), TTItemTags.GREATWOOD_LOGS, 4);
        planksFromLogs(output, TTItems.PLANK_SILVERWOOD.get(), TTItemTags.SILVERWOOD_LOGS, 4);
        woodFromLogs(output, TTItems.WOOD_GREATWOOD.get(), TTItems.LOG_GREATWOOD.get());
        woodFromLogs(output, TTItems.STRIPPED_WOOD_GREATWOOD.get(), TTItems.STRIPPED_LOG_GREATWOOD.get());
        woodFromLogs(output, TTItems.WOOD_SILVERWOOD.get(), TTItems.LOG_SILVERWOOD.get());
        woodFromLogs(output, TTItems.STRIPPED_WOOD_SILVERWOOD.get(), TTItems.STRIPPED_LOG_SILVERWOOD.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.PHIAL, 8)
                .pattern(" C ")
                .pattern("P P")
                .pattern(" P ")
                .define('C', Items.CLAY_BALL)
                .define('P', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

        oreSmelting(TTItems.QUICKSILVER, TTItemTags.ORES_CINNABAR, 1F, "quicksilver");
        rawSmelting(TTItems.QUICKSILVER, TTItemTags.RAW_MATERIALS_CINNABAR, 0.7F, "quicksilver", "raw_cinnabar");
        oreSmelting(TTItems.AMBER, TTItemTags.ORES_AMBER, 1F, "amber");
        oreSmelting(Items.QUARTZ, Tags.Items.ORES_QUARTZ, 0.2F, "quartz");

        block3x3(
                TTItems.METAL_BRASS_BLOCK,
                TTItemTags.INGOTS_BRASS,
                TTItems.INGOT_BRASS,
                TTItemTags.STORAGE_BLOCKS_BRASS);
        block3x3(
                TTItems.METAL_THAUMIUM_BLOCK,
                TTItemTags.INGOTS_THAUMIUM,
                TTItems.INGOT_THAUMIUM,
                TTItemTags.STORAGE_BLOCKS_THAUMIUM);
        block3x3(
                TTItems.METAL_VOID_BLOCK,
                TTItemTags.INGOTS_VOID_METAL,
                TTItems.INGOT_VOID,
                TTItemTags.STORAGE_BLOCKS_VOID_METAL);
        block2x2(TTItems.AMBER_BLOCK, TTItemTags.GEMS_AMBER, TTItems.AMBER, TTItemTags.STORAGE_BLOCKS_AMBER);

        nuggets3x3(Items.QUARTZ, TTItemTags.NUGGETS_QUARTZ, TTItems.NUGGET_QUARTZ, Tags.Items.GEMS_QUARTZ);
        nuggets3x3(
                TTItems.QUICKSILVER,
                TTItemTags.NUGGETS_QUICKSILVER,
                TTItems.NUGGET_QUICKSILVER,
                TTItemTags.GEMS_QUICKSILVER);
        nuggets3x3(TTItems.INGOT_BRASS, TTItemTags.NUGGETS_BRASS, TTItems.NUGGET_BRASS, TTItemTags.INGOTS_BRASS);
        nuggets3x3(
                TTItems.INGOT_THAUMIUM,
                TTItemTags.NUGGETS_THAUMIUM,
                TTItems.NUGGET_THAUMIUM,
                TTItemTags.INGOTS_THAUMIUM);
        nuggets3x3(
                TTItems.INGOT_VOID, TTItemTags.NUGGETS_VOID_METAL, TTItems.NUGGET_VOID, TTItemTags.INGOTS_VOID_METAL);

        plateRecipe(TTItems.PLATE_IRON, Tags.Items.INGOTS_IRON);
        plateRecipe(TTItems.PLATE_BRASS, TTItemTags.INGOTS_BRASS);
        plateRecipe(TTItems.PLATE_THAUMIUM, TTItemTags.INGOTS_THAUMIUM);
        plateRecipe(TTItems.PLATE_VOID, TTItemTags.INGOTS_VOID_METAL);

        clusterSmelting(Items.IRON_INGOT, TTItems.CLUSTER_IRON, "iron_ingot");
        clusterSmelting(Items.GOLD_INGOT, TTItems.CLUSTER_GOLD, "gold_ingot");
        clusterSmelting(Items.COPPER_INGOT, TTItems.CLUSTER_COPPER, "copper_ingot");
        clusterSmelting(TTItems.QUICKSILVER, TTItems.CLUSTER_CINNABAR, "quicksilver");
        clusterSmelting(Items.QUARTZ, TTItems.CLUSTER_QUARTZ, "quartz");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TTItems.QUICKSILVER)
                .requires(TTItems.PLANT_SHIMMERLEAF)
                .unlockedBy("has", has(TTItems.PLANT_SHIMMERLEAF))
                .save(output, TTIds.MODID + ":quicksilver_from_shimmerleaf");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLAZE_POWDER)
                .requires(TTItems.PLANT_CINDERPEARL)
                .unlockedBy("has", has(TTItems.PLANT_CINDERPEARL))
                .save(output, TTIds.MODID + ":blaze_powder_from_cinderpearl");

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.DECORATIONS,
                        TTBlocks.CANDLES.get(DyeColor.WHITE).get(),
                        3)
                .pattern(" S ")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', Tags.Items.STRINGS)
                .define('T', TTItems.TALLOW.get())
                .unlockedBy("has_tallow", has(TTItems.TALLOW.get()))
                .save(output);
        arcaneShaped(
                        new ItemStack(TTItems.CANDLE_HOLDERS
                                .get(CandleHolderMaterial.BRASS)
                                .get()),
                        10)
                .aspect(TTAspects.IGNIS, 1)
                .pattern(" N ")
                .pattern("NPN")
                .define('N', TTItemTags.NUGGETS_BRASS)
                .define('P', TTItemTags.PLATES_BRASS)
                .gate(gate("candle_holders", 1))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);
        arcaneShaped(
                        new ItemStack(TTItems.CANDLE_HOLDERS
                                .get(CandleHolderMaterial.THAUMIUM)
                                .get()),
                        25)
                .aspect(TTAspects.IGNIS, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern(" N ")
                .pattern("NPN")
                .define('N', TTItemTags.NUGGETS_THAUMIUM)
                .define('P', TTItemTags.PLATES_THAUMIUM)
                .gate(gate("candle_holders", 2))
                .unlockedBy("has", has(TTItemTags.PLATES_THAUMIUM))
                .save(output);
        arcaneShaped(
                        new ItemStack(TTItems.CANDLE_HOLDERS
                                .get(CandleHolderMaterial.VOID)
                                .get()),
                        50)
                .aspect(TTAspects.IGNIS, 1)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern(" N ")
                .pattern("NPN")
                .define('N', TTItemTags.NUGGETS_VOID_METAL)
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .gate(gate("candle_holders", 3))
                .unlockedBy("has", has(TTItemTags.PLATES_VOID_METAL))
                .save(output);
        for (DyeColor dye : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(
                            RecipeCategory.DECORATIONS,
                            TTBlocks.CANDLES.get(dye).get())
                    .requires(dyeTag(dye))
                    .requires(TTItemTags.CANDLES)
                    .unlockedBy("has_candle", has(TTItemTags.CANDLES))
                    .save(output, TTIds.MODID + ":candle_" + dye.getName() + "_from_dye");
        }

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.THAUMONOMICON_LINKING.get()),
                        Ingredient.of(TTItems.THAUMONOMICON_SHARING.get()))
                .aspect(TTAspects.COGNITIO, 40)
                .aspect(TTAspects.SENSUS, 20)
                .aspect(TTAspects.ALIENIS, 10)
                .component(Ingredient.of(TTItems.VOID_SEED.get()))
                .component(Ingredient.of(TTItems.BRAIN.get()))
                .component(Ingredient.of(TTItems.VOID_SEED.get()))
                .component(Ingredient.of(Items.ENDER_EYE))
                .instability(2)
                .gate(gate("link_book", 1))
                .unlockedBy("has", has(TTItems.THAUMONOMICON_SHARING))
                .save(output);
    }

    private void block3x3(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', baseTag)
                .unlockedBy("has", has(baseTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, baseItem, 9)
                .requires(blockTag)
                .unlockedBy("has", has(blockTag))
                .save(
                        output,
                        TTIds.MODID + ":"
                                + BuiltInRegistries.ITEM
                                        .getKey(baseItem.asItem())
                                        .getPath() + "_from_block");
    }

    private void block2x2(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("##")
                .pattern("##")
                .define('#', baseTag)
                .unlockedBy("has", has(baseTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, baseItem, 4)
                .requires(blockTag)
                .unlockedBy("has", has(blockTag))
                .save(
                        output,
                        TTIds.MODID + ":"
                                + BuiltInRegistries.ITEM
                                        .getKey(baseItem.asItem())
                                        .getPath() + "_from_block");
    }

    private void nuggets3x3(ItemLike item, TagKey<Item> nuggetsTag, ItemLike nuggets, TagKey<Item> itemTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', nuggetsTag)
                .unlockedBy("has", has(nuggetsTag))
                .save(
                        output,
                        TTIds.MODID + ":"
                                + BuiltInRegistries.ITEM
                                        .getKey(nuggets.asItem())
                                        .getPath() + "_from_nuggets");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nuggets, 9)
                .requires(itemTag)
                .unlockedBy("has", has(itemTag))
                .save(output);
    }

    private void oreSmelting(ItemLike item, TagKey<Item> oreTag, float xp, String group) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(oreTag), RecipeCategory.MISC, item, xp, 200)
                .group(group)
                .unlockedBy("has", this.has(oreTag))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_from_ore");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(oreTag), RecipeCategory.MISC, item, xp, 100)
                .group(group)
                .unlockedBy("has", this.has(oreTag))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_blasting_from_ore");
    }

    private void rawSmelting(ItemLike item, TagKey<Item> rawTag, float xp, String group, String rawName) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(rawTag), RecipeCategory.MISC, item, xp, 200)
                .group(group)
                .unlockedBy("has", this.has(rawTag))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_from_" + rawName);

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(rawTag), RecipeCategory.MISC, item, xp, 100)
                .group(group)
                .unlockedBy("has", this.has(rawTag))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_blasting_from_" + rawName);
    }

    private void clusterSmelting(ItemLike item, ItemLike cluster, String group) {

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(cluster), RecipeCategory.MISC, new ItemStack(item.asItem(), 2), 1F, 200)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_from_cluster");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(cluster), RecipeCategory.MISC, new ItemStack(item.asItem(), 2), 1F, 100)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, TTIds.MODID + ":" + getItemName(item) + "_blasting_from_cluster");
    }

    private void plateRecipe(ItemLike plate, TagKey<Item> ingotTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, plate, 3)
                .pattern("NNN")
                .define('N', ingotTag)
                .unlockedBy("has", has(ingotTag))
                .save(output);
    }

    private void buildLegacyWardRecipes() {
        ResearchGate wardedArcanaGate = gate("warded_arcana");
        arcaneShaped(new ItemStack(TTItems.WARDED_GLASS.get(), 8), 25)
                .aspect(TTAspects.AQUA, 5)
                .aspect(TTAspects.ORDO, 10)
                .aspect(TTAspects.TERRA, 5)
                .aspect(TTAspects.IGNIS, 5)
                .pattern("GGG")
                .pattern("WBW")
                .pattern("GGG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('W', TTItems.PLANK_GREATWOOD)
                .define('B', TTItems.BRAIN)
                .gate(wardedArcanaGate)
                .unlockedBy("has", has(TTItems.BRAIN))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.ARCANE_DOOR.get()), 45)
                .aspect(TTAspects.AQUA, 20)
                .aspect(TTAspects.ORDO, 10)
                .aspect(TTAspects.TERRA, 10)
                .aspect(TTAspects.IGNIS, 5)
                .pattern("TDT")
                .pattern("DBD")
                .pattern("TDT")
                .define('T', TTItems.INGOT_THAUMIUM)
                .define('D', TTItems.PLANK_GREATWOOD)
                .define('B', TTItems.BRAIN)
                .gate(wardedArcanaGate)
                .unlockedBy("has", has(TTItems.BRAIN))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.ARCANE_PRESSURE_PLATE.get()), 45)
                .aspect(TTAspects.AQUA, 20)
                .aspect(TTAspects.ORDO, 10)
                .aspect(TTAspects.TERRA, 10)
                .aspect(TTAspects.IGNIS, 5)
                .pattern(" B ")
                .pattern("TDT")
                .define('T', TTItems.INGOT_THAUMIUM)
                .define('D', TTItems.PLANK_GREATWOOD)
                .define('B', TTItems.BRAIN)
                .gate(wardedArcanaGate)
                .unlockedBy("has", has(TTItems.BRAIN))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.GOLEM_FETTER.get()), 10)
                .aspect(TTAspects.TERRA, 5)
                .aspect(TTAspects.ORDO, 5)
                .pattern("SSS")
                .pattern("IRI")
                .pattern("BBB")
                .define('S', TTItems.STONE_ARCANE)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.BEACON)
                .define('B', TTItems.STONE_ARCANE_BRICK)
                .unlockedBy("has", has(Items.BEACON))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.ARCANE_KEY_IRON.get(), 2), 10)
                .aspect(TTAspects.AQUA, 5)
                .aspect(TTAspects.ORDO, 5)
                .pattern("NNI")
                .pattern("N  ")
                .define('N', Items.IRON_NUGGET)
                .define('I', Items.IRON_INGOT)
                .gate(wardedArcanaGate)
                .unlockedBy("has", has(Items.IRON_INGOT))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.ARCANE_KEY_GOLD.get(), 2), 10)
                .aspect(TTAspects.AQUA, 5)
                .aspect(TTAspects.ORDO, 5)
                .pattern("NNI")
                .pattern("N  ")
                .define('N', Items.GOLD_NUGGET)
                .define('I', Items.GOLD_INGOT)
                .gate(wardedArcanaGate)
                .unlockedBy("has", has(Items.GOLD_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.TALLOW_BLOCK)
                .pattern("TTT")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', TTItems.TALLOW)
                .unlockedBy("has", has(TTItems.TALLOW))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TTItems.ITEM_GRATE)
                .pattern("#")
                .pattern("H")
                .define('#', Items.IRON_BARS)
                .define('H', Items.HOPPER)
                .unlockedBy("has", has(Items.HOPPER))
                .save(output);
    }

    private void buildDecorRecipes() {
        ResearchGate artificeGate = gate("paving_stones");

        stairsRecipe(TTBlocks.STAIRS_GREATWOOD.get(), TTItemTags.PLANKS_GREATWOOD);
        stairsRecipe(TTBlocks.STAIRS_SILVERWOOD.get(), TTItemTags.PLANKS_SILVERWOOD);
        slabRecipe(TTBlocks.SLAB_GREATWOOD.get(), TTItemTags.PLANKS_GREATWOOD);
        slabRecipe(TTBlocks.SLAB_SILVERWOOD.get(), TTItemTags.PLANKS_SILVERWOOD);
        arcaneShaped(new ItemStack(TTItems.ARCANE_GRINDSTONE.get()), ARCANE_GRINDSTONE_VIS)
                .aspect(TTAspects.ORDO, 2)
                .aspect(TTAspects.PERDITIO, 2)
                .pattern(" T ")
                .pattern("TGT")
                .pattern(" T ")
                .define('T', TTItemTags.INGOTS_THAUMIUM)
                .define('G', Items.GRINDSTONE)
                .gate(gate("infusion_enchantment"))
                .unlockedBy("has", has(TTItemTags.INGOTS_THAUMIUM))
                .save(output);
        doorBuilder(TTBlocks.DOOR_GREATWOOD.get(), Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_door")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        trapdoorBuilder(TTBlocks.TRAPDOOR_GREATWOOD.get(), Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_trapdoor")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        fenceBuilder(TTBlocks.FENCE_GREATWOOD.get(), Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_fence")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        fenceGateBuilder(TTBlocks.FENCE_GATE_GREATWOOD.get(), Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_fence_gate")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        buttonBuilder(TTBlocks.BUTTON_GREATWOOD.get(), Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_button")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        pressurePlateBuilder(
                        RecipeCategory.REDSTONE,
                        TTBlocks.PRESSURE_PLATE_GREATWOOD.get(),
                        Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .group("wooden_pressure_plate")
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);
        doorBuilder(TTBlocks.DOOR_SILVERWOOD.get(), Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_door")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);
        trapdoorBuilder(TTBlocks.TRAPDOOR_SILVERWOOD.get(), Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_trapdoor")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);
        fenceBuilder(TTBlocks.FENCE_SILVERWOOD.get(), Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_fence")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);
        fenceGateBuilder(TTBlocks.FENCE_GATE_SILVERWOOD.get(), Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_fence_gate")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);
        buttonBuilder(TTBlocks.BUTTON_SILVERWOOD.get(), Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_button")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);
        pressurePlateBuilder(
                        RecipeCategory.REDSTONE,
                        TTBlocks.PRESSURE_PLATE_SILVERWOOD.get(),
                        Ingredient.of(TTItemTags.PLANKS_SILVERWOOD))
                .group("wooden_pressure_plate")
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TTItems.TABLE_WOOD)
                .pattern("SSS")
                .pattern("W W")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('W', ItemTags.PLANKS)
                .unlockedBy("has", has(ItemTags.WOODEN_SLABS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TTItems.TABLE_STONE)
                .pattern("SSS")
                .pattern("W W")
                .define('S', Items.STONE_SLAB)
                .define('W', Tags.Items.STONES)
                .unlockedBy("has", has(Items.STONE_SLAB))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.FLESH_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Items.ROTTEN_FLESH)
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ROTTEN_FLESH, 9)
                .requires(TTItems.FLESH_BLOCK)
                .unlockedBy("has", has(TTItems.FLESH_BLOCK))
                .save(output, TTIds.MODID + ":rotten_flesh_from_flesh_block");

        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(Tags.Items.OBSIDIANS_NORMAL),
                        RecipeCategory.BUILDING_BLOCKS,
                        TTItems.OBSIDIAN_TILE)
                .unlockedBy("has", has(Tags.Items.OBSIDIANS_NORMAL))
                .save(output, TTIds.MODID + ":obsidian_tile_from_obsidian_stonecutting");
        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(Tags.Items.OBSIDIANS_NORMAL),
                        RecipeCategory.BUILDING_BLOCKS,
                        TTItems.OBSIDIAN_TOTEM)
                .unlockedBy("has", has(Tags.Items.OBSIDIANS_NORMAL))
                .save(output, TTIds.MODID + ":obsidian_totem_from_obsidian_stonecutting");
        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(TTItems.OBSIDIAN_TILE), RecipeCategory.BUILDING_BLOCKS, TTItems.OBSIDIAN_TOTEM)
                .unlockedBy("has", has(TTItems.OBSIDIAN_TILE))
                .save(output, TTIds.MODID + ":obsidian_totem_from_obsidian_tile_stonecutting");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.AMBER_BRICK, 4)
                .pattern("##")
                .pattern("##")
                .define('#', TTItemTags.STORAGE_BLOCKS_AMBER)
                .unlockedBy("has", has(TTItemTags.STORAGE_BLOCKS_AMBER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TTItems.AMBER_BLOCK, 4)
                .pattern("##")
                .pattern("##")
                .define('#', TTItems.AMBER_BRICK)
                .unlockedBy("has", has(TTItems.AMBER_BRICK))
                .save(output, TTIds.MODID + ":amber_block_from_brick");

        arcaneShaped(new ItemStack(TTItems.PAVING_STONE_BARRIER.get(), 4), 50)
                .pattern("SAS")
                .pattern("SBS")
                .define('S', TTItems.STONE_ARCANE_BRICK)
                .define('A', crystal(TTAspects.IGNIS))
                .define('B', crystal(TTAspects.ORDO))
                .gate(artificeGate)
                .unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PAVING_STONE_TRAVEL.get(), 4), 50)
                .pattern("SAS")
                .pattern("SBS")
                .define('S', TTItems.STONE_ARCANE_BRICK)
                .define('A', crystal(TTAspects.AER))
                .define('B', crystal(TTAspects.TERRA))
                .gate(artificeGate)
                .unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK))
                .save(output);
    }

    private void stairsRecipe(Block result, Block base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern("K  ")
                .pattern("KK ")
                .pattern("KKK")
                .define('K', base)
                .unlockedBy("has", has(base))
                .save(output);
    }

    private void stairsRecipe(Block result, TagKey<Item> base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern("K  ")
                .pattern("KK ")
                .pattern("KKK")
                .define('K', base)
                .unlockedBy("has", has(base))
                .save(output);
    }

    private void pillarRecipe(Block result, Block base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 2)
                .pattern("K")
                .pattern("K")
                .define('K', base)
                .unlockedBy("has", has(base))
                .save(output);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, result, base, 1);
    }

    private void slabRecipe(Block result, TagKey<Item> base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 6)
                .pattern("KKK")
                .define('K', base)
                .unlockedBy("has", has(base))
                .save(output);
    }

    private void buildConstructRecipes() {
        arcaneShapeless(new ItemStack(TTItems.ACTIVATOR_RAIL.get()), 10)
                .requires(Items.ACTIVATOR_RAIL)
                .gate(gate("first_steps"))
                .unlockedBy("has", has(Items.ACTIVATOR_RAIL))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.TURRET_BASIC.get()), 100)
                .aspect(TTAspects.AER, 1)
                .pattern("BGI")
                .pattern("WMW")
                .pattern("S S")
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('I', TTItemTags.PLATES_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('M', TTItems.MIND_CLOCKWORK)
                .define('B', Tags.Items.TOOLS_BOW)
                .define('W', TTItemTags.PLANKS_GREATWOOD)
                .gate(gate("basic_turret"))
                .unlockedBy("has", has(TTItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.TURRET_ADVANCED.get()), 150)
                .aspect(TTAspects.AER, 2)
                .pattern("PMP")
                .pattern("PTP")
                .define('T', TTItems.TURRET_BASIC)
                .define('P', TTItemTags.PLATES_IRON)
                .define('M', TTItems.MIND_BIOTHAUMIC)
                .gate(gate("advanced_turret"))
                .unlockedBy("has", has(TTItems.MIND_BIOTHAUMIC))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TTItems.ARCANE_BORE.get()),
                        Ingredient.of(TTItems.TURRET_BASIC.get()))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .component(Ingredient.of(TTItems.MECHANISM_COMPLEX.get()))
                .component(Ingredient.of(TTItemTags.PLATES_BRASS))
                .component(Ingredient.of(Items.DIAMOND_PICKAXE))
                .component(Ingredient.of(Items.DIAMOND_SHOVEL))
                .component(Ingredient.of(TTItems.MORPHIC_RESONATOR.get()))
                .component(Ingredient.of(TTItems.RARE_EARTH.get()))
                .aspect(TTAspects.POTENTIA, 25)
                .aspect(TTAspects.TERRA, 25)
                .aspect(TTAspects.MACHINA, 100)
                .aspect(TTAspects.VACUOS, 25)
                .aspect(TTAspects.MOTUS, 25)
                .instability(4)
                .gate(gate("arcane_bore"))
                .unlockedBy("has", has(TTItems.TURRET_BASIC))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GRAPPLE_GUN_TIP.get()), 25)
                .aspect(TTAspects.TERRA, 1)
                .pattern("BRB")
                .pattern("RHR")
                .pattern("BRB")
                .define('B', TTItemTags.PLATES_BRASS)
                .define('R', TTItems.RARE_EARTH)
                .define('H', Items.TRIPWIRE_HOOK)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TTItems.RARE_EARTH))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GRAPPLE_GUN_SPOOL.get()), 25)
                .aspect(TTAspects.AQUA, 1)
                .pattern("SHS")
                .pattern("SGS")
                .pattern("SSS")
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('S', Tags.Items.STRINGS)
                .define('H', Items.TRIPWIRE_HOOK)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GRAPPLE_GUN.get()), 75)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.IGNIS, 1)
                .pattern("  S")
                .pattern("TII")
                .pattern(" BW")
                .define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON)
                .define('T', TTItems.GRAPPLE_GUN_TIP)
                .define('W', ItemTags.PLANKS)
                .define('S', TTItems.GRAPPLE_GUN_SPOOL)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TTItems.GRAPPLE_GUN_TIP))
                .save(output);
    }

    private void buildFocalManipulatorRecipe() {
        arcaneShaped(new ItemStack(TTItems.FOCAL_MANIPULATOR.get()), 100)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("ISI")
                .pattern("BRB")
                .pattern("GTG")
                .define('I', TTItemTags.PLATES_IRON)
                .define('S', TTItems.SLAB_ARCANE_STONE)
                .define('B', TTItems.STONE_ARCANE)
                .define('R', TTItems.VIS_RESONATOR)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('T', TTItems.TABLE_STONE)
                .gate(gate("base_auromancy", 1))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);
    }

    private void buildInfusionAltarRecipes() {
        arcaneShaped(new ItemStack(TTItems.INFUSION_MATRIX.get()), 150)
                .allAspects()
                .pattern("S S")
                .pattern(" N ")
                .pattern("S S")
                .define('S', TTItems.STONE_ARCANE_BRICK)
                .define('N', TTItemTags.NITORS)
                .gate(gate("infusion", 1))
                .unlockedBy("has", has(TTItems.STONE_ARCANE_BRICK))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PEDESTAL_ARCANE.get()), 10)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TTItems.SLAB_ARCANE_STONE)
                .define('B', TTItems.STONE_ARCANE)
                .gate(gate("infusion"))
                .unlockedBy("has", has(TTItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PEDESTAL_ANCIENT.get()), 150)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TTItems.SLAB_ANCIENT)
                .define('B', TTItems.STONE_ANCIENT)
                .gate(gate("infusion_ancient"))
                .unlockedBy("has", has(TTItems.STONE_ANCIENT))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PEDESTAL_ELDRITCH.get()), 150)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TTItems.SLAB_ELDRITCH)
                .define('B', TTItems.STONE_ELDRITCH_TILE)
                .gate(gate("infusion_eldritch"))
                .unlockedBy("has", has(TTItems.STONE_ELDRITCH_TILE))
                .save(output);
    }

    private void buildElementalToolRecipes() {
        ResearchGate gate = gate("elemental_tools");
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TTItems.ELEMENTAL_AXE.get(),
                                Map.of(InfusionEnchantment.COLLECTOR, 1, InfusionEnchantment.BURROWING, 1)),
                        Ingredient.of(TTItems.THAUMIUM_AXE.get()))
                .component(crystal(TTAspects.AQUA))
                .component(crystal(TTAspects.AQUA))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .aspect(TTAspects.AQUA, 60)
                .aspect(TTAspects.HERBA, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TTItems.THAUMIUM_AXE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TTItems.ELEMENTAL_PICKAXE.get(),
                                Map.of(InfusionEnchantment.REFINING, 1, InfusionEnchantment.SOUNDING, 2)),
                        Ingredient.of(TTItems.THAUMIUM_PICKAXE.get()))
                .component(crystal(TTAspects.IGNIS))
                .component(crystal(TTAspects.IGNIS))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .aspect(TTAspects.IGNIS, 30)
                .aspect(TTAspects.METALLUM, 30)
                .aspect(TTAspects.SENSUS, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TTItems.THAUMIUM_PICKAXE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.COMBAT,
                        enchantedTool(TTItems.ELEMENTAL_SWORD.get(), Map.of(InfusionEnchantment.ARCING, 2)),
                        Ingredient.of(TTItems.THAUMIUM_SWORD.get()))
                .component(crystal(TTAspects.AER))
                .component(crystal(TTAspects.AER))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .aspect(TTAspects.AER, 30)
                .aspect(TTAspects.MOTUS, 30)
                .aspect(TTAspects.AVERSIO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TTItems.THAUMIUM_SWORD))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(TTItems.ELEMENTAL_SHOVEL.get(), Map.of(InfusionEnchantment.DESTRUCTIVE, 1)),
                        Ingredient.of(TTItems.THAUMIUM_SHOVEL.get()))
                .component(crystal(TTAspects.TERRA))
                .component(crystal(TTAspects.TERRA))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .aspect(TTAspects.TERRA, 60)
                .aspect(TTAspects.FABRICO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TTItems.THAUMIUM_SHOVEL))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TTItems.ELEMENTAL_HOE.get()),
                        Ingredient.of(TTItems.THAUMIUM_HOE.get()))
                .component(crystal(TTAspects.ORDO))
                .component(crystal(TTAspects.PERDITIO))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                .component(Ingredient.of(TTItemTags.PLANKS_GREATWOOD))
                .aspect(TTAspects.ORDO, 30)
                .aspect(TTAspects.HERBA, 30)
                .aspect(TTAspects.PERDITIO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TTItems.THAUMIUM_HOE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TTItems.PRIMAL_CRUSHER.get(),
                                Map.of(InfusionEnchantment.DESTRUCTIVE, 1, InfusionEnchantment.REFINING, 1)),
                        Ingredient.of(TTItems.PRIMORDIAL_PEARL.get()))
                .component(Ingredient.of(TTItems.VOID_PICKAXE.get()))
                .component(Ingredient.of(TTItems.VOID_SHOVEL.get()))
                .component(Ingredient.of(TTItems.ELEMENTAL_PICKAXE.get()))
                .component(Ingredient.of(TTItems.ELEMENTAL_SHOVEL.get()))
                .aspect(TTAspects.TERRA, 75)
                .aspect(TTAspects.INSTRUMENTUM, 75)
                .aspect(TTAspects.PERDITIO, 50)
                .aspect(TTAspects.VACUOS, 50)
                .aspect(TTAspects.AVERSIO, 50)
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.DESIDERIUM, 50)
                .instability(6)
                .gate(gate("primal_crusher"))
                .unlockedBy("has", has(TTItems.PRIMORDIAL_PEARL))
                .save(output);
    }

    private void buildRechargePedestalRecipe() {
        arcaneShaped(new ItemStack(TTItems.RECHARGE_PEDESTAL.get()), 100)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern(" R ")
                .pattern("DID")
                .pattern("SSS")
                .define('R', TTItems.VIS_RESONATOR)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('S', Tags.Items.STONES)
                .gate(gate("recharge_pedestal"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);
    }

    private void buildTravellerBootsRecipe() {
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.TRAVELLER_BOOTS.get()),
                        Ingredient.of(Items.LEATHER_BOOTS))
                .component(crystal(TTAspects.AER))
                .component(crystal(TTAspects.AER))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(Tags.Items.FEATHERS))
                .component(Ingredient.of(ItemTags.FISHES))
                .aspect(TTAspects.VOLATUS, 100)
                .aspect(TTAspects.MOTUS, 100)
                .instability(1)
                .gate(gate("boots_traveller"))
                .unlockedBy("has", has(Items.LEATHER_BOOTS))
                .save(output);
    }

    private static ItemStack enchantedTool(Item item, Map<InfusionEnchantment, Integer> enchantments) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(TTDataComponents.INFUSION_ENCHANTMENTS.get(), new InfusionEnchantments(enchantments))
                .build();
        return new ItemStack(item.builtInRegistryHolder(), 1, patch);
    }

    private void buildRunicAugmentRecipe() {
        HolderGetter<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList baseAspects = AspectList.of(
                new AspectInstance(aspects.getOrThrow(TTAspects.PRAEMUNIO), 40),
                new AspectInstance(aspects.getOrThrow(TTAspects.VITREUS), 20),
                new AspectInstance(aspects.getOrThrow(TTAspects.POTENTIA), 20));
        Ingredient amber = Ingredient.of(TTItemTags.GEMS_AMBER);
        InfusionRunicAugmentRecipe recipe = new InfusionRunicAugmentRecipe(
                List.of(Ingredient.of(TTItems.SALIS_MUNDUS.get()), amber),
                amber,
                baseAspects,
                Ingredient.of(Items.IRON_CHESTPLATE),
                Optional.of(gate("runic_shielding")));
        output.accept(TTIds.rl("runic_augment/runic_shielding"), recipe, null);
    }

    private void buildThaumostaticHarnessRecipe() {
        Ingredient airCrystal = crystal(TTAspects.AER);
        Ingredient greatwoodPlanks = Ingredient.of(TTItemTags.PLANKS_GREATWOOD);
        Ingredient gold = Ingredient.of(Tags.Items.INGOTS_GOLD);
        Ingredient iron = Ingredient.of(Tags.Items.INGOTS_IRON);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.THAUMOSTATIC_HARNESS.get()),
                        Ingredient.of(Items.LEATHER_CHESTPLATE))
                .component(airCrystal)
                .component(airCrystal)
                .component(greatwoodPlanks)
                .component(greatwoodPlanks)
                .component(Ingredient.of(Items.COMPARATOR))
                .component(gold)
                .component(gold)
                .component(iron)
                .component(iron)
                .aspect(TTAspects.VOLATUS, 32)
                .aspect(TTAspects.POTENTIA, 32)
                .aspect(TTAspects.MACHINA, 32)
                .aspect(TTAspects.MOTUS, 16)
                .instability(6)
                .gate(gate("thaumostatic_harness"))
                .unlockedBy("has", has(TTItems.TRAVELLER_BOOTS))
                .save(output);
    }

    private void buildInfusionEnchantmentRecipes() {
        infusionEnchantment(InfusionEnchantment.BURROWING, Items.WOODEN_PICKAXE, Ingredient.of(Items.RABBIT_FOOT))
                .aspect(TTAspects.SENSUS, 80)
                .aspect(TTAspects.TERRA, 150)
                .save(output);
        infusionEnchantment(InfusionEnchantment.COLLECTOR, Items.STONE_AXE, Ingredient.of(Items.LEAD))
                .aspect(TTAspects.DESIDERIUM, 80)
                .aspect(TTAspects.AQUA, 100)
                .save(output);
        infusionEnchantment(InfusionEnchantment.DESTRUCTIVE, Items.STONE_PICKAXE, Ingredient.of(Items.TNT))
                .aspect(TTAspects.AVERSIO, 200)
                .aspect(TTAspects.PERDITIO, 250)
                .save(output);
        infusionEnchantment(InfusionEnchantment.REFINING, Items.IRON_PICKAXE, Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.ORDO, 80)
                .aspect(TTAspects.PERMUTATIO, 60)
                .save(output);
        infusionEnchantment(InfusionEnchantment.SOUNDING, Items.GOLDEN_PICKAXE, Ingredient.of(Items.MAP))
                .aspect(TTAspects.SENSUS, 40)
                .aspect(TTAspects.IGNIS, 60)
                .save(output);
        infusionEnchantment(
                        InfusionEnchantment.ARCING,
                        Items.WOODEN_SWORD,
                        Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .aspect(TTAspects.POTENTIA, 40)
                .aspect(TTAspects.AER, 60)
                .save(output);
        infusionEnchantment(
                        InfusionEnchantment.ESSENCE, Items.STONE_SWORD, Ingredient.of(TTItems.ESSENTIA_CRYSTAL.get()))
                .aspect(TTAspects.BESTIA, 40)
                .aspect(TTAspects.VITIUM, 60)
                .save(output);
        infusionEnchantment(InfusionEnchantment.LAMPLIGHT, Items.GOLDEN_PICKAXE, Ingredient.of(TTItemTags.NITORS))
                .aspect(TTAspects.LUX, 80)
                .aspect(TTAspects.AER, 20)
                .save(output);
    }

    private InfusionEnchantmentRecipeBuilder infusionEnchantment(
            InfusionEnchantment enchantment, Item displayCatalyst, Ingredient signature) {
        return new InfusionEnchantmentRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY), enchantment, Ingredient.of(displayCatalyst))
                .component(Ingredient.of(Items.ENCHANTED_BOOK))
                .component(signature)
                .gate(gate("infusion_enchantment"));
    }

    private void buildGearRecipes() {
        toolRecipes(
                "thaumium",
                TTItemTags.INGOTS_THAUMIUM,
                TTItems.THAUMIUM_SWORD.get(),
                TTItems.THAUMIUM_PICKAXE.get(),
                TTItems.THAUMIUM_AXE.get(),
                TTItems.THAUMIUM_SHOVEL.get(),
                TTItems.THAUMIUM_HOE.get());
        toolRecipes(
                "void",
                TTItemTags.INGOTS_VOID_METAL,
                TTItems.VOID_SWORD.get(),
                TTItems.VOID_PICKAXE.get(),
                TTItems.VOID_AXE.get(),
                TTItems.VOID_SHOVEL.get(),
                TTItems.VOID_HOE.get());
        armorRecipes(
                "thaumium",
                TTItemTags.INGOTS_THAUMIUM,
                TTItems.THAUMIUM_HELM.get(),
                TTItems.THAUMIUM_CHEST.get(),
                TTItems.THAUMIUM_LEGS.get(),
                TTItems.THAUMIUM_BOOTS.get());
        armorRecipes(
                "void",
                TTItemTags.INGOTS_VOID_METAL,
                TTItems.VOID_HELM.get(),
                TTItems.VOID_CHEST.get(),
                TTItems.VOID_LEGS.get(),
                TTItems.VOID_BOOTS.get());
    }

    private void toolRecipes(
            String name, TagKey<Item> ingot, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, sword)
                .pattern("I")
                .pattern("I")
                .pattern("S")
                .define('I', ingot)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pickaxe)
                .pattern("III")
                .pattern(" S ")
                .pattern(" S ")
                .define('I', ingot)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, axe)
                .pattern("II")
                .pattern("IS")
                .pattern(" S")
                .define('I', ingot)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, shovel)
                .pattern("I")
                .pattern("S")
                .pattern("S")
                .define('I', ingot)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hoe)
                .pattern("II")
                .pattern(" S")
                .pattern(" S")
                .define('I', ingot)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
    }

    private void armorRecipes(String name, TagKey<Item> ingot, Item helm, Item chest, Item legs, Item boots) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helm)
                .pattern("III")
                .pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chest)
                .pattern("I I")
                .pattern("III")
                .pattern("III")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, legs)
                .pattern("III")
                .pattern("I I")
                .pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
                .pattern("I I")
                .pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(output);
    }

    private void buildBannerRecipes() {
        for (DyeColor dye : DyeColor.values()) {
            arcaneShaped(new ItemStack(TTItems.BANNERS.get(dye).get()), 10)
                    .pattern("WS")
                    .pattern("WS")
                    .pattern("WB")
                    .define('W', wool(dye))
                    .define('S', Tags.Items.RODS_WOODEN)
                    .define('B', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_wool", has(ItemTags.WOOL))
                    .save(output, TTIds.MODID + ":arcane/banner_" + dye.getName());
        }
    }

    private static Item wool(DyeColor dye) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(dye.getName() + "_wool"));
    }

    private static TagKey<Item> dyeTag(DyeColor dye) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dyes/" + dye.getName()));
    }

    private void buildIngredientRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        arcaneShapeless(new ItemStack(TTItems.INLAY.get(), 2), 25)
                .aspect(TTAspects.AQUA, 1)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Tags.Items.INGOTS_GOLD)
                .gate(gate("infusion_stable"))
                .unlockedBy("has", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PATTERN_CRAFTER.get()), 50)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern("VH ")
                .pattern("GCG")
                .pattern(" W ")
                .define('H', Items.HOPPER)
                .define('W', TTItemTags.PLANKS_GREATWOOD)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('V', TTItems.VIS_RESONATOR)
                .define('C', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
                .gate(gate("arcane_pattern_crafter"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TTItems.SCRIBING_TOOLS)
                .requires(TTItems.SCRIBING_TOOLS)
                .requires(Tags.Items.DYES_BLACK)
                .unlockedBy("has", has(TTItems.SCRIBING_TOOLS))
                .save(output, "thaumaturge:scribing_tools_refill");

        arcaneShaped(new ItemStack(TTBlocks.DECONSTRUCTION_TABLE.asItem()), 20)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern(" S ")
                .pattern("ATP")
                .define('S', TTItems.THAUMOMETER)
                .define('T', TTBlocks.TABLE_WOOD.asItem())
                .define('A', Items.GOLDEN_AXE)
                .define('P', Items.GOLDEN_PICKAXE)
                .gate(gate("deconstructor"))
                .unlockedBy("has", has(TTItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.POTION_SPRAYER.get()), 75)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.IGNIS, 1)
                .pattern("BDB")
                .pattern("IAI")
                .pattern("ICI")
                .define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON)
                .define('A', Items.BREWING_STAND)
                .define('D', Items.DISPENSER)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("potion_sprayer"))
                .unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SPA.get()), 50)
                .aspect(TTAspects.AQUA)
                .pattern("QIQ")
                .pattern("SJS")
                .pattern("SPS")
                .define('Q', Items.QUARTZ_BLOCK)
                .define('I', Items.IRON_BARS)
                .define('S', TTItems.STONE_ARCANE)
                .define('J', TTItems.JAR_NORMAL)
                .define('P', TTItems.MECHANISM_SIMPLE)
                .gate(gate("arcane_spa"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.FABRIC.get()), 5)
                .pattern(" S ")
                .pattern("SCS")
                .pattern(" S ")
                .define('S', Tags.Items.STRINGS)
                .define('C', ItemTags.WOOL)
                .gate(gate("unlock_infusion"))
                .unlockedBy("has", has(Tags.Items.STRINGS))
                .save(output);

        clothRecipe(TTItems.CLOTH_CHEST.get(), "I I", "III", "III");
        clothRecipe(TTItems.CLOTH_LEGS.get(), "III", "I I", "I I");
        clothRecipe(TTItems.CLOTH_BOOTS.get(), "I I", "I I", null);

        arcaneShaped(new ItemStack(TTItems.MECHANISM_SIMPLE.get()), 10)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.AQUA)
                .pattern(" B ")
                .pattern("ISI")
                .pattern(" B ")
                .define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MECHANISM_COMPLEX.get()), 50)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.AQUA)
                .pattern(" M ")
                .pattern("TQT")
                .pattern(" M ")
                .define('T', TTItemTags.PLATES_THAUMIUM)
                .define('Q', Items.PISTON)
                .define('M', TTItems.MECHANISM_SIMPLE)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.MIRRORED_GLASS.get()), 50)
                .aspect(TTAspects.AQUA)
                .aspect(TTAspects.ORDO)
                .requires(TTItemTags.GEMS_QUICKSILVER)
                .requires(Tags.Items.GLASS_PANES)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TTItemTags.GEMS_QUICKSILVER))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.FILTER.get(), 2), 15)
                .aspect(TTAspects.AQUA)
                .pattern("GWG")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('W', TTItemTags.PLANKS_SILVERWOOD)
                .gate(gate("base_alchemy"))
                .unlockedBy("has", has(TTItemTags.PLANKS_SILVERWOOD))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MORPHIC_RESONATOR.get()), 50)
                .aspect(TTAspects.AER)
                .aspect(TTAspects.IGNIS)
                .pattern(" G ")
                .pattern("BSB")
                .pattern(" G ")
                .define('G', Tags.Items.GLASS_PANES)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('S', TTItemTags.NUGGETS_QUICKSILVER)
                .gate(gate("base_alchemy"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.BOTTLE_TAINT.get()),
                        DataComponentIngredient.of(
                                false,
                                TTDataComponents.ASPECTS.get(),
                                AspectList.of(new AspectInstance(
                                        aspects.getOrThrow(TTAspects.VITIUM), PhialItem.BASE_AMOUNT)),
                                TTItems.PHIAL.get()))
                .aspect(TTAspects.VITIUM, 30)
                .aspect(TTAspects.AQUA, 30)
                .gate(gate("bottle_taint"))
                .unlockedBy("has", has(TTItems.PHIAL.get()))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.ETHEREAL_BLOOM.get()),
                        Ingredient.of(TTItems.PLANT_SHIMMERLEAF.get()))
                .aspect(TTAspects.LUX, 8)
                .aspect(TTAspects.HERBA, 16)
                .aspect(TTAspects.VICTUS, 16)
                .aspect(TTAspects.VITIUM, 16)
                .gate(gate("ethereal_bloom"))
                .unlockedBy("has", has(TTItems.PLANT_SHIMMERLEAF.get()))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.BATH_SALTS.get()),
                        Ingredient.of(TTItems.SALIS_MUNDUS))
                .aspect(TTAspects.COGNITIO, 40)
                .aspect(TTAspects.AER, 40)
                .aspect(TTAspects.ORDO, 40)
                .aspect(TTAspects.VICTUS, 40)
                .gate(gate("bath_salts"))
                .unlockedBy("has", has(TTItems.SALIS_MUNDUS))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.SANITY_SOAP.get()),
                        Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TTAspects.COGNITIO, 75)
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.ORDO, 75)
                .aspect(TTAspects.VICTUS, 50)
                .gate(gate("sane_soap"))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, TTItems.TRIPLE_MEAT_TREAT)
                .requires(TTItemTags.MEAT_CHUNKS)
                .requires(TTItemTags.MEAT_CHUNKS)
                .requires(TTItemTags.MEAT_CHUNKS)
                .requires(Items.SUGAR)
                .unlockedBy("has", has(TTItemTags.MEAT_CHUNKS))
                .save(output);
    }

    private void clothRecipe(Item result, String row1, String row2, String row3) {
        ArcaneWorkbenchShapedRecipeBuilder builder =
                arcaneShaped(new ItemStack(result), 100).pattern(row1).pattern(row2);
        if (row3 != null) {
            builder.pattern(row3);
        }
        builder.define('I', TTItems.FABRIC)
                .gate(gate("unlock_infusion"))
                .unlockedBy("has", has(TTItems.FABRIC))
                .save(output);
    }

    private void buildCrystalClusterRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        crystalCluster(aspects, TTItems.CRYSTAL_AER, TTAspects.AER, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_IGNIS, TTAspects.IGNIS, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_AQUA, TTAspects.AQUA, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_TERRA, TTAspects.TERRA, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_ORDO, TTAspects.ORDO, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_PERDITIO, TTAspects.PERDITIO, 0);
        crystalCluster(aspects, TTItems.CRYSTAL_VITIUM, TTAspects.VITIUM, 4);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.ELDRITCH_EYE.get()),
                        Ingredient.of(Items.ENDER_EYE))
                .component(Ingredient.of(TTItems.VOID_SEED.get()))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .aspect(TTAspects.ALIENIS, 64)
                .aspect(TTAspects.VACUOS, 16)
                .aspect(TTAspects.TENEBRAE, 16)
                .aspect(TTAspects.MOTUS, 16)
                .instability(5)
                .gate(gate("oculus"))
                .unlockedBy("has", has(Items.ENDER_EYE))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.CAUSALITY_COLLAPSER.get()),
                        Ingredient.of(Items.TNT))
                .component(Ingredient.of(TTItems.MORPHIC_RESONATOR.get()))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .component(Ingredient.of(TTItems.ALUMENTUM.get()))
                .component(Ingredient.of(TTItemTags.NITORS))
                .component(Ingredient.of(TTItems.VIS_RESONATOR.get()))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .component(Ingredient.of(TTItems.ALUMENTUM.get()))
                .component(Ingredient.of(TTItemTags.NITORS))
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.VITIUM, 50)
                .instability(8)
                .gate(gate("rift_closer"))
                .unlockedBy("has", has(TTItems.MORPHIC_RESONATOR))
                .save(output);
    }

    private void crystalCluster(
            HolderLookup<IAspect> aspects, ItemLike cluster, ResourceKey<IAspect> aspect, int instability) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(cluster.asItem()), crystal(aspect))
                .component(Ingredient.of(Tags.Items.SEEDS_WHEAT))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(aspect, 10)
                .aspect(TTAspects.VITREUS, 10)
                .aspect(TTAspects.VINCULUM, 5)
                .instability(instability)
                .gate(gate("crystal_farmer"))
                .unlockedBy("has", has(TTItems.SALIS_MUNDUS))
                .save(output);
    }

    private void buildFocusRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.FOCUS_1.get()),
                        DataComponentIngredient.of(
                                false,
                                TTDataComponents.CRYSTAL_ASPECT.get(),
                                new AspectInstance(aspects.getOrThrow(TTAspects.ORDO), 1),
                                TTItems.ESSENTIA_CRYSTAL.get()))
                .gate(gate("unlock_auromancy"))
                .aspect(TTAspects.VITREUS, 20)
                .aspect(TTAspects.PRAECANTATIO, 10)
                .aspect(TTAspects.AURAM, 5)
                .unlockedBy("has", has(TTItems.ESSENTIA_CRYSTAL.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.FOCUS_2.get()),
                        Ingredient.of(TTItems.FOCUS_1.get()))
                .component(Ingredient.of(TTItemTags.GEMS_QUICKSILVER))
                .component(Ingredient.of(Tags.Items.GEMS_DIAMOND))
                .component(Ingredient.of(TTItemTags.GEMS_QUICKSILVER))
                .component(Ingredient.of(Tags.Items.ENDER_PEARLS))
                .aspect(TTAspects.PRAECANTATIO, 25)
                .aspect(TTAspects.ORDO, 50)
                .instability(3)
                .gate(gate("focus_advanced", 0))
                .unlockedBy("has", has(TTItems.FOCUS_1.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.FOCUS_3.get()),
                        Ingredient.of(TTItems.FOCUS_2.get()))
                .component(Ingredient.of(TTItemTags.GEMS_QUICKSILVER))
                .component(Ingredient.of(TTItems.PRIMORDIAL_PEARL.get()))
                .component(Ingredient.of(TTItemTags.GEMS_QUICKSILVER))
                .component(Ingredient.of(Tags.Items.NETHER_STARS))
                .aspect(TTAspects.PRAECANTATIO, 25)
                .aspect(TTAspects.ORDO, 50)
                .aspect(TTAspects.VACUOS, 100)
                .instability(5)
                .gate(gate("focus_greater", 0))
                .unlockedBy("has", has(TTItems.FOCUS_2.get()))
                .save(output);
    }

    private void buildCrucibleRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.TALLOW.get()),
                        Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TTAspects.IGNIS, 1)
                .gate(gate("hedge_alchemy", 0))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.LEATHER), Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TTAspects.AER, 3)
                .aspect(TTAspects.BESTIA, 3)
                .gate(gate("hedge_alchemy", 0))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output, TTIds.MODID + ":crucible/leather");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.GUNPOWDER, 2),
                        Ingredient.of(Tags.Items.GUNPOWDERS))
                .aspect(TTAspects.IGNIS, 10)
                .aspect(TTAspects.PERDITIO, 10)
                .aspect(TTAspects.ALKIMIA, 5)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Tags.Items.GUNPOWDERS))
                .save(output, TTIds.MODID + ":crucible/gunpowder");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.SLIME_BALL, 2),
                        Ingredient.of(Tags.Items.SLIME_BALLS))
                .aspect(TTAspects.AQUA, 5)
                .aspect(TTAspects.VICTUS, 5)
                .aspect(TTAspects.ALKIMIA, 1)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Tags.Items.SLIME_BALLS))
                .save(output, TTIds.MODID + ":crucible/slime_ball");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.GLOWSTONE_DUST, 2),
                        Ingredient.of(Tags.Items.DUSTS_GLOWSTONE))
                .aspect(TTAspects.SENSUS, 5)
                .aspect(TTAspects.LUX, 10)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Tags.Items.DUSTS_GLOWSTONE))
                .save(output, TTIds.MODID + ":crucible/glowstone_dust");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.INK_SAC, 2), Ingredient.of(Items.INK_SAC))
                .aspect(TTAspects.AQUA, 2)
                .aspect(TTAspects.BESTIA, 2)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.INK_SAC))
                .save(output, TTIds.MODID + ":crucible/dye");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.CLAY_BALL), Ingredient.of(ItemTags.DIRT))
                .aspect(TTAspects.AQUA, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(ItemTags.DIRT))
                .save(output, TTIds.MODID + ":crucible/clay_ball");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.STRING),
                        Ingredient.of(Tags.Items.CROPS_WHEAT))
                .aspect(TTAspects.BESTIA, 5)
                .aspect(TTAspects.FABRICO, 1)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Tags.Items.CROPS_WHEAT))
                .save(output, TTIds.MODID + ":crucible/string");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.COBWEB), Ingredient.of(Tags.Items.STRINGS))
                .aspect(TTAspects.VINCULUM, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Tags.Items.STRINGS))
                .save(output, TTIds.MODID + ":crucible/cobweb");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Blocks.MOSSY_COBBLESTONE),
                        Ingredient.of(Blocks.COBBLESTONE))
                .aspect(TTAspects.HERBA, 2)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Blocks.COBBLESTONE))
                .save(output, TTIds.MODID + ":crucible/mossy_cobblestone");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Blocks.MOSSY_STONE_BRICKS),
                        Ingredient.of(Blocks.STONE_BRICKS))
                .aspect(TTAspects.HERBA, 2)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Blocks.STONE_BRICKS))
                .save(output, TTIds.MODID + ":crucible/mossy_stone_bricks");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Blocks.ICE), Ingredient.of(Blocks.SNOW_BLOCK))
                .aspect(TTAspects.ORDO, 1)
                .aspect(TTAspects.GELUM, 1)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Blocks.SNOW_BLOCK))
                .save(output, TTIds.MODID + ":crucible/ice");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.BONE_MEAL, 4), Ingredient.of(Items.BONE))
                .aspect(TTAspects.PERDITIO, 1)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.BONE))
                .save(output, TTIds.MODID + ":crucible/bone_meal");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.LAVA_BUCKET), Ingredient.of(Items.BUCKET))
                .aspect(TTAspects.IGNIS, 15)
                .aspect(TTAspects.TERRA, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Tags.Items.BUCKETS_EMPTY))
                .save(output, TTIds.MODID + ":crucible/lava_bucket");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.BUCKET_LIQUID_DEATH.get()),
                        Ingredient.of(Tags.Items.BUCKETS_EMPTY))
                .aspect(TTAspects.MORTUUS, 100)
                .aspect(TTAspects.PERDITIO, 50)
                .aspect(TTAspects.ALKIMIA, 20)
                .gate(gate("liquid_death", 0))
                .unlockedBy("has", has(Tags.Items.BUCKETS_EMPTY))
                .save(output, TTIds.MODID + ":crucible/liquid_death");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.INGOT_BRASS.get()),
                        Ingredient.of(Tags.Items.INGOTS_COPPER))
                .aspect(TTAspects.INSTRUMENTUM, 5)
                .gate(gate("metallurgy", 0))
                .unlockedBy("has", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.INGOT_THAUMIUM.get()),
                        Ingredient.of(Tags.Items.INGOTS_IRON))
                .aspect(TTAspects.PRAECANTATIO, 5)
                .aspect(TTAspects.TERRA, 5)
                .gate(gate("metallurgy", 1))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.NITORS.get(DyeColor.YELLOW).get()),
                        Ingredient.of(Tags.Items.DUSTS_GLOWSTONE))
                .gate(gate("unlock_alchemy", 2))
                .aspect(TTAspects.POTENTIA, 10)
                .aspect(TTAspects.IGNIS, 10)
                .aspect(TTAspects.LUX, 10)
                .unlockedBy("has", has(Tags.Items.DUSTS_GLOWSTONE))
                .save(output);

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            new CrucibleRecipeBuilder(
                            aspects,
                            RecipeCategory.MISC,
                            new ItemStack(
                                    TTItems.ESSENTIA_CRYSTAL,
                                    1,
                                    DataComponentPatch.builder()
                                            .set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1))
                                            .build()),
                            Ingredient.of(TTItemTags.NUGGETS_QUARTZ))
                    .gate(gate("base_alchemy"))
                    .aspect(aspect, 2)
                    .unlockedBy("has", has(TTItemTags.NUGGETS_QUARTZ))
                    .save(
                            output,
                            TTIds.MODID + ":crucible/vis_crystal/"
                                    + aspect.getKey().location().getPath());
        });

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.INGOT_VOID.get()),
                        Ingredient.of(TTItems.VOID_SEED.get()))
                .gate(gate("base_eldritch"))
                .aspect(TTAspects.METALLUM, 10)
                .aspect(TTAspects.VITIUM, 5)
                .unlockedBy("has", has(TTItems.VOID_SEED.get()))
                .save(output, TTIds.MODID + ":crucible/void_ingot");

        clusterRecipe(TTItems.CLUSTER_IRON, Tags.Items.RAW_MATERIALS_IRON);
        clusterRecipe(TTItems.CLUSTER_GOLD, Tags.Items.RAW_MATERIALS_GOLD);
        clusterRecipe(TTItems.CLUSTER_COPPER, Tags.Items.RAW_MATERIALS_COPPER);
        clusterRecipe(TTItems.CLUSTER_TIN, TTItemTags.ORES_TIN);
        clusterRecipe(TTItems.CLUSTER_SILVER, TTItemTags.ORES_SILVER);
        clusterRecipe(TTItems.CLUSTER_LEAD, TTItemTags.ORES_LEAD);
        clusterRecipe(TTItems.CLUSTER_CINNABAR, TTItemTags.RAW_MATERIALS_CINNABAR);
        clusterRecipe(TTItems.CLUSTER_QUARTZ, Tags.Items.ORES_QUARTZ);

        transmutationRecipe("iron_nugget", Items.IRON_NUGGET, Tags.Items.NUGGETS_IRON, TTAspects.METALLUM);
        transmutationRecipe(
                "gold_nugget", Items.GOLD_NUGGET, Tags.Items.NUGGETS_GOLD, TTAspects.METALLUM, TTAspects.DESIDERIUM);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.ALUMENTUM.get()),
                        Ingredient.of(ItemTags.COALS))
                .gate(gate("alumentum"))
                .aspect(TTAspects.IGNIS, 10)
                .aspect(TTAspects.POTENTIA, 10)
                .aspect(TTAspects.PERDITIO, 5)
                .unlockedBy("has", has(ItemTags.COALS))
                .save(output);
    }

    private void clusterRecipe(ItemLike cluster, TagKey<Item> oreTag) {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        new CrucibleRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(cluster.asItem()), Ingredient.of(oreTag))
                .aspect(TTAspects.METALLUM, 5)
                .aspect(TTAspects.ORDO, 5)
                .gate(gate("metal_purification"))
                .unlockedBy("has", has(oreTag))
                .save(output.withConditions(new NotCondition(new TagEmptyCondition(oreTag))));
    }

    private void transmutationRecipe(
            String name, ItemLike result, TagKey<Item> catalyst, ResourceKey<IAspect>... costs) {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        CrucibleRecipeBuilder builder = new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(result, 3), Ingredient.of(catalyst))
                .aspect(TTAspects.METALLUM, 2)
                .gate(gate("metal_purification"));
        builder.unlockedBy("has", has(catalyst));
        for (ResourceKey<IAspect> cost : costs) {
            if (!cost.equals(TTAspects.METALLUM)) {
                builder.aspect(cost, 1);
            }
        }
        builder.save(
                output.withConditions(new NotCondition(new TagEmptyCondition(catalyst))),
                TTIds.MODID + ":crucible/" + name + "_transmutation");
    }

    private void buildArcaneWorkbenchRecipes() {
        arcaneShaped(new ItemStack(TTItems.THAUMOMETER.get()), 20)
                .allAspects()
                .pattern(" G ")
                .pattern("GPG")
                .pattern(" G ")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('P', Tags.Items.GLASS_PANES)
                .gate(gate("first_steps", 1))
                .unlockedBy("has", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.VIS_RESONATOR.get()), 50)
                .aspect(TTAspects.AER)
                .aspect(TTAspects.AQUA)
                .requires(TTItemTags.PLATES_IRON)
                .requires(Tags.Items.GEMS_QUARTZ)
                .gate(gate("unlock_auromancy", 1))
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ARCANE_WORKBENCH_CHARGER.get()), 200)
                .aspect(TTAspects.AER, 2)
                .aspect(TTAspects.ORDO, 2)
                .pattern(" R ")
                .pattern("P P")
                .pattern("I I")
                .define('R', TTItems.VIS_RESONATOR)
                .define('P', TTItemTags.PLANKS_GREATWOOD)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("workbench_charger"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOGGLES_REVEALING.get()), 50)
                .pattern("LBL")
                .pattern("L L")
                .pattern("MBM")
                .define('L', Tags.Items.LEATHERS)
                .define('B', TTItemTags.INGOTS_BRASS)
                .define('M', TTItems.THAUMOMETER)
                .gate(gate("unlock_artifice"))
                .unlockedBy("has", has(TTItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ALEMBIC.get()), 50)
                .aspect(TTAspects.AQUA)
                .pattern("GFG")
                .pattern("PBP")
                .pattern("GFG")
                .define('G', TTItemTags.PLANKS_GREATWOOD)
                .define('F', TTItems.FILTER)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('B', Tags.Items.BUCKETS_EMPTY)
                .gate(gate("essentia_smelter"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SMELTER_BASIC.get()), 50)
                .aspect(TTAspects.IGNIS)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', ItemTags.STONE_TOOL_MATERIALS)
                .define('F', Tags.Items.PLAYER_WORKSTATIONS_FURNACES)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('R', TTItems.CRUCIBLE)
                .gate(gate("essentia_smelter", 1))
                .unlockedBy("has", has(TTItems.CRUCIBLE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SMELTER_THAUMIUM.get()), 250)
                .aspect(TTAspects.IGNIS, 2)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TTItemTags.PLATES_THAUMIUM)
                .define('F', TTItems.ALCHEMICAL_CONSTRUCT)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('R', TTItems.SMELTER_BASIC)
                .gate(gate("essentia_smelter_thaumium"))
                .unlockedBy("has", has(TTItems.SMELTER_BASIC))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SMELTER_VOID.get()), 750)
                .aspect(TTAspects.IGNIS, 3)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TTItemTags.PLATES_VOID_METAL)
                .define('F', TTItems.ADVANCED_ALCHEMICAL_CONSTRUCT)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('R', TTItems.SMELTER_THAUMIUM)
                .gate(gate("essentia_smelter_void"))
                .unlockedBy("has", has(TTItems.SMELTER_THAUMIUM))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.JAR_NORMAL.get()), 5)
                .pattern("PRP")
                .pattern("P P")
                .pattern("PPP")
                .define('P', Tags.Items.GLASS_PANES)
                .define('R', ItemTags.WOODEN_SLABS)
                .gate(gate("warded_jars"))
                .unlockedBy("has", has(Tags.Items.GLASS_PANES))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.JAR_VOID.get()), 50)
                .aspect(TTAspects.PERDITIO)
                .requires(TTItems.JAR_NORMAL)
                .gate(gate("warded_jars"))
                .unlockedBy("has", has(TTItems.JAR_NORMAL))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.TUBE.get(), 8), 10)
                .pattern(" Q ")
                .pattern("PGP")
                .pattern(" B ")
                .define('Q', TTItemTags.NUGGETS_QUICKSILVER)
                .define('P', TTItemTags.PLATES_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', TTItemTags.NUGGETS_BRASS)
                .gate(gate("tubes"))
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.TUBE_RESTRICT.get()), 10)
                .aspect(TTAspects.TERRA)
                .requires(TTItems.TUBE)
                .requires(Tags.Items.STONES)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.TUBE_ONEWAY.get()), 10)
                .aspect(TTAspects.AQUA)
                .requires(TTItems.TUBE)
                .requires(Tags.Items.DYES_BLUE)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.TUBE_FILTER.get()), 10)
                .requires(TTItems.TUBE)
                .requires(TTItems.FILTER)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.TUBE_VALVE.get()), 10)
                .requires(TTItems.TUBE)
                .requires(Items.LEVER)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.TUBE_BUFFER.get()), 25)
                .pattern("PVP")
                .pattern("TIT")
                .pattern("PRP")
                .define('P', TTItems.PHIAL)
                .define('V', TTItems.TUBE_VALVE)
                .define('T', TTItems.TUBE)
                .define('I', TTItemTags.PLATES_IRON)
                .define('R', TTItems.TUBE_RESTRICT)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItems.TUBE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SMELTER_AUX.get()), 100)
                .aspect(TTAspects.AER)
                .aspect(TTAspects.TERRA)
                .pattern("PVP")
                .pattern("BCB")
                .pattern("ILI")
                .define('P', TTItemTags.PLANKS_GREATWOOD)
                .define('V', TTItems.TUBE_FILTER)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .define('L', TTItems.BELLOWS)
                .gate(gate("improved_smelting"))
                .unlockedBy("has", has(TTItems.BELLOWS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.SMELTER_VENT.get()), 150)
                .aspect(TTAspects.AER)
                .pattern("IBI")
                .pattern("FCF")
                .pattern("IBI")
                .define('F', TTItems.FILTER)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('I', TTItemTags.PLATES_IRON)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("improved_smelting_2"))
                .unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ALCHEMICAL_CONSTRUCT.get(), 2), 75)
                .aspect(TTAspects.AQUA)
                .aspect(TTAspects.PERDITIO)
                .aspect(TTAspects.ORDO)
                .pattern("IAI")
                .pattern("VPV")
                .pattern("IAI")
                .define('A', TTItems.TUBE_VALVE)
                .define('V', TTItems.TUBE)
                .define('I', TTItemTags.PLATES_IRON)
                .define('P', TTItemTags.PLANKS_GREATWOOD)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItemTags.PLATES_IRON))
                .save(output);

        // Retain both the total cost and each primal requirement
        // through the modern generic-vis and crystal payment model.
        arcaneShaped(new ItemStack(TTItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get(), 4), 50)
                .aspect(TTAspects.AQUA, 10)
                .aspect(TTAspects.ORDO, 30)
                .aspect(TTAspects.TERRA, 10)
                .pattern("VAV")
                .pattern("APA")
                .pattern("VAV")
                .define('A', TTItems.ALCHEMICAL_CONSTRUCT)
                .define('V', TTItems.INGOT_VOID)
                .define('P', TTItems.PRIMORDIAL_PEARL)
                .gate(gate("essentia_smelter_void", 0))
                .unlockedBy("has", has(TTItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.BELLOWS.get()), 25)
                .aspect(TTAspects.AER)
                .pattern("PP ")
                .pattern("LLI")
                .pattern("PP ")
                .define('P', ItemTags.PLANKS)
                .define('L', Tags.Items.LEATHERS)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("bellows"))
                .unlockedBy("has", has(Tags.Items.LEATHERS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.THAUMONOMICON_SHARING.get()), 500)
                .allAspects()
                .pattern(" B ")
                .pattern("MQM")
                .pattern(" B ")
                .define('B', TTItems.BRAIN)
                .define('M', TTItems.MIRROR)
                .define('Q', Items.WRITABLE_BOOK)
                .gate(gate("share_book", 1))
                .unlockedBy("has", has(TTItems.BRAIN))
                .save(output);
    }

    private Holder<IAspect> getAspect(ResourceKey<IAspect> key) {
        return registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private void buildGolemancyRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TTItems.GOLEM_BELL.get())
                .pattern(" QQ")
                .pattern(" QQ")
                .pattern("S  ")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('Q', Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOLEM_TOP_HAT.get()), 16)
                .aspect(TTAspects.ORDO, 1)
                .aspect(TTAspects.IGNIS, 1)
                .pattern(" C ")
                .pattern(" G ")
                .pattern("CCC")
                .define('C', Items.BLACK_WOOL)
                .define('G', Tags.Items.INGOTS_GOLD)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOLEM_FEZ.get()), 8)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.TERRA, 1)
                .pattern("CCS")
                .pattern("CCS")
                .pattern("  S")
                .define('C', Items.RED_WOOL)
                .define('S', Tags.Items.STRINGS)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOLEM_BOWTIE.get()), 8)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern("CSC")
                .pattern("C C")
                .define('C', Items.BLACK_WOOL)
                .define('S', Tags.Items.STRINGS)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOLEM_GLASSES.get()), 8)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("GIG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.GOLEM_VISOR.get()), 8)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("IHI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('H', Items.IRON_HELMET)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MIND_CLOCKWORK.get()), 25)
                .aspect(TTAspects.IGNIS, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern(" P ")
                .pattern("PGP")
                .pattern("BCB")
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('P', Tags.Items.GLASS_PANES)
                .define('C', Items.COMPARATOR)
                .gate(gate("mind_clockwork", 1))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.MIND_BIOTHAUMIC.get()),
                        Ingredient.of(TTItems.MIND_CLOCKWORK.get()))
                .component(Ingredient.of(TTItems.BRAIN.get()))
                .component(Ingredient.of(TTItems.MECHANISM_COMPLEX.get()))
                .aspect(TTAspects.COGNITIO, 50)
                .aspect(TTAspects.MACHINA, 25)
                .instability(4)
                .gate(gate("mind_biothaumic"))
                .unlockedBy("has", has(TTItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MODULE_VISION.get()), 50)
                .aspect(TTAspects.AQUA, 1)
                .pattern("B B")
                .pattern("E E")
                .pattern("PGP")
                .define('B', Items.GLASS_BOTTLE)
                .define('E', Items.FERMENTED_SPIDER_EYE)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .gate(gate("golem_vision"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MODULE_AGGRESSION.get()), 50)
                .aspect(TTAspects.IGNIS, 1)
                .pattern(" R ")
                .pattern("RTR")
                .pattern("PGP")
                .define('R', Tags.Items.GLASS_PANES)
                .define('T', Items.BLAZE_POWDER)
                .define('P', TTItemTags.PLATES_BRASS)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .gate(gate("seal_guard"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.LEVITATOR.get()), 35)
                .aspect(TTAspects.AER, 1)
                .pattern("WIW")
                .pattern("BNB")
                .pattern("WGW")
                .define('I', TTItemTags.PLATES_THAUMIUM)
                .define('N', TTItemTags.NITORS)
                .define('W', ItemTags.PLANKS)
                .define('B', TTItemTags.PLATES_IRON)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .gate(gate("levitator"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.SEAL_BLANK.get(), 3), 20)
                .aspect(TTAspects.AER, 1)
                .requires(Items.CLAY_BALL)
                .requires(TTItems.TALLOW.get())
                .requires(Tags.Items.DYES_RED)
                .requires(TTItemTags.NITORS)
                .gate(gate("control_seals"))
                .unlockedBy("has", has(TTItems.TALLOW))
                .save(output);

        sealCrucible(
                aspects,
                gate("seal_collect"),
                TTItems.SEAL_PICKUP,
                TTItems.SEAL_BLANK,
                builder -> builder.aspect(TTAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_collect"),
                TTItems.SEAL_PICKUP_ADVANCED,
                TTItems.SEAL_PICKUP,
                builder -> builder.aspect(TTAspects.SENSUS, 10).aspect(TTAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_store"),
                TTItems.SEAL_FILL,
                TTItems.SEAL_BLANK,
                builder -> builder.aspect(TTAspects.AVERSIO, 10));
        sealCrucible(
                aspects,
                gate("seal_store"),
                TTItems.SEAL_FILL_ADVANCED,
                TTItems.SEAL_FILL,
                builder -> builder.aspect(TTAspects.SENSUS, 10).aspect(TTAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_empty"),
                TTItems.SEAL_EMPTY,
                TTItems.SEAL_BLANK,
                builder -> builder.aspect(TTAspects.VACUOS, 10));
        sealCrucible(
                aspects,
                gate("seal_empty"),
                TTItems.SEAL_EMPTY_ADVANCED,
                TTItems.SEAL_EMPTY,
                builder -> builder.aspect(TTAspects.SENSUS, 10).aspect(TTAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_provide"),
                TTItems.SEAL_PROVIDER,
                TTItems.SEAL_EMPTY_ADVANCED,
                builder -> builder.aspect(TTAspects.PERMUTATIO, 10).aspect(TTAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_stock"),
                TTItems.SEAL_STOCK,
                TTItems.SEAL_FILL,
                builder -> builder.aspect(TTAspects.COGNITIO, 10).aspect(TTAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_guard"),
                TTItems.SEAL_GUARD,
                TTItems.SEAL_BLANK,
                builder -> builder.aspect(TTAspects.AVERSIO, 20).aspect(TTAspects.PRAEMUNIO, 20));
        sealCrucible(
                aspects,
                gate("seal_guard"),
                TTItems.SEAL_GUARD_ADVANCED,
                TTItems.SEAL_GUARD,
                builder -> builder.aspect(TTAspects.SENSUS, 20).aspect(TTAspects.COGNITIO, 20));
        sealCrucible(
                aspects,
                gate("seal_lumber"),
                TTItems.SEAL_LUMBER,
                TTItems.SEAL_BREAKER,
                builder -> builder.aspect(TTAspects.HERBA, 40).aspect(TTAspects.SENSUS, 20));
        sealCrucible(
                aspects,
                gate("seal_use"),
                TTItems.SEAL_USE,
                TTItems.SEAL_BLANK,
                builder -> builder.aspect(TTAspects.FABRICO, 20)
                        .aspect(TTAspects.SENSUS, 10)
                        .aspect(TTAspects.COGNITIO, 20));
        sealCrucible(
                aspects,
                gate("seal_break"),
                TTItems.SEAL_BREAKER_ADVANCED,
                TTItems.SEAL_BREAKER,
                builder -> builder.aspect(TTAspects.SENSUS, 10)
                        .aspect(TTAspects.COGNITIO, 10)
                        .aspect(TTAspects.INSTRUMENTUM, 20));

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.SEAL_HARVEST.get()),
                        Ingredient.of(TTItems.SEAL_BLANK.get()))
                .component(Ingredient.of(Tags.Items.SEEDS_WHEAT))
                .component(Ingredient.of(Tags.Items.SEEDS_PUMPKIN))
                .component(Ingredient.of(Tags.Items.SEEDS_MELON))
                .component(Ingredient.of(Tags.Items.SEEDS_BEETROOT))
                .component(Ingredient.of(Tags.Items.CROPS_SUGAR_CANE))
                .component(Ingredient.of(Tags.Items.CROPS_CACTUS))
                .aspect(TTAspects.HERBA, 10)
                .aspect(TTAspects.SENSUS, 10)
                .aspect(TTAspects.HUMANUS, 10)
                .instability(0)
                .gate(gate("seal_harvest"))
                .unlockedBy("has", has(TTItems.SEAL_BLANK))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.SEAL_BUTCHER.get()),
                        Ingredient.of(TTItems.SEAL_GUARD.get()))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .component(Ingredient.of(ItemTags.WOOL))
                .component(Ingredient.of(Items.RABBIT_HIDE))
                .component(Ingredient.of(Items.PORKCHOP))
                .component(Ingredient.of(Items.MUTTON))
                .component(Ingredient.of(Items.BEEF))
                .aspect(TTAspects.BESTIA, 10)
                .aspect(TTAspects.SENSUS, 10)
                .aspect(TTAspects.HUMANUS, 10)
                .instability(0)
                .gate(gate("seal_butcher"))
                .unlockedBy("has", has(TTItems.SEAL_GUARD))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.SEAL_BREAKER.get()),
                        Ingredient.of(TTItems.SEAL_BLANK.get()))
                .component(Ingredient.of(Items.GOLDEN_AXE))
                .component(Ingredient.of(Items.GOLDEN_PICKAXE))
                .component(Ingredient.of(Items.GOLDEN_SHOVEL))
                .aspect(TTAspects.INSTRUMENTUM, 10)
                .aspect(TTAspects.PERDITIO, 10)
                .aspect(TTAspects.HUMANUS, 10)
                .instability(1)
                .gate(gate("seal_break"))
                .unlockedBy("has", has(TTItems.SEAL_BLANK))
                .save(output);
    }

    private void sealCrucible(
            HolderLookup<IAspect> aspects,
            ResearchGate gate,
            DeferredItem<ItemSealPlacer> result,
            DeferredItem<ItemSealPlacer> catalyst,
            UnaryOperator<CrucibleRecipeBuilder> configure) {
        configure
                .apply(new CrucibleRecipeBuilder(
                                aspects,
                                RecipeCategory.MISC,
                                new ItemStack(result.get()),
                                Ingredient.of(catalyst.get()))
                        .gate(gate))
                .unlockedBy("has", has(catalyst))
                .save(output);
    }

    private void buildAuraDeviceRecipes() {

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.DECORATIONS,
                        new ItemStack(TTItems.MIRROR.get()),
                        Ingredient.of(TTItems.MIRRORED_GLASS.get()))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.ENDER_PEARLS))
                .aspect(TTAspects.MOTUS, 25)
                .aspect(TTAspects.TENEBRAE, 25)
                .aspect(TTAspects.PERMUTATIO, 25)
                .instability(1)
                .gate(gate("mirror"))
                .unlockedBy("has", has(TTItems.MIRRORED_GLASS))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TTItems.HAND_MIRROR.get()),
                        Ingredient.of(TTItems.MIRROR.get()))
                .component(Ingredient.of(Tags.Items.RODS_WOODEN))
                .component(Ingredient.of(Items.COMPASS))
                .component(Ingredient.of(Items.MAP))
                .aspect(TTAspects.INSTRUMENTUM, 50)
                .aspect(TTAspects.MOTUS, 50)
                .instability(5)
                .gate(gate("mirror_hand"))
                .unlockedBy("has", has(TTItems.MIRROR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.DECORATIONS,
                        new ItemStack(TTItems.MIRROR_ESSENTIA.get()),
                        Ingredient.of(TTItems.MIRRORED_GLASS.get()))
                .component(Ingredient.of(Tags.Items.INGOTS_IRON))
                .component(Ingredient.of(Tags.Items.INGOTS_IRON))
                .component(Ingredient.of(Tags.Items.INGOTS_IRON))
                .component(Ingredient.of(Tags.Items.ENDER_PEARLS))
                .aspect(TTAspects.MOTUS, 25)
                .aspect(TTAspects.AQUA, 25)
                .aspect(TTAspects.PERMUTATIO, 25)
                .instability(2)
                .gate(gate("mirror_essentia"))
                .unlockedBy("has", has(TTItems.MIRRORED_GLASS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MATRIX_SPEED.get()), 500)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern("SNS")
                .pattern("NGN")
                .pattern("SNS")
                .define('S', TTItems.STONE_ARCANE)
                .define('N', TTItemTags.NITORS)
                .define('G', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .gate(gate("infusion_boost"))
                .unlockedBy("has", has(TTItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.MATRIX_COST.get()), 500)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern("SAS")
                .pattern("AGA")
                .pattern("SAS")
                .define('S', TTItems.STONE_ARCANE)
                .define('A', TTItems.ALUMENTUM)
                .define('G', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .gate(gate("infusion_boost"))
                .unlockedBy("has", has(TTItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.DIOPTRA.get()), 50)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("APA")
                .pattern("IGI")
                .pattern("AAA")
                .define('A', TTItems.STONE_ARCANE)
                .define('P', TTItems.VIS_RESONATOR)
                .define('G', TTItems.THAUMOMETER)
                .define('I', TTItemTags.PLATES_IRON)
                .gate(gate("dioptra"))
                .unlockedBy("has", has(TTItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.VIS_BATTERY.get()), 50)
                .aspect(TTAspects.AER, 2)
                .aspect(TTAspects.TERRA, 2)
                .aspect(TTAspects.AQUA, 2)
                .aspect(TTAspects.IGNIS, 2)
                .aspect(TTAspects.ORDO, 2)
                .aspect(TTAspects.PERDITIO, 2)
                .pattern("SSS")
                .pattern("SRS")
                .pattern("SSS")
                .define('S', TTItems.SLAB_ARCANE_STONE)
                .define('R', TTItems.VIS_RESONATOR)
                .gate(gate("vis_battery"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.JAR_BRAIN.get()),
                        Ingredient.of(TTItems.JAR_NORMAL.get()))
                .component(Ingredient.of(TTItems.BRAIN.get()))
                .component(Ingredient.of(Items.SPIDER_EYE))
                .component(Ingredient.of(Tags.Items.BUCKETS_WATER))
                .component(Ingredient.of(Items.SPIDER_EYE))
                .aspect(TTAspects.COGNITIO, 25)
                .aspect(TTAspects.SENSUS, 25)
                .aspect(TTAspects.EXANIMIS, 25)
                .instability(4)
                .gate(gate("jar_brain"))
                .unlockedBy("has", has(TTItems.JAR_NORMAL.get()))
                .save(output);
    }

    private void buildNoiseDeviceRecipes() {

        arcaneShaped(new ItemStack(TTItems.LAMP_ARCANE.get()), 50)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.IGNIS, 1)
                .pattern(" I ")
                .pattern("IAI")
                .pattern(" I ")
                .define('A', TTItemTags.STORAGE_BLOCKS_AMBER)
                .define('I', TTItemTags.PLATES_IRON)
                .gate(gate("arcane_lamp"))
                .unlockedBy("has", has(TTItemTags.PLATES_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ARCANE_EAR.get()), 15)
                .aspect(TTAspects.AER, 1)
                .pattern("P P")
                .pattern(" G ")
                .pattern("WRW")
                .define('W', ItemTags.WOODEN_SLABS)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('P', TTItemTags.PLATES_BRASS)
                .gate(gate("arcane_ear"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        arcaneShapeless(new ItemStack(TTItems.ARCANE_EAR_TOGGLE.get()), 5)
                .requires(TTItems.ARCANE_EAR.get())
                .requires(Items.LEVER)
                .gate(gate("arcane_ear"))
                .unlockedBy("has", has(TTItems.ARCANE_EAR.get()))
                .save(output, TTIds.MODID + ":arcane_ear_toggle");

        arcaneShaped(new ItemStack(TTItems.HUNGRY_CHEST.get()), 15)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("WTW")
                .pattern("W W")
                .pattern("WWW")
                .define('W', TTItemTags.PLANKS_GREATWOOD)
                .define('T', ItemTags.WOODEN_TRAPDOORS)
                .gate(gate("hungry_chest"))
                .unlockedBy("has", has(TTItemTags.PLANKS_GREATWOOD))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.CENTRIFUGE.get()), 100)
                .aspect(TTAspects.ORDO, 1)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern(" T ")
                .pattern("RCP")
                .pattern(" T ")
                .define('T', TTItems.TUBE)
                .define('P', TTItems.MECHANISM_SIMPLE)
                .define('R', TTItems.MORPHIC_RESONATOR)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("centrifuge"))
                .unlockedBy("has", has(TTItems.MORPHIC_RESONATOR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.LAMP_GROWTH.get()),
                        Ingredient.of(TTItems.LAMP_ARCANE.get()))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Items.BONE_MEAL))
                .component(crystal(TTAspects.TERRA))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Items.BONE_MEAL))
                .component(crystal(TTAspects.TERRA))
                .aspect(TTAspects.HERBA, 20)
                .aspect(TTAspects.LUX, 15)
                .aspect(TTAspects.VICTUS, 15)
                .aspect(TTAspects.INSTRUMENTUM, 15)
                .instability(4)
                .gate(gate("lamp_growth"))
                .unlockedBy("has", has(TTItems.LAMP_ARCANE.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.LAMP_FERTILITY.get()),
                        Ingredient.of(TTItems.LAMP_ARCANE.get()))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.CROPS_WHEAT))
                .component(crystal(TTAspects.IGNIS))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.CROPS_CARROT))
                .component(crystal(TTAspects.IGNIS))
                .aspect(TTAspects.BESTIA, 20)
                .aspect(TTAspects.LUX, 15)
                .aspect(TTAspects.VICTUS, 15)
                .aspect(TTAspects.DESIDERIUM, 15)
                .instability(4)
                .gate(gate("lamp_fertility"))
                .unlockedBy("has", has(TTItems.LAMP_ARCANE.get()))
                .save(output);
    }

    private void buildEssentiaMachineRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.EVERFULL_URN.get()),
                        Ingredient.of(Items.DECORATED_POT))
                .aspect(TTAspects.AQUA, 30)
                .aspect(TTAspects.FABRICO, 10)
                .aspect(TTAspects.TERRA, 10)
                .gate(gate("everfull_urn"))
                .unlockedBy("has", has(Items.DECORATED_POT))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.VIS_GENERATOR.get()), 25)
                .aspect(TTAspects.IGNIS, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern("WSW")
                .pattern("EPE")
                .pattern("WRW")
                .define('R', TTItems.VIS_RESONATOR)
                .define('E', TTItemTags.NUGGETS_BRASS)
                .define('S', Tags.Items.DUSTS_REDSTONE)
                .define('P', Items.PISTON)
                .define('W', ItemTags.PLANKS)
                .gate(gate("vis_generator"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ESSENTIA_INPUT.get()), 100)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("BQB")
                .pattern("IGI")
                .define('I', TTItemTags.PLATES_IRON)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('Q', Items.DISPENSER)
                .define('G', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("essentia_transport"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ESSENTIA_OUTPUT.get()), 100)
                .aspect(TTAspects.AER, 1)
                .aspect(TTAspects.AQUA, 1)
                .pattern("BQB")
                .pattern("IGI")
                .define('I', TTItemTags.PLATES_IRON)
                .define('B', TTItemTags.PLATES_BRASS)
                .define('Q', Items.HOPPER)
                .define('G', TTItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("essentia_transport"))
                .unlockedBy("has", has(TTItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.ESSENTIA_CRYSTALIZER.get()), 125)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.TERRA, 3)
                .aspect(TTAspects.ORDO, 1)
                .pattern("IDI")
                .pattern("QCQ")
                .pattern("WTW")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', Items.DIAMOND_BLOCK)
                .define('Q', TTItems.SALIS_MUNDUS)
                .define('C', TTItems.ALCHEMICAL_CONSTRUCT)
                .define('W', ItemTags.PLANKS)
                .define('T', TTItems.TUBE)
                .gate(gate("essentia_crystalizer"))
                .unlockedBy("has", has(TTItems.SALIS_MUNDUS))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.ESSENTIA_RESERVOIR.get()),
                        Ingredient.of(TTItems.TUBE_BUFFER.get()))
                .component(Ingredient.of(TTItems.INGOT_VOID.get()))
                .component(Ingredient.of(TTItems.JAR_NORMAL.get()))
                .component(Ingredient.of(TTItems.JAR_NORMAL.get()))
                .component(Ingredient.of(TTItems.INGOT_VOID.get()))
                .component(Ingredient.of(TTItems.JAR_NORMAL.get()))
                .component(Ingredient.of(TTItems.JAR_NORMAL.get()))
                .aspect(TTAspects.AQUA, 8)
                .aspect(TTAspects.VACUOS, 8)
                .aspect(TTAspects.PRAECANTATIO, 8)
                .aspect(TTAspects.PERMUTATIO, 8)
                .instability(6)
                .gate(gate("essentia_reservoir"))
                .unlockedBy("has", has(TTItems.TUBE_BUFFER.get()))
                .save(output);
    }

    private void buildFluxMachineRecipes() {

        arcaneShaped(new ItemStack(TTItems.FLUX_SCRUBBER.get()), 200)
                .aspect(TTAspects.AQUA, 2)
                .aspect(TTAspects.ORDO, 2)
                .aspect(TTAspects.AER, 1)
                .pattern(" B ")
                .pattern("GOG")
                .pattern("STS")
                .define('B', TTItems.BELLOWS)
                .define('G', Items.IRON_BARS)
                .define('O', TTItems.FILTER)
                .define('S', TTItems.STONE_ARCANE_BRICK)
                .define('T', TTItems.TUBE)
                .gate(gate("flux_scrubber"))
                .unlockedBy("has", has(TTItems.BELLOWS))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.BRAIN_BOX.get()), 50)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.ORDO, 1)
                .pattern("IAI")
                .pattern("ABA")
                .pattern("IAI")
                .define('B', TTItems.MIND_CLOCKWORK)
                .define('A', TTItemTags.GEMS_AMBER)
                .define('I', TTItemTags.PLATES_IRON)
                .gate(gate("thaumatorium"))
                .unlockedBy("has", has(TTItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.CONDENSER.get()), 500)
                .aspect(TTAspects.AER, 5)
                .aspect(TTAspects.AQUA, 5)
                .aspect(TTAspects.PERDITIO, 5)
                .pattern("BCB")
                .pattern("WMW")
                .pattern("BTB")
                .define('T', TTItems.TUBE)
                .define('C', TTItems.MORPHIC_RESONATOR)
                .define('W', ItemTags.PLANKS)
                .define('M', TTItems.MECHANISM_COMPLEX)
                .define('B', TTItemTags.PLATES_BRASS)
                .gate(gate("flux_cleanup"))
                .unlockedBy("has", has(TTItems.MORPHIC_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.CONDENSER_LATTICE.get()), 100)
                .aspect(TTAspects.TERRA, 3)
                .aspect(TTAspects.AER, 3)
                .pattern("QTQ")
                .pattern("QFQ")
                .pattern("QTQ")
                .define('T', TTItemTags.PLATES_THAUMIUM)
                .define('F', TTItems.FILTER)
                .define('Q', Tags.Items.GEMS_QUARTZ)
                .gate(gate("flux_cleanup"))
                .unlockedBy("has", has(TTItems.FILTER))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.STABILIZER.get()), 250)
                .aspect(TTAspects.TERRA, 1)
                .aspect(TTAspects.AQUA, 1)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern("SRS")
                .pattern("BVB")
                .pattern("IMI")
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('S', TTItems.SLAB_ARCANE_STONE)
                .define('B', TTItems.STONE_ARCANE)
                .define('M', TTItems.MECHANISM_COMPLEX)
                .define('V', TTItems.VIS_RESONATOR)
                .define('I', TTItemTags.PLATES_IRON)
                .gate(gate("infusion_stable"))
                .unlockedBy("has", has(TTItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.REDSTONE_RELAY.get()), 10)
                .aspect(TTAspects.ORDO, 1)
                .pattern("TGT")
                .pattern("SSS")
                .define('T', Items.REDSTONE_TORCH)
                .define('G', TTItems.MECHANISM_SIMPLE)
                .define('S', Items.STONE_SLAB)
                .gate(gate("redstone_relay"))
                .unlockedBy("has", has(TTItems.MECHANISM_SIMPLE))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.VOID_SIPHON.get()),
                        Ingredient.of(TTItemTags.STORAGE_BLOCKS_VOID_METAL))
                .component(Ingredient.of(TTItems.STONE_ARCANE.get()))
                .component(Ingredient.of(TTItems.STONE_ARCANE.get()))
                .component(Ingredient.of(TTItems.MECHANISM_COMPLEX.get()))
                .component(Ingredient.of(TTItemTags.PLATES_BRASS))
                .component(Ingredient.of(TTItemTags.PLATES_BRASS))
                .component(Ingredient.of(Tags.Items.NETHER_STARS))
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.PERDITIO, 50)
                .aspect(TTAspects.VACUOS, 100)
                .aspect(TTAspects.FABRICO, 50)
                .instability(7)
                .gate(gate("void_siphon"))
                .unlockedBy("has", has(TTItemTags.STORAGE_BLOCKS_VOID_METAL))
                .save(output);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemStack result, int vis) {
        return new ArcaneWorkbenchShapedRecipeBuilder(
                RecipeCategory.MISC, result, items, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis);
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemStack result, int vis) {
        return new ArcaneWorkbenchShapelessRecipeBuilder(
                RecipeCategory.MISC, result, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis, items);
    }

    private Ingredient crystal(ResourceKey<IAspect> aspect) {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        return DataComponentIngredient.of(
                false,
                TTDataComponents.CRYSTAL_ASPECT.get(),
                new AspectInstance(aspects.getOrThrow(aspect), 1),
                TTItems.ESSENTIA_CRYSTAL.get());
    }

    private static final TagKey<Item> NUGGETS_COPPER =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/copper"));
    private static final TagKey<Item> NUGGETS_SILVER =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/silver"));
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

    private ItemStack wandResult(WandCap cap, WandRod rod, boolean sceptre) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(TTDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre))
                .build();
        return new ItemStack(TTItems.WAND, 1, patch);
    }

    private void buildWandRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        ShapedRecipePattern starterPattern = ShapedRecipePattern.of(
                Map.of('I', Ingredient.of(TTItems.WAND_CAP_IRON.get()), 'S', Ingredient.of(Tags.Items.RODS_WOODEN)),
                List.of("  I", " S ", "I  "));
        ShapedRecipe starter = new ShapedRecipe(
                "",
                CraftingBookCategory.EQUIPMENT,
                starterPattern,
                wandResult(TTWandParts.CAP_IRON.get(), TTWandParts.ROD_WOOD.get(), false));
        output.accept(TTIds.rl("wand/assembly/iron_wood"), starter, null);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.WAND_CAP_IRON)
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has", has(Tags.Items.NUGGETS_IRON))
                .save(output, TTIds.MODID + ":wand/part/wand_cap_iron");

        arcaneShaped(new ItemStack(TTItems.WAND_CAP_GOLD.get()), WAND_CAP_GOLD_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_GOLD)
                .gate(gate("cap_gold"))
                .unlockedBy("has", has(Tags.Items.NUGGETS_GOLD))
                .save(output, TTIds.MODID + ":wand/part/wand_cap_gold");

        arcaneShaped(new ItemStack(TTItems.WAND_CAP_COPPER.get()), WAND_CAP_COPPER_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', NUGGETS_COPPER)
                .gate(gate("cap_copper"))
                .unlockedBy("has", has(NUGGETS_COPPER))
                .save(
                        output.withConditions(new NotCondition(new TagEmptyCondition(NUGGETS_COPPER))),
                        TTIds.MODID + ":wand/part/wand_cap_copper");

        arcaneShaped(new ItemStack(TTItems.WAND_CAP_SILVER_INERT.get()), WAND_CAP_SILVER_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', NUGGETS_SILVER)
                .gate(gate("cap_silver"))
                .unlockedBy("has", has(NUGGETS_SILVER))
                .save(
                        output.withConditions(new NotCondition(new TagEmptyCondition(NUGGETS_SILVER))),
                        TTIds.MODID + ":wand/part/wand_cap_silver_inert");

        arcaneShaped(new ItemStack(TTItems.WAND_CAP_THAUMIUM_INERT.get()), WAND_CAP_THAUMIUM_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', TTItemTags.NUGGETS_THAUMIUM)
                .gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TTItemTags.NUGGETS_THAUMIUM))
                .save(output, TTIds.MODID + ":wand/part/wand_cap_thaumium_inert");

        arcaneShaped(new ItemStack(TTItems.WAND_CAP_VOID_INERT.get()), WAND_CAP_VOID_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', TTItemTags.NUGGETS_VOID_METAL)
                .gate(gate("cap_void"))
                .unlockedBy("has", has(TTItemTags.NUGGETS_VOID_METAL))
                .save(output, TTIds.MODID + ":wand/part/wand_cap_void_inert");

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.WAND_CAP_SILVER.get()),
                        Ingredient.of(TTItems.WAND_CAP_SILVER_INERT.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.POTENTIA, 8)
                .aspect(TTAspects.AURAM, 4)
                .instability(4)
                .gate(gate("cap_silver"))
                .unlockedBy("has", has(TTItems.WAND_CAP_SILVER_INERT.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.WAND_CAP_THAUMIUM.get()),
                        Ingredient.of(TTItems.WAND_CAP_THAUMIUM_INERT.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.POTENTIA, 12)
                .aspect(TTAspects.AURAM, 6)
                .instability(5)
                .gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TTItems.WAND_CAP_THAUMIUM_INERT.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.WAND_CAP_VOID.get()),
                        Ingredient.of(TTItems.WAND_CAP_VOID_INERT.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.POTENTIA, 18)
                .aspect(TTAspects.VACUOS, 18)
                .aspect(TTAspects.ALIENIS, 18)
                .aspect(TTAspects.AURAM, 18)
                .instability(8)
                .gate(gate("cap_void"))
                .unlockedBy("has", has(TTItems.WAND_CAP_VOID_INERT.get()))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.WAND_ROD_GREATWOOD.get()), WAND_ROD_GREATWOOD_VIS)
                .pattern(" G")
                .pattern("G ")
                .define('G', TTItemTags.GREATWOOD_LOGS)
                .gate(gate("rod_greatwood"))
                .unlockedBy("has", has(TTItemTags.GREATWOOD_LOGS))
                .save(output, TTIds.MODID + ":wand/part/wand_rod_greatwood");

        elementalRodInfusion(
                aspects,
                TTItems.WAND_ROD_OBSIDIAN,
                Ingredient.of(Tags.Items.OBSIDIANS_NORMAL),
                TTAspects.TERRA,
                TTAspects.TENEBRAE,
                "rod_obsidian");
        elementalRodInfusion(
                aspects, TTItems.WAND_ROD_ICE, Ingredient.of(Blocks.ICE), TTAspects.AQUA, TTAspects.GELUM, "rod_ice");
        elementalRodInfusion(
                aspects,
                TTItems.WAND_ROD_QUARTZ,
                Ingredient.of(Blocks.QUARTZ_BLOCK),
                TTAspects.ORDO,
                TTAspects.VITREUS,
                "rod_quartz");
        elementalRodInfusion(
                aspects,
                TTItems.WAND_ROD_REED,
                Ingredient.of(Tags.Items.CROPS_SUGAR_CANE),
                TTAspects.AER,
                TTAspects.MOTUS,
                "rod_reed");
        elementalRodInfusion(
                aspects,
                TTItems.WAND_ROD_BLAZE,
                Ingredient.of(Tags.Items.RODS_BLAZE),
                TTAspects.IGNIS,
                TTAspects.BESTIA,
                "rod_blaze");
        elementalRodInfusion(
                aspects,
                TTItems.WAND_ROD_BONE,
                Ingredient.of(Tags.Items.BONES),
                TTAspects.PERDITIO,
                TTAspects.EXANIMIS,
                "rod_bone");

        InfusionRecipeBuilder silverwoodRod = new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.WAND_ROD_SILVERWOOD.get()),
                        Ingredient.of(TTItemTags.SILVERWOOD_LOGS))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()));
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            silverwoodRod.component(crystal(primal)).aspect(primal, 9);
        }
        silverwoodRod
                .aspect(TTAspects.PRAECANTATIO, 9)
                .instability(5)
                .gate(gate("rod_silverwood"))
                .unlockedBy("has", has(TTItemTags.SILVERWOOD_LOGS))
                .save(output);

        staffCoreRecipe(TTItems.STAFF_ROD_GREATWOOD, TTItems.WAND_ROD_GREATWOOD, STAFF_GREATWOOD_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_OBSIDIAN, TTItems.WAND_ROD_OBSIDIAN, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_ICE, TTItems.WAND_ROD_ICE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_QUARTZ, TTItems.WAND_ROD_QUARTZ, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_REED, TTItems.WAND_ROD_REED, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_BLAZE, TTItems.WAND_ROD_BLAZE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_BONE, TTItems.WAND_ROD_BONE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TTItems.STAFF_ROD_SILVERWOOD, TTItems.WAND_ROD_SILVERWOOD, STAFF_SILVERWOOD_VIS);

        InfusionRecipeBuilder primalStaff = new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.STAFF_ROD_PRIMAL.get()),
                        Ingredient.of(TTItems.WAND_ROD_SILVERWOOD.get()))
                .component(Ingredient.of(TTItems.PRIMAL_CHARM.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_OBSIDIAN.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_ICE.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_QUARTZ.get()))
                .component(Ingredient.of(TTItems.PRIMAL_CHARM.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_REED.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_BLAZE.get()))
                .component(Ingredient.of(TTItems.WAND_ROD_BONE.get()));
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            primalStaff.aspect(primal, 32);
        }
        primalStaff
                .aspect(TTAspects.PRAECANTATIO, 64)
                .instability(8)
                .gate(gate("staff_primal"))
                .unlockedBy("has", has(TTItems.WAND_ROD_SILVERWOOD.get()))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.PRIMAL_CHARM.get()), PRIMAL_CHARM_VIS)
                .pattern("123")
                .pattern("ISI")
                .pattern("456")
                .define('1', crystal(TTAspects.AER))
                .define('2', crystal(TTAspects.IGNIS))
                .define('3', crystal(TTAspects.AQUA))
                .define('4', crystal(TTAspects.TERRA))
                .define('5', crystal(TTAspects.ORDO))
                .define('6', crystal(TTAspects.PERDITIO))
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('S', TTItems.SALIS_MUNDUS)
                .gate(gate("unlock_artifice"))
                .unlockedBy("has", has(TTItems.SALIS_MUNDUS))
                .save(output, TTIds.MODID + ":wand/part/primal_charm");
    }

    private static final int NODE_STABILIZER_VIS = 96;

    private void buildNodeHusbandryRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        arcaneShaped(new ItemStack(TTItems.NODE_STABILIZER.get()), NODE_STABILIZER_VIS)
                .pattern(" G ")
                .pattern("QPQ")
                .pattern("SNS")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .define('P', Blocks.PISTON)
                .define('S', TTItems.STONE_ARCANE_BRICK)
                .define('N', TTItemTags.NITORS)
                .gate(gate("node_stabilizer"))
                .unlockedBy("has", has(TTItemTags.NITORS))
                .save(output, TTIds.MODID + ":node_stabilizer");

        arcaneShaped(new ItemStack(TTItems.NODE_TRANSDUCER.get()), NODE_STABILIZER_VIS)
                .pattern("RCR")
                .pattern("ISI")
                .pattern("RAR")
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('C', Items.COMPARATOR)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', TTItems.NODE_STABILIZER)
                .define('A', TTItemTags.NITORS)
                .gate(gate("node_transducer"))
                .unlockedBy("has", has(TTItems.NODE_STABILIZER))
                .save(output, TTIds.MODID + ":node_transducer");

        arcaneShaped(new ItemStack(TTItems.VIS_RELAY.get()), NODE_STABILIZER_VIS)
                .pattern(" A ")
                .pattern("GNG")
                .pattern(" S ")
                .define('A', Tags.Items.GEMS_AMETHYST)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('N', TTItemTags.NITORS)
                .define('S', TTItems.STONE_ARCANE)
                .gate(gate("vis_relay"))
                .unlockedBy("has", has(TTItemTags.NITORS))
                .save(output, TTIds.MODID + ":vis_relay");

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.NODE_STABILIZER_ADVANCED.get()),
                        Ingredient.of(TTItems.NODE_STABILIZER.get()))
                .component(Ingredient.of(TTItemTags.NITORS))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .component(Ingredient.of(TTItems.ALUMENTUM.get()))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .component(Ingredient.of(TTItemTags.NITORS))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .component(Ingredient.of(TTItems.ALUMENTUM.get()))
                .component(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .aspect(TTAspects.AURAM, 32)
                .aspect(TTAspects.PRAECANTATIO, 16)
                .aspect(TTAspects.ORDO, 16)
                .aspect(TTAspects.POTENTIA, 16)
                .instability(10)
                .gate(gate("node_stabilizer_advanced"))
                .unlockedBy("has", has(TTItems.NODE_STABILIZER.get()))
                .save(output);
    }

    private void elementalRodInfusion(
            HolderLookup<IAspect> aspects,
            DeferredItem<? extends Item> rod,
            Ingredient catalyst,
            ResourceKey<IAspect> primal,
            ResourceKey<IAspect> flavor,
            String gateEntry) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(rod.get()), catalyst)
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(crystal(primal))
                .aspect(primal, 12)
                .aspect(TTAspects.PRAECANTATIO, 6)
                .aspect(flavor, 6)
                .instability(3)
                .gate(gate(gateEntry))
                .unlockedBy("has", has(TTItems.SALIS_MUNDUS.get()))
                .save(output);
    }

    private void staffCoreRecipe(DeferredItem<? extends Item> core, DeferredItem<? extends Item> rod, int vis) {
        arcaneShaped(new ItemStack(core.get()), vis)
                .pattern("  S")
                .pattern(" G ")
                .pattern("G  ")
                .define('S', TTItems.PRIMAL_CHARM)
                .define('G', rod)
                .gate(gate("staves"))
                .unlockedBy("has", has(TTItems.PRIMAL_CHARM))
                .save(output, TTIds.MODID + ":wand/part/" + core.getId().getPath());
    }

    private void buildBaubleRecipes() {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.AMULET_MUNDANE)
                .pattern(" S ")
                .pattern("S S")
                .pattern(" I ")
                .define('S', Tags.Items.STRINGS)
                .define('I', TTItemTags.INGOTS_BRASS)
                .unlockedBy("has", has(TTItemTags.INGOTS_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.RING_MUNDANE)
                .pattern("NNN")
                .pattern("N N")
                .pattern("NNN")
                .define('N', TTItemTags.NUGGETS_BRASS)
                .unlockedBy("has", has(TTItemTags.NUGGETS_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.GIRDLE_MUNDANE)
                .pattern(" L ")
                .pattern("L L")
                .pattern(" I ")
                .define('L', Tags.Items.LEATHERS)
                .define('I', TTItemTags.INGOTS_BRASS)
                .unlockedBy("has", has(TTItemTags.INGOTS_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.AMULET_FANCY)
                .pattern(" S ")
                .pattern("SGS")
                .pattern(" I ")
                .define('S', Tags.Items.STRINGS)
                .define('G', Tags.Items.GEMS_DIAMOND)
                .define('I', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has", has(Tags.Items.GEMS_DIAMOND))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.RING_FANCY)
                .pattern("NGN")
                .pattern("N N")
                .pattern("NNN")
                .define('G', Tags.Items.GEMS_DIAMOND)
                .define('N', Tags.Items.NUGGETS_GOLD)
                .unlockedBy("has", has(Tags.Items.GEMS_DIAMOND))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TTItems.GIRDLE_FANCY)
                .pattern(" L ")
                .pattern("LGL")
                .pattern(" I ")
                .define('L', Tags.Items.LEATHERS)
                .define('G', Tags.Items.GEMS_DIAMOND)
                .define('I', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has", has(Tags.Items.GEMS_DIAMOND))
                .save(output);

        arcaneShaped(new ItemStack(TTItems.FOCUS_POUCH.get()), 25)
                .pattern("LGL")
                .pattern("LBL")
                .pattern("LLL")
                .define('B', TTItems.GIRDLE_MUNDANE)
                .define('L', Tags.Items.LEATHERS)
                .define('G', Tags.Items.INGOTS_GOLD)
                .gate(gate("focus_pouch"))
                .unlockedBy("has", has(Tags.Items.LEATHERS))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.SANITY_CHECKER.get()), 20)
                .aspect(TTAspects.ORDO, 1)
                .aspect(TTAspects.PERDITIO, 1)
                .pattern("BN ")
                .pattern("M N")
                .pattern("BN ")
                .define('N', TTItemTags.NUGGETS_BRASS)
                .define('B', TTItems.BRAIN)
                .define('M', TTItems.MIRRORED_GLASS)
                .gate(gate("warp"))
                .unlockedBy("has", has(TTItems.MIRRORED_GLASS))
                .save(output);
        arcaneShaped(new ItemStack(TTItems.RESONATOR.get()), 50)
                .pattern("I I")
                .pattern("INI")
                .pattern(" S ")
                .define('I', TTItemTags.PLATES_IRON)
                .define('N', Tags.Items.GEMS_QUARTZ)
                .define('S', Tags.Items.RODS_WOODEN)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TTItemTags.PLATES_IRON))
                .save(output);
    }

    private void buildWearableInfusionRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.AMULET_VIS_CRAFTED.get()),
                        Ingredient.of(TTItems.AMULET_MUNDANE.get()))
                .component(Ingredient.of(TTItems.VIS_RESONATOR.get()))
                .component(crystal(TTAspects.AER))
                .component(crystal(TTAspects.IGNIS))
                .component(crystal(TTAspects.AQUA))
                .component(crystal(TTAspects.TERRA))
                .component(crystal(TTAspects.ORDO))
                .aspect(TTAspects.AURAM, 50)
                .aspect(TTAspects.POTENTIA, 100)
                .aspect(TTAspects.VACUOS, 50)
                .instability(6)
                .gate(gate("vis_amulet"))
                .unlockedBy("has", has(TTItems.AMULET_MUNDANE.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.VERDANT_CHARM.get()),
                        Ingredient.of(TTItems.AMULET_FANCY.get()))
                .component(Ingredient.of(TTItemTags.NUGGETS_QUICKSILVER))
                .component(crystal(TTAspects.VICTUS))
                .component(Ingredient.of(Tags.Items.BUCKETS_MILK))
                .component(crystal(TTAspects.HERBA))
                .aspect(TTAspects.VICTUS, 60)
                .aspect(TTAspects.ORDO, 30)
                .aspect(TTAspects.HERBA, 60)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TTItems.AMULET_FANCY.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.VERDANT_CHARM.get()),
                        Ingredient.of(TTItems.VERDANT_CHARM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TTDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_LIFE)
                        .build())
                .component(Ingredient.of(Items.GOLDEN_APPLE))
                .component(crystal(TTAspects.VICTUS))
                .component(potion(Potions.STRONG_HEALING))
                .component(crystal(TTAspects.HUMANUS))
                .aspect(TTAspects.VICTUS, 80)
                .aspect(TTAspects.HUMANUS, 80)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TTItems.VERDANT_CHARM.get()))
                .save(output, TTIds.MODID + ":infusion/verdant_charm_life");
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.VERDANT_CHARM.get()),
                        Ingredient.of(TTItems.VERDANT_CHARM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TTDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_SUSTAIN)
                        .build())
                .component(Ingredient.of(TTItems.TRIPLE_MEAT_TREAT.get()))
                .component(crystal(TTAspects.DESIDERIUM))
                .component(potion(Potions.STRONG_REGENERATION))
                .component(crystal(TTAspects.AER))
                .aspect(TTAspects.DESIDERIUM, 80)
                .aspect(TTAspects.AER, 80)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TTItems.VERDANT_CHARM.get()))
                .save(output, TTIds.MODID + ":infusion/verdant_charm_sustain");
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.CLOUD_RING.get()),
                        Ingredient.of(TTItems.RING_MUNDANE.get()))
                .component(crystal(TTAspects.AER))
                .component(Ingredient.of(Tags.Items.FEATHERS))
                .aspect(TTAspects.AER, 50)
                .instability(1)
                .gate(gate("cloud_ring"))
                .unlockedBy("has", has(TTItems.RING_MUNDANE.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.CURIOSITY_BAND.get()),
                        Ingredient.of(TTItems.GIRDLE_FANCY.get()))
                .component(Ingredient.of(Tags.Items.GEMS_EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Tags.Items.GEMS_EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Tags.Items.GEMS_EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Tags.Items.GEMS_EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .aspect(TTAspects.COGNITIO, 150)
                .aspect(TTAspects.VACUOS, 50)
                .aspect(TTAspects.VINCULUM, 100)
                .instability(5)
                .gate(gate("curiosity_band"))
                .unlockedBy("has", has(TTItems.GIRDLE_FANCY.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.CHARM_UNDYING.get()),
                        Ingredient.of(Items.TOTEM_OF_UNDYING))
                .component(Ingredient.of(TTItemTags.PLATES_BRASS))
                .aspect(TTAspects.VICTUS, 25)
                .instability(2)
                .gate(gate("charm_undying"))
                .unlockedBy("has", has(Items.TOTEM_OF_UNDYING))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TTItems.VOIDSEER_CHARM.get()),
                        Ingredient.of(TTItems.AMULET_FANCY.get()))
                .component(Ingredient.of(TTItems.BRAIN.get()))
                .component(Ingredient.of(TTItems.VOID_SEED.get()))
                .component(Ingredient.of(TTItems.BRAIN.get()))
                .component(Ingredient.of(TTItems.PRIMORDIAL_PEARL.get()))
                .aspect(TTAspects.COGNITIO, 150)
                .aspect(TTAspects.VACUOS, 150)
                .aspect(TTAspects.PRAECANTATIO, 100)
                .instability(8)
                .gate(gate("voidseer_pearl"))
                .unlockedBy("has", has(TTItems.PRIMORDIAL_PEARL.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.FORTRESS_HELM.get()),
                        Ingredient.of(TTItems.THAUMIUM_HELM.get()))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.GEMS_EMERALD))
                .aspect(TTAspects.METALLUM, 50)
                .aspect(TTAspects.PRAEMUNIO, 20)
                .aspect(TTAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_HELM.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.FORTRESS_CHEST.get()),
                        Ingredient.of(TTItems.THAUMIUM_CHEST.get()))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .aspect(TTAspects.METALLUM, 50)
                .aspect(TTAspects.PRAEMUNIO, 30)
                .aspect(TTAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_CHEST.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.FORTRESS_LEGS.get()),
                        Ingredient.of(TTItems.THAUMIUM_LEGS.get()))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(TTItemTags.PLATES_THAUMIUM))
                .component(Ingredient.of(Tags.Items.INGOTS_GOLD))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .aspect(TTAspects.METALLUM, 50)
                .aspect(TTAspects.PRAEMUNIO, 25)
                .aspect(TTAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TTItems.THAUMIUM_LEGS.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.FORTRESS_HELM.get()),
                        Ingredient.of(TTItems.FORTRESS_HELM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)
                        .build())
                .component(Ingredient.of(Tags.Items.SLIME_BALLS))
                .component(Ingredient.of(TTItems.GOGGLES_REVEALING.get()))
                .aspect(TTAspects.SENSUS, 40)
                .aspect(TTAspects.AURAM, 20)
                .aspect(TTAspects.PRAEMUNIO, 20)
                .instability(5)
                .gate(gate("fortress_mask"))
                .unlockedBy("has", has(TTItems.FORTRESS_HELM.get()))
                .save(output, TTIds.MODID + ":infusion/fortress_helm_goggles");
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                0,
                TTAspects.COGNITIO,
                TTAspects.VICTUS,
                Ingredient.of(Tags.Items.DYES_BLACK),
                Ingredient.of(TTItems.PLANT_SHIMMERLEAF.get()),
                Ingredient.of(TTItems.BRAIN.get()));
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                1,
                TTAspects.PERDITIO,
                TTAspects.MORTUUS,
                Ingredient.of(Tags.Items.DYES_WHITE),
                Ingredient.of(Items.POISONOUS_POTATO),
                Ingredient.of(Items.WITHER_SKELETON_SKULL));
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                2,
                TTAspects.EXANIMIS,
                TTAspects.VICTUS,
                Ingredient.of(Tags.Items.DYES_RED),
                Ingredient.of(Items.GHAST_TEAR),
                Ingredient.of(Tags.Items.BUCKETS_MILK));

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.VOID_ROBE_HELM.get()),
                        Ingredient.of(TTItems.VOID_HELM.get()))
                .component(Ingredient.of(TTItems.GOGGLES_REVEALING.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .aspect(TTAspects.METALLUM, 25)
                .aspect(TTAspects.SENSUS, 25)
                .aspect(TTAspects.PRAEMUNIO, 25)
                .aspect(TTAspects.POTENTIA, 25)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.VACUOS, 25)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TTItems.VOID_HELM.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.VOID_ROBE_CHEST.get()),
                        Ingredient.of(TTItems.VOID_CHEST.get()))
                .component(Ingredient.of(TTItems.CLOTH_CHEST.get()))
                .component(Ingredient.of(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .aspect(TTAspects.METALLUM, 35)
                .aspect(TTAspects.PRAEMUNIO, 35)
                .aspect(TTAspects.POTENTIA, 25)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.VACUOS, 35)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TTItems.VOID_CHEST.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.VOID_ROBE_LEGS.get()),
                        Ingredient.of(TTItems.VOID_LEGS.get()))
                .component(Ingredient.of(TTItems.CLOTH_LEGS.get()))
                .component(Ingredient.of(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .aspect(TTAspects.METALLUM, 30)
                .aspect(TTAspects.PRAEMUNIO, 30)
                .aspect(TTAspects.POTENTIA, 25)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.VACUOS, 30)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TTItems.VOID_LEGS.get()))
                .save(output);
    }

    private void buildMaskRecipe(
            HolderLookup<IAspect> aspects,
            ResearchGate gate,
            int mask,
            ResourceKey<IAspect> first,
            ResourceKey<IAspect> second,
            Ingredient dye,
            Ingredient special1,
            Ingredient special2) {
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TTItems.FORTRESS_HELM.get()),
                        Ingredient.of(TTItems.FORTRESS_HELM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TTDataComponents.FORTRESS_MASK.get(), mask)
                        .build())
                .component(dye)
                .component(Ingredient.of(TTItemTags.PLATES_IRON))
                .component(Ingredient.of(Tags.Items.LEATHERS))
                .component(special1)
                .component(special2)
                .component(Ingredient.of(TTItemTags.PLATES_IRON))
                .aspect(first, 80)
                .aspect(second, 80)
                .aspect(TTAspects.PRAEMUNIO, 20)
                .instability(8)
                .gate(gate)
                .unlockedBy("has", has(TTItems.FORTRESS_HELM.get()))
                .save(output, TTIds.MODID + ":infusion/fortress_helm_mask_" + mask);
    }

    private Ingredient potion(Holder<Potion> potion) {
        return DataComponentIngredient.of(
                false, DataComponents.POTION_CONTENTS, new PotionContents(potion), Items.POTION);
    }

    private void buildDustTriggerRecipes() {
        dustTrigger(
                "bookshelf_to_thaumonomicon",
                new DustTriggerTagRecipe(
                        Tags.Blocks.BOOKSHELVES,
                        new ItemStack(TTItems.THAUMONOMICON.get()),
                        Optional.of(gate("gotdream"))));
        dustTrigger(
                "crafting_tables_to_arcane_workbench",
                new DustTriggerTagRecipe(
                        Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES,
                        new ItemStack(TTBlocks.ARCANE_WORKBENCH.get().asItem()),
                        Optional.of(gate("first_steps", 0))));
        dustTrigger(
                "cauldron_to_crucible",
                new DustTriggerSimpleRecipe(
                        Blocks.CAULDRON,
                        new ItemStack(TTBlocks.CRUCIBLE.get().asItem()),
                        Optional.of(gate("unlock_alchemy", 0))));
        dustTrigger(
                "golem_press",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("golem_press"),
                        new ItemStack(TTBlocks.GOLEM_BUILDER.get().asItem()),
                        Optional.of(gate("mind_clockwork"))));
        dustTrigger(
                "advanced_alchemical_furnace",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("advanced_alchemical_furnace"),
                        new ItemStack(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get().asItem()),
                        Optional.of(gate("essentia_smelter_void"))));
        dustTrigger(
                "infernal_furnace",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("infernal_furnace"),
                        new ItemStack(TTBlocks.INFERNAL_FURNACE.get().asItem()),
                        Optional.of(gate("infernal_furnace"))));
        dustTrigger(
                "infusion_altar",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("infusion_altar"),
                        new ItemStack(TTBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion"))));
        dustTrigger(
                "infusion_altar_ancient",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("infusion_altar_ancient"),
                        new ItemStack(TTBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion_ancient"))));
        dustTrigger(
                "infusion_altar_eldritch",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("infusion_altar_eldritch"),
                        new ItemStack(TTBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion_eldritch"))));
        dustTrigger(
                "thaumatorium",
                new DustTriggerMultiblockRecipe(
                        TTIds.rl("thaumatorium"),
                        new ItemStack(TTBlocks.THAUMATORIUM.get().asItem()),
                        Optional.of(gate("thaumatorium"))));
    }

    private void dustTrigger(String name, Recipe<?> recipe) {
        output.accept(TTIds.rl("dust_trigger/" + name), recipe, null);
    }

    private void buildSalisMundusRecipe() {
        SpecialRecipeBuilder.special(category -> SalisMundusRecipe.INSTANCE).save(output, "thaumaturge:salis_mundus");
    }
}
