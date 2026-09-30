package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.api.research.IResearchTableAid;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockCandleHolder extends Block implements IInfusionStabiliser, IResearchTableAid {
    public static final EnumProperty<HeldCandle> CANDLE = EnumProperty.create("candle", HeldCandle.class);

    private static final int LIT_LIGHT = 14;
    private static final double FLAME_Y_OFFSET = 0.69;
    private static final float PLACE_VOLUME = 1.0F;
    private static final float TAKE_VOLUME = 0.2F;
    private static final VoxelShape DISH = Block.box(4.0, 0.0, 4.0, 12.0, 2.5, 12.0);
    private static final VoxelShape HANDLE = Block.box(12.0, 0.5, 7.25, 14.0, 3.5, 8.75);
    private static final VoxelShape HELD_WAX = Block.box(6.0, 2.5, 6.0, 10.0, 10.75, 10.5);
    private static final VoxelShape EMPTY_SHAPE = Shapes.or(DISH, HANDLE);
    private static final VoxelShape FILLED_SHAPE = Shapes.or(DISH, HANDLE, HELD_WAX);

    private final CandleHolderMaterial material;

    public BlockCandleHolder(CandleHolderMaterial material, Properties properties) {
        super(properties);
        this.material = material;
        registerDefaultState(stateDefinition.any().setValue(CANDLE, HeldCandle.NONE));
    }

    public CandleHolderMaterial material() {
        return material;
    }

    public static int lightEmission(BlockState state) {
        return state.getValue(CANDLE).isPresent() ? LIT_LIGHT : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CANDLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CANDLE).isPresent() ? FILLED_SHAPE : EMPTY_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EMPTY_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        Optional<HeldCandle> candle = HeldCandle.of(stack);
        if (state.getValue(CANDLE).isPresent() || candle.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        level.setBlock(pos, state.setValue(CANDLE, candle.get()), Block.UPDATE_ALL);
        stack.consume(1, player);
        level.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, PLACE_VOLUME, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        HeldCandle held = state.getValue(CANDLE);
        if (!held.isPresent()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack candle = held.toStack();
        if (!player.getInventory().add(candle)) {
            player.drop(candle, false);
        }
        level.setBlock(pos, state.setValue(CANDLE, HeldCandle.NONE), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, TAKE_VOLUME, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(CANDLE).isPresent()) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + FLAME_Y_OFFSET;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public boolean canStabiliseInfusion(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(CANDLE).isPresent();
    }

    @Override
    public float aspectSaveChance(Level level, BlockPos pos, BlockState state) {
        return state.getValue(CANDLE).isPresent() ? material.researchSaveChance() : 0.0F;
    }

    @Override
    public float getStabilizationAmount(Level level, BlockPos pos) {
        return material.stabilization();
    }

    @Override
    public Block stabiliserIdentity(Level level, BlockPos pos) {
        return level.getBlockState(pos)
                .getValue(CANDLE)
                .candle()
                .<Block>map(candle -> candle)
                .orElse(this);
    }
}
