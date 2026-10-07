package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.spell.item.SpellPartIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TTIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TTIds.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<SpellPartIngredient>> SPELL_PART = INGREDIENT_TYPES.register("focus_element",
            () -> new IngredientType<>(SpellPartIngredient.CODEC));

    private TTIngredientTypes() {}

    public static void register(IEventBus modBus) {
        INGREDIENT_TYPES.register(modBus);
    }
}
