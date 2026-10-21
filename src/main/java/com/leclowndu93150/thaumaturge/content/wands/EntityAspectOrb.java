package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbPickupEvent;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbSnapshot;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class EntityAspectOrb extends Entity {
    public static final int MAX_AGE = 150;

    private static final EntityDataAccessor<String> DATA_ASPECT = SynchedEntityData.defineId(EntityAspectOrb.class, EntityDataSerializers.STRING);
    private static final short DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_VALUE = 1;
    private static final String KEY_HEALTH = "Health";
    private static final String KEY_AGE = "Age";
    private static final String KEY_VALUE = "Value";
    private static final String KEY_ASPECT = "Aspect";
    private static final double FOLLOW_RANGE = 8.0;
    private static final int PLAYER_SCAN_PERIOD = 5;
    private static final double FOLLOW_STRENGTH = 0.1;
    private static final double GRAVITY = 0.03;
    private static final double BOUNCE_FACTOR = 0.9;
    private static final float FRICTION = 0.98F;
    private static final double UNDERWATER_DRAG = 0.99;
    private static final double UNDERWATER_LIFT = 0.0005;
    private static final double UNDERWATER_MAX_RISE = 0.06;
    private static final double LAVA_SPREAD = 0.2;
    private static final double LAVA_RISE = 0.2;
    private static final float LAVA_HISS_VOLUME = 0.4F;
    private static final float LAVA_HISS_PITCH_BASE = 2.0F;
    private static final float LAVA_HISS_PITCH_SPREAD = 0.4F;
    private static final double SPAWN_HORIZONTAL_SPEED = 0.4;
    private static final double SPAWN_HORIZONTAL_OFFSET = 0.2;
    private static final double SPAWN_VERTICAL_SPEED = 0.4;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final int PICKUP_DELAY_TICKS = 2;
    private static final float PICKUP_VOLUME = 0.1F;
    private static final float PICKUP_PITCH_FACTOR = 0.5F;
    private static final float PICKUP_PITCH_SPREAD = 0.7F;
    private static final float PICKUP_PITCH_BASE = 1.8F;
    private static final int PICKUP_ITEM_COUNT = 1;

    private int age = DEFAULT_AGE;
    private int health = DEFAULT_HEALTH;
    private int value = DEFAULT_VALUE;
    private boolean collecting;
    private @Nullable Player followTarget;

    public EntityAspectOrb(EntityType<? extends EntityAspectOrb> type, Level level) {
        super(type, level);
    }

    public EntityAspectOrb(Level level, double x, double y, double z, ResourceKey<IAspect> aspect, int value) {
        this(TTEntities.ASPECT_ORB.get(), level);
        setPos(x, y, z);
        setYRot(random.nextFloat() * FULL_TURN_DEGREES);
        setDeltaMovement((random.nextDouble() * SPAWN_HORIZONTAL_SPEED) - SPAWN_HORIZONTAL_OFFSET, random.nextDouble() * SPAWN_VERTICAL_SPEED,
                (random.nextDouble() * SPAWN_HORIZONTAL_SPEED) - SPAWN_HORIZONTAL_OFFSET);
        setAspect(aspect);
        this.value = value;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ASPECT, TTAspects.AER.identifier().toString());
    }

    public ResourceKey<IAspect> getAspect() {
        String text = entityData.get(DATA_ASPECT);
        Identifier id = Identifier.tryParse(text.indexOf(':') >= 0 ? text : TTIds.MODID + ":" + text);
        return id == null ? TTAspects.AER : ResourceKey.create(IAspect.REGISTRY_KEY, id);
    }

    boolean hasKnownAspect() {
        String text = entityData.get(DATA_ASPECT);
        Identifier id = Identifier.tryParse(text.indexOf(':') >= 0 ? text : TTIds.MODID + ":" + text);
        return id != null && level().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).isPresent();
    }

    public void setAspect(ResourceKey<IAspect> aspect) {
        entityData.set(DATA_ASPECT, aspect.identifier().toString());
    }

    public int getAge() {
        return age;
    }

    public int getAspectColor() {
        return WandVisHelper.colorOf(level().registryAccess(), getAspect());
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    public void tick() {
        super.tick();
        if (isEyeInFluid(FluidTags.WATER)) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x * UNDERWATER_DRAG, Math.min(motion.y + UNDERWATER_LIFT, UNDERWATER_MAX_RISE), motion.z * UNDERWATER_DRAG);
        } else {
            applyGravity();
        }
        if (level().getFluidState(blockPosition()).is(FluidTags.LAVA)) {
            setDeltaMovement((random.nextFloat() - random.nextFloat()) * LAVA_SPREAD, LAVA_RISE, (random.nextFloat() - random.nextFloat()) * LAVA_SPREAD);
            playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, LAVA_HISS_VOLUME, LAVA_HISS_PITCH_BASE + random.nextFloat() * LAVA_HISS_PITCH_SPREAD);
        }
        if (level() instanceof ServerLevel) {
            followPlayer();
        }
        double fallSpeed = getDeltaMovement().y;
        move(MoverType.SELF, getDeltaMovement());
        applyEffectsFromBlocks();
        float horizontalFriction = FRICTION;
        if (onGround()) {
            BlockPos below = getBlockPosBelowThatAffectsMyMovement();
            horizontalFriction = level().getBlockState(below).getBlock().getFriction() * FRICTION;
        }
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x * horizontalFriction, motion.y * FRICTION, motion.z * horizontalFriction);
        if (verticalCollisionBelow && fallSpeed < -GRAVITY) {
            Vec3 damped = getDeltaMovement();
            setDeltaMovement(damped.x, -fallSpeed * BOUNCE_FACTOR, damped.z);
        }
        age++;
        if (age >= MAX_AGE) {
            discard();
        }
    }

    private void followPlayer() {
        if (followTarget != null && (followTarget.isRemoved() || followTarget.distanceToSqr(this) > FOLLOW_RANGE * FOLLOW_RANGE)) {
            followTarget = null;
        }
        if (followTarget == null && tickCount % PLAYER_SCAN_PERIOD == 0) {
            followTarget = level().getNearestPlayer(getX(), getY(), getZ(), FOLLOW_RANGE, this::canAttract);
        }
        if (followTarget == null) {
            return;
        }
        Vec3 offset = followTarget.getEyePosition().subtract(position());
        double distance = offset.length();
        if (distance >= FOLLOW_RANGE || distance <= 0.0) {
            return;
        }
        double pull = 1.0 - distance / FOLLOW_RANGE;
        setDeltaMovement(getDeltaMovement().add(offset.scale(1.0 / distance).scale(pull * pull * FOLLOW_STRENGTH)));
    }

    private boolean canAttract(Entity candidate) {
        return candidate instanceof Player player && !player.isSpectator() && !findWand(player).isEmpty();
    }

    private ItemStack findWand(Player player) {
        return WandVisHelper.findWandInHotbarWithRoom(player, getAspect(), value);
    }

    @Override
    public void playerTouch(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            collect(serverPlayer);
        }
    }

    public AspectOrbSnapshot pickupSnapshot(ServerPlayer player) {
        return new AspectOrbSnapshot(getAspect(), value, !collecting && canCollect(player));
    }

    private boolean canCollect(ServerPlayer player) {
        return level() instanceof ServerLevel serverLevel && serverLevel.getServer().isSameThread() && player.level() == serverLevel && serverLevel.getEntity(getUUID()) == this && isAlive()
                && age < MAX_AGE && value > 0 && value <= Integer.MAX_VALUE / WandEconomy.CENTIVIS_PER_VIS && player.isAlive() && !player.isSpectator() && player.takeXpDelay == 0
                && player.distanceToSqr(this) <= 9.0 && hasKnownAspect() && TTAspects.PRIMALS.contains(getAspect());
    }

    public int collect(ServerPlayer player) {
        if (collecting || !canCollect(player)) {
            return 0;
        }
        collecting = true;
        try {
            AspectOrbSnapshot source = new AspectOrbSnapshot(getAspect(), value, true);
            int originalCredit = value * WandEconomy.CENTIVIS_PER_VIS;
            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                WandSlot slot = WandSlot.playerInventory(player, slotIndex);
                var initialQuote = WandAccess.quoteCharge(player, slot, source.aspect(), originalCredit);
                if (initialQuote.isEmpty() || initialQuote.get().acceptedCentivis() == 0) {
                    continue;
                }
                ItemStack recipient = slot.stack();
                ItemStack before = recipient.copy();
                int capacity = WandAccess.capacityCentivis(recipient, source.aspect()).orElse(0);
                AspectOrbPickupEvent event = new AspectOrbPickupEvent(player, recipient, source, capacity, originalCredit);
                NeoForge.EVENT_BUS.post(event);
                int credit = event.getCreditCentivis();
                if (credit <= 0 || !canCollect(player) || value != source.vis() || !getAspect().equals(source.aspect()) || slot.stack() != recipient || !ItemStack.matches(before, recipient)) {
                    return 0;
                }
                var quote = WandAccess.quoteCharge(player, slot, source.aspect(), credit);
                if (quote.isEmpty() || quote.get().acceptedCentivis() != credit) {
                    continue;
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    if (WandAccess.tryCharge(player, slot, quote.get(), transaction) != credit || !canCollect(player) || value != source.vis() || !getAspect().equals(source.aspect())
                            || slot.stack() != recipient) {
                        return 0;
                    }
                    transaction.commit();
                }
                player.takeXpDelay = PICKUP_DELAY_TICKS;
                player.take(this, PICKUP_ITEM_COUNT);
                playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, PICKUP_VOLUME, PICKUP_PITCH_FACTOR * ((random.nextFloat() - random.nextFloat()) * PICKUP_PITCH_SPREAD + PICKUP_PITCH_BASE));
                discard();
                return credit;
            }
            return 0;
        } finally {
            collecting = false;
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
        output.putShort(KEY_HEALTH, (short) health);
        output.putShort(KEY_AGE, (short) age);
        output.putShort(KEY_VALUE, (short) value);
        output.putString(KEY_ASPECT, entityData.get(DATA_ASPECT));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        health = input.getShortOr(KEY_HEALTH, DEFAULT_HEALTH);
        age = input.getShortOr(KEY_AGE, DEFAULT_AGE);
        value = input.getShortOr(KEY_VALUE, DEFAULT_VALUE);
        entityData.set(DATA_ASPECT, input.getStringOr(KEY_ASPECT, TTAspects.AER.identifier().toString()));
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
