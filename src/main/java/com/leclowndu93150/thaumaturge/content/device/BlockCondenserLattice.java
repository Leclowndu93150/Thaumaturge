package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockCondenserLattice extends Block {
    public static final MapCodec<BlockCondenserLattice> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.BOOL.fieldOf("dirty").forGetter(block -> block.dirty), propertiesCodec())
                    .apply(instance, BlockCondenserLattice::new));

    private static final VoxelShape CORE = Shapes.or(
            box(5.0, 6.0, 6.0, 11.0, 10.0, 10.0),
            box(6.0, 5.0, 6.0, 10.0, 6.0, 10.0),
            box(6.0, 6.0, 5.0, 10.0, 10.0, 6.0),
            box(6.0, 6.0, 10.0, 10.0, 10.0, 11.0),
            box(6.0, 10.0, 6.0, 10.0, 11.0, 10.0));
    private static final Map<Direction, VoxelShape> ARMS = DeviceShapes.facingShapesFromDown(
            Shapes.or(box(6.0, 0.0, 6.0, 10.0, 1.0, 10.0), box(7.0, 1.0, 7.0, 9.0, 5.0, 9.0)));

    private final boolean dirty;
    private final VoxelShape[] shapeCache = new VoxelShape[64];

    public BlockCondenserLattice(boolean dirty, BlockBehaviour.Properties properties) {
        super(properties);
        this.dirty = dirty;
        BlockState base = getStateDefinition().any();
        for (Direction direction : Direction.values()) {
            base = base.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false);
        }
        registerDefaultState(base);
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    protected MapCodec<BlockCondenserLattice> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction directionToNeighbour,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        boolean connects = connectsTo(neighbourState, directionToNeighbour);
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(directionToNeighbour), connects);
    }

    private BlockState connected(LevelReader level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), connectsTo(neighbour, direction));
        }
        return state;
    }

    private static boolean connectsTo(BlockState neighbour, Direction direction) {
        return neighbour.getBlock() instanceof BlockCondenserLattice
                || direction == Direction.DOWN && neighbour.is(TTBlocks.CONDENSER.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int key = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                key |= 1 << direction.ordinal();
            }
        }
        VoxelShape cached = shapeCache[key];
        if (cached == null) {
            cached = CORE;
            for (Direction direction : Direction.values()) {
                if ((key & 1 << direction.ordinal()) != 0) {
                    cached = Shapes.or(cached, ARMS.get(direction));
                }
            }
            shapeCache[key] = cached;
        }
        return cached;
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
        if (!dirty || !stack.is(TTItems.FILTER.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        stack.consume(1, player);
        if (level.getRandom().nextBoolean()) {
            ItemStack crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
            crystal.set(
                    TTDataComponents.CRYSTAL_ASPECT.get(),
                    new AspectInstance(
                            level.registryAccess()
                                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                                    .getOrThrow(TTAspects.VITIUM),
                            1));
            Direction face = hit.getDirection();
            level.addFreshEntity(new ItemEntity(
                    level,
                    pos.getX() + 0.5 + face.getStepX() / 3.0,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + face.getStepZ() / 3.0,
                    crystal));
        }
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        level.setBlock(
                pos, connected(level, pos, TTBlocks.CONDENSER_LATTICE.get().defaultBlockState()), Block.UPDATE_ALL);
        return ItemInteractionResult.SUCCESS;
    }
}
