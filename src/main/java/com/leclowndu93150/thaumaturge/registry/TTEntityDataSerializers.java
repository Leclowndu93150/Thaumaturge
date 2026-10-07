package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TTEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, TTIds.MODID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<GolemProperties>> GOLEM_PROPERTIES = SERIALIZERS.register("golem_properties",
            () -> EntityDataSerializer.forValueType(GolemProperties.STREAM_CODEC));

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<GolemAccessoryStates>> GOLEM_ACCESSORY_STATES = SERIALIZERS.register("golem_accessory_states",
            () -> EntityDataSerializer.forValueType(GolemAccessoryStates.STREAM_CODEC));

    private TTEntityDataSerializers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
