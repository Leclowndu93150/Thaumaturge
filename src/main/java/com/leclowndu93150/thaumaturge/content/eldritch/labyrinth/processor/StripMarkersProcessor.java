package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor;

import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class StripMarkersProcessor extends StructureProcessor {
    public static final StripMarkersProcessor INSTANCE = new StripMarkersProcessor();
    public static final MapCodec<StripMarkersProcessor> CODEC = MapCodec.unit(INSTANCE);

    private StripMarkersProcessor() {}

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo process(
            LevelReader level,
            BlockPos targetPosition,
            BlockPos referencePos,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo processedBlockInfo,
            StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {
        if (processedBlockInfo.state().is(Blocks.STRUCTURE_BLOCK)) {
            return new StructureTemplate.StructureBlockInfo(
                    processedBlockInfo.pos(), Blocks.AIR.defaultBlockState(), null);
        }
        return processedBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TTStructureProcessors.STRIP_MARKERS.get();
    }
}
