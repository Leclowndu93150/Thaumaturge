package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockAdvancedAlchemicalFurnace extends BaseEntityBlock {
    public static final MapCodec<BlockAdvancedAlchemicalFurnace> CODEC =
            simpleCodec(BlockAdvancedAlchemicalFurnace::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final ResourceLocation RESEARCH = TTIds.rl("advanced_alchemical_furnace");
    private static final VoxelShape INPUT_COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 11.2, 16.0);
    private static final int ITEM_INTAKE_INTERVAL = 10;

    public BlockAdvancedAlchemicalFurnace(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<BlockAdvancedAlchemicalFurnace> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return INPUT_COLLISION;
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
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!DeviceGate.passes(player, RESEARCH)) {
            return ItemInteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnace furnace)
                || !furnace.insertInput(stack)) {
            return ItemInteractionResult.FAIL;
        }
        stack.consume(1, player);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide()
                && entity instanceof ItemEntity item
                && item.tickCount % ITEM_INTAKE_INTERVAL == 0
                && level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnace furnace
                && furnace.insertInput(item.getItem())) {
            ItemStack remaining = item.getItem().copy();
            remaining.shrink(1);
            if (remaining.isEmpty()) {
                item.discard();
            } else {
                item.setItem(remaining);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
                furnace.dropContents(pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (!level.isClientSide()) {
                AdvancedAlchemicalFurnaceStructure.restore(level, pos);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAdvancedAlchemicalFurnace(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        TTBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(),
                        BlockEntityAdvancedAlchemicalFurnace::serverTick);
    }
}
