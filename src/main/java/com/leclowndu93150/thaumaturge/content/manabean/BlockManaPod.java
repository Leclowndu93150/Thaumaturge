package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesCompat;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

public final class BlockManaPod extends BaseEntityBlock {
    public static final MapCodec<BlockManaPod> CODEC = simpleCodec(BlockManaPod::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final IntegerProperty DYNAMIC_TREE_LIFT = IntegerProperty.create("dynamic_tree_lift", 0, 4);
    public static final List<Integer> DYNAMIC_TREE_LIFT_PIXELS = List.of(0, 4, 5, 6, 8);
    private static final int SUPPORT_LOSS_TICK_DELAY = 1;
    private static final int GROWTH_CHANCE_BOUND = 30;
    private static final int BREAK_SPEED_BASE = BlockEntityManaPod.MAX_AGE + 1;
    private static final VoxelShape SMALL_STEM = Shapes.or(box(5, 10, 7.5, 11, 16, 8.5), box(7.5, 10, 5, 8.5, 16, 11));
    private static final VoxelShape MATURE_STEM = Shapes.or(box(0, 10, 7.5, 16, 16, 8.5), box(7.5, 10, 0, 8.5, 16, 16));
    private static final VoxelShape SEED = box(7, 12, 7, 9, 14, 9);
    private static final VoxelShape BUD = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape POD = Shapes.or(box(4, 3.2, 4, 12, 11.2, 12), box(5, 2.2, 5, 11, 4.2, 11), box(5, 10.2, 5, 11, 12.2, 11));
    private static final double PIXELS_PER_BLOCK = 16.0;
    private static final VoxelShape MATURE = Shapes.or(MATURE_STEM, POD);
    private static final VoxelShape[][] BODIES = lifted(new VoxelShape[] {SEED, BUD, POD, POD, POD, POD, POD, POD});
    private static final VoxelShape[][] SHAPES = lifted(new VoxelShape[] {Shapes.or(SMALL_STEM, SEED), Shapes.or(SMALL_STEM, BUD), MATURE, MATURE, MATURE, MATURE, MATURE, MATURE});

    public BlockManaPod(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(DYNAMIC_TREE_LIFT, 0));
    }

    @Override
    protected MapCodec<BlockManaPod> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, DYNAMIC_TREE_LIFT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(DYNAMIC_TREE_LIFT)][state.getValue(AGE)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BODIES[state.getValue(DYNAMIC_TREE_LIFT)][state.getValue(AGE)];
    }

    private static VoxelShape[][] lifted(VoxelShape[] shapes) {
        VoxelShape[][] lifted = new VoxelShape[DYNAMIC_TREE_LIFT_PIXELS.size()][];
        for (int lift = 0; lift < lifted.length; lift++) {
            double offset = DYNAMIC_TREE_LIFT_PIXELS.get(lift) / PIXELS_PER_BLOCK;
            lifted[lift] = Arrays.stream(shapes).map(shape -> shape.move(0.0, offset, 0.0)).toArray(VoxelShape[]::new);
        }
        return lifted;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean canGrowAt(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(TTBiomeTags.IS_MAGICAL) && isSupport(level, pos.above());
    }

    public static BlockState placementState(LevelReader level, BlockPos pos) {
        BlockPos support = pos.above();
        int lift = ModList.get().isLoaded(TTIds.DYNAMIC_TREES) && DynamicTreesCompat.supportsManaPod(level, support) ? DynamicTreesCompat.manaPodLift(level, support) : 0;
        return TTBlocks.MANA_POD.get().defaultBlockState().setValue(DYNAMIC_TREE_LIFT, lift);
    }

    private static boolean isSupport(LevelReader level, BlockPos support) {
        BlockState state = level.getBlockState(support);
        return (state.is(BlockTags.LOGS) && state.isFaceSturdy(level, support, Direction.DOWN)) || (ModList.get().isLoaded(TTIds.DYNAMIC_TREES) && DynamicTreesCompat.supportsManaPod(level, support));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod && pod.isShowcase()) {
            return level.getBlockState(pos.above()).is(BlockTags.LOGS);
        }
        return canGrowAt(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP && !isSupport(level, neighbourPos)) {
            ticks.scheduleTick(pos, this, SUPPORT_LOSS_TICK_DELAY);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        dropIfUnsupported(state, level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (dropIfUnsupported(state, level, pos)) {
            return;
        }
        if (random.nextInt(GROWTH_CHANCE_BOUND) != 0) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod && !pod.isShowcase()) {
            pod.checkGrowth();
        }
    }

    private boolean dropIfUnsupported(BlockState state, ServerLevel level, BlockPos pos) {
        boolean unsupported = !canSurvive(state, level, pos);
        if (unsupported) {
            level.destroyBlock(pos, true);
        }
        return unsupported;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float multiplier = BREAK_SPEED_BASE - state.getValue(AGE);
        return multiplier * super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack bean = new ItemStack(TTItems.MANA_BEAN.get());
        if (!(level.getBlockEntity(pos) instanceof BlockEntityManaPod pod)) {
            return bean;
        }
        Holder<IAspect> aspect = pod.aspect();
        if (aspect != null) {
            bean.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
        }
        return bean;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityManaPod(pos, state);
    }
}
