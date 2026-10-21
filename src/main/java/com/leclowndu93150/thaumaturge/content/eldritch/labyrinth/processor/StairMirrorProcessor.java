package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor;

import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class StairMirrorProcessor extends StructureProcessor {
    public static final StairMirrorProcessor INSTANCE = new StairMirrorProcessor();
    public static final MapCodec<StairMirrorProcessor> CODEC = MapCodec.unit(INSTANCE);

    private static final StairsShape[] SHAPES = StairsShape.values();

    private StairMirrorProcessor() {}

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo process(
            LevelReader level,
            BlockPos targetPosition,
            BlockPos referencePos,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo processedBlockInfo,
            StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {
        Mirror mirror = settings.getMirror();
        BlockState state = processedBlockInfo.state();
        if (mirror == Mirror.NONE
                || !state.hasProperty(BlockStateProperties.STAIRS_SHAPE)
                || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return processedBlockInfo;
        }
        Direction facing = mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        StairsShape shape = opposite(state.getValue(BlockStateProperties.STAIRS_SHAPE));
        for (StairsShape candidate : SHAPES) {
            BlockState placed = state.setValue(BlockStateProperties.STAIRS_SHAPE, candidate);
            BlockState mirrored = placed.mirror(mirror);
            if (mirrored.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
                    && mirrored.getValue(BlockStateProperties.STAIRS_SHAPE) == shape) {
                return placed == state
                        ? processedBlockInfo
                        : new StructureTemplate.StructureBlockInfo(
                                processedBlockInfo.pos(), placed, processedBlockInfo.nbt());
            }
        }
        return processedBlockInfo;
    }

    private static StairsShape opposite(StairsShape shape) {
        return switch (shape) {
            case STRAIGHT -> StairsShape.STRAIGHT;
            case INNER_LEFT -> StairsShape.INNER_RIGHT;
            case INNER_RIGHT -> StairsShape.INNER_LEFT;
            case OUTER_LEFT -> StairsShape.OUTER_RIGHT;
            case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
        };
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TTStructureProcessors.STAIR_MIRROR.get();
    }
}
