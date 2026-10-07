package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class BlockCrystal extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);

    private static final int VIS_THRESHOLD = 10;

    private static final double[][][] SHARD_BOXES = {
        {{6.0, 0.0, 5.5, 10.0, 7.5, 9.0}, {7.0, 0.0, 9.0, 9.5, 6.0, 10.0}, {7.0, 6.5, 7.0, 9.0, 8.0, 8.5}},
        {{12.0, 0.0, 5.5, 15.0, 4.5, 7.5}, {13.0, 0.0, 7.5, 14.0, 3.5, 8.0}, {13.0, 2.0, 5.0, 13.5, 3.0, 5.5}},
        {{2.0, 0.0, 10.5, 4.5, 3.5, 12.0}, {2.0, 0.0, 12.0, 4.5, 3.0, 12.5}, {2.5, 3.0, 11.0, 4.0, 4.5, 12.5}},
        {{9.0, 0.0, 1.5, 12.0, 6.5, 3.5}, {10.0, 0.0, 3.5, 11.5, 5.0, 4.5}, {10.0, 2.5, 1.0, 10.5, 5.0, 1.5}},
        {{4.5, 0.0, 2.0, 7.5, 4.5, 3.5}, {5.0, 0.0, 1.0, 7.0, 4.0, 2.0}, {5.0, 0.0, 3.0, 7.0, 4.0, 4.0}},
        {{11.5, 0.0, 9.5, 14.0, 5.0, 12.5}, {12.5, 5.0, 10.5, 13.5, 6.0, 11.5}, {14.0, 2.5, 10.0, 14.5, 4.0, 10.5}},
        {{6.5, 0.0, 12.0, 9.5, 4.0, 14.5}, {7.0, 3.5, 12.5, 9.0, 5.5, 14.5}, {7.5, 1.5, 14.5, 9.0, 4.5, 15.0}},
        {{1.0, 0.0, 5.0, 4.0, 6.0, 7.0}, {1.5, 1.5, 4.5, 3.0, 4.0, 5.0}, {2.0, 0.5, 7.0, 3.5, 4.0, 7.5}}
    };

    private static final double SHAPE_SNAP = 32.0;

    private static final Map<Direction, List<VoxelShape>> SHARD_SHAPES = shardShapes();

    private final ResourceKey<IAspect> aspect;
    private final boolean flux;

    public BlockCrystal(BlockBehaviour.Properties properties, ResourceKey<IAspect> aspect, boolean flux) {
        super(properties);
        this.aspect = aspect;
        this.flux = flux;
        registerDefaultState(stateDefinition.any().setValue(SIZE, 0).setValue(GENERATION, 1));
    }

    private static final MapCodec<BlockCrystal> CODEC = simpleCodec(p -> new BlockCrystal(p, null, false));

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public boolean isFlux() {
        return flux;
    }

    public int growth(BlockState state) {
        return state.getValue(SIZE);
    }

    public int generation(BlockState state) {
        return state.getValue(GENERATION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        long seed = CrystalShards.seed(state, pos);
        int count = growth(state) + 1;
        VoxelShape shape = Shapes.empty();
        boolean supported = false;
        for (Direction face : Direction.values()) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            supported = true;
            List<Integer> order = CrystalShards.order(face, seed);
            List<VoxelShape> shards = SHARD_SHAPES.get(face);
            for (int i = 0; i < count; i++) {
                shape = Shapes.or(shape, shards.get(order.get(i)));
            }
        }
        return supported ? shape : SHARD_SHAPES.get(Direction.DOWN).get(CrystalShards.unsupported(seed));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static Map<Direction, List<VoxelShape>> shardShapes() {
        Map<Direction, List<VoxelShape>> shapes = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            List<VoxelShape> shards = new ArrayList<>();
            for (double[][] boxes : SHARD_BOXES) {
                VoxelShape shard = Shapes.empty();
                for (double[] box : boxes) {
                    shard = Shapes.or(shard, transformed(transform, box));
                }
                shards.add(shard.optimize());
            }
            shapes.put(face, List.copyOf(shards));
        }
        return shapes;
    }

    private static VoxelShape transformed(Matrix4f transform, double[] box) {
        Vector3f from = transform.transformPosition(
                new Vector3f((float) (box[0] / 16.0), (float) (box[1] / 16.0), (float) (box[2] / 16.0)));
        Vector3f to = transform.transformPosition(
                new Vector3f((float) (box[3] / 16.0), (float) (box[4] / 16.0), (float) (box[5] / 16.0)));
        return Shapes.box(
                snap(Math.min(from.x, to.x)),
                snap(Math.min(from.y, to.y)),
                snap(Math.min(from.z, to.z)),
                snap(Math.max(from.x, to.x)),
                snap(Math.max(from.y, to.y)),
                snap(Math.max(from.z, to.z)));
    }

    private static double snap(float value) {
        return Math.round(value * SHAPE_SNAP) / SHAPE_SNAP;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSturdyNeighbour(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3 + generation(state)) != 0) {
            return;
        }
        int growth = growth(state);
        int generation = generation(state);
        if (!flux) {
            float vis = AuraHelper.getVis(level, pos);
            if (vis <= VIS_THRESHOLD) {
                if (growth > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(SIZE, growth - 1));
                    AuraHelper.addVis(level, pos, VIS_THRESHOLD);
                } else if (touchingSameCrystal(level, pos)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    AuraHelper.addVis(level, pos, VIS_THRESHOLD);
                    AuraHelper.addFlux(level, pos, 1.0F);
                }
            } else if (vis > AuraHelper.getAuraBase(level, pos) + VIS_THRESHOLD) {
                if (growth < 3 && growth < 5 - generation + Math.floorMod(pos.asLong(), 3L)) {
                    if (AuraHelper.drainVis(level, pos, VIS_THRESHOLD, false) > 0.0F) {
                        level.setBlockAndUpdate(pos, state.setValue(SIZE, growth + 1));
                    }
                } else if (generation < 4) {
                    BlockPos spreadTo = spreadCrystal(level, pos, random);
                    if (spreadTo != null && AuraHelper.drainVis(level, pos, VIS_THRESHOLD, false) > 0.0F) {
                        int childGeneration = generation;
                        if (random.nextInt(6) == 0) {
                            childGeneration--;
                        }
                        level.setBlockAndUpdate(
                                spreadTo, defaultBlockState().setValue(GENERATION, childGeneration + 1));
                    }
                }
            } else {
                float ambientFlux = AuraHelper.getFlux(level, pos);
                float base = AuraHelper.getAuraBase(level, pos);
                int conversionCost = growth + 1;
                if (ambientFlux > vis
                        && ambientFlux > base / 2.0F
                        && AuraHelper.drainFlux(level, pos, conversionCost, false) >= conversionCost - 0.001F) {
                    level.setBlockAndUpdate(
                            pos,
                            TTBlocks.CRYSTAL_VITIUM
                                    .get()
                                    .defaultBlockState()
                                    .setValue(SIZE, growth)
                                    .setValue(GENERATION, generation));
                }
            }
        } else {
            float flux = AuraHelper.getFlux(level, pos);
            if (flux <= VIS_THRESHOLD) {
                if (growth > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(SIZE, growth - 1));
                    AuraHelper.addFlux(level, pos, VIS_THRESHOLD);
                } else if (touchingSameCrystal(level, pos)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    AuraHelper.addFlux(level, pos, VIS_THRESHOLD);
                }
            } else if (flux > AuraHelper.getAuraBase(level, pos) + VIS_THRESHOLD) {
                if (growth < 3 && growth < 5 - generation + Math.floorMod(pos.asLong(), 3L)) {
                    if (AuraHelper.drainFlux(level, pos, VIS_THRESHOLD, false) > 0.0F) {
                        level.setBlockAndUpdate(pos, state.setValue(SIZE, growth + 1));
                    }
                } else if (generation < 4) {
                    BlockPos spreadTo = spreadCrystal(level, pos, random);
                    if (spreadTo != null && AuraHelper.drainFlux(level, pos, VIS_THRESHOLD, false) > 0.0F) {
                        int childGeneration = generation;
                        if (random.nextInt(6) == 0) {
                            childGeneration--;
                        }
                        level.setBlockAndUpdate(
                                spreadTo, defaultBlockState().setValue(GENERATION, childGeneration + 1));
                    }
                }
            }
        }
    }

    private boolean touchingSameCrystal(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(this)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (neighbourState.isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private BlockPos spreadCrystal(ServerLevel level, BlockPos pos, RandomSource random) {
        int xx = pos.getX() + random.nextInt(3) - 1;
        int yy = pos.getY() + random.nextInt(3) - 1;
        int zz = pos.getZ() + random.nextInt(3) - 1;
        BlockPos target = new BlockPos(xx, yy, zz);
        if (target.equals(pos)) {
            return null;
        }
        BlockState targetState = level.getBlockState(target);
        if (targetState.liquid()) {
            return null;
        }
        if (!targetState.isAir() && !targetState.canBeReplaced()) {
            return null;
        }
        if (random.nextInt(16) != 0) {
            return null;
        }
        return hasSturdyNeighbour(level, target) ? target : null;
    }
}
