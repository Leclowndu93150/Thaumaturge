package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockTaintGeyser extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintGeyser> CODEC = simpleCodec(BlockTaintGeyser::new);

    private static final float SWARM_SPAWN_CHANCE = 0.2F;
    private static final double SWARM_PLAYER_RANGE = 32.0;
    private static final double SWARM_EXCLUSION_RANGE = 32.0;
    private static final float LOW_FLUX_THRESHOLD = 2.0F;
    private static final float POLLUTE_AMOUNT = 0.25F;

    public BlockTaintGeyser(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintGeyser> codec() {
        return CODEC;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (random.nextFloat() < SWARM_SPAWN_CHANCE && level.hasNearbyAlivePlayer(x, y, z, SWARM_PLAYER_RANGE)
                && level.getEntitiesOfClass(EntityTaintSwarm.class, AABB.ofSize(pos.getCenter(), SWARM_EXCLUSION_RANGE * 2, SWARM_EXCLUSION_RANGE * 2, SWARM_EXCLUSION_RANGE * 2)).isEmpty()) {
            EntityTaintSwarm swarm = TCEntities.TAINT_SWARM.get().create(level, EntitySpawnReason.NATURAL);
            if (swarm != null) {
                swarm.snapTo(x, pos.getY() + 1.25, z, random.nextInt(360), 0.0F);
                level.addFreshEntity(swarm);
            }
        } else if (AuraHelper.getFlux(level, pos) < LOW_FLUX_THRESHOLD) {
            AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
        }
    }
}
