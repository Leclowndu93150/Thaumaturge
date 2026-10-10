package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockCondenserLattice extends Block {
    public static final MapCodec<BlockCondenserLattice> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.BOOL.fieldOf("dirty").forGetter(BlockCondenserLattice::isDirty), propertiesCodec()).apply(instance, BlockCondenserLattice::new));

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MASK_COUNT = 1 << DIRECTIONS.length;
    private static final double FACE_SHIFT_DIVISOR = 3.0;
    private static final float CLEAN_SOUND_VOLUME = 0.5F;
    private static final float CLEAN_SOUND_PITCH = 1.0F;
    private static final int CRYSTAL_AMOUNT = 1;
    private static final VoxelShape CORE = Shapes.or(box(5.0, 6.0, 6.0, 11.0, 10.0, 10.0), box(6.0, 5.0, 6.0, 10.0, 6.0, 10.0), box(6.0, 6.0, 5.0, 10.0, 10.0, 6.0),
            box(6.0, 6.0, 10.0, 10.0, 10.0, 11.0), box(6.0, 10.0, 6.0, 10.0, 11.0, 10.0));
    private static final Map<Direction, VoxelShape> ARMS = buildArms();

    private final boolean dirty;
    private final VoxelShape[] shapes = new VoxelShape[MASK_COUNT];

    public BlockCondenserLattice(boolean dirty, BlockBehaviour.Properties properties) {
        super(properties);
        this.dirty = dirty;
        BlockState state = getStateDefinition().any();
        for (Direction direction : DIRECTIONS) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false);
        }
        registerDefaultState(state);
        for (int mask = 0; mask < MASK_COUNT; mask++) {
            shapes[mask] = buildShape(mask);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    private static Map<Direction, VoxelShape> buildArms() {
        Map<Direction, VoxelShape> arms = new EnumMap<>(Direction.class);
        arms.put(Direction.DOWN, Shapes.or(box(6.0, 0.0, 6.0, 10.0, 1.0, 10.0), box(7.0, 1.0, 7.0, 9.0, 5.0, 9.0)));
        arms.put(Direction.UP, Shapes.or(box(6.0, 15.0, 6.0, 10.0, 16.0, 10.0), box(7.0, 11.0, 7.0, 9.0, 15.0, 9.0)));
        arms.put(Direction.NORTH, Shapes.or(box(6.0, 6.0, 0.0, 10.0, 10.0, 1.0), box(7.0, 7.0, 1.0, 9.0, 9.0, 5.0)));
        arms.put(Direction.SOUTH, Shapes.or(box(6.0, 6.0, 15.0, 10.0, 10.0, 16.0), box(7.0, 7.0, 11.0, 9.0, 9.0, 15.0)));
        arms.put(Direction.WEST, Shapes.or(box(0.0, 6.0, 6.0, 1.0, 10.0, 10.0), box(1.0, 7.0, 7.0, 5.0, 9.0, 9.0)));
        arms.put(Direction.EAST, Shapes.or(box(15.0, 6.0, 6.0, 16.0, 10.0, 10.0), box(11.0, 7.0, 7.0, 15.0, 9.0, 9.0)));
        return arms;
    }

    private static VoxelShape buildShape(int mask) {
        VoxelShape shape = CORE;
        for (Direction direction : DIRECTIONS) {
            if ((mask & (1 << direction.ordinal())) != 0) {
                shape = Shapes.or(shape, ARMS.get(direction));
            }
        }
        return shape;
    }

    private static int maskOf(BlockState state) {
        int mask = 0;
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    private static boolean connectsTo(Direction direction, BlockState neighbour) {
        return neighbour.getBlock() instanceof BlockCondenserLattice || direction == Direction.DOWN && neighbour.is(TTBlocks.CONDENSER.get());
    }

    private static BlockState linkNeighbours(BlockState base, BlockPos pos, LevelReader level) {
        BlockState linked = base;
        for (Map.Entry<Direction, BooleanProperty> arm : PipeBlock.PROPERTY_BY_DIRECTION.entrySet()) {
            Direction side = arm.getKey();
            linked = linked.setValue(arm.getValue(), connectsTo(side, level.getBlockState(pos.relative(side))));
        }
        return linked;
    }

    @Override
    protected MapCodec<BlockCondenserLattice> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.NORTH, PipeBlock.EAST, PipeBlock.SOUTH, PipeBlock.WEST, PipeBlock.UP, PipeBlock.DOWN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return linkNeighbours(defaultBlockState(), context.getClickedPos(), context.getLevel());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        LatticeAnchor.schedule(ticks, pos, this);
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(directionToNeighbour), connectsTo(directionToNeighbour, neighbourState));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) {
            LatticeAnchor.schedule(level, pos, this);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        LatticeAnchor.settle(level, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        LatticeAnchor.released(level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[maskOf(state)];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!dirty || !stack.is(TTItems.FILTER.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (level instanceof ServerLevel serverLevel) {
            consumeFilter(player, stack);
            scrub(serverLevel, pos, hit.getDirection());
        }
        return InteractionResult.SUCCESS;
    }

    private static void consumeFilter(Player player, ItemStack filter) {
        if (!player.hasInfiniteMaterials()) {
            filter.shrink(1);
        }
    }

    private static void scrub(ServerLevel level, BlockPos pos, Direction face) {
        spawnCrystalByChance(level, pos, face);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, CLEAN_SOUND_VOLUME, CLEAN_SOUND_PITCH);
        BlockState cleaned = TTBlocks.CONDENSER_LATTICE.get().defaultBlockState();
        level.setBlock(pos, linkNeighbours(cleaned, pos, level), Block.UPDATE_ALL);
    }

    private static void spawnCrystalByChance(ServerLevel level, BlockPos pos, Direction face) {
        if (!level.getRandom().nextBoolean()) {
            return;
        }
        Holder<IAspect> vitium = Aspects.resolve(level, TTAspects.VITIUM);
        if (vitium == null) {
            return;
        }
        ItemStack crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        crystal.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(vitium, CRYSTAL_AMOUNT));
        Vec3 drop = Vec3.atCenterOf(pos).add(face.getStepX() / FACE_SHIFT_DIVISOR, 0.0, face.getStepZ() / FACE_SHIFT_DIVISOR);
        level.addFreshEntity(new ItemEntity(level, drop.x, drop.y, drop.z, crystal));
    }
}
