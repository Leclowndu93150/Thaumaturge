package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchTrap extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchTrap> CODEC = simpleCodec(BlockEldritchTrap::new);

    private static final int RUNE_DURATION_BASE = 16;

    public BlockEldritchTrap(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchTrap> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchTrap(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.ELDRITCH_TRAP.get(), (tickLevel, pos, tickState, trap) -> trap.serverTick(tickLevel, pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int x = pos.getX() + random.nextInt(2) - random.nextInt(2);
        int y = pos.getY() + random.nextInt(2) - random.nextInt(2);
        int z = pos.getZ() + random.nextInt(2) - random.nextInt(2);
        if (level.isEmptyBlock(new BlockPos(x, y, z))) {
            level.addParticle(
                    new BlockRunesParticleOptions(0.5F + random.nextFloat() * 0.5F, random.nextFloat() * 0.3F, 0.9F + random.nextFloat() * 0.1F, RUNE_DURATION_BASE + random.nextInt(4), 0.0F, false),
                    x + 0.5, y + 0.5, z + 0.5, 0.0, 0.0, 0.0);
        }
    }
}
