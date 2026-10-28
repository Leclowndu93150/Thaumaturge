package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockTaintGeyser extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintGeyser> CODEC = simpleCodec(BlockTaintGeyser::new);

    private static final float SWARM_SPAWN_CHANCE = 0.2F;
    private static final double SWARM_PLAYER_RANGE = 32.0;
    private static final double SWARM_EXCLUSION_RANGE = 32.0;
    private static final double SWARM_HEIGHT = 1.25;
    private static final double CELL_CENTRE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;

    public BlockTaintGeyser(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintGeyser> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + CELL_CENTRE;
        double y = pos.getY() + CELL_CENTRE;
        double z = pos.getZ() + CELL_CENTRE;
        if (random.nextFloat() < SWARM_SPAWN_CHANCE
                && level.hasNearbyAlivePlayer(x, y, z, SWARM_PLAYER_RANGE)
                && level.getEntitiesOfClass(EntityTaintSwarm.class, new AABB(pos).inflate(SWARM_EXCLUSION_RANGE))
                        .isEmpty()) {
            EntityTaintSwarm swarm = TTEntities.TAINT_SWARM.get().create(level);
            if (swarm != null) {
                swarm.moveTo(x, pos.getY() + SWARM_HEIGHT, z, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
                level.addFreshEntity(swarm);
            }
        }
    }
}
