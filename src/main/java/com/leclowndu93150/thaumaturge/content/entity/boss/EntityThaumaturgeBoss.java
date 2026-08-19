package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeBoss extends Monster {
    private static final EntityDataAccessor<Integer> DATA_AGGRO = SynchedEntityData.defineId(EntityThaumaturgeBoss.class, EntityDataSerializers.INT);

    private static final int BOSS_XP = 50;
    private static final int HOME_RADIUS = 24;
    private static final int HEAL_INTERVAL = 30;
    private static final int RETARGET_INTERVAL = 20;
    private static final int RETARGET_FLAT_MARGIN = 25;
    private static final double RETARGET_RATIO = 1.1;
    private static final double AGGRO_RANGE_SQ = 16384.0;
    private static final float ENRAGE_THRESHOLD = 35.0F;
    private static final int ENRAGE_TICKS = 200;
    private static final float ENRAGE_REGEN_DIVISOR = 15.0F;
    private static final float ENRAGE_STRENGTH_DIVISOR = 10.0F;
    private static final float ENRAGE_HASTE_DIVISOR = 40.0F;
    private static final int ANGRY_PARTICLE_CHANCE = 15;
    private static final int MAX_PLAYER_BUFFS = 5;
    private static final double PLAYER_HP_BUFF = 50.0;
    private static final double PLAYER_DMG_BUFF = 0.5;
    private static final float PEARL_DROP_LIFT = 1.5F;

    protected final ServerBossEvent bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private final Map<Integer, Integer> aggro = new HashMap<>();
    protected int spawnTimer;

    public EntityThaumaturgeBoss(EntityType<? extends EntityThaumaturgeBoss> type, Level level) {
        super(type, level);
        this.xpReward = BOSS_XP;
        this.bossEvent.setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 0.95).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_AGGRO, 0);
    }

    public int getAnger() {
        return this.entityData.get(DATA_AGGRO);
    }

    public void setAnger(int anger) {
        this.entityData.set(DATA_AGGRO, anger);
    }

    public int getSpawnTimer() {
        return this.spawnTimer;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.getSpawnTimer() == 0) {
            super.customServerAiStep(level);
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        this.setHomeTo(this.blockPosition(), HOME_RADIUS);
        this.generateName();
        this.bossEvent.setName(this.getDisplayName());
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getSpawnTimer() > 0) {
            this.spawnTimer--;
        }
        if (this.getAnger() > 0 && !this.level().isClientSide()) {
            this.setAnger(this.getAnger() - 1);
        }
        if (this.level().isClientSide() && this.getAnger() > 0 && this.random.nextInt(ANGRY_PARTICLE_CHANCE) == 0) {
            this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                    this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5, this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                    this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
        }
        if (!this.level().isClientSide()) {
            if (this.tickCount % HEAL_INTERVAL == 0) {
                this.heal(1.0F);
            }
            if (this.getTarget() != null && this.tickCount % RETARGET_INTERVAL == 0) {
                this.retargetAndBuff();
            }
        }
    }

    private void retargetAndBuff() {
        LivingEntity current = this.getTarget();
        int currentAggro = current == null ? 0 : this.aggro.getOrDefault(current.getId(), 0);
        int best = currentAggro;
        LivingEntity newTarget = null;
        int players = 0;
        Iterator<Map.Entry<Integer, Integer>> iterator = this.aggro.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            Entity candidate = this.level().getEntity(entry.getKey());
            if (candidate == null || !candidate.isAlive() || this.distanceToSqr(candidate) > AGGRO_RANGE_SQ) {
                iterator.remove();
                continue;
            }
            if (candidate instanceof Player) {
                players++;
            }
            if (candidate instanceof LivingEntity living && entry.getValue() > currentAggro + RETARGET_FLAT_MARGIN && entry.getValue() > currentAggro * RETARGET_RATIO && entry.getValue() > best) {
                newTarget = living;
                best = entry.getValue();
            }
        }
        if (newTarget != null && current != null && newTarget.getId() != current.getId()) {
            this.setTarget(newTarget);
        }
        float oldMax = this.getMaxHealth();
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        for (int slot = 0; slot < MAX_PLAYER_BUFFS; slot++) {
            health.removeModifier(hpBuffId(slot));
            damage.removeModifier(dmgBuffId(slot));
        }
        for (int slot = 0; slot < Math.min(MAX_PLAYER_BUFFS, players - 1); slot++) {
            health.addTransientModifier(new AttributeModifier(hpBuffId(slot), PLAYER_HP_BUFF, AttributeModifier.Operation.ADD_VALUE));
            damage.addTransientModifier(new AttributeModifier(dmgBuffId(slot), PLAYER_DMG_BUFF, AttributeModifier.Operation.ADD_VALUE));
        }
        this.setHealth(this.getHealth() * this.getMaxHealth() / oldMax);
    }

    private static Identifier hpBuffId(int slot) {
        return Identifier.fromNamespaceAndPath("thaumaturge", "boss_hp_buff_" + slot);
    }

    private static Identifier dmgBuffId(int slot) {
        return Identifier.fromNamespaceAndPath("thaumaturge", "boss_dmg_buff_" + slot);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.aggro.merge(attacker.getId(), (int) damage, Integer::sum);
        }
        if (damage > ENRAGE_THRESHOLD && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (this.getAnger() == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ENRAGE_TICKS, (int) (damage / ENRAGE_REGEN_DIVISOR)));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, ENRAGE_TICKS, (int) (damage / ENRAGE_STRENGTH_DIVISOR)));
                this.addEffect(new MobEffectInstance(MobEffects.HASTE, ENRAGE_TICKS, (int) (damage / ENRAGE_HASTE_DIVISOR)));
                this.setAnger(ENRAGE_TICKS);
                if (source.getEntity() instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty().append(this.getDisplayName()).append(" ").append(Component.translatable("tc.boss.enrage"))));
                }
            }
            damage = ENRAGE_THRESHOLD;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source) || (this.getSpawnTimer() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.getSpawnTimer() <= 0;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean considersEntityAsAlly(Entity other) {
        return other instanceof IEldritchMob || super.considersEntityAsAlly(other);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        level.addFreshEntity(new EntitySpecialItem(level, this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(), new ItemStack(TCItems.PRIMORDIAL_PEARL.get())));
        this.spawnAtLocation(level, new ItemStack(TCItems.LOOT_BAG_RARE.get()), PEARL_DROP_LIFT);
    }

    public void generateName() {}
}
