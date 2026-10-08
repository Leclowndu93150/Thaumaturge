package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class EntityTaintacleGiant extends AbstractTaintacle {
    private static final EntityDataAccessor<Integer> DATA_AGGRO =
            SynchedEntityData.defineId(EntityTaintacleGiant.class, EntityDataSerializers.INT);

    private static final int GIANT_XP = 20;
    private static final int HEAL_INTERVAL = 30;

    private final BossBar bossBar = new BossBar(this);
    private final BossRage rage = new BossRage(this, DATA_AGGRO);

    public EntityTaintacleGiant(EntityType<? extends EntityTaintacleGiant> type, Level level) {
        super(type, level);
        this.xpReward = GIANT_XP;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(175.0, 9.0);
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

    @Override
    public float enrage() {
        return this.getAnger() > 0 ? 1.0F : 0.0F;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        ChampionHelper.makeChampion(this, true);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        this.rage.tick();
        if (!this.level().isClientSide() && this.tickCount % HEAL_INTERVAL == 0) {
            this.heal(1.0F);
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();
        super.customServerAiStep();
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
    public boolean hurt(DamageSource source, float damage) {
        if (!(level() instanceof ServerLevel level)) return false;
        damage = this.rage.absorb(source, damage);
        return super.hurt(source, damage);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
