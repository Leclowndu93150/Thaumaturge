package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ClusteredReplaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ExposedSurfaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StripMarkersProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTStructureProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TTIds.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<StripMarkersProcessor>>
            STRIP_MARKERS = PROCESSORS.register("strip_markers", () -> () -> StripMarkersProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ExposedSurfaceProcessor>>
            EXPOSED_SURFACE = PROCESSORS.register("exposed_surface", () -> () -> ExposedSurfaceProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ClusteredReplaceProcessor>>
            CLUSTERED_REPLACE = PROCESSORS.register("clustered_replace", () -> () -> ClusteredReplaceProcessor.CODEC);

    private TTStructureProcessors() {}

    public static void register(IEventBus modBus) {
        PROCESSORS.register(modBus);
    }
}
