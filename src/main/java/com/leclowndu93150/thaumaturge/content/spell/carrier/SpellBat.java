package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.entity.IBatAnimated;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class SpellBat extends Monster implements TraceableEntity, IEntityWithComplexSpawn, IBatAnimated {
    private static final int LIFESPAN = 600;
    private static final int STRIKE_COOLDOWN = 40;
    private static final double HUNT_RANGE = 12.0;
    private static final float STRIKE_REACH = 2.5F;
    private static final double LIFT_DRAG = 0.6;
    private static final double STEER = 0.1;
    private static final double CRUISE_XZ = 0.5;
    private static final double CRUISE_Y = 0.7;
    private static final int WANDER_RADIUS = 6;
    private static final int RETHINK_ONE_IN = 30;
    private static final double ARRIVED = 2.0;
    private static final float STRIKE_COST = 1.0F;
    private static final double AURA_SPREAD = 0.125;
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 1.0;
    private static final float SOUND_VOLUME = 0.1F;
    private static final float STRIKE_WIDTH_REACH = 1.1F;
    private static final float HURT_VOLUME = 0.5F;
    private static final float HURT_PITCH = 0.9F;
    private static final float HURT_PITCH_SPREAD = 0.2F;
    private static final int WANDER_HEIGHT = 6;
    private static final double WANDER_DROP = 2.0;
    private static final float MODEL_YAW_OFFSET = 90.0F;
    private static final float FORWARD_INPUT = 0.5F;

    public final AnimationState flyAnimationState = new AnimationState();

    private final CarrierCharge charge = new CarrierCharge();
    private @Nullable UUID owner;
    private boolean allies;
    private @Nullable BlockPos roost;
    private int recharge;

    public SpellBat(EntityType<? extends SpellBat> type, Level level) {
        super(type, level);
    }

    public static void release(
            ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, boolean allies) {
        SpellBat bat = TTEntities.SPELL_BAT.get().create(level);
        if (bat == null) {
            return;
        }
        bat.charge.arm(payload);
        bat.owner = owner.getUUID();
        bat.allies = allies;
        bat.moveTo(origin.position().x, origin.position().y, origin.position().z, owner.getYRot(), 0.0F);
        level.addFreshEntity(bat);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    public int color() {
        return charge.look().color();
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        Entity resolved = owner == null
                ? null
                : level() instanceof ServerLevel server ? server.getEntity(owner) : level().getPlayerByUUID(owner);
        return resolved instanceof LivingEntity living ? living : null;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        charge.save(output, registries);
        if (owner != null) output.putUUID("owner", owner);
        output.putBoolean("allies", allies);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        charge.load(input, registries);
        owner = input.hasUUID("owner") ? input.getUUID("owner") : null;
        allies = (input.contains("allies") ? input.getBoolean("allies") : false);
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity master = getOwner();
        return master != null ? master.getTeam() : super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        LivingEntity master = getOwner();
        return master != null
                ? other == master || master.isAlliedTo(other) || other.isAlliedTo(master)
                : super.isAlliedTo(other);
    }

    @Override
    protected void checkFallDamage(double fall, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x, motion.y * LIFT_DRAG, motion.z);
        flyAnimationState.startIfStopped(tickCount);
        if (level().isClientSide()) {
            CarrierPayload.particle(
                    level(),
                    charge.look(),
                    position()
                            .add(
                                    random.nextGaussian() * AURA_SPREAD,
                                    getBbHeight() / 2.0 + random.nextGaussian() * AURA_SPREAD,
                                    random.nextGaussian() * AURA_SPREAD),
                    Vec3.ZERO);
        } else if (tickCount > LIFESPAN || getOwner() == null || charge.isSpent()) {
            discard();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        if (recharge > 0) {
            recharge--;
        }
        LivingEntity prey = getTarget();
        if (prey != null
                && (!prey.isAlive()
                        || prey instanceof Player player && player.getAbilities().invulnerable && !allies)) {
            setTarget(null);
            prey = null;
        }
        if (prey == null) {
            prey = hunt(level).orElse(null);
            setTarget(prey);
        }
        if (prey == null) {
            wander(level);
            return;
        }
        Vec3 aim = prey.getBoundingBox().getCenter();
        flyToward(aim);
        if (recharge == 0
                && distanceTo(prey) < Math.max(STRIKE_REACH, prey.getBbWidth() * STRIKE_WIDTH_REACH)
                && hasLineOfSight(prey)) {
            strike(level, prey, aim);
        }
    }

    private Optional<LivingEntity> hunt(ServerLevel level) {
        LivingEntity master = getOwner();
        return SpellTargeting.nearest(
                level,
                position(),
                HUNT_RANGE,
                living -> living != this
                        && allies == SpellTargeting.isAlly(master, living)
                        && !(living instanceof SpellBat));
    }

    private void strike(ServerLevel level, LivingEntity prey, Vec3 aim) {
        recharge = STRIKE_COOLDOWN;
        charge.resume(level, List.of(SpellTarget.entity(prey, aim.subtract(position()))));
        setHealth(getHealth() - STRIKE_COST);
        playSound(SoundEvents.BAT_HURT, HURT_VOLUME, HURT_PITCH + random.nextFloat() * HURT_PITCH_SPREAD);
    }

    private void wander(ServerLevel level) {
        if (roost != null && (!level.isEmptyBlock(roost) || roost.getY() <= level.getMinBuildHeight())) {
            roost = null;
        }
        if (roost == null || random.nextInt(RETHINK_ONE_IN) == 0 || roost.closerToCenterThan(position(), ARRIVED)) {
            roost = BlockPos.containing(
                    getX() + random.nextInt(WANDER_RADIUS + 1) - random.nextInt(WANDER_RADIUS + 1),
                    getY() + random.nextInt(WANDER_HEIGHT) - WANDER_DROP,
                    getZ() + random.nextInt(WANDER_RADIUS + 1) - random.nextInt(WANDER_RADIUS + 1));
        }
        flyToward(Vec3.atCenterOf(roost));
    }

    private void flyToward(Vec3 goal) {
        Vec3 offset = goal.subtract(position());
        Vec3 motion = getDeltaMovement();
        Vec3 steered = motion.add(
                (Math.signum(offset.x) * CRUISE_XZ - motion.x) * STEER,
                (Math.signum(offset.y) * CRUISE_Y - motion.y) * STEER,
                (Math.signum(offset.z) * CRUISE_XZ - motion.z) * STEER);
        setDeltaMovement(steered);
        float yaw = (float) (Mth.atan2(steered.z, steered.x) * Mth.RAD_TO_DEG) - MODEL_YAW_OFFSET;
        setYRot(getYRot() + Mth.wrapDegrees(yaw - getYRot()));
        zza = FORWARD_INPUT;
    }

    private final AnimationState restAnimationState = new AnimationState();

    @Override
    public AnimationState flyAnimation() {
        return flyAnimationState;
    }

    @Override
    public AnimationState restAnimation() {
        return restAnimationState;
    }

    @Override
    public boolean isResting() {
        return false;
    }
}
