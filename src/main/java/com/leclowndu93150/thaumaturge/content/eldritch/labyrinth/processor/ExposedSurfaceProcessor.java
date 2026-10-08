package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.LabyrinthNoise;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.StateOrientation;
import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class ExposedSurfaceProcessor extends StructureProcessor {
    private static final float CLUMP_GAIN = 2.0F;
    private static final Direction[] FACE_PRIORITY = {
        Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    public static final MapCodec<ExposedSurfaceProcessor> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            RuleTest.CODEC.fieldOf("target").forGetter(processor -> processor.target),
                            SimpleWeightedRandomList.wrappedCodecAllowingEmpty(BlockState.CODEC)
                                    .optionalFieldOf("floor", SimpleWeightedRandomList.empty())
                                    .forGetter(processor -> processor.floor),
                            SimpleWeightedRandomList.wrappedCodecAllowingEmpty(BlockState.CODEC)
                                    .optionalFieldOf("wall", SimpleWeightedRandomList.empty())
                                    .forGetter(processor -> processor.wall),
                            SimpleWeightedRandomList.wrappedCodecAllowingEmpty(BlockState.CODEC)
                                    .optionalFieldOf("ceiling", SimpleWeightedRandomList.empty())
                                    .forGetter(processor -> processor.ceiling),
                            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(processor -> processor.density),
                            ProcessorSeeds.PITCH_CODEC
                                    .optionalFieldOf("cluster_pitch", ProcessorSeeds.DEFAULT_PITCH)
                                    .forGetter(processor -> processor.clusterPitch),
                            Codec.INT.optionalFieldOf("salt", 0).forGetter(processor -> processor.salt))
                    .apply(instance, ExposedSurfaceProcessor::new));

    private final RuleTest target;
    private final SimpleWeightedRandomList<BlockState> floor;
    private final SimpleWeightedRandomList<BlockState> wall;
    private final SimpleWeightedRandomList<BlockState> ceiling;
    private final float density;
    private final int clusterPitch;
    private final int salt;

    public ExposedSurfaceProcessor(
            RuleTest target,
            SimpleWeightedRandomList<BlockState> floor,
            SimpleWeightedRandomList<BlockState> wall,
            SimpleWeightedRandomList<BlockState> ceiling,
            float density,
            int clusterPitch,
            int salt) {
        this.target = target;
        this.floor = floor;
        this.wall = wall;
        this.ceiling = ceiling;
        this.density = density;
        this.clusterPitch = clusterPitch;
        this.salt = salt;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
            ServerLevelAccessor level,
            BlockPos position,
            BlockPos referencePos,
            List<StructureTemplate.StructureBlockInfo> originalBlockInfoList,
            List<StructureTemplate.StructureBlockInfo> processedBlockInfoList,
            StructurePlaceSettings settings) {
        float chanceScale = density * ProcessorSeeds.decorationScale(settings);
        if (chanceScale <= 0.0F) {
            return processedBlockInfoList;
        }
        LongOpenHashSet air = new LongOpenHashSet();
        for (StructureTemplate.StructureBlockInfo info : processedBlockInfoList) {
            if (info.state().isAir()) {
                air.add(info.pos().asLong());
            }
        }
        long seed = ProcessorSeeds.seed(settings);
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>(processedBlockInfoList.size());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (StructureTemplate.StructureBlockInfo info : processedBlockInfoList) {
            result.add(decorate(info, air, seed, chanceScale, settings, cursor));
        }
        return result;
    }

    private StructureTemplate.StructureBlockInfo decorate(
            StructureTemplate.StructureBlockInfo info,
            LongOpenHashSet air,
            long seed,
            float chanceScale,
            StructurePlaceSettings settings,
            BlockPos.MutableBlockPos cursor) {
        BlockState state = info.state();
        if (state.isAir() || info.nbt() != null) {
            return info;
        }
        BlockPos pos = info.pos();
        int exposed = 0;
        for (Direction direction : Direction.values()) {
            cursor.setWithOffset(pos, direction);
            if (air.contains(cursor.asLong())) {
                exposed |= StateOrientation.faceBit(direction);
            }
        }
        if (exposed == 0 || !target.test(state, settings.getRandom(pos))) {
            return info;
        }
        float clump = LabyrinthNoise.cluster(seed, pos.getX(), pos.getY(), pos.getZ(), clusterPitch, salt);
        if (LabyrinthNoise.unit(seed, pos.getX(), pos.getY(), pos.getZ(), salt + 1)
                >= chanceScale * clump * CLUMP_GAIN) {
            return info;
        }
        Direction face = faceOf(exposed);
        SimpleWeightedRandomList<BlockState> options =
                face == Direction.UP ? floor : face == Direction.DOWN ? ceiling : wall;
        BlockState decor = ProcessorSeeds.pick(options, seed, pos.getX(), pos.getY(), pos.getZ(), salt + 2);
        if (decor == null) {
            return info;
        }
        BlockState oriented = StateOrientation.exposeFaces(StateOrientation.face(decor, face), exposed);
        return new StructureTemplate.StructureBlockInfo(pos, oriented, null);
    }

    private static Direction faceOf(int exposed) {
        for (Direction direction : FACE_PRIORITY) {
            if ((exposed & StateOrientation.faceBit(direction)) != 0) {
                return direction;
            }
        }
        return Direction.UP;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TTStructureProcessors.EXPOSED_SURFACE.get();
    }
}
