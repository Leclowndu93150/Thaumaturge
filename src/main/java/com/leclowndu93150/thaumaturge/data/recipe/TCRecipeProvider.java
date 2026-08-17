package com.leclowndu93150.thaumaturge.data.recipe;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionEnchantmentRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumaturge.registry.TCBlockFamilies;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCWandParts;
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

public final class TCRecipeProvider extends RecipeProvider {

    private RecipeOutput output;
    private HolderLookup.Provider registries;
    private HolderLookup<Item> items;

    public TCRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    private static ResearchGate gate(String path) {
        return new ResearchGate(TCIds.rl(path), Optional.empty(), false);
    }

    private static ResearchGate gate(String path, int stage) {
        return new ResearchGate(TCIds.rl(path), Optional.of(stage), false);
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
        buildNoiseDeviceRecipes();
        buildEssentiaMachineRecipes();
        buildFluxMachineRecipes();
        buildBaubleRecipes();
        buildWearableInfusionRecipes();
        buildWandRecipes();
        buildNodeHusbandryRecipes();

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.SCRIBING_TOOLS)
                .requires(TCItems.PHIAL)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(TCItems.PHIAL))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.SCRIBING_TOOLS)
                .requires(Items.GLASS_BOTTLE)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(Tags.Items.GLASS_PANES))
                .save(output, TCIds.MODID + ":scribing_tools_alt");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.LABEL)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PAPER, 4)
                .unlockedBy("has", has(Tags.Items.SLIME_BALLS))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.LABEL)
                .requires(TCItems.LABEL)
                .unlockedBy("has", has(TCItems.LABEL))
                .save(output, TCIds.MODID + ":label_clear");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.JAR_BRACE, 2)
                .pattern("SBS")
                .pattern("B B")
                .pattern("SBS")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('B', TCItemTags.NUGGETS_BRASS)
                .unlockedBy("has", has(TCItems.NUGGET_BRASS))
                .save(output);

        for (DyeColor color : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(
                            RecipeCategory.MISC, TCItems.NITORS.get(color).get())
                    .requires(TCItemTags.NITORS)
                    .requires(color.getTag())
                    .unlockedBy("has", has(TCItemTags.NITORS))
                    .save(output, TCIds.MODID + ":nitors/" + color.getName());
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCItems.STONE_ARCANE, 8)
                .pattern("SSS")
                .pattern("SVS")
                .pattern("SSS")
                .define('S', Tags.Items.STONES)
                .define('V', TCItems.ESSENTIA_CRYSTAL)
                .unlockedBy("has", has(TCItems.ESSENTIA_CRYSTAL))
                .save(output);

        TCBlockFamilies.getAllFamilies()
                .forEach(family -> generateRecipes(output, family, FeatureFlagSet.of(FeatureFlags.VANILLA)));

        planksFromLogs(output, TCItems.PLANK_GREATWOOD.get(), TCItemTags.GREATWOOD_LOGS, 4);
        planksFromLogs(output, TCItems.PLANK_SILVERWOOD.get(), TCItemTags.SILVERWOOD_LOGS, 4);
        woodFromLogs(output, TCItems.WOOD_GREATWOOD.get(), TCItems.LOG_GREATWOOD.get());
        woodFromLogs(output, TCItems.STRIPPED_WOOD_GREATWOOD.get(), TCItems.STRIPPED_LOG_GREATWOOD.get());
        woodFromLogs(output, TCItems.WOOD_SILVERWOOD.get(), TCItems.LOG_SILVERWOOD.get());
        woodFromLogs(output, TCItems.STRIPPED_WOOD_SILVERWOOD.get(), TCItems.STRIPPED_LOG_SILVERWOOD.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.PHIAL, 8)
                .pattern(" C ")
                .pattern("P P")
                .pattern(" P ")
                .define('C', Items.CLAY_BALL)
                .define('P', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

        oreSmelting(TCItems.QUICKSILVER, TCItemTags.ORES_CINNABAR, 1F, "quicksilver");
        oreSmelting(TCItems.AMBER, TCItemTags.ORES_AMBER, 1F, "amber");
        oreSmelting(Items.QUARTZ, Tags.Items.ORES_QUARTZ, 0.2F, "quartz");

        block3x3(
                TCItems.METAL_BRASS_BLOCK,
                TCItemTags.INGOTS_BRASS,
                TCItems.INGOT_BRASS,
                TCItemTags.STORAGE_BLOCKS_BRASS);
        block3x3(
                TCItems.METAL_THAUMIUM_BLOCK,
                TCItemTags.INGOTS_THAUMIUM,
                TCItems.INGOT_THAUMIUM,
                TCItemTags.STORAGE_BLOCKS_THAUMIUM);
        block3x3(
                TCItems.METAL_VOID_BLOCK,
                TCItemTags.INGOTS_VOID_METAL,
                TCItems.INGOT_VOID,
                TCItemTags.STORAGE_BLOCKS_VOID_METAL);
        block2x2(TCItems.AMBER_BLOCK, TCItemTags.GEMS_AMBER, TCItems.AMBER, TCItemTags.STORAGE_BLOCKS_AMBER);

        nuggets3x3(Items.QUARTZ, TCItemTags.NUGGETS_QUARTZ, TCItems.NUGGET_QUARTZ, Tags.Items.GEMS_QUARTZ);
        nuggets3x3(
                TCItems.QUICKSILVER,
                TCItemTags.NUGGETS_QUICKSILVER,
                TCItems.NUGGET_QUICKSILVER,
                TCItemTags.GEMS_QUICKSILVER);
        nuggets3x3(TCItems.INGOT_BRASS, TCItemTags.NUGGETS_BRASS, TCItems.NUGGET_BRASS, TCItemTags.INGOTS_BRASS);
        nuggets3x3(
                TCItems.INGOT_THAUMIUM,
                TCItemTags.NUGGETS_THAUMIUM,
                TCItems.NUGGET_THAUMIUM,
                TCItemTags.INGOTS_THAUMIUM);
        nuggets3x3(
                TCItems.INGOT_VOID, TCItemTags.NUGGETS_VOID_METAL, TCItems.NUGGET_VOID, TCItemTags.INGOTS_VOID_METAL);

        plateRecipe(TCItems.PLATE_IRON, Tags.Items.INGOTS_IRON);
        plateRecipe(TCItems.PLATE_BRASS, TCItemTags.INGOTS_BRASS);
        plateRecipe(TCItems.PLATE_THAUMIUM, TCItemTags.INGOTS_THAUMIUM);
        plateRecipe(TCItems.PLATE_VOID, TCItemTags.INGOTS_VOID_METAL);

        clusterSmelting(Items.IRON_INGOT, TCItems.CLUSTER_IRON, "iron_ingot");
        clusterSmelting(Items.GOLD_INGOT, TCItems.CLUSTER_GOLD, "gold_ingot");
        clusterSmelting(Items.COPPER_INGOT, TCItems.CLUSTER_COPPER, "copper_ingot");
        clusterSmelting(TCItems.QUICKSILVER, TCItems.CLUSTER_CINNABAR, "quicksilver");
        clusterSmelting(Items.QUARTZ, TCItems.CLUSTER_QUARTZ, "quartz");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.QUICKSILVER)
                .requires(TCItems.PLANT_SHIMMERLEAF)
                .unlockedBy("has", has(TCItems.PLANT_SHIMMERLEAF))
                .save(output, TCIds.MODID + ":quicksilver_from_shimmerleaf");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLAZE_POWDER)
                .requires(TCItems.PLANT_CINDERPEARL)
                .unlockedBy("has", has(TCItems.PLANT_CINDERPEARL))
                .save(output, TCIds.MODID + ":blaze_powder_from_cinderpearl");

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.DECORATIONS,
                        TCBlocks.CANDLES.get(DyeColor.WHITE).get(),
                        3)
                .pattern(" S ")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', Tags.Items.STRINGS)
                .define('T', TCItems.TALLOW.get())
                .unlockedBy("has_tallow", has(TCItems.TALLOW.get()))
                .save(output);
        for (DyeColor dye : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(
                            RecipeCategory.DECORATIONS,
                            TCBlocks.CANDLES.get(dye).get())
                    .requires(dyeTag(dye))
                    .requires(TCItemTags.CANDLES)
                    .unlockedBy("has_candle", has(TCItemTags.CANDLES))
                    .save(output, TCIds.MODID + ":candle_" + dye.getName() + "_from_dye");
        }

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.THAUMONOMICON_LINKING.get()),
                        Ingredient.of(TCItems.THAUMONOMICON_SHARING.get()))
                .aspect(TCAspects.COGNITIO, 40)
                .aspect(TCAspects.SENSUS, 20)
                .aspect(TCAspects.ALIENIS, 10)
                .component(Ingredient.of(TCItems.VOID_SEED.get()))
                .component(Ingredient.of(TCItems.BRAIN.get()))
                .component(Ingredient.of(TCItems.VOID_SEED.get()))
                .component(Ingredient.of(Items.ENDER_EYE))
                .instability(2)
                .gate(gate("link_book", 1))
                .unlockedBy("has", has(TCItems.THAUMONOMICON_SHARING))
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
                        TCIds.MODID + ":"
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
                        TCIds.MODID + ":"
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
                        TCIds.MODID + ":"
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
                .save(this.output, getItemName(item) + "_from_ore");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(oreTag), RecipeCategory.MISC, item, xp, 100)
                .group(group)
                .unlockedBy("has", this.has(oreTag))
                .save(this.output, getItemName(item) + "_blasting_from_ore");
    }

    private void clusterSmelting(ItemLike item, ItemLike cluster, String group) {

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(cluster), RecipeCategory.MISC, new ItemStack(item.asItem(), 2), 1F, 200)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, getItemName(item) + "_from_cluster");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(cluster), RecipeCategory.MISC, new ItemStack(item.asItem(), 2), 1F, 100)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, getItemName(item) + "_blasting_from_cluster");
    }

    private void plateRecipe(ItemLike plate, TagKey<Item> ingotTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, plate, 3)
                .pattern("NNN")
                .define('N', ingotTag)
                .unlockedBy("has", has(ingotTag))
                .save(output);
    }

    private void buildDecorRecipes() {
        ResearchGate artificeGate = gate("paving_stones");

        stairsRecipe(TCBlocks.STAIRS_GREATWOOD.get(), TCBlocks.PLANK_GREATWOOD.get());
        stairsRecipe(TCBlocks.STAIRS_SILVERWOOD.get(), TCBlocks.PLANK_SILVERWOOD.get());
        slabRecipe(TCBlocks.SLAB_GREATWOOD.get(), TCBlocks.PLANK_GREATWOOD.get());
        slabRecipe(TCBlocks.SLAB_SILVERWOOD.get(), TCBlocks.PLANK_SILVERWOOD.get());
        slabRecipe(TCBlocks.SLAB_ARCANE_STONE.get(), TCBlocks.STONE_ARCANE.get());
        slabRecipe(TCBlocks.SLAB_ARCANE_BRICK.get(), TCBlocks.STONE_ARCANE_BRICK.get());
        slabRecipe(TCBlocks.SLAB_ANCIENT.get(), TCBlocks.STONE_ANCIENT.get());
        slabRecipe(TCBlocks.SLAB_ELDRITCH.get(), TCBlocks.STONE_ELDRITCH_TILE.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TCItems.TABLE_WOOD)
                .pattern("SSS")
                .pattern("W W")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('W', ItemTags.PLANKS)
                .unlockedBy("has", has(ItemTags.WOODEN_SLABS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TCItems.TABLE_STONE)
                .pattern("SSS")
                .pattern("W W")
                .define('S', Items.STONE_SLAB)
                .define('W', Tags.Items.STONES)
                .unlockedBy("has", has(Items.STONE_SLAB))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCItems.FLESH_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Items.ROTTEN_FLESH)
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ROTTEN_FLESH, 9)
                .requires(TCItems.FLESH_BLOCK)
                .unlockedBy("has", has(TCItems.FLESH_BLOCK))
                .save(output, TCIds.MODID + ":rotten_flesh_from_flesh_block");

        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(Items.OBSIDIAN), RecipeCategory.BUILDING_BLOCKS, TCItems.OBSIDIAN_TILE)
                .unlockedBy("has", has(Items.OBSIDIAN))
                .save(output, TCIds.MODID + ":obsidian_tile_from_obsidian_stonecutting");
        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(Items.OBSIDIAN), RecipeCategory.BUILDING_BLOCKS, TCItems.OBSIDIAN_TOTEM)
                .unlockedBy("has", has(Items.OBSIDIAN))
                .save(output, TCIds.MODID + ":obsidian_totem_from_obsidian_stonecutting");
        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(TCItems.OBSIDIAN_TILE), RecipeCategory.BUILDING_BLOCKS, TCItems.OBSIDIAN_TOTEM)
                .unlockedBy("has", has(TCItems.OBSIDIAN_TILE))
                .save(output, TCIds.MODID + ":obsidian_totem_from_obsidian_tile_stonecutting");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCItems.AMBER_BRICK, 4)
                .pattern("##")
                .pattern("##")
                .define('#', TCItems.AMBER_BLOCK)
                .unlockedBy("has", has(TCItems.AMBER_BLOCK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCItems.AMBER_BLOCK, 4)
                .pattern("##")
                .pattern("##")
                .define('#', TCItems.AMBER_BRICK)
                .unlockedBy("has", has(TCItems.AMBER_BRICK))
                .save(output, TCIds.MODID + ":amber_block_from_brick");

        arcaneShaped(new ItemStack(TCItems.PAVING_STONE_BARRIER.get(), 4), 50)
                .aspect(TCAspects.IGNIS, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("SS")
                .pattern("SS")
                .define('S', TCItems.STONE_ARCANE_BRICK)
                .gate(artificeGate)
                .unlockedBy("has", has(TCItems.STONE_ARCANE_BRICK))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PAVING_STONE_TRAVEL.get(), 4), 50)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.TERRA, 1)
                .pattern("SS")
                .pattern("SS")
                .define('S', TCItems.STONE_ARCANE_BRICK)
                .gate(artificeGate)
                .unlockedBy("has", has(TCItems.STONE_ARCANE_BRICK))
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

    private void slabRecipe(Block result, Block base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 6)
                .pattern("KKK")
                .define('K', base)
                .unlockedBy("has", has(base))
                .save(output);
    }

    private void buildConstructRecipes() {
        arcaneShapeless(new ItemStack(TCItems.ACTIVATOR_RAIL.get()), 10)
                .requires(Items.ACTIVATOR_RAIL)
                .gate(gate("first_steps"))
                .unlockedBy("has", has(Items.ACTIVATOR_RAIL))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.TURRET_BASIC.get()), 100)
                .aspect(TCAspects.AER, 1)
                .pattern("BGI")
                .pattern("WMW")
                .pattern("S S")
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('I', TCItemTags.PLATES_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('M', TCItems.MIND_CLOCKWORK)
                .define('B', Items.BOW)
                .define('W', TCBlocks.PLANK_GREATWOOD)
                .gate(gate("basic_turret"))
                .unlockedBy("has", has(TCItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.TURRET_ADVANCED.get()), 150)
                .aspect(TCAspects.AER, 2)
                .pattern("PMP")
                .pattern("PTP")
                .define('T', TCItems.TURRET_BASIC)
                .define('P', TCItemTags.PLATES_IRON)
                .define('M', TCItems.MIND_BIOTHAUMIC)
                .gate(gate("advanced_turret"))
                .unlockedBy("has", has(TCItems.MIND_BIOTHAUMIC))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TCItems.TURRET_BORE.get()),
                        Ingredient.of(TCItems.TURRET_BASIC.get()))
                .component(Ingredient.of(TCBlocks.PLANK_GREATWOOD.get()))
                .component(Ingredient.of(TCBlocks.PLANK_GREATWOOD.get()))
                .component(Ingredient.of(TCItems.MECHANISM_COMPLEX.get()))
                .component(Ingredient.of(TCItemTags.PLATES_BRASS))
                .component(Ingredient.of(Items.DIAMOND_PICKAXE))
                .component(Ingredient.of(Items.DIAMOND_SHOVEL))
                .component(Ingredient.of(TCItems.MORPHIC_RESONATOR.get()))
                .component(Ingredient.of(TCItems.RARE_EARTH.get()))
                .aspect(TCAspects.POTENTIA, 25)
                .aspect(TCAspects.TERRA, 25)
                .aspect(TCAspects.MACHINA, 100)
                .aspect(TCAspects.VACUOS, 25)
                .aspect(TCAspects.MOTUS, 25)
                .instability(4)
                .gate(gate("arcane_bore"))
                .unlockedBy("has", has(TCItems.TURRET_BASIC))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GRAPPLE_GUN_TIP.get()), 25)
                .aspect(TCAspects.TERRA, 1)
                .pattern("BRB")
                .pattern("RHR")
                .pattern("BRB")
                .define('B', TCItemTags.PLATES_BRASS)
                .define('R', TCItems.RARE_EARTH)
                .define('H', Items.TRIPWIRE_HOOK)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TCItems.RARE_EARTH))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GRAPPLE_GUN_SPOOL.get()), 25)
                .aspect(TCAspects.AQUA, 1)
                .pattern("SHS")
                .pattern("SGS")
                .pattern("SSS")
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('S', Tags.Items.STRINGS)
                .define('H', Items.TRIPWIRE_HOOK)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GRAPPLE_GUN.get()), 75)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.IGNIS, 1)
                .pattern("  S")
                .pattern("TII")
                .pattern(" BW")
                .define('B', TCItemTags.PLATES_BRASS)
                .define('I', TCItemTags.PLATES_IRON)
                .define('T', TCItems.GRAPPLE_GUN_TIP)
                .define('W', ItemTags.PLANKS)
                .define('S', TCItems.GRAPPLE_GUN_SPOOL)
                .gate(gate("grapple_gun"))
                .unlockedBy("has", has(TCItems.GRAPPLE_GUN_TIP))
                .save(output);
    }

    private void buildFocalManipulatorRecipe() {
        arcaneShaped(new ItemStack(TCItems.FOCAL_MANIPULATOR.get()), 100)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("ISI")
                .pattern("BRB")
                .pattern("GTG")
                .define('I', TCItemTags.PLATES_IRON)
                .define('S', TCItems.SLAB_ARCANE_STONE)
                .define('B', TCItems.STONE_ARCANE)
                .define('R', TCItems.VIS_RESONATOR)
                .define('G', Items.GOLD_INGOT)
                .define('T', TCItems.TABLE_STONE)
                .gate(gate("base_auromancy", 1))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);
    }

    private void buildInfusionAltarRecipes() {
        arcaneShaped(new ItemStack(TCItems.INFUSION_MATRIX.get()), 150)
                .allAspects()
                .pattern("S S")
                .pattern(" N ")
                .pattern("S S")
                .define('S', TCItems.STONE_ARCANE_BRICK)
                .define('N', TCItemTags.NITORS)
                .gate(gate("infusion", 1))
                .unlockedBy("has", has(TCItems.STONE_ARCANE_BRICK))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PEDESTAL_ARCANE.get()), 10)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TCItems.SLAB_ARCANE_STONE)
                .define('B', TCItems.STONE_ARCANE)
                .gate(gate("infusion"))
                .unlockedBy("has", has(TCItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PEDESTAL_ANCIENT.get()), 150)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TCItems.SLAB_ANCIENT)
                .define('B', TCItems.STONE_ANCIENT)
                .gate(gate("infusion_ancient"))
                .unlockedBy("has", has(TCItems.STONE_ANCIENT))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PEDESTAL_ELDRITCH.get()), 150)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TCItems.SLAB_ELDRITCH)
                .define('B', TCItems.STONE_ELDRITCH_TILE)
                .gate(gate("infusion_eldritch"))
                .unlockedBy("has", has(TCItems.STONE_ELDRITCH_TILE))
                .save(output);
    }

    private void buildElementalToolRecipes() {
        ResearchGate gate = gate("elemental_tools");
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TCItems.ELEMENTAL_AXE.get(),
                                Map.of(InfusionEnchantment.COLLECTOR, 1, InfusionEnchantment.BURROWING, 1)),
                        Ingredient.of(TCItems.THAUMIUM_AXE.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AQUA.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AQUA.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PLANK_GREATWOOD.get()))
                .aspect(TCAspects.AQUA, 60)
                .aspect(TCAspects.HERBA, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TCItems.THAUMIUM_AXE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TCItems.ELEMENTAL_PICKAXE.get(),
                                Map.of(InfusionEnchantment.REFINING, 1, InfusionEnchantment.SOUNDING, 2)),
                        Ingredient.of(TCItems.THAUMIUM_PICKAXE.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_IGNIS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_IGNIS.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PLANK_GREATWOOD.get()))
                .aspect(TCAspects.IGNIS, 30)
                .aspect(TCAspects.METALLUM, 30)
                .aspect(TCAspects.SENSUS, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TCItems.THAUMIUM_PICKAXE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.COMBAT,
                        enchantedTool(TCItems.ELEMENTAL_SWORD.get(), Map.of(InfusionEnchantment.ARCING, 2)),
                        Ingredient.of(TCItems.THAUMIUM_SWORD.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PLANK_GREATWOOD.get()))
                .aspect(TCAspects.AER, 30)
                .aspect(TCAspects.MOTUS, 30)
                .aspect(TCAspects.AVERSIO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TCItems.THAUMIUM_SWORD))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(TCItems.ELEMENTAL_SHOVEL.get(), Map.of(InfusionEnchantment.DESTRUCTIVE, 1)),
                        Ingredient.of(TCItems.THAUMIUM_SHOVEL.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_TERRA.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_TERRA.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PLANK_GREATWOOD.get()))
                .aspect(TCAspects.TERRA, 60)
                .aspect(TCAspects.FABRICO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TCItems.THAUMIUM_SHOVEL))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TCItems.ELEMENTAL_HOE.get()),
                        Ingredient.of(TCItems.THAUMIUM_HOE.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_ORDO.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_PERDITIO.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PLANK_GREATWOOD.get()))
                .aspect(TCAspects.ORDO, 30)
                .aspect(TCAspects.HERBA, 30)
                .aspect(TCAspects.PERDITIO, 30)
                .instability(1)
                .gate(gate)
                .unlockedBy("has", has(TCItems.THAUMIUM_HOE))
                .save(output);
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        enchantedTool(
                                TCItems.PRIMAL_CRUSHER.get(),
                                Map.of(InfusionEnchantment.DESTRUCTIVE, 1, InfusionEnchantment.REFINING, 1)),
                        Ingredient.of(TCItems.PRIMORDIAL_PEARL.get()))
                .component(Ingredient.of(TCItems.VOID_PICKAXE.get()))
                .component(Ingredient.of(TCItems.VOID_SHOVEL.get()))
                .component(Ingredient.of(TCItems.ELEMENTAL_PICKAXE.get()))
                .component(Ingredient.of(TCItems.ELEMENTAL_SHOVEL.get()))
                .aspect(TCAspects.TERRA, 75)
                .aspect(TCAspects.INSTRUMENTUM, 75)
                .aspect(TCAspects.PERDITIO, 50)
                .aspect(TCAspects.VACUOS, 50)
                .aspect(TCAspects.AVERSIO, 50)
                .aspect(TCAspects.ALIENIS, 50)
                .aspect(TCAspects.DESIDERIUM, 50)
                .instability(6)
                .gate(gate("primal_crusher"))
                .unlockedBy("has", has(TCItems.PRIMORDIAL_PEARL))
                .save(output);
    }

    private void buildRechargePedestalRecipe() {
        arcaneShaped(new ItemStack(TCItems.RECHARGE_PEDESTAL.get()), 100)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern(" R ")
                .pattern("DID")
                .pattern("SSS")
                .define('R', TCItems.VIS_RESONATOR)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('S', Tags.Items.STONES)
                .gate(gate("recharge_pedestal"))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);
    }

    private void buildTravellerBootsRecipe() {
        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.TRAVELLER_BOOTS.get()),
                        Ingredient.of(Items.LEATHER_BOOTS))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(Items.FEATHER))
                .component(Ingredient.of(ItemTags.FISHES))
                .aspect(TCAspects.VOLATUS, 100)
                .aspect(TCAspects.MOTUS, 100)
                .instability(1)
                .gate(gate("boots_traveller"))
                .unlockedBy("has", has(Items.LEATHER_BOOTS))
                .save(output);
    }

    private static ItemStack enchantedTool(Item item, Map<InfusionEnchantment, Integer> enchantments) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(TCDataComponents.INFUSION_ENCHANTMENTS.get(), new InfusionEnchantments(enchantments))
                .build();
        return new ItemStack(item.builtInRegistryHolder(), 1, patch);
    }

    private void buildRunicAugmentRecipe() {
        HolderGetter<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList baseAspects = AspectList.of(
                new AspectInstance(aspects.getOrThrow(TCAspects.PRAEMUNIO), 40),
                new AspectInstance(aspects.getOrThrow(TCAspects.VITREUS), 20),
                new AspectInstance(aspects.getOrThrow(TCAspects.POTENTIA), 20));
        Ingredient amber = Ingredient.of(TCItemTags.GEMS_AMBER);
        InfusionRunicAugmentRecipe recipe = new InfusionRunicAugmentRecipe(
                List.of(Ingredient.of(TCItems.SALIS_MUNDUS.get()), amber),
                amber,
                baseAspects,
                Ingredient.of(Items.IRON_CHESTPLATE),
                Optional.of(gate("runic_shielding")));
        output.accept(TCIds.rl("runic_augment/runic_shielding"), recipe, null);
    }

    private void buildInfusionEnchantmentRecipes() {
        infusionEnchantment(InfusionEnchantment.BURROWING, Items.WOODEN_PICKAXE, Ingredient.of(Items.RABBIT_FOOT))
                .aspect(TCAspects.SENSUS, 80)
                .aspect(TCAspects.TERRA, 150)
                .save(output);
        infusionEnchantment(InfusionEnchantment.COLLECTOR, Items.STONE_AXE, Ingredient.of(Items.LEAD))
                .aspect(TCAspects.DESIDERIUM, 80)
                .aspect(TCAspects.AQUA, 100)
                .save(output);
        infusionEnchantment(InfusionEnchantment.DESTRUCTIVE, Items.STONE_PICKAXE, Ingredient.of(Items.TNT))
                .aspect(TCAspects.AVERSIO, 200)
                .aspect(TCAspects.PERDITIO, 250)
                .save(output);
        infusionEnchantment(InfusionEnchantment.REFINING, Items.IRON_PICKAXE, Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.ORDO, 80)
                .aspect(TCAspects.PERMUTATIO, 60)
                .save(output);
        infusionEnchantment(InfusionEnchantment.SOUNDING, Items.GOLDEN_PICKAXE, Ingredient.of(Items.MAP))
                .aspect(TCAspects.SENSUS, 40)
                .aspect(TCAspects.IGNIS, 60)
                .save(output);
        infusionEnchantment(InfusionEnchantment.ARCING, Items.WOODEN_SWORD, Ingredient.of(Items.REDSTONE_BLOCK))
                .aspect(TCAspects.POTENTIA, 40)
                .aspect(TCAspects.AER, 60)
                .save(output);
        infusionEnchantment(
                        InfusionEnchantment.ESSENCE, Items.STONE_SWORD, Ingredient.of(TCItems.ESSENTIA_CRYSTAL.get()))
                .aspect(TCAspects.BESTIA, 40)
                .aspect(TCAspects.VITIUM, 60)
                .save(output);
        infusionEnchantment(InfusionEnchantment.LAMPLIGHT, Items.GOLDEN_PICKAXE, Ingredient.of(TCItemTags.NITORS))
                .aspect(TCAspects.LUX, 80)
                .aspect(TCAspects.AER, 20)
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
                TCItemTags.INGOTS_THAUMIUM,
                TCItems.THAUMIUM_SWORD.get(),
                TCItems.THAUMIUM_PICKAXE.get(),
                TCItems.THAUMIUM_AXE.get(),
                TCItems.THAUMIUM_SHOVEL.get(),
                TCItems.THAUMIUM_HOE.get());
        toolRecipes(
                "void",
                TCItemTags.INGOTS_VOID_METAL,
                TCItems.VOID_SWORD.get(),
                TCItems.VOID_PICKAXE.get(),
                TCItems.VOID_AXE.get(),
                TCItems.VOID_SHOVEL.get(),
                TCItems.VOID_HOE.get());
        armorRecipes(
                "thaumium",
                TCItemTags.INGOTS_THAUMIUM,
                TCItems.THAUMIUM_HELM.get(),
                TCItems.THAUMIUM_CHEST.get(),
                TCItems.THAUMIUM_LEGS.get(),
                TCItems.THAUMIUM_BOOTS.get());
        armorRecipes(
                "void",
                TCItemTags.INGOTS_VOID_METAL,
                TCItems.VOID_HELM.get(),
                TCItems.VOID_CHEST.get(),
                TCItems.VOID_LEGS.get(),
                TCItems.VOID_BOOTS.get());
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
            arcaneShaped(new ItemStack(TCItems.BANNERS.get(dye).get()), 10)
                    .pattern("WS")
                    .pattern("WS")
                    .pattern("WB")
                    .define('W', wool(dye))
                    .define('S', Tags.Items.RODS_WOODEN)
                    .define('B', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_wool", has(ItemTags.WOOL))
                    .save(output, TCIds.MODID + ":arcane/banner_" + dye.getName());
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

        arcaneShapeless(new ItemStack(TCItems.INLAY.get(), 2), 25)
                .aspect(TCAspects.AQUA, 1)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Tags.Items.INGOTS_GOLD)
                .gate(gate("infusion_stable"))
                .unlockedBy("has", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PATTERN_CRAFTER.get()), 50)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("VH ")
                .pattern("GCG")
                .pattern(" W ")
                .define('H', Items.HOPPER)
                .define('W', TCBlocks.PLANK_GREATWOOD)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('V', TCItems.VIS_RESONATOR)
                .define('C', Items.CRAFTING_TABLE)
                .gate(gate("arcane_pattern_crafter"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TCItems.SCRIBING_TOOLS)
                .requires(TCItems.SCRIBING_TOOLS)
                .requires(Tags.Items.DYES_BLACK)
                .unlockedBy("has", has(TCItems.SCRIBING_TOOLS))
                .save(output, "thaumaturge:scribing_tools_refill");

        arcaneShaped(new ItemStack(TCBlocks.DECONSTRUCTION_TABLE.asItem()), 20)
                .aspect(TCAspects.PERDITIO, 1)
                .pattern(" S ")
                .pattern("ATP")
                .define('S', TCItems.THAUMOMETER)
                .define('T', TCBlocks.TABLE_WOOD.asItem())
                .define('A', Items.GOLDEN_AXE)
                .define('P', Items.GOLDEN_PICKAXE)
                .gate(gate("deconstructor"))
                .unlockedBy("has", has(TCItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.POTION_SPRAYER.get()), 75)
                .aspect(TCAspects.AQUA, 1)
                .aspect(TCAspects.IGNIS, 1)
                .pattern("BDB")
                .pattern("IAI")
                .pattern("ICI")
                .define('B', TCItemTags.PLATES_BRASS)
                .define('I', TCItemTags.PLATES_IRON)
                .define('A', Items.BREWING_STAND)
                .define('D', Items.DISPENSER)
                .define('C', TCItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("potion_sprayer"))
                .unlockedBy("has", has(TCItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SPA.get()), 50)
                .aspect(TCAspects.AQUA)
                .pattern("QIQ")
                .pattern("SJS")
                .pattern("SPS")
                .define('Q', Items.QUARTZ_BLOCK)
                .define('I', Items.IRON_BARS)
                .define('S', TCItems.STONE_ARCANE)
                .define('J', TCItems.JAR_NORMAL)
                .define('P', TCItems.MECHANISM_SIMPLE)
                .gate(gate("arcane_spa"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.FABRIC.get()), 5)
                .pattern(" S ")
                .pattern("SCS")
                .pattern(" S ")
                .define('S', Tags.Items.STRINGS)
                .define('C', ItemTags.WOOL)
                .gate(gate("unlock_infusion"))
                .unlockedBy("has", has(Tags.Items.STRINGS))
                .save(output);

        clothRecipe(TCItems.CLOTH_CHEST.get(), "I I", "III", "III");
        clothRecipe(TCItems.CLOTH_LEGS.get(), "III", "I I", "I I");
        clothRecipe(TCItems.CLOTH_BOOTS.get(), "I I", "I I", null);

        arcaneShaped(new ItemStack(TCItems.MECHANISM_SIMPLE.get()), 10)
                .aspect(TCAspects.IGNIS)
                .aspect(TCAspects.AQUA)
                .pattern(" B ")
                .pattern("ISI")
                .pattern(" B ")
                .define('B', TCItemTags.PLATES_BRASS)
                .define('I', TCItemTags.PLATES_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TCItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MECHANISM_COMPLEX.get()), 50)
                .aspect(TCAspects.IGNIS)
                .aspect(TCAspects.AQUA)
                .pattern(" M ")
                .pattern("TQT")
                .pattern(" M ")
                .define('T', TCItemTags.PLATES_THAUMIUM)
                .define('Q', Items.PISTON)
                .define('M', TCItems.MECHANISM_SIMPLE)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.MIRRORED_GLASS.get()), 50)
                .aspect(TCAspects.AQUA)
                .aspect(TCAspects.ORDO)
                .requires(TCItems.QUICKSILVER)
                .requires(Tags.Items.GLASS_PANES)
                .gate(gate("base_artifice"))
                .unlockedBy("has", has(TCItems.QUICKSILVER))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.FILTER.get(), 2), 15)
                .aspect(TCAspects.AQUA)
                .pattern("GWG")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('W', TCItems.PLANK_SILVERWOOD)
                .gate(gate("base_alchemy"))
                .unlockedBy("has", has(TCItems.PLANK_SILVERWOOD))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MORPHIC_RESONATOR.get()), 50)
                .aspect(TCAspects.AER)
                .aspect(TCAspects.IGNIS)
                .pattern(" G ")
                .pattern("BSB")
                .pattern(" G ")
                .define('G', Tags.Items.GLASS_PANES)
                .define('B', TCItemTags.PLATES_BRASS)
                .define('S', TCItems.NUGGET_QUICKSILVER)
                .gate(gate("base_alchemy"))
                .unlockedBy("has", has(TCItemTags.PLATES_BRASS))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.BOTTLE_TAINT.get()),
                        DataComponentIngredient.of(
                                false,
                                TCDataComponents.ASPECTS.get(),
                                AspectList.of(new AspectInstance(
                                        aspects.getOrThrow(TCAspects.VITIUM), PhialItem.BASE_AMOUNT)),
                                TCItems.PHIAL.get()))
                .aspect(TCAspects.VITIUM, 30)
                .aspect(TCAspects.AQUA, 30)
                .gate(gate("bottle_taint"))
                .unlockedBy("has", has(TCItems.PHIAL.get()))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.BATH_SALTS.get()),
                        Ingredient.of(TCItems.SALIS_MUNDUS))
                .aspect(TCAspects.COGNITIO, 40)
                .aspect(TCAspects.AER, 40)
                .aspect(TCAspects.ORDO, 40)
                .aspect(TCAspects.VICTUS, 40)
                .gate(gate("bath_salts"))
                .unlockedBy("has", has(TCItems.SALIS_MUNDUS))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.SANITY_SOAP.get()),
                        Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TCAspects.COGNITIO, 75)
                .aspect(TCAspects.ALIENIS, 50)
                .aspect(TCAspects.ORDO, 75)
                .aspect(TCAspects.VICTUS, 50)
                .gate(gate("sane_soap"))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, TCItems.TRIPLE_MEAT_TREAT)
                .requires(TCItemTags.MEAT_CHUNKS)
                .requires(TCItemTags.MEAT_CHUNKS)
                .requires(TCItemTags.MEAT_CHUNKS)
                .requires(Items.SUGAR)
                .unlockedBy("has", has(TCItemTags.MEAT_CHUNKS))
                .save(output);
    }

    private void clothRecipe(Item result, String row1, String row2, String row3) {
        ArcaneWorkbenchShapedRecipeBuilder builder =
                arcaneShaped(new ItemStack(result), 100).pattern(row1).pattern(row2);
        if (row3 != null) {
            builder.pattern(row3);
        }
        builder.define('I', TCItems.FABRIC)
                .gate(gate("unlock_infusion"))
                .unlockedBy("has", has(TCItems.FABRIC))
                .save(output);
    }

    private void buildCrystalClusterRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        crystalCluster(aspects, TCItems.CRYSTAL_AER, TCAspects.AER, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_IGNIS, TCAspects.IGNIS, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_AQUA, TCAspects.AQUA, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_TERRA, TCAspects.TERRA, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_ORDO, TCAspects.ORDO, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_PERDITIO, TCAspects.PERDITIO, 0);
        crystalCluster(aspects, TCItems.CRYSTAL_VITIUM, TCAspects.VITIUM, 4);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.ELDRITCH_EYE.get()),
                        Ingredient.of(Items.ENDER_EYE))
                .component(Ingredient.of(TCItems.VOID_SEED.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .aspect(TCAspects.ALIENIS, 64)
                .aspect(TCAspects.VACUOS, 16)
                .aspect(TCAspects.TENEBRAE, 16)
                .aspect(TCAspects.MOTUS, 16)
                .instability(5)
                .gate(gate("oculus"))
                .unlockedBy("has", has(Items.ENDER_EYE))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.CAUSALITY_COLLAPSER.get()),
                        Ingredient.of(Items.TNT))
                .component(Ingredient.of(TCItems.MORPHIC_RESONATOR.get()))
                .component(Ingredient.of(Items.REDSTONE_BLOCK))
                .component(Ingredient.of(TCItems.ALUMENTUM.get()))
                .component(Ingredient.of(TCItemTags.NITORS))
                .component(Ingredient.of(TCItems.VIS_RESONATOR.get()))
                .component(Ingredient.of(Items.REDSTONE_BLOCK))
                .component(Ingredient.of(TCItems.ALUMENTUM.get()))
                .component(Ingredient.of(TCItemTags.NITORS))
                .aspect(TCAspects.ALIENIS, 50)
                .aspect(TCAspects.VITIUM, 50)
                .instability(8)
                .gate(gate("rift_closer"))
                .unlockedBy("has", has(TCItems.MORPHIC_RESONATOR))
                .save(output);
    }

    private void crystalCluster(
            HolderLookup<IAspect> aspects, ItemLike cluster, ResourceKey<IAspect> aspect, int instability) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(cluster.asItem()), crystal(aspect))
                .component(Ingredient.of(Items.WHEAT_SEEDS))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(aspect, 10)
                .aspect(TCAspects.VITREUS, 10)
                .aspect(TCAspects.VINCULUM, 5)
                .instability(instability)
                .gate(gate("crystal_farmer"))
                .unlockedBy("has", has(TCItems.SALIS_MUNDUS))
                .save(output);
    }

    private void buildFocusRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.FOCUS_1.get()),
                        DataComponentIngredient.of(
                                false,
                                TCDataComponents.CRYSTAL_ASPECT.get(),
                                new AspectInstance(aspects.getOrThrow(TCAspects.ORDO), 1),
                                TCItems.ESSENTIA_CRYSTAL.get()))
                .gate(gate("unlock_auromancy"))
                .aspect(TCAspects.VITREUS, 20)
                .aspect(TCAspects.PRAECANTATIO, 10)
                .aspect(TCAspects.AURAM, 5)
                .unlockedBy("has", has(TCItems.ESSENTIA_CRYSTAL.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.FOCUS_2.get()),
                        Ingredient.of(TCItems.FOCUS_1.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(Tags.Items.GEMS_DIAMOND))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(Items.ENDER_PEARL))
                .aspect(TCAspects.PRAECANTATIO, 25)
                .aspect(TCAspects.ORDO, 50)
                .instability(3)
                .gate(gate("focus_advanced", 0))
                .unlockedBy("has", has(TCItems.FOCUS_1.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.FOCUS_3.get()),
                        Ingredient.of(TCItems.FOCUS_2.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(TCItems.PRIMORDIAL_PEARL.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(Items.NETHER_STAR))
                .aspect(TCAspects.PRAECANTATIO, 25)
                .aspect(TCAspects.ORDO, 50)
                .aspect(TCAspects.VACUOS, 100)
                .instability(5)
                .gate(gate("focus_greater", 0))
                .unlockedBy("has", has(TCItems.FOCUS_2.get()))
                .save(output);
    }

    private void buildCrucibleRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.TALLOW.get()),
                        Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TCAspects.IGNIS, 1)
                .gate(gate("hedge_alchemy", 0))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.LEATHER), Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TCAspects.AER, 3)
                .aspect(TCAspects.BESTIA, 3)
                .gate(gate("hedge_alchemy", 0))
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output, TCIds.MODID + ":crucible/leather");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.GUNPOWDER, 2), Ingredient.of(Items.GUNPOWDER))
                .aspect(TCAspects.IGNIS, 10)
                .aspect(TCAspects.PERDITIO, 10)
                .aspect(TCAspects.ALKIMIA, 5)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.GUNPOWDER))
                .save(output, TCIds.MODID + ":crucible/gunpowder");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.SLIME_BALL, 2),
                        Ingredient.of(Items.SLIME_BALL))
                .aspect(TCAspects.AQUA, 5)
                .aspect(TCAspects.VICTUS, 5)
                .aspect(TCAspects.ALKIMIA, 1)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.SLIME_BALL))
                .save(output, TCIds.MODID + ":crucible/slime_ball");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(Items.GLOWSTONE_DUST, 2),
                        Ingredient.of(Items.GLOWSTONE_DUST))
                .aspect(TCAspects.SENSUS, 5)
                .aspect(TCAspects.LUX, 10)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.GLOWSTONE_DUST))
                .save(output, TCIds.MODID + ":crucible/glowstone_dust");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.INK_SAC, 2), Ingredient.of(Items.INK_SAC))
                .aspect(TCAspects.AQUA, 2)
                .aspect(TCAspects.BESTIA, 2)
                .gate(gate("hedge_alchemy", 1))
                .unlockedBy("has", has(Items.INK_SAC))
                .save(output, TCIds.MODID + ":crucible/dye");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.CLAY_BALL), Ingredient.of(Items.DIRT))
                .aspect(TCAspects.AQUA, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.DIRT))
                .save(output, TCIds.MODID + ":crucible/clay_ball");

        new CrucibleRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(Items.STRING), Ingredient.of(Items.WHEAT))
                .aspect(TCAspects.BESTIA, 5)
                .aspect(TCAspects.FABRICO, 1)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.WHEAT))
                .save(output, TCIds.MODID + ":crucible/string");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.COBWEB), Ingredient.of(Items.STRING))
                .aspect(TCAspects.VINCULUM, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.STRING))
                .save(output, TCIds.MODID + ":crucible/cobweb");

        new CrucibleRecipeBuilder(
                        aspects, RecipeCategory.MISC, new ItemStack(Items.LAVA_BUCKET), Ingredient.of(Items.BUCKET))
                .aspect(TCAspects.IGNIS, 15)
                .aspect(TCAspects.TERRA, 5)
                .gate(gate("hedge_alchemy", 2))
                .unlockedBy("has", has(Items.BUCKET))
                .save(output, TCIds.MODID + ":crucible/lava_bucket");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.BUCKET_LIQUID_DEATH.get()),
                        Ingredient.of(Items.BUCKET))
                .aspect(TCAspects.MORTUUS, 100)
                .aspect(TCAspects.PERDITIO, 50)
                .aspect(TCAspects.ALKIMIA, 20)
                .gate(gate("liquid_death", 0))
                .unlockedBy("has", has(Items.BUCKET))
                .save(output, TCIds.MODID + ":crucible/liquid_death");

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.INGOT_BRASS.get()),
                        Ingredient.of(Tags.Items.INGOTS_COPPER))
                .aspect(TCAspects.INSTRUMENTUM, 5)
                .gate(gate("metallurgy", 0))
                .unlockedBy("has", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.INGOT_THAUMIUM.get()),
                        Ingredient.of(Tags.Items.INGOTS_IRON))
                .aspect(TCAspects.PRAECANTATIO, 5)
                .aspect(TCAspects.TERRA, 5)
                .gate(gate("metallurgy", 1))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.NITORS.get(DyeColor.YELLOW).get()),
                        Ingredient.of(Items.GLOWSTONE_DUST))
                .gate(gate("unlock_alchemy", 2))
                .aspect(TCAspects.POTENTIA, 10)
                .aspect(TCAspects.IGNIS, 10)
                .aspect(TCAspects.LUX, 10)
                .unlockedBy("has", has(Items.GLOWSTONE_DUST))
                .save(output);

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            new CrucibleRecipeBuilder(
                            aspects,
                            RecipeCategory.MISC,
                            new ItemStack(
                                    TCItems.ESSENTIA_CRYSTAL,
                                    1,
                                    DataComponentPatch.builder()
                                            .set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1))
                                            .build()),
                            Ingredient.of(TCItemTags.NUGGETS_QUARTZ))
                    .gate(gate("base_alchemy"))
                    .aspect(aspect, 2)
                    .unlockedBy("has", has(TCItemTags.NUGGETS_QUARTZ))
                    .save(
                            output,
                            TCIds.MODID + ":crucible/vis_crystal/"
                                    + aspect.getKey().location().getPath());
        });

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.INGOT_VOID.get()),
                        Ingredient.of(TCItems.VOID_SEED.get()))
                .gate(gate("base_eldritch"))
                .aspect(TCAspects.METALLUM, 10)
                .aspect(TCAspects.VITIUM, 5)
                .unlockedBy("has", has(TCItems.VOID_SEED.get()))
                .save(output, TCIds.MODID + ":crucible/void_ingot");

        clusterRecipe(TCItems.CLUSTER_IRON, Tags.Items.ORES_IRON);
        clusterRecipe(TCItems.CLUSTER_GOLD, Tags.Items.ORES_GOLD);
        clusterRecipe(TCItems.CLUSTER_COPPER, Tags.Items.ORES_COPPER);
        clusterRecipe(TCItems.CLUSTER_TIN, TCItemTags.ORES_TIN);
        clusterRecipe(TCItems.CLUSTER_SILVER, TCItemTags.ORES_SILVER);
        clusterRecipe(TCItems.CLUSTER_LEAD, TCItemTags.ORES_LEAD);
        clusterRecipe(TCItems.CLUSTER_CINNABAR, TCItemTags.ORES_CINNABAR);
        clusterRecipe(TCItems.CLUSTER_QUARTZ, Tags.Items.ORES_QUARTZ);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.ALUMENTUM.get()),
                        Ingredient.of(ItemTags.COALS))
                .gate(gate("alumentum"))
                .aspect(TCAspects.IGNIS, 10)
                .aspect(TCAspects.POTENTIA, 10)
                .aspect(TCAspects.PERDITIO, 5)
                .unlockedBy("has", has(ItemTags.COALS))
                .save(output);
    }

    private void clusterRecipe(ItemLike cluster, TagKey<Item> oreTag) {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        new CrucibleRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStack(cluster.asItem()), Ingredient.of(oreTag))
                .aspect(TCAspects.METALLUM, 5)
                .aspect(TCAspects.ORDO, 5)
                .gate(gate("metal_purification"))
                .unlockedBy("has", has(oreTag))
                .save(output.withConditions(new NotCondition(new TagEmptyCondition(oreTag))));
    }

    private void buildArcaneWorkbenchRecipes() {
        arcaneShaped(new ItemStack(TCItems.THAUMOMETER.get()), 20)
                .allAspects()
                .pattern(" G ")
                .pattern("GPG")
                .pattern(" G ")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('P', Tags.Items.GLASS_PANES)
                .gate(gate("first_steps", 1))
                .unlockedBy("has", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.VIS_RESONATOR.get()), 50)
                .aspect(TCAspects.AER)
                .aspect(TCAspects.AQUA)
                .requires(TCItemTags.PLATES_IRON)
                .requires(Tags.Items.GEMS_QUARTZ)
                .gate(gate("unlock_auromancy", 1))
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ARCANE_WORKBENCH_CHARGER.get()), 200)
                .aspect(TCAspects.AER, 2)
                .aspect(TCAspects.ORDO, 2)
                .pattern(" R ")
                .pattern("P P")
                .pattern("I I")
                .define('R', TCItems.VIS_RESONATOR)
                .define('P', TCItems.PLANK_GREATWOOD)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("workbench_charger"))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOGGLES_REVEALING.get()), 50)
                .pattern("LBL")
                .pattern("L L")
                .pattern("MBM")
                .define('L', Tags.Items.LEATHERS)
                .define('B', TCItemTags.INGOTS_BRASS)
                .define('M', TCItems.THAUMOMETER)
                .gate(gate("unlock_artifice"))
                .unlockedBy("has", has(TCItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ALEMBIC.get()), 50)
                .aspect(TCAspects.AQUA)
                .pattern("GFG")
                .pattern("PBP")
                .pattern("GFG")
                .define('G', TCItems.PLANK_GREATWOOD)
                .define('F', TCItems.FILTER)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('B', Items.BUCKET)
                .gate(gate("essentia_smelter"))
                .unlockedBy("has", has(TCItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SMELTER_BASIC.get()), 50)
                .aspect(TCAspects.IGNIS)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', ItemTags.STONE_TOOL_MATERIALS)
                .define('F', Items.FURNACE)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R', TCItems.CRUCIBLE)
                .gate(gate("essentia_smelter", 1))
                .unlockedBy("has", has(TCItems.CRUCIBLE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SMELTER_THAUMIUM.get()), 250)
                .aspect(TCAspects.IGNIS, 2)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TCItemTags.PLATES_THAUMIUM)
                .define('F', TCItems.ALCHEMICAL_CONSTRUCT)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R', TCItems.SMELTER_BASIC)
                .gate(gate("essentia_smelter_thaumium"))
                .unlockedBy("has", has(TCItems.SMELTER_BASIC))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SMELTER_VOID.get()), 750)
                .aspect(TCAspects.IGNIS, 3)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TCItemTags.PLATES_VOID_METAL)
                .define('F', TCItems.ADVANCED_ALCHEMICAL_CONSTRUCT)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R', TCItems.SMELTER_THAUMIUM)
                .gate(gate("essentia_smelter_void"))
                .unlockedBy("has", has(TCItems.SMELTER_THAUMIUM))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.JAR_NORMAL.get()), 5)
                .pattern("PRP")
                .pattern("P P")
                .pattern("PPP")
                .define('P', Tags.Items.GLASS_PANES)
                .define('R', ItemTags.WOODEN_SLABS)
                .gate(gate("warded_jars"))
                .unlockedBy("has", has(Tags.Items.GLASS_PANES))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.JAR_VOID.get()), 50)
                .aspect(TCAspects.PERDITIO)
                .requires(TCItems.JAR_NORMAL)
                .gate(gate("warded_jars"))
                .unlockedBy("has", has(TCItems.JAR_NORMAL))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.TUBE.get(), 8), 10)
                .pattern(" Q ")
                .pattern("PGP")
                .pattern(" B ")
                .define('Q', TCItemTags.NUGGETS_QUICKSILVER)
                .define('P', TCItemTags.PLATES_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', TCItemTags.NUGGETS_BRASS)
                .gate(gate("tubes"))
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.TUBE_RESTRICT.get()), 10)
                .aspect(TCAspects.TERRA)
                .requires(TCItems.TUBE)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.TUBE_ONEWAY.get()), 10)
                .aspect(TCAspects.AQUA)
                .requires(TCItems.TUBE)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.TUBE_FILTER.get()), 10)
                .requires(TCItems.TUBE)
                .requires(TCItems.FILTER)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.TUBE_VALVE.get()), 10)
                .requires(TCItems.TUBE)
                .requires(Items.LEVER)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.TUBE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.TUBE_BUFFER.get()), 25)
                .pattern("PVP")
                .pattern("TIT")
                .pattern("PRP")
                .define('P', TCItems.PHIAL)
                .define('V', TCItems.TUBE_VALVE)
                .define('T', TCItems.TUBE)
                .define('I', TCItemTags.PLATES_IRON)
                .define('R', TCItems.TUBE_RESTRICT)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.TUBE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SMELTER_AUX.get()), 100)
                .aspect(TCAspects.AER)
                .aspect(TCAspects.TERRA)
                .pattern("PVP")
                .pattern("BCB")
                .pattern("ILI")
                .define('P', TCItems.PLANK_GREATWOOD)
                .define('V', TCItems.TUBE_FILTER)
                .define('B', TCItemTags.PLATES_BRASS)
                .define('I', TCItemTags.PLATES_IRON)
                .define('C', TCItems.ALCHEMICAL_CONSTRUCT)
                .define('L', TCItems.BELLOWS)
                .gate(gate("improved_smelting"))
                .unlockedBy("has", has(TCItems.BELLOWS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.SMELTER_VENT.get()), 150)
                .aspect(TCAspects.AER)
                .pattern("IBI")
                .pattern("FCF")
                .pattern("IBI")
                .define('F', TCItems.FILTER)
                .define('B', TCItemTags.PLATES_BRASS)
                .define('I', TCItemTags.PLATES_IRON)
                .define('C', TCItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("improved_smelting_2"))
                .unlockedBy("has", has(TCItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ALCHEMICAL_CONSTRUCT.get(), 2), 75)
                .aspect(TCAspects.AQUA)
                .aspect(TCAspects.PERDITIO)
                .aspect(TCAspects.ORDO)
                .pattern("IAI")
                .pattern("VPV")
                .pattern("IAI")
                .define('A', TCItems.TUBE_VALVE)
                .define('V', TCItems.TUBE)
                .define('I', TCItemTags.PLATES_IRON)
                .define('P', TCItems.PLANK_GREATWOOD)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItemTags.PLATES_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get()), 200)
                .aspect(TCAspects.TERRA)
                .aspect(TCAspects.IGNIS)
                .pattern(" A ")
                .pattern("VPV")
                .pattern(" A ")
                .define('A', TCItems.ALCHEMICAL_CONSTRUCT)
                .define('V', TCItemTags.PLATES_VOID_METAL)
                .define('P', TCItems.PRIMORDIAL_PEARL)
                .gate(gate("essentia_smelter_void", 0))
                .unlockedBy("has", has(TCItems.ALCHEMICAL_CONSTRUCT))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.BELLOWS.get()), 25)
                .aspect(TCAspects.AER)
                .pattern("PP ")
                .pattern("LLI")
                .pattern("PP ")
                .define('P', ItemTags.PLANKS)
                .define('L', Tags.Items.LEATHERS)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("bellows"))
                .unlockedBy("has", has(Tags.Items.LEATHERS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.THAUMONOMICON_SHARING.get()), 500)
                .allAspects()
                .pattern(" B ")
                .pattern("MQM")
                .pattern(" B ")
                .define('B', TCItems.BRAIN)
                .define('M', TCItems.MIRROR)
                .define('Q', Items.WRITABLE_BOOK)
                .gate(gate("share_book", 1))
                .unlockedBy("has", has(TCItems.BRAIN))
                .save(output);
    }

    private Holder<IAspect> getAspect(ResourceKey<IAspect> key) {
        return registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private void buildGolemancyRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.GOLEM_BELL.get())
                .pattern(" QQ")
                .pattern(" QQ")
                .pattern("S  ")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('Q', Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOLEM_TOP_HAT.get()), 16)
                .aspect(TCAspects.ORDO, 1)
                .aspect(TCAspects.IGNIS, 1)
                .pattern(" C ")
                .pattern(" G ")
                .pattern("CCC")
                .define('C', Items.BLACK_WOOL)
                .define('G', Tags.Items.INGOTS_GOLD)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOLEM_FEZ.get()), 8)
                .aspect(TCAspects.AQUA, 1)
                .aspect(TCAspects.TERRA, 1)
                .pattern("CCS")
                .pattern("CCS")
                .pattern("  S")
                .define('C', Items.RED_WOOL)
                .define('S', Tags.Items.STRINGS)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOLEM_BOWTIE.get()), 8)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("CSC")
                .pattern("C C")
                .define('C', Items.BLACK_WOOL)
                .define('S', Tags.Items.STRINGS)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(ItemTags.WOOL))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOLEM_GLASSES.get()), 8)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("GIG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.GOLEM_VISOR.get()), 8)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("IHI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('H', Items.IRON_HELMET)
                .gate(gate("golem_accessories"))
                .unlockedBy("has", has(Tags.Items.INGOTS_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MIND_CLOCKWORK.get()), 25)
                .aspect(TCAspects.IGNIS, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern(" P ")
                .pattern("PGP")
                .pattern("BCB")
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('B', TCItems.PLATE_BRASS)
                .define('P', Tags.Items.GLASS_PANES)
                .define('C', Items.COMPARATOR)
                .gate(gate("mind_clockwork", 1))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.MIND_BIOTHAUMIC.get()),
                        Ingredient.of(TCItems.MIND_CLOCKWORK.get()))
                .component(Ingredient.of(TCItems.BRAIN.get()))
                .component(Ingredient.of(TCItems.MECHANISM_COMPLEX.get()))
                .aspect(TCAspects.COGNITIO, 50)
                .aspect(TCAspects.MACHINA, 25)
                .instability(4)
                .gate(gate("mind_biothaumic"))
                .unlockedBy("has", has(TCItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MODULE_VISION.get()), 50)
                .aspect(TCAspects.AQUA, 1)
                .pattern("B B")
                .pattern("E E")
                .pattern("PGP")
                .define('B', Items.GLASS_BOTTLE)
                .define('E', Items.FERMENTED_SPIDER_EYE)
                .define('P', TCItems.PLATE_BRASS)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .gate(gate("golem_vision"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MODULE_AGGRESSION.get()), 50)
                .aspect(TCAspects.IGNIS, 1)
                .pattern(" R ")
                .pattern("RTR")
                .pattern("PGP")
                .define('R', Tags.Items.GLASS_PANES)
                .define('T', Items.BLAZE_POWDER)
                .define('P', TCItems.PLATE_BRASS)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .gate(gate("seal_guard"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.LEVITATOR.get()), 35)
                .aspect(TCAspects.AER, 1)
                .pattern("WIW")
                .pattern("BNB")
                .pattern("WGW")
                .define('I', TCItems.PLATE_THAUMIUM)
                .define('N', TCItemTags.NITORS)
                .define('W', ItemTags.PLANKS)
                .define('B', TCItems.PLATE_IRON)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .gate(gate("levitator"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.SEAL_BLANK.get(), 3), 20)
                .aspect(TCAspects.AER, 1)
                .requires(Items.CLAY_BALL)
                .requires(TCItems.TALLOW.get())
                .requires(Tags.Items.DYES_RED)
                .requires(TCItemTags.NITORS)
                .gate(gate("control_seals"))
                .unlockedBy("has", has(TCItems.TALLOW))
                .save(output);

        sealCrucible(
                aspects,
                gate("seal_collect"),
                TCItems.SEAL_PICKUP,
                TCItems.SEAL_BLANK,
                builder -> builder.aspect(TCAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_collect"),
                TCItems.SEAL_PICKUP_ADVANCED,
                TCItems.SEAL_PICKUP,
                builder -> builder.aspect(TCAspects.SENSUS, 10).aspect(TCAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_store"),
                TCItems.SEAL_FILL,
                TCItems.SEAL_BLANK,
                builder -> builder.aspect(TCAspects.AVERSIO, 10));
        sealCrucible(
                aspects,
                gate("seal_store"),
                TCItems.SEAL_FILL_ADVANCED,
                TCItems.SEAL_FILL,
                builder -> builder.aspect(TCAspects.SENSUS, 10).aspect(TCAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_empty"),
                TCItems.SEAL_EMPTY,
                TCItems.SEAL_BLANK,
                builder -> builder.aspect(TCAspects.VACUOS, 10));
        sealCrucible(
                aspects,
                gate("seal_empty"),
                TCItems.SEAL_EMPTY_ADVANCED,
                TCItems.SEAL_EMPTY,
                builder -> builder.aspect(TCAspects.SENSUS, 10).aspect(TCAspects.COGNITIO, 10));
        sealCrucible(
                aspects,
                gate("seal_provide"),
                TCItems.SEAL_PROVIDER,
                TCItems.SEAL_EMPTY_ADVANCED,
                builder -> builder.aspect(TCAspects.PERMUTATIO, 10).aspect(TCAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_stock"),
                TCItems.SEAL_STOCK,
                TCItems.SEAL_FILL,
                builder -> builder.aspect(TCAspects.COGNITIO, 10).aspect(TCAspects.DESIDERIUM, 10));
        sealCrucible(
                aspects,
                gate("seal_guard"),
                TCItems.SEAL_GUARD,
                TCItems.SEAL_BLANK,
                builder -> builder.aspect(TCAspects.AVERSIO, 20).aspect(TCAspects.PRAEMUNIO, 20));
        sealCrucible(
                aspects,
                gate("seal_guard"),
                TCItems.SEAL_GUARD_ADVANCED,
                TCItems.SEAL_GUARD,
                builder -> builder.aspect(TCAspects.SENSUS, 20).aspect(TCAspects.COGNITIO, 20));
        sealCrucible(
                aspects,
                gate("seal_lumber"),
                TCItems.SEAL_LUMBER,
                TCItems.SEAL_BREAKER,
                builder -> builder.aspect(TCAspects.HERBA, 40).aspect(TCAspects.SENSUS, 20));
        sealCrucible(
                aspects,
                gate("seal_use"),
                TCItems.SEAL_USE,
                TCItems.SEAL_BLANK,
                builder -> builder.aspect(TCAspects.FABRICO, 20)
                        .aspect(TCAspects.SENSUS, 10)
                        .aspect(TCAspects.COGNITIO, 20));
        sealCrucible(
                aspects,
                gate("seal_break"),
                TCItems.SEAL_BREAKER_ADVANCED,
                TCItems.SEAL_BREAKER,
                builder -> builder.aspect(TCAspects.SENSUS, 10)
                        .aspect(TCAspects.COGNITIO, 10)
                        .aspect(TCAspects.INSTRUMENTUM, 20));

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.SEAL_HARVEST.get()),
                        Ingredient.of(TCItems.SEAL_BLANK.get()))
                .component(Ingredient.of(Items.WHEAT_SEEDS))
                .component(Ingredient.of(Items.PUMPKIN_SEEDS))
                .component(Ingredient.of(Items.MELON_SEEDS))
                .component(Ingredient.of(Items.BEETROOT_SEEDS))
                .component(Ingredient.of(Items.SUGAR_CANE))
                .component(Ingredient.of(Items.CACTUS))
                .aspect(TCAspects.HERBA, 10)
                .aspect(TCAspects.SENSUS, 10)
                .aspect(TCAspects.HUMANUS, 10)
                .instability(0)
                .gate(gate("seal_harvest"))
                .unlockedBy("has", has(TCItems.SEAL_BLANK))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.SEAL_BUTCHER.get()),
                        Ingredient.of(TCItems.SEAL_GUARD.get()))
                .component(Ingredient.of(Items.LEATHER))
                .component(Ingredient.of(ItemTags.WOOL))
                .component(Ingredient.of(Items.RABBIT_HIDE))
                .component(Ingredient.of(Items.PORKCHOP))
                .component(Ingredient.of(Items.MUTTON))
                .component(Ingredient.of(Items.BEEF))
                .aspect(TCAspects.BESTIA, 10)
                .aspect(TCAspects.SENSUS, 10)
                .aspect(TCAspects.HUMANUS, 10)
                .instability(0)
                .gate(gate("seal_butcher"))
                .unlockedBy("has", has(TCItems.SEAL_GUARD))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.SEAL_BREAKER.get()),
                        Ingredient.of(TCItems.SEAL_BLANK.get()))
                .component(Ingredient.of(Items.GOLDEN_AXE))
                .component(Ingredient.of(Items.GOLDEN_PICKAXE))
                .component(Ingredient.of(Items.GOLDEN_SHOVEL))
                .aspect(TCAspects.INSTRUMENTUM, 10)
                .aspect(TCAspects.PERDITIO, 10)
                .aspect(TCAspects.HUMANUS, 10)
                .instability(1)
                .gate(gate("seal_break"))
                .unlockedBy("has", has(TCItems.SEAL_BLANK))
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
                        new ItemStack(TCItems.MIRROR.get()),
                        Ingredient.of(TCItems.MIRRORED_GLASS.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.ENDER_PEARL))
                .aspect(TCAspects.MOTUS, 25)
                .aspect(TCAspects.TENEBRAE, 25)
                .aspect(TCAspects.PERMUTATIO, 25)
                .instability(1)
                .gate(gate("mirror"))
                .unlockedBy("has", has(TCItems.MIRRORED_GLASS))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.TOOLS,
                        new ItemStack(TCItems.HAND_MIRROR.get()),
                        Ingredient.of(TCItems.MIRROR.get()))
                .component(Ingredient.of(Items.STICK))
                .component(Ingredient.of(Items.COMPASS))
                .component(Ingredient.of(Items.MAP))
                .aspect(TCAspects.INSTRUMENTUM, 50)
                .aspect(TCAspects.MOTUS, 50)
                .instability(5)
                .gate(gate("mirror_hand"))
                .unlockedBy("has", has(TCItems.MIRROR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.DECORATIONS,
                        new ItemStack(TCItems.MIRROR_ESSENTIA.get()),
                        Ingredient.of(TCItems.MIRRORED_GLASS.get()))
                .component(Ingredient.of(Items.IRON_INGOT))
                .component(Ingredient.of(Items.IRON_INGOT))
                .component(Ingredient.of(Items.IRON_INGOT))
                .component(Ingredient.of(Items.ENDER_PEARL))
                .aspect(TCAspects.MOTUS, 25)
                .aspect(TCAspects.AQUA, 25)
                .aspect(TCAspects.PERMUTATIO, 25)
                .instability(2)
                .gate(gate("mirror_essentia"))
                .unlockedBy("has", has(TCItems.MIRRORED_GLASS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MATRIX_SPEED.get()), 500)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("SNS")
                .pattern("NGN")
                .pattern("SNS")
                .define('S', TCItems.STONE_ARCANE)
                .define('N', TCItemTags.NITORS)
                .define('G', Items.DIAMOND_BLOCK)
                .gate(gate("infusion_boost"))
                .unlockedBy("has", has(TCItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.MATRIX_COST.get()), 500)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.AQUA, 1)
                .aspect(TCAspects.PERDITIO, 1)
                .pattern("SAS")
                .pattern("AGA")
                .pattern("SAS")
                .define('S', TCItems.STONE_ARCANE)
                .define('A', TCItems.ALUMENTUM)
                .define('G', Items.DIAMOND_BLOCK)
                .gate(gate("infusion_boost"))
                .unlockedBy("has", has(TCItems.STONE_ARCANE))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.DIOPTRA.get()), 50)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("APA")
                .pattern("IGI")
                .pattern("AAA")
                .define('A', TCItems.STONE_ARCANE)
                .define('P', TCItems.VIS_RESONATOR)
                .define('G', TCItems.THAUMOMETER)
                .define('I', TCItems.PLATE_IRON)
                .gate(gate("dioptra"))
                .unlockedBy("has", has(TCItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.VIS_BATTERY.get()), 50)
                .aspect(TCAspects.AER, 2)
                .aspect(TCAspects.TERRA, 2)
                .aspect(TCAspects.AQUA, 2)
                .aspect(TCAspects.IGNIS, 2)
                .aspect(TCAspects.ORDO, 2)
                .aspect(TCAspects.PERDITIO, 2)
                .pattern("SSS")
                .pattern("SRS")
                .pattern("SSS")
                .define('S', TCItems.SLAB_ARCANE_STONE)
                .define('R', TCItems.VIS_RESONATOR)
                .gate(gate("vis_battery"))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.JAR_BRAIN.get()),
                        Ingredient.of(TCItems.JAR_NORMAL.get()))
                .component(Ingredient.of(TCItems.BRAIN.get()))
                .component(Ingredient.of(Items.SPIDER_EYE))
                .component(Ingredient.of(Items.WATER_BUCKET))
                .component(Ingredient.of(Items.SPIDER_EYE))
                .aspect(TCAspects.COGNITIO, 25)
                .aspect(TCAspects.SENSUS, 25)
                .aspect(TCAspects.EXANIMIS, 25)
                .instability(4)
                .gate(gate("jar_brain"))
                .unlockedBy("has", has(TCItems.JAR_NORMAL.get()))
                .save(output);
    }

    private void buildNoiseDeviceRecipes() {

        arcaneShaped(new ItemStack(TCItems.LAMP_ARCANE.get()), 50)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.IGNIS, 1)
                .pattern(" I ")
                .pattern("IAI")
                .pattern(" I ")
                .define('A', TCItems.AMBER_BLOCK)
                .define('I', TCItems.PLATE_IRON)
                .gate(gate("arcane_lamp"))
                .unlockedBy("has", has(TCItems.PLATE_IRON))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ARCANE_EAR.get()), 15)
                .aspect(TCAspects.AER, 1)
                .pattern("P P")
                .pattern(" G ")
                .pattern("WRW")
                .define('W', ItemTags.WOODEN_SLABS)
                .define('R', Items.REDSTONE)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('P', TCItems.PLATE_BRASS)
                .gate(gate("arcane_ear"))
                .unlockedBy("has", has(TCItems.PLATE_BRASS))
                .save(output);

        arcaneShapeless(new ItemStack(TCItems.ARCANE_EAR_TOGGLE.get()), 5)
                .requires(TCItems.ARCANE_EAR.get())
                .requires(Items.LEVER)
                .gate(gate("arcane_ear"))
                .unlockedBy("has", has(TCItems.ARCANE_EAR.get()))
                .save(output, TCIds.MODID + ":arcane_ear_toggle");

        arcaneShaped(new ItemStack(TCItems.HUNGRY_CHEST.get()), 15)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("WTW")
                .pattern("W W")
                .pattern("WWW")
                .define('W', TCItems.PLANK_GREATWOOD)
                .define('T', ItemTags.WOODEN_TRAPDOORS)
                .gate(gate("hungry_chest"))
                .unlockedBy("has", has(TCItems.PLANK_GREATWOOD))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.CENTRIFUGE.get()), 100)
                .aspect(TCAspects.ORDO, 1)
                .aspect(TCAspects.PERDITIO, 1)
                .pattern(" T ")
                .pattern("RCP")
                .pattern(" T ")
                .define('T', TCItems.TUBE)
                .define('P', TCItems.MECHANISM_SIMPLE)
                .define('R', TCItems.MORPHIC_RESONATOR)
                .define('C', TCItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("centrifuge"))
                .unlockedBy("has", has(TCItems.MORPHIC_RESONATOR))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.LAMP_GROWTH.get()),
                        Ingredient.of(TCItems.LAMP_ARCANE.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.BONE_MEAL))
                .component(Ingredient.of(TCItems.CRYSTAL_TERRA.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.BONE_MEAL))
                .component(Ingredient.of(TCItems.CRYSTAL_TERRA.get()))
                .aspect(TCAspects.HERBA, 20)
                .aspect(TCAspects.LUX, 15)
                .aspect(TCAspects.VICTUS, 15)
                .aspect(TCAspects.INSTRUMENTUM, 15)
                .instability(4)
                .gate(gate("lamp_growth"))
                .unlockedBy("has", has(TCItems.LAMP_ARCANE.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.LAMP_FERTILITY.get()),
                        Ingredient.of(TCItems.LAMP_ARCANE.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.WHEAT))
                .component(Ingredient.of(TCItems.CRYSTAL_IGNIS.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.CARROT))
                .component(Ingredient.of(TCItems.CRYSTAL_IGNIS.get()))
                .aspect(TCAspects.BESTIA, 20)
                .aspect(TCAspects.LUX, 15)
                .aspect(TCAspects.VICTUS, 15)
                .aspect(TCAspects.DESIDERIUM, 15)
                .instability(4)
                .gate(gate("lamp_fertility"))
                .unlockedBy("has", has(TCItems.LAMP_ARCANE.get()))
                .save(output);
    }

    private void buildEssentiaMachineRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.EVERFULL_URN.get()),
                        Ingredient.of(Items.FLOWER_POT))
                .aspect(TCAspects.AQUA, 30)
                .aspect(TCAspects.FABRICO, 10)
                .aspect(TCAspects.TERRA, 10)
                .gate(gate("everfull_urn"))
                .unlockedBy("has", has(Items.FLOWER_POT))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.VIS_GENERATOR.get()), 25)
                .aspect(TCAspects.IGNIS, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("WSW")
                .pattern("EPE")
                .pattern("WRW")
                .define('R', TCItems.VIS_RESONATOR)
                .define('E', TCItems.NUGGET_BRASS)
                .define('S', Items.REDSTONE)
                .define('P', Items.PISTON)
                .define('W', ItemTags.PLANKS)
                .gate(gate("vis_generator"))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ESSENTIA_INPUT.get()), 100)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("BQB")
                .pattern("IGI")
                .define('I', TCItemTags.PLATES_IRON)
                .define('B', TCItems.PLATE_BRASS)
                .define('Q', Items.DISPENSER)
                .define('G', TCItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("essentia_transport"))
                .unlockedBy("has", has(TCItems.PLATE_BRASS))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.ESSENTIA_OUTPUT.get()), 100)
                .aspect(TCAspects.AER, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("BQB")
                .pattern("IGI")
                .define('I', TCItemTags.PLATES_IRON)
                .define('B', TCItems.PLATE_BRASS)
                .define('Q', Items.HOPPER)
                .define('G', TCItems.ALCHEMICAL_CONSTRUCT)
                .gate(gate("essentia_transport"))
                .unlockedBy("has", has(TCItems.PLATE_BRASS))
                .save(output);
    }

    private void buildFluxMachineRecipes() {

        arcaneShaped(new ItemStack(TCItems.BRAIN_BOX.get()), 50)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.ORDO, 1)
                .pattern("IAI")
                .pattern("ABA")
                .pattern("IAI")
                .define('B', TCItems.MIND_CLOCKWORK)
                .define('A', TCItems.AMBER)
                .define('I', TCItems.PLATE_IRON)
                .gate(gate("thaumatorium"))
                .unlockedBy("has", has(TCItems.MIND_CLOCKWORK))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.CONDENSER.get()), 500)
                .aspect(TCAspects.AER, 5)
                .aspect(TCAspects.AQUA, 5)
                .aspect(TCAspects.PERDITIO, 5)
                .pattern("BCB")
                .pattern("WMW")
                .pattern("BTB")
                .define('T', TCItems.TUBE)
                .define('C', TCItems.MORPHIC_RESONATOR)
                .define('W', ItemTags.PLANKS)
                .define('M', TCItems.MECHANISM_COMPLEX)
                .define('B', TCItems.PLATE_BRASS)
                .gate(gate("flux_cleanup"))
                .unlockedBy("has", has(TCItems.MORPHIC_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.CONDENSER_LATTICE.get()), 100)
                .aspect(TCAspects.TERRA, 3)
                .aspect(TCAspects.AER, 3)
                .pattern("QTQ")
                .pattern("QFQ")
                .pattern("QTQ")
                .define('T', TCItems.PLATE_THAUMIUM)
                .define('F', TCItems.FILTER)
                .define('Q', Items.QUARTZ)
                .gate(gate("flux_cleanup"))
                .unlockedBy("has", has(TCItems.FILTER))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.STABILIZER.get()), 250)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .aspect(TCAspects.PERDITIO, 1)
                .pattern("SRS")
                .pattern("BVB")
                .pattern("IMI")
                .define('R', Items.REDSTONE_BLOCK)
                .define('S', TCItems.SLAB_ARCANE_STONE)
                .define('B', TCItems.STONE_ARCANE)
                .define('M', TCItems.MECHANISM_COMPLEX)
                .define('V', TCItems.VIS_RESONATOR)
                .define('I', TCItems.PLATE_IRON)
                .gate(gate("infusion_stable"))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.REDSTONE_RELAY.get()), 10)
                .aspect(TCAspects.ORDO, 1)
                .pattern("TGT")
                .pattern("SSS")
                .define('T', Items.REDSTONE_TORCH)
                .define('G', TCItems.MECHANISM_SIMPLE)
                .define('S', Items.STONE_SLAB)
                .gate(gate("redstone_relay"))
                .unlockedBy("has", has(TCItems.MECHANISM_SIMPLE))
                .save(output);

        new InfusionRecipeBuilder(
                        registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.VOID_SIPHON.get()),
                        Ingredient.of(TCItems.METAL_VOID_BLOCK.get()))
                .component(Ingredient.of(TCItems.STONE_ARCANE.get()))
                .component(Ingredient.of(TCItems.STONE_ARCANE.get()))
                .component(Ingredient.of(TCItems.MECHANISM_COMPLEX.get()))
                .component(Ingredient.of(TCItems.PLATE_BRASS.get()))
                .component(Ingredient.of(TCItems.PLATE_BRASS.get()))
                .component(Ingredient.of(Items.NETHER_STAR))
                .aspect(TCAspects.ALIENIS, 50)
                .aspect(TCAspects.PERDITIO, 50)
                .aspect(TCAspects.VACUOS, 100)
                .aspect(TCAspects.FABRICO, 50)
                .instability(7)
                .gate(gate("void_siphon"))
                .unlockedBy("has", has(TCItems.METAL_VOID_BLOCK.get()))
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
                TCDataComponents.CRYSTAL_ASPECT.get(),
                new AspectInstance(aspects.getOrThrow(aspect), 1),
                TCItems.ESSENTIA_CRYSTAL.get());
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
                .set(TCDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre))
                .build();
        return new ItemStack(TCItems.WAND, 1, patch);
    }

    private void buildWandRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        ShapedRecipePattern starterPattern = ShapedRecipePattern.of(
                Map.of('I', Ingredient.of(TCItems.WAND_CAP_IRON.get()), 'S', Ingredient.of(Tags.Items.RODS_WOODEN)),
                List.of("  I", " S ", "I  "));
        ShapedRecipe starter = new ShapedRecipe(
                "",
                CraftingBookCategory.EQUIPMENT,
                starterPattern,
                wandResult(TCWandParts.CAP_IRON.get(), TCWandParts.ROD_WOOD.get(), false));
        output.accept(TCIds.rl("wand/assembly/iron_wood"), starter, null);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.WAND_CAP_IRON)
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has", has(Tags.Items.NUGGETS_IRON))
                .save(output, TCIds.MODID + ":wand/part/wand_cap_iron");

        arcaneShaped(new ItemStack(TCItems.WAND_CAP_GOLD.get()), WAND_CAP_GOLD_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_GOLD)
                .gate(gate("cap_gold"))
                .unlockedBy("has", has(Tags.Items.NUGGETS_GOLD))
                .save(output, TCIds.MODID + ":wand/part/wand_cap_gold");

        arcaneShaped(new ItemStack(TCItems.WAND_CAP_COPPER.get()), WAND_CAP_COPPER_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', NUGGETS_COPPER)
                .gate(gate("cap_copper"))
                .unlockedBy("has", has(NUGGETS_COPPER))
                .save(
                        output.withConditions(new NotCondition(new TagEmptyCondition(NUGGETS_COPPER))),
                        TCIds.MODID + ":wand/part/wand_cap_copper");

        arcaneShaped(new ItemStack(TCItems.WAND_CAP_SILVER_INERT.get()), WAND_CAP_SILVER_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', NUGGETS_SILVER)
                .gate(gate("cap_silver"))
                .unlockedBy("has", has(NUGGETS_SILVER))
                .save(
                        output.withConditions(new NotCondition(new TagEmptyCondition(NUGGETS_SILVER))),
                        TCIds.MODID + ":wand/part/wand_cap_silver_inert");

        arcaneShaped(new ItemStack(TCItems.WAND_CAP_THAUMIUM_INERT.get()), WAND_CAP_THAUMIUM_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', TCItemTags.NUGGETS_THAUMIUM)
                .gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TCItemTags.NUGGETS_THAUMIUM))
                .save(output, TCIds.MODID + ":wand/part/wand_cap_thaumium_inert");

        arcaneShaped(new ItemStack(TCItems.WAND_CAP_VOID_INERT.get()), WAND_CAP_VOID_VIS)
                .pattern("NNN")
                .pattern("N N")
                .define('N', TCItemTags.NUGGETS_VOID_METAL)
                .gate(gate("cap_void"))
                .unlockedBy("has", has(TCItemTags.NUGGETS_VOID_METAL))
                .save(output, TCIds.MODID + ":wand/part/wand_cap_void_inert");

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.WAND_CAP_SILVER.get()),
                        Ingredient.of(TCItems.WAND_CAP_SILVER_INERT.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.POTENTIA, 8)
                .aspect(TCAspects.AURAM, 4)
                .instability(4)
                .gate(gate("cap_silver"))
                .unlockedBy("has", has(TCItems.WAND_CAP_SILVER_INERT.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.WAND_CAP_THAUMIUM.get()),
                        Ingredient.of(TCItems.WAND_CAP_THAUMIUM_INERT.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.POTENTIA, 12)
                .aspect(TCAspects.AURAM, 6)
                .instability(5)
                .gate(gate("cap_thaumium"))
                .unlockedBy("has", has(TCItems.WAND_CAP_THAUMIUM_INERT.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.WAND_CAP_VOID.get()),
                        Ingredient.of(TCItems.WAND_CAP_VOID_INERT.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.POTENTIA, 18)
                .aspect(TCAspects.VACUOS, 18)
                .aspect(TCAspects.ALIENIS, 18)
                .aspect(TCAspects.AURAM, 18)
                .instability(8)
                .gate(gate("cap_void"))
                .unlockedBy("has", has(TCItems.WAND_CAP_VOID_INERT.get()))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.WAND_ROD_GREATWOOD.get()), WAND_ROD_GREATWOOD_VIS)
                .pattern(" G")
                .pattern("G ")
                .define('G', TCItemTags.GREATWOOD_LOGS)
                .gate(gate("rod_greatwood"))
                .unlockedBy("has", has(TCItemTags.GREATWOOD_LOGS))
                .save(output, TCIds.MODID + ":wand/part/wand_rod_greatwood");

        elementalRodInfusion(
                aspects,
                TCItems.WAND_ROD_OBSIDIAN,
                Ingredient.of(Blocks.OBSIDIAN),
                TCAspects.TERRA,
                TCAspects.TENEBRAE,
                "rod_obsidian");
        elementalRodInfusion(
                aspects, TCItems.WAND_ROD_ICE, Ingredient.of(Blocks.ICE), TCAspects.AQUA, TCAspects.GELUM, "rod_ice");
        elementalRodInfusion(
                aspects,
                TCItems.WAND_ROD_QUARTZ,
                Ingredient.of(Blocks.QUARTZ_BLOCK),
                TCAspects.ORDO,
                TCAspects.VITREUS,
                "rod_quartz");
        elementalRodInfusion(
                aspects,
                TCItems.WAND_ROD_REED,
                Ingredient.of(Items.SUGAR_CANE),
                TCAspects.AER,
                TCAspects.MOTUS,
                "rod_reed");
        elementalRodInfusion(
                aspects,
                TCItems.WAND_ROD_BLAZE,
                Ingredient.of(Items.BLAZE_ROD),
                TCAspects.IGNIS,
                TCAspects.BESTIA,
                "rod_blaze");
        elementalRodInfusion(
                aspects,
                TCItems.WAND_ROD_BONE,
                Ingredient.of(Items.BONE),
                TCAspects.PERDITIO,
                TCAspects.EXANIMIS,
                "rod_bone");

        InfusionRecipeBuilder silverwoodRod = new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.WAND_ROD_SILVERWOOD.get()),
                        Ingredient.of(TCItemTags.SILVERWOOD_LOGS))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()));
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            silverwoodRod.component(crystal(primal)).aspect(primal, 9);
        }
        silverwoodRod
                .aspect(TCAspects.PRAECANTATIO, 9)
                .instability(5)
                .gate(gate("rod_silverwood"))
                .unlockedBy("has", has(TCItemTags.SILVERWOOD_LOGS))
                .save(output);

        staffCoreRecipe(TCItems.STAFF_ROD_GREATWOOD, TCItems.WAND_ROD_GREATWOOD, STAFF_GREATWOOD_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_OBSIDIAN, TCItems.WAND_ROD_OBSIDIAN, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_ICE, TCItems.WAND_ROD_ICE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_QUARTZ, TCItems.WAND_ROD_QUARTZ, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_REED, TCItems.WAND_ROD_REED, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_BLAZE, TCItems.WAND_ROD_BLAZE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_BONE, TCItems.WAND_ROD_BONE, STAFF_ELEMENTAL_VIS);
        staffCoreRecipe(TCItems.STAFF_ROD_SILVERWOOD, TCItems.WAND_ROD_SILVERWOOD, STAFF_SILVERWOOD_VIS);

        InfusionRecipeBuilder primalStaff = new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.STAFF_ROD_PRIMAL.get()),
                        Ingredient.of(TCItems.WAND_ROD_SILVERWOOD.get()))
                .component(Ingredient.of(TCItems.PRIMAL_CHARM.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_OBSIDIAN.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_ICE.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_QUARTZ.get()))
                .component(Ingredient.of(TCItems.PRIMAL_CHARM.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_REED.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_BLAZE.get()))
                .component(Ingredient.of(TCItems.WAND_ROD_BONE.get()));
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            primalStaff.aspect(primal, 32);
        }
        primalStaff
                .aspect(TCAspects.PRAECANTATIO, 64)
                .instability(8)
                .gate(gate("staff_primal"))
                .unlockedBy("has", has(TCItems.WAND_ROD_SILVERWOOD.get()))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.PRIMAL_CHARM.get()), PRIMAL_CHARM_VIS)
                .pattern("123")
                .pattern("ISI")
                .pattern("456")
                .define('1', crystal(TCAspects.AER))
                .define('2', crystal(TCAspects.IGNIS))
                .define('3', crystal(TCAspects.AQUA))
                .define('4', crystal(TCAspects.TERRA))
                .define('5', crystal(TCAspects.ORDO))
                .define('6', crystal(TCAspects.PERDITIO))
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('S', TCItems.SALIS_MUNDUS)
                .gate(gate("unlock_artifice"))
                .unlockedBy("has", has(TCItems.SALIS_MUNDUS))
                .save(output, TCIds.MODID + ":wand/part/primal_charm");
    }

    private static final int NODE_STABILIZER_VIS = 96;

    private void buildNodeHusbandryRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        arcaneShaped(new ItemStack(TCItems.NODE_STABILIZER.get()), NODE_STABILIZER_VIS)
                .pattern(" G ")
                .pattern("QPQ")
                .pattern("SNS")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .define('P', Blocks.PISTON)
                .define('S', TCItems.STONE_ARCANE_BRICK)
                .define('N', TCItemTags.NITORS)
                .gate(gate("node_stabilizer"))
                .unlockedBy("has", has(TCItemTags.NITORS))
                .save(output, TCIds.MODID + ":node_stabilizer");

        arcaneShaped(new ItemStack(TCItems.NODE_TRANSDUCER.get()), NODE_STABILIZER_VIS)
                .pattern("RCR")
                .pattern("ISI")
                .pattern("RAR")
                .define('R', Blocks.REDSTONE_BLOCK)
                .define('C', Items.COMPARATOR)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', TCItems.NODE_STABILIZER)
                .define('A', TCItemTags.NITORS)
                .gate(gate("node_transducer"))
                .unlockedBy("has", has(TCItems.NODE_STABILIZER))
                .save(output, TCIds.MODID + ":node_transducer");

        arcaneShaped(new ItemStack(TCItems.VIS_RELAY.get()), NODE_STABILIZER_VIS)
                .pattern(" A ")
                .pattern("GNG")
                .pattern(" S ")
                .define('A', Items.AMETHYST_SHARD)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('N', TCItemTags.NITORS)
                .define('S', TCItems.STONE_ARCANE)
                .gate(gate("vis_relay"))
                .unlockedBy("has", has(TCItemTags.NITORS))
                .save(output, TCIds.MODID + ":vis_relay");

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.NODE_STABILIZER_ADVANCED.get()),
                        Ingredient.of(TCItems.NODE_STABILIZER.get()))
                .component(Ingredient.of(TCItemTags.NITORS))
                .component(Ingredient.of(Blocks.REDSTONE_BLOCK))
                .component(Ingredient.of(TCItems.ALUMENTUM.get()))
                .component(Ingredient.of(Blocks.REDSTONE_BLOCK))
                .component(Ingredient.of(TCItemTags.NITORS))
                .component(Ingredient.of(Blocks.REDSTONE_BLOCK))
                .component(Ingredient.of(TCItems.ALUMENTUM.get()))
                .component(Ingredient.of(Blocks.REDSTONE_BLOCK))
                .aspect(TCAspects.AURAM, 32)
                .aspect(TCAspects.PRAECANTATIO, 16)
                .aspect(TCAspects.ORDO, 16)
                .aspect(TCAspects.POTENTIA, 16)
                .instability(10)
                .gate(gate("node_stabilizer_advanced"))
                .unlockedBy("has", has(TCItems.NODE_STABILIZER.get()))
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
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(crystal(primal))
                .aspect(primal, 12)
                .aspect(TCAspects.PRAECANTATIO, 6)
                .aspect(flavor, 6)
                .instability(3)
                .gate(gate(gateEntry))
                .unlockedBy("has", has(TCItems.SALIS_MUNDUS.get()))
                .save(output);
    }

    private void staffCoreRecipe(DeferredItem<? extends Item> core, DeferredItem<? extends Item> rod, int vis) {
        arcaneShaped(new ItemStack(core.get()), vis)
                .pattern("  S")
                .pattern(" G ")
                .pattern("G  ")
                .define('S', TCItems.PRIMAL_CHARM)
                .define('G', rod)
                .gate(gate("staves"))
                .unlockedBy("has", has(TCItems.PRIMAL_CHARM))
                .save(output, TCIds.MODID + ":wand/part/" + core.getId().getPath());
    }

    private void buildBaubleRecipes() {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.AMULET_MUNDANE)
                .pattern(" S ")
                .pattern("S S")
                .pattern(" I ")
                .define('S', Items.STRING)
                .define('I', TCItemTags.INGOTS_BRASS)
                .unlockedBy("has", has(TCItemTags.INGOTS_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.RING_MUNDANE)
                .pattern("NNN")
                .pattern("N N")
                .pattern("NNN")
                .define('N', TCItems.NUGGET_BRASS)
                .unlockedBy("has", has(TCItems.NUGGET_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.GIRDLE_MUNDANE)
                .pattern(" L ")
                .pattern("L L")
                .pattern(" I ")
                .define('L', Items.LEATHER)
                .define('I', TCItemTags.INGOTS_BRASS)
                .unlockedBy("has", has(TCItemTags.INGOTS_BRASS))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.AMULET_FANCY)
                .pattern(" S ")
                .pattern("SGS")
                .pattern(" I ")
                .define('S', Items.STRING)
                .define('G', Items.DIAMOND)
                .define('I', Items.GOLD_INGOT)
                .unlockedBy("has", has(Items.DIAMOND))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.RING_FANCY)
                .pattern("NGN")
                .pattern("N N")
                .pattern("NNN")
                .define('G', Items.DIAMOND)
                .define('N', Items.GOLD_NUGGET)
                .unlockedBy("has", has(Items.DIAMOND))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCItems.GIRDLE_FANCY)
                .pattern(" L ")
                .pattern("LGL")
                .pattern(" I ")
                .define('L', Items.LEATHER)
                .define('G', Items.DIAMOND)
                .define('I', Items.GOLD_INGOT)
                .unlockedBy("has", has(Items.DIAMOND))
                .save(output);

        arcaneShaped(new ItemStack(TCItems.FOCUS_POUCH.get()), 25)
                .pattern("LGL")
                .pattern("LBL")
                .pattern("LLL")
                .define('B', TCItems.GIRDLE_MUNDANE)
                .define('L', Items.LEATHER)
                .define('G', Items.GOLD_INGOT)
                .gate(gate("focus_pouch"))
                .unlockedBy("has", has(Items.LEATHER))
                .save(output);
        arcaneShaped(new ItemStack(TCItems.SANITY_CHECKER.get()), 20)
                .aspect(TCAspects.ORDO, 1)
                .aspect(TCAspects.PERDITIO, 1)
                .pattern("BN ")
                .pattern("M N")
                .pattern("BN ")
                .define('N', TCItemTags.NUGGETS_BRASS)
                .define('B', TCItems.BRAIN)
                .define('M', TCItems.MIRRORED_GLASS)
                .gate(gate("warp"))
                .unlockedBy("has", has(TCItems.MIRRORED_GLASS))
                .save(output);
        arcaneShaped(new ItemStack(TCItems.RESONATOR.get()), 50)
                .pattern("I I")
                .pattern("INI")
                .pattern(" S ")
                .define('I', TCItems.PLATE_IRON)
                .define('N', Items.QUARTZ)
                .define('S', Items.STICK)
                .gate(gate("tubes"))
                .unlockedBy("has", has(TCItems.PLATE_IRON))
                .save(output);
    }

    private void buildWearableInfusionRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.AMULET_VIS_CRAFTED.get()),
                        Ingredient.of(TCItems.AMULET_MUNDANE.get()))
                .component(Ingredient.of(TCItems.VIS_RESONATOR.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_IGNIS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AQUA.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_TERRA.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_ORDO.get()))
                .aspect(TCAspects.AURAM, 50)
                .aspect(TCAspects.POTENTIA, 100)
                .aspect(TCAspects.VACUOS, 50)
                .instability(6)
                .gate(gate("vis_amulet"))
                .unlockedBy("has", has(TCItems.AMULET_MUNDANE.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.VERDANT_CHARM.get()),
                        Ingredient.of(TCItems.AMULET_FANCY.get()))
                .component(Ingredient.of(TCItems.NUGGET_QUICKSILVER.get()))
                .component(crystal(TCAspects.VICTUS))
                .component(Ingredient.of(Items.MILK_BUCKET))
                .component(crystal(TCAspects.HERBA))
                .aspect(TCAspects.VICTUS, 60)
                .aspect(TCAspects.ORDO, 30)
                .aspect(TCAspects.HERBA, 60)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TCItems.AMULET_FANCY.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.VERDANT_CHARM.get()),
                        Ingredient.of(TCItems.VERDANT_CHARM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TCDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_LIFE)
                        .build())
                .component(Ingredient.of(Items.GOLDEN_APPLE))
                .component(crystal(TCAspects.VICTUS))
                .component(potion(Potions.STRONG_HEALING))
                .component(crystal(TCAspects.HUMANUS))
                .aspect(TCAspects.VICTUS, 80)
                .aspect(TCAspects.HUMANUS, 80)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TCItems.VERDANT_CHARM.get()))
                .save(output, TCIds.MODID + ":infusion/verdant_charm_life");
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.VERDANT_CHARM.get()),
                        Ingredient.of(TCItems.VERDANT_CHARM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TCDataComponents.VERDANT_TYPE.get(), VerdantCharmItem.TYPE_SUSTAIN)
                        .build())
                .component(Ingredient.of(TCItems.TRIPLE_MEAT_TREAT.get()))
                .component(crystal(TCAspects.DESIDERIUM))
                .component(potion(Potions.STRONG_REGENERATION))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .aspect(TCAspects.DESIDERIUM, 80)
                .aspect(TCAspects.AER, 80)
                .instability(5)
                .gate(gate("verdant_charms"))
                .unlockedBy("has", has(TCItems.VERDANT_CHARM.get()))
                .save(output, TCIds.MODID + ":infusion/verdant_charm_sustain");
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.CLOUD_RING.get()),
                        Ingredient.of(TCItems.RING_MUNDANE.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(Items.FEATHER))
                .aspect(TCAspects.AER, 50)
                .instability(1)
                .gate(gate("cloud_ring"))
                .unlockedBy("has", has(TCItems.RING_MUNDANE.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.CURIOSITY_BAND.get()),
                        Ingredient.of(TCItems.GIRDLE_FANCY.get()))
                .component(Ingredient.of(Items.EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Items.EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Items.EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .component(Ingredient.of(Items.EMERALD))
                .component(Ingredient.of(Items.WRITABLE_BOOK))
                .aspect(TCAspects.COGNITIO, 150)
                .aspect(TCAspects.VACUOS, 50)
                .aspect(TCAspects.VINCULUM, 100)
                .instability(5)
                .gate(gate("curiosity_band"))
                .unlockedBy("has", has(TCItems.GIRDLE_FANCY.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.CHARM_UNDYING.get()),
                        Ingredient.of(Items.TOTEM_OF_UNDYING))
                .component(Ingredient.of(TCItems.PLATE_BRASS.get()))
                .aspect(TCAspects.VICTUS, 25)
                .instability(2)
                .gate(gate("charm_undying"))
                .unlockedBy("has", has(Items.TOTEM_OF_UNDYING))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.MISC,
                        new ItemStack(TCItems.VOIDSEER_CHARM.get()),
                        Ingredient.of(TCItems.AMULET_FANCY.get()))
                .component(Ingredient.of(TCItems.BRAIN.get()))
                .component(Ingredient.of(TCItems.VOID_SEED.get()))
                .component(Ingredient.of(TCItems.BRAIN.get()))
                .component(Ingredient.of(TCItems.PRIMORDIAL_PEARL.get()))
                .aspect(TCAspects.COGNITIO, 150)
                .aspect(TCAspects.VACUOS, 150)
                .aspect(TCAspects.PRAECANTATIO, 100)
                .instability(8)
                .gate(gate("voidseer_pearl"))
                .unlockedBy("has", has(TCItems.PRIMORDIAL_PEARL.get()))
                .save(output);

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.FORTRESS_HELM.get()),
                        Ingredient.of(TCItems.THAUMIUM_HELM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.EMERALD))
                .aspect(TCAspects.METALLUM, 50)
                .aspect(TCAspects.PRAEMUNIO, 20)
                .aspect(TCAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TCItems.THAUMIUM_HELM.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.FORTRESS_CHEST.get()),
                        Ingredient.of(TCItems.THAUMIUM_CHEST.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.LEATHER))
                .aspect(TCAspects.METALLUM, 50)
                .aspect(TCAspects.PRAEMUNIO, 30)
                .aspect(TCAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TCItems.THAUMIUM_CHEST.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.FORTRESS_LEGS.get()),
                        Ingredient.of(TCItems.THAUMIUM_LEGS.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(TCItems.PLATE_THAUMIUM.get()))
                .component(Ingredient.of(Items.GOLD_INGOT))
                .component(Ingredient.of(Items.LEATHER))
                .aspect(TCAspects.METALLUM, 50)
                .aspect(TCAspects.PRAEMUNIO, 25)
                .aspect(TCAspects.POTENTIA, 25)
                .instability(3)
                .gate(gate("armor_fortress"))
                .unlockedBy("has", has(TCItems.THAUMIUM_LEGS.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.FORTRESS_HELM.get()),
                        Ingredient.of(TCItems.FORTRESS_HELM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TCDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)
                        .build())
                .component(Ingredient.of(Items.SLIME_BALL))
                .component(Ingredient.of(TCItems.GOGGLES_REVEALING.get()))
                .aspect(TCAspects.SENSUS, 40)
                .aspect(TCAspects.AURAM, 20)
                .aspect(TCAspects.PRAEMUNIO, 20)
                .instability(5)
                .gate(gate("fortress_mask"))
                .unlockedBy("has", has(TCItems.FORTRESS_HELM.get()))
                .save(output, TCIds.MODID + ":infusion/fortress_helm_goggles");
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                0,
                TCAspects.COGNITIO,
                TCAspects.VICTUS,
                Ingredient.of(Items.INK_SAC),
                Ingredient.of(TCItems.PLANT_SHIMMERLEAF.get()),
                Ingredient.of(TCItems.BRAIN.get()));
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                1,
                TCAspects.PERDITIO,
                TCAspects.MORTUUS,
                Ingredient.of(Items.BONE_MEAL),
                Ingredient.of(Items.POISONOUS_POTATO),
                Ingredient.of(Items.WITHER_SKELETON_SKULL));
        buildMaskRecipe(
                aspects,
                gate("fortress_mask"),
                2,
                TCAspects.EXANIMIS,
                TCAspects.VICTUS,
                Ingredient.of(Items.RED_DYE),
                Ingredient.of(Items.GHAST_TEAR),
                Ingredient.of(Items.MILK_BUCKET));

        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.VOID_ROBE_HELM.get()),
                        Ingredient.of(TCItems.VOID_HELM.get()))
                .component(Ingredient.of(TCItems.GOGGLES_REVEALING.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .aspect(TCAspects.METALLUM, 25)
                .aspect(TCAspects.SENSUS, 25)
                .aspect(TCAspects.PRAEMUNIO, 25)
                .aspect(TCAspects.POTENTIA, 25)
                .aspect(TCAspects.ALIENIS, 25)
                .aspect(TCAspects.VACUOS, 25)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TCItems.VOID_HELM.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.VOID_ROBE_CHEST.get()),
                        Ingredient.of(TCItems.VOID_CHEST.get()))
                .component(Ingredient.of(TCItems.CLOTH_CHEST.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(Items.LEATHER))
                .aspect(TCAspects.METALLUM, 35)
                .aspect(TCAspects.PRAEMUNIO, 35)
                .aspect(TCAspects.POTENTIA, 25)
                .aspect(TCAspects.ALIENIS, 25)
                .aspect(TCAspects.VACUOS, 35)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TCItems.VOID_CHEST.get()))
                .save(output);
        new InfusionRecipeBuilder(
                        aspects,
                        RecipeCategory.COMBAT,
                        new ItemStack(TCItems.VOID_ROBE_LEGS.get()),
                        Ingredient.of(TCItems.VOID_LEGS.get()))
                .component(Ingredient.of(TCItems.CLOTH_LEGS.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(Items.LEATHER))
                .aspect(TCAspects.METALLUM, 30)
                .aspect(TCAspects.PRAEMUNIO, 30)
                .aspect(TCAspects.POTENTIA, 25)
                .aspect(TCAspects.ALIENIS, 25)
                .aspect(TCAspects.VACUOS, 30)
                .instability(6)
                .gate(gate("void_robe_armor"))
                .unlockedBy("has", has(TCItems.VOID_LEGS.get()))
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
                        new ItemStack(TCItems.FORTRESS_HELM.get()),
                        Ingredient.of(TCItems.FORTRESS_HELM.get()))
                .catalystPatch(DataComponentPatch.builder()
                        .set(TCDataComponents.FORTRESS_MASK.get(), mask)
                        .build())
                .component(dye)
                .component(Ingredient.of(TCItems.PLATE_IRON.get()))
                .component(Ingredient.of(Items.LEATHER))
                .component(special1)
                .component(special2)
                .component(Ingredient.of(TCItems.PLATE_IRON.get()))
                .aspect(first, 80)
                .aspect(second, 80)
                .aspect(TCAspects.PRAEMUNIO, 20)
                .instability(8)
                .gate(gate)
                .unlockedBy("has", has(TCItems.FORTRESS_HELM.get()))
                .save(output, TCIds.MODID + ":infusion/fortress_helm_mask_" + mask);
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
                        new ItemStack(TCItems.THAUMONOMICON.get()),
                        Optional.of(gate("gotdream"))));
        dustTrigger(
                "crafting_tables_to_arcane_workbench",
                new DustTriggerTagRecipe(
                        Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES,
                        new ItemStack(TCBlocks.ARCANE_WORKBENCH.get().asItem()),
                        Optional.of(gate("first_steps", 0))));
        dustTrigger(
                "cauldron_to_crucible",
                new DustTriggerSimpleRecipe(
                        Blocks.CAULDRON,
                        new ItemStack(TCBlocks.CRUCIBLE.get().asItem()),
                        Optional.of(gate("unlock_alchemy", 0))));
        dustTrigger(
                "golem_press",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("golem_press"),
                        new ItemStack(TCBlocks.GOLEM_BUILDER.get().asItem()),
                        Optional.of(gate("mind_clockwork"))));
        dustTrigger(
                "infernal_furnace",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("infernal_furnace"),
                        new ItemStack(TCBlocks.INFERNAL_FURNACE.get().asItem()),
                        Optional.of(gate("infernal_furnace"))));
        dustTrigger(
                "infusion_altar",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("infusion_altar"),
                        new ItemStack(TCBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion"))));
        dustTrigger(
                "infusion_altar_ancient",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("infusion_altar_ancient"),
                        new ItemStack(TCBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion_ancient"))));
        dustTrigger(
                "infusion_altar_eldritch",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("infusion_altar_eldritch"),
                        new ItemStack(TCBlocks.INFUSION_MATRIX.get().asItem()),
                        Optional.of(gate("infusion_eldritch"))));
        dustTrigger(
                "thaumatorium",
                new DustTriggerMultiblockRecipe(
                        TCIds.rl("thaumatorium"),
                        new ItemStack(TCBlocks.THAUMATORIUM.get().asItem()),
                        Optional.of(gate("thaumatorium"))));
    }

    private void dustTrigger(String name, Recipe<?> recipe) {
        output.accept(TCIds.rl("dust_trigger/" + name), recipe, null);
    }

    private void buildSalisMundusRecipe() {
        SpecialRecipeBuilder.special(category -> SalisMundusRecipe.INSTANCE).save(output, "thaumaturge:salis_mundus");
    }
}
