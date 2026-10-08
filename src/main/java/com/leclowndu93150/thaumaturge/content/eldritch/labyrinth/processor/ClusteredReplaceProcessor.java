package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.LabyrinthNoise;
import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class ClusteredReplaceProcessor extends StructureProcessor {
    public static final MapCodec<ClusteredReplaceProcessor> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            RuleTest.CODEC.fieldOf("target").forGetter(processor -> processor.target),
                            SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
                                    .fieldOf("replacements")
                                    .forGetter(processor -> processor.replacements),
                            Codec.floatRange(0.0F, 1.0F)
                                    .fieldOf("threshold")
                                    .forGetter(processor -> processor.threshold),
                            ProcessorSeeds.PITCH_CODEC
                                    .optionalFieldOf("pitch", ProcessorSeeds.DEFAULT_PITCH)
                                    .forGetter(processor -> processor.pitch),
                            Codec.INT.optionalFieldOf("salt", 0).forGetter(processor -> processor.salt))
                    .apply(instance, ClusteredReplaceProcessor::new));

    private final RuleTest target;
    private final SimpleWeightedRandomList<BlockState> replacements;
    private final float threshold;
    private final int pitch;
    private final int salt;

    public ClusteredReplaceProcessor(
            RuleTest target, SimpleWeightedRandomList<BlockState> replacements, float threshold, int pitch, int salt) {
        this.target = target;
        this.replacements = replacements;
        this.threshold = threshold;
        this.pitch = pitch;
        this.salt = salt;
    }

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo process(
            LevelReader level,
            BlockPos targetPosition,
            BlockPos referencePos,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo processedBlockInfo,
            StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {
        BlockPos pos = processedBlockInfo.pos();
        if (!target.test(processedBlockInfo.state(), settings.getRandom(pos))) {
            return processedBlockInfo;
        }
        long seed = ProcessorSeeds.seed(settings);
        if (LabyrinthNoise.cluster(seed, pos.getX(), pos.getY(), pos.getZ(), pitch, salt) < threshold) {
            return processedBlockInfo;
        }
        BlockState replacement = ProcessorSeeds.pick(replacements, seed, pos.getX(), pos.getY(), pos.getZ(), salt + 1);
        return replacement == null
                ? processedBlockInfo
                : new StructureTemplate.StructureBlockInfo(pos, replacement, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TTStructureProcessors.CLUSTERED_REPLACE.get();
    }
}
