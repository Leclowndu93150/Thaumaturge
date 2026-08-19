package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockJarNode extends Block implements EntityBlock {
    public static final MapCodec<BlockJarNode> CODEC = simpleCodec(BlockJarNode::new);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public BlockJarNode(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockJarNode> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ICaster) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }
            if (serverLevel.getBlockEntity(pos) instanceof BlockEntityJarNode jar) {
                NodeData data = new NodeData(jar.getNodeType(), Optional.ofNullable(jar.getNodeModifier()), jar.getAspects(), jar.getAspectsBase());
                serverLevel.removeBlockEntity(pos);
                serverLevel.setBlock(pos, TCBlocks.NODE.get().defaultBlockState(), Block.UPDATE_ALL);
                if (serverLevel.getBlockEntity(pos) instanceof BlockEntityNode node) {
                    node.applyNodeData(data);
                    node.setChanged();
                    serverLevel.sendBlockUpdated(pos, node.getBlockState(), node.getBlockState(), Block.UPDATE_ALL);
                }
                serverLevel.levelEvent(2001, pos, Block.getId(state));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarNode(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TCBlockEntities.JAR_NODE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, node) -> ((BlockEntityJarNode) node).serverTick(tickLevel, pos);
    }
}
