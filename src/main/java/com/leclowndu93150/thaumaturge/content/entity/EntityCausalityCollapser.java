package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class EntityCausalityCollapser extends ThrowableItemProjectile {
    private static final float EXPLOSION_STRENGTH = 2.0F;
    private static final double RIFT_COLLAPSE_RANGE = 3.0;
    private static final int TRAIL_STEPS = 3;
    private static final float TRAIL_JITTER = 0.0125F;
    private static final float MOTE_ALPHA = 0.5F;
    private static final float MOTE_SCALE = 4.0F;
    private static final int SPARK_SPRITE_START = 448;
    private static final int SPARK_SPRITE_COUNT = 8;
    private static final int SPARK_AGE = 8;
    private static final float SPARK_ALPHA = 0.7F;
    private static final float SPARK_SCALE = 0.3F;
    private static final double SPARK_SPREAD = 0.2;

    public EntityCausalityCollapser(EntityType<? extends EntityCausalityCollapser> type, Level level) {
        super(type, level);
    }

    public EntityCausalityCollapser(Level level, LivingEntity shooter, ItemStack stack) {
        super(TCEntities.CAUSALITY_COLLAPSER.get(), shooter, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.CAUSALITY_COLLAPSER.get();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof EntityFluxRift || super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < TRAIL_STEPS; i++) {
                double coeff = (double) i / TRAIL_STEPS;
                this.level().addParticle(
                        Effects.fireMoteData(this.random, TRAIL_JITTER * (this.random.nextFloat() - 0.5F), TRAIL_JITTER * (this.random.nextFloat() - 0.5F),
                                TRAIL_JITTER * (this.random.nextFloat() - 0.5F), 0.8F + this.random.nextFloat() * 0.2F, 0.3F + this.random.nextFloat() * 0.1F, this.random.nextFloat() * 0.1F,
                                MOTE_ALPHA, MOTE_SCALE),
                        this.xOld + (this.getX() - this.xOld) * coeff, this.yOld + (this.getY() - this.yOld) * coeff + this.getBbHeight() / 2.0F, this.zOld + (this.getZ() - this.zOld) * coeff, 0.0,
                        0.0, 0.0);
                this.level().addParticle(new ShieldSparkParticleOptions(0xFFFFFF, SPARK_ALPHA, SPARK_SCALE, SPARK_AGE, 0, false), this.getX() + this.random.nextGaussian() * SPARK_SPREAD,
                        this.getY() + this.random.nextGaussian() * SPARK_SPREAD, this.getZ() + this.random.nextGaussian() * SPARK_SPREAD, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level().isClientSide()) {
            return;
        }
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_STRENGTH, Level.ExplosionInteraction.MOB);
        for (EntityFluxRift rift : this.level().getEntitiesOfClass(EntityFluxRift.class, this.getBoundingBox().inflate(RIFT_COLLAPSE_RANGE))) {
            rift.setCollapse(true);
        }
        this.discard();
    }
}
