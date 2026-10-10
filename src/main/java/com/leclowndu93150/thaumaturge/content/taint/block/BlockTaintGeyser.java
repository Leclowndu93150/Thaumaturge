package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockTaintGeyser extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintGeyser> CODEC = simpleCodec(BlockTaintGeyser::new);

    private static final int SWARM_ONE_IN = 5;
    private static final double PLAYER_RANGE = 32.0;
    private static final double SWARM_SPACING = 32.0;
    private static final double CELL_CENTRE = 0.5;
    private static final double HOVER_HEIGHT = 1.2;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float POLLUTION_AMOUNT = 1.0F;
    private static final float POLLUTION_FLUX_RATIO = 0.25F;

    public BlockTaintGeyser(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintGeyser> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, FluxGooFluid.gooBlockState(PhysicalFlux.MAX_QUANTA));
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!trySpawnSwarm(level, pos, random)) {
            topUpFlux(level, pos);
        }
    }

    private static void topUpFlux(ServerLevel level, BlockPos pos) {
        int base = AuraHelper.getAuraBase(level, pos);
        boolean belowRatio = AuraHelper.getFlux(level, pos) < POLLUTION_FLUX_RATIO * base;
        if (base > 0 && belowRatio) {
            AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
        }
    }

    private static boolean trySpawnSwarm(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SWARM_ONE_IN) != 0 || !swarmWanted(level, pos)) {
            return false;
        }
        EntityTaintSwarm swarm = TTEntities.TAINT_SWARM.get().create(level, EntitySpawnReason.NATURAL);
        if (swarm == null) {
            return false;
        }
        swarm.snapTo(pos.getX() + CELL_CENTRE, pos.getY() + HOVER_HEIGHT, pos.getZ() + CELL_CENTRE, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        return level.addFreshEntity(swarm);
    }

    private static boolean swarmWanted(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos);
        if (!level.hasNearbyAlivePlayer(centre.x, centre.y, centre.z, PLAYER_RANGE)) {
            return false;
        }
        return level.getEntitiesOfClass(EntityTaintSwarm.class, new AABB(pos).inflate(SWARM_SPACING)).isEmpty();
    }
}
