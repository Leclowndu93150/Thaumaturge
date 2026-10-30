package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesPlacement;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

public final class BlockManaPod extends BaseEntityBlock {
    public static final MapCodec<BlockManaPod> CODEC = simpleCodec(BlockManaPod::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final IntegerProperty DYNAMIC_TREE_LIFT = IntegerProperty.create("dynamic_tree_lift", 0, 4);

    private static final int GROWTH_CHANCE = 30;
    private static final int MAX_HARDNESS_STEPS = 8;
    public static final List<Integer> DYNAMIC_TREE_LIFT_PIXELS = List.of(0, 4, 5, 6, 8);
    private static final VoxelShape SMALL_CROSS = Shapes.or(box(5, 10, 7.5, 11, 16, 8.5), box(7.5, 10, 5, 8.5, 16, 11));
    private static final VoxelShape MATURE_CROSS =
            Shapes.or(box(0, 10, 7.5, 16, 16, 8.5), box(7.5, 10, 0, 8.5, 16, 16));
    private static final VoxelShape MATURE_POD = Shapes.or(
            Shapes.or(MATURE_CROSS, box(4, 3.2, 4, 12, 11.2, 12)),
            Shapes.or(box(5, 2.2, 5, 11, 4.2, 11), box(5, 10.2, 5, 11, 12.2, 11)));
    private static final VoxelShape[] SHAPES = {
        Shapes.or(SMALL_CROSS, box(7, 12, 7, 9, 14, 9)),
        Shapes.or(SMALL_CROSS, box(6, 6, 6, 10, 10, 10)),
        MATURE_POD,
        MATURE_POD,
        MATURE_POD,
        MATURE_POD,
        MATURE_POD,
        MATURE_POD
    };

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
        return SHAPES[state.getValue(AGE)].move(
                0, DYNAMIC_TREE_LIFT_PIXELS.get(state.getValue(DYNAMIC_TREE_LIFT)) / 16.0, 0);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean canGrowAt(LevelReader level, BlockPos pos) {
        if (!level.getBiome(pos).is(TTBiomeTags.IS_MAGICAL)) {
            return false;
        }
        BlockPos support = pos.above();
        BlockState supportState = level.getBlockState(support);
        if (supportState.is(BlockTags.LOGS)) {
            return true;
        }
        if (ModList.get().isLoaded(TTIds.DYNAMIC_TREES)) {
            return DynamicTreesPlacement.supportsManaPod(level, support);
        }
        return false;
    }

    public static BlockState placementState(LevelReader level, BlockPos pos) {
        BlockPos support = pos.above();
        int dynamicTreeLift =
                ModList.get().isLoaded(TTIds.DYNAMIC_TREES) && DynamicTreesPlacement.supportsManaPod(level, support)
                        ? DynamicTreesPlacement.manaPodLift(level, support)
                        : 0;
        return TTBlocks.MANA_POD.get().defaultBlockState().setValue(DYNAMIC_TREE_LIFT, dynamicTreeLift);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod && pod.isShowcase()) {
            return level.getBlockState(pos.above()).is(BlockTags.LOGS);
        }
        return canGrowAt(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction == Direction.UP && !neighborState.is(BlockTags.LOGS)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod
                && !pod.isShowcase()
                && random.nextInt(GROWTH_CHANCE) == 0) {
            pod.checkGrowth();
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return super.getDestroyProgress(state, player, level, pos) * (MAX_HARDNESS_STEPS - state.getValue(AGE));
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(TTItems.MANA_BEAN.get());
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod) {
            Holder<IAspect> aspect = pod.aspect();
            if (aspect != null) {
                stack.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
            }
        }
        return stack;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityManaPod(pos, state);
    }
}
