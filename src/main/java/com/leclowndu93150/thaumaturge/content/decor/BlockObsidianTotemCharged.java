package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockObsidianTotemCharged extends BlockObsidianTotem implements EntityBlock {
    public BlockObsidianTotemCharged(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNode(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
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
}
