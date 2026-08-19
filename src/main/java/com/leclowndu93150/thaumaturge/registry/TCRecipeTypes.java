package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipeDisplay;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TCIds.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DustTrigger>> DUST_TRIGGER = RECIPE_TYPES.register("dust_trigger", () -> new RecipeType<DustTrigger>() {
        @Override
        public String toString() {
            return "thaumaturge:dust_trigger";
        }
    });

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRecipe>> INFUSION = RECIPE_TYPES.register("infusion", () -> new RecipeType<InfusionRecipe>() {
        @Override
        public String toString() {
            return "thaumaturge:infusion";
        }
    });

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT = RECIPE_TYPES.register("infusion_enchantment",
            () -> new RecipeType<InfusionEnchantmentRecipe>() {
                @Override
                public String toString() {
                    return "thaumaturge:infusion_enchantment";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRunicAugmentRecipe>> RUNIC_AUGMENT = RECIPE_TYPES.register("runic_augment",
            () -> new RecipeType<InfusionRunicAugmentRecipe>() {
                @Override
                public String toString() {
                    return "thaumaturge:runic_augment";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipe>> CRUCIBLE = RECIPE_TYPES.register("crucible", () -> new RecipeType<CrucibleRecipe>() {
        @Override
        public String toString() {
            return "thaumaturge:crucible";
        }
    });

    public static final DeferredHolder<RecipeType<?>, RecipeType<ArcaneCraftingRecipe>> ARCANE = RECIPE_TYPES.register("arcane_workbench", () -> new RecipeType<ArcaneCraftingRecipe>() {
        @Override
        public String toString() {
            return "thaumaturge:arcane_workbench";
        }
    });

    private TCRecipeTypes() {}

    public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAYS = DeferredRegister.create(Registries.RECIPE_DISPLAY, TCIds.MODID);

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ArcaneCraftingRecipeDisplay>> ARCANE_DISPLAY = RECIPE_DISPLAYS.register("arcane_crafting",
            () -> new RecipeDisplay.Type<>(ArcaneCraftingRecipeDisplay.MAP_CODEC, ArcaneCraftingRecipeDisplay.STREAM_CODEC));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CrucibleRecipeDisplay>> CRUCIBLE_DISPLAY = RECIPE_DISPLAYS.register("crucible",
            () -> new RecipeDisplay.Type<>(CrucibleRecipeDisplay.MAP_CODEC, CrucibleRecipeDisplay.STREAM_CODEC));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<InfusionRecipeDisplay>> INFUSION_DISPLAY = RECIPE_DISPLAYS.register("infusion",
            () -> new RecipeDisplay.Type<>(InfusionRecipeDisplay.MAP_CODEC, InfusionRecipeDisplay.STREAM_CODEC));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<MultiblockRecipeDisplay>> MULTIBLOCK_DISPLAY = RECIPE_DISPLAYS.register("multiblock",
            () -> new RecipeDisplay.Type<>(MultiblockRecipeDisplay.MAP_CODEC, MultiblockRecipeDisplay.STREAM_CODEC));

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_DISPLAYS.register(modBus);
    }

    public static void registerSynchronizedRecipes(OnDatapackSyncEvent event) {
        List<RecipeType<?>> types = RECIPE_TYPES.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toUnmodifiableList());
        event.sendRecipes(types);
    }
}
