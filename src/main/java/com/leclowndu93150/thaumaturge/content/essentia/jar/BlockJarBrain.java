package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockJarBrain extends BaseEntityBlock {
    public static final MapCodec<BlockJarBrain> CODEC = simpleCodec(BlockJarBrain::new);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);
    private static final int RELEASE_EAT_DELAY = 40;
    private static final int MAX_RELEASE = 64;

    public BlockJarBrain(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockJarBrain> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarBrain(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJarBrain jar)) {
            return InteractionResult.PASS;
        }
        jar.setEatDelay(RELEASE_EAT_DELAY);
        if (level instanceof ServerLevel server) {
            int release = server.getRandom().nextInt(Math.min(jar.xp() + 1, MAX_RELEASE));
            if (release > 0) {
                jar.setXp(jar.xp() - release);
                ExperienceOrb.award(server, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), release);
                jar.setChanged();
                jar.syncToClient();
            }
        } else {
            level.playSound(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.JAR_BRAIN.get(), level.isClientSide() ? BlockEntityJarBrain::clientTick : BlockEntityJarBrain::serverTick);
    }
}
