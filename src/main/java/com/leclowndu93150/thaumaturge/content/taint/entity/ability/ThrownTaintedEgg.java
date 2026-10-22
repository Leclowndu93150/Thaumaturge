package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ThrownTaintedEgg extends ThrowableItemProjectile {
    public static final ThrowProfile THROW = new ThrowProfile(1.5F, 1.0F, 0.0F, 0.5F);

    private static final byte SHATTER_EVENT = 3;
    private static final int GROWTH_ATTEMPTS = 3;
    private static final int SWARM_ONE_IN = 8;
    private static final int SHARD_COUNT = 8;
    private static final double SHARD_SPREAD = 0.08;
    private static final double SHARD_CENTER = 0.5;

    public ThrownTaintedEgg(EntityType<? extends ThrownTaintedEgg> type, Level level) {
        super(type, level);
    }

    public ThrownTaintedEgg(Level level, LivingEntity thrower, ItemStack stack) {
        super(TTEntities.TAINTED_EGG.get(), thrower, level);
        setItem(stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.TAINTED_EGG.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != SHATTER_EVENT) {
            super.handleEntityEvent(id);
            return;
        }
        ItemStack item = this.getItem();
        if (item.isEmpty()) {
            return;
        }
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, item);
        for (int i = 0; i < SHARD_COUNT; i++) {
            this.level()
                    .addParticle(
                            shard,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            (this.random.nextFloat() - SHARD_CENTER) * SHARD_SPREAD,
                            (this.random.nextFloat() - SHARD_CENTER) * SHARD_SPREAD,
                            (this.random.nextFloat() - SHARD_CENTER) * SHARD_SPREAD);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos origin = BlockPos.containing(hit.getLocation());
        for (int i = 0; i < GROWTH_ATTEMPTS; i++) {
            TaintHelper.attemptFibreGrowth(level, origin, true);
        }
        if (level.getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(SWARM_ONE_IN) == 0) {
            EntityTaintSwarm.release(level, this.getX(), this.getY(), this.getZ());
        }
        level.broadcastEntityEvent(this, SHATTER_EVENT);
        this.discard();
    }
}
