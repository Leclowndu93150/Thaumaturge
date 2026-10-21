package com.leclowndu93150.thaumaturge.content.infusion;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

public record MatrixEnvironment(List<BlockPos> pedestals, int cycleTime, float costMult, float stabilityReplenish, List<BlockPos> faultyStabilisers) {
    private static final int BASE_CYCLE_TIME = 10;
    private static final float BASE_COST_MULTIPLIER = 1.0F;
    private static final int ALTAR_DEPTH = 2;
    private static final int UPGRADE_DEPTH = 3;
    private static final List<BlockPos> CORNERS = List.of(new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(1, 0, 1));
    private static final List<BlockPos> UPGRADE_OFFSETS = CORNERS.stream().map(corner -> corner.below(UPGRADE_DEPTH)).toList();
    private static final List<BlockPos> SCAN_OFFSETS = scanOffsets();

    public MatrixEnvironment {
        pedestals = List.copyOf(pedestals);
        faultyStabilisers = List.copyOf(faultyStabilisers);
    }

    public static MatrixEnvironment survey(Level level, BlockPos matrix) {
        List<BlockPos> spots = scanPedestals(level, matrix);
        Stream<InfusionModifier> fromPedestals = spots.stream().map(spot -> modifier(level.getBlockState(spot), InfusionDataMaps.PEDESTAL));
        Stream<InfusionModifier> fromPillars = Stream.of(pillarModifier(level, altarOf(matrix)));
        Stream<InfusionModifier> fromUpgrades = UPGRADE_OFFSETS.stream().map(matrix::offset).filter(level::hasChunkAt)
                .map(position -> modifier(level.getBlockState(position), InfusionDataMaps.MATRIX_UPGRADE));
        List<InfusionModifier> modifiers = Stream.of(fromPedestals, fromPillars, fromUpgrades).flatMap(stream -> stream).filter(Objects::nonNull).toList();
        int cycle = BASE_CYCLE_TIME;
        float cost = BASE_COST_MULTIPLIER;
        InfusionStabilitySurvey.Result stabilisers = InfusionStabilitySurvey.survey(level, matrix);
        float stability = stabilisers.stabilityReplenish();
        for (InfusionModifier entry : modifiers) {
            cycle += entry.cycleTime();
            cost += entry.cost();
            stability += entry.stability();
        }
        return new MatrixEnvironment(spots, cycle, cost, stability, stabilisers.problemBlocks());
    }

    public static boolean validLocation(Level level, BlockPos matrix) {
        BlockPos altar = altarOf(matrix);
        if (!level.hasChunkAt(matrix) || !level.hasChunkAt(altar) || CORNERS.stream().anyMatch(corner -> !level.hasChunkAt(altar.offset(corner)))) {
            return false;
        }
        boolean pedestalBelow = level.getBlockState(altar).getBlock() instanceof BlockPedestal;
        return pedestalBelow && cornerBlocks(level, altar).stream().allMatch(BlockPillar.class::isInstance);
    }

    private static BlockPos altarOf(BlockPos matrix) {
        return matrix.offset(0, -ALTAR_DEPTH, 0);
    }

    private static List<BlockPos> scanOffsets() {
        int half = InfusionStabilitySurvey.HALF_WIDTH;
        return IntStream.rangeClosed(-half, half).boxed()
                .flatMap(dx -> IntStream.rangeClosed(-half, half).filter(dz -> dx != 0 || dz != 0).boxed().flatMap(
                        dz -> IntStream.iterate(InfusionStabilitySurvey.HEIGHT_ABOVE, dy -> dy >= -InfusionStabilitySurvey.DEPTH_BELOW, dy -> dy - 1).mapToObj(dy -> new BlockPos(dx, dy, dz))))
                .toList();
    }

    private static List<Block> cornerBlocks(Level level, BlockPos base) {
        return CORNERS.stream().map(corner -> level.getBlockState(base.offset(corner)).getBlock()).toList();
    }

    private static List<BlockPos> scanPedestals(Level level, BlockPos matrix) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        return SCAN_OFFSETS.stream().filter(offset -> isLoadedPedestal(level, probe.setWithOffset(matrix, offset))).map(matrix::offset).toList();
    }

    private static boolean isLoadedPedestal(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof BlockPedestal;
    }

    private static @Nullable InfusionModifier pillarModifier(Level level, BlockPos base) {
        Block pillar = uniformPillar(level, base);
        return pillar == null ? null : pillar.defaultBlockState().typeHolder().getData(InfusionDataMaps.PILLAR_SET);
    }

    private static @Nullable Block uniformPillar(Level level, BlockPos base) {
        List<Block> blocks = cornerBlocks(level, base);
        Block head = blocks.getFirst();
        return blocks.stream().allMatch(block -> block == head) ? head : null;
    }

    private static @Nullable InfusionModifier modifier(BlockState state, DataMapType<Block, InfusionModifier> type) {
        return state.typeHolder().getData(type);
    }
}
