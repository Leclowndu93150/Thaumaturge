package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesWorldgenBiomeModifier;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCBiomeModifierSerializers {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TCIds.MODID);

    public static final Supplier<MapCodec<DynamicTreesWorldgenBiomeModifier>> DYNAMIC_TREES_WORLDGEN =
            SERIALIZERS.register("dynamic_trees_worldgen", () -> MapCodec.unit(DynamicTreesWorldgenBiomeModifier::new));

    private TCBiomeModifierSerializers() {}

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
