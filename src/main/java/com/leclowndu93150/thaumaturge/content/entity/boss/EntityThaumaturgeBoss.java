package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.server.command.admin.ShowcaseEntities;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeBoss extends Monster {
    private static final EntityDataAccessor<Integer> DATA_AGGRO =
            SynchedEntityData.defineId(EntityThaumaturgeBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMERGENCE =
            SynchedEntityData.defineId(EntityThaumaturgeBoss.class, EntityDataSerializers.INT);

    private static final int BOSS_XP = 50;
    private static final int HOME_RADIUS = 24;
    private static final int HEAL_INTERVAL = 30;
    private static final int RETARGET_INTERVAL = 20;
    private static final int RETARGET_FLAT_MARGIN = 25;
    private static final double RETARGET_RATIO = 1.1;
    private static final double AGGRO_RANGE_SQ = 16384.0;
    private static final int MAX_PLAYER_BUFFS = 5;
    private static final double PLAYER_HP_BUFF = 50.0;
    private static final double PLAYER_DMG_BUFF = 0.5;

    protected final BossBar bossBar = new BossBar(this);
    private final BossRage rage = new BossRage(this, DATA_AGGRO);
    private final Map<Integer, Integer> aggro = new HashMap<>();
    protected int spawnTimer;

    public EntityThaumaturgeBoss(EntityType<? extends EntityThaumaturgeBoss> type, Level level) {
        super(type, level);
        this.xpReward = BOSS_XP;
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_AGGRO, 0);
        entityData.define(DATA_EMERGENCE, 0);
    }

    public int getAnger() {
        return this.rage.anger();
    }

    public void setAnger(int anger) {
        this.rage.setAnger(anger);
    }

    public int getSpawnTimer() {
        return this.level().isClientSide() ? this.entityData.get(DATA_EMERGENCE) : this.spawnTimer;
    }

    @Override
    protected void customServerAiStep() {
        if (this.getSpawnTimer() == 0) {
            super.customServerAiStep();
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
        this.setAiGate(this.getSpawnTimer() <= 0);
        this.bossBar.update();
    }

    private void setAiGate(boolean open) {
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, open);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, open);
        this.targetSelector.setControlFlag(Goal.Flag.TARGET, open);
        if (!open) {
            this.getNavigation().stop();
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!ShowcaseEntities.isDisplay(this)) {
            this.bossBar.show(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.hide(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossBar.rename();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        this.restrictTo(this.blockPosition(), HOME_RADIUS);
        this.generateName();
        this.bossBar.rename();
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.spawnTimer > 0) {
            this.spawnTimer--;
        }
        this.rage.tick();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_EMERGENCE, Math.max(0, this.spawnTimer));
            if (this.tickCount % HEAL_INTERVAL == 0) {
                this.heal(passiveHealing());
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
            if (this.isStaleAttacker(candidate)) {
                iterator.remove();
                continue;
            }
            if (candidate instanceof Player) {
                players++;
            }
            if (candidate instanceof LivingEntity living
                    && entry.getValue() > currentAggro + RETARGET_FLAT_MARGIN
                    && entry.getValue() > currentAggro * RETARGET_RATIO
                    && entry.getValue() > best) {
                newTarget = living;
                best = entry.getValue();
            }
        }
        if (newTarget != null && current != null && newTarget.getId() != current.getId() && this.canAttack(newTarget)) {
            this.setTarget(newTarget);
        }
        if (LabyrinthHelper.isLabyrinthBound(this)) {
            return;
        }
        this.applyScalingSlots(Math.min(MAX_PLAYER_BUFFS, players - 1));
    }

    private boolean isStaleAttacker(@Nullable Entity attacker) {
        if (!(attacker instanceof LivingEntity living)
                || !living.isAlive()
                || living.isRemoved()
                || living.level() != this.level()) {
            return true;
        }
        if (living instanceof Player player && (player.isSpectator() || player.isCreative())) {
            return true;
        }
        return this.distanceToSqr(living) > AGGRO_RANGE_SQ;
    }

    private void applyScalingSlots(int slots) {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health == null || damage == null) {
            return;
        }
        float oldMax = this.getMaxHealth();
        float healthFraction = oldMax > 0.0F ? this.getHealth() / oldMax : 1.0F;
        boolean healthChanged = false;
        for (int slot = 0; slot < MAX_PLAYER_BUFFS; slot++) {
            boolean enabled = slot < slots;
            healthChanged |= this.syncModifier(health, hpBuffId(slot), PLAYER_HP_BUFF, enabled);
            this.syncModifier(damage, dmgBuffId(slot), PLAYER_DMG_BUFF, enabled);
        }
        if (healthChanged && this.isAlive()) {
            this.setHealth(healthFraction * this.getMaxHealth());
        }
    }

    private boolean syncModifier(AttributeInstance instance, ResourceLocation id, double amount, boolean wanted) {
        if (instance.hasModifier(id) == wanted) {
            return false;
        }
        if (wanted) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        } else {
            instance.removeModifier(id);
        }
        return true;
    }

    private static ResourceLocation hpBuffId(int slot) {
        return TTIds.rl("boss_hp_buff_" + slot);
    }

    private static ResourceLocation dmgBuffId(int slot) {
        return TTIds.rl("boss_dmg_buff_" + slot);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (ShowcaseEntities.isDisplay(this)) {
            return false;
        }
        float aggroDamage = damage;
        if (usesLegacyEnrage()) {
            damage = this.rage.absorb(source, damage);
        }
        boolean hurt = super.hurt(source, damage);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.aggro.merge(attacker.getId(), (int) aggroDamage, Integer::sum);
        }
        return hurt;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source)
                || (this.getSpawnTimer() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
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
    public boolean isAlliedTo(Entity other) {
        return other.getType().is(ThaumaturgeEntityTypeTags.ELDRITCH) || super.isAlliedTo(other);
    }

    protected boolean usesLegacyEnrage() {
        return true;
    }

    protected float passiveHealing() {
        return 1.0F;
    }

    public void generateName() {}
}
