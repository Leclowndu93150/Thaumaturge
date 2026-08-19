package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class EntityMindSpider extends Spider implements IEldritchMob {
    private static final EntityDataAccessor<Boolean> DATA_HARMLESS = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_VIEWER = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.STRING);

    private static final int XP_REWARD = 1;
    private static final int HARMLESS_LIFESPAN_TICKS = 1200;
    private static final float VOICE_PITCH = 0.7F;

    private int lifeSpan = Integer.MAX_VALUE;

    public EntityMindSpider(EntityType<? extends EntityMindSpider> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_HARMLESS, false);
        entityData.define(DATA_VIEWER, "");
    }

    public String getViewer() {
        return this.entityData.get(DATA_VIEWER);
    }

    public void setViewer(String player) {
        this.entityData.set(DATA_VIEWER, player);
    }

    public boolean isHarmless() {
        return this.entityData.get(DATA_HARMLESS);
    }

    public void setHarmless(boolean harmless) {
        if (harmless) {
            this.lifeSpan = HARMLESS_LIFESPAN_TICKS;
        }
        this.entityData.set(DATA_HARMLESS, harmless);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            passenger.stopRiding();
            passenger.discard();
        }
        return result;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return isHarmless() ? 0 : super.getBaseExperienceReward(level);
    }

    @Override
    public float getVoicePitch() {
        return VOICE_PITCH;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return !isHarmless() && super.doHurtTarget(level, target);
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > this.lifeSpan) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("harmless", isHarmless());
        output.putString("viewer", getViewer());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setHarmless(input.getBooleanOr("harmless", false));
        setViewer(input.getStringOr("viewer", ""));
    }
}
