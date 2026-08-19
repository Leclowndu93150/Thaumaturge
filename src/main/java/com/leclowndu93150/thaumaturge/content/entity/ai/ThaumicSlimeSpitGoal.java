package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class ThaumicSlimeSpitGoal extends Goal {
    private static final int COOLDOWN_TICKS = 100;
    private static final int COOLDOWN_RESET_TICKS = 101;
    private static final double PLAYER_RANGE = 16.0;
    private static final double MIN_SPIT_DISTANCE = 4.0;
    private static final int MIN_SPIT_SIZE = 3;
    private static final float SPIT_SPEED = 1.5F;
    private static final float SPIT_SPREAD = 1.0F;
    private static final float SPREAD_SCALE = 0.0075F;
    private static final float ARC_COMPENSATION = 0.2F;

    private final ThaumicSlime slime;
    private int cooldown = COOLDOWN_TICKS;

    public ThaumicSlimeSpitGoal(ThaumicSlime slime) {
        this.slime = slime;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        Player nearest = slime.level().getNearestPlayer(slime, PLAYER_RANGE);
        if (nearest == null) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (slime.getSize() < MIN_SPIT_SIZE) {
            return false;
        }
        return slime.distanceTo(nearest) > MIN_SPIT_DISTANCE;
    }

    @Override
    public void start() {
        if (!(slime.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player target = serverLevel.getNearestPlayer(slime, PLAYER_RANGE);
        if (target == null) {
            return;
        }
        cooldown = COOLDOWN_RESET_TICKS;
        ThaumicSlime child = TCEntities.THAUMIC_SLIME.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (child == null) {
            return;
        }
        child.setSize(1, true);
        AABB box = slime.getBoundingBox();
        double originY = (box.minY + box.maxY) / 2.0;
        double dx = target.getX() - slime.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0F - originY;
        double dz = target.getZ() - slime.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist >= 1.0E-7) {
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(dy, hDist) * 180.0 / Math.PI));
            child.snapTo(slime.getX() + dx / hDist, originY, slime.getZ() + dz / hDist, yaw, pitch);
            shoot(child, dx, dy + hDist * ARC_COMPENSATION, dz, SPIT_SPEED, SPIT_SPREAD);
        }
        serverLevel.addFreshEntity(child);
        slime.playSound(TCSounds.GORE.get(), 1.0F, ((slime.getRandom().nextFloat() - slime.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
        slime.setSize(slime.getSize() - 1, true);
    }

    private void shoot(ThaumicSlime child, double dx, double dy, double dz, float speed, float spread) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        dx += child.getRandom().nextGaussian() * SPREAD_SCALE * spread;
        dy += child.getRandom().nextGaussian() * SPREAD_SCALE * spread;
        dz += child.getRandom().nextGaussian() * SPREAD_SCALE * spread;
        dx *= speed;
        dy *= speed;
        dz *= speed;
        child.setDeltaMovement(dx, dy, dz);
        float hLength = (float) Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(dy, hLength) * 180.0 / Math.PI);
        child.setYRot(yaw);
        child.yRotO = yaw;
        child.setXRot(pitch);
        child.xRotO = pitch;
    }
}
