package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.label.LabelFilterRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TCIds.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerSimpleRecipe>> DUST_TRIGGER_SIMPLE = RECIPE_SERIALIZERS.register("dust_trigger_simple",
            () -> DustTriggerSimpleRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerTagRecipe>> DUST_TRIGGER_TAG = RECIPE_SERIALIZERS.register("dust_trigger_tag",
            () -> DustTriggerTagRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerMultiblockRecipe>> DUST_TRIGGER_MULTIBLOCK = RECIPE_SERIALIZERS.register("dust_trigger_multiblock",
            () -> DustTriggerMultiblockRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionRecipe>> INFUSION = RECIPE_SERIALIZERS.register("infusion", () -> InfusionRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT = RECIPE_SERIALIZERS.register("infusion_enchantment",
            () -> InfusionEnchantmentRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionRunicAugmentRecipe>> RUNIC_AUGMENT = RECIPE_SERIALIZERS.register("runic_augment",
            () -> InfusionRunicAugmentRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleRecipe>> CRUCIBLE = RECIPE_SERIALIZERS.register("crucible", () -> CrucibleRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArcaneShapedCraftingRecipe>> ARCANE_SHAPED = RECIPE_SERIALIZERS.register("arcane_workbench_shaped",
            () -> ArcaneShapedCraftingRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArcaneShapelessCraftingRecipe>> ARCANE_SHAPELESS = RECIPE_SERIALIZERS.register("arcane_workbench_shapeless",
            () -> ArcaneShapelessCraftingRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SalisMundusRecipe>> SALIS_MUNDUS = RECIPE_SERIALIZERS.register("salis_mundus",
            () -> new RecipeSerializer<>(SalisMundusRecipe.MAP_CODEC, SalisMundusRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LabelFilterRecipe>> LABEL_FILTER = RECIPE_SERIALIZERS.register("label_filter", () -> LabelFilterRecipe.SERIALIZER);

    private TCRecipeSerializers() {}

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
