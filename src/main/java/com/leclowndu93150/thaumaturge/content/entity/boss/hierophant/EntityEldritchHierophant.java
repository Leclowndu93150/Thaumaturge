package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import java.util.List;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityThaumaturgeBoss;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EntityEldritchHierophant extends EntityThaumaturgeBoss {
    public static final float MODEL_SCALE = 0.75F;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(EntityEldritchHierophant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(EntityEldritchHierophant.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> AWAKENED = SynchedEntityData.defineId(EntityEldritchHierophant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CAST_YAW = SynchedEntityData.defineId(EntityEldritchHierophant.class, EntityDataSerializers.FLOAT);
    private static final double MAX_HEALTH = 480.0;
    private static final double ARMOR = 8.0;
    private static final double ATTACK_DAMAGE = 10.0;
    private static final double MOVE_SPEED = 0.23;
    private static final double ENCOUNTER_RADIUS = 26.0;
    private static final double ENCOUNTER_RADIUS_SQ = ENCOUNTER_RADIUS * ENCOUNTER_RADIUS;
    private static final double APPROACH_DISTANCE_SQ = 49.0;
    private static final double CAST_DISTANCE_SQ = 144.0;
    private static final double RESET_DISTANCE_SQ = 1600.0;
    private static final int TARGET_INTERVAL = 10;
    private static final int RESET_TICKS = 200;
    private static final int RECOVERY_TICKS = 24;
    private static final int AWAKENED_RECOVERY_TICKS = 14;
    private static final int COMBO_RECOVERY_TICKS = 5;
    private static final int EFFECT_INTERVAL = 4;
    private static final int AMBIENT_INTERVAL = 100;
    private static final int MAX_MARKED_PLAYERS = 4;
    private static final int ATTACK_CYCLE = 6;
    private static final double GROUND_SEARCH_DEPTH = 12.0;
    private static final float SOUND_VOLUME = 1.8F;
    private static final float SOUND_PITCH = 0.7F;
    private static final int ARC_COLOR = 0xAC80D0;
    private static final int AMBER_COLOR = 0xC49B58;
    private static final double HAND_HEIGHT = 2.5;
    private static final double SPELL_HEIGHT = 0.12;
    private static final int DEATH_BURST_TICK = 60;
    private int cooldown = RECOVERY_TICKS;
    private int unattendedTicks;
    private int attackIndex;
    private boolean comboPending;
    private boolean rewardPending;
    private @Nullable Vec3 arenaOrigin;

    public EntityEldritchHierophant(EntityType<? extends EntityEldritchHierophant> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ARMOR, ARMOR).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVE_SPEED);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, HierophantAction.IDLE.ordinal());
        builder.define(ACTION_START, 0L);
        builder.define(AWAKENED, false);
        builder.define(CAST_YAW, 0.0F);
    }

    public HierophantAction action() {
        return HierophantAction.byId(entityData.get(ACTION));
    }
    public float actionAge(float partialTick) {
        return Math.max(0, level().getGameTime() - entityData.get(ACTION_START) + partialTick);
    }
    public boolean awakened() {
        return entityData.get(AWAKENED);
    }
    public float spellDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected boolean usesLegacyEnrage() {
        return false;
    }
    @Override
    protected float passiveHealing() {
        return 0;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        final SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        arenaOrigin = position();
        if (level instanceof ServerLevel server) {
            begin(server, HierophantAction.SUMMON);
        }
        return result;
    }

    private Vec3 arenaOrigin() {
        return arenaOrigin == null ? position() : arenaOrigin;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (arenaOrigin == null) {
            arenaOrigin = position();
        }
        if (tickCount % TARGET_INTERVAL == 0 || getTarget() == null || !validTarget(getTarget())) {
            setTarget(level.getNearestPlayer(getX(), getY(), getZ(), ENCOUNTER_RADIUS, this::validTarget));
        }
        bossEvent.setName(getDisplayName());
        bossEvent.setColor(awakened() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
        final LivingEntity target = getTarget();
        if (target == null) {
            unattendedTicks++;
            if (unattendedTicks == RESET_TICKS || distanceToSqr(arenaOrigin()) > RESET_DISTANCE_SQ || getY() < arenaOrigin().y - GROUND_SEARCH_DEPTH) {
                resetEncounter(level);
                return;
            }
        } else {
            unattendedTicks = 0;
        }
        if (action() != HierophantAction.IDLE) {
            getNavigation().stop();
            setDeltaMovement(0, getDeltaMovement().y, 0);
            final int age = (int) actionAge(0);
            if (age == action().release()) {
                release(level, action());
            }
            if (age < action().release() || action() == HierophantAction.SUMMON) {
                charge(level, age);
            }
            if (age >= action().duration()) {
                final HierophantAction finished = action();
                setAction(HierophantAction.IDLE);
                cooldown = comboPending && finished == HierophantAction.SWIPE_RIGHT ? COMBO_RECOVERY_TICKS : awakened() ? AWAKENED_RECOVERY_TICKS : RECOVERY_TICKS;
            }
            return;
        }
        if (target == null) {
            getNavigation().stop();
            return;
        }
        getLookControl().setLookAt(target, 12, 12);
        if (getHealth() <= getMaxHealth() / 2 && !awakened()) {
            entityData.set(AWAKENED, true);
            comboPending = false;
            speak("entity.thaumaturge.eldritch_hierophant.awaken");
            begin(level, HierophantAction.NOVA);
            return;
        }
        if (distanceToSqr(target) > APPROACH_DISTANCE_SQ && tickCount % TARGET_INTERVAL == 0) {
            getNavigation().moveTo(target, 1.0);
        } else if (distanceToSqr(target) <= APPROACH_DISTANCE_SQ) {
            getNavigation().stop();
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!hasLineOfSight(target)) {
            return;
        }
        faceTarget(target);
        if (comboPending) {
            comboPending = false;
            begin(level, HierophantAction.SWIPE_LEFT);
            return;
        }
        final int choice = attackIndex++ % ATTACK_CYCLE;
        if (choice == ATTACK_CYCLE - 1) {
            begin(level, HierophantAction.NOVA);
        } else if (choice == ATTACK_CYCLE - 2) {
            begin(level, HierophantAction.THROW);
        } else if (choice == 2 || distanceToSqr(target) > CAST_DISTANCE_SQ) {
            begin(level, HierophantAction.CAST);
        } else {
            final boolean right = choice % 2 == 0;
            comboPending = right && awakened();
            begin(level, right ? HierophantAction.SWIPE_RIGHT : HierophantAction.SWIPE_LEFT);
        }
    }

    private boolean validTarget(Entity target) {
        return target instanceof Player player && player.isAlive() && !player.isSpectator() && !player.getAbilities().instabuild && player.distanceToSqr(arenaOrigin()) <= ENCOUNTER_RADIUS_SQ;
    }

    private void faceTarget(LivingEntity target) {
        final Vec3 delta = target.position().subtract(position());
        setYRot((float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F);
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    private void setAction(HierophantAction action) {
        entityData.set(ACTION, action.ordinal());
        entityData.set(ACTION_START, level().getGameTime());
        entityData.set(CAST_YAW, getYRot());
        spawnTimer = action == HierophantAction.SUMMON ? action.duration() : 0;
    }

    private void begin(ServerLevel level, HierophantAction action) {
        setAction(action);
        final SoundEvent sound = switch (action) {
            case SUMMON, CAST -> TTSounds.CHANT.get();
            case NOVA -> TTSounds.EGSCREECH.get();
            default -> TTSounds.RUNICSHIELDCHARGE.get();
        };
        level.playSound(null, blockPosition(), sound, SoundSource.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
    }

    private Vec3 releaseHand(boolean left) {
        return position().add(HierophantSockets.worldOffset(action(), left, entityData.get(CAST_YAW), MODEL_SCALE * getScale()));
    }

    private Vec3 aimFrom(Vec3 origin) {
        final LivingEntity target = getTarget();
        return target == null ? Vec3.directionFromRotation(0, entityData.get(CAST_YAW)) : target.getBoundingBox().getCenter().subtract(origin).normalize();
    }

    private void charge(ServerLevel level, int age) {
        if (age % EFFECT_INTERVAL != 0) {
            return;
        }
        final float progress = action().release() > 0 ? (float) age / action().release() : (float) age / action().duration();
        if (action() == HierophantAction.NOVA || action() == HierophantAction.SUMMON) {
            final double radius = EntityHierophantNova.START_RADIUS + (1 - progress) * 2;
            final double angle = age * 0.4;
            final Vec3 point = position().add(Math.cos(angle) * radius, SPELL_HEIGHT, Math.sin(angle) * radius);
            Effects.blockRunes(level, point).color(0.65F, 0.4F, 0.8F).duration(16).send();
            level.sendParticles(new DustParticleOptions(AMBER_COLOR, 1.4F), point.x, point.y, point.z, 2, 0.1, 0.1, 0.1, 0);
        }
    }

    private void release(ServerLevel level, HierophantAction action) {
        switch (action) {
            case SWIPE_LEFT, SWIPE_RIGHT -> {
                final boolean left = action == HierophantAction.SWIPE_LEFT;
                final Vec3 hand = releaseHand(left);
                castSpell(TTIds.ELDRITCH_CRESCENT, hand, aimFrom(hand), 0.9F, left);
                level.playSound(null, blockPosition(), TTSounds.WIND.get(), SoundSource.HOSTILE, SOUND_VOLUME, action == HierophantAction.SWIPE_LEFT ? 0.85F : 0.65F);
            }
            case THROW -> {
                final Vec3 hand = releaseHand(false);
                castSpell(TTIds.ELDRITCH_HAMMER, hand, aimFrom(hand), 1.6F, false);
                level.playSound(null, blockPosition(), TTSounds.WIND.get(), SoundSource.HOSTILE, SOUND_VOLUME, 0.5F);
            }
            case CAST -> {
                int marked = 0;
                for (ServerPlayer player : level.players()) {
                    if (validTarget(player) && hasLineOfSight(player) && marked++ < MAX_MARKED_PLAYERS) {
                        mark(level, player.position());
                    }
                }
                if (awakened() && getTarget() != null) {
                    final Vec3 direction = getTarget().position().subtract(position()).multiply(1, 0, 1).normalize();
                    final Vec3 side = new Vec3(direction.z, 0, -direction.x).scale(EntityHierophantSigil.RADIUS * 2);
                    mark(level, getTarget().position().add(side));
                    mark(level, getTarget().position().subtract(side));
                }
                level.playSound(null, blockPosition(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
            }
            case NOVA -> {
                castSpell(TTIds.ELDRITCH_NOVA, ground(level, position()), Vec3.directionFromRotation(0, getYRot()), 1.2F, false);
                level.playSound(null, blockPosition(), TTSounds.SHOCK.get(), SoundSource.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
                Effects.bamf(level, position().add(0, HAND_HEIGHT, 0)).color(0.4F, 0.2F, 0.55F).send();
            }
            default -> {
            }
        }
    }

    private void mark(ServerLevel level, Vec3 position) {
        if (position.distanceToSqr(arenaOrigin()) > ENCOUNTER_RADIUS_SQ) {
            return;
        }
        final Vec3 seal = ground(level, position);
        Effects.arcBolt(level, releaseHand(false)).to(seal.add(0, 0.5, 0)).color(ARC_COLOR).send();
        castSpell(TTIds.ELDRITCH_SIGIL, seal, Vec3.directionFromRotation(0, getYRot()), 1.4F, false);
    }

    private void castSpell(Identifier medium, Vec3 origin, Vec3 direction, float power, boolean left) {
        final SpellNode rend = SpellNode.of(ResourceKey.create(SpellPart.REGISTRY_KEY, TTIds.ELDRITCH_REND));
        final SpellNode carrier = SpellNode.of(ResourceKey.create(SpellPart.REGISTRY_KEY, medium)).withSetting("left", left ? 1 : 0).then(rend);
        final Spell spell = new Spell(CastStyle.INSTANT, SpellNode.of(Spell.ORIGIN).then(carrier));
        final SpellTarget start = new SpellTarget(BlockHitResult.miss(origin, Direction.UP, BlockPos.containing(origin)), origin, direction);
        Spells.cast(this, ItemStack.EMPTY, spell, List.of(start), spellDamage() * power);
    }

    private Vec3 ground(ServerLevel level, Vec3 at) {
        final BlockHitResult hit = level.clip(new ClipContext(at.add(0, 1, 0), at.add(0, -GROUND_SEARCH_DEPTH, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? at.add(0, SPELL_HEIGHT, 0) : hit.getLocation().add(0, SPELL_HEIGHT, 0);
    }

    private void resetEncounter(ServerLevel level) {
        getNavigation().stop();
        setTarget(null);
        final Vec3 origin = arenaOrigin();
        teleportTo(origin.x, origin.y, origin.z);
        setDeltaMovement(Vec3.ZERO);
        setHealth(getMaxHealth());
        entityData.set(AWAKENED, false);
        comboPending = false;
        attackIndex = 0;
        cooldown = RECOVERY_TICKS;
        begin(level, HierophantAction.SUMMON);
    }

    @Override
    public void tick() {
        super.tick();
        if (action() != HierophantAction.IDLE) {
            setYRot(entityData.get(CAST_YAW));
            yBodyRot = getYRot();
            yHeadRot = getYRot();
        }
        if (isAlive() && getTarget() != null && tickCount % AMBIENT_INTERVAL == 0 && level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), TTSounds.EGIDLE.get(), SoundSource.HOSTILE, 0.8F, 0.6F);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel) {
            getNavigation().stop();
            setAction(HierophantAction.DEATH);
            bossEvent.setProgress(0);
            speak("entity.thaumaturge.eldritch_hierophant.defeat");
        }
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        setDeltaMovement(Vec3.ZERO);
        if (level() instanceof ServerLevel server) {
            if (deathTime >= DEATH_BURST_TICK && rewardPending) {
                rewardPending = false;
                super.dropCustomDeathLoot(server, damageSources().generic(), true);
            }
            if (deathTime == DEATH_BURST_TICK) {
                Effects.bamf(server, position().add(0, 1, 0)).color(0.65F, 0.4F, 0.8F).withSound().fancy().send();
                server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1, getZ(), 60, 1, 1, 1, 0.05);
            }
            if (deathTime >= HierophantAction.DEATH.duration()) {
                bossEvent.removeAllPlayers();
                remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        rewardPending = true;
    }

    private void speak(String key) {
        for (ServerPlayer player : bossEvent.getPlayers()) {
            player.sendSystemMessage(Component.translatable(key));
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.EGATTACK.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.EGDEATH.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("ArenaOrigin", Vec3.CODEC, arenaOrigin());
        output.store("Awakened", Codec.BOOL, awakened());
        output.store("AttackIndex", Codec.INT, attackIndex);
        output.store("RewardPending", Codec.BOOL, rewardPending);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        arenaOrigin = input.read("ArenaOrigin", Vec3.CODEC).orElse(position());
        entityData.set(AWAKENED, input.read("Awakened", Codec.BOOL).orElse(false));
        attackIndex = Math.max(0, input.read("AttackIndex", Codec.INT).orElse(0));
        rewardPending = input.read("RewardPending", Codec.BOOL).orElse(false);
        setAction(getHealth() <= 0 ? HierophantAction.DEATH : HierophantAction.IDLE);
        if (getHealth() <= 0) {
            entityData.set(ACTION_START, level().getGameTime() - deathTime);
        }
        cooldown = RECOVERY_TICKS;
    }
}
