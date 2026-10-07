package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityAspectOrb extends Entity {
    private static final EntityDataAccessor<String> DATA_ASPECT =
            SynchedEntityData.defineId(EntityAspectOrb.class, EntityDataSerializers.STRING);
    public static final int MAX_AGE = 150;
    private static final int DEFAULT_HEALTH = 5;
    private static final int PLAYER_SCAN_PERIOD = 5;
    private static final double FOLLOW_RANGE = 8.0;
    private static final float GROUND_BOUNCE = 0.9F;
    private static final float FRICTION = 0.98F;

    private int age;
    private int health = DEFAULT_HEALTH;
    private int aspectValue = 1;
    private Player followingPlayer;

    public EntityAspectOrb(EntityType<? extends EntityAspectOrb> type, Level level) {
        super(type, level);
    }

    public EntityAspectOrb(Level level, double x, double y, double z, ResourceKey<IAspect> aspect, int value) {
        this(TTEntities.ASPECT_ORB.get(), level);
        setPos(x, y, z);
        setYRot(random.nextFloat() * 360.0F);
        setDeltaMovement(
                (random.nextDouble() * 0.2 - 0.1) * 2.0,
                random.nextDouble() * 0.2 * 2.0,
                (random.nextDouble() * 0.2 - 0.1) * 2.0);
        this.aspectValue = value;
        setAspect(aspect);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ASPECT, TTAspects.AER.location().toString());
    }

    public ResourceKey<IAspect> getAspect() {
        String stored = entityData.get(DATA_ASPECT);
        ResourceLocation id = stored.indexOf(':') >= 0
                ? ResourceLocation.tryParse(stored)
                : ResourceLocation.tryBuild(TTIds.MODID, stored);
        return id == null ? TTAspects.AER : ResourceKey.create(IAspect.REGISTRY_KEY, id);
    }

    public void setAspect(ResourceKey<IAspect> aspect) {
        entityData.set(DATA_ASPECT, aspect.location().toString());
    }

    public int getAge() {
        return age;
    }

    public int getAspectColor() {
        Holder<IAspect> holder = level().registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(getAspect())
                .orElse(null);
        return holder == null ? 0xFFFFFF : holder.value().color();
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        if (isEyeInFluid(FluidTags.WATER)) {
            Vec3 movement = getDeltaMovement();
            setDeltaMovement(movement.x * 0.99, Math.min(movement.y + 5.0E-4, 0.06), movement.z * 0.99);
        } else {
            applyGravity();
        }
        if (level().getFluidState(blockPosition()).is(FluidTags.LAVA)) {
            setDeltaMovement(
                    (random.nextFloat() - random.nextFloat()) * 0.2F,
                    0.2,
                    (random.nextFloat() - random.nextFloat()) * 0.2F);
            playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.4F, 2.0F + random.nextFloat() * 0.4F);
        }
        followNearbyPlayer();
        double fallSpeed = getDeltaMovement().y;
        move(MoverType.SELF, getDeltaMovement());
        checkInsideBlocks();
        float friction = FRICTION;
        if (onGround()) {
            friction = level().getBlockState(getBlockPosBelowThatAffectsMyMovement())
                            .getBlock()
                            .getFriction()
                    * FRICTION;
        }
        setDeltaMovement(getDeltaMovement().multiply(friction, FRICTION, friction));
        if (verticalCollisionBelow && fallSpeed < -getGravity()) {
            setDeltaMovement(new Vec3(getDeltaMovement().x, -fallSpeed * GROUND_BOUNCE, getDeltaMovement().z));
        }
        age++;
        if (age >= MAX_AGE) {
            discard();
        }
    }

    private void followNearbyPlayer() {
        if (level().isClientSide()) {
            return;
        }
        if (tickCount % PLAYER_SCAN_PERIOD == 0
                && (followingPlayer == null || followingPlayer.distanceToSqr(this) > FOLLOW_RANGE * FOLLOW_RANGE)) {
            followingPlayer = null;
            double closest = Double.MAX_VALUE;
            for (Player player :
                    level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(FOLLOW_RANGE))) {
                double distance = player.distanceToSqr(this);
                if (distance < closest
                        && !player.isSpectator()
                        && !WandVisHelper.findWandInHotbarWithRoom(player, getAspect(), aspectValue)
                                .isEmpty()) {
                    closest = distance;
                    followingPlayer = player;
                }
            }
        }
        if (followingPlayer != null) {
            Vec3 delta = new Vec3(
                    followingPlayer.getX() - getX(),
                    followingPlayer.getY() + followingPlayer.getEyeHeight() - getY(),
                    followingPlayer.getZ() - getZ());
            double distance = delta.length();
            double power = 1.0 - distance / FOLLOW_RANGE;
            if (power > 0.0) {
                power *= power;
                setDeltaMovement(getDeltaMovement().add(delta.normalize().scale(power * 0.1)));
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (player instanceof ServerPlayer && player.takeXpDelay == 0) {
            ItemStack wand = WandVisHelper.findWandInHotbarWithRoom(player, getAspect(), aspectValue);
            if (!wand.isEmpty() && TTAspects.PRIMALS.contains(getAspect())) {
                WandVisHelper.addVis(wand, getAspect(), aspectValue, true);
                player.takeXpDelay = 2;
                player.take(this, 1);
                playSound(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        0.1F,
                        0.5F * ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.8F));
                discard();
            }
        }
    }

    @Override
    public final boolean hurt(DamageSource source, float amount) {
        if (super.isInvulnerableTo(source)) {
            return false;
        }
        if (level().isClientSide()) {
            return true;
        }
        markHurt();
        health = (int) (health - amount);
        if (health <= 0) {
            discard();
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        output.putShort("Health", (short) health);
        output.putShort("Age", (short) age);
        output.putShort("Value", (short) aspectValue);
        output.putString("Aspect", entityData.get(DATA_ASPECT));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
        health = input.contains("Health") ? input.getShort("Health") : (short) DEFAULT_HEALTH;
        age = input.getShort("Age");
        aspectValue = input.contains("Value") ? input.getShort("Value") : (short) 1;
        entityData.set(
                DATA_ASPECT,
                input.contains("Aspect")
                        ? input.getString("Aspect")
                        : TTAspects.AER.location().toString());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }
}
