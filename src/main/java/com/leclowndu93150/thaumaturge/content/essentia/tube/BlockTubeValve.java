package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public final class BlockTubeValve extends BlockTube {
    public static final MapCodec<BlockTubeValve> CODEC = simpleCodec(BlockTubeValve::new);

    private static final double OPEN_HEAD_DROP = 0.5;
    private static final double CLOSED_HEAD_DROP = 2.0;
    private static final Map<Direction, VoxelShape> OPEN_HEADS = DeviceShapes.facingShapesFromUp(head(OPEN_HEAD_DROP));
    private static final Map<Direction, VoxelShape> CLOSED_HEADS = DeviceShapes.facingShapesFromUp(head(CLOSED_HEAD_DROP));

    public BlockTubeValve(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockTube> codec() {
        return CODEC;
    }

    private static VoxelShape head(double drop) {
        return Shapes.or(box(7.0, 10.0, 7.0, 9.0, 14.0 - drop, 9.0), box(5.0, 13.0 - drop, 5.0, 11.0, 15.0 - drop, 11.0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape body = super.getShape(state, level, pos, context);
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeValve valve)) {
            return body;
        }
        Map<Direction, VoxelShape> heads = valve.allowFlow() ? OPEN_HEADS : CLOSED_HEADS;
        return Shapes.or(body, heads.get(valve.facing()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTubeValve(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, TTBlockEntities.TUBE_VALVE.get(), (lvl, pos, st, tube) -> tube.tickClient(lvl, pos, st));
        }
        return createTickerHelper(type, TTBlockEntities.TUBE_VALVE.get(), (lvl, pos, st, tube) -> tube.tickServer(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeValve valve))
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            valve.setAllowFlow(!valve.allowFlow());
            level.playSound(null, pos, TTSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.SUCCESS;
    }
}
