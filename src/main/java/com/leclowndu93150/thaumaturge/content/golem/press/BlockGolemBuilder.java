package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockGolemBuilder extends BaseEntityBlock {
    private static final Identifier MIND_CLOCKWORK_RESEARCH = TCIds.rl("mind_clockwork");

    public static final MapCodec<BlockGolemBuilder> CODEC = simpleCodec(BlockGolemBuilder::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public BlockGolemBuilder(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockGolemBuilder> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityGolemBuilder(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, TCBlockEntities.GOLEM_BUILDER.get(), BlockEntityGolemBuilder::clientTick)
                : createTickerHelper(type, TCBlockEntities.GOLEM_BUILDER.get(), BlockEntityGolemBuilder::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return openBuilderGui(level, pos, player);
    }

    public static InteractionResult openBuilderGui(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && !KnowledgeAccess.of(serverPlayer).isResearchComplete(MIND_CLOCKWORK_RESEARCH)) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("tc.device.unknown").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityGolemBuilder builder) {
            player.openMenu(builder, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        restoreStructure(level, pos, pos);
        super.destroy(level, pos, state);
    }

    public static void restoreStructure(LevelAccessor level, BlockPos pos, BlockPos startPos) {
        if (level.isClientSide()) {
            return;
        }
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos offset = pos.offset(x, y, z);
                    if (offset.equals(startPos)) {
                        continue;
                    }
                    BlockState neighbor = level.getBlockState(offset);
                    if (neighbor.is(TCBlocks.PLACEHOLDER_IRON_BARS.get())) {
                        level.setBlock(offset, Blocks.IRON_BARS.defaultBlockState(), 3);
                    } else if (neighbor.is(TCBlocks.PLACEHOLDER_ANVIL.get())) {
                        level.setBlock(offset, Blocks.ANVIL.defaultBlockState(), 3);
                    } else if (neighbor.is(TCBlocks.PLACEHOLDER_CAULDRON.get())) {
                        level.setBlock(offset, Blocks.CAULDRON.defaultBlockState(), 3);
                    } else if (neighbor.is(TCBlocks.PLACEHOLDER_TABLE.get())) {
                        level.setBlock(offset, TCBlocks.TABLE_STONE.get().defaultBlockState(), 3);
                    }
                }
            }
        }
        if (!pos.equals(startPos)) {
            level.setBlock(pos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP), 3);
        }
    }
}
