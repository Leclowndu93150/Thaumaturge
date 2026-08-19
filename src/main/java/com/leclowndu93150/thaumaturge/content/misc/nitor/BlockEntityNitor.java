package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityNitor extends BlockEntity {
    private int count = 0;

    public BlockEntityNitor(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NITOR.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityNitor be) {
        if (!level.isClientSide())
            return;
        if (!(state.getBlock() instanceof BlockNitor nitor))
            return;
        int rgb = nitor.dyeColor();
        double cx = pos.getX() + 0.5 + level.getRandom().nextGaussian() * 0.025;
        double cy = pos.getY() + 0.45 + level.getRandom().nextGaussian() * 0.025;
        double cz = pos.getZ() + 0.5 + level.getRandom().nextGaussian() * 0.025;
        double vx = level.getRandom().nextGaussian() * 0.0025;
        double vy = level.getRandom().nextFloat() * 0.06;
        double vz = level.getRandom().nextGaussian() * 0.0025;
        ClientEffects.nitorFlames(level, cx, cy, cz, vx, vy, vz, rgb, 0);
        if (be.count++ % 10 == 0) {
            ClientEffects.nitorCore(level, pos.getX() + 0.5, pos.getY() + 0.49, pos.getZ() + 0.5, 0.0, 0.0, 0.0, rgb);
        }
    }
}
