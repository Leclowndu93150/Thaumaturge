package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.ChunkGeneratorOuter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCChunkGenerators {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, TCIds.MODID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkGeneratorOuter>> OUTER_LANDS = CHUNK_GENERATORS.register("outer_lands", () -> ChunkGeneratorOuter.CODEC);

    private TCChunkGenerators() {}

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
