package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityAspectOrb extends Entity {
    private static final EntityDataAccessor<String> DATA_ASPECT = SynchedEntityData.defineId(EntityAspectOrb.class, EntityDataSerializers.STRING);
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
        this(TCEntities.ASPECT_ORB.get(), level);
        setPos(x, y, z);
        setYRot(random.nextFloat() * 360.0F);
        setDeltaMovement((random.nextDouble() * 0.2 - 0.1) * 2.0, random.nextDouble() * 0.2 * 2.0, (random.nextDouble() * 0.2 - 0.1) * 2.0);
        this.aspectValue = value;
        setAspect(aspect);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ASPECT, TCAspects.AER.identifier().getPath());
    }

    public ResourceKey<IAspect> getAspect() {
        return ResourceKey.create(IAspect.REGISTRY_KEY, Identifier.fromNamespaceAndPath("thaumaturge", entityData.get(DATA_ASPECT)));
    }

    public void setAspect(ResourceKey<IAspect> aspect) {
        entityData.set(DATA_ASPECT, aspect.identifier().getPath());
    }

    public int getAge() {
        return age;
    }

    public int getAspectColor() {
        Holder<IAspect> holder = level().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(getAspect()).orElse(null);
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
            setDeltaMovement((random.nextFloat() - random.nextFloat()) * 0.2F, 0.2, (random.nextFloat() - random.nextFloat()) * 0.2F);
            playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.4F, 2.0F + random.nextFloat() * 0.4F);
        }
        followNearbyPlayer();
        double fallSpeed = getDeltaMovement().y;
        move(MoverType.SELF, getDeltaMovement());
        applyEffectsFromBlocks();
        float friction = FRICTION;
        if (onGround()) {
            friction = level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * FRICTION;
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
        if (tickCount % PLAYER_SCAN_PERIOD == 0 && (followingPlayer == null || followingPlayer.distanceToSqr(this) > FOLLOW_RANGE * FOLLOW_RANGE)) {
            followingPlayer = null;
            double closest = Double.MAX_VALUE;
            for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(FOLLOW_RANGE))) {
                double distance = player.distanceToSqr(this);
                if (distance < closest && !player.isSpectator() && !WandVisHelper.findWandInHotbarWithRoom(player, getAspect(), aspectValue).isEmpty()) {
                    closest = distance;
                    followingPlayer = player;
                }
            }
        }
        if (followingPlayer != null) {
            Vec3 delta = new Vec3(followingPlayer.getX() - getX(), followingPlayer.getY() + followingPlayer.getEyeHeight() - getY(), followingPlayer.getZ() - getZ());
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
            if (!wand.isEmpty() && TCAspects.PRIMALS.contains(getAspect())) {
                WandVisHelper.addVis(wand, getAspect(), aspectValue, true);
                player.takeXpDelay = 2;
                player.take(this, 1);
                playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F, 0.5F * ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.8F));
                discard();
            }
        }
    }

    @Override
    public final boolean hurtClient(DamageSource source) {
        return !isInvulnerableToBase(source);
    }

    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableToBase(source)) {
            return false;
        }
        markHurt();
        health = (int) (health - amount);
        if (health <= 0) {
            discard();
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putShort("Health", (short) health);
        output.putShort("Age", (short) age);
        output.putShort("Value", (short) aspectValue);
        output.putString("Aspect", entityData.get(DATA_ASPECT));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        health = input.getShortOr("Health", (short) DEFAULT_HEALTH);
        age = input.getShortOr("Age", (short) 0);
        aspectValue = input.getShortOr("Value", (short) 1);
        entityData.set(DATA_ASPECT, input.getStringOr("Aspect", TCAspects.AER.identifier().getPath()));
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
