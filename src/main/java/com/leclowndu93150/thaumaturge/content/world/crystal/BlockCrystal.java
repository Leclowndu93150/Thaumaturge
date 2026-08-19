package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
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

public final class BlockCrystal extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);

    private static final int VIS_THRESHOLD = 10;

    private static final VoxelShape SHAPE_FULL = Shapes.block();
    private static final VoxelShape SHAPE_UP = box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape SHAPE_EAST = box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WEST = box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);

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
        VoxelShape combined = Shapes.empty();
        int touching = 0;
        for (Direction direction : Direction.values()) {
            if (touchesStone(level, pos, direction)) {
                combined = Shapes.or(combined, faceShape(direction));
                touching++;
            }
        }
        if (touching == 0) {
            return SHAPE_FULL;
        }
        if (touching > 1) {
            return SHAPE_FULL;
        }
        return combined;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static VoxelShape faceShape(Direction direction) {
        return switch (direction) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case SOUTH -> SHAPE_SOUTH;
            case NORTH -> SHAPE_NORTH;
        };
    }

    private static boolean touchesStone(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        BlockState neighbourState = level.getBlockState(neighbour);
        return neighbourState.isFaceSturdy(level, neighbour, direction.getOpposite());
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
                        level.setBlockAndUpdate(spreadTo, defaultBlockState().setValue(GENERATION, childGeneration + 1));
                    }
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
                        level.setBlockAndUpdate(spreadTo, defaultBlockState().setValue(GENERATION, childGeneration + 1));
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
