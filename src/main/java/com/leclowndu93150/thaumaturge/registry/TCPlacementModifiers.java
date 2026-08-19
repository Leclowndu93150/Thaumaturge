package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigRarityFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TCIds.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigRarityFilter>> CRIMSON_PORTAL_RARITY = PLACEMENT_MODIFIERS.register("crimson_portal_rarity",
            () -> () -> ConfigRarityFilter.CODEC);

    private TCPlacementModifiers() {}

    public static void register(IEventBus bus) {
        PLACEMENT_MODIFIERS.register(bus);
    }
}
