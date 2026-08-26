package com.leclowndu93150.thaumaturge.content.misc.alumentum;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownAlumentum extends ThrowableItemProjectile {

    public ThrownAlumentum(EntityType<? extends ThrownAlumentum> type, Level level) {
        super(type, level);
    }

    public ThrownAlumentum(Level level, LivingEntity mob, ItemStack itemStack) {
        super(TCEntities.ALUMENTUM.get(), mob, level);
        this.setItem(itemStack);
    }

    public ThrownAlumentum(Level level, double x, double y, double z, ItemStack itemStack) {
        super(TCEntities.ALUMENTUM.get(), x, y, z, level);
        this.setItem(itemStack);
    }

    @Override
    public void shoot(double xd, double yd, double zd, float pow, float uncertainty) {
        super.shoot(xd, yd, zd, 0.75F, uncertainty);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double coeff = i / 3.0;
                Effects.alumentum(
                                (ServerLevel) level(),
                                new Vec3(
                                        (float) (this.xOld + (this.getX() - this.xOld) * coeff),
                                        (float) (this.yOld + (this.getY() - this.yOld) * coeff)
                                                + this.getBbHeight() / 2.0F,
                                        (float) (this.zOld + (this.getZ() - this.zOld) * coeff)))
                        .motion(
                                0.0125F * (this.random.nextFloat() - 0.5F),
                                0.0125F * (this.random.nextFloat() - 0.5F),
                                0.0125F * (this.random.nextFloat() - 0.5F))
                        .color(
                                this.random.nextFloat() * 0.2F,
                                this.random.nextFloat() * 0.1F,
                                this.random.nextFloat() * 0.1F)
                        .alpha(0.5F)
                        .scale(4.0F)
                        .send();

                Effects.spawn(
                        (ServerLevel) level(),
                        new ShieldSparkParticleOptions(0xFFFFFF, 0.7F, 0.3F, 8, 0, false),
                        getX() + level().getRandom().nextGaussian() * 0.2F,
                        getY() + level().getRandom().nextGaussian() * 0.2F,
                        getZ() + level().getRandom().nextGaussian() * 0.2F);
            }
        }
    }

    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.1F, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }

    protected Item getDefaultItem() {
        return Items.EGG;
    }
}
