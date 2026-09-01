package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockNode extends Block implements EntityBlock {
    public static final MapCodec<BlockNode> CODEC = simpleCodec(BlockNode::new);

    private static final VoxelShape SHAPE = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);
    private static final float PRIMORDIAL_PEARL_FLUX_POLLUTION = 25.0F;

    public BlockNode(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockNode> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
        if (!stack.is(TCItems.PRIMORDIAL_PEARL.get())
                || stack.getDamageValue() > 2
                || !(level.getBlockEntity(pos) instanceof BlockEntityNode node)
                || node.isEnergized()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return ItemInteractionResult.SUCCESS;
        }

        boolean researched = KnowledgeAccess.of(player).isResearchComplete(TCIds.rl("primordial_nodes"));
        node.applyPrimordialPearl(serverLevel.random, researched);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        float strength = 3.0F + serverLevel.random.nextFloat() * (researched ? 3.0F : 5.0F);
        AuraHelper.polluteAura(serverLevel, pos, PRIMORDIAL_PEARL_FLUX_POLLUTION, true);
        serverLevel.explode(
                null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, strength, Level.ExplosionInteraction.BLOCK);
        for (int i = 0; i < 33; i++) {
            BlockPos target = pos.offset(
                    serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6),
                    serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6),
                    serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6));
            if (!serverLevel.isLoaded(target)
                    || !serverLevel.getBlockState(target).canBeReplaced()) {
                continue;
            }
            if (target.getY() < pos.getY()) {
                PhysicalFlux.placeGoo(serverLevel, target, PhysicalFlux.MAX_QUANTA);
            } else {
                PhysicalFlux.placeGas(serverLevel, target, PhysicalFlux.MAX_QUANTA);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNode(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TCBlockEntities.NODE.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, node) -> ((BlockEntityNode) node).clientTick(tickLevel, pos);
        }
        return (tickLevel, pos, tickState, node) -> ((BlockEntityNode) node).serverTick(tickLevel, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            node.burstIntoOrbs(serverLevel, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !state.is(newState.getBlock())) {
            NodeLocationIndex.get(serverLevel).remove(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
