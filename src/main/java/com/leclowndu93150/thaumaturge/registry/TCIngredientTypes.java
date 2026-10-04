package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.FocusElementIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TCIds.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<FocusElementIngredient>> FOCUS_ELEMENT =
            INGREDIENT_TYPES.register("focus_element", () -> new IngredientType<>(FocusElementIngredient.CODEC));

    private TCIngredientTypes() {}

    public static void register(IEventBus modBus) {
        INGREDIENT_TYPES.register(modBus);
    }
}
