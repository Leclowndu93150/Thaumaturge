package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockManaPod extends BaseEntityBlock {
    public static final MapCodec<BlockManaPod> CODEC = simpleCodec(BlockManaPod::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    private static final int GROWTH_CHANCE = 30;
    private static final int MAX_HARDNESS_STEPS = 8;
    private static final VoxelShape[] SHAPES = {box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0), box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0), box(4.0, 8.0, 4.0, 12.0, 16.0, 12.0),
            box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0), box(4.0, 5.0, 4.0, 12.0, 16.0, 12.0), box(4.0, 4.0, 4.0, 12.0, 16.0, 12.0), box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0),
            box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0)};

    public BlockManaPod(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<BlockManaPod> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean canGrowAt(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(TCBiomeTags.IS_MAGICAL) && level.getBlockState(pos.above()).is(BlockTags.LOGS);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canGrowAt(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !neighborState.is(BlockTags.LOGS)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
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
        if (random.nextInt(GROWTH_CHANCE) == 0 && level.getBlockEntity(pos) instanceof BlockEntityManaPod pod) {
            pod.checkGrowth();
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return super.getDestroyProgress(state, player, level, pos) * (MAX_HARDNESS_STEPS - state.getValue(AGE));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(TCItems.MANA_BEAN.get());
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod) {
            Holder<IAspect> aspect = pod.aspect();
            if (aspect != null) {
                stack.set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
            }
        }
        return stack;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityManaPod(pos, state);
    }
}
