package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
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
    }

    public int getAnger() {
        return this.rage.anger();
    }

    public void setAnger(int anger) {
        this.rage.setAnger(anger);
    }

    public int getSpawnTimer() {
        return this.spawnTimer;
    }

    @Override
    protected void customServerAiStep() {
        if (this.getSpawnTimer() == 0) {
            super.customServerAiStep();
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
        this.bossBar.update();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.show(player);
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
        if (this.getSpawnTimer() > 0) {
            this.spawnTimer--;
        }
        this.rage.tick();
        if (!this.level().isClientSide()) {
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
            if (candidate == null || !candidate.isAlive() || this.distanceToSqr(candidate) > AGGRO_RANGE_SQ) {
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
        if (newTarget != null && current != null && newTarget.getId() != current.getId()) {
            this.setTarget(newTarget);
        }
        if (LabyrinthHelper.isLabyrinthBound(this)) {
            return;
        }
        float oldMax = this.getMaxHealth();
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        for (int slot = 0; slot < MAX_PLAYER_BUFFS; slot++) {
            health.removeModifier(hpBuffId(slot));
            damage.removeModifier(dmgBuffId(slot));
        }
        for (int slot = 0; slot < Math.min(MAX_PLAYER_BUFFS, players - 1); slot++) {
            health.addTransientModifier(
                    new AttributeModifier(hpBuffId(slot), PLAYER_HP_BUFF, AttributeModifier.Operation.ADD_VALUE));
            damage.addTransientModifier(
                    new AttributeModifier(dmgBuffId(slot), PLAYER_DMG_BUFF, AttributeModifier.Operation.ADD_VALUE));
        }
        this.setHealth(this.getHealth() * this.getMaxHealth() / oldMax);
    }

    private static ResourceLocation hpBuffId(int slot) {
        return TTIds.rl("boss_hp_buff_" + slot);
    }

    private static ResourceLocation dmgBuffId(int slot) {
        return TTIds.rl("boss_dmg_buff_" + slot);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.aggro.merge(attacker.getId(), (int) damage, Integer::sum);
        }
        if (usesLegacyEnrage()) {
            damage = this.rage.absorb(source, damage);
        }
        return super.hurt(source, damage);
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
