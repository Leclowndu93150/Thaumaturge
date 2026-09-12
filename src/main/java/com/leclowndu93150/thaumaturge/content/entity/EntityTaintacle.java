package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintacle extends AbstractTaintacle {
    private static final double MELEE_REACH = 3.0;
    private static final int REMOTE_TENTACLE_MIN_COOLDOWN = 40;
    private static final int REMOTE_TENTACLE_COOLDOWN_VARIANCE = 20;

    private int remoteTentacleCooldown;

    public EntityTaintacle(EntityType<? extends EntityTaintacle> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(50.0, 7.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        if (remoteTentacleCooldown > 0) {
            remoteTentacleCooldown--;
        }
        LivingEntity target = getTarget();
        if (target != null
                && target.isAlive()
                && target.onGround()
                && distanceTo(target) > MELEE_REACH
                && remoteTentacleCooldown <= 0
                && hasLineOfSight(target)) {
            spawnRemoteTentacle(server, target);
        }
    }

    private void spawnRemoteTentacle(ServerLevel level, LivingEntity target) {
        BlockPos pos = target.blockPosition();
        boolean eldritch = level.getBiome(pos).is(TCBiomes.ELDRITCH);
        boolean tainted = TaintBiomeManager.isTainted(level, pos);
        boolean taintSubstrate = level.getBlockState(pos).getBlock() instanceof ITaintBlock
                || level.getBlockState(pos.below()).getBlock() instanceof ITaintBlock;
        if (!eldritch && !(tainted && taintSubstrate)) {
            return;
        }

        EntityTaintacleSmall small = TCEntities.TAINTACLE_SMALL.get().create(level);
        if (small == null) {
            return;
        }
        remoteTentacleCooldown = REMOTE_TENTACLE_MIN_COOLDOWN + random.nextInt(REMOTE_TENTACLE_COOLDOWN_VARIANCE);
        small.moveTo(
                target.getX() + random.nextFloat() - random.nextFloat(),
                target.getY(),
                target.getZ() + random.nextFloat() - random.nextFloat(),
                0.0F,
                0.0F);
        level.addFreshEntity(small);

        // TC4 let a Taintacle attack in Eldritch terrain establish a tiny Tainted Lands foothold.
        if (eldritch
                && level.getBlockState(pos).canBeReplaced()
                && TaintHelper.isAdjacentToSolidBlock(level, pos)
                && TaintBiomeManager.taintColumn(level, pos)) {
            level.setBlock(pos, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), 3);
        }
    }
}
