package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockDeconstructionTable extends BaseEntityBlock {
    private static final Identifier DECONSTRUCTOR_RESEARCH = TTIds.rl("deconstructor");

    public static final MapCodec<BlockDeconstructionTable> CODEC = simpleCodec(BlockDeconstructionTable::new);

    private static final VoxelShape SHAPE = Shapes.or(box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0), box(0.0, 0.0, 0.0, 3.0, 13.0, 3.0), box(13.0, 0.0, 0.0, 16.0, 13.0, 3.0),
            box(0.0, 0.0, 13.0, 3.0, 13.0, 16.0), box(13.0, 0.0, 13.0, 16.0, 13.0, 16.0), box(3.0, 8.0, 1.0, 13.0, 13.0, 2.0), box(3.0, 8.0, 14.0, 13.0, 13.0, 15.0),
            box(1.0, 8.0, 3.0, 2.0, 13.0, 13.0), box(14.0, 8.0, 3.0, 15.0, 13.0, 13.0), box(3.0, 1.0, 1.0, 13.0, 2.0, 2.0), box(3.0, 1.0, 14.0, 13.0, 2.0, 15.0), box(1.0, 1.0, 3.0, 2.0, 2.0, 13.0),
            box(14.0, 1.0, 3.0, 15.0, 2.0, 13.0));

    public BlockDeconstructionTable(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockDeconstructionTable> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDeconstructionTable(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer && !KnowledgeAccess.of(serverPlayer).isResearchComplete(DECONSTRUCTOR_RESEARCH)) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("tc.device.unknown").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(pos) instanceof BlockEntityDeconstructionTable table) {
                player.openMenu(table, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.DECONSTRUCTION_TABLE.get(), BlockEntityDeconstructionTable::serverTick);
    }
}
