package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntityFollowingItem extends ItemEntity implements IEntityWithComplexSpawn {
    private static final int NO_COLLECTOR = -1;
    private static final int ACCELERATION_TICKS = 20;
    private static final double ARRIVAL_RADIUS = 0.5;
    private static final double ARRIVAL_BRAKE = 0.1;
    private static final int BUBBLE_COLOR = 0xFF6F86FF;
    private static final float BUBBLE_SCALE = 0.35F;
    private static final float BUBBLE_SCALE_SPREAD = 0.25F;
    private static final int BUBBLE_AGE = 14;
    private static final int BUBBLE_AGE_SPREAD = 8;
    private static final float BUBBLE_RISE = -0.002F;
    private static final double BUBBLE_SCATTER = 0.12;

    private @Nullable Entity collector;
    private int pursuitTicks;

    public EntityFollowingItem(EntityType<? extends EntityFollowingItem> type, Level level) {
        super(type, level);
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, Entity collector) {
        this(TTEntities.FOLLOWING_ITEM.get(), level);
        setPos(x, y, z);
        setItem(stack);
        setYRot(random.nextFloat() * 360.0F);
        lifespan = stack.getEntityLifespan(level);
        follow(collector);
    }

    @Override
    public void tick() {
        if (collector != null) {
            pursue(collector);
        }
        super.tick();
    }

    private void follow(@Nullable Entity entity) {
        collector = entity;
        setNoGravity(entity != null);
    }

    private void pursue(Entity target) {
        if (target.isRemoved()) {
            follow(null);
            return;
        }
        Vec3 toTarget = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(position());
        double distance = toTarget.length();
        if (distance <= ARRIVAL_RADIUS) {
            setDeltaMovement(getDeltaMovement().scale(ARRIVAL_BRAKE));
            follow(null);
            return;
        }
        pursuitTicks = Math.min(pursuitTicks + 1, ACCELERATION_TICKS - 1);
        setDeltaMovement(toTarget.scale(1.0 / (distance * (ACCELERATION_TICKS - pursuitTicks))));
        if (level().isClientSide()) {
            trailBubble();
        }
    }

    private void trailBubble() {
        RandomSource random = getRandom();
        BubbleParticleOptions bubble = new BubbleParticleOptions(BUBBLE_COLOR, 1.0F, BUBBLE_SCALE + random.nextFloat() * BUBBLE_SCALE_SPREAD, BUBBLE_AGE + random.nextInt(BUBBLE_AGE_SPREAD),
                BUBBLE_RISE, false);
        level().addParticle(bubble, xo + random.triangle(0.0, BUBBLE_SCATTER), yo + getBbHeight() * 0.5 + random.triangle(0.0, BUBBLE_SCATTER), zo + random.triangle(0.0, BUBBLE_SCATTER), 0.0, 0.0,
                0.0);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        ByteBufCodecs.VAR_INT.encode(buffer, collector == null ? NO_COLLECTOR : collector.getId());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int collectorId = ByteBufCodecs.VAR_INT.decode(buffer);
        follow(collectorId == NO_COLLECTOR ? null : level().getEntity(collectorId));
    }
}
