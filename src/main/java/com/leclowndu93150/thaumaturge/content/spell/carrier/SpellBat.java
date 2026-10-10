package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class SpellBat extends Monster implements TraceableEntity, IEntityWithComplexSpawn {
    private static final String OWNER_KEY = "owner";
    private static final String ALLIES_KEY = "allies";
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 1.0;
    private static final int MAX_AGE = 600;
    private static final SoundEvent AMBIENT_SOUND = SoundEvents.BAT_AMBIENT;
    private static final SoundEvent HURT_SOUND = SoundEvents.BAT_HURT;
    private static final SoundEvent DEATH_SOUND = SoundEvents.BAT_DEATH;
    private static final double TARGET_RANGE = 12.0;
    private static final double ROOST_ARRIVAL = 2.0;
    private static final int ROOST_SPAN = 6;
    private static final int ROOST_WIDTH = 2 * ROOST_SPAN + 1;
    private static final int ROOST_DEPTH_BELOW = 2;
    private static final int ROOST_HEIGHT = 6;
    private static final double STRIKE_REACH = 2.5;
    private static final double STRIKE_WIDTH_FACTOR = 1.1;
    private static final int STRIKE_COOLDOWN = 40;
    private static final float STRIKE_COST = 1.0F;
    private static final double CRUISE_HORIZONTAL = 0.5;
    private static final double CRUISE_VERTICAL = 0.7;
    private static final double VERTICAL_DAMPING = 0.4;
    private static final double STEER_RATE = 0.3;
    private static final float FORWARD_INPUT = 0.5F;
    private static final float QUARTER_TURN = 90.0F;
    private static final int ROOST_REROLL_ODDS = 30;
    private static final double AURA_SPREAD = 0.3;
    private static final double BODY_HALF = 0.5;
    private static final float SOUND_VOLUME = 0.1F;
    private static final float STRIKE_VOLUME = 0.6F;
    private static final float STRIKE_PITCH_BASE = 1.4F;
    private static final float STRIKE_PITCH_RANGE = 0.3F;
    private static final float DEGREES_PER_RADIAN = 180.0F / (float) Math.PI;

    public final AnimationState flyAnimationState = new AnimationState();

    private final CarrierCharge charge = new CarrierCharge();
    private @Nullable EntityReference<LivingEntity> owner;
    private boolean allies;
    private int strikeReadyAt;
    private @Nullable BlockPos roost;
    private @Nullable LivingEntity prey;

    public SpellBat(EntityType<? extends SpellBat> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static void release(ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, boolean allies) {
        SpellBat bat = TTEntities.SPELL_BAT.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (bat == null) {
            return;
        }
        bat.charge.arm(payload);
        bat.owner = EntityReference.of(owner);
        bat.allies = allies;
        Vec3 at = origin.position();
        bat.snapTo(at.x, at.y, at.z, owner.getYRot(), 0.0F);
        level.addFreshEntity(bat);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    public int color() {
        return charge.look().color();
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(owner, level());
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {}

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMBIENT_SOUND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return HURT_SOUND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DEATH_SOUND;
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(ALLIES_KEY, allies);
        EntityReference.store(owner, output, OWNER_KEY);
        charge.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        allies = input.getBooleanOr(ALLIES_KEY, false);
        owner = EntityReference.read(input, OWNER_KEY);
        charge.load(input);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity master = getOwner();
        return master == null ? super.getTeam() : master.getTeam();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        LivingEntity master = getOwner();
        if (master != null && (other == master || master.isAlliedTo(other))) {
            return true;
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!allies) {
            super.doPush(entity);
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && shouldExpire()) {
            discard();
            return;
        }
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(1.0, VERTICAL_DAMPING, 1.0));
        if (level().isClientSide()) {
            flyAnimationState.startIfStopped(tickCount);
            emitAura();
        }
    }

    private void emitAura() {
        Vec3 centre = getBoundingBox().getCenter();
        double spread = AURA_SPREAD + getBbWidth() * BODY_HALF;
        Vec3 at = centre.add(random.nextGaussian() * spread, random.nextGaussian() * spread, random.nextGaussian() * spread);
        CarrierPayload.particle(level(), charge.look(), at, Vec3.ZERO);
    }

    private boolean shouldExpire() {
        return level().getDifficulty() == Difficulty.PEACEFUL || tickCount > MAX_AGE || charge.isSpent() || getOwner() == null;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity master = getOwner();
        if (master != null) {
            hunt(level, master);
        }
    }

    private void hunt(ServerLevel level, LivingEntity master) {
        if (prey != null && !valid(prey)) {
            prey = null;
        }
        if (prey == null) {
            prey = SpellTargeting.nearest(level, position(), TARGET_RANGE, candidate -> valid(candidate) && SpellTargeting.isAlly(master, candidate) == allies).orElse(null);
        }
        if (prey == null) {
            fly(roostGoal(level));
        } else {
            pursue(level, prey);
        }
    }

    private void pursue(ServerLevel level, LivingEntity target) {
        Vec3 aim = target.getBoundingBox().getCenter();
        fly(aim);
        double reach = Math.max(STRIKE_REACH, STRIKE_WIDTH_FACTOR * target.getBbWidth());
        boolean ready = tickCount >= strikeReadyAt;
        if (ready && getBoundingBox().getCenter().distanceTo(aim) <= reach && overlapsHeight(target) && hasLineOfSight(target)) {
            strike(level, target, aim);
        }
    }

    private boolean overlapsHeight(LivingEntity target) {
        return getBoundingBox().minY < target.getBoundingBox().maxY && target.getBoundingBox().minY < getBoundingBox().maxY;
    }

    private void strike(ServerLevel level, LivingEntity target, Vec3 aim) {
        strikeReadyAt = tickCount + STRIKE_COOLDOWN;
        Vec3 heading = aim.subtract(getBoundingBox().getCenter());
        charge.resume(level, List.of(SpellTarget.entity(target, heading)));
        float pitch = STRIKE_PITCH_BASE + random.nextFloat() * STRIKE_PITCH_RANGE;
        level.playSound(null, getX(), getY(), getZ(), AMBIENT_SOUND, SoundSource.HOSTILE, STRIKE_VOLUME, pitch);
        float left = getHealth() - STRIKE_COST;
        if (left > 0.0F) {
            setHealth(left);
        } else {
            kill(level);
        }
    }

    private void fly(Vec3 goal) {
        Vec3 motion = getDeltaMovement();
        double pullX = (Math.signum(goal.x - getX()) * CRUISE_HORIZONTAL - motion.x) * STEER_RATE;
        double pullY = (Math.signum(goal.y - getY()) * CRUISE_VERTICAL - motion.y) * STEER_RATE;
        double pullZ = (Math.signum(goal.z - getZ()) * CRUISE_HORIZONTAL - motion.z) * STEER_RATE;
        Vec3 next = motion.add(pullX, pullY, pullZ);
        setDeltaMovement(next);
        float yaw = (float) Mth.atan2(next.z, next.x) * DEGREES_PER_RADIAN - QUARTER_TURN;
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        zza = FORWARD_INPUT;
    }

    private Vec3 roostGoal(ServerLevel level) {
        if (roost != null && (!level.isEmptyBlock(roost) || roost.getY() <= level.getMinY())) {
            roost = null;
        }
        if (roost == null || roost.closerToCenterThan(position(), ROOST_ARRIVAL) || random.nextInt(ROOST_REROLL_ODDS) == 0) {
            int dx = random.nextInt(ROOST_WIDTH) - ROOST_SPAN;
            int dy = random.nextInt(ROOST_HEIGHT) - ROOST_DEPTH_BELOW;
            int dz = random.nextInt(ROOST_WIDTH) - ROOST_SPAN;
            roost = BlockPos.containing(getX() + dx, getY() + dy, getZ() + dz);
        }
        return Vec3.atCenterOf(roost);
    }

    private boolean valid(LivingEntity candidate) {
        if (candidate == this || candidate instanceof SpellBat || !candidate.isAlive()) {
            return false;
        }
        return allies || !(candidate instanceof Player player && player.getAbilities().invulnerable);
    }

}
