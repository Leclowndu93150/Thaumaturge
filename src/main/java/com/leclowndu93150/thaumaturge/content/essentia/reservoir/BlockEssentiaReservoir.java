package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** TC4 essentia reservoir: a 256-unit mixed-aspect store with one configurable tube face. */
public final class BlockEssentiaReservoir extends BaseEntityBlock implements IInteractWithCaster {
    public static final MapCodec<BlockEssentiaReservoir> CODEC = simpleCodec(BlockEssentiaReservoir::new);
    private static final float RUPTURE_AURA_FLUX_PER_ESSENTIA = 0.25F;
    private static final float MAX_RUPTURE_AURA_FLUX = 64.0F;
    private static final Map<Direction, VoxelShape> SHAPES =
            DeviceShapes.facingShapesFromDown(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));

    public BlockEssentiaReservoir(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<BlockEssentiaReservoir> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEssentiaReservoir(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                TCBlockEntities.ESSENTIA_RESERVOIR.get(),
                level.isClientSide()
                        ? BlockEntityEssentiaReservoir::clientTick
                        : BlockEntityEssentiaReservoir::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityEssentiaReservoir reservoir)) return 0;
        int amount = reservoir.getStoredAmount();
        return amount <= 0
                ? 0
                : 1 + (int) Math.floor((amount / (double) BlockEntityEssentiaReservoir.CAPACITY) * 14.0D);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof BlockEntityEssentiaReservoir reservoir) {
            releaseStoredEssentia(serverLevel, pos, reservoir.getStoredAmount());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void releaseStoredEssentia(ServerLevel level, BlockPos pos, int stored) {
        int releases = stored / 16;
        if (stored <= 0) return;

        // Preserve the TC4 physical rupture and add a bounded TC6 Aura consequence proportional
        // to the stored essentia. This is a catastrophic containment failure, not routine leakage.
        AuraHelper.polluteAura(
                level, pos, Math.min(stored * RUPTURE_AURA_FLUX_PER_ESSENTIA, MAX_RUPTURE_AURA_FLUX), true);
        if (releases <= 0) return;

        // TC4 physically ruptured a loaded reservoir: full-strength Flux Goo formed below the
        // tank and full-strength Flux Gas formed at/above it. Keep the original 50-attempt search
        // and post-increment release limit, but route placement through the shared modern helper.
        level.explode(
                null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F, Level.ExplosionInteraction.NONE);

        int placed = 0;
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextInt(5) - level.getRandom().nextInt(5),
                    level.getRandom().nextInt(5) - level.getRandom().nextInt(5),
                    level.getRandom().nextInt(5) - level.getRandom().nextInt(5));
            if (!level.isEmptyBlock(target)) continue;

            if (target.getY() < pos.getY()) {
                PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
            } else {
                PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
            }
            // Preserve TC4's post-increment check: a value N can release up to N+1 pockets.
            if (placed++ >= releases) break;
        }
    }

    @Override
    public boolean onCasterRightClick(
            Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level.isClientSide()) return true;
        BlockState state = level.getBlockState(pos);
        Direction facing = player.isShiftKeyDown() ? side.getOpposite() : side;
        if (state.getValue(BlockStateProperties.FACING) != facing) {
            level.setBlock(pos, state.setValue(BlockStateProperties.FACING, facing), Block.UPDATE_ALL);
            BlockEssentiaTransport.refreshConnectionsAround(level, pos);
        }
        player.swing(hand);
        return true;
    }
}
